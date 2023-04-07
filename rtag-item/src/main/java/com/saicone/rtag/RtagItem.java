package com.saicone.rtag;

import com.saicone.rtag.item.ItemObject;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * RtagItem class to edit any {@link ItemStack} NBT tags.
 *
 * @author Rubenicos
 */
public class RtagItem extends RtagEditor<ItemStack> {

    /**
     * Create an RtagItem using ItemStack.
     *
     * @param item Item to load changes.
     * @return     new RtagItem instance.
     */
    public static RtagItem of(ItemStack item) {
        return new RtagItem(item);
    }

    /**
     * Create an RtagItem using ItemStack and specified Rtag parent.
     *
     * @param rtag Rtag parent.
     * @param item Item to load changes.
     * @return     new RtagItem instance.
     */
    public static RtagItem of(Rtag rtag, ItemStack item) {
        return new RtagItem(rtag, item);
    }

    /**
     * Constructs an RtagItem with ItemStack to edit.
     *
     * @param item Item to load changes.
     */
    public RtagItem(ItemStack item) {
        super(Rtag.INSTANCE, item);
    }

    /**
     * Constructs an RtagItem with specified Rtag parent
     * and ItemStack to edit.
     *
     * @param rtag Rtag parent.
     * @param item Item to load changes.
     */
    public RtagItem(Rtag rtag, ItemStack item) {
        super(rtag, item);
    }

    /**
     * Constructs an RtagItem with specified Rtag parent
     * and NMS ItemStack to edit.
     *
     * @param rtag   Rtag parent.
     * @param item   Item to load changes.
     * @param mcItem Minecraft server item to edit.
     */
    public RtagItem(Rtag rtag, ItemStack item, Object mcItem) {
        super(rtag, item, mcItem);
    }

    /**
     * Constructs an RtagItem with specified Rtag parent
     * and NMS ItemStack to edit.
     *
     * @param rtag   Rtag parent.
     * @param item   Item to load changes.
     * @param mcItem Minecraft server item to edit.
     * @param tag    Item tag to edit.
     */
    public RtagItem(Rtag rtag, ItemStack item, Object mcItem, Object tag) {
        super(rtag, item, mcItem, tag);
    }

    /**
     * Get current item instance.
     *
     * @return A Bukkit ItemStack.
     */
    public ItemStack getItem() {
        return getTypeObject();
    }

    @Override
    public Object getLiteralObject(ItemStack item) {
        return ItemObject.asNMSCopy(item);
    }

    @Override
    public Object getTag(Object item) {
        return ItemObject.getTag(item);
    }

    /**
     * Load changes into item instance.
     */
    public void load() {
        ItemObject.setHandle(getTypeObject(), getLiteralObject());
    }

    /**
     * Load changes into new ItemStack instance and return them.
     *
     * @return Copy of the original item with changes loaded.
     */
    public ItemStack loadCopy() {
        return ItemObject.asBukkitCopy(getLiteralObject());
    }

    /**
     * Change item tag into new one.<br>
     * Value must be Map&lt;String, Object&gt; or NBTTagListCompound.
     *
     * @param value Object to replace current tag.
     * @return      True if tag has replaced.
     */
    public boolean set(Object value) {
        if (super.set(value)) {
            try {
                ItemObject.setTag(getLiteralObject(), getTag());
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Edit the current RtagItem instance and return itself.
     *
     * @param consumer Consumer to apply.
     * @return         The current RtagItem instance.
     */
    public RtagItem edit(Consumer<RtagItem> consumer) {
        consumer.accept(this);
        return this;
    }

    /**
     * Edit the provided item using a RtagItem instance by consumer.
     *
     * @param item     Item to edit.
     * @param consumer Consumer to accept.
     * @return         The provided item.
     * @param <T>      ItemStack type.
     */
    public static <T extends ItemStack> T edit(T item, Consumer<RtagItem> consumer) {
        return edit(Rtag.INSTANCE, item, consumer);
    }

    /**
     * Edit a RtagItem instance using the provided item by function that return any type.<br>
     * Take in count that you should use {@link RtagEditor#load()} if you want to load the changes.
     *
     * @param item     Item to edit.
     * @param function Function to apply.
     * @return         The object provided by the function.
     * @param <T>      ItemStack type.
     * @param <R>      The required return type.
     */
    public static <T extends ItemStack, R> R edit(T item, Function<RtagItem, R> function) {
        return edit(Rtag.INSTANCE, item, function);
    }

    /**
     * Edit the provided item using a RtagItem instance by consumer with defined Rtag parent.
     *
     * @param rtag     Rtag parent.
     * @param item     Item to edit.
     * @param consumer Consumer to accept.
     * @return         The provided item.
     * @param <T>      ItemStack type.
     */
    public static <T extends ItemStack> T edit(Rtag rtag, T item, Consumer<RtagItem> consumer) {
        new RtagItem(rtag, item).edit(consumer).load();
        return item;
    }

    /**
     * Edit a RtagItem instance using the provided item by function that return any type with defined Rtag parent.<br>
     * Take in count that you should use {@link RtagEditor#load()} if you want to load the changes.
     *
     * @param rtag     Rtag parent.
     * @param item     Item to edit.
     * @param function Function to apply.
     * @return         The object provided by the function.
     * @param <T>      ItemStack type.
     * @param <R>      The required return type.
     */
    public static <T extends ItemStack, R> R edit(Rtag rtag, T item, Function<RtagItem, R> function) {
        return function.apply(new RtagItem(rtag, item));
    }
}
