package com.saicone.rtag.stream;

import com.saicone.rtag.util.EasyLookup;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tag stream class to handle NBTTagCompound
 * with writeable and readable objects.<br>
 * The TStream instance provide easy methods
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
public class TStream<T> {

    private static final Class<?> byteArray = EasyLookup.classById("byte[]");
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

    /**
     * Extract object information into NBTTagCompound.
     *
     * @param object Object to extract.
     * @return       A NBTTagCompound with object information.
     */
    public Object extract(T object)  {
        return object;
    }

    /**
     * Build object type using an NBTTagCompound.
     *
     * @param compound NBTTagCompound with object information.
     * @return         A new object with NBTTagCompound parameters.
     */
    @SuppressWarnings("unchecked")
    public T build(Object compound) {
        try {
            return (T) compound;
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
        Object compound = extract(object);
        if (tagCompound.isInstance(compound)) {
            return compound;
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
            TStreamTools.write(toCompound(object), file);
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
                for (T object : objects) {
                    byte[] bytes = toBytes(object);
                    if (bytes != null) {
                        output.writeObject(bytes);
                    }
                }
                output.writeObject(null);
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
        Object compound = toCompound(object);
        if (compound == null) {
            return null;
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            TStreamTools.write(compound, output);
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
            return fromCompound(TStreamTools.read(file));
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
        if (!base64.isBlank()) {
            try (ByteArrayInputStream in = new ByteArrayInputStream(Base64Coder.decodeLines(base64)); BukkitObjectInputStream input = new BukkitObjectInputStream(in)) {
                Object o;
                while ((o = input.readObject()) != null) {
                    if (byteArray.isInstance(o)) {
                        T object = fromBytes((byte[]) o);
                        if (object != null) {
                            list.add(object);
                        }
                    }
                }
            } catch (EOFException ignored) {
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
            return fromCompound(TStreamTools.read(new ByteArrayInputStream(bytes)));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
