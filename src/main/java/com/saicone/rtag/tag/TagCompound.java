package com.saicone.rtag.tag;

import com.saicone.rtag.RtagMirror;
import com.saicone.rtag.stream.TStream;
import com.saicone.rtag.util.EasyLookup;
import com.saicone.rtag.util.ServerInstance;

import java.lang.invoke.MethodHandle;
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

    private static final Class<?> nbtCompound = EasyLookup.classById("NBTTagCompound");

    private static final MethodHandle newEmpty;
    private static final MethodHandle newCompound;
    private static final MethodHandle setMapField;
    private static final MethodHandle getMapField;
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
        MethodHandle method$clone = null;
        MethodHandle method$hasKey = null;
        MethodHandle method$remove = null;
        MethodHandle method$set = null;
        MethodHandle method$get = null;
        MethodHandle method$getKeys = null;
        try {
            Class<?> base = EasyLookup.classById("NBTBase");
            // Old names
            String clone = "clone";
            String hasKey = "hasKey";
            String remove = "remove";
            String set = "set";
            String get = "get";
            String getKeys = "c";
            // New names
            if (ServerInstance.verNumber >= 18) {
                clone = "g";
                hasKey = "e";
                remove = "r";
                set = "a";
                get = "c";
                getKeys = "d";
            } else if (ServerInstance.verNumber >= 13) {
                getKeys = "getKeys";
            } else if (ServerInstance.verNumber >= 10) {
                clone = "g";
            }

            new$EmptyCompound = EasyLookup.constructor(nbtCompound);
            if (ServerInstance.verNumber >= 15) {
                // Private method
                new$Compound = EasyLookup.unreflectConstructor(nbtCompound, Map.class);
            } else {
                // Private field
                set$map = EasyLookup.unreflectSetter(nbtCompound, "map");
            }
            // Private field
            get$map = EasyLookup.unreflectGetter(nbtCompound, "map");
            // Unreflect reason:
            // (1.8 -  1.9) return NBTBase
            // Other versions return NBTTagCompound
            method$clone = EasyLookup.unreflectMethod(nbtCompound, clone);
            method$hasKey = EasyLookup.method(nbtCompound, hasKey, boolean.class, String.class);
            method$remove = EasyLookup.method(nbtCompound, remove, void.class, String.class);
            // Unreflect reason:
            // (1.8 -  1.13) void method
            // Other versions return NBTBase
            method$set = EasyLookup.unreflectMethod(nbtCompound, set, String.class, base);
            method$get = EasyLookup.method(nbtCompound, get, base, String.class);
            method$getKeys = EasyLookup.method(nbtCompound, getKeys, Set.class);
        } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        newEmpty = new$EmptyCompound;
        newCompound = new$Compound;
        setMapField = set$map;
        getMapField = get$map;
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
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static Object newTag() throws Throwable {
        return newEmpty.invoke();
    }

    /**
     * Constructs an NBTTagCompound with provided Map of NBTBase values.
     *
     * @param map Map with tags.
     * @return    New NBTTagCompound instance.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static Object newTag(Map<String, Object> map) throws Throwable {
        if (map.isEmpty()) return newEmpty.invoke();
        if (ServerInstance.verNumber >= 15) {
            return newCompound.invoke(map);
        } else {
            Object tag = newEmpty.invoke();
            setMapField.invoke(tag, map);
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
        Object finalObject = null;
        try {
            if (map.isEmpty()) {
                finalObject = newEmpty.invoke();
            } else {
                Map<String, Object> tags = new HashMap<>();
                map.forEach((key, value) -> tags.put(key, mirror.newTag(value)));
                finalObject = newTag(tags);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return finalObject;
    }

    /**
     * Get current tag map.
     *
     * @param tag NBTTagCompound instance.
     * @return    A Map of NBTBase Objects.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getValue(Object tag) throws Throwable {
        return (Map<String, Object>) getMapField.invoke(tag);
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
        Map<String, Object> map = new HashMap<>();
        try {
            for (String key : (Set<String>) getKeys.invoke(tag)) {
                map.put(key, mirror.getTagValue(get.invoke(tag, key)));
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return map;
    }

    /**
     * Copy provided NBTTagCompound into new one without exceptions.
     *
     * @param tag NBTTagCompound instance.
     * @return    A copy of original NBTTagCompound.
     */
    public static Object safeClone(Object tag) {
        return EasyLookup.safeInvoke(clone, tag);
    }

    /**
     * Copy provided NBTTagCompound into new one.
     *
     * @param tag NBTTagCompound instance.
     * @return    A copy of original NBTTagCompound.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static Object clone(Object tag) throws Throwable {
        return clone.invoke(tag);
    }

    /**
     * The inverse result of {@link #hasKey(Object, String)}.
     *
     * @param tag NBTTagCompound instance.
     * @param key Key to find.
     * @return    True if key exist.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static boolean notHasKey(Object tag, String key) throws Throwable {
        return !(boolean) hasKey.invoke(tag, key);
    }

    /**
     * Check if NBTTagCompound contains certain key in Map.
     *
     * @param tag NBTTagCompound instance.
     * @param key Key to find.
     * @return    True if key exist.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static boolean hasKey(Object tag, String key) throws Throwable {
        return (boolean) hasKey.invoke(tag, key);
    }

    /**
     * Remove certain key from NBTTagCompound.
     *
     * @param tag NBTTagCompound instance.
     * @param key Key to remove.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static void remove(Object tag, String key) throws Throwable {
        remove.invoke(tag, key);
    }

    /**
     * Put certain NBTBase value to NBTTagCompound.
     *
     * @param tag   NBTTagCompound instance.
     * @param key   Value key.
     * @param value Value to put.
     * @return      The value that was set.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static Object set(Object tag, String key, Object value) throws Throwable {
        set.invoke(tag, key, value);
        return value;
    }

    /**
     * Get NBTBase value associated with key.
     *
     * @param tag NBTTagCompound instance.
     * @param key Value key.
     * @return    A NBTBase value if exist inside compound, null if not.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static Object get(Object tag, String key) throws Throwable {
        return get.invoke(tag, key);
    }
}
