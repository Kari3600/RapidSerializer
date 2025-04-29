package com.Kari3600.me.RapidSerializer;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;

public class ServiceGenerator {
    private static ServiceGenerator instance;

    public static ServiceGenerator getInstance() {
        return instance;
    }

    private final Writer writer;

    public void registerService(String serviceName) throws IOException {
        writer.write(serviceName+"\n");
    }

    public void build() throws IOException {
        writer.close();
    }

    public ServiceGenerator(ProcessingEnvironment processingEnv, String service) throws IOException {
        instance = this;
        Filer filer = processingEnv.getFiler();
        FileObject file = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/"+service);
        writer = file.openWriter();

    }
}
