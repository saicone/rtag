package com.saicone.rtag;

import com.saicone.rtag.data.DataComponent;
import com.saicone.rtag.item.ItemObject;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;
import com.saicone.rtag.util.ChatComponent;
import com.saicone.rtag.util.EnchantmentTag;
import com.saicone.rtag.util.ServerInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * RtagItem class to edit any {@link ItemStack} NBT tags.
 *
 * @author Rubenicos
 */
public class RtagItem extends RtagEditor<ItemStack, RtagItem> {

    private final Object components;

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
        this(Rtag.INSTANCE, item);
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
        this.components = ServerInstance.useDataComponents ? DataComponent.Holder.getComponents(getLiteralObject()) : null;
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
        this.components = ServerInstance.useDataComponents ? DataComponent.Holder.getComponents(mcItem) : null;
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
        this.components = ServerInstance.useDataComponents ? DataComponent.Holder.getComponents(mcItem) : null;
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
    protected RtagItem getEditor() {
        return this;
    }

    @Override
    public Object getLiteralObject(ItemStack item) {
        return ItemObject.asNMSCopy(item);
    }

    @Override
    public Object getTag(Object item) {
        final Object tag = ItemObject.getCustomDataTag(item);
        return tag != null ? tag : TagCompound.newTag();
    }

    /**
     * Get current data components.
     *
     * @return A patched data component map.
     */
    @ApiStatus.Experimental
    public Object getComponents() {
        return components;
    }

    /**
     * Load changes into item instance.
     */
    public ItemStack load() {
        if (getTag() != null) {
            ItemObject.setCustomDataTag(getLiteralObject(), getTag());
        }
        ItemObject.setHandle(getTypeObject(), getLiteralObject());
        return getTypeObject();
    }

    /**
     * Load changes into new ItemStack instance and return them.
     *
     * @return Copy of the original item with changes loaded.
     */
    public ItemStack loadCopy() {
        final Object literal;
        if (getTag() != null && !ItemObject.hasCustomData(getLiteralObject())) {
            literal = ItemObject.copy(getLiteralObject());
            ItemObject.setCustomDataTag(literal, getTag());
        } else {
            literal = getLiteralObject();
        }
        return ItemObject.asBukkitCopy(literal);
    }

