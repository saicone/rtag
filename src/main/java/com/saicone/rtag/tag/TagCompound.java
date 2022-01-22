package com.saicone.rtag.tag;

import com.saicone.rtag.Rtag;
import com.saicone.rtag.data.TagData;
import com.saicone.rtag.util.EasyLookup;
import com.saicone.rtag.util.ServerInstance;

import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TagCompound {

    public static final TagData<Object> DATA = new TagData<>() {
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
                m2 = EasyLookup.unreflectConstructor(nbtCompound, Map.class);
            } else {
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
        newEmpty = m1; newCompound = m2; mapField = m3; clone = m4; hasKey = m5; remove = m6; set = m7; get = m8; getKeys = m9;
    }

    public static Object newTag() throws Throwable {
        return newEmpty.invoke();
    }

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

    public static Object safeClone(Object tag) {
        return EasyLookup.safeInvoke(clone, tag);
    }

    public static Object clone(Object tag) throws Throwable {
        return clone.invoke(tag);
    }

    public static boolean notHasKey(Object tag, String key) throws Throwable {
        return !(boolean) hasKey.invoke(tag, key);
    }

    public static boolean hasKey(Object tag, String key) throws Throwable {
        return (boolean) hasKey.invoke(tag, key);
    }

    public static void remove(Object tag, String key) throws Throwable {
        remove.invoke(tag, key);
    }

    public static void set(Object tag, String key, Object value) throws Throwable {
        set.invoke(tag, key, value);
    }

    public static Object get(Object tag, String key) throws Throwable {
        return get.invoke(tag, key);
    }
}
