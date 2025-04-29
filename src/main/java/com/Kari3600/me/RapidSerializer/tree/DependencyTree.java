package com.Kari3600.me.RapidSerializer.tree;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class DependencyTree {
    protected static Map<TypeMirror, TypeElement> classMap;

    public static void setup(RoundEnvironment roundEnv) {
        classMap = new HashMap<>();
        for (Element element :  roundEnv.getRootElements()) {
            if (element instanceof TypeElement) {
                classMap.put(element.asType(), (TypeElement) element);
            }
        }
    }

    public static TypeElement get(TypeMirror typeMirror) {
        return classMap.get(typeMirror);
    }

    protected Set<TypeElement> getAncestors(TypeElement classElement) {
        TypeElement parentElement = getParent(classElement);
        Set<TypeElement> ancestors = (parentElement == null) ? new HashSet<>() : getAncestors(parentElement);
        ancestors.add(classElement);
        return ancestors;
    }

    protected TypeElement getParent(TypeElement classElement) {
        return classMap.get(classElement.getSuperclass());
    }

    public abstract Set<TypeElement> getChildren(TypeElement element);
}
