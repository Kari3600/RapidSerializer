package com.Kari3600.me.RapidSerializer.tree;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class InterfaceDependencyTree extends DependencyTree {
    private final TypeElement interfaceElement;
    private final Set<TypeElement> includedClasses;

    @Override
    public Set<TypeElement> getChildren(TypeElement element) {
            return includedClasses.stream()
                    .filter(e -> e.getKind() == ElementKind.CLASS)
                    .filter(e -> (element == interfaceElement) ? getParent(e) == null : getParent(e) == element)
                    .collect(Collectors.toSet());
    }

    private boolean isImplementation(TypeElement element) {
        if (interfaceElement == element) return true;
        List<? extends TypeMirror> interfaces = element.getInterfaces();
        if (interfaces.isEmpty()) return false;
        return interfaces.stream().map(classMap::get).filter(Objects::nonNull).anyMatch(this::isImplementation);
    }

    public InterfaceDependencyTree(TypeElement interfaceElement) {
        this.interfaceElement = interfaceElement;
        includedClasses = classMap.values().stream()
                .filter(e -> e.getKind() == ElementKind.CLASS)
                .filter(e -> getAncestors(e).stream()
                        .anyMatch(this::isImplementation)
                )
                .flatMap(e -> getAncestors(e).stream())
                .collect(Collectors.toSet());
        System.out.println("Included Classes: ");
        for (TypeElement element : includedClasses) {
            System.out.println(element.getQualifiedName());
        }
    }
}
