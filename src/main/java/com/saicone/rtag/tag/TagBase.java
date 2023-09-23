package com.saicone.rtag.tag;

import com.saicone.rtag.RtagMirror;
import com.saicone.rtag.util.EasyLookup;
import com.saicone.rtag.util.ServerInstance;
import com.saicone.rtag.util.ThrowableFunction;

import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Class to invoke methods inside classes that extends NBTBase.
 *
 * @author Rubenicos
 */
public class TagBase {

    private static final Class<?> NBT_BASE = EasyLookup.classById("NBTBase");

    private static final Map<Class<?>, ThrowableFunction<Object, Object>> newTagFunction = new HashMap<>();
    private static final Map<Class<?>, ThrowableFunction<Object, Object>> getValueFunction = new HashMap<>();

    private static final MethodHandle getTypeId;

    private static final MethodHandle tagByte;
    private static final MethodHandle asByte;

    private static final MethodHandle tagByteArray;
    private static final MethodHandle asByteArray;

    private static final MethodHandle tagDouble;
    private static final MethodHandle asDouble;

    private static final MethodHandle tagFloat;
    private static final MethodHandle asFloat;

    private static final MethodHandle tagInt;
    private static final MethodHandle asInt;

    private static final MethodHandle tagIntArray;
    private static final MethodHandle asIntArray;

    private static final MethodHandle tagLong;
    private static final MethodHandle asLong;

    // Only +1.12
    private static final MethodHandle tagLongArray;
    private static final MethodHandle asLongArray;

    private static final MethodHandle tagShort;
    private static final MethodHandle asShort;

    private static final MethodHandle tagString;
    private static final MethodHandle asString;

