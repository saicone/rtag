package com.saicone.rtag;

import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;
import com.saicone.rtag.util.EasyLookup;

import java.util.List;
import java.util.Map;

/**
 * <p>RtagMirror class to convert objects.<br>
 * By default it's only compatible with regular Java
 * objects like String, Short, Integer, Double, Float,
 * Long, Byte, Map and List.<br>
 * It also convert Byte, Integer and Long arrays as well.</p>
 *
 * @author Rubenicos
 */
public class RtagMirror {

    private static final Class<?> nbtBase = EasyLookup.classById("NBTBase");
    private static final Class<?> tagCompound = EasyLookup.classById("NBTTagCompound");
    private static final Class<?> tagList = EasyLookup.classById("NBTTagList");

    private Rtag rtag;

    /**
     * Constructs an RtagMirror without Rtag parent.
     * Not compatible with NBTTagCompound or NBTTagList.
     */
    public RtagMirror() {
        this(null);
    }

    /**
     * Constructs an RtagMirror with specified Rtag parent.
     *
     * @param rtag Rtag parent.
     */
    public RtagMirror(Rtag rtag) {
        this.rtag = rtag;
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
     * Set current {@link Rtag} parent.
     *
     * @param rtag Rtag to set.
     */
    public void setRtag(Rtag rtag) {
        this.rtag = rtag;
    }

    /**
     * Convert any object to NBTBase tag.
     *
     * @param object Object to convert.
     * @return       Converted NBTBase or null;
     */
    @SuppressWarnings("unchecked")
    public Object toTag(Object object) {
        if (nbtBase.isInstance(object)) {
            return object;
        } else if (getRtag() != null) {
            if (object instanceof Map) {
                return TagCompound.newTag(getRtag(), (Map<String, Object>) object);
            } else if (object instanceof List) {
                return TagList.newTag(getRtag(), (List<Object>) object);
            }
        }
        try {
            return TagBase.newTag(object);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Convert any NBTBase tag to regular Java object.
     *
     * @param tag Tag to convert.
     * @return    Converted object.
     */
    public Object fromTag(Object tag) {
        if (getRtag() != null) {
            if (tagCompound.isInstance(tag)) {
                return TagCompound.getValue(getRtag(), tag);
            } else if (tagList.isInstance(tag)) {
                return TagList.getValue(getRtag(), tag);
            }
        }
        try {
            return TagBase.getValue(tag);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }
}
