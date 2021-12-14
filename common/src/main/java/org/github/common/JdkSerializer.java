package org.github.common;

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
