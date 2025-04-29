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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class TreeSerializerBuilder extends SerializerBuilder {

    private final DependencyTree tree;

    @Override
    public void process() {
        reader.addStatement("$L object", mainElement.getQualifiedName());

        processElement(mainElement);

        reader.addStatement("return object");
    }

    private void processElement(TypeElement element) {
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
                processElement(child);
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
            processField(element, field);
        }
    }

    public TreeSerializerBuilder(ProcessingEnvironment processingEnv, TypeElement element, DependencyTree tree) {
        super(processingEnv, element);
        this.tree = tree;
    }
}
