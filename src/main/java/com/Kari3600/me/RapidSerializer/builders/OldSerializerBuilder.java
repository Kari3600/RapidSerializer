package com.Kari3600.me.RapidSerializer.builders;

import com.Kari3600.me.RapidSerializer.PrimitiveSerializable;
import com.Kari3600.me.RapidSerializer.ServiceGenerator;
import com.Kari3600.me.RapidSerializer.serializers.RapidSerializer;
import com.Kari3600.me.RapidSerializer.tree.DependencyTree;
import com.Kari3600.me.RapidSerializer.util.StringUtil;
import com.squareup.javapoet.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.JavaFileObject;
import java.io.*;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class OldSerializerBuilder {
    private static final String classPackage= "com.Kari3600.me.RapidSerializer.serializers";

    private final ProcessingEnvironment processingEnv;
    private final DependencyTree tree;

    private final CodeBlock.Builder initializer = CodeBlock.builder();
    private final CodeBlock.Builder writer = CodeBlock.builder();
    private final CodeBlock.Builder reader = CodeBlock.builder();
    private final TypeSpec.Builder serializer;

    private final Set<String> visited = new HashSet<>();

    private void processField(TypeElement element, VariableElement field, String variableName) {
        String fieldVar = "FIELD_" + element.getSimpleName() + "_" + field.getSimpleName();
        if (!visited.contains(fieldVar)) {
            serializer.addField(FieldSpec.builder(ClassName.get(Field.class), fieldVar, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL).build());
            initializer.addStatement("$L = $L.class.getDeclaredField(\"$L\")", fieldVar, element.getQualifiedName(), field.getSimpleName());
            initializer.addStatement("$L.setAccessible(true)", fieldVar);
            visited.add(fieldVar);
        }
        switch (field.asType().toString()) {
            case "byte":
            case "int":
            case "float":
            case "long":
            case "boolean":
                reader.addStatement("FIELD_$L_$L.set($L, dis.read$L())",element.getSimpleName(), field.getSimpleName(), variableName, StringUtil.capitalize(field.asType().toString()));
                writer.addStatement("dos.write$L(($L) FIELD_$L_$L.get($L))",StringUtil.capitalize(field.asType().toString()), field.asType().toString(), element.getSimpleName(),field.getSimpleName(), variableName);
                break;
            case "java.lang.String":
                reader.addStatement("FIELD_$L_$L.set($L, dis.readUTF())",element.getSimpleName(), field.getSimpleName(), variableName);
                writer.addStatement("dos.writeUTF((String) FIELD_$L_$L.get(object))", element.getSimpleName(),field.getSimpleName());
                break;
            case "java.util.UUID":
                reader.addStatement("FIELD_$L_$L.set($L, new java.util.UUID(dis.readLong(),dis.readLong()))",element.getSimpleName(),field.getSimpleName(), variableName);
                writer.addStatement("dos.writeLong(((java.util.UUID) FIELD_$L_$L.get($L)).getMostSignificantBits())",element.getSimpleName(),field.getSimpleName(), variableName);
                writer.addStatement("dos.writeLong(((java.util.UUID) FIELD_$L_$L.get($L)).getLeastSignificantBits())",element.getSimpleName(),field.getSimpleName(), variableName);
                break;
            default:
                TypeElement fieldClassElement = DependencyTree.get(field.asType());
                if (fieldClassElement == null) {
                    System.err.println("Unsupported field type: " + field.asType());
                    break;
                }
                if (!processingEnv.getTypeUtils().isAssignable(field.asType(), processingEnv.getElementUtils().getTypeElement(PrimitiveSerializable.class.getCanonicalName()).asType())) {
                    reader.addStatement("$L _$L = ($L) unsafe.allocateInstance($L.class)", field.asType().toString(), field.getSimpleName(), field.asType().toString(), field.asType().toString());
                    writer.addStatement("$L _$L = ($L) $L.get($L)", field.asType().toString(), field.getSimpleName(), field.asType().toString(), fieldVar, variableName);
                    for (VariableElement fieldElement : ElementFilter.fieldsIn(fieldClassElement.getEnclosedElements())) {
                        processField(fieldClassElement, fieldElement, "_"+field.getSimpleName());
                    }
                    reader.addStatement("$L.set($L,_$L)", fieldVar, variableName, field.getSimpleName());
                }// else if (processingEnv.getElementUtils()) {

                //}

        }
    }

    private void process(TypeElement element) {
        Set<TypeElement> children = tree.getChildren(element);
        if (children.isEmpty()) {
            reader.addStatement("object = ($L) unsafe.allocateInstance($L.class)", element.getQualifiedName(), element.getQualifiedName());
        } else {
            reader.beginControlFlow("switch (dis.readByte())");
            int id = 0;
            for (TypeElement child : children) {
                reader.beginControlFlow("case " + id + ":");
                if (id == 0) {
                    writer.beginControlFlow("if (object instanceof $L)", child.getQualifiedName().toString());
                } else {
                    writer.nextControlFlow("else if (object instanceof $L)", child.getQualifiedName().toString());
                }
                writer.addStatement("dos.writeByte($L)",id);
                process(child);
                reader.addStatement("break");
                reader.endControlFlow();
                id++;
            }
            reader.beginControlFlow("default:");
            reader.addStatement("throw new IOException(\"Invalid packet ID\")");
            reader.endControlFlow();
            reader.endControlFlow();
            writer.nextControlFlow("else");
            writer.addStatement("throw new IOException(\"Invalid packet ID\")");
            writer.endControlFlow();
        }
        for (VariableElement field : ElementFilter.fieldsIn(element.getEnclosedElements())) {
            processField(element, field, "object");
        }
    }

    public OldSerializerBuilder(ProcessingEnvironment processingEnv, TypeElement element, DependencyTree tree) {
        String className = element.getSimpleName().toString()+"Serializer";

        this.processingEnv = processingEnv;
        this.tree = tree;

        serializer = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ParameterizedTypeName.get(ClassName.get(RapidSerializer.class), TypeName.get(element.asType())));

        reader.addStatement("$L object", element.getQualifiedName());

        initializer.beginControlFlow("try");

        process(element);

        initializer.nextControlFlow("catch (NoSuchFieldException e)");
        initializer.addStatement("throw new RuntimeException(\"Field not found\")");
        initializer.endControlFlow();

        reader.addStatement("return object");

        serializer.addMethod(MethodSpec.methodBuilder("serialize")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(DataOutputStream.class,"dos")
                .addParameter(TypeName.get(element.asType()),"object")
                .addException(IOException.class)
                .addException(IllegalAccessException.class)
                .addCode(writer.build())
                .build()
        );

        serializer.addMethod(MethodSpec.methodBuilder("deserialize")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(DataInputStream.class,"dis")
                .addException(IOException.class)
                .addException(ClassNotFoundException.class)
                .addException(IllegalAccessException.class)
                .addException(InstantiationException.class)
                .returns(TypeName.get(element.asType()))
                .addCode(reader.build())
                .build()
        );

        serializer.addStaticBlock(initializer.build());

        serializer.addMethod(MethodSpec.methodBuilder("getType")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.get(processingEnv.getTypeUtils().getDeclaredType(
                        processingEnv.getElementUtils().getTypeElement("java.lang.Class"),
                        element.asType()
                )))
                .addCode(CodeBlock.builder()
                        .addStatement("return $T.class", element.asType())
                        .build()
                )
                .build()
        );

        try {
            JavaFile javaFile = JavaFile.builder(classPackage, serializer.build()).indent("    ").build();
            JavaFileObject javaFileObject = processingEnv.getFiler().createSourceFile(classPackage+"."+className);
            System.out.println("Creating file: " + javaFileObject.toUri().getPath());
            try (Writer owriter = javaFileObject.openWriter()) {
                javaFile.writeTo(owriter);
            }
            ServiceGenerator.getInstance().registerService(classPackage+"."+className);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
