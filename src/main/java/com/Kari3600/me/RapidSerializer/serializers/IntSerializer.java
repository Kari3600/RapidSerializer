package com.Kari3600.me.RapidSerializer.serializers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class IntSerializer extends RapidSerializer<Integer> {
    @Override
    public Integer deserialize(DataInputStream dis) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        return dis.readInt();
    }

    @Override
    public void serialize(DataOutputStream dos, Integer object) throws IOException, IllegalAccessException {
        dos.writeInt(object);
    }

    @Override
    public Class<Integer> getType() {
        return int.class;
    }
}
