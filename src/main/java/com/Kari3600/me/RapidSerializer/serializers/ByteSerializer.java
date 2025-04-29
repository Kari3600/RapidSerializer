package com.Kari3600.me.RapidSerializer.serializers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ByteSerializer extends RapidSerializer<Byte> {
    @Override
    public Byte deserialize(DataInputStream dis) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        return dis.readByte();
    }

    @Override
    public void serialize(DataOutputStream dos, Byte object) throws IOException, IllegalAccessException {
        dos.writeByte(object);
    }

    @Override
    public Class<Byte> getType() {
        return byte.class;
    }
}
