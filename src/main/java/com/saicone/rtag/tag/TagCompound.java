package com.saicone.rtag.tag;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.saicone.rtag.RtagMirror;
import com.saicone.rtag.stream.TStream;
import com.saicone.rtag.util.MC;
import com.saicone.rtag.util.OptionalType;
import com.saicone.rtag.util.reflect.Lookup;
import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to invoke CompoundTag methods across versions.
 *
 * @author Rubenicos
 */
public class TagCompound {

    // import
    private static final Lookup.AClass<?> CompoundTag = Lookup.SERVER.importClass("net.minecraft.nbt.CompoundTag");
    private static final Lookup.AClass<?> Tag = Lookup.SERVER.importClass("net.minecraft.nbt.Tag");
    private static final Lookup.AClass<?> TagParser = Lookup.SERVER.importClass("net.minecraft.nbt.TagParser");

    // declare
    private static final MethodHandle CompoundTag$new = CompoundTag.constructor().handle();
    private static final MethodHandle CompoundTag$new_tags;
    static {
        if (MC.version().isNewerThanOrEquals(MC.V_1_15)) {
            CompoundTag$new_tags = CompoundTag.constructor(Map.class).handle();
        } else {
            CompoundTag$new_tags = null;
        }
    }
    private static final MethodHandle CompoundTag$get_tags = CompoundTag.field(Map.class, "tags").getter();
    private static final MethodHandle CompoundTag$set_tags = CompoundTag.field(Map.class, "tags").setter();
    private static final MethodHandle CompoundTag_copy;
    static {
        if (MC.version().isNewerThanOrEquals(MC.V_1_10)) {
            CompoundTag_copy = CompoundTag.method(CompoundTag, "copy").handle();
        } else {
            CompoundTag_copy = CompoundTag.method(Tag, "copy").handle();
        }
    }

    private static final MethodHandle TagParser_parseCompoundFully;
    static {
        if (MC.version().isNewerThanOrEquals(MC.V_1_21_5)) {
            TagParser_parseCompoundFully = TagParser.method(Modifier.STATIC, CompoundTag, "parseCompoundFully", String.class).handle();
        } else {
            TagParser_parseCompoundFully = TagParser.method(Modifier.STATIC, CompoundTag, "parseTag", String.class).handle();
        }
    }

