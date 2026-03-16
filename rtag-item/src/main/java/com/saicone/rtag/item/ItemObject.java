package com.saicone.rtag.item;

import com.mojang.serialization.Codec;
import com.saicone.rtag.data.ComponentType;
import com.saicone.rtag.data.DataComponent;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.util.MC;
import com.saicone.rtag.util.ServerInstance;
import com.saicone.rtag.util.reflect.Lookup;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Optional;

/**
 * Class to invoke ItemStack methods across versions.
 *
 * @author Rubenicos
 */
public class ItemObject {

    // import
    private static final Lookup.AClass<?> DefaultedRegistry = Lookup.SERVER.importClass("net.minecraft.core.DefaultedRegistry");
    private static final Lookup.AClass<?> DataComponentPatch = Lookup.SERVER.importClass("net.minecraft.core.component.DataComponentPatch");
    private static final Lookup.AClass<?> BuiltInRegistries = Lookup.SERVER.importClass("net.minecraft.core.registries.BuiltInRegistries");
    private static final Lookup.AClass<?> CompoundTag = Lookup.SERVER.importClass("net.minecraft.nbt.CompoundTag");
    private static final Lookup.AClass<?> Identifier;
    static {
        if (MC.version().isNewerThanOrEquals(MC.V_1_21_11)) {
            Identifier = Lookup.SERVER.importClass("net.minecraft.resources.Identifier");
        } else {
            Identifier = Lookup.SERVER.importClass("net.minecraft.resources.ResourceLocation");
        }
    }
    private static final Lookup.AClass<?> Item = Lookup.SERVER.importClass("net.minecraft.world.item.Item");
    private static final Lookup.AClass<?> MC_ItemStack = Lookup.SERVER.importClass("net.minecraft.world.item.ItemStack");
    private static final Lookup.AClass<?> CustomData = Lookup.SERVER.importClass("net.minecraft.world.item.component.CustomData");
    private static final Lookup.AClass<?> CraftItemStack = Lookup.SERVER.importClass("org.bukkit.craftbukkit.inventory.CraftItemStack");

