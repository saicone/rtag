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

    private final Rtag rtag;

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
     * Convert any object to NBTBase tag.
     *
     * @param object Object to convert.
     * @return       Converted NBTBase or null;
     */
    @SuppressWarnings("unchecked")
    public Object toTag(Object object) {
        if (nbtBase.isInstance(object)) {
            return object;
        } else if (object instanceof Map) {
            return TagCompound.newTag(getRtag(), (Map<String, Object>) object);
        } else if (object instanceof List) {
            return TagList.newTag(getRtag(), (List<Object>) object);
        } else {
            return TagBase.newTag(object);
        }
    }

    /**
     * Convert any NBTBase tag to regular Java object.
     *
     * @param tag Tag to convert.
     * @return    Converted object.
     */
    public Object fromTag(Object tag) {
        if (tagCompound.isInstance(tag)) {
            return TagCompound.getValue(getRtag(), tag);
        } else if (tagList.isInstance(tag)) {
            return TagList.getValue(getRtag(), tag);
        } else {
            return TagBase.getValue(tag);
        }
    }
}
