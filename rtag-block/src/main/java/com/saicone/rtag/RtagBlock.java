package com.saicone.rtag;

import com.saicone.rtag.block.TileBridge;
import com.saicone.rtag.block.TileTag;
import org.bukkit.block.Block;

/**
 * RtagItem class to edit any {@link Block} NBT tags.
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

    private final Block block;

    /**
     * Constructs an RtagItem with specified Rtag parent
     * and Block to edit.
     *
     * @param rtag Rtag parent.
     * @param block Block to edit.
     */
    public RtagBlock(Rtag rtag, Block block) {
        this(rtag, block, asMinecraft(block));
    }

    /**
     * Constructs an RtagItem with specified Rtag parent
     * and NMS Block to edit.
     *
     * @param rtag   Rtag parent.
     * @param block  Original block.
     * @param object NMS block to edit.
     */
    public RtagBlock(Rtag rtag, Block block, Object object) {
        this(rtag, block, object, getTag(object));
    }

    /**
     * Constructs an RtagItem with specified Rtag parent
     * and NMS Block to edit.
     *
     * @param rtag   Rtag parent.
     * @param block  Original block.
     * @param object NMS block to edit.
     * @param tag    Block tag to edit.
     */
    public RtagBlock(Rtag rtag, Block block, Object object, Object tag) {
        super(rtag, object, tag);
        this.block = block;
    }

    /**
     * Load changes into block instance.
     *
     * @return The original block.
     */
    public Block load() {
        try {
            TileTag.loadTag(getObject(), getTag());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return block;
    }
}
