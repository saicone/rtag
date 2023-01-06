package com.saicone.rtag.tag;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.saicone.rtag.RtagMirror;
import com.saicone.rtag.stream.TStream;
import com.saicone.rtag.util.EasyLookup;
import com.saicone.rtag.util.ServerInstance;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
    private static final MethodHandle hasKey;
    private static final MethodHandle remove;
    private static final MethodHandle set;
    private static final MethodHandle get;
    private static final MethodHandle getKeys;

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
        MethodHandle method$hasKey = null;
        MethodHandle method$remove = null;
        MethodHandle method$set = null;
        MethodHandle method$get = null;
        MethodHandle method$getKeys = null;
        try {
            EasyLookup.addNMSClass("nbt.MojangsonParser");
            // Old names
            String map = "map";
            String parse = "parse";
            String clone = "clone";
            String hasKey = "hasKey";
            String remove = "remove";
            String set = "set";
            String get = "get";
            String getKeys = "c";
            // New names
            if (ServerInstance.verNumber >= 18) {
                parse = "a";
                clone = "g";
                hasKey = "e";
                remove = "r";
                set = "a";
                get = "c";
                if (ServerInstance.fullVersion >= 11902) { // v1_19_R2
                    getKeys = "e";
                } else {
                    getKeys = "d";
                }
            } else if (ServerInstance.verNumber >= 13) {
                getKeys = "getKeys";
            } else if (ServerInstance.verNumber >= 10) {
                clone = "g";
            }
            if (ServerInstance.isUniversal) {
                map = "x";
            }

            new$EmptyCompound = EasyLookup.constructor(NBT_COMPOUND);
            if (ServerInstance.verNumber >= 15) {
                // Protected method
                new$Compound = EasyLookup.unreflectConstructor(NBT_COMPOUND, Map.class);
            } else {
                // Private field
                set$map = EasyLookup.unreflectSetter(NBT_COMPOUND, map);
            }
            // Private field
            get$map = EasyLookup.unreflectGetter(NBT_COMPOUND, map);
            method$parse = EasyLookup.staticMethod("MojangsonParser", parse, NBT_COMPOUND, String.class);
            // Unreflect reason:
            // (1.8 -  1.9) return NBTBase
            // Other versions return NBTTagCompound
            method$clone = EasyLookup.unreflectMethod(NBT_COMPOUND, clone);
            method$hasKey = EasyLookup.method(NBT_COMPOUND, hasKey, boolean.class, String.class);
            method$remove = EasyLookup.method(NBT_COMPOUND, remove, void.class, String.class);
            // Unreflect reason:
            // (1.8 -  1.13) void method
            // Other versions return NBTBase
            method$set = EasyLookup.unreflectMethod(NBT_COMPOUND, set, String.class, "NBTBase");
            method$get = EasyLookup.method(NBT_COMPOUND, get, "NBTBase", String.class);
            method$getKeys = EasyLookup.method(NBT_COMPOUND, getKeys, Set.class);
        } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        newEmpty = new$EmptyCompound;
        newCompound = new$Compound;
        setMapField = set$map;
        getMapField = get$map;
        parse = method$parse;
        clone = method$clone;
        hasKey = method$hasKey;
        remove = method$remove;
        set = method$set;
        get = method$get;
        getKeys = method$getKeys;
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
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getValue(RtagMirror mirror, Object tag) {
        final Map<String, Object> map = new HashMap<>();
        try {
            for (String key : (Set<String>) getKeys.invoke(tag)) {
                map.put(key, mirror.getTagValue(get.invoke(tag, key)));
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
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
        try {
            return (boolean) hasKey.invoke(tag, key);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Remove certain key from NBTTagCompound.
     *
     * @param tag NBTTagCompound instance.
     * @param key Key to remove.
     */
    public static void remove(Object tag, String key) {
        try {
            remove.invoke(tag, key);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Put certain NBTBase value to NBTTagCompound.
     *
     * @param tag   NBTTagCompound instance.
     * @param key   Value key.
     * @param value Value to put.
     * @return      The value that was set.
     */
    public static Object set(Object tag, String key, Object value) {
        try {
            set.invoke(tag, key, value);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        return value;
    }

    /**
     * Get NBTBase value associated with key.
     *
     * @param tag NBTTagCompound instance.
     * @param key Value key.
     * @return    A NBTBase value if exist inside compound, null if not.
     */
    public static Object get(Object tag, String key) {
        try {
            return get.invoke(tag, key);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
