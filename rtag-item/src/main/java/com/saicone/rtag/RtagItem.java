package com.saicone.rtag;

import com.saicone.rtag.item.ItemBridge;
import com.saicone.rtag.item.ItemTag;
import com.saicone.rtag.util.EasyLookup;
import org.bukkit.inventory.ItemStack;

public class RtagItem {

    private static final Class<?> tagCompound = EasyLookup.classById("NBTTagCompound");

    private final Rtag rtag;
    private final Object item;
    private Object tag;

    public RtagItem(Rtag rtag, ItemStack item) {
        this.rtag = rtag;
        Object finalItem = null;
        Object finalTag = null;
        try {
            finalItem = ItemBridge.asMinecraft(item);
            finalTag = ItemTag.getTag(finalItem);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        this.item = finalItem;
        tag = finalTag;
    }

    public ItemStack load() {
        try {
            return ItemBridge.asBukkit(item);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
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
            try {
                ItemTag.setTag(item, tag);
                return true;
            } catch (Throwable t) {
                t.printStackTrace();
            }
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
