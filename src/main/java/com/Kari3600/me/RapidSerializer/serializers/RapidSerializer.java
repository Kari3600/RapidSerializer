package com.Kari3600.me.RapidSerializer.serializers;

import sun.misc.Unsafe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public abstract class RapidSerializer<T> {
    private static final Map<Class<?>,RapidSerializer<?>> serializers = new HashMap<>();

    static {
        ServiceLoader<RapidSerializer> loader = ServiceLoader.load(RapidSerializer.class);
        for (RapidSerializer<?> serializer : loader) {
            serializers.put(serializer.getType(), serializer);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> RapidSerializer<T> getSerializer(Class<T> clazz) {
        if (!serializers.containsKey(clazz)) {
            throw new RuntimeException("No RapidSerializer registered for " + clazz);
        }
        return (RapidSerializer<T>) serializers.get(clazz);
    }

    protected static Unsafe unsafe;
    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Unable to get unsafe", e);
        }

    }

    public abstract T deserialize(DataInputStream dis) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException;
    public abstract void serialize(DataOutputStream dos, T object) throws IOException, IllegalAccessException;

    public abstract Class<T> getType();
}
