package org.github.common;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoPool;

import java.io.*;

/**
 * @author zengchzh
 * @date 2021/12/13
 */
public class JdkSerializer {

    public <T> byte[] serialize(T object) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(buffer)) {
            output.writeObject(object);
        }
        return buffer.toByteArray();
    }

    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try (ObjectInputStream input = new ObjectInputStream(in)) {
            try {
                return (T) input.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