    /**
     * Change item tag into new one.<br>
     * Value must be Map&lt;String, Object&gt; or NBTTagCompound.
     *
     * @param value Object to replace current tag.
     * @return      True if tag has replaced.
     */
    public boolean set(Object value) {
        if (value == null) {
            this.tag = null;
        } else if (!super.set(value)) {
            return false;
        }
        try {
            ItemObject.setCustomDataTag(getLiteralObject(), getTag());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return true;
    }

    /**
     * Check if the current item has the provided hide flags ordinals.
     *
     * @see #addHideFlags(int...) Flag ordinals.
     *
     * @param hideFlags Flags to check.
     * @return          true if the item has all the flags.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @Deprecated
    public boolean hasHideFlags(int... hideFlags) {
        // Since 1.20.5, items cannot hold hide flags
        if (ServerInstance.useDataComponents) {
            // TODO: Replace with converted component value check
            if (!getItem().hasItemMeta()) {
                return false;
            }
            final ItemFlag[] values = ItemFlag.values();
            for (int ordinal : hideFlags) {
                if (!getItem().getItemMeta().hasItemFlag(values[ordinal])) {
                    return false;
                }
            }
            return true;
        }
        return hasEnum(hideFlags, "HideFlags");
    }

    /**
     * Check if the current item has the provided enchantment.
     *
     * @param enchant Enchantment object.
     * @return        true if contains the enchantment.
     */
    public boolean hasEnchantment(Object enchant) {
        // Since 1.20.5, items cannot hold invalid enchantments
        if (ServerInstance.useDataComponents) {
            final EnchantmentTag tag = EnchantmentTag.of(enchant);
            if (tag == null) {
                return false;
            }
            return getItem().containsEnchantment(tag.getEnchantment());
        }
        return getEnchantment(enchant) != null;
    }

    /**
     * Fix bad item caused by Bukkit serialization, most specifically
     * item lore component that doesn't allow to compare with similar items.
     */
    public void fixSerialization() {
        if (ServerInstance.verNumber < 14) {
            return;
        }
        // Since 1.20.5, the server use a different serialization
        if (ServerInstance.useDataComponents) {
            return;
        }
        // Fix lore
        final Object tag = getExact("display", "Lore");
        if (tag != null) {
            final List<Object> lore = TagList.getValue(tag);
            for (int i = 0; i < lore.size(); i++) {
                // NBTTagString -> component as json -> colored string -> fixed component as json -> NBTTagString
                final String line = (String) TagBase.getValue(lore.get(i));
                if (ChatComponent.isChatComponent(line)) {
                    lore.set(i, TagBase.newTag(ChatComponent.toJson(ChatComponent.toString(line))));
                }
            }
        }
    }

    /**
     * Add hide flags to item using flag ordinal:<br>
     * 0 = Enchantments<br>
     * 1 = AttributeModifiers<br>
     * 2 = Unbreakable<br>
     * 3 = CanDestroy<br>
     * 4 = CanPlaceOn<br>
     * 5 = Other information (stored enchants, potion effects, generation, author, explosion and fireworks)<br>
     * 6 = Dyed<br>
     * 7 = Palette information (armor trim)
     *
     * @param hideFlags Flags to add.
     * @return          true if the flags was added.
     */
    // 0 = minecraft:enchantments={show_in_tooltip:false}
    // 1 = minecraft:attribute_modifiers={show_in_tooltip:false}
    // 2 = minecraft:unbreakable={show_in_tooltip:false}
    // 3 = minecraft:can_break={show_in_tooltip:false}
    // 4 = minecraft:can_place_on={show_in_tooltip:false}
    // 5 = minecraft:hide_additional_tooltip={}
    // 6 = minecraft:dyed_color={show_in_tooltip:false}
    // 7 = minecraft:trim={show_in_tooltip:false}
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @Deprecated
    public boolean addHideFlags(int... hideFlags) {
        // Since 1.20.5, items cannot hold hide flags
        if (ServerInstance.useDataComponents) {
            boolean result = false;
            // TODO: Replace with component conversion
            if (getItem().hasItemMeta()) {
                final ItemFlag[] values = ItemFlag.values();
                final ItemMeta meta = getItem().getItemMeta();
                for (int ordinal : hideFlags) {
                    final ItemFlag flag = values[ordinal];
                    if (!meta.hasItemFlag(flag)) {
                        meta.addItemFlags(flag);
                        result = true;
                    }
                }
                getItem().setItemMeta(meta);
            }
            return result;
        }
        return addEnum(hideFlags, "HideFlags");
    }

    /**
     * Add the provided enchantment to the item.
     *
     * @param enchant Enchantment object.
     * @param level   Enchantment level.
     * @return        true if the enchantment was added.
     */
    public boolean addEnchantment(Object enchant, int level) {
        final EnchantmentTag tag = EnchantmentTag.of(enchant);
        if (tag == null) {
            return false;
        }
        // Since 1.20.5, items cannot hold invalid enchantments
        if (ServerInstance.useDataComponents) {
            getItem().addEnchantment(tag.getEnchantment(), level);
            return true;
        }
        final Object enchantment = getEnchantment(tag);
        if (enchantment == null) {
            return add(Map.of("id", tag.getKey(), "lvl", level), EnchantmentTag.getEnchantmentKey(getTypeObject()));
        }
        TagCompound.set(enchantment, "lvl", TagBase.newTag(level));
        return true;
    }

    /**
     * Change the current unbreakable status of item.
     *
     * @param unbreakable true to make item unbreakable.
     * @return            true if the status was changed.
     */
    public boolean setUnbreakable(boolean unbreakable) {
        // TODO: Replace with component value setter
        if (ServerInstance.useDataComponents) {
            if (getItem().hasItemMeta()) {
                final ItemMeta meta = getItem().getItemMeta();
                meta.setUnbreakable(unbreakable);
                getItem().setItemMeta(meta);
                return true;
            }
            return false;
        }
        return set(unbreakable, "Unbreakable");
    }

    /**
     * Set the current custom model data of item.
     *
     * @param model Model id, null of you want to remove it.
     * @return      true if the model was set or removed.
     */
    public boolean setCustomModelData(Integer model) {
        // TODO: Replace with component value setter
        if (ServerInstance.useDataComponents) {
            if (getItem().hasItemMeta()) {
                final ItemMeta meta = getItem().getItemMeta();
                meta.setCustomModelData(model);
                getItem().setItemMeta(meta);
                return true;
            } else {
                return model == null;
            }
        }
        return set(model, "CustomModelData");
    }

    /**
     * Override the current item hide flags.
     *
     * @see #addHideFlags(int...) Flag ordinals.
     *
     * @param hideFlags Flags to set.
     * @return          true if the flags was set.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @Deprecated
    public boolean setHideFlags(int... hideFlags) {
        // Since 1.20.5, items cannot hold hide flags
        if (ServerInstance.useDataComponents) {
            boolean result = false;
            // TODO: Replace with component conversion
            if (getItem().hasItemMeta()) {
                final ItemFlag[] values = ItemFlag.values();
                final ItemMeta meta = getItem().getItemMeta();
                meta.removeItemFlags(values);
                for (int ordinal : hideFlags) {
                    meta.addItemFlags(values[ordinal]);
                    result = true;
                }
                getItem().setItemMeta(meta);
            }
            return result;
        }
        return setEnum(hideFlags, "HideFlags");
    }

    /**
     * Change the item repair cost inside anvil.
     *
     * @param cost XP level repair cost.
     * @return     true if the repair cost was changes.
     */
    public boolean setRepairCost(int cost) {
        if (ServerInstance.useDataComponents) {
            DataComponent.MapPatch.set(components, DataComponent.type("minecraft:repair_cost"), cost);
            return true;
        }
        return set(cost, "RepairCost");
    }

    /**
     * Remove hide flags from item using flag ordinal.
     *
     * @see #addHideFlags(int...) Flag ordinals.
     *
     * @param hideFlags Flags to remove.
     * @return          true if the flags was removed.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @Deprecated
    public boolean removeHideFlags(int... hideFlags) {
        // Since 1.20.5, items cannot hold hide flags
        if (ServerInstance.useDataComponents) {
            boolean result = false;
            // TODO: Replace with component conversion
            if (getItem().hasItemMeta()) {
                final ItemFlag[] values = ItemFlag.values();
                final ItemMeta meta = getItem().getItemMeta();
                for (int ordinal : hideFlags) {
                    final ItemFlag flag = values[ordinal];
                    if (meta.hasItemFlag(flag)) {
                        meta.removeItemFlags(flag);
                        result = true;
                    }
                }
                getItem().setItemMeta(meta);
            }
            return result;
        }
        return removeEnum(hideFlags, "HideFlags");
    }

    /**
     * Remove from item the provided enchantment.
     *
     * @param enchant Enchantment object.
     * @return        the enchantment level that was set on the item.
     */
    public int removeEnchantment(EnchantmentTag enchant) {
        final EnchantmentTag tag = EnchantmentTag.of(enchant);
        if (tag == null) {
            return 0;
        }

        // Since 1.20.5, items cannot hold invalid enchantments
        if (ServerInstance.useDataComponents) {
            return getItem().removeEnchantment(tag.getEnchantment());
        }

        final String enchantmentKey = EnchantmentTag.getEnchantmentKey(getTypeObject());
        final Object enchantments = getExact(enchantmentKey);
        if (enchantments == null) {
            return 0;
        }

        final List<Object> list = TagList.getValue(enchantments);
        if (list.isEmpty()) {
            return 0;
        }

        int index = -1;
        int level = 0;

        for (int i = 0; i < list.size(); i++) {
            final Object o = list.get(i);
            if (tag.compareKey(TagBase.getValue(TagCompound.get(o, "id")))) {
                index = i;
                level = Integer.parseInt(String.valueOf(TagBase.getValue(TagCompound.get(o, "lvl"))));
                break;
            }
        }

        if (index < 0) {
            return 0;
        } else if (list.size() == 1) {
            remove(enchantmentKey);
        } else {
            final List<Object> listCopy = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                if (index != i) {
                    listCopy.add(list.get(i));
                }
            }
            set(listCopy, enchantmentKey);
        }

        return level;
    }