    // declare
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    private static final MethodHandle DefaultedRegistry_getValue;
    static {
        if (MC.version().isNewerThanOrEquals(MC.V_1_21_2)) {
            DefaultedRegistry_getValue = DefaultedRegistry.method(Object.class, "getValue", Identifier).handle();
        } else if (MC.version().isComponent()) {
            DefaultedRegistry_getValue = DefaultedRegistry.method(Object.class, "get", Identifier).handle();
        } else {
            DefaultedRegistry_getValue = null;
        }
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    private static final MethodHandle Identifier_parse;
    static {
        if (MC.version().isNewerThanOrEquals(MC.V_1_21)) {
            Identifier_parse = Identifier.method(Modifier.STATIC, Identifier, "parse", String.class).handle();
        } else if (MC.version().isNewerThanOrEquals(MC.V_1_19_3)) {
            Identifier_parse = Identifier.constructor(String.class).handle();
        } else {
            Identifier_parse = null;
        }
    }

    private static final MethodHandle CustomData$new_tag;
    private static final MethodHandle CustomData$get_tag;
    static {
        if (MC.version().isComponent()) {
            CustomData$new_tag = CustomData.constructor(CompoundTag).handle();
            CustomData$get_tag = CustomData.field(CompoundTag, "tag").getter();
        } else {
            CustomData$new_tag = null;
            CustomData$get_tag = null;
        }
    }

    private static final MethodHandle ItemStack_of;
    static {
        if (MC.version().isComponent()) {
            ItemStack_of = null;
        } else if (MC.version().isNewerThanOrEquals(MC.V_1_13) || MC.version().isOlderThanOrEquals(MC.V_1_10_2)) {
            ItemStack_of = MC_ItemStack.method(Modifier.STATIC, MC_ItemStack, "of", CompoundTag).handle();
        } else {
            ItemStack_of = MC_ItemStack.constructor(CompoundTag).handle();
        }
    }
    private static final MethodHandle ItemStack_isEmpty;
    static {
        if (MC.version().isNewerThanOrEquals(MC.V_1_11)) {
            ItemStack_isEmpty = MC_ItemStack.method(boolean.class, "isEmpty").handle();
        } else {
            ItemStack_isEmpty = null;
        }
    }
    private static final MethodHandle ItemStack_copy = MC_ItemStack.method(MC_ItemStack, "copy").handle();
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    private static final MethodHandle ItemStack$set_item;
    private static final MethodHandle ItemStack_applyComponentsAndValidate;
    private static final MethodHandle ItemStack_save;
    private static final MethodHandle ItemStack_load;
    private static final MethodHandle ItemStack_getTag;
    private static final MethodHandle ItemStack_setTag;
    static {
        if (MC.version().isComponent()) {
            ItemStack$set_item = MC_ItemStack.field(Item, "item").getter();
            ItemStack_applyComponentsAndValidate = MC_ItemStack.method(void.class, "applyComponentsAndValidate", DataComponentPatch).handle();
            ItemStack_save = null;
            ItemStack_load = null;
            ItemStack_getTag = null;
            ItemStack_setTag = null;
        } else {
            ItemStack$set_item = null;
            ItemStack_applyComponentsAndValidate = null;
            ItemStack_save = MC_ItemStack.method(CompoundTag, "save", CompoundTag).handle();
            ItemStack_load = MC_ItemStack.method(void.class, "load", CompoundTag).handle();
            ItemStack_getTag = MC_ItemStack.method(CompoundTag, "getTag").handle();
            ItemStack_setTag = MC_ItemStack.method(void.class, "setTag", CompoundTag).handle();
        }
    }
    private static final MethodHandle ItemStack_getCount;
    private static final MethodHandle ItemStack_setCount;
    static {
        if (MC.version().isNewerThanOrEquals(MC.V_1_11)) {
            ItemStack_getCount = MC_ItemStack.method(int.class, "getCount").handle();
            ItemStack_setCount = MC_ItemStack.method(void.class, "setCount", int.class).handle();
        } else {
            ItemStack_getCount = MC_ItemStack.field(int.class, "count").getter();
            ItemStack_setCount = MC_ItemStack.field(int.class, "count").setter();
        }
    }

    private static final MethodHandle CraftItemStack_getCraftStack;
    static {
        if (ServerInstance.Platform.PAPER && MC.version().isNewerThanOrEquals(MC.V_1_21)) {
            CraftItemStack_getCraftStack = CraftItemStack.method(Modifier.STATIC, CraftItemStack, "getCraftStack", ItemStack.class).handle();
        } else {
            CraftItemStack_getCraftStack = null;
        }
    }
    private static final MethodHandle CraftItemStack_asNMSCopy = CraftItemStack.method(Modifier.STATIC, MC_ItemStack, "asNMSCopy", ItemStack.class).handle();
    private static final MethodHandle CraftItemStack_asBukkitCopy = CraftItemStack.method(Modifier.STATIC, ItemStack.class, "asBukkitCopy", MC_ItemStack).handle();
    private static final MethodHandle CraftItemStack_asCraftMirror = CraftItemStack.method(Modifier.STATIC, CraftItemStack, "asCraftMirror", MC_ItemStack).handle();
    private static final MethodHandle CraftItemStack$new_item = CraftItemStack.constructor(ItemStack.class).handle();
    private static final MethodHandle CraftItemStack$get_handle = CraftItemStack.field(MC_ItemStack, "handle").getter();
    private static final MethodHandle CraftItemStack$set_handle = CraftItemStack.field(MC_ItemStack, "handle").setter();

    private static final Object CUSTOM_DATA;
    static {
        if (MC.version().isComponent()) {
            CUSTOM_DATA = ComponentType.of("minecraft:custom_data");
        } else {
            CUSTOM_DATA = null;
        }
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    private static final Object ITEM_REGISTRY;
    static {
        if (MC.version().isNewerThanOrEquals(MC.V_1_19_3)) {
            ITEM_REGISTRY = BuiltInRegistries.field(Modifier.STATIC, DefaultedRegistry, "ITEM").getValue();
        } else {
            ITEM_REGISTRY = null;
        }
    }

    /**
     * ItemStack codec.
     */
    @ApiStatus.Experimental
    public static final Object CODEC;
    static {
        if (MC.version().isNewerThanOrEquals(MC.V_1_16)) {
            CODEC = MC_ItemStack.field(Modifier.STATIC, Codec.class, "CODEC").getValue();
        } else {
            CODEC = null;
        }
    }

    ItemObject() {
    }

    /**
     * Create ItemStack from NBTTagCompound.
     *
     * @param compound NBTTagCompound that represent the item.
     * @return         A new ItemStack.
     */
    @SuppressWarnings("unchecked")
    public static Object newItem(Object compound) {
        if (MC.version().isComponent()) {
            return ((Codec<Object>) CODEC).parse(ComponentType.createGlobalContext(ComponentType.NBT_OPS), compound).result().orElse(null);
        } else {
            return Lookup.invoke(ItemStack_of, compound);
        }
    }

    /**
     * Create CraftItemStack from ItemStack object.
     *
     * @param item The item to use as constructor parameter.
     * @return     A new CraftItemStack.
     */
    public static ItemStack newCraftItem(ItemStack item) {
        return Lookup.invoke(CraftItemStack$new_item, item);
    }

    /**
     * Check if the provided object is instance of Minecraft ItemStack.
     *
     * @param object the object to check.
     * @return       true if the object is an instance of Minecraft ItemStack.
     */
    public static boolean isMinecraftItem(Object object) {
        return MC_ItemStack.isInstance(object);
    }

    /**
     * Check if the provided object is instance of CraftItemStack.
     *
     * @param object the object to check.
     * @return       true if the object is an instance of CraftItemStack.
     */
    public static boolean isCraftItem(Object object) {
        return CraftItemStack.isInstance(object);
    }

    /**
     * Check if the provided Minecraft ItemStack is empty.<br>
     * On versions before 1.11 this method only check item count.
     *
     * @param item the item to check.
     * @return     true if item is empty, false otherwise.
     */
    public static boolean isEmpty(Object item) {
        if (MC.version().isNewerThanOrEquals(MC.V_1_11)) {
            return Lookup.invoke(ItemStack_isEmpty, item);
        } else {
            return Lookup.<Integer>invoke(ItemStack_getCount, item) <= 0;
        }
    }

    /**
     * Check if the provided Minecraft ItemStack has custom data.<br>
     * On versions older than 1.20.5 this check item tag.
     *
     * @param item the item to check.
     * @return     true if the item has custom data.
     */
    public static boolean hasCustomData(Object item) {
        if (MC.version().isComponent()) {
            return DataComponent.Holder.has(item, CUSTOM_DATA);
        } else {
            return Lookup.invoke(ItemStack_getTag, item) != null;
        }
    }

    /**
     * Save current Item tag into new NBTTagCompound.
     *
     * @param item ItemStack instance.
     * @return     A NBTTagCompound that represent the item.
     */
    @SuppressWarnings("unchecked")
    public static Object save(Object item) {
        if (item == null) {
            return TagCompound.newTag();
        }
        if (MC.version().isComponent()) {
            if (isEmpty(item)) {
                return TagCompound.newTag();
            }
            return ((Codec<Object>) CODEC).encode(item, ComponentType.createGlobalContext(ComponentType.NBT_OPS), TagCompound.newTag()).getOrThrow();
        } else {
            return Lookup.invoke(ItemStack_save, item, TagCompound.newTag());
        }
    }

    /**
     * Load NBTTagCompound into ItemStack.
     *
     * @deprecated Since Minecraft 1.20.5, the safe-way to load data into item is applying a data component patch.
     * @see #apply(Object, Object)
     *
     * @param item     ItemStack instance.
     * @param compound The NBTTagCompound to load.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.6.0")
    @Deprecated
    public static void load(Object item, Object compound) {
        if (MC.version().isComponent()) {
            final Object id = TagCompound.get(compound, "id");
            if (id != null) {
                try {
                    final Object loadedItem = DefaultedRegistry_getValue.invoke(ITEM_REGISTRY, Identifier_parse.invoke(TagBase.getValue(id)));
                    if (loadedItem != null) {
                        ItemStack$set_item.invoke(item, loadedItem);
                    }
                } catch (Throwable t) {
                    throw new RuntimeException("Cannot set item id", t);
                }
            }
            final Object count = TagCompound.get(compound, "count");
            if (count != null) {
                Lookup.invoke(ItemStack_setCount, item, Integer.parseInt(String.valueOf(TagBase.getValue(count))));
            }
            final Object components = TagCompound.get(compound, "components");
            if (components != null) {
                final DataComponent.Builder<Optional<?>> builder = DataComponent.Patch.builder();
                for (Map.Entry<String, Object> entry : TagCompound.getValue(components).entrySet()) {
                    final Object type = ComponentType.of(entry.getKey());
                    if (type != null) {
                        ComponentType.parseNbt(entry.getKey(), entry.getValue()).ifPresent(component -> builder.set(type, Optional.of(component)));
                    }
                }
                apply(item, builder.build());
            }
        } else {
            apply(item, compound);
        }
    }

    /**
     * Load Minecraft ItemStack handle into Bukkit ItemStack.
     *
     * @param item   Bukkit ItemStack.
     * @param handle Minecraft ItemStack.
     */
    @SuppressWarnings("deprecation")
    public static void loadHandle(ItemStack item, Object handle) {
        final ItemStack mirror = asCraftMirror(handle);
        if (mirror != null) {
            item.setType(mirror.getType());
            item.setAmount(mirror.getAmount());
            if (MC.version().isLegacy()) {
                item.setDurability(mirror.getDurability());
            }
            item.setItemMeta(mirror.getItemMeta());
        }
    }

    /**
     * Apply data component into ItemStack.<br>
     * On versions before 1.20.5 this method load NBTTagCompound into item.
     *
     * @param item      ItemStack instance.
     * @param component The data component to apply into.
     */
    public static void apply(Object item, Object component) {
        if (MC.version().isComponent()) {
            Lookup.invoke(ItemStack_applyComponentsAndValidate, item, component);
        } else {
            Lookup.invoke(ItemStack_load, item, component);
        }
    }

    /**
     * Copy the provided Minecraft ItemStack.
     *
     * @param item ItemStack to copy.
     * @return     A copy from Minecraft ItemStack.
     */
    public static Object copy(Object item) {
        return Lookup.invoke(ItemStack_copy, item);
    }

    /**
     * Convert old tag path into new component path.<br>
     * This method skips first key of tag path, it assumes the given path has "tag" at first element
     *
     * @deprecated To get component paths use {@link ItemData#getComponentPath(Object...)} instead.
     * @see ItemData#getComponentPath(Object...)
     *
     * @return        a new component path representation of tag path.
     * @throws IndexOutOfBoundsException if starting position of source or destination path is out of range.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.6.0")
    @Deprecated
    public static Object[] getComponentPath(Object... path) throws IndexOutOfBoundsException {
        return ItemData.getComponentPath(0, 0, path);
    }

    /**
     * Convert old tag path into new component path.<br>
     * Every component path start with "components" key, if you want to skip that use a destPos of 1.
     *
     * @deprecated To get component paths use {@link ItemData#getComponentPath(int, int, Object...)} instead.
     * @see ItemData#getComponentPath(int, int, Object...) 
     *
     * @param srcPos  starting position in the source path.
     * @param destPos starting position in the extracted destination data.
     * @param src     the source path array.
     * @return        a new component path representation of tag path.
     * @throws IndexOutOfBoundsException if starting position of source or destination path is out of range.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.6.0")
    @Deprecated
    public static Object[] getComponentPath(int srcPos, int destPos, Object... src) throws IndexOutOfBoundsException {
        return ItemData.getComponentPath(srcPos, destPos, src);
    }

    /**
     * Convert new component path into old tag path.<br>
     * This method skips first key of component path, it assumes the given path has "components" at first element
     *
     * @deprecated To get tag paths use {@link ItemData#getTagPath(Object...)} instead.
     * @see ItemData#getTagPath(Object...)
     *
     * @return an old tag path representation of component path.
     * @throws IndexOutOfBoundsException if starting position of source or destination path is out of range.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.6.0")
    @Deprecated
    public static Object[] getTagPath(Object... path) throws IndexOutOfBoundsException {
        return ItemData.getTagPath(1, 0, path);
    }

    /**
     * Convert new component path into old tag path.<br>
     * Most tag paths start with "tag" key, if you want to skip that use a destPos of 1.
     *
     * @deprecated To get tag paths use {@link ItemData#getTagPath(int, int, Object...)} instead.
     * @see ItemData#getTagPath(int, int, Object...)
     *
     * @param srcPos  starting position in the source path.
     * @param destPos starting position in the extracted destination data.
     * @param src     the source path array.
     * @return        an old tag path representation of component path.
     * @throws IndexOutOfBoundsException if starting position of source or destination path is out of range.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.6.0")
    @Deprecated
    public static Object[] getTagPath(int srcPos, int destPos, Object... src) throws IndexOutOfBoundsException {
        return ItemData.getTagPath(srcPos, destPos, src);
    }

    /**
     * Get delegated CraftItemStack from ItemStack object or the object itself
     * if it's already a craft item.<br>
     * On non-paper platforms with versions before 1.21 this method will return
     * the provided item if is CraftItemStack instance.
     *
     * @param item the Bukkit ItemStack to extract delegated craft item.
     * @return     a CraftItemStack instance.
     */
    public static ItemStack getCraftStack(ItemStack item) {
        if (CraftItemStack_getCraftStack != null) {
            return Lookup.invoke(CraftItemStack_getCraftStack, item);
        } else if (CraftItemStack.isInstance(item)) {
            return item;
        } else {
            return null;
        }
    }

    /**
     * Get defined Minecraft ItemStack inside CraftItemStack or
     * create new one.
     *
     * @param item Bukkit ItemStack.
     * @return     Minecraft ItemStack.
     */
    public static Object getHandle(ItemStack item) {
        if (CraftItemStack.isInstance(item)) {
            return getUncheckedHandle(item);
        } else {
            return asNMSCopy(item);
        }
    }

    /**
     * Get defined Minecraft ItemStack inside CraftItemStack.
     *
     * @param item A CraftItemStack.
     * @return     Minecraft ItemStack or null.
     */
    public static Object getUncheckedHandle(ItemStack item) {
        return Lookup.invoke(CraftItemStack$get_handle, item);
    }

    /**
     * Get current NBTTagCompound from custom data component.<br>
     * On versions before 1.20.5 this method return item tag.
     *
     * @param item ItemStack instance.
     * @return     The custom data component inside provided item.
     */
    public static Object getCustomDataTag(Object item) {
        if (MC.version().isComponent()) {
            final Object customData = DataComponent.Holder.get(item, CUSTOM_DATA);
            if (customData == null) {
                return null;
            }
            return Lookup.invoke(CustomData$get_tag, customData);
        } else {
            return Lookup.invoke(ItemStack_getTag, item);
        }
    }

    /**
     * Get current NBTTagCompound.
     *
     * @deprecated To get item tag-like data use {@link #getCustomDataTag(Object)} instead.
     * @see #getCustomDataTag(Object)
     *
     * @param item ItemStack instance.
     * @return     The NBTTagCompound inside provided item.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.6.0")
    @Deprecated
    public static Object getTag(Object item) {
        return getCustomDataTag(item);
    }

    /**
     * Replace defined Minecraft ItemStack inside CraftItemStack or
     * load changes into Bukkit ItemStack.
     *
     * @param item   Bukkit ItemStack.
     * @param handle Minecraft ItemStack.
     */
    @SuppressWarnings("deprecation")
    public static void setHandle(ItemStack item, Object handle) {
        if (CraftItemStack.isInstance(item)) {
            Lookup.invoke(CraftItemStack$set_handle, item, handle);
        } else {
            ItemStack copy = asBukkitCopy(handle);
            if (copy != null) {
                item.setType(copy.getType());
                item.setAmount(copy.getAmount());
                if (MC.version().isLegacy()) {
                    item.setDurability(copy.getDurability());
                }
                item.setItemMeta(copy.getItemMeta());
            }
        }
    }

    /**
     * Overwrite current custom data component.<br>
     * On versions before 1.20.5 this method replace item tag.
     *
     * @param item ItemStack instance.
     * @param tag  NBTTagCompound to put into custom data component.
     */
    public static void setCustomDataTag(Object item, Object tag) {
        if (MC.version().isComponent()) {
            if (tag == null || TagCompound.getValue(tag).isEmpty()) {
                DataComponent.MapPatch.remove(DataComponent.Holder.getComponents(item), CUSTOM_DATA);
            } else {
                DataComponent.MapPatch.set(DataComponent.Holder.getComponents(item), CUSTOM_DATA, Lookup.invoke(CustomData$new_tag, tag));
            }
        } else {
            Lookup.invoke(ItemStack_setTag, item, tag);
        }
    }

    /**
     * Overwrite current NBTTagCompound.
     *
     * @deprecated To set item tag-like data use {@link #setCustomDataTag(Object, Object)} instead.
     * @see #setCustomDataTag(Object, Object)
     *
     * @param item ItemStack instance.
     * @param tag  NBTTagCompound to put into item.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.6.0")
    @Deprecated
    public static void setTag(Object item, Object tag) {
       setCustomDataTag(item, tag);
    }

    /**
     * Convert Minecraft ItemStack into Bukkit ItemStack.<br>
     * Take in count this method creates a new ItemStack instance.
     *
     * @param item Minecraft ItemStack.
     * @return     Bukkit ItemStack.
     */
    public static ItemStack asBukkitCopy(Object item) {
        return Lookup.invoke(CraftItemStack_asBukkitCopy, item);
    }

    /**
     * Convert Minecraft ItemStack into Bukkit ItemStack.<br>
     * Take in count this method creates a new CraftItemStack instance.
     *
     * @param item Minecraft ItemStack.
     * @return     Bukkit ItemStack.
     */
    public static ItemStack asCraftMirror(Object item) {
        return Lookup.invoke(CraftItemStack_asCraftMirror, item);
    }

    /**
     * Convert Bukkit ItemStack into Minecraft ItemStack.<br>
     * Take in count this method copy the original ItemStack
     *
     * @param item Bukkit ItemStack.
     * @return     Minecraft ItemStack.
     */
    public static Object asNMSCopy(ItemStack item) {
        return Lookup.invoke(CraftItemStack_asNMSCopy, item);
    }
}
