package com.Kari3600.me.RapidSerializer.builders;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

public class PrimitiveSerializerBuilder extends SerializerBuilder {

    @Override
    protected void process() {
        reader.addStatement("$L object = ($L) unsafe.allocateInstance($L.class)", mainElement.getQualifiedName(), mainElement.getQualifiedName(), mainElement.getQualifiedName());
        for (VariableElement field : ElementFilter.fieldsIn(mainElement.getEnclosedElements())) {
            processField(mainElement, field);
        }
        reader.addStatement("return object");
    }

    public PrimitiveSerializerBuilder(ProcessingEnvironment processingEnv, TypeElement mainElement) {
        super(processingEnv, mainElement);
    }
}