    /**
     * Get the current custom model data inside item.
     *
     * @return Model id, null if the item doesn't have model.
     */
    public Integer getCustomModelData() {
        // TODO: Replace with component value getter
        if (ServerInstance.useDataComponents) {
            return getItem().hasItemMeta() && getItem().getItemMeta().hasCustomModelData() ? getItem().getItemMeta().getCustomModelData() : null;
        }
        return get("CustomModelData");
    }

    /**
     * Get current hide flags.
     *
     * @see #addHideFlags(int...) Flag ordinals.
     *
     * @return A set of flag ordinals.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @Deprecated
    public Set<Integer> getHideFlags() {
        // Since 1.20.5, items cannot hold hide flags
        if (ServerInstance.useDataComponents) {
            final Set<Integer> hideFlags = new HashSet<>();
            if (getItem().hasItemMeta()) {
                for (ItemFlag flag : getItem().getItemMeta().getItemFlags()) {
                    hideFlags.add(flag.ordinal());
                }
            }
            return hideFlags;
        }
        return getOptional("HideFlags").asOrdinalSet(8);
    }

    /**
     * Get the XP level repair cost inside anvil.
     *
     * @return Level repair cost.
     */
    public int getRepairCost() {
        if (ServerInstance.useDataComponents) {
            return (int) DataComponent.Map.get(components, DataComponent.type("minecraft:repair_cost"));
        }
        return getOptional("RepairCost").or(0);
    }

