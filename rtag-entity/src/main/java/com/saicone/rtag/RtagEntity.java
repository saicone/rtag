package com.saicone.rtag;

import com.saicone.rtag.entity.EntityBridge;
import com.saicone.rtag.entity.EntityTag;
import com.saicone.rtag.util.EasyLookup;
import org.bukkit.entity.Entity;

public class RtagEntity {

    private static final Class<?> tagCompound = EasyLookup.classById("NBTTagCompound");

    private final Rtag rtag;
    private final Object entity;
    private Object tag;

    public RtagEntity(Rtag rtag, Entity entity) {
        this.rtag = rtag;
        Object finalEntity = null;
        Object finalTag = null;
        try {
            finalEntity = EntityBridge.asMinecraft(entity);
            finalTag = EntityTag.saveTag(finalEntity);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        this.entity = finalEntity;
        tag = finalTag;
    }

    public void load() {
        try {
            EntityTag.loadTag(entity, tag);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public boolean add(Object value, Object... path) {
        try {
            return rtag.add(tag, value, path);
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    public boolean set(Object value) {
        Object tag = rtag.toTag(value);
        if (tagCompound.isInstance(tag)) {
            this.tag = tag;
            return true;
        }
        return false;
    }

    public boolean set(Object value, Object... path) {
        try {
            return rtag.set(tag, value, path);
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    public <T> T get() {
        return rtag.fromTag(tag);
    }

    public <T> T get(Object... path) {
        try {
            return rtag.get(tag, path);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    public Object getExact() {
        return tag;
    }

    public Object getExact(Object... path) {
        try {
            return rtag.getExact(tag, path);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }
}
