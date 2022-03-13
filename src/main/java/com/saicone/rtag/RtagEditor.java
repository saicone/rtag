package com.saicone.rtag;

import com.saicone.rtag.util.EasyLookup;

import java.util.Map;

/**
 * RtagEditor abstract class who edit any object
 * with NBTTagCompound inside.<br>
 * Also provide methods to easy-edit object tags
 * using a {@link Rtag} instance.
 *
 * @author Rubenicos
 *
 * @param <T> Object type to return after load changes.
 */
public abstract class RtagEditor<T> {

    private static final Class<?> tagCompound = EasyLookup.classById("NBTTagCompound");

    private final Rtag rtag;
    private final Object object;
    private Object tag;

    /**
     * Constructs an NBTEditor with specified Rtag parent.
     *
     * @param rtag   Rtag parent.
     * @param object Object instance with NBTTagCompound inside.
     * @param tag    Object tag to edit.
     */
    public RtagEditor(Rtag rtag, Object object, Object tag) {
        this.rtag = rtag;
        this.object = object;
        this.tag = tag;
    }

    /**
     * Get current {@link Rtag} parent.
     *
     * @return A Rtag instance.
     */
    public Rtag getRtag() {
        return rtag;
    }

    /**
     * Get current object instance.
     *
     * @return A object with NBTTagCompound inside.
     */
    public Object getObject() {
        return object;
    }

    /**
     * Get current tag.
     *
     * @return A NBTTagCompound.
     */
    public Object getTag() {
        return tag;
    }

    /**
     * Load changes into object instance.
     */
    public abstract void load();

    /**
     * Add value to an NBTTagList on specified path inside current object tag.<br>
     * See {@link Rtag#add(Object, Object, Object...)} for more information.
     *
     * @param value Value to add.
     * @param path  Final list path to add the specified value.
     * @return      True if value is added.
     */
    public boolean add(Object value, Object... path) {
        try {
            return rtag.add(tag, value, path);
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    /**
     * Change object tag into new one.<br>
     * Value must be Map&lt;String, Object&gt; or NBTTagListCompound.
     *
     * @param value Object to replace current tag.
     * @return      True if tag has replaced.
     */
    public boolean set(Object value) {
        Object tag = rtag.toTag(value);
        if (tagCompound.isInstance(tag)) {
            this.tag = tag;
            return true;
        }
        return false;
    }

    /**
     * Set value to specified path inside current tag.<br>
     * See {@link Rtag#set(Object, Object, Object...)} for more information.
     * 
     * @param value Value to set.
     * @param path  Final value path to set.
     * @return      True if the value is set.
     */
    public boolean set(Object value, Object... path) {
        try {
            return rtag.set(tag, value, path);
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    /**
     * Remove value to specified path inside current tag.<br>
     * See {@link Rtag#set(Object, Object, Object...)} for more information.
     *
     * @param path Final value path to remove.
     * @return     True if the value associated with the path exists and is removed.
     */
    public boolean remove(Object... path) {
        try {
            return rtag.set(tag, null, path);
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    /**
     * Get a converted version of current tag.
     *
     * @return A Map with objects converted by Rtag parent.
     */
    public Map<String, Object> get() {
        return rtag.fromTag(tag);
    }

    /**
     * Get value from the specified path inside current tag.<br>
     * See {@link Rtag#get(Object, Object...)} for more information.
     *
     * @param path Final value path to get.
     * @param <V>  Object type to cast the value.
     * @return     The value assigned to specified path, null if not
     *             exist or a ClassCastException occurs.
     */
    public <V> V get(Object... path) {
        try {
            return rtag.get(tag, path);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Get exact NBTBase value without any conversion,
     * from the specified path inside current tag.<br>
     * See {@link Rtag#getExact(Object, Object...)} for more information.
     *
     * @param path Final value path to get.
     * @return     The value assigned to specified path, null if not exist.
     */
    public Object getExact(Object... path) {
        try {
            return rtag.getExact(tag, path);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }
}
