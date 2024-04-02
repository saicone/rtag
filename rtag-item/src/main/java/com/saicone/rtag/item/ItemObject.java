package com.saicone.rtag.item;

import com.saicone.rtag.data.DataComponent;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.util.EasyLookup;
import com.saicone.rtag.util.ServerInstance;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodHandle;
import java.util.*;

/**
 * Class to invoke ItemStack methods across versions.
 *
 * @author Rubenicos
 */
public class ItemObject {

    private static final String ROOT_PATH = "==root";
    private static final Class<?> MC_ITEM = EasyLookup.classById("ItemStack");
    private static final Class<?> CRAFT_ITEM = EasyLookup.classById("CraftItemStack");
    private static final Object CUSTOM_DATA = DataComponent.type("minecraft:custom_data");

    private static final Map<String, Object> tagPaths = new LinkedHashMap<>();
    private static final Map<String, Object> componentPaths = new LinkedHashMap<>();

    private static final MethodHandle newItem;
    private static final MethodHandle newCustomData;
    private static final MethodHandle getHandleField;
    private static final MethodHandle setHandleField;
    private static final MethodHandle save;
    private static final MethodHandle apply;
    private static final MethodHandle copy;
    private static final MethodHandle getTag;
    private static final MethodHandle setTag; // < 1.20.5
    private static final MethodHandle setCount; // >= 1.20.5
    private static final MethodHandle asBukkitCopy;
    private static final MethodHandle asNMSCopy;

    static {
        initPaths();

        // Constructors
        MethodHandle new$ItemStack = null;
        MethodHandle new$CustomData = null;
        // Getters
        MethodHandle get$handle = null;
        // Setters
        MethodHandle set$handle = null;
        // Methods
        MethodHandle method$save = null;
        MethodHandle method$apply = null;
        MethodHandle method$copy = null;
        MethodHandle method$getTag = null;
        MethodHandle method$setTag = null;
        MethodHandle method$setCount = null;
        MethodHandle method$asBukkitCopy = null;
        MethodHandle method$asNMSCopy = null;
        try {
            // TODO: Add non-mapped names for 1.20.5

            // Old method names
            String createStack = "createStack";
            String save = "save";
            String apply = "c";
            String copy = "";
            String getTag = "getTag";
            String setTag = "setTag";
            String setCount = "";

            // New method names
            if (ServerInstance.Type.MOJANG_MAPPED) {
                if (ServerInstance.Release.COMPONENT) {
                    createStack = "parseOptional";
                    save = "saveOptional";
                    apply = "applyComponents";
                    copy = "copy";
                    getTag = "getUnsafe";
                    setCount = "setCount";
                } else {
                    createStack = "of";
                    apply = "load";
                }
            } else if (ServerInstance.MAJOR_VERSION >= 11) {
                apply = "load";
                if (ServerInstance.MAJOR_VERSION >= 13) {
                    createStack = "a";
                    if (ServerInstance.MAJOR_VERSION >= 18) {
                        save = "b";
                        setTag = "c";
                        if (ServerInstance.MAJOR_VERSION >= 20) {
                            getTag = "v";
                        } else if (ServerInstance.MAJOR_VERSION >= 19) {
                            getTag = "u";
                        } else {
                            getTag = ServerInstance.RELEASE_VERSION >= 2 ? "t" : "s";
                        }
                    }
                }
            }

            if (ServerInstance.Release.COMPONENT) {
                EasyLookup.addNMSClass("world.item.component.CustomData");
                new$ItemStack = EasyLookup.staticMethod(MC_ITEM, createStack, "ItemStack", "HolderLookup.Provider", "NBTTagCompound");
                new$CustomData = EasyLookup.constructor("CustomData", "NBTTagCompound");
            } else if (ServerInstance.MAJOR_VERSION >= 13 || ServerInstance.MAJOR_VERSION <= 10) {
                new$ItemStack = EasyLookup.staticMethod(MC_ITEM, createStack, "ItemStack", "NBTTagCompound");
            } else {
                // (1.11 - 1.12) Only by public constructor
                new$ItemStack = EasyLookup.constructor(MC_ITEM, "NBTTagCompound");
            }

            // Private field
            get$handle = EasyLookup.getter(CRAFT_ITEM, "handle", MC_ITEM);
            set$handle = EasyLookup.setter(CRAFT_ITEM, "handle", MC_ITEM);

            if (ServerInstance.Release.COMPONENT) {
                method$save = EasyLookup.method(MC_ITEM, save, "NBTBase", "HolderLookup.Provider");
                method$apply = EasyLookup.method(MC_ITEM, apply, void.class, "DataComponentPatch");
                method$copy = EasyLookup.method(MC_ITEM, copy, MC_ITEM);
                method$getTag = EasyLookup.unreflectGetter("CustomData", getTag);
                method$setCount = EasyLookup.method(MC_ITEM, setCount, void.class, int.class);
            } else {
                method$save = EasyLookup.method(MC_ITEM, save, "NBTTagCompound", "NBTTagCompound");
                // Private method
                method$apply = EasyLookup.method(MC_ITEM, apply, void.class, "NBTTagCompound");
                method$getTag = EasyLookup.method(MC_ITEM, getTag, "NBTTagCompound");
                method$setTag = EasyLookup.method(MC_ITEM, setTag, void.class, "NBTTagCompound");
            }
            method$asBukkitCopy = EasyLookup.staticMethod(CRAFT_ITEM, "asBukkitCopy", ItemStack.class, "ItemStack");
            // Bukkit -> Minecraft
            method$asNMSCopy = EasyLookup.staticMethod(CRAFT_ITEM, "asNMSCopy", "ItemStack", ItemStack.class);
        } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        newItem = new$ItemStack;
        newCustomData = new$CustomData;
        getHandleField = get$handle;
        setHandleField = set$handle;
        save = method$save;
        apply = method$apply;
        copy = method$copy;
        getTag = method$getTag;
        setTag = method$setTag;
        setCount = method$setCount;
        asBukkitCopy = method$asBukkitCopy;
        asNMSCopy = method$asNMSCopy;
    }

