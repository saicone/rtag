package com.saicone.rtag;

import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;
import com.saicone.rtag.util.EasyLookup;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Rtag class to edit NBTTagCompound & NBTTagList objects.<br>
 * Uses a tree-like path format to find the required tag
 * instead of creating multiple classes for deep-tags.<br></p>
 *
 * <h2>Object conversion</h2>
 * <p>The Rtag instance uses a {@link RtagMirror} to convert
 * objects between TagBase <-> Object.<br>
 * By default it's only compatible with regular Java
 * objects like String, Short, Integer, Double, Float,
 * Long, Byte, Map and List.<br>
 * It also convert Byte, Integer and Long arrays as well.<br></p>
 *
 * <b>Other Objects</b>
 * <p>If you want to add "custom object conversion" just
 * register a properly {@link RtagSerializer} and {@link RtagDeserializer}
 * that aims the specified object that you want to write and read
 * from tag.<br>
 * See {@link #putSerializer(Class, RtagSerializer)} and {@link #putDeserializer(RtagDeserializer)} for details.</p>
 *
 * @author Rubenicos
 */
public class Rtag {

    private static final Class<?> tagCompound = EasyLookup.classById("NBTTagCompound");
    private static final Class<?> tagList = EasyLookup.classById("NBTTagList");

    private final RtagMirror mirror;
    private final Map<String, RtagDeserializer<Object>> deserializers = new HashMap<>();
    private final Map<Class<?>, RtagSerializer<Object>> serializers = new HashMap<>();

    /**
     * Constructs an simple Rtag with default {@link RtagMirror} object.
     */
    public Rtag() {
        this(null);
    }

    /**
     * Constructs an Rtag with specified mirror.
     *
     * @param mirror Mirror to convert objects.
     */
    public Rtag(RtagMirror mirror) {
        this.mirror = mirror == null ? new RtagMirror(this) : mirror;
    }

    /**
     * Get current {@link RtagMirror} instance.
     *
     * @return A RtagMirror instance.
     */
    public RtagMirror getMirror() {
        return mirror;
    }

    /**
     * Register an {@link RtagDeserializer} for {@link #fromTag(Object)} operations.
     *
     * @param deserializer Deserializer instance.
     * @param <T>          Deserializable object type.
     * @return             Current {@link Rtag} instance.
     */
    @SuppressWarnings("unchecked")
    public <T> Rtag putDeserializer(RtagDeserializer<T> deserializer) {
        deserializers.put(deserializer.getOutID(), (RtagDeserializer<Object>) deserializer);
        return this;
    }

    /**
     * Register an {@link RtagSerializer} for {@link #toTag(Object)} operations.
     *
     * @param type       Serializable object class that match with Serializer.
     * @param serializer Serializer instance.
     * @param <T>        Serializable object type.
     * @return           Current {@link Rtag} instance.
     */
    @SuppressWarnings("unchecked")
    public <T> Rtag putSerializer(Class<T> type, RtagSerializer<T> serializer) {
        serializers.put(type, (RtagSerializer<Object>) serializer);
        return this;
    }

    /**
     * Add value to an NBTTagList on specified path inside tag.<br>
     * Note that empty path returns false because this method is
     * only made for lists inside compounds or lists.<br>
     * See {@link #get(Object, Object...)} for path information.
     *
     * @param tag   Tag instance, can be NBTTagCompound or NBTTagList.
     * @param value Value to add.
     * @param path  Final list path to add the specified value.
     * @return      True if value is added.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public boolean add(Object tag, Object value, Object... path) throws Throwable {
        if (path.length == 0) {
            return false;
        }
        Object finalTag = tag;
        for (int i = 0; i < path.length; i++) {
            Object key = path[i];
            if (key instanceof Integer && tagList.isInstance(finalTag)) {
                if (TagList.size(finalTag) >= (int) key) {
                    finalTag = TagList.get(finalTag, (int) key);
                } else {
                    // Out of bounds
                    return false;
                }
            } else if (tagCompound.isInstance(finalTag)) {
                String keyString = String.valueOf(key);
                // Create tag if not exists
                if (TagCompound.notHasKey(finalTag, keyString)) {
                    int i1 = i + 1;
                    if (path.length == i1 || path[i1] instanceof Integer) {
                        TagCompound.set(finalTag, keyString, TagList.newTag());
                    } else {
                        TagCompound.set(finalTag, keyString, TagCompound.newTag());
                    }
                }
                finalTag = TagCompound.get(finalTag, keyString);
            } else {
                // Incompatible tag
                return false;
            }
        }
        if (tagList.isInstance(finalTag)) {
            Object valueTag = toTag(value);
            if (valueTag != null) {
                TagList.add(finalTag, valueTag);
                return true;
            }
        }
        // Incompatible tag or value
        return false;
    }

    /**
     * Set value to specified path inside tag.<br>
     * Note that empty path returns false because this method is
     * only made for tags inside compounds or lists.<br>
     * If you want something like "remove", just put a null value.<br>
     * See {@link #get(Object, Object...)} for path information.
     *
     * @param tag   Tag instance, can be NBTTagCompound or NBTTagList.
     * @param value Value to set.
     * @param path  Final value path to set.
     * @return      True if the value is set.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public boolean set(Object tag, Object value, Object... path) throws Throwable {
        if (path.length == 0) {
            return false;
        }
        Object finalTag = tag;
        for (int i = 0; i < path.length - 1; i++) {
            Object key = path[i];
            if (key instanceof Integer && tagList.isInstance(finalTag)) {
                if (TagList.size(finalTag) >= (int) key) {
                    finalTag = TagList.get(finalTag, (int) key);
                } else {
                    // Out of bounds
                    return false;
                }
            } else if (tagCompound.isInstance(finalTag)) {
                String keyString = String.valueOf(key);
                // Create tag if not exists
                if (TagCompound.notHasKey(finalTag, keyString)) {
                    TagCompound.set(finalTag, keyString, path[i + 1] instanceof Integer ? TagList.newTag() : TagCompound.newTag());
                }
                finalTag = TagCompound.get(finalTag, keyString);
            } else {
                // Incompatible tag
                return false;
            }
        }
        Object key = path[path.length - 1];
        if (key instanceof Integer && tagList.isInstance(finalTag)) {
            if (value == null) {
                TagList.remove(finalTag, (int) key);
            } else {
                Object valueTag = toTag(value);
                if (valueTag != null) {
                    TagList.set(finalTag, (int) key, valueTag);
                } else {
                    // Fail on convert
                    return false;
                }
            }
        } else if (tagCompound.isInstance(finalTag)) {
            if (value == null) {
                TagCompound.remove(finalTag, String.valueOf(key));
            } else {
                Object valueTag = toTag(value);
                if (valueTag != null) {
                    TagCompound.set(finalTag, String.valueOf(key), valueTag);
                } else {
                    // Fail on convert
                    return false;
                }
            }
        } else {
            // Incompatible tag
            return false;
        }
        return true;
    }

    /**
     * Get value from the specified path inside tag.<br>
     * The value will be cast to the type are you looking for after conversion.<br>
     *
     * <h2>Path format</h2>
     * <p>Rtag uses a tree-like format for paths, every object inside
     * path can be {@link Integer} or {@link String} and will used
     * to obtain the last possible NBTBase instance.<br>
     * Path like ["normal", "path", "asd"] will look inside the NBTTagCompound
     * for "normal" key, if value assigned for that key is instance of
     * NBTTagCompound will look inside for the next key in path.<br>
     * If current path key is instance of Integer and the current
     * value that Rtag looking at is instance of NBTTagList, will get
     * list index value for that path key.</p>
     *
     * @param tag  Tag instance, can be NBTTagCompound or NBTTagList.
     * @param path Final value path to get.
     * @param <T>  Object type to cast the value.
     * @return     The value assigned to specified path, null if not
     *             exist or a ClassCastException occurs.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public <T> T get(Object tag, Object... path) throws Throwable {
        return fromTag(getExact(tag, path));
    }

    /**
     * Get exact NBTBase value without any conversion, from the specified path inside tag.<br>
     * See {@link #get(Object, Object...)} for path information.
     *
     * @param tag  Tag instance, can be NBTTagCompound or NBTTagList.
     * @param path Final value path to get.
     * @return     The value assigned to specified path, null if not exist.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public Object getExact(Object tag, Object... path) throws Throwable {
        Object finalTag = tag;
        for (Object key : path) {
            if (key instanceof Integer && tagList.isInstance(finalTag)) {
                if (TagList.size(finalTag) >= (int) key) {
                    finalTag = TagList.get(finalTag, (int) key);
                } else {
                    // Out of bounds
                    return null;
                }
            } else if (tagCompound.isInstance(finalTag)) {
                finalTag = TagCompound.get(finalTag, String.valueOf(key));
                if (finalTag == null) {
                    // Unknown path
                    return null;
                }
            } else {
                // Incompatible tag
                return null;
            }
        }
        return finalTag;
    }

    /**
     * Convert any object to NBTBase tag.<br>
     * This method first check for any serializer an then use the current {@link RtagMirror}.
     *
     * @param object Object to convert.
     * @return       Converted object instance of NBTBase or null.
     */
    public Object toTag(Object object) {
        if (serializers.containsKey(object.getClass())) {
            RtagSerializer<Object> serializer = serializers.get(object.getClass());
            Map<String, Object> map = serializer.serialize(object);
            map.put("rtag==", serializer.getInID());
            return toTag(map);
        } else {
            return mirror.toTag(object);
        }
    }

    /**
     * Convert any NBTBase tag to regular Java object or custom by deserializer.<br>
     * This method will cast the object to the type you looking for.
     *
     * @param tag NBTBase tag.
     * @param <T> Object type to cast the value.
     * @return    Converted value, null if any error occurs.
     */
    @SuppressWarnings("unchecked")
    public <T> T fromTag(Object tag) {
        try {
            return (T) fromTagExact(tag);
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * Convert any NBTBase tag to exact regular Java object
     * or custom by deserializer without any cast.
     *
     * @param tag NBTBase tag.
     * @return    Converted value or null.
     */
    @SuppressWarnings("unchecked")
    public Object fromTagExact(Object tag) {
        Object object = mirror.fromTag(tag);
        if (object instanceof Map) {
            Object type = ((Map<String, Object>) object).get("rtag==");
            if (type instanceof String && deserializers.containsKey((String) type)) {
                return deserializers.get((String) type).deserialize((Map<String, Object>) object);
            }
        }
        return object;
    }
}
