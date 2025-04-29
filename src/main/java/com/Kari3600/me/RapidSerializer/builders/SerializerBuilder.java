package com.Kari3600.me.RapidSerializer.builders;

import com.Kari3600.me.RapidSerializer.ServiceGenerator;
import com.Kari3600.me.RapidSerializer.serializers.RapidSerializer;
import com.squareup.javapoet.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.JavaFileObject;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public abstract class SerializerBuilder {
    protected static final String classPackage= "com.Kari3600.me.RapidSerializer.serializers";

    protected final ProcessingEnvironment processingEnv;
    protected final TypeElement mainElement;

    protected final CodeBlock.Builder initializer = CodeBlock.builder();
    protected final CodeBlock.Builder writer = CodeBlock.builder();
    protected final CodeBlock.Builder reader = CodeBlock.builder();
    protected TypeSpec.Builder serializer;

    private final Set<String> visited = new HashSet<>();

    protected void processField(TypeElement element, VariableElement field) {
        String fieldVar = "FIELD_" + element.getSimpleName() + "_" + field.getSimpleName();
        if (!visited.contains(fieldVar)) {
            serializer.addField(FieldSpec.builder(ClassName.get(Field.class), fieldVar, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL).build());
            initializer.addStatement("$L = $L.class.getDeclaredField(\"$L\")", fieldVar, element.getQualifiedName(), field.getSimpleName());
            initializer.addStatement("$L.setAccessible(true)", fieldVar);
            visited.add(fieldVar);
        }
        if (field.asType().toString().endsWith("[]")) {
            reader.addStatement("$L _$L = new $L[dis.readInt()]",field.asType(), field.getSimpleName(), field.asType().toString().replace("[]",""));
            reader.beginControlFlow("for (int i_$L = 0; i_$L < _$L.length; ++i_$L)", field.getSimpleName(), field.getSimpleName(), field.getSimpleName(), field.getSimpleName());
            reader.addStatement("_$L[i_$L] = RapidSerializer.getSerializer($L.class).deserialize(dis)", field.getSimpleName(), field.getSimpleName(), field.asType().toString().replace("[]",""));
            reader.endControlFlow();
            reader.addStatement("$L.set(object, _$L)", fieldVar, field.getSimpleName());
            writer.addStatement("$L _$L = ($L) $L.get(object)",field.asType(), field.getSimpleName(), field.asType(), fieldVar);
            writer.addStatement("dos.writeInt(_$L.length)", field.getSimpleName());
            writer.beginControlFlow("for (int i_$L = 0; i_$L < _$L.length; ++i_$L)", field.getSimpleName(), field.getSimpleName(), field.getSimpleName(), field.getSimpleName());
            writer.addStatement("RapidSerializer.getSerializer($L.class).serialize(dos, _$L[i_$L])", field.asType().toString().replace("[]",""), field.getSimpleName(), field.getSimpleName());
            writer.endControlFlow();
        } else {
            reader.addStatement("$L.set(object, RapidSerializer.getSerializer($L.class).deserialize(dis))", fieldVar, field.asType());
            writer.addStatement("RapidSerializer.getSerializer($L.class).serialize(dos, ($L) $L.get(object))",field.asType(), field.asType(), fieldVar);
        }
    }

    protected abstract void process();

    public void build() {
        String className = mainElement.getSimpleName().toString()+"Serializer";

        serializer = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ParameterizedTypeName.get(ClassName.get(RapidSerializer.class), TypeName.get(mainElement.asType())));

        initializer.beginControlFlow("try");

        process();

        initializer.nextControlFlow("catch (NoSuchFieldException e)");
        initializer.addStatement("throw new RuntimeException(\"Field not found\")");
        initializer.endControlFlow();

        serializer.addMethod(MethodSpec.methodBuilder("serialize")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(DataOutputStream.class,"dos")
                .addParameter(TypeName.get(mainElement.asType()),"object")
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
                .returns(TypeName.get(mainElement.asType()))
                .addCode(reader.build())
                .build()
        );

        serializer.addStaticBlock(initializer.build());

        serializer.addMethod(MethodSpec.methodBuilder("getType")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.get(processingEnv.getTypeUtils().getDeclaredType(
                        processingEnv.getElementUtils().getTypeElement("java.lang.Class"),
                        mainElement.asType()
                )))
                .addCode(CodeBlock.builder()
                        .addStatement("return $T.class", mainElement.asType())
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

    protected SerializerBuilder(ProcessingEnvironment processingEnv, TypeElement element) {
        this.processingEnv = processingEnv;
        this.mainElement = element;
    }
}
