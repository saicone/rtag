package com.saicone.rtag.data;

import com.saicone.rtag.util.EasyLookup;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TagData<T> {

    private static final Class<?> tagCompound = EasyLookup.classById("NBTTagCompound");

    public T clone(T object) {
        return fromCompound(toCompound(object));
    }

    protected Object extract(T object) {
        return object;
    }

    @SuppressWarnings("unchecked")
    protected T build(Object object) {
        try {
            return (T) object;
        } catch (ClassCastException e) {
            return null;
        }
    }

    public Object toCompound(T object) {
        if (object == null) {
            return null;
        }
        Object tag = extract(object);
        if (tagCompound.isInstance(tag)) {
            return tag;
        } else {
            return null;
        }
    }

    public T fromCompound(Object object) {
        if (tagCompound.isInstance(object)) {
            return build(object);
        } else {
            return null;
        }
    }

    public File toFile(T object, File file) {
        try {
            TagDataStream.write(toCompound(object), file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    @SafeVarargs
    public final String toBase64(T... object) {
        return listToBase64(Arrays.asList(object));
    }

    public String listToBase64(List<T> objects) {
        String data = "";
        if (!objects.isEmpty()) {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream(); BukkitObjectOutputStream output = new BukkitObjectOutputStream(out)) {
                output.writeInt(objects.size());
                for (T object : objects) {
                    output.writeObject(toBytes(object));
                }
                data = Base64Coder.encodeLines(out.toByteArray());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    public byte[] toBytes(T object) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            TagDataStream.write(toCompound(object), output);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return output.toByteArray();
    }

    public T fromFile(File file) {
        try {
            return fromCompound(TagDataStream.read(file));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public T[] fromBase64(String base64) {
        return (T[]) listFromBase64(base64).toArray();
    }

    public List<T> listFromBase64(String base64) {
        List<T> list = new ArrayList<>();
        if (!base64.trim().isEmpty()) {
            try (ByteArrayInputStream in = new ByteArrayInputStream(Base64Coder.decodeLines(base64)); BukkitObjectInputStream input = new BukkitObjectInputStream(in)) {
                int size = input.readInt();
                for (int i = 0; i < size; i++) {
                    byte[] bytes = (byte[]) input.readObject();
                    if (bytes != null) {
                        T object = fromBytes(bytes);
                        if (object != null) {
                            list.add(object);
                        }
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public T fromBytes(byte[] bytes) {
        try {
            return fromCompound(TagDataStream.read(new ByteArrayInputStream(bytes)));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
