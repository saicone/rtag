package com.saicone.rtag;

import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;
import com.saicone.rtag.util.reflect.Lookup;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Map;

/**
 * <p>RtagMirror class to convert objects.<br>
 * By default, it's only compatible with regular Java
 * objects like String, Short, Integer, Double, Float,
 * Long, Byte, Map and List.<br>
 * It also converts Byte, Integer and Long arrays as well.</p>
 *
 * @author Rubenicos
 */
public class RtagMirror {

    /**
     * Tag class object.
     */
    protected static final Class<?> TAG_BASE = Lookup.SERVER.importClass("net.minecraft.nbt.Tag").get();
    /**
     * CompoundTag class object.
     */
    protected static final Class<?> TAG_COMPOUND = Lookup.SERVER.importClass("net.minecraft.nbt.CompoundTag").get();
    /**
     * ListTag class object.
     */
    protected static final Class<?> TAG_LIST = Lookup.SERVER.importClass("net.minecraft.nbt.ListTag").get();

    /**
     * {@link RtagMirror} public instance only compatible with regular Java objects.
     */
    public static final RtagMirror INSTANCE = new RtagMirror();

    /**
     * Constructs an {@link RtagMirror} only compatible with regular Java objects.
     */
    public RtagMirror() {
    }

    /**
     * Constructs an {@link RtagMirror} only compatible with regular Java objects,
     * ignoring the provided {@link Rtag}.
     *
     * @deprecated {@link Rtag} extends {@link RtagMirror}.
     *
     * @param ignored Ignored Rtag instance.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.6.0")
    @Deprecated
    public RtagMirror(Rtag ignored) {
    }

    /**
     * Get the current object if it is and instance of {@link Rtag}.
     *
     * @deprecated {@link Rtag} extends {@link RtagMirror}.
     *
     * @return the current Rtag instance or null.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.6.0")
    @Deprecated
    public Rtag getRtag() {
        if (this instanceof Rtag) {
            return (Rtag) this;
        } else {
            return null;
        }
    }

    /**
     * Ignored method to set Rtag instance.
     *
     * @deprecated {@link Rtag} extends {@link RtagMirror}.
     *
     * @param rtag Ignored Rtag instance.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.6.0")
    @Deprecated
    public void setRtag(Rtag rtag) {
        // empty method
    }

    /**
     * Convert any object to tag object.
     *
     * @param object Object to convert.
     * @return       the object as its tag representation or null.
     */
    @SuppressWarnings("unchecked")
    public Object newTag(Object object) {
        if (TAG_BASE.isInstance(object)) {
            return object;
        } else if (object instanceof Map) {
            return TagCompound.newTag(this, (Map<String, Object>) object);
        } else if (object instanceof List) {
            return TagList.newTag(this, (List<Object>) object);
        } else {
            return TagBase.newTag(this, object);
        }
    }

    /**
     * Copy any Tag object into new one.
     *
     * @param tag Tag to copy.
     * @return    A tag object with the same value.
     */
    public Object clone(Object tag) {
        if (TAG_BASE.isInstance(tag)) {
            if (TAG_COMPOUND.isInstance(tag)) {
                return TagCompound.clone(tag);
            } else if (TAG_LIST.isInstance(tag)) {
                return TagList.clone(tag);
            } else {
                return TagBase.clone(tag);
            }
        } else {
            return tag == null ? null : newTag(tag);
        }
    }

    /**
     * Convert any tag object to regular Java object.
     *
     * @param tag Tag to convert.
     * @return    Converted object.
     */
    public Object getTagValue(Object tag) {
        if (TAG_COMPOUND.isInstance(tag)) {
            return TagCompound.getValue(this, tag);
        } else if (TAG_LIST.isInstance(tag)) {
            return TagList.getValue(this, tag);
        } else {
            return TagBase.getValue(this, tag);
        }
    }
}
