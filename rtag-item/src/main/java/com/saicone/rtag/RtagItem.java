package com.saicone.rtag;

import com.saicone.rtag.data.ComponentType;
import com.saicone.rtag.data.DataComponent;
import com.saicone.rtag.item.ItemObject;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;
import com.saicone.rtag.util.ChatComponent;
import com.saicone.rtag.util.EnchantmentTag;
import com.saicone.rtag.util.OptionalType;
import com.saicone.rtag.util.ServerInstance;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
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

    private static final List<String> HIDE_FLAGS = List.of(
            "minecraft:enchantments",
            "minecraft:attribute_modifiers",
            "minecraft:unbreakable",
            "minecraft:can_break",
            "minecraft:can_place_on",
            "minecraft:stored_enchantments",
            "minecraft:dyed_color",
            "minecraft:trim"
    );

    private final Object components;

    private transient DataComponent.Builder<Optional<?>> patch;
    private transient boolean edited = false;
    private transient boolean copied = true;

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
        this.components = ServerInstance.Release.COMPONENT ? DataComponent.Holder.getComponents(getLiteralObject()) : null;
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
        this.components = ServerInstance.Release.COMPONENT ? DataComponent.Holder.getComponents(mcItem) : null;
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
        this.components = ServerInstance.Release.COMPONENT ? DataComponent.Holder.getComponents(mcItem) : null;
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
        final ItemStack craftItem = ItemObject.getCraftStack(item);
        if (craftItem == null) {
            this.copied = true;
            return ItemObject.asNMSCopy(item);
        }

        this.copied = false;
        return ItemObject.getUncheckedHandle(craftItem);
    }

    /**
     * Get current item tag or null.
     *
     * @return A NBTTagCompound.
     */
    public Object getLiteralTag() {
        return this.tag;
    }

    /**
     * Get current item tag to edit.<br>
     * If this instance has not been edited, it will copy the current tag
     * or create a new one if the current tag is null.
     *
     * @return A NBTTagCompound.
     */
    @Override
    public Object getTag() {
        if (!this.edited) {
            this.tag = this.tag == null ? TagCompound.newTag() : TagCompound.clone(this.tag);
            this.edited = true;
        }
        return this.tag;
    }

    @Override
    public Object getTag(Object item) {
        return ItemObject.getCustomDataTag(item);
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

    private DataComponent.Builder<Optional<?>> getPatch() {
        if (patch == null) {
            patch = DataComponent.Patch.builder();
        }
        return patch;
    }

    /**
     * Load changes into item instance.
     *
     * @return The current item instance.
     */
    @Override
    public ItemStack load() {
        final Object literal = loadInto(getLiteralObject());
        if (this.copied) {
            ItemObject.loadHandle(getTypeObject(), literal);
        }
        return getTypeObject();
    }

    /**
     * Load changes into new ItemStack instance and return it.
     *
     * @return Copy of the original item with changes loaded.
     */
    public ItemStack loadCopy() {
        final Object copy = loadInto(ItemObject.copy(getLiteralObject()));
        if (this.copied) {
            return ItemObject.asBukkitCopy(copy);
        } else {
            return ItemObject.asCraftMirror(copy);
        }
    }

    /**
     * Load changes into provided Minecraft ItemStack and return it.
     *
     * @param item The item to load changes into
     * @return     The provided item with changes loaded.
     * @param <T>  Item type.
     */
    public <T> T loadInto(T item) {
        if (this.edited) {
            ItemObject.setCustomDataTag(item, this.tag);
        }
        if (patch != null) {
            ItemObject.apply(item, patch.build());
        }
        return item;
    }

    @Override
    public void update(Object object) {
        this.edited = false;
        super.update(object);
    }

    /**
     * Check if the current item or patch has he provided data component type.
     *
     * @param type Data component type object or ID.
     * @return     true if the item has the component type.
     */
    @ApiStatus.Experimental
    public boolean hasComponent(Object type) {
        final Object componentType = ComponentType.of(type);
        if (patch != null && patch.has(componentType)) {
            return patch.get(componentType).isPresent();
        }
        return DataComponent.Map.has(components, componentType);
    }

    /**
     * Change item tag into new one.<br>
     * Value must be Map&lt;String, Object&gt; or NBTTagCompound.
     *
     * @param value Object to replace current tag.
     * @return      True if tag has replaced.
     */
    @Override
    public boolean set(Object value) {
        if (value == null) {
            this.edited = true;
            this.tag = null;
            return true;
        } else if (super.set(value)) {
            this.edited = true;
            return true;
        }
        return false;
    }

    /**
     * Set the provided component into patch.
     *
     * @param type Data component type object or ID.
     */
    @ApiStatus.Experimental
    public void setComponent(Object type) {
        getPatch().set(ComponentType.of(type), Optional.of(Rtag.UNIT));
    }

    /**
     * Set the provided component value into patch.
     *
     * @param type  Data component type object or ID.
     * @param value The value to parse, can be NBT, JsonElement or Java object.
     */
    @ApiStatus.Experimental
    public void setComponent(Object type, Object value) {
        final Object parsed = ComponentType.parse(type, value).orElseThrow(() ->
                new RuntimeException("Cannot parse provided value into defined component type")
        );
        getPatch().set(ComponentType.of(type), Optional.of(parsed));
    }

    /**
     * Mark the provided component type to be removed using current patch.
     *
     * @param type Data component type object or ID.
     */
    @ApiStatus.Experimental
    public void removeComponent(Object type) {
        getPatch().remove(ComponentType.of(type));
    }

    @Override
    public Map<String, Object> get() {
        return this.tag == null ? Map.of() : super.get();
    }

    @Override
    public <V> V get(Object... path) {
        return this.tag == null ? null : super.get(path);
    }

    @Override
    public OptionalType getOptional(Object... path) {
        return this.tag == null ? OptionalType.EMPTY : super.getOptional(path);
    }

    @Override
    public Object getExact(Object... path) {
        return this.tag == null ? null : super.getExact(path);
    }

    /**
     * Get the provided component from patch or item.
     *
     * @param type Data component type object or ID.
     * @return     An object represented by component type or null if not exist.
     */
    @ApiStatus.Experimental
    public Object getComponent(Object type) {
        final Object componentType = ComponentType.of(type);
        if (patch != null && patch.has(componentType)) {
            return patch.get(componentType).orElse(null);
        }
        return DataComponent.Map.get(components, componentType);
    }

    /**
     * Check if the current item has the provided hide flags ordinals.
     *
     * @see #addHideFlags(int...) Flag ordinals.
     *
     * @param hideFlags Flags to check.
     * @return          true if the item has all the flags.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.6.0")
    @Deprecated
    @SuppressWarnings("unchecked")
    public boolean hasHideFlags(int... hideFlags) {
        // Since 1.20.5, items cannot hold hide flags
        if (ServerInstance.Release.COMPONENT) {
            for (int ordinal : hideFlags) {
                if (ordinal < 0 || ordinal >= HIDE_FLAGS.size()) return false;
                if (ordinal == 5 && getItem().getType() != Material.ENCHANTED_BOOK) {
                    if (!hasComponent("minecraft:hide_additional_tooltip")) return false;
                    continue;
                }
                final String name = HIDE_FLAGS.get(ordinal);
                final Object component = DataComponent.Map.get(components, ComponentType.of(name));
                if (component == null) return false;
                final Map<String, Object> map = (Map<String, Object>) ComponentType.encodeJava(name, component).orElse(null);
                if (map == null || !Boolean.FALSE.equals(map.get("show_in_tooltip"))) return false;
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
        if (ServerInstance.Release.COMPONENT) {
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
        if (ServerInstance.MAJOR_VERSION < 14) {
            return;
        }
        // Since 1.20.5, the server use a different serialization
        if (ServerInstance.Release.COMPONENT) {
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
    @ApiStatus.ScheduledForRemoval(inVersion = "1.6.0")
    @Deprecated
    @SuppressWarnings("unchecked")
    public boolean addHideFlags(int... hideFlags) {
        // Since 1.20.5, items cannot hold hide flags
        if (ServerInstance.Release.COMPONENT) {
            boolean result = false;
            for (int ordinal : hideFlags) {
                if (ordinal < 0 || ordinal >= HIDE_FLAGS.size()) continue;
                if (ordinal == 5 && getItem().getType() != Material.ENCHANTED_BOOK) {
                    result = true;
                    setComponent("minecraft:hide_additional_tooltip");
                    continue;
                }
                final String name = HIDE_FLAGS.get(ordinal);
                final Object component = DataComponent.Map.get(components, ComponentType.of(name));
                if (component == null) {
                    result = true;
                    setComponent(name, Map.of("show_in_tooltip", false));
                } else {
                    Map<String, Object> map = (Map<String, Object>) ComponentType.encodeJava(name, component).orElse(null);
                    if (map != null) {
                        if (Boolean.FALSE.equals(map.get("show_in_tooltip"))) continue;
                        result = true;
                        map = new HashMap<>(map);
                        map.put("show_in_tooltip", false);
                        setComponent(name, map);
                    }
                }
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
        if (ServerInstance.Release.COMPONENT) {
            getItem().addUnsafeEnchantment(tag.getEnchantment(), level);
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
        if (ServerInstance.Release.COMPONENT) {
            setComponent("minecraft:unbreakable", Map.of());
            return true;
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
        if (ServerInstance.Release.COMPONENT) {
            if (model == null) {
                removeComponent("minecraft:custom_model_data");
            } else {
                setComponent("minecraft:custom_model_data", model);
            }
            return true;
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
    @ApiStatus.ScheduledForRemoval(inVersion = "1.6.0")
    @Deprecated
    public boolean setHideFlags(int... hideFlags) {
        // Since 1.20.5, items cannot hold hide flags
        if (ServerInstance.Release.COMPONENT) {
            boolean result = addHideFlags(hideFlags);
            final Set<Integer> toRemove = new HashSet<>(Set.of(0, 1, 2, 3, 4, 5, 6, 7));
            for (int ordinal : hideFlags) {
                toRemove.remove(ordinal);
            }
            if (toRemove.isEmpty()) {
                return result;
            } else {
                final int[] flags = new int[toRemove.size()];
                int index = 0;
                for (Integer ordinal : toRemove) {
                    flags[index] = ordinal;
                    index++;
                }
                return removeHideFlags(flags) || result;
            }
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
        if (ServerInstance.Release.COMPONENT) {
            DataComponent.MapPatch.set(components, ComponentType.of("minecraft:repair_cost"), cost);
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
    @ApiStatus.ScheduledForRemoval(inVersion = "1.6.0")
    @Deprecated
    @SuppressWarnings("unchecked")
    public boolean removeHideFlags(int... hideFlags) {
        // Since 1.20.5, items cannot hold hide flags
        if (ServerInstance.Release.COMPONENT) {
            boolean result = false;
            for (int ordinal : hideFlags) {
                if (ordinal < 0 || ordinal >= HIDE_FLAGS.size()) continue;
                if (ordinal == 5 && getItem().getType() != Material.ENCHANTED_BOOK) {
                    result = result || hasComponent("minecraft:hide_additional_tooltip");
                    removeComponent("minecraft:hide_additional_tooltip");
                    continue;
                }
                final String name = HIDE_FLAGS.get(ordinal);
                final Object component = DataComponent.Map.get(components, ComponentType.of(name));
                if (component == null) continue;
                Map<String, Object> map = (Map<String, Object>) ComponentType.encodeJava(name, component).orElse(null);
                if (map != null && Boolean.FALSE.equals(map.get("show_in_tooltip"))) {
                    result = true;
                    if (map.size() == 1) {
                        removeComponent(name);
                    } else {
                        map = new HashMap<>(map);
                        map.put("show_in_tooltip", true);
                        setComponent(name, map);
                    }
                }
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
        if (ServerInstance.Release.COMPONENT) {
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
        if (ServerInstance.Release.COMPONENT) {
            return (Integer) ComponentType.encodeJava("minecraft:custom_model_data", getComponent("minecraft:custom_model_data")).orElse(null);
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
    @ApiStatus.ScheduledForRemoval(inVersion = "1.6.0")
    @Deprecated
    public Set<Integer> getHideFlags() {
        // Since 1.20.5, items cannot hold hide flags
        if (ServerInstance.Release.COMPONENT) {
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
        if (ServerInstance.Release.COMPONENT) {
            return (int) DataComponent.Map.get(components, ComponentType.of("minecraft:repair_cost"));
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
        if (ServerInstance.Release.COMPONENT) {
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
        if (ServerInstance.Release.COMPONENT) {
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
        if (ServerInstance.Release.COMPONENT) {
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
        if (ServerInstance.Release.COMPONENT) {
            return DataComponent.Map.has(components, ComponentType.of("minecraft:unbreakable"));
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
