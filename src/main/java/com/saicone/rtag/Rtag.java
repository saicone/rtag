package com.saicone.rtag;

import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;
import com.saicone.rtag.util.OptionalType;
import com.saicone.rtag.util.ThrowableFunction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * <p>Rtag class to edit NBTTagCompound &amp; NBTTagList objects.<br>
 * Uses a tree-like path format to find the required tag
 * instead of creating multiple classes for deep-tags.<br></p>
 *
 * <h2>Object conversion</h2>
 * <p>The Rtag instance extends {@link RtagMirror} to convert
 * objects between TagBase &lt;-&gt; Object.<br>
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
public class Rtag extends RtagMirror {

    private static final BiPredicate<Integer, Object[]> addPredicate = (index, path) -> path.length == index || path[index] instanceof Integer;
    private static final BiPredicate<Integer, Object[]> setPredicate = (index, path) -> path.length > index && path[index] instanceof Integer;

    /**
     * {@link Rtag} public instance only compatible with regular Java objects.
     */
    public static final Rtag INSTANCE = new Rtag();

    private final Map<String, RtagDeserializer<Object>> deserializers = new HashMap<>();
    private final Map<Class<?>, RtagSerializer<Object>> serializers = new HashMap<>();

    /**
     * Create new {@link Rtag} instance.
     */
    public Rtag() {
    }

    /**
     * Create new {@link Rtag} instance without use mirror parameter,
     * because the class extends {@link RtagMirror} itself.
     *
     * @deprecated {@link Rtag} extends {@link RtagMirror}.
     *
     * @param mirror Mirror instance.
     */
    @Deprecated
    @SuppressWarnings("unused")
    public Rtag(RtagMirror mirror) {
    }

    /**
     * Get this object as {@link RtagMirror} instance.
     *
     * @deprecated {@link Rtag} extends {@link RtagMirror}.
     *
     * @return The Rtag itself.
     */
    @Deprecated
    public RtagMirror getMirror() {
        return this;
    }

