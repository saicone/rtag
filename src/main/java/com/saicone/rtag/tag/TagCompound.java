package com.saicone.rtag.tag;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.saicone.rtag.RtagMirror;
import com.saicone.rtag.stream.TStream;
import com.saicone.rtag.util.EasyLookup;
import com.saicone.rtag.util.OptionalType;
import com.saicone.rtag.util.ServerInstance;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to invoke NBTTagCompound methods across versions.
 *
 * @author Rubenicos
 */
public class TagCompound {

    /**
     * Tag stream instance to save and get compounds.
     *
     * @see TStream
     */
    public static final TStream<Object> DATA = new TStream<>() {
        @Override
        public Object clone(Object object) {
            return TagCompound.safeClone(object);
        }
    };

    private static final Class<?> NBT_COMPOUND = EasyLookup.classById("NBTTagCompound");

    private static final MethodHandle newEmpty;
    private static final MethodHandle newCompound;
    private static final MethodHandle setMapField;
    private static final MethodHandle getMapField;
    private static final MethodHandle parse;
    private static final MethodHandle clone;

    static {
        // Constructors
        MethodHandle new$EmptyCompound = null;
        MethodHandle new$Compound = null;
        // Getters
        MethodHandle get$map = null;
        // Setters
        MethodHandle set$map = null;
        // Methods
        MethodHandle method$parse = null;
        MethodHandle method$clone = null;
        try {
            EasyLookup.addNMSClass("nbt.MojangsonParser");
            // Old names
            String map = "map";
            String parse = "parse";
            String clone = "clone";
            // New names
            if (ServerInstance.isUniversal) {
                map = "x";
                if (ServerInstance.verNumber >= 18) {
                    parse = "a";
                    if (ServerInstance.fullVersion >= 11903) {
                        clone = "h";
                    } else {
                        clone = "g";
                    }
                }
            } else if (ServerInstance.verNumber >= 10) {
                clone = "g";
            }

            new$EmptyCompound = EasyLookup.constructor(NBT_COMPOUND);
            if (ServerInstance.verNumber >= 15) {
                // Protected method
                new$Compound = EasyLookup.constructor(NBT_COMPOUND, Map.class);
            }

            // Private field
            get$map = EasyLookup.getter(NBT_COMPOUND, map, Map.class);
            set$map = EasyLookup.setter(NBT_COMPOUND, map, Map.class);

            method$parse = EasyLookup.staticMethod("MojangsonParser", parse, NBT_COMPOUND, String.class);
            // (1.8 -  1.9) return NBTBase
            method$clone = EasyLookup.method(NBT_COMPOUND, clone, NBT_COMPOUND);
        } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        newEmpty = new$EmptyCompound;
        newCompound = new$Compound;
        setMapField = set$map;
        getMapField = get$map;
        parse = method$parse;
        clone = method$clone;
    }

    TagCompound() {
    }

