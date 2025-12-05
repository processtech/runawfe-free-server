package ru.runa.wfe.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import lombok.SneakyThrows;

public class SerialisationUtils {

    @SneakyThrows
    public static String writeObjectAsBase64(Serializable serializable) {
        return Base64.getEncoder().encodeToString(serialize(serializable));
    }

    @SneakyThrows
    public static Object readObjectFromBase64(String base64) {
        return deserialize(Base64.getDecoder().decode(base64));
    }

    public static byte[] serialize(Serializable data) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {

            out.writeObject(data);
            return bos.toByteArray();
        }
    }

    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return in.readObject();
        }
    }
}