    static {
        // TagBase Methods
        MethodHandle method$getTypeId = null;
        // TagBase Constructors
        MethodHandle new$Byte = null;
        MethodHandle new$ByteArray = null;
        MethodHandle new$Double = null;
        MethodHandle new$Float = null;
        MethodHandle new$Int = null;
        MethodHandle new$IntArray = null;
        MethodHandle new$Long = null;
        MethodHandle new$LongArray = null;
        MethodHandle new$Short = null;
        MethodHandle new$String = null;
        // TagBase Getters
        MethodHandle get$Byte = null;
        MethodHandle get$ByteArray = null;
        MethodHandle get$Double = null;
        MethodHandle get$Float = null;
        MethodHandle get$Int = null;
        MethodHandle get$IntArray = null;
        MethodHandle get$Long = null;
        MethodHandle get$LongArray = null;
        MethodHandle get$Short = null;
        MethodHandle get$String = null;
        try {
            // Old names
            String getTypeId = "getTypeId";
            String data = "data";
            String asByte = data;
            String asDouble = data;
            String asFloat = data;
            String asLongArray = "b";
            String asString = data;

            // New names
            if (ServerInstance.isMojangMapped) {
                getTypeId = "getId";
                asLongArray = "data";
            } else if (ServerInstance.isUniversal) {
                if (ServerInstance.fullVersion >= 11902) { // v1_19_R2
                    getTypeId = "b";
                } else if (ServerInstance.verNumber >= 18) {
                    getTypeId = "a";
                }
                data = "c";
                asByte = "x";
                asDouble = "w";
                asFloat = "w";
                asLongArray = "c";
                asString = "A";
            } else if (ServerInstance.verNumber == 13 || ServerInstance.verNumber == 14) {
                asLongArray = "f";
            }

            method$getTypeId = EasyLookup.method(NBT_BASE, getTypeId, byte.class);

            // Unreflect reason:
            // Method names change a lot across versions
            // Fields and constructors are private in all versions
            new$Byte = EasyLookup.unreflectConstructor("NBTTagByte", byte.class);
            get$Byte = EasyLookup.getter("NBTTagByte", asByte, byte.class);

            new$ByteArray = EasyLookup.unreflectConstructor("NBTTagByteArray", byte[].class);
            get$ByteArray = EasyLookup.getter("NBTTagByteArray", data, byte[].class);

            new$Double = EasyLookup.unreflectConstructor("NBTTagDouble", double.class);
            get$Double = EasyLookup.getter("NBTTagDouble", asDouble, double.class);

            new$Float = EasyLookup.unreflectConstructor("NBTTagFloat", float.class);
            get$Float = EasyLookup.getter("NBTTagFloat", asFloat, float.class);

            new$Int = EasyLookup.unreflectConstructor("NBTTagInt", int.class);
            get$Int = EasyLookup.getter("NBTTagInt", data, int.class);

            new$IntArray = EasyLookup.unreflectConstructor("NBTTagIntArray", "int[]");
            get$IntArray = EasyLookup.getter("NBTTagIntArray", data, "int[]");

            new$Long = EasyLookup.unreflectConstructor("NBTTagLong", long.class);
            get$Long = EasyLookup.getter("NBTTagLong", data, long.class);

            if (ServerInstance.verNumber >= 12) {
                new$LongArray = EasyLookup.unreflectConstructor("NBTTagLongArray", "long[]");
                get$LongArray = EasyLookup.getter("NBTTagLongArray", asLongArray, "long[]");
            }

            new$Short = EasyLookup.unreflectConstructor("NBTTagShort", short.class);
            get$Short = EasyLookup.getter("NBTTagShort", data, short.class);

            new$String = EasyLookup.unreflectConstructor("NBTTagString", String.class);
            get$String = EasyLookup.getter("NBTTagString", asString, String.class);
        } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        getTypeId = method$getTypeId;

        tagByte = new$Byte;
        tagByteArray = new$ByteArray;
        tagDouble = new$Double;
        tagFloat = new$Float;
        tagInt = new$Int;
        tagIntArray = new$IntArray;
        tagLong = new$Long;
        tagLongArray = new$LongArray;
        tagShort = new$Short;
        tagString = new$String;

        asByte = get$Byte;
        asByteArray = get$ByteArray;
        asDouble = get$Double;
        asFloat = get$Float;
        asInt = get$Int;
        asIntArray = get$IntArray;
        asLong = get$Long;
        asLongArray = get$LongArray;
        asShort = get$Short;
        asString = get$String;

        newFunction(tagByte::invoke, byte.class, Byte.class);
        // Boolean -> Byte compatibility
        newFunction((bool) -> tagByte.invoke((Boolean) bool ? (byte) 1 : (byte) 0), boolean.class, Boolean.class);
        getValueFunction.put(EasyLookup.classById("NBTTagByte"), asByte::invoke);

        newTagFunction.put(EasyLookup.classById("byte[]"), tagByteArray::invoke);
        getValueFunction.put(EasyLookup.classById("NBTTagByteArray"), asByteArray::invoke);

        newFunction(tagDouble::invoke, double.class, Double.class);
        getValueFunction.put(EasyLookup.classById("NBTTagDouble"), asDouble::invoke);

        newFunction(tagFloat::invoke, float.class, Float.class);
        getValueFunction.put(EasyLookup.classById("NBTTagFloat"), asFloat::invoke);

        newFunction(tagInt::invoke, int.class, Integer.class);
        getValueFunction.put(EasyLookup.classById("NBTTagInt"), asInt::invoke);

        newTagFunction.put(EasyLookup.classById("int[]"), tagIntArray::invoke);
        getValueFunction.put(EasyLookup.classById("NBTTagIntArray"), asIntArray::invoke);

        newFunction(tagLong::invoke, long.class, Long.class);
        getValueFunction.put(EasyLookup.classById("NBTTagLong"), asLong::invoke);

        if (ServerInstance.verNumber >= 12) {
            newTagFunction.put(EasyLookup.classById("long[]"), tagLongArray::invoke);
            getValueFunction.put(EasyLookup.classById("NBTTagLongArray"), asLongArray::invoke);
        }

        newFunction(tagShort::invoke, short.class, Short.class);
        getValueFunction.put(EasyLookup.classById("NBTTagShort"), asShort::invoke);

        newTagFunction.put(String.class, tagString::invoke);
        // UUID -> String compatibility
        newTagFunction.put(UUID.class, (uuid) -> tagString.invoke(uuid.toString()));
        getValueFunction.put(EasyLookup.classById("NBTTagString"), asString::invoke);

    }

    TagBase() {
    }

    private static void newFunction(ThrowableFunction<Object, Object> function, Class<?>... classes) {
        for (Class<?> c : classes) {
            newTagFunction.put(c, function);
        }
    }

    /**
     * Constructs an NBTBase directly associated with Java object.<br>
     * For example Float -&gt;  NBTTagFloat
     *
     * @see TagCompound#newTag(Map) 
     * @see TagList#newTag(List) 
     *
     * @param object Java object that exist in NBTBase tag.
     * @return       A NBTBase tag associated with provided object.
     * @throws IllegalArgumentException if the object is not supported by this method.
     */
    public static Object newTag(Object object) throws IllegalArgumentException {
        if (object == null) {
            return null;
        }

        final ThrowableFunction<Object, Object> function = newTagFunction.get(object.getClass());
        if (function == null) {
            throw new IllegalArgumentException("The object type " + object.getClass().getName() + " cannot be used to create NBTBase tag using TagBase class");
        }

        try {
            return function.apply(object);
        } catch (Throwable t) {
            throw new RuntimeException("Cannot create NBTBase object from " + object.getClass().getName() + " object", t);
        }
    }

