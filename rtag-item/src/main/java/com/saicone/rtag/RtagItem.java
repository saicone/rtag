package com.saicone.rtag;

import com.saicone.rtag.item.ItemBridge;
import com.saicone.rtag.item.ItemTag;
import org.bukkit.inventory.ItemStack;

/**
 * RtagItem class to edit any {@link ItemStack} NBT tags.
 *
 * @author Rubenicos
 */
public class RtagItem extends RtagEditor<ItemStack> {

    private static Object asMinecraft(ItemStack item) {
        try {
            return ItemBridge.asMinecraft(item);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    private static Object getTag(Object item) {
        try {
            return ItemTag.getTag(item);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    private final ItemStack item;

    /**
     * Constructs an RtagItem with ItemStack to edit.
     *
     * @param item Item to edit.
     */
    public RtagItem(ItemStack item) {
        this(Rtag.INSTANCE, item);
    }

    /**
     * Constructs an RtagItem with specified Rtag parent
     * and ItemStack to edit.
     *
     * @param rtag Rtag parent.
     * @param item Item to edit.
     */
    public RtagItem(Rtag rtag, ItemStack item) {
        this(rtag, item, asMinecraft(item));
    }

    /**
     * Constructs an RtagItem with specified Rtag parent
     * and NMS ItemStack to edit.
     *
     * @param rtag   Rtag parent.
     * @param item   Item to load changes.
     * @param object NMS item to edit.
     */
    public RtagItem(Rtag rtag, ItemStack item, Object object) {
        this(rtag, item, object, getTag(object));
    }

    /**
     * Constructs an RtagItem with specified Rtag parent
     * and NMS ItemStack to edit.
     *
     * @param rtag   Rtag parent.
     * @param item   Item to load changes.
     * @param object NMS item to edit.
     * @param tag    Item tag to edit.
     */
    public RtagItem(Rtag rtag, ItemStack item, Object object, Object tag) {
        super(rtag, object, tag);
        this.item = item;
    }

    /**
     * Get current item instance.
     *
     * @return A Bukkit ItemStack.
     */
    public ItemStack getItem() {
        return item;
    }

    /**
     * Load changes into item instance.
     */
    public void load() {
        try {
            ItemBridge.setHandle(item, getObject());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Load changes into new ItemStack instance and return them.
     *
     * @return Copy of the original item with changes loaded.
     */
    public ItemStack loadCopy() {
        try {
            return ItemBridge.asBukkit(getObject());
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Change item tag into new one.<br>
     * Value must be Map<String, Object> or NBTTagListCompound.
     *
     * @param value Object to replace current tag.
     * @return      True if tag has replaced.
     */
    public boolean set(Object value) {
        if (super.set(value)) {
            try {
                ItemTag.setTag(getObject(), getTag());
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return true;
        } else {
            return false;
        }
    }
}