    ItemObject() {
    }

    private static void initPaths() {
        initPath("minecraft:repair_cost", "tag", "RepairCost");
        initPath("minecraft:unbreakable", "tag", "Unbreakable");
        initPath("minecraft:stored_enchantments", "tag", "StoredEnchantments");
        initPath("minecraft:custom_name", "tag", "display", "Name");
        initPath("minecraft:lore", "tag", "display", "Lore");
        initPath("minecraft:dyed_color", "tag", "display", "color");
        initPath("minecraft:map_color", "tag", "display", "MapColor");
        initPath("minecraft:map_decorations", "tag", "Decorations");
        initPath("minecraft:map_id", "tag", "map");
        initPath("minecraft:can_break", "tag", "CanDestroy");
        initPath("minecraft:can_place_on", "tag", "CanPlaceOn");
        initPath("minecraft:attribute_modifiers", "tag", "AttributeModifiers");
        initPath("minecraft:charged_projectiles", "tag", "ChargedProjectiles");
        initPath("minecraft:bundle_contents", "tag", "Items");
        initPath("minecraft:custom_model_data", "tag", "CustomModelData");
        initPath("minecraft:trim", "tag", "Trim");
        initPath("minecraft:suspicious_stew_effects", "tag", "effects");
        initPath("minecraft:debug_stick_state", "tag", "DebugProperty");
        initPath("minecraft:entity_data", "tag", "EntityTag");
        initPath("minecraft:instrument", "tag", "instrument");
        initPath("minecraft:recipes", "tag", "Recipes");
        initPath("minecraft:profile", "tag", "SkullOwner");
        initPath("minecraft:note_block_sound", "tag", "BlockEntityTag", "note_block_sound");
        initPath("minecraft:base_color", "tag", "BlockEntityTag", "Base");
        initPath("minecraft:banner_patterns", "tag", "BlockEntityTag", "Patterns");
        initPath("minecraft:pot_decorations", "tag", "BlockEntityTag", "sherds");
        initPath("minecraft:container", "tag", "BlockEntityTag", "Items");
        initPath("minecraft:bees", "tag", "BlockEntityTag", "Bees");
        initPath("minecraft:lock", "tag", "BlockEntityTag", "Lock");
        initPath("minecraft:container_loot", new Object[] { "tag", "BlockEntityTag" }, Map.of(
                "loot_table", "LootTable",
                "seed", "LootTableSeed"
        ));
        initPath("minecraft:block_entity_data", "tag", "BlockEntityTag");
        initPath("minecraft:block_state", "tag", "BlockStateTag");
        initPath("minecraft:potion_contents", Map.of(
                "potion", "Potion",
                "custom_color", "CustomPotionColor",
                "custom_effects", "custom_potion_effects"
        ));
        initPath("minecraft:writable_book_content", Map.of(
                "pages", "pages"
        ));
        initPath("minecraft:written_book_content", Map.of(
                "pages", "pages",
                "title", "title",
                "author", "author",
                "generation", "generation",
                "resolved", "resolved"
        ));
        initPath("minecraft:bucket_entity_data", "tag");
        initPath("minecraft:lodestone_tracker", Map.of(
                "tracked", "LodestoneTracked",
                "target", Map.of(
                        "pos", "LodestonePos",
                        "dimension", "LodestoneDimension"
                )
        ));
        initPath("minecraft:firework_explosion", new Object[] { "tag", "Explosion" }, Map.of(
                "shape", "Type",
                "colors", "Colors",
                "fade_colors", "FadeColors",
                "has_trail", "Trail",
                "has_twinkle", "Flicker"
        ));
        initPath("minecraft:fireworks", new Object[] { "tag", "Fireworks" }, Map.of(
                "explosions", "Explosions",
                "flight_duration", "Flight"
        ));
        if (ServerInstance.Release.LEGACY) {
            initPath("minecraft:damage", "Damage");
            initPath("minecraft:enchantments", "tag", "ench");
        } else {
            initPath("minecraft:damage", "tag", "Damage");
            initPath("minecraft:enchantments", "tag", "Enchantments");
        }
        initPath("minecraft:custom_data", "tag");

        // --- Not exist in old versions
        // - 24w09a
        initPath("minecraft:intangible_projectile", "tag", "components", "minecraft:intangible_projectile");
        initPath("minecraft:enchantment_glint_override", "tag", "components", "minecraft:enchantment_glint_override");
        // - 24w12a
        initPath("minecraft:food", "tag", "components", "minecraft:food");
        initPath("minecraft:max_stack_size", "tag", "components", "minecraft:max_stack_size");
        initPath("minecraft:max_damage", "tag", "components", "minecraft:max_damage");
        initPath("minecraft:fire_resistant", "tag", "components", "minecraft:fire_resistant");
        initPath("minecraft:rarity", "tag", "components", "minecraft:rarity");
        initPath("minecraft:tool", "tag", "components", "minecraft:tool");
        initPath("minecraft:hide_tooltip", "tag", "components", "minecraft:hide_tooltip");
        // - 24w13a
        initPath("minecraft:ominous_bottle_amplifier", "tag", "components", "minecraft:ominous_bottle_amplifier");
        // --- Not supported
        // minecraft:hide_additional_tooltip = Same has 6th bit from tag.HideFlags
    }