    /**
     * Get the NBTTagCompound that represent the provided enchantment.
     *
     * @param enchant Enchantment object.
     * @return        A NBTTagCompound if the enchantment was found, null otherwise.
     */
    public Object getEnchantment(Object enchant) {
        final EnchantmentTag tag = EnchantmentTag.of(enchant);
        if (tag == null) {
            return null;
        }
        // Since 1.20.5, items cannot hold invalid enchantments
        if (ServerInstance.useDataComponents) {
            int level = getItem().getEnchantmentLevel(tag.getEnchantment());
            if (level > 0) {
                return TagCompound.newTag(RtagMirror.INSTANCE, Map.of("id", tag.getKey(), "lvl", level));
            }
        } else {
            final Object enchantments = getExact(EnchantmentTag.getEnchantmentKey(getTypeObject()));
            if (enchantments != null) {
                for (Object o : TagList.getValue(enchantments)) {
                    if (tag.compareKey(TagBase.getValue(TagCompound.get(o, "id")))) {
                        return o;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get the current level for provided enchantment.
     *
     * @param enchant Enchantment object.
     * @return        Level integer if the enchantment was found, 0 otherwise.
     */
    public int getEnchantmentLevel(Object enchant) {
        // Since 1.20.5, items cannot hold invalid enchantments
        if (ServerInstance.useDataComponents) {
            final EnchantmentTag tag = EnchantmentTag.of(enchant);
            if (tag == null) {
                return 0;
            }
            return getItem().getEnchantmentLevel(tag.getEnchantment());
        }
        final Object enchantment = getEnchantment(enchant);
        if (enchantment == null) {
            return 0;
        }
        final Object lvl = TagBase.getValue(TagCompound.get(enchantment, "lvl"));
        return lvl == null ? -1 : Integer.parseInt(String.valueOf(lvl));
    }

    /**
     * Get all the enchantments inside item.
     *
     * @return A Map of EnchantmentTag objects.
     */
    public Map<EnchantmentTag, Integer> getEnchantments() {
        final Map<EnchantmentTag, Integer> enchants = new HashMap<>();
        // Since 1.20.5, items cannot hold invalid enchantments
        if (ServerInstance.useDataComponents) {
            for (Map.Entry<Enchantment, Integer> entry : getItem().getEnchantments().entrySet()) {
                enchants.put(EnchantmentTag.of(entry.getKey()), entry.getValue());
            }
            return enchants;
        }

        final Object enchantments = getExact(EnchantmentTag.getEnchantmentKey(getTypeObject()));
        if (enchantments == null) {
            return enchants;
        }
        for (EnchantmentTag value : EnchantmentTag.VALUES) {
            for (Object o : TagList.getValue(enchantments)) {
                if (value.compareKey(TagBase.getValue(TagCompound.get(o, "id")))) {
                    enchants.put(value, Integer.parseInt(String.valueOf(TagBase.getValue(TagCompound.get(o, "lvl")))));
                }
            }
        }
        return enchants;
    }

    /**
     * Get the current unbreakable status.
     *
     * @return true if the item is unbreakable.
     */
    public boolean isUnbreakable() {
        if (ServerInstance.useDataComponents) {
            return DataComponent.Map.has(components, DataComponent.type("minecraft:unbreakable"));
        }
        return getOptional("Unbreakable").asBoolean(false);
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
