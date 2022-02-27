package com.saicone.rtag;

import com.saicone.rtag.block.TileBridge;
import com.saicone.rtag.block.TileTag;
import org.bukkit.block.Block;

/**
 * RtagBlock class to edit any {@link Block} NBT tags.
 *
 * @author Rubenicos
 */
public class RtagBlock extends RtagEditor<Block> {

    private static Object asMinecraft(Block block) {
        try {
            return TileBridge.asMinecraft(block);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    private static Object getTag(Object block) {
        try {
            return TileTag.saveTag(block);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Constructs an RtagBlock with Block to edit.
     *
     * @param block Block to edit.
     */
    public RtagBlock(Block block) {
        this(Rtag.INSTANCE, block);
    }

    /**
     * Constructs an RtagBlock with specified Rtag parent
     * and Block to edit.
     *
     * @param rtag  Rtag parent.
     * @param block Block to edit.
     */
    public RtagBlock(Rtag rtag, Block block) {
        this(rtag, asMinecraft(block));
    }

    /**
     * Constructs an RtagBlock with specified Rtag parent
     * and NMS Block to edit.
     *
     * @param rtag   Rtag parent.
     * @param object NMS block to edit.
     */
    public RtagBlock(Rtag rtag, Object object) {
        this(rtag, object, getTag(object));
    }

    /**
     * Constructs an RtagBlock with specified Rtag parent
     * and NMS Block to edit.
     *
     * @param rtag   Rtag parent.
     * @param object NMS block to edit.
     * @param tag    Block tag to edit.
     */
    public RtagBlock(Rtag rtag, Object object, Object tag) {
        super(rtag, object, tag);
    }

    /**
     * Load changes into block instance.
     */
    public void load() {
        try {
            TileTag.loadTag(getObject(), getTag());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
