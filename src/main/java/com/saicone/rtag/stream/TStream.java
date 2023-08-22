package com.saicone.rtag.stream;

import com.saicone.rtag.RtagMirror;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;
import org.bukkit.util.Consumer;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Tag stream class to handle NBTTagCompound
 * with writeable and readable objects.<br>
 * The TStream instance provide easy methods
 * handle objects has bytes.
 * <h2>Write</h2>
 * Object -&gt; NBTTagCompound -&gt; Bytes<br>
 * With bytes you can write to file or convert
 * into Base64 String.
 * <h2>Read</h2>
 * Bytes -&gt; NBTTagCompound -&gt; Object<br>
 * You can read bytes from file or Base64.
 *
 * @author Rubenicos
 *
 * @param <T> Object type to write and read.
 */
public class TStream<T> {

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
        if (TagCompound.isTagCompound(compound)) {
            return compound;
        } else {
            return null;
        }
    }

    /**
     * Consume new objects if it can be created from NBTBase object.<br>
     * Only compatible with NBTTagByteArray, NBTTagList and NBTTagCompound.
     *
     * @param nbt      NBTBase instance.
     * @param consumer The consumer that accept non-null objects.
     */
    public void fromBase(Object nbt, Consumer<T> consumer) {
        switch (TagBase.getTypeId(nbt)) {
            case 7: // NBTTagByteArray
                fromBytes((byte[]) TagBase.getValue(nbt), consumer);
                break;
            case 9: // NBTTagList
                fromList(nbt, consumer);
                break;
            case 10: // NBTTagCompound
                fromCompound(nbt, consumer);
                break;
            default:
                break;
        }
    }

    /**
     * Create new object from NBTTagCompound.
     *
     * @param compound NBTTagCompound instance.
     * @return         A new object with NBTTagCompound parameters.
     */
    public T fromCompound(Object compound) {
        if (TagCompound.isTagCompound(compound)) {
            return build(compound);
        }

        if (TagList.isTagList(compound)) {
            final List<Object> list = TagList.getValue(compound);
            if (!list.isEmpty()) {
                return fromCompound(list.get(0));
            }
        }

        return null;
    }

    /**
     * Consume new object if it can be created from NBTTagCompound.
     *
     * @param compound NBTTagCompound instance.
     * @param consumer The consumer that accept non-null objects.
     */
    public void fromCompound(Object compound, Consumer<T> consumer) {
        final T t = fromCompound(compound);
        if (t != null) {
            consumer.accept(t);
        }
    }

    /**
     * Consume new objects if it can be created from NBTTagList object.<br>
     * Only compatible with list types of NBTTagByteArray, NBTTagList and NBTTagCompound.
     *
     * @param list     NBTTagList instance.
     * @param consumer The consumer that accept non-null objects.
     */
    public void fromList(Object list, Consumer<T> consumer) {
        switch (TagList.getType(list)) {
            case 7: // List<NBTTagByteArray>
                for (Object o : TagList.getValue(list)) {
                    fromBytes((byte[]) TagBase.getValue(o), consumer);
                }
                break;
            case 9: // List<NBTTagList>
                for (Object o : TagList.getValue(list)) {
                    fromList(o, consumer);
                }
                break;
            case 10: // List<NBTTagCompound>
                for (Object o : TagList.getValue(list)) {
                    fromCompound(o, consumer);
                }
                break;
            default:
                break;
        }
    }

    /**
     * Convert object into Map of objects.
     *
     * @param object Object to convert.
     * @return       A map that represent the provided object compound.
     */
    public Map<String, Object> toMap(T object) {
        return TagCompound.getValue(RtagMirror.INSTANCE, toCompound(object));
    }

    /**
     * Convert object into NBT String.
     *
     * @param object Object to convert.
     * @return       The object compound as String.
     */
    public String toString(T object) {
        return toCompound(object).toString();
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
     * Object -&gt; NBTTagCompound -&gt; Bytes
     *
     * @param object Object to convert.
     * @return       A byte array that represent the object.
     */
    public byte[] toBytes(T object) {
        Object compound = toCompound(object);
        if (compound == null) {
            return null;
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            TStreamTools.write(compound, out);
            return out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get object by read provided Map of objects.
     *
     * @param map Map that represent the object.
     * @return    An object representation using Map as compound.
     */
    public T fromMap(Map<String, Object> map) {
        return fromCompound(TagCompound.newTag(RtagMirror.INSTANCE, map));
    }

    /**
     * Get object by read provided NBT String.
     *
     * @param snbt A NBTTagCompound string.
     * @return     An object representation using NBT String as compound.
     */
    public T fromString(String snbt) {
        return fromCompound(TagCompound.newTag(snbt));
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
     * Consume new objects if it can be created from file.
     *
     * @param file     File to read.
     * @param consumer The consumer that accept non-null objects.
     */
    public void fromFile(File file, Consumer<T> consumer) {
        try {
            fromBase(TStreamTools.read(file), consumer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get array of objects by read Base64 string.
     *
     * @param base64 Base64 that represent the objects.
     * @return       Array of objects.
     */
    @SuppressWarnings("unchecked")
    public T[] fromBase64(String base64) {
        return (T[]) listFromBase64(base64).toArray();
    }

    /**
     * Consume new objects if it can be created from Base64 string.
     *
     * @param base64   Base64 that represent the objects.
     * @param consumer The consumer that accept non-null objects.
     */
    public void fromBase64(String base64, Consumer<T> consumer) {
        if (base64.equalsIgnoreCase("null")) {
            return;
        }
        fromBytes(Base64Coder.decodeLines(base64), consumer);
    }

    /**
     * Get list of objects by read Base64 string.
     *
     * @param base64 Base64 that represent the list.
     * @return       List of objects.
     */
    public List<T> listFromBase64(String base64) {
        if (base64.equalsIgnoreCase("null")) {
            return new ArrayList<>();
        }
        return listFromBytes(Base64Coder.decodeLines(base64));
    }

    /**
     * Get list of objects by read byte array.
     *
     * @param bytes Bytes to read,
     * @return      A list of converted objects from any saved compound.
     */
    public List<T> listFromBytes(byte[] bytes) {
        final List<T> list = new ArrayList<>();
        fromBytes(bytes, list::add);
        return list;
    }

    /**
     * Get object from bytes.<br>
     * This method first read the bytes with ByteArrayInputStream to
     * get an NBTTagCompound and convert it into object.<br>
     * Bytes -&gt; NBTTagCompound -&gt; Object
     *
     * @param bytes Bytes to read.
     * @return      A NBTTagCompound converted to object.
     */
    public T fromBytes(byte[] bytes) {
        try {
            return fromCompound(TStreamTools.read(bytes));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Consume new objects if it can be created from byte array.<br>
     * This method detect any NBTTagCompound inside provided bytes and convert into current
     * instance object using the detected compression format, it is also compatible with
     * any saved object inside NBTTagByteArray, NBTTagList and BukkitObjectInputStream.
     *
     * @param bytes    Byte array to read.
     * @param consumer The consumer that accept non-null objects.
     */
    public void fromBytes(byte[] bytes, Consumer<T> consumer) {
        if (bytes.length < 3) {
            return;
        }

        // Detect NBT and parse with GZIP compression format if it's applicable
        final Boolean gzip = TStreamTools.isGzipHeader(bytes) ? Boolean.TRUE : (TStreamTools.isNbtHeader(bytes) ? Boolean.FALSE : null);
        if (gzip != null) {
            final Object nbt;
            try (DataInputStream in = TStreamTools.getDataInput(new ByteArrayInputStream(bytes), gzip)) {
                nbt = TStreamTools.read((DataInput) in);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            if (nbt != null) {
                fromBase(nbt, consumer);
            }
            return;
        }

        // Try to read with BukkitObject stream
        fromBukkitObject(bytes, consumer);
    }

    /**
     * Consume new objects if it can be created from byte array read by BukkitObjectInputStream.
     *
     * @param bytes    Byte array to read.
     * @param consumer The consumer that accept non-null objects.
     */
    @SuppressWarnings("unchecked")
    public void fromBukkitObject(byte[] bytes, Consumer<T> consumer) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes); BukkitObjectInputStream input = new BukkitObjectInputStream(in)) {
            Object o;
            while ((o = input.readObject()) != null) {
                if (o instanceof byte[]) {
                    fromBytes((byte[]) o, consumer);
                } else {
                    // Try to cast to current type
                    try {
                        final T t = (T) o;
                        consumer.accept(t);
                    } catch (ClassCastException ignored) { }
                }
            }
        } catch (EOFException ignored) {
        } catch (ClassNotFoundException | IOException e) {
            if (e.getMessage().contains("invalid stream header")) {
                throw new IllegalArgumentException("Unsupported serialization format", e);
            }
            e.printStackTrace();
        }
    }
}
