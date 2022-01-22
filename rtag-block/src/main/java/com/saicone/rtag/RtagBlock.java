package com.saicone.rtag;

import com.saicone.rtag.block.BlockBridge;
import com.saicone.rtag.block.BlockTag;
import com.saicone.rtag.util.EasyLookup;
import org.bukkit.block.Block;

public class RtagBlock {

    private static final Class<?> tagCompound = EasyLookup.classById("NBTTagCompound");

    private final Rtag rtag;
    private final Object block;
    private Object tag;

    public RtagBlock(Rtag rtag, Block block) {
        this.rtag = rtag;
        Object finalBlock = null;
        Object finalTag = null;
        try {
            finalBlock = BlockBridge.asMinecraft(block);
            finalTag = BlockTag.saveTag(finalBlock);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        this.block = finalBlock;
        tag = finalTag;
    }

    public void load() {
        try {
            BlockTag.loadTag(block, tag);
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
