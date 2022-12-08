package com.saicone.rtag.tag;

import com.saicone.rtag.RtagMirror;
import com.saicone.rtag.util.EasyLookup;
import com.saicone.rtag.util.ServerInstance;
import com.saicone.rtag.util.ThrowableFunction;

import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to invoke methods inside classes that extends NBTBase.
 *
 * @author Rubenicos
 */
public class TagBase {

    private static final ThrowableFunction<Object, Object> DEFAULT_FUNCTION = (o) -> null;

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
            if (ServerInstance.isUniversal) {
                if (ServerInstance.verNumber >= 18) {
                    if (ServerInstance.fullVersion >= 11902) { // v1_19_R2
                        getTypeId = "b";
                        asLongArray = "g";
                    } else {
                        getTypeId = "a";
                        asLongArray = "f";
                    }
                } else {
                    asLongArray = "getLongs";
                }
                data = "c";
                asByte = "x";
                asDouble = "w";
                asFloat = "w";
                asString = "A";
            } else if (ServerInstance.verNumber >= 14) {
                asLongArray = "getLongs";
            } else if (ServerInstance.verNumber >= 13) {
                asLongArray = "d";
            }

            method$getTypeId = EasyLookup.method("NBTBase", getTypeId, byte.class);

            // Unreflect reason:
            // Method names change a lot across versions
            // Fields and constructors are private in all versions
            new$Byte = EasyLookup.unreflectConstructor("NBTTagByte", byte.class);
            get$Byte = EasyLookup.unreflectGetter("NBTTagByte", asByte);

            new$ByteArray = EasyLookup.unreflectConstructor("NBTTagByteArray", byte[].class);
            get$ByteArray = EasyLookup.unreflectGetter("NBTTagByteArray", data);

            new$Double = EasyLookup.unreflectConstructor("NBTTagDouble", double.class);
            get$Double = EasyLookup.unreflectGetter("NBTTagDouble", asDouble);

            new$Float = EasyLookup.unreflectConstructor("NBTTagFloat", float.class);
            get$Float = EasyLookup.unreflectGetter("NBTTagFloat", asFloat);

            new$Int = EasyLookup.unreflectConstructor("NBTTagInt", int.class);
            get$Int = EasyLookup.unreflectGetter("NBTTagInt", data);

            new$IntArray = EasyLookup.unreflectConstructor("NBTTagIntArray", "int[]");
            get$IntArray = EasyLookup.unreflectGetter("NBTTagIntArray", data);

            new$Long = EasyLookup.unreflectConstructor("NBTTagLong", long.class);
            get$Long = EasyLookup.unreflectGetter("NBTTagLong", data);

            if (ServerInstance.verNumber >= 12) {
                new$LongArray = EasyLookup.unreflectConstructor("NBTTagLongArray", "long[]");
                if (ServerInstance.verNumber >= 13) {
                    get$LongArray = EasyLookup.method("NBTTagLongArray", asLongArray, "long[]");
                } else {
                    // 1.12 only by getter
                    get$LongArray = EasyLookup.unreflectGetter("NBTTagLongArray", asLongArray);
                }
            }

            new$Short = EasyLookup.unreflectConstructor("NBTTagShort", short.class);
            get$Short = EasyLookup.unreflectGetter("NBTTagShort", data);

            new$String = EasyLookup.unreflectConstructor("NBTTagString", String.class);
            get$String = EasyLookup.unreflectGetter("NBTTagString", asString);
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
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static Object newTag(Object object) throws Throwable {
        return object == null ? null : newTagFunction.getOrDefault(object.getClass(), DEFAULT_FUNCTION).apply(object);
    }

    /**
     * Copy provided NBTBase object into new one.
     *
     * @param tag Tag to copy.
     * @return    A NBTBase tag with the same value.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static Object clone(Object tag) throws Throwable {
        return newTag(getValue(tag));
    }

    /**
     * Get current tag type ID.<br>
     * Byte = 1 | Short = 2 | Int = 3 | Long = 4 | Float = 5 | Double = 6 |
     * ByteArray = 7 | String = 8 | List = 9 | Compound = 10 | IntArray = 11 | LongArray = 12
     *
     * @param tag TagBase instance to get the ID.
     * @return    An ID that represents the tag type.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static byte getTypeId(Object tag) throws Throwable {
        return (byte) getTypeId.invoke(tag);
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
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static Object getValue(Object tag) throws Throwable {
        return tag == null ? null : getValueFunction.getOrDefault(tag.getClass(), DEFAULT_FUNCTION).apply(tag);
    }
}
