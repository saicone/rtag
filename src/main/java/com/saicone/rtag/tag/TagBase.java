package com.saicone.rtag.tag;

import com.saicone.rtag.Rtag;
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
        // Constructors
        MethodHandle m1 = null, m2 = null, m3 = null, m4 = null, m5 = null, m6 = null, m7 = null, m8 = null, m9 = null, m10 = null;
        // Methods
        MethodHandle m11 = null, m12 = null, m13 = null, m14 = null, m15 = null, m16 = null, m17 = null, m18 = null, m19 = null, m20 = null;
        try {
            // Old names
            String data = "data", asByte = data, asDouble = data, asFloat = data, asLongArray = "b", asString = data;
            // New names
            if (ServerInstance.isUniversal) {
                data = "c";
                asByte = "x";
                asDouble = "w";
                asFloat = "w";
                asString = "A";
            }
            if (ServerInstance.verNumber >= 18) {
                asLongArray = "f";
            } else if (ServerInstance.verNumber >= 14) {
                asLongArray = "getLongs";
            } else if (ServerInstance.verNumber >= 13) {
                asLongArray = "d";
            }

            // Unreflect reason:
            // Method names change a lot across versions
            // Fields and constructors are private in all versions
            m1 = EasyLookup.unreflectConstructor("NBTTagByte", byte.class);
            m11 = EasyLookup.unreflectGetter("NBTTagByte", asByte);

            m2 = EasyLookup.unreflectConstructor("NBTTagByteArray", byte[].class);
            m12 = EasyLookup.unreflectGetter("NBTTagByteArray", data);

            m3 = EasyLookup.unreflectConstructor("NBTTagDouble", double.class);
            m13 = EasyLookup.unreflectGetter("NBTTagDouble", asDouble);

            m4 = EasyLookup.unreflectConstructor("NBTTagFloat", float.class);
            m14 = EasyLookup.unreflectGetter("NBTTagFloat", asFloat);

            m5 = EasyLookup.unreflectConstructor("NBTTagInt", int.class);
            m15 = EasyLookup.unreflectGetter("NBTTagInt", data);

            m6 = EasyLookup.unreflectConstructor("NBTTagIntArray", "int[]");
            m16 = EasyLookup.unreflectGetter("NBTTagIntArray", data);

            m7 = EasyLookup.unreflectConstructor("NBTTagLong", long.class);
            m17 = EasyLookup.unreflectGetter("NBTTagLong", data);

            if (ServerInstance.verNumber >= 12) {
                m8 = EasyLookup.unreflectConstructor("NBTTagLongArray", "long[]");
                if (ServerInstance.verNumber >= 13) {
                    m18 = EasyLookup.method("NBTTagLongArray", asLongArray, "long[]");
                } else {
                    // 1.12 only by getter
                    m18 = EasyLookup.unreflectGetter("NBTTagLongArray", asLongArray);
                }
            }

            m9 = EasyLookup.unreflectConstructor("NBTTagShort", short.class);
            m19 = EasyLookup.unreflectGetter("NBTTagShort", data);

            m10 = EasyLookup.unreflectConstructor("NBTTagString", String.class);
            m20 = EasyLookup.unreflectGetter("NBTTagString", asString);
        } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        tagByte = m1;
        tagByteArray = m2;
        tagDouble = m3;
        tagFloat = m4;
        tagInt = m5;
        tagIntArray = m6;
        tagLong = m7;
        tagLongArray = m8;
        tagShort = m9;
        tagString = m10;

        asByte = m11;
        asByteArray = m12;
        asDouble = m13;
        asFloat = m14;
        asInt = m15;
        asIntArray = m16;
        asLong = m17;
        asLongArray = m18;
        asShort = m19;
        asString = m20;

        newFunction(tagByte::invoke, byte.class, Byte.class);
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
     * Get Java value of NBTBase tag.<br>
     * For example NBTTagString -&gt; String.
     *
     * @see TagCompound#getValue(Rtag, Object) 
     * @see TagList#getValue(Rtag, Object) 
     *
     * @param tag Tag to extract value.
     * @return    A java object inside NBTBase tag.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static Object getValue(Object tag) throws Throwable {
        return tag == null ? null : getValueFunction.getOrDefault(tag.getClass(), DEFAULT_FUNCTION).apply(tag);
    }
}