    /**
     * Constructs an NBTBase directly associated with provided object.<br>
     * For example List -&gt; NBTTagList
     *
     * @param mirror RtagMirror to convert objects into tags.
     * @param object Object that can be converted to NBTBase tag.
     * @return       A NBTBase tag associated with provided object.
     * @throws IllegalArgumentException if the object is not supported.
     */
    public static Object newTag(RtagMirror mirror, Object object) throws IllegalArgumentException {
        try {
            return newTag(object);
        } catch (IllegalArgumentException e) {
            if (object instanceof List) {
                return TagList.newTag(mirror, (List<?>) object);
            } else {
                return TagCompound.newTag(mirror, object);
            }
        }
    }

    /**
     * Check if the provided object is instance of NBTBase class.
     *
     * @param object the object to check.
     * @return       true if the object is an instance of NBTBase class.
     */
    public static boolean isTag(Object object) {
        return NBT_BASE.isInstance(object);
    }

    /**
     * Check if NBTBase object type is equals to provided type.
     *
     * @param tag  the NBTBase object to check.
     * @param type the nbt type.
     * @return     true if NBTBase type is equals.
     */
    public static boolean isTypeOf(Object tag, byte type) {
        return getTypeId(tag) == type;
    }

    /**
     * Check if two NBTBase objects has the same type.
     *
     * @param tag1 the first NBTBase object to check.
     * @param tag2 the second NBTBase object to check.
     * @return     true if the two NBTBase type are equals.
     */
    public static boolean isTypeOf(Object tag1, Object tag2) {
        return getTypeId(tag1) == getTypeId(tag2);
    }

    /**
     * Copy provided NBTBase object into new one.
     *
     * @param tag Tag to copy.
     * @return    A NBTBase tag with the same value.
     */
    public static Object clone(Object tag) {
        return newTag(getValue(tag));
    }

    /**
     * Get current tag type ID.<br>
     * Byte = 1 | Short = 2 | Int = 3 | Long = 4 | Float = 5 | Double = 6 |
     * ByteArray = 7 | String = 8 | List = 9 | Compound = 10 | IntArray = 11 | LongArray = 12
     *
     * @param tag TagBase instance to get the ID.
     * @return    An ID that represents the tag type.
     */
    public static byte getTypeId(Object tag) {
        try {
            return (byte) getTypeId.invoke(tag);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Get Java value of NBTBase tag.<br>
     * For example NBTTagString -&gt; String.
     *
     * @see TagCompound#getValue(RtagMirror, Object)
     * @see TagList#getValue(RtagMirror, Object)
     *
     * @param tag Tag to extract value.
     * @return    A java object inside NBTBase tag.
     * @throws IllegalArgumentException if tag is not supported by this class.
     */
    public static Object getValue(Object tag) throws IllegalArgumentException {
        if (tag == null) return null;

        final ThrowableFunction<Object, Object> function = getValueFunction.get(tag.getClass());
        if (function == null) {
            throw new IllegalArgumentException("The object type " + tag.getClass().getName() + " is not supported by TagBase class");
        }

        try {
            return function.apply(tag);
        } catch (Throwable t) {
            throw new RuntimeException("Cannot get java object from " + tag.getClass().getName() + " class", t);
        }
    }

    /**
     * Get Java value of any NBTBase tag.<br>
     * For example NBTTagCompound -&gt; Map.
     *
     * @param mirror RtagMirror to convert objects into tags.
     * @param tag    Tag to extract value.
     * @return       A java object like NBTBase tag.
     * @throws IllegalArgumentException if tag is not supported.
     */
    public static Object getValue(RtagMirror mirror, Object tag) throws IllegalArgumentException {
        try {
            return getValue(tag);
        } catch (IllegalArgumentException e) {
            if (TagCompound.isTagCompound(tag)) {
                return TagCompound.getValue(mirror, tag);
            }
            if (TagList.isTagList(tag)) {
                return TagList.getValue(mirror, tag);
            }
            throw e;
        }
    }

    /**
     * Get the size of elements inside NBTTagCompound or NBTTagList.
     *
     * @param tag NBTBase instance.
     * @return    Size of map or list inside.
     */
    public static int size(Object tag) {
        if (TagCompound.isTagCompound(tag)) {
            return TagCompound.getValue(tag).size();
        } else if (TagList.isTagList(tag)) {
            return TagList.size(tag);
        } else {
            return -1;
        }
    }

    /**
     * Clear the provided NBTTagCompound or NBTTagList.
     *
     * @param tag NBTBase instance.
     */
    public static void clear(Object tag) {
        if (TagCompound.isTagCompound(tag)) {
            TagCompound.clear(tag);
        } else if (TagList.isTagList(tag)) {
            TagList.clear(tag);
        }
    }
}