    /**
     * Tag stream instance to save and get compounds.
     *
     * @deprecated Use {@link TStream#COMPOUND} instead.
     * @see TStream#COMPOUND
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.6.0")
    @Deprecated
    public static final TStream<Object> DATA = TStream.COMPOUND;

    TagCompound() {
    }

    /**
     * Constructs an empty CompoundTag.
     *
     * @return New CompoundTag instance.
     */
    public static Object newTag() {
        try {
            return CompoundTag$new.invoke();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Constructs an CompoundTag with provided NBT string.
     *
     * @param snbt NBT String with data.
     * @return     New CompoundTag instance.
     */
    public static Object newTag(String snbt) {
        try {
            return TagParser_parseCompoundFully.invoke(snbt);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Constructs an CompoundTag with provided Map of tag values.
     *
     * @param map Map with tags.
     * @return    New CompoundTag instance.
     */
    public static Object newTag(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return newTag();
        }

        // Check if map is mutable
        try {
            map.putAll(Map.of());
        } catch (UnsupportedOperationException e) {
            return newTag(new HashMap<>(map));
        }

        return newUncheckedTag(map);
    }

    /**
     * Constructs an CompoundTag with provided Map of tag values.<br>
     * This method doesn't provide any safe check and assumes that the provided
     * map is completely usable to create a new CompoundTag.
     *
     * @param map Map with tags.
     * @return    New CompoundTag instance.
     */
    public static Object newUncheckedTag(Map<String, Object> map) {
        if (MC.version().isNewerThanOrEquals(MC.V_1_15)) {
            try {
                return CompoundTag$new_tags.invoke(map);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        } else {
            final Object tag = newTag();
            try {
                CompoundTag$set_tags.invoke(tag, map);
            } catch (ClassCastException e) {
                getValue(tag).putAll(map);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
            return tag;
        }
    }

    /**
     * Constructs an CompoundTag with provided object
     * and required {@link RtagMirror} to convert Objects.<br>
     * This method can convert any supported object to Map
     * of objects using Gson deserializer.
     *
     * @param mirror RtagMirror to convert objects into tags.
     * @param object Object that can be converted to CompoundTag.
     * @return       New CompoundTag instance.
     * @throws IllegalArgumentException if the object is not supported.
     */
    @SuppressWarnings("unchecked")
    public static Object newTag(RtagMirror mirror, Object object) {
        final Map<String, Object> map = (Map<String, Object>) OptionalType.of(object).as(Map.class);
        if (map == null) {
            throw new IllegalArgumentException("The object type " + object.getClass().getName() + " cannot be used to create CompoundTag using TagCompound class");
        }
        return newTag(mirror, map);
    }

    /**
     * Constructs an CompoundTag with provided Map of Objects
     * and required {@link RtagMirror} to convert Objects.
     *
     * @param mirror RtagMirror to convert objects into tags.
     * @param map    Map with objects.
     * @return       New CompoundTag instance.
     */
    public static Object newTag(RtagMirror mirror, Map<String, Object> map) {
        if (map.isEmpty()) {
            return newTag();
        }

        final Map<String, Object> tags = new HashMap<>();
        for (var entry : map.entrySet()) {
            tags.put(entry.getKey(), mirror.newTag(entry.getValue()));
        }
        return newUncheckedTag(tags);
    }

    /**
     * Check if the provided object is instance of CompoundTag class.
     *
     * @param object the object to check.
     * @return       true if the object is an instance of CompoundTag class.
     */
    public static boolean isTagCompound(Object object) {
        return CompoundTag.isInstance(object);
    }

    /**
     * Copy provided CompoundTag into new one.
     *
     * @param tag CompoundTag instance.
     * @return    A copy of original CompoundTag.
     */
    public static Object clone(Object tag) {
        try {
            return CompoundTag_copy.invoke(tag);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Copy provided CompoundTag into new one without exceptions.
     *
     * @param tag CompoundTag instance.
     * @return    A copy of original CompoundTag.
     */
    public static Object safeClone(Object tag) {
        try {
            return CompoundTag_copy.invoke(tag);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Get current tag map.
     *
     * @param tag CompoundTag instance.
     * @return    a Map of tags.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getValue(Object tag) {
        try {
            return (Map<String, Object>) CompoundTag$get_tags.invoke(tag);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Get current tag map with converted values.
     *
     * @param mirror RtagMirror to convert tags.
     * @param tag    CompoundTag instance.
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
     * Get the provided CompoundTag as Json string.
     *
     * @param tag CompoundTag instance.
     * @return    A Json string.
     */
    @SuppressWarnings("all")
    public static String getJson(Object tag) {
        return getJson(new Gson(), tag);
    }

    /**
     * Get the provided CompoundTag as Json string.
     *
     * @param gson The Gson instance to use.
     * @param tag  CompoundTag instance.
     * @return     A Json string.
     */
    @SuppressWarnings("all")
    public static String getJson(Gson gson, Object tag) {
        final Type type = new TypeToken(){}.getType();
        return gson.toJson(getValue(RtagMirror.INSTANCE, tag), type);
    }

    /**
     * The inverse result of {@link #hasKey(Object, String)}.
     *
     * @param tag CompoundTag instance.
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
     * Check if CompoundTag contains certain key in Map.
     *
     * @param tag CompoundTag instance.
     * @param key Key to find.
     * @return    True if key exist.
     */
    public static boolean hasKey(Object tag, String key) {
        return getValue(tag).containsKey(key);
    }

    /**
     * Remove certain key from CompoundTag.
     *
     * @param tag CompoundTag instance.
     * @param key Key to remove.
     * @return    The previous value associated with key, or null if there was no mapping for key.
     */
    public static Object remove(Object tag, String key) {
        return getValue(tag).remove(key);
    }

    /**
     * Put certain Tag value to CompoundTag.
     *
     * @param tag   CompoundTag instance.
     * @param key   Value key.
     * @param value Value to put.
     * @return      The previous value associated with key, or null if there was no mapping for key.
     */
    public static Object set(Object tag, String key, Object value) {
        return getValue(tag).put(key, value);
    }

    /**
     * Override the current map of tag values inside CompoundTag.
     *
     * @param tag CompoundTag instance.
     * @param map Map with tag objects.
     */
    public static void setValue(Object tag, Map<String, Object> map) {
        if (map.isEmpty()) {
            clear(tag);
        } else {
            try {
                CompoundTag$set_tags.invoke(tag, map);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    /**
     * Merge the provided value into CompoundTag.
     *
     * @param tag     CompoundTag instance.
     * @param value   The value to merge.
     * @param replace True to replace the repeated values inside CompoundTag.
     * @return        true if the value was merged.
     */
    public static boolean merge(Object tag, Object value, boolean replace) {
        return merge(tag, value, replace, false);
    }

    /**
     * Merge the provided value into CompoundTag.
     *
     * @param tag     CompoundTag instance.
     * @param value   The value to merge.
     * @param replace True to replace the repeated values inside CompoundTag.
     * @param deep    True to merge into sub paths instead of main path.
     * @return        true if the value was merged.
     */
    public static boolean merge(Object tag, Object value, boolean replace, boolean deep) {
        if (!isTagCompound(value)) {
            return false;
        }
        final Map<String, Object> from = getValue(value);
        final Map<String, Object> to = getValue(tag);

        boolean merged = false;
        Object tempValue;
        for (var entry : from.entrySet()) {
            if (!to.containsKey(entry.getKey())) {
                to.put(entry.getKey(), entry.getValue());
                merged = true;
            } else if (deep && isTagCompound(entry.getValue()) && isTagCompound((tempValue = to.get(entry.getKey())))) {
                final boolean result = merge(tempValue, entry.getValue(), replace, true);
                if (!merged && result) {
                    merged = true;
                }
            } else if (replace) {
                to.put(entry.getKey(), entry.getValue());
                merged = true;
            }
        }
        return merged;
    }

    /**
     * Get Tag value associated with key.
     *
     * @param tag CompoundTag instance.
     * @param key Value key.
     * @return    A Tag value if exist inside compound, null if not.
     */
    public static Object get(Object tag, String key) {
        return getValue(tag).get(key);
    }

    /**
     * Clear the provided CompoundTag tag.
     *
     * @param tag CompoundTag instance.
     */
    public static void clear(Object tag) {
        getValue(tag).clear();
    }
}
