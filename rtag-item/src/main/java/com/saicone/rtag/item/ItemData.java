package com.saicone.rtag.item;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ItemData {

    private static final String ROOT_PATH = "==root";

    private static final Map<String, Object> TAG_PATHS = new LinkedHashMap<>();
    private static final Map<String, Object> COMPONENT_PATHS = new LinkedHashMap<>();

    static {
        initPaths();
    }

    ItemData() {
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

        Map<String, Object> map = TAG_PATHS;
        Object[] componentPath = null;
        int i;
        for (i = srcPos; i < src.length ; i++) {
            Object value = map.get(String.valueOf(src[i]));
            if (value instanceof Map) {
                map = (Map<String, Object>) value;
                continue;
            }
            if (value instanceof Object[]) {
                componentPath = (Object[]) value;
            } else {
                componentPath = (Object[]) map.get(ROOT_PATH);
                i--;
            }
            break;
        }

        if (componentPath == null) {
            return src;
        }
        if (destPos < 0 || destPos >= componentPath.length) {
            throw new IndexOutOfBoundsException("Destination position out of range:" + srcPos);
        }

        i++;
        final int srcLength = src.length - i;
        final int destLength = componentPath.length - destPos;
        final Object[] path = new Object[srcLength + destLength];
        System.arraycopy(componentPath, destPos, path, 0, destLength);
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

        Map<String, Object> map = COMPONENT_PATHS;
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

    // Data initialization

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
        initPath("minecraft:profile", new Object[] { "tag", "SkullOwner" }, Map.of(
                "name", "Name",
                "id", "Id",
                "properties", "Properties"
        ));
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
//        initPath("minecraft:writable_book_contents", Map.of(
//                "filtered_pages", "filtered_pages",
//                "pages", "pages"
//        ));
        initPath("minecraft:written_book_contents", Map.of(
                "filtered_pages", "filtered_pages",
                "filtered_title", "filtered_title",
                "pages", "pages",
                "title", "title",
                "author", "author",
                "generation", "generation",
                "resolved", "resolved"
        ));
        initPath("minecraft:bucket_entity_data", Map.of(
                "NoAI", "NoAI",
                "Silent", "Silent",
                "NoGravity", "NoGravity",
                "Glowing", "Glowing",
                "Invulnerable", "Invulnerable",
                "Health", "Health",
                "Age", "Age",
                "Variant", "Variant",
                "HuntingCooldown", "HuntingCooldown",
                "BucketVariantTag", "BucketVariantTag"
        ));
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
        // -- Legacy
        //initPath("minecraft:damage", "Damage");
        //initPath("minecraft:enchantments", "tag", "ench");

        initPath("minecraft:damage", "tag", "Damage");
        initPath("minecraft:enchantments", "tag", "Enchantments");
        initPath("minecraft:custom_data", "tag");

        // --- Not exist in old versions
        // - 24w09a
        initPath("minecraft:creative_slot_lock", "tag", "components", "minecraft:creative_slot_lock");
        initPath("minecraft:intangible_projectile", "tag", "components", "minecraft:intangible_projectile");
        initPath("minecraft:enchantment_glint_override", "tag", "components", "minecraft:enchantment_glint_override");
        initPath("minecraft:map_post_processing", "tag", "components", "minecraft:map_post_processing");
        // - 24w12a
        initPath("minecraft:food", "tag", "components", "minecraft:food");
        initPath("minecraft:max_stack_size", "tag", "components", "minecraft:max_stack_size");
        initPath("minecraft:max_damage", "tag", "components", "minecraft:max_damage");
        initPath("minecraft:fire_resistant", "tag", "components", "minecraft:fire_resistant");
        initPath("minecraft:rarity", "tag", "components", "minecraft:rarity");
        initPath("minecraft:tool", "tag", "components", "minecraft:tool");
        initPath("minecraft:hide_tooltip", "tag", "components", "minecraft:hide_tooltip");
        // - 24w13a
        initPath("minecraft:item_name", "tag", "components", "minecraft:item_name");
        initPath("minecraft:ominous_bottle_amplifier", "tag", "components", "minecraft:ominous_bottle_amplifier");
        // - 24w21a
        initPath("minecraft:jukebox_playable", "tag", "components", "minecraft:jukebox_playable");
        // --- Not supported
        // minecraft:hide_additional_tooltip = Same has 6th bit from tag.HideFlags
    }

    private static void initPath(String name, Object... path) {
        COMPONENT_PATHS.put(name, path);
        loadTagPath(path, new Object[] { name });
    }

    private static void initPath(String name, Map<String, Object> aliases) {
        initPath(name, new Object[] { "tag" }, aliases);
    }

    private static void initPath(String name, Object[] path, Map<String, Object> aliases) {
        COMPONENT_PATHS.put(name, loadComponentPath(new Object[] { name }, path, aliases));
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
        Map<String, Object> map = TAG_PATHS;
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
        if (map.get(key) instanceof Map) {
            ((Map<String, Object>) map.get(key)).put(ROOT_PATH, appendFirst(componentPath, "components"));
        } else {
            map.put(key, appendFirst(componentPath, "components"));
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
