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

/**
 * TagData class to handle NBTTagCompound
 * with writeable and readable objects.<br>
 * The TagData instance provide easy methods
 * handle objects has bytes.
 * <h2>Write</h2>
 * Object -> NBTTagCompound -> Bytes<br>
 * With bytes you can write to file or convert
 * into Base64 String.
 * <h2>Read</h2>
 * Bytes -> NBTTagCompound -> Object<br>
 * You can read bytes from file or Base64.
 *
 * @author Rubenicos
 *
 * @param <T> Object type to write and read.
 */
public class TagData<T> {

    private static final Class<?> tagCompound = EasyLookup.classById("NBTTagCompound");

    /**
     * Clone provided object by extract NBTTagCompound
     * and use it to build new object.
     *
     * @param object Object to copy.
     * @return       A copy of provided object.
     */
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

    /**
     * Convert object into NBTTagCompound.
     *
     * @param object Object to convert.
     * @return       A NBTTagCompound representing the object.
     */
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

    /**
     * Create new object from NBTTagCompound.
     *
     * @param compound NBTTagCompound instance.
     * @return         A new object with NBTTagCompound parameters.
     */
    public T fromCompound(Object compound) {
        if (tagCompound.isInstance(compound)) {
            return build(compound);
        } else {
            return null;
        }
    }

    /**
     * Write provided object into file.<br>
     * Provided file must be exist.<br>
     * This method first convert provided object into NBTTagCompound,
     * then write compound into FileOutputStream.
     *
     * @param object Object to write.
     * @param file   File to write inside.
     * @return       Provided file.
     */
    public File toFile(T object, File file) {
        try {
            TagDataStream.write(toCompound(object), file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * Convert objects into Base64.
     *
     * @param object Objects to convert.
     * @return       A Base64 String that represent provided objects.
     */
    @SafeVarargs
    public final String toBase64(T... object) {
        return listToBase64(Arrays.asList(object));
    }

    /**
     * Convert list of objects into Base64.
     *
     * @param objects Objects to convert.
     * @return        A Base64 String that represent provided list.
     */
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

    /**
     * Convert object into bytes.<br>
     * This method first convert provided object into NBTTagCompound,
     * then write compound into ByteArrayOutputStream.<br>
     * Object -> NBTTagCompound -> Bytes
     *
     * @param object Object to convert.
     * @return       A byte array that represent the object.
     */
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

    /**
     * Get object by read provided file.
     *
     * @param file File to read.
     * @return     A object if provided file contains it, null if not.
     */
    public T fromFile(File file) {
        try {
            return fromCompound(TagDataStream.read(file));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get array of objects by read Base64 string.
     *
     * @param base64 Base64 that represent the objets.
     * @return       Array of objects.
     */
    @SuppressWarnings("unchecked")
    public T[] fromBase64(String base64) {
        return (T[]) listFromBase64(base64).toArray();
    }

    /**
     * Get list og objects by reAD Base64 string.
     *
     * @param base64 Base64 that represent the list.
     * @return       List of objects.
     */
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

    /**
     * Get object from bytes.<br>
     * The method first read the bytes with ByteArrayInputStream to
     * get an NBTTagCompound and convert it into object.<br>
     * Bytes -> NBTTagCompound -> Object
     *
     * @param bytes Bytes to read.
     * @return      A NBTTagCompound converted to object.
     */
    public T fromBytes(byte[] bytes) {
        try {
            return fromCompound(TagDataStream.read(new ByteArrayInputStream(bytes)));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