    private static void initPath(String name, Object... path) {
        componentPaths.put(name, path);
        loadTagPath(path, new Object[] { name });
    }

    private static void initPath(String name, Map<String, Object> aliases) {
        initPath(name, new Object[] { "tag" }, aliases);
    }

    private static void initPath(String name, Object[] path, Map<String, Object> aliases) {
        componentPaths.put(name, loadComponentPath(new Object[] { name }, path, aliases));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadComponentPath(Object[] root, Object[] start, Map<String, Object> path) {
        final Map<String, Object> map = new HashMap<>();
        for (var entry : path.entrySet()) {
            if (entry.getValue() instanceof Map) {
                map.put(entry.getKey(), loadComponentPath(append(root, entry.getKey()), start, (Map<String, Object>) entry.getValue()));
            } else if (entry.getValue() instanceof String) {
                final Object[] tagPath = append(start, entry.getValue());
                map.put(entry.getKey(), tagPath);
                loadTagPath(tagPath, append(root, entry.getKey()));
            }
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private static void loadTagPath(Object[] tagPath, Object[] componentPath) {
        Map<String, Object> map = tagPaths;
        if (tagPath.length >= 2) {
            for (int i = 0; i < tagPath.length - 1; i++) {
                final String key = String.valueOf(tagPath[i]);
                Map<String, Object> subMap = (Map<String, Object>) map.get(key);
                if (subMap == null) {
                    subMap = new HashMap<>();
                    map.put(key, subMap);
                }
                map = subMap;
            }
        }
        final String key = String.valueOf(tagPath[tagPath.length - 1]);
        map.put(map.get(key) == null ? key : ROOT_PATH, appendFirst(componentPath, "components"));
    }

    /**
     * Create ItemStack from NBTTagCompound.
     *
     * @param compound NBTTagCompound that represent the item.
     * @return         A new ItemStack.
     */
    public static Object newItem(Object compound) {
        try {
            if (ServerInstance.Release.COMPONENT) {
                // TODO: Add RegistryAccess to args
                return newItem.invoke(null, compound);
            } else {
                return newItem.invoke(compound);
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
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
     * Check if the provided Minecraft ItemStack has custom data.<br>
     * On versions older than 1.20.5 this check item tag.
     *
     * @param item the item to check.
     * @return     true if the item has custom data.
     */
    public static boolean hasCustomData(Object item) {
        if (ServerInstance.Release.COMPONENT) {
            return DataComponent.Holder.has(item, CUSTOM_DATA);
        } else {
            return getTag(item) != null;
        }
    }

    /**
     * Save current Item tag into new NBTTagCompound.
     *
     * @param item ItemStack instance.
     * @return     A NBTTagCompound that represent the item.
     */
    public static Object save(Object item) {
        if (item == null) {
            return TagCompound.newTag();
        }
        try {
            if (ServerInstance.Release.COMPONENT) {
                // TODO: Add save by providing a lookup
                return save.invoke(item, null);
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
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @Deprecated
    public static void load(Object item, Object compound) {
        if (ServerInstance.Release.COMPONENT) {
            final Object id = TagCompound.get(compound, "id");
            if (id != null) {
                // TODO: Replace item field inside ItemStack
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
                final DataComponent.Builder builder = DataComponent.Patch.builder();
                for (Map.Entry<String, Object> entry : TagCompound.getValue(components).entrySet()) {
                    final Object type = DataComponent.type(entry.getKey());
                    if (type != null) {
                        // TODO: Convert NBT tag value into component-required value
                    }
                }
                apply(item, builder.build());
            }
        } else {
            apply(item, compound);
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
        if (ServerInstance.Release.COMPONENT) {
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
     * @return        a new component path representation of tag path.
     * @throws IndexOutOfBoundsException if starting position of source or destination path is out of range.
     */
    public static Object[] getComponentPath(Object... path) throws IndexOutOfBoundsException {
        return getComponentPath(0, 0, path);
    }

    /**
     * Convert old tag path into new component path.<br>
     * Every component path start with "components" key, if you want to skip that use a destPos of 1.
     *
     * @param srcPos  starting position in the source path.
     * @param destPos starting position in the extracted destination data.
     * @param src     the source path array.
     * @return        a new component path representation of tag path.
     * @throws IndexOutOfBoundsException if starting position of source or destination path is out of range.
     */
    @SuppressWarnings("unchecked")
    public static Object[] getComponentPath(int srcPos, int destPos, Object... src) throws IndexOutOfBoundsException {
        if (src == null) {
            return null;
        }
        if (srcPos < 0 || srcPos >= src.length) {
            throw new IndexOutOfBoundsException("Source position out of range:" + srcPos);
        }

        Map<String, Object> map = tagPaths;
        Object[] tagPath = null;
        int i;
        for (i = srcPos; i < src.length ; i++) {
            Object value = map.get(String.valueOf(src[i]));
            if (value instanceof Map) {
                map = (Map<String, Object>) value;
                continue;
            }
            if (value instanceof Object[]) {
                tagPath = (Object[]) value;
            } else {
                tagPath = (Object[]) map.get(ROOT_PATH);
            }
            break;
        }

        if (tagPath == null) {
            return src;
        }
        if (destPos < 0 || destPos >= tagPath.length) {
            throw new IndexOutOfBoundsException("Destination position out of range:" + srcPos);
        }

        i++;
        final int srcLength = src.length - i;
        final int destLength = tagPath.length - destPos;
        final Object[] path = new Object[srcLength + destLength];
        System.arraycopy(tagPath, destPos, path, 0, destLength);
        if (i < src.length) {
            System.arraycopy(src, i, path, destLength, srcLength);
        }
        return path;
    }

    /**
     * Convert new component path into old tag path.<br>
     * This method skips first key of component path, it assumes the given path has "components" at first element
     *
     * @return an old tag path representation of component path.
     * @throws IndexOutOfBoundsException if starting position of source or destination path is out of range.
     */
    public static Object[] getTagPath(Object... path) throws IndexOutOfBoundsException {
        return getTagPath(1, 0, path);
    }

    /**
     * Convert new component path into old tag path.<br>
     * Most tag paths start with "tag" key, if you want to skip that use a destPos of 1.
     *
     * @param srcPos  starting position in the source path.
     * @param destPos starting position in the extracted destination data.
     * @param src     the source path array.
     * @return        an old tag path representation of component path.
     * @throws IndexOutOfBoundsException if starting position of source or destination path is out of range.
     */
    @SuppressWarnings("unchecked")
    public static Object[] getTagPath(int srcPos, int destPos, Object... src) throws IndexOutOfBoundsException {
        if (src == null) {
            return null;
        }
        if (srcPos < 0 || srcPos >= src.length) {
            throw new IndexOutOfBoundsException("Source position out of range:" + srcPos);
        }

        Map<String, Object> map = componentPaths;
        Object[] tagPath = null;
        int i;
        for (i = srcPos; i < src.length ; i++) {
            String key = String.valueOf(src[i]);
            // First key transformation
            if (i == srcPos && !key.contains(":")) {
                key = "minecraft:" + key;
            }
            final Object value = map.get(key);
            if (value instanceof Map) {
                map = (Map<String, Object>) value;
            } else if (value instanceof Object[]) {
                tagPath = (Object[]) value;
                break;
            } else {
                return src;
            }
        }

        if (tagPath == null) {
            return src;
        }
        if (destPos < 0 || destPos >= tagPath.length) {
            throw new IndexOutOfBoundsException("Destination position out of range:" + srcPos);
        }

        i++;
        final int srcLength = src.length - i;
        final int destLength = tagPath.length - destPos;
        final Object[] path = new Object[srcLength + destLength];
        System.arraycopy(tagPath, destPos, path, 0, destLength);
        if (i < src.length) {
            System.arraycopy(src, i, path, destLength, srcLength);
        }
        return path;
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
            try {
                return getHandleField.invoke(item);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        } else {
            return asNMSCopy(item);
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
        if (ServerInstance.Release.COMPONENT) {
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
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
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
                if (ServerInstance.Release.LEGACY) {
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
        if (ServerInstance.Release.COMPONENT) {
            try {
                DataComponent.MapPatch.set(DataComponent.Holder.getComponents(item), CUSTOM_DATA, newCustomData.invoke(tag));
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
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
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

    private static Object[] append(Object[] array, Object obj) {
        final Object[] a = new Object[array.length + 1];
        System.arraycopy(array, 0, a, 0, array.length);
        a[a.length - 1] = obj;
        return a;
    }

    private static Object[] appendFirst(Object[] array, Object obj) {
        final Object[] a = new Object[array.length + 1];
        System.arraycopy(array, 0, a, 1, array.length);
        a[0] = obj;
        return a;
    }
}