    /**
     * Constructs an empty NBTTagCompound.
     *
     * @return New NBTTagCompound instance.
     */
    public static Object newTag() {
        try {
            return newEmpty.invoke();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Constructs an NBTTagCompound with provided NBT string.
     *
     * @param snbt NBT String with data.
     * @return     New NBTTagCompound instance.
     */
    public static Object newTag(String snbt) {
        try {
            return parse.invoke(snbt);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Constructs an NBTTagCompound with provided Map of NBTBase values.
     *
     * @param map Map with tags.
     * @return    New NBTTagCompound instance.
     */
    public static Object newTag(Map<String, Object> map) {
        if (map.isEmpty()) {
            return newTag();
        }
        if (ServerInstance.verNumber >= 15) {
            try {
                return newCompound.invoke(map);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        } else {
            final Object tag = newTag();
            try {
                setMapField.invoke(tag, map);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
            return tag;
        }
    }

    /**
     * Constructs an NBTTagCompound with provided object
     * and required {@link RtagMirror} to convert Objects.<br>
     * This method can convert any supported object to Map
     * of objects using Gson deserializer.
     *
     * @param mirror RtagMirror to convert objects into tags.
     * @param object Object that can be converted to NBTTagCompound.
     * @return       New NBTTagCompound instance.
     * @throws IllegalArgumentException if the object is not supported.
     */
    @SuppressWarnings("unchecked")
    public static Object newTag(RtagMirror mirror, Object object) {
        final Map<String, Object> map = (Map<String, Object>) OptionalType.of(object).as(Map.class);
        if (map == null) {
            throw new IllegalArgumentException("The object type " + object.getClass().getName() + " cannot be used to create NBTTagCompound tag using TagCompound class");
        }
        return newTag(mirror, map);
    }

    /**
     * Constructs an NBTTagCompound with provided Map of Objects
     * and required {@link RtagMirror} to convert Objects.
     *
     * @param mirror RtagMirror to convert objects into tags.
     * @param map    Map with objects.
     * @return       New NBTTagCompound instance.
     */
    public static Object newTag(RtagMirror mirror, Map<String, Object> map) {
        if (map.isEmpty()) {
            return newTag();
        }

        final Map<String, Object> tags = new HashMap<>();
        for (var entry : map.entrySet()) {
            tags.put(entry.getKey(), mirror.newTag(entry.getValue()));
        }
        return newTag(tags);
    }

    /**
     * Check if the provided object is instance of NBTTagCompound class.
     *
     * @param object the object to check.
     * @return       true if the object is an instance of NBTTagCompound class.
     */
    public static boolean isTagCompound(Object object) {
        return NBT_COMPOUND.isInstance(object);
    }

    /**
     * Copy provided NBTTagCompound into new one.
     *
     * @param tag NBTTagCompound instance.
     * @return    A copy of original NBTTagCompound.
     */
    public static Object clone(Object tag) {
        try {
            return clone.invoke(tag);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Copy provided NBTTagCompound into new one without exceptions.
     *
     * @param tag NBTTagCompound instance.
     * @return    A copy of original NBTTagCompound.
     */
    public static Object safeClone(Object tag) {
        try {
            return clone.invoke(tag);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Get current tag map.
     *
     * @param tag NBTTagCompound instance.
     * @return    A Map of NBTBase Objects.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getValue(Object tag) {
        try {
            return (Map<String, Object>) getMapField.invoke(tag);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Get current tag map with converted values.
     *
     * @param mirror RtagMirror to convert tags.
     * @param tag    NBTTagCompound instance.
     * @return       A Map of Objects.
     */
    public static Map<String, Object> getValue(RtagMirror mirror, Object tag) {
        final Map<String, Object> map = new HashMap<>();
        for (var entry : getValue(tag).entrySet()) {
            map.put(entry.getKey(), mirror.getTagValue(entry.getValue()));
        }
        return map;
    }

    /**
     * Get the provided NBTTagCompound as Json string.
     *
     * @param tag NBTTagCompound instance.
     * @return    A Json string.
     */
    @SuppressWarnings("all")
    public static String getJson(Object tag) {
        final Type type = new TypeToken(){}.getType();
        return new Gson().toJson(getValue(RtagMirror.INSTANCE, tag), type);
    }

    /**
     * The inverse result of {@link #hasKey(Object, String)}.
     *
     * @param tag NBTTagCompound instance.
     * @param key Key to find.
     * @return    True if key exist.
     */
    public static boolean notHasKey(Object tag, String key) {
        try {
            return !hasKey(tag, key);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Check if NBTTagCompound contains certain key in Map.
     *
     * @param tag NBTTagCompound instance.
     * @param key Key to find.
     * @return    True if key exist.
     */
    public static boolean hasKey(Object tag, String key) {
        return getValue(tag).containsKey(key);
    }

    /**
     * Remove certain key from NBTTagCompound.
     *
     * @param tag NBTTagCompound instance.
     * @param key Key to remove.
     * @return    The previous value associated with key, or null if there was no mapping for key.
     */
    public static Object remove(Object tag, String key) {
        return getValue(tag).remove(key);
    }

    /**
     * Put certain NBTBase value to NBTTagCompound.
     *
     * @param tag   NBTTagCompound instance.
     * @param key   Value key.
     * @param value Value to put.
     * @return      The previous value associated with key, or null if there was no mapping for key.
     */
    public static Object set(Object tag, String key, Object value) {
        return getValue(tag).put(key, value);
    }

    /**
     * Override the current map of NBTBase inside NBTTagCompound.
     *
     * @param tag NBTTagCompound instance.
     * @param map Map with NBTBase tags.
     */
    public static void setValue(Object tag, Map<String, Object> map) {
        if (map.isEmpty()) {
            clear(tag);
        } else {
            try {
                setMapField.invoke(tag, map);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    /**
     * Get NBTBase value associated with key.
     *
     * @param tag NBTTagCompound instance.
     * @param key Value key.
     * @return    A NBTBase value if exist inside compound, null if not.
     */
    public static Object get(Object tag, String key) {
        return getValue(tag).get(key);
    }

    /**
     * Clear the provided NBTTagCompound tag.
     *
     * @param tag NBTTagCompound instance.
     */
    public static void clear(Object tag) {
        getValue(tag).clear();
    }
}
