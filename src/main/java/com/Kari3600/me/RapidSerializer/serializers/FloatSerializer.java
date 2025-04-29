package com.Kari3600.me.RapidSerializer.serializers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FloatSerializer extends RapidSerializer<Float> {
    @Override
    public Float deserialize(DataInputStream dis) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        return dis.readFloat();
    }

    @Override
    public void serialize(DataOutputStream dos, Float object) throws IOException, IllegalAccessException {
        dos.writeFloat(object);
    }

    @Override
    public Class<Float> getType() {
        return float.class;
    }
}
