package com.saicone.rtag.tag;

import com.saicone.rtag.Rtag;
import com.saicone.rtag.util.EasyLookup;
import com.saicone.rtag.util.ServerInstance;

import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Class to invoke methods inside classes that extends NBTBase.
 *
 * @author Rubenicos
 */
public class TagBase {

    private static final Map<Class<?>, Function<Object, Object>> newTagFunction = new HashMap<>();
    private static final Map<Class<?>, Function<Object, Object>> getValueFunction = new HashMap<>();

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
        tagByte = m1; tagByteArray = m2; tagDouble = m3; tagFloat = m4; tagInt = m5; tagIntArray = m6; tagLong = m7; tagLongArray = m8; tagShort = m9; tagString = m10;
        asByte = m11; asByteArray = m12; asDouble = m13; asFloat = m14; asInt = m15; asIntArray = m16; asLong = m17; asLongArray = m18; asShort = m19; asString = m20;

        newFunction(o -> EasyLookup.safeInvoke(tagByte, o), byte.class, Byte.class);
        getValueFunction.put(EasyLookup.classById("NBTTagByte"), o -> EasyLookup.safeInvoke(asByte, o));

        newTagFunction.put(EasyLookup.classById("byte[]"), o -> EasyLookup.safeInvoke(tagByteArray, o));
        getValueFunction.put(EasyLookup.classById("NBTTagByteArray"), o -> EasyLookup.safeInvoke(asByteArray, o));

        newFunction(o -> EasyLookup.safeInvoke(tagDouble, o), double.class, Double.class);
        getValueFunction.put(EasyLookup.classById("NBTTagDouble"), o -> EasyLookup.safeInvoke(asDouble, o));

        newFunction(o -> EasyLookup.safeInvoke(tagFloat, o), float.class, Float.class);
        getValueFunction.put(EasyLookup.classById("NBTTagFloat"), o -> EasyLookup.safeInvoke(asFloat, o));

        newFunction(o -> EasyLookup.safeInvoke(tagInt, o), int.class, Integer.class);
        getValueFunction.put(EasyLookup.classById("NBTTagInt"), o -> EasyLookup.safeInvoke(asInt, o));

        newTagFunction.put(EasyLookup.classById("int[]"), o -> EasyLookup.safeInvoke(tagIntArray, o));
        getValueFunction.put(EasyLookup.classById("NBTTagIntArray"), o -> EasyLookup.safeInvoke(asIntArray, o));

        newFunction(o -> EasyLookup.safeInvoke(tagLong, o), long.class, Long.class);
        getValueFunction.put(EasyLookup.classById("NBTTagLong"), o -> EasyLookup.safeInvoke(asLong, o));

        if (ServerInstance.verNumber >= 12) {
            newTagFunction.put(EasyLookup.classById("long[]"), o -> EasyLookup.safeInvoke(tagLongArray, o));
            getValueFunction.put(EasyLookup.classById("NBTTagLongArray"), o -> EasyLookup.safeInvoke(asLongArray, o));
        }

        newFunction(o -> EasyLookup.safeInvoke(tagShort, o), short.class, Short.class);
        getValueFunction.put(EasyLookup.classById("NBTTagShort"), o -> EasyLookup.safeInvoke(asShort, o));

        newTagFunction.put(String.class, o -> EasyLookup.safeInvoke(tagString, o));
        getValueFunction.put(EasyLookup.classById("NBTTagString"), o -> EasyLookup.safeInvoke(asString, o));

    }

    private static void newFunction(Function<Object, Object> function, Class<?>... classes) {
        for (Class<?> c : classes) {
            newTagFunction.put(c, function);
        }
    }

    /**
     * Constructs an NBTBase directly associated with Java object.<br>
     * For example Float ->  NBTTagFloat
     *
     * @see TagCompound#newTag(Map) 
     * @see TagList#newTag(List) 
     *
     * @param object Java object that exist in NBTBase tag.
     * @return       A NBTBase tag associated with provided object.
     */
    public static Object newTag(Object object) {
        return newTagFunction.get(object.getClass()).apply(object);
    }

    /**
     * Get Java value of NBTBase tag.<br>
     * For example NBTTagString -> String.
     *
     * @see TagCompound#getValue(Rtag, Object) 
     * @see TagList#getValue(Rtag, Object) 
     *
     * @param tag Tag to extract value.
     * @return    A java object inside NBTBase tag.
     */
    public static Object getValue(Object tag) {
        return getValueFunction.get(tag.getClass()).apply(tag);
    }
}
