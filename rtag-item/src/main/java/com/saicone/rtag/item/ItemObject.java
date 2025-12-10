package com.saicone.rtag.item;

import com.mojang.serialization.Codec;
import com.saicone.rtag.data.ComponentType;
import com.saicone.rtag.data.DataComponent;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.util.EasyLookup;
import com.saicone.rtag.util.MC;
import com.saicone.rtag.util.ServerInstance;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.Optional;

/**
 * Class to invoke ItemStack methods across versions.
 *
 * @author Rubenicos
 */
public class ItemObject {

    // Import reflected classes
    static {
        try {
            EasyLookup.addNMSClass("world.item.ItemStack");
            if (MC.version().isComponent()) {
                EasyLookup.addNMSClass("core.RegistryBlocks", "DefaultedRegistry");
                EasyLookup.addNMSClass("world.item.component.CustomData");
            }

            EasyLookup.addOBCClass("inventory.CraftItemStack");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static final Class<?> MC_ITEM = EasyLookup.classById("ItemStack");
    private static final Class<?> CRAFT_ITEM = EasyLookup.classById("CraftItemStack");
    private static final Object CUSTOM_DATA;
    private static final Object ITEM_REGISTRY; // Remove in 2.0.0

    // Minecraft-related

    // since 1.20.5
    /**
     * ItemStack codec.
     */
    @ApiStatus.Experimental
    public static final Object CODEC;
    // pre 1.20.5
    private static final MethodHandle newItem;

    private static final MethodHandle newCustomData;
    private static final MethodHandle newMinecraftKey; // Remove in 2.0.0
    private static final MethodHandle save;
    private static final MethodHandle apply;
    private static final MethodHandle copy;
    private static final MethodHandle isEmpty;
    private static final MethodHandle getItem; // Remove in 2.0.0
    private static final MethodHandle getTag;
    private static final MethodHandle setItem; // Remove in 2.0.0
    private static final MethodHandle setTag; // < 1.20.5
    private static final MethodHandle setCount; // >= 1.20.5

    // CraftBukkit-related
    private static final MethodHandle newCraftItem;
    private static final MethodHandle getHandleField;
    private static final MethodHandle setHandleField;
    private static final MethodHandle getCraftStack;
    private static final MethodHandle asBukkitCopy;
    private static final MethodHandle asCraftMirror;
    private static final MethodHandle asNMSCopy;

    static {
        // Constants
        Object const$customData = null;
        Object const$item = null;
        Object const$codec = null;
        // Constructors
        MethodHandle new$ItemStack = null;
        MethodHandle new$CustomData = null;
        MethodHandle new$MinecraftKey = null;
        MethodHandle new$CraftItemStack = null;
        // Getters
        MethodHandle get$handle = null;
        // Setters
        MethodHandle set$handle = null;
        // Methods
        MethodHandle method$save = null;
        MethodHandle method$apply = null;
        MethodHandle method$copy = null;
        MethodHandle method$isEmpty = null;
        MethodHandle method$getItem = null;
        MethodHandle method$getTag = null;
        MethodHandle method$setItem = null;
        MethodHandle method$setTag = null;
        MethodHandle method$setCount = null;
        MethodHandle method$getCraftStack = null;
        MethodHandle method$asBukkitCopy = null;
        MethodHandle method$asNMSCopy = null;
        MethodHandle method$asCraftMirror = null;
        try {
            // Old method names
            String registry$item = "h";
            String key$parse = "a";
            String codec = "b";
            String createStack = "createStack";
            String save = "save";
            String apply = "c";
            String copy = "s";
            String isEmpty = "isEmpty";
            String getItem = "a";
            String getTag = "getTag";
            String setItem = "q";
            String setTag = "setTag";
            String setCount = "e";

            // New method names
            if (ServerInstance.Type.MOJANG_MAPPED) {
                registry$item = "ITEM";
                key$parse = "parse";
                codec = "CODEC";
                apply = "load";
                getItem = "get";
                setItem = "item";
                if (MC.version().isComponent()) {
                    apply = "applyComponentsAndValidate";
                    copy = "copy";
                    getTag = "tag";
                    setCount = "setCount";
                }
                if (MC.version().isNewerThanOrEquals(MC.V_1_21_2)) { // 1.21.2
                    getItem = "getValue";
                }
            } else {
                if (MC.version().isNewerThanOrEquals(MC.V_1_11)) {
                    apply = "load";
                }
                if (MC.version().isNewerThanOrEquals(MC.V_1_13)) {
                    createStack = "a";
                }
                if (MC.version().isNewerThanOrEquals(MC.V_1_18)) {
                    save = "b";
                    isEmpty = "b";
                    getTag = "s";
                    setTag = "c";
                }
                if (MC.version().isNewerThanOrEquals(MC.V_1_18_2)) { // 1.18.2
                    getTag = "t";
                }
                if (MC.version().isNewerThanOrEquals(MC.V_1_19)) {
                    getTag = "u";
                }
                if (MC.version().isNewerThanOrEquals(MC.V_1_20)) {
                    getTag = "v";
                }
                if (MC.version().isComponent()) {
                    apply = "a";
                    isEmpty = "e";
                    getTag = "e";
                }
                if (MC.version().isNewerThanOrEquals(MC.V_1_20_6)) { // 1.20.6
                    getTag = "f";
                }
                if (MC.version().isNewerThanOrEquals(MC.V_1_21)) {
                    registry$item = "g";
                }
                if (MC.version().isNewerThanOrEquals(MC.V_1_21_2)) { // 1.21.2
                    codec = "a";
                    copy = "v";
                    isEmpty = "f";
                    setItem = "o";
                }
                if (MC.version().isNewerThanOrEquals(MC.V_1_21_4)) { // 1.21.3
                    getTag = "g";
                    setItem = "p";
                }
                if (MC.version().isNewerThanOrEquals(MC.V_1_21_5)) { // 1.21.5
                    codec = "b";
                    setItem = "s";
                }
                if (MC.version().isNewerThanOrEquals(MC.V_1_21_9)) { // 1.21.9
                    registry$item = "h";
                    getTag = "e";
                }
            }

            if (MC.version().isComponent()) {
                const$customData = ComponentType.of("minecraft:custom_data");
                const$item = EasyLookup.classById("BuiltInRegistries").getDeclaredField(registry$item).get(null);

                const$codec = MC_ITEM.getDeclaredField(codec).get(null);
                new$CustomData = EasyLookup.constructor("CustomData", "NBTTagCompound");
                if (MC.version().isNewerThanOrEquals(MC.V_1_21)) {
                    new$MinecraftKey = EasyLookup.staticMethod("MinecraftKey", key$parse, "MinecraftKey", String.class);
                } else {
                    new$MinecraftKey = EasyLookup.constructor("MinecraftKey", String.class);
                }

                method$apply = EasyLookup.method(MC_ITEM, apply, void.class, "DataComponentPatch");
                method$copy = EasyLookup.method(MC_ITEM, copy, MC_ITEM);
                method$getItem = EasyLookup.method("RegistryBlocks", getItem, Object.class, "MinecraftKey");
                method$getTag = EasyLookup.getter("CustomData", getTag, "NBTTagCompound");
                method$setItem = EasyLookup.unreflectSetter(MC_ITEM, setItem);
                method$setCount = EasyLookup.method(MC_ITEM, setCount, void.class, int.class);
            } else {
                if (MC.version().isNewerThanOrEquals(MC.V_1_13) || MC.version().isOlderThanOrEquals(MC.V_1_10_2)) {
                    new$ItemStack = EasyLookup.staticMethod(MC_ITEM, createStack, "ItemStack", "NBTTagCompound");
                } else {
                    // (1.11 - 1.12) Only by public constructor
                    new$ItemStack = EasyLookup.constructor(MC_ITEM, "NBTTagCompound");
                }

                method$save = EasyLookup.method(MC_ITEM, save, "NBTTagCompound", "NBTTagCompound");
                // Private method
                method$apply = EasyLookup.method(MC_ITEM, apply, void.class, "NBTTagCompound");
                method$getTag = EasyLookup.method(MC_ITEM, getTag, "NBTTagCompound");
                method$setTag = EasyLookup.method(MC_ITEM, setTag, void.class, "NBTTagCompound");
            }
            if (MC.version().isNewerThanOrEquals(MC.V_1_11)) {
                method$isEmpty = EasyLookup.method(MC_ITEM, isEmpty, boolean.class);
            } else {
                method$isEmpty = EasyLookup.getter(MC_ITEM, "count", int.class);
            }

            new$CraftItemStack = EasyLookup.constructor(CRAFT_ITEM, ItemStack.class);

            // Private field
            get$handle = EasyLookup.getter(CRAFT_ITEM, "handle", MC_ITEM);
            set$handle = EasyLookup.setter(CRAFT_ITEM, "handle", MC_ITEM);

            if (ServerInstance.Platform.PAPER && MC.version().isNewerThanOrEquals(MC.V_1_21)) {
                method$getCraftStack = EasyLookup.method(CRAFT_ITEM, "getCraftStack", CRAFT_ITEM, ItemStack.class);
            }
            method$asBukkitCopy = EasyLookup.staticMethod(CRAFT_ITEM, "asBukkitCopy", ItemStack.class, "ItemStack");
            method$asCraftMirror = EasyLookup.staticMethod(CRAFT_ITEM, "asCraftMirror", CRAFT_ITEM, "ItemStack");
            // Bukkit -> Minecraft
            method$asNMSCopy = EasyLookup.staticMethod(CRAFT_ITEM, "asNMSCopy", "ItemStack", ItemStack.class);
        } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }

        CUSTOM_DATA = const$customData;
        ITEM_REGISTRY = const$item;

        CODEC = const$codec;
        newItem = new$ItemStack;
        newCustomData = new$CustomData;
        newMinecraftKey = new$MinecraftKey;
        save = method$save;
        apply = method$apply;
        copy = method$copy;
        isEmpty = method$isEmpty;
        getItem = method$getItem;
        getTag = method$getTag;
        setItem = method$setItem;
        setTag = method$setTag;
        setCount = method$setCount;

        newCraftItem = new$CraftItemStack;
        getHandleField = get$handle;
        setHandleField = set$handle;
        getCraftStack = method$getCraftStack;
        asBukkitCopy = method$asBukkitCopy;
        asCraftMirror = method$asCraftMirror;
        asNMSCopy = method$asNMSCopy;
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
            try {
                return newItem.invoke(compound);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    /**
     * Create CraftItemStack from ItemStack object.
     *
     * @param item The item to use as constructor parameter.
     * @return     A new CraftItemStack.
     */
    public static ItemStack newCraftItem(ItemStack item) {
        try {
            return (ItemStack) newCraftItem.invoke(item);
        } catch (Throwable t) {
            throw new RuntimeException("Cannot create CraftItemStack using ItemStack object", t);
        }
    }

    /**
     * Check if the provided object is instance of Minecraft ItemStack.
     *
     * @param object the object to check.
     * @return       true if the object is an instance of Minecraft ItemStack.
     */
    public static boolean isMinecraftItem(Object object) {
        return MC_ITEM.isInstance(object);
    }

    /**
     * Check if the provided object is instance of CraftItemStack.
     *
     * @param object the object to check.
     * @return       true if the object is an instance of CraftItemStack.
     */
    public static boolean isCraftItem(Object object) {
        return CRAFT_ITEM.isInstance(object);
    }

    /**
     * Check if the provided Minecraft ItemStack is empty.<br>
     * On versions before 1.11 this method only check item count.
     *
     * @param item the item to check.
     * @return     true if item is empty, false otherwise.
     */
    public static boolean isEmpty(Object item) {
        try {
            if (MC.version().isNewerThanOrEquals(MC.V_1_11)) {
                return (boolean) isEmpty.invoke(item);
            } else {
                return (int) isEmpty.invoke(item) <= 0;
            }
        } catch (Throwable t) {
            throw new RuntimeException("Cannot check if ItemStack is empty", t);
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
            try {
                return getTag.invoke(item) != null;
            } catch (Throwable t) {
                throw new RuntimeException("Cannot get tag from Minecraft ItemStack", t);
            }
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
        try {
            if (MC.version().isComponent()) {
                if ((boolean) isEmpty.invoke(item)) {
                    return TagCompound.newTag();
                }
                return ((Codec<Object>) CODEC).encode(item, ComponentType.createGlobalContext(ComponentType.NBT_OPS), TagCompound.newTag()).getOrThrow();
            } else {
                return save.invoke(item, TagCompound.newTag());
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
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
                    final Object loadedItem = getItem.invoke(ITEM_REGISTRY, newMinecraftKey.invoke(TagBase.getValue(id)));
                    if (loadedItem != null) {
                        setItem.invoke(item, loadedItem);
                    }
                } catch (Throwable t) {
                    throw new RuntimeException("Cannot set item id", t);
                }
            }
            final Object count = TagCompound.get(compound, "count");
            if (count != null) {
                try {
                    setCount.invoke(item, Integer.parseInt(String.valueOf(TagBase.getValue(count))));
                } catch (Throwable t) {
                    throw new RuntimeException("Cannot set item count", t);
                }
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
        try {
            apply.invoke(item, component);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Copy the provided Minecraft ItemStack.
     *
     * @param item ItemStack to copy.
     * @return     A copy from Minecraft ItemStack.
     */
    public static Object copy(Object item) {
        if (MC.version().isComponent()) {
            try {
                return copy.invoke(item);
            } catch (Throwable t) {
                throw new RuntimeException("Cannot copy the provided Minecraft ItemStack", t);
            }
        } else {
            return newItem(save(item));
        }
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
        if (getCraftStack != null) {
            try {
                return (ItemStack) getCraftStack.invoke(item);
            } catch (Throwable t) {
                throw new RuntimeException("Cannot get CraftItemStack from ItemStack object", t);
            }
        } else if (CRAFT_ITEM.isInstance(item)) {
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
        if (CRAFT_ITEM.isInstance(item)) {
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
        try {
            return getHandleField.invoke(item);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
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
            try {
                return getTag.invoke(customData);
            } catch (Throwable t) {
                throw new RuntimeException("Cannot get tag from custom data component", t);
            }
        } else {
            try {
                return getTag.invoke(item);
            } catch (Throwable t) {
                throw new RuntimeException("Cannot get tag from Minecraft ItemStack", t);
            }
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
        if (CRAFT_ITEM.isInstance(item)) {
            try {
                setHandleField.invoke(item, handle);
            } catch (Throwable t) {
                throw new RuntimeException("Cannot set handle into CraftItemStack", t);
            }
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
            try {
                if (tag == null || TagCompound.getValue(tag).isEmpty()) {
                    DataComponent.MapPatch.remove(DataComponent.Holder.getComponents(item), CUSTOM_DATA);
                } else {
                    DataComponent.MapPatch.set(DataComponent.Holder.getComponents(item), CUSTOM_DATA, newCustomData.invoke(tag));
                }
            } catch (Throwable t) {
                throw new RuntimeException("Cannot set custom data tag to Minecraft ItemStack", t);
            }
        } else {
            try {
                setTag.invoke(item, tag);
            } catch (Throwable t) {
                throw new RuntimeException("Cannot set tag to Minecraft ItemStack", t);
            }
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
        try {
            return (ItemStack) asBukkitCopy.invoke(item);
        } catch (Throwable t) {
            throw new RuntimeException("Cannot convert Minecraft ItemStack into Bukkit ItemStack", t);
        }
    }

    /**
     * Convert Minecraft ItemStack into Bukkit ItemStack.<br>
     * Take in count this method creates a new CraftItemStack instance.
     *
     * @param item Minecraft ItemStack.
     * @return     Bukkit ItemStack.
     */
    public static ItemStack asCraftMirror(Object item) {
        try {
            return (ItemStack) asCraftMirror.invoke(item);
        } catch (Throwable t) {
            throw new RuntimeException("Cannot convert Minecraft ItemStack into Bukkit ItemStack", t);
        }
    }

    /**
     * Convert Bukkit ItemStack into Minecraft ItemStack.<br>
     * Take in count this method copy the original ItemStack
     *
     * @param item Bukkit ItemStack.
     * @return     Minecraft ItemStack.
     */
    public static Object asNMSCopy(ItemStack item) {
        try {
            return asNMSCopy.invoke(item);
        } catch (Throwable t) {
            throw new RuntimeException("Cannot convert Bukkit ItemStack into Minecraft ItemStack", t);
        }
    }
}