    /**
     * Register an {@link RtagDeserializer} for {@link #getTagValue(Object)} operations.
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
     * Register an {@link RtagSerializer} for {@link #newTag(Object)} operations.
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
     * @return      true if value was added.
     */
    public boolean add(Object tag, Object value, Object... path) {
        if (path.length == 0) {
            return false;
        }
        Object finalTag = getExactOrCreate(tag, path, addPredicate);
        if (TAG_LIST.isInstance(finalTag)) {
            Object valueTag = newTag(value);
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
     * @return      true if the value was set.
     */
    public boolean set(Object tag, Object value, Object... path) {
        if (path.length == 0) {
            return false;
        } else {
            int last = path.length - 1;
            Object finalTag = getExactOrCreate(tag, Arrays.copyOf(path, last), value == null ? null : setPredicate);
            if (finalTag == null) {
                return false;
            } else if (value == null) {
                return removeExact(finalTag, path[last]);
            } else {
                return setExact(finalTag, value, path[last]);
            }
        }
    }

    /**
     * Set value to exact NBTTag list or compound.
     *
     * @param tag   Tag instance, can be NBTTagCompound or NBTTagList.
     * @param value Value to set.
     * @param key   Key associated with value.
     * @return      true if the value was set.
     */
    public boolean setExact(Object tag, Object value, Object key) {
        if (key instanceof Integer && TAG_LIST.isInstance(tag)) {
            Object valueTag = newTag(value);
            if (valueTag != null) {
                TagList.set(tag, (int) key, valueTag);
                return true;
            }
        } else if (TAG_COMPOUND.isInstance(tag)) {
            Object valueTag = newTag(value);
            if (valueTag != null) {
                TagCompound.set(tag, String.valueOf(key), valueTag);
                return true;
            }
        }
        // Incompatible tag or value
        return false;
    }

    /**
     * Merge the provided value with NBTTagCompound at provided path.
     *
     * @param tag     Tag instance, can be NBTTagCompound or NBTTagList.
     * @param value   The value to merge.
     * @param replace True to replace the repeated values inside NBTTagCompound.
     * @param path    Final value path to merge into.
     * @return        true if the value was merged.
     */
    public boolean merge(Object tag, Object value, boolean replace, Object... path) {
        return getExactOrCreate(tag, path, setPredicate, finalTag -> TagCompound.merge(finalTag, newTag(value), replace));
    }

    /**
     * Merge the provided value with NBTTagCompound at provided path using deep method.
     *
     * @param tag     Tag instance, can be NBTTagCompound or NBTTagList.
     * @param value   The value to merge.
     * @param replace True to replace the repeated values inside NBTTagCompound.
     * @param path    Final value path to merge into.
     * @return        true if the value was merged.
     */
    public boolean deepMerge(Object tag, Object value, boolean replace, Object... path) {
        return getExactOrCreate(tag, path, setPredicate, finalTag -> TagCompound.merge(finalTag, newTag(value), replace, true));
    }

    /**
     * Move tag from specified path to any path.
     *
     * @param tag   Tag instance, can be NBTTagCompound or NBTTagList.
     * @param from  Path to get the value.
     * @param to    Path to set the value.
     * @return      true if the value was moved.
     */
    public boolean move(Object tag, Object[] from, Object[] to) {
        return move(tag, from, to, true);
    }

    /**
     * Move tag from specified path to any path.
     *
     * @param tag   Tag instance, can be NBTTagCompound or NBTTagList.
     * @param from  Path to get the value.
     * @param to    Path to set the value.
     * @param clear True to clear empty paths.
     * @return      true if the value was moved.
     */
    public boolean move(Object tag, Object[] from, Object[] to, boolean clear) {
        final Object value = getExact(tag, from);
        if (value == null) {
            return false;
        }
        final boolean result = set(tag, value, to);
        if (!result) {
            return false;
        }
        if (clear && from.length > 1) {
            Object[] path = from;
            for (int i = 1; i < from.length; i++) {
                final Object[] copy = Arrays.copyOf(from, from.length - i);
                final int size = TagBase.size(getExact(tag, copy));
                if (size < 0 || size > 1) {
                    break;
                }
                path = copy;
            }
            set(tag, null, path);
        } else {
            set(tag, null, from);
        }
        return true;
    }

    /**
     * Remove value from exact NBTTag list or compound.
     *
     * @param tag Tag instance, can be NBTTagCompound or NBTTagList.
     * @param key Key associated with value.
     * @return    true if the value is removed (or don't exist).
     */
    public boolean removeExact(Object tag, Object key) {
        if (key instanceof Integer && TAG_LIST.isInstance(tag)) {
            TagList.remove(tag, (int) key);
        } else if (TAG_COMPOUND.isInstance(tag)) {
            TagCompound.remove(tag, String.valueOf(key));
        } else {
            // Incompatible tag
            return false;
        }
        return true;
    }

    /**
     * Get value from the specified path inside tag.<br>
     * The value will be cast to the type are you looking for after conversion.<br>
     * <br>
     * <b>Path format</b>
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
     */
    public <T> T get(Object tag, Object... path) {
        return OptionalType.cast(getTagValue(getExact(tag, path)));
    }

    /**
     * Same has {@link #get(Object, Object...)} but save the value into {@link OptionalType}.
     *
     * @param tag  Tag instance, can be NBTTagCompound or NBTTagList.
     * @param path Final value path to get.
     * @return     The value assigned to specified path has {@link OptionalType}.
     */
    public OptionalType getOptional(Object tag, Object... path) {
        return OptionalType.of(getTagValue(getExact(tag, path)));
    }

    /**
     * Get exact NBTBase value without any conversion, from the specified path inside tag.<br>
     * See {@link #get(Object, Object...)} for path information.
     *
     * @param tag  Tag instance, can be NBTTagCompound or NBTTagList.
     * @param path Final value path to get.
     * @return     The value assigned to specified path, null if not exist.
     */
    public Object getExact(Object tag, Object... path) {
        return getExactOrCreate(tag, path, null);
    }

    /**
     * Get exact NBTBase value without any conversion, from the specified path inside tag.<br>
     * See {@link #get(Object, Object...)} for path information.
     *
     * @param tag           Tag instance, can be NBTTagCompound or NBTTagList.
     * @param path          Final value path to get.
     * @param listPredicate Predicate to set new NBTTagList if NBTTagCompound doesn't contain key.
     * @return              The value assigned to specified path or null.
     */
    @SuppressWarnings("unchecked")
    public Object getExactOrCreate(Object tag, Object[] path, BiPredicate<Integer, Object[]> listPredicate) {
        Object finalTag = tag;
        for (int i = 0; i < path.length; i++) {
            Object key = path[i];
            if (key instanceof ThrowableFunction) {
                try {
                    finalTag = ((ThrowableFunction<Object, Object>) key).apply(finalTag);
                } catch (Throwable t) {
                    return null;
                }
                if (finalTag == null) {
                    return null;
                }
            } else if (key instanceof Integer && TAG_LIST.isInstance(finalTag)) {
                if ((int) key >= 0 ? TagList.size(finalTag) > (int) key : TagList.size(finalTag) >= Math.abs((int) key))  {
                    finalTag = TagList.get(finalTag, (int) key);
                } else {
                    // Out of bounds
                    return null;
                }
            } else if (TAG_COMPOUND.isInstance(finalTag)) {
                String keyString = String.valueOf(key);
                // Create tag if not exists
                if (listPredicate != null && TagCompound.notHasKey(finalTag, keyString)) {
                    TagCompound.set(finalTag, keyString, listPredicate.test(i + 1, path) ? TagList.newTag() : TagCompound.newTag());
                }
                finalTag = TagCompound.get(finalTag, keyString);
                if (finalTag == null) {
                    // Unknown path or incompatible tag
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
     * Get and test exact NBTBase value without any conversion, from the specified path inside tag.<br>
     * See {@link #get(Object, Object...)} for path information.
     *
     * @param tag           Tag instance, can be NBTTagCompound or NBTTagList.
     * @param path          Final value path to get.
     * @param listPredicate Predicate to set new NBTTagList if NBTTagCompound doesn't contain key.
     * @param predicate     Consumer that accept non-null value.
     * @return              true if the value was consumed.
     */
    public boolean getExactOrCreate(Object tag, Object[] path, BiPredicate<Integer, Object[]> listPredicate, Predicate<Object> predicate) {
        final Object finalTag = getExactOrCreate(tag, path, listPredicate);
        if (finalTag == null) {
            return false;
        }
        return predicate.test(finalTag);
    }

    /**
     * Convert any NBTBase tag to exact regular Java object
     * or custom by deserializer without any cast.
     *
     * @param tag NBTBase tag.
     * @return    Converted value or null.
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object getTagValue(Object tag) {
        Object object = super.getTagValue(tag);
        if (object instanceof Map) {
            Object type = ((Map<String, Object>) object).get("rtag==");
            if (type instanceof String && deserializers.containsKey((String) type)) {
                return deserializers.get((String) type).deserialize((Map<String, Object>) object);
            }
        }
        return object;
    }

    /**
     * Convert any object to NBTBase tag.<br>
     * This method first check for any serializer and then use the current {@link RtagMirror}.
     *
     * @param object Object to convert.
     * @return       Converted object instance of NBTBase or null.
     */
    @Override
    public Object newTag(Object object) {
        if (object == null) {
            return null;
        } else if (serializers.containsKey(object.getClass())) {
            RtagSerializer<Object> serializer = serializers.get(object.getClass());
            Map<String, Object> map = serializer.serialize(object);
            map.put("rtag==", serializer.getInID());
            return newTag(map);
        } else {
            return super.newTag(object);
        }
    }

    /**
     * Convert any object to NBTBase tag.
     *
     * @deprecated To create tag object use {@link #newTag(Object)} instead.
     * @see #newTag(Object)
     *
     * @param object Object to convert.
     * @return       NBTBase tag or null.
     */
    @Deprecated
    public Object toTag(Object object) {
        return newTag(object);
    }

    /**
     * Convert any NBTBase tag to regular Java object or custom by deserializer.<br>
     * This method will cast the object to the type you're looking for.
     *
     * @see #getTagValue(Object)
     * @see OptionalType#cast(Object) 
     * 
     * @param tag NBTBase tag.
     * @param <T> Object type to cast the value.
     * @return    Converted value, null if any error occurs.
     */
    @Deprecated
    public <T> T fromTag(Object tag) {
        return OptionalType.cast(getTagValue(tag));
    }

    /**
     * Convert any NBTBase tag to regular Java object.
     *
     * @deprecated To get tag value without conversion use {@link #getTagValue(Object)} instead.
     * @see #getTagValue(Object)
     *
     * @param tag Tag to convert.
     * @return    Converted object.
     */
    @Deprecated
    public Object fromTagExact(Object tag) {
        return getTagValue(tag);
    }
}
