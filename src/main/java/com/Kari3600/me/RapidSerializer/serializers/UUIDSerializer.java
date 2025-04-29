package com.Kari3600.me.RapidSerializer.serializers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class UUIDSerializer extends RapidSerializer<UUID> {

    @Override
    public UUID deserialize(DataInputStream dis) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        return new UUID(dis.readLong(), dis.readLong());
    }

    @Override
    public void serialize(DataOutputStream dos, UUID object) throws IOException, IllegalAccessException {
        dos.writeLong(object.getMostSignificantBits());
        dos.writeLong(object.getLeastSignificantBits());
    }

    @Override
    public Class<UUID> getType() {
        return UUID.class;
    }
}
