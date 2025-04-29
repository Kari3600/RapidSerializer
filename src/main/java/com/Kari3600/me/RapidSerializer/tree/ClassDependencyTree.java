package com.Kari3600.me.RapidSerializer.tree;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.Set;
import java.util.stream.Collectors;

public class ClassDependencyTree extends DependencyTree {

    @Override
    public Set<TypeElement> getChildren(TypeElement element) {
        return classMap.values().stream()
                .filter(e -> e.getKind() == ElementKind.CLASS)
                .filter(e -> getParent(e) == element)
                .collect(Collectors.toSet());
    }

    public ClassDependencyTree(TypeElement classElement) {

    }
}
