package com.Kari3600.me.RapidSerializer.serializers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class LongSerializer extends RapidSerializer<Long> {

    @Override
    public Long deserialize(DataInputStream dis) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        return dis.readLong();
    }

    @Override
    public void serialize(DataOutputStream dos, Long object) throws IOException, IllegalAccessException {
        dos.writeLong(object);
    }

    @Override
    public Class<Long> getType() {
        return long.class;
    }
}
