package com.Kari3600.me.RapidSerializer.serializers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class StringSerializer extends RapidSerializer<String> {
    @Override
    public String deserialize(DataInputStream dis) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        return dis.readUTF();
    }

    @Override
    public void serialize(DataOutputStream dos, String object) throws IOException, IllegalAccessException {
        dos.writeUTF(object);
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }
}
