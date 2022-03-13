package com.saicone.rtag.tag;

import com.saicone.rtag.Rtag;
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
    private static final MethodHandle mapField;
    private static final MethodHandle clone;
    private static final MethodHandle hasKey;
    private static final MethodHandle remove;
    private static final MethodHandle set;
    private static final MethodHandle get;
    private static final MethodHandle getKeys;

    static {
        MethodHandle m1 = null, m2 = null, m3 = null, m4 = null, m5 = null, m6 = null, m7 = null, m8 = null, m9 = null;
        try {
            Class<?> base = EasyLookup.classById("NBTBase");
            // Old names
            String clone = "clone", hasKey = "hasKey", remove = "remove", set = "set", get = "get", getKeys = "c";
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

            m1 = EasyLookup.constructor(nbtCompound);
            if (ServerInstance.verNumber >= 15) {
                // Private method
                m2 = EasyLookup.unreflectConstructor(nbtCompound, Map.class);
            } else {
                // Private field
                m3 = EasyLookup.unreflectSetter(nbtCompound, "map");
            }
            // Unreflect reason:
            // (1.8 -  1.9) return NBTBase
            // Other versions return NBTTagCompound
            m4 = EasyLookup.unreflectMethod(nbtCompound, clone);
            m5 = EasyLookup.method(nbtCompound, hasKey, boolean.class, String.class);
            m6 = EasyLookup.method(nbtCompound, remove, void.class, String.class);
            // Unreflect reason:
            // (1.8 -  1.13) void method
            // Other versions return NBTBase
            m7 = EasyLookup.unreflectMethod(nbtCompound, set, String.class, base);
            m8 = EasyLookup.method(nbtCompound, get, base, String.class);
            m9 = EasyLookup.method(nbtCompound, getKeys, Set.class);
        } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        newEmpty = m1;
        newCompound = m2;
        mapField = m3;
        clone = m4;
        hasKey = m5;
        remove = m6;
        set = m7;
        get = m8;
        getKeys = m9;
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
            mapField.invoke(tag, map);
            return tag;
        }
    }

    /**
     * Constructs an NBTTagCompound with provided Map of Objects
     * and required {@link Rtag} to convert Objects.
     *
     * @param rtag Rtag parent to convert objects into tags.
     * @param map  Map with objects.
     * @return     New NBTTagCompound instance.
     */
    public static Object newTag(Rtag rtag, Map<String, Object> map) {
        Object finalObject = null;
        try {
            if (map.isEmpty()) {
                finalObject = newEmpty.invoke();
            } else {
                Map<String, Object> tags = new HashMap<>();
                map.forEach((key, value) -> tags.put(key, rtag.toTag(value)));
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
     * @param rtag Rtag parent to convert tags.
     * @param tag  NBTTagCompound instance.
     * @return     A Map of Objects.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getValue(Rtag rtag, Object tag) {
        Map<String, Object> map = new HashMap<>();
        try {
            for (String key : (Set<String>) getKeys.invoke(tag)) {
                map.put(key, rtag.fromTagExact(get.invoke(tag, key)));
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
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static void set(Object tag, String key, Object value) throws Throwable {
        set.invoke(tag, key, value);
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
