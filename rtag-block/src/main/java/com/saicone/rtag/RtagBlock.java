package com.saicone.rtag;

import com.saicone.rtag.block.BlockObject;
import com.saicone.rtag.util.ChatComponent;
import org.bukkit.block.Block;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * RtagBlock class to edit any {@link Block} NBT tags.
 *
 * @author Rubenicos
 */
public class RtagBlock extends RtagEditor<Block> {

    /**
     * Create an RtagBlock using Block.
     *
     * @param block Block to load the changes.
     * @return      new RtagBlock instance.
     */
    public static RtagBlock of(Block block) {
        return new RtagBlock(block);
    }

    /**
     * Create an RtagBlock using Block and specified Rtag parent.
     *
     * @param rtag  Rtag parent.
     * @param block Block to load the changes.
     * @return      new RtagBlock instance.
     */
    public static RtagBlock of(Rtag rtag, Block block) {
        return new RtagBlock(rtag, block);
    }

    /**
     * Constructs an RtagBlock with Block to edit.
     *
     * @param block Block to load the changes.
     */
    public RtagBlock(Block block) {
        super(Rtag.INSTANCE, block);
    }

    /**
     * Constructs an RtagBlock with specified Rtag parent
     * and Block to edit.
     *
     * @param rtag  Rtag parent.
     * @param block Block to load the changes.
     */
    public RtagBlock(Rtag rtag, Block block) {
        super(rtag, block);
    }

    /**
     * Constructs an RtagBlock with specified Rtag parent
     * and NMS Block to edit.
     *
     * @param rtag     Rtag parent.
     * @param block    Block to load the changes.
     * @param mcObject Minecraft server block to edit.
     */
    public RtagBlock(Rtag rtag, Block block, Object mcObject) {
        super(rtag, block, mcObject);
    }

    /**
     * Constructs an RtagBlock with specified Rtag parent
     * and NMS Block to edit.
     *
     * @param rtag     Rtag parent.
     * @param block    Block to load the changes.
     * @param mcObject Minecraft server block to edit.
     * @param tag      Block tag to edit.
     */
    public RtagBlock(Rtag rtag, Block block, Object mcObject, Object tag) {
        super(rtag, block, mcObject, tag);
    }

    /**
     * Get current block instance.
     *
     * @return A Bukkit Block.
     */
    public Block getBlock() {
        return getTypeObject();
    }

    @Override
    public Object getLiteralObject(Block block) {
        return BlockObject.getTileEntity(block);
    }

    @Override
    public Object getTag(Object block) {
        return BlockObject.save(block);
    }

    /**
     * Load changes into block instance.
     */
    public void load() {
        try {
            BlockObject.load(getLiteralObject(), getTag());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Change the entity display name.<br>
     * Make sure to use color codes correctly or chat component format.
     *
     * @param name Normal string or ChatComponent.
     * @return     true if the custom name was changed.
     */
    public boolean setCustomName(String name) {
        return set(ChatComponent.isChatComponent(name) ? name : ChatComponent.toJson(name), "CustomName");
    }

    /**
     * Get the entity display name.<br>
     * This method convert any ChatComponent to normal colored string.
     *
     * @return A colored string.
     */
    public String getCustomName() {
        final String name = get("CustomName");
        if (name == null) {
            return null;
        }
        return ChatComponent.isChatComponent(name) ? name : ChatComponent.toString(name);
    }

    /**
     * Edit the current RtagBlock instance and return itself.
     *
     * @param consumer Function to apply.
     * @return         The current RtagBlock instance.
     */
    public RtagBlock edit(Consumer<RtagBlock> consumer) {
        consumer.accept(this);
        return this;
    }

    /**
     * Edit the provided block using a RtagBlock instance by consumer.
     *
     * @param block    Block to edit.
     * @param consumer Consumer to accept.
     * @return         The provided item.
     * @param <T>      Block type.
     */
    public static <T extends Block> T edit(T block, Consumer<RtagBlock> consumer) {
        return edit(Rtag.INSTANCE, block, consumer);
    }

    /**
     * Edit a RtagBlock instance using the provided block by function that return any type.<br>
     * Take in count that you should use {@link RtagEditor#load()} if you want to load the changes.
     *
     * @param block    Block to edit.
     * @param function Function to apply.
     * @return         The object provided by the function.
     * @param <T>      Block type.
     * @param <R>      The required return type.
     */
    public static <T extends Block, R> R edit(T block, Function<RtagBlock, R> function) {
        return edit(Rtag.INSTANCE, block, function);
    }

    /**
     * Edit the provided block using a RtagBlock instance by consumer with defined Rtag parent.
     *
     * @param rtag     Rtag parent.
     * @param block    Block to edit.
     * @param consumer Consumer to accept.
     * @return         The provided item.
     * @param <T>      Block type.
     */
    public static <T extends Block> T edit(Rtag rtag, T block, Consumer<RtagBlock> consumer) {
        new RtagBlock(rtag, block).edit(consumer).load();
        return block;
    }

    /**
     * Edit a RtagBlock instance using the provided block by function that return any type with defined Rtag parent.<br>
     * Take in count that you should use {@link RtagEditor#load()} if you want to load the changes.
     *
     * @param rtag     Rtag parent.
     * @param block    Block to edit.
     * @param function Function to apply.
     * @return         The object provided by the function.
     * @param <T>      Block type.
     * @param <R>      The required return type.
     */
    public static <T extends Block, R> R edit(Rtag rtag, T block, Function<RtagBlock, R> function) {
        return function.apply(new RtagBlock(rtag, block));
    }
}
