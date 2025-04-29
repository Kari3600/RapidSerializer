package com.Kari3600.me.RapidSerializer;

import java.io.IOException;
import java.util.*;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.*;

import com.Kari3600.me.RapidSerializer.builders.PrimitiveSerializerBuilder;
import com.Kari3600.me.RapidSerializer.builders.SerializerBuilder;
import com.Kari3600.me.RapidSerializer.builders.TreeSerializerBuilder;
import com.Kari3600.me.RapidSerializer.tree.ClassDependencyTree;
import com.Kari3600.me.RapidSerializer.tree.DependencyTree;
import com.Kari3600.me.RapidSerializer.tree.InterfaceDependencyTree;

//@SupportedAnnotationTypes("com.Kari3600.me.RapidSerializer.RapidSerializable")
@SupportedAnnotationTypes("*")
public class RapidSerializerAnnotationProcessor extends AbstractProcessor {

    private static boolean avaliable = true;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!avaliable) return true;
        avaliable = false;
        try {
            DependencyTree.setup(roundEnv);
            ServiceGenerator generator = new ServiceGenerator(processingEnv,"com.Kari3600.me.RapidSerializer.serializers.RapidSerializer");

            generator.registerService("com.Kari3600.me.RapidSerializer.serializers.IntSerializer");
            generator.registerService("com.Kari3600.me.RapidSerializer.serializers.LongSerializer");
            generator.registerService("com.Kari3600.me.RapidSerializer.serializers.StringSerializer");
            generator.registerService("com.Kari3600.me.RapidSerializer.serializers.UUIDSerializer");
            generator.registerService("com.Kari3600.me.RapidSerializer.serializers.ByteSerializer");
            generator.registerService("com.Kari3600.me.RapidSerializer.serializers.FloatSerializer");

            Set<SerializerBuilder> builders = new HashSet<>();

            for (TypeElement element : (Set<TypeElement>) roundEnv.getElementsAnnotatedWith(TreeSerializable.class)) {
                DependencyTree dependencyTree;
                if (element.getKind() == ElementKind.CLASS) {
                    dependencyTree = new ClassDependencyTree(element);
                } else if (element.getKind() == ElementKind.INTERFACE) {
                    dependencyTree = new InterfaceDependencyTree(element);
                } else {
                    throw new RuntimeException("Invalid element kind: " + element.getKind());
                }
                builders.add(new TreeSerializerBuilder(processingEnv, element, dependencyTree));
            }

            for (TypeElement element : (Set<TypeElement>) roundEnv.getElementsAnnotatedWith(PrimitiveSerializable.class)) {
                builders.add(new PrimitiveSerializerBuilder(processingEnv, element));
            }

            for (SerializerBuilder builder : builders) {
                builder.build();
            }

            generator.build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
    
}
