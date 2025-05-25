package com.saicone.rtag.item;

import com.saicone.rtag.Rtag;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;
import com.saicone.rtag.util.ChatComponent;
import com.saicone.rtag.util.ItemMaterialTag;
import com.saicone.rtag.util.ServerInstance;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;

/**
 * Class to item-related data.
 *
 * @author Rubenicos
 */
public class ItemData {

    private static final String ROOT_PATH = "==root";

    // Paths
    private static final Map<String, Object> COMPONENT_PATHS = new LinkedHashMap<>();
    private static final Map<String, Object> TAG_PATHS = new LinkedHashMap<>();
    // Detectors
    private static final TreeMap<Float, Predicate<Map<String, Object>>> COMPONENT_DETECTORS = new TreeMap<>(Comparator.reverseOrder()) {
        @Override
        public Predicate<Map<String, Object>> put(Float key, Predicate<Map<String, Object>> value) {
            if (containsKey(key)) {
                final Predicate<Map<String, Object>> predicate = remove(key);
                return super.put(key, map -> predicate.test(map) || value.test(map));
            }
            return super.put(key, value);
        }
    };
    private static final TreeMap<Float, Predicate<Map<String, Object>>> TAG_DETECTORS = new TreeMap<>(Comparator.reverseOrder()) {
        @Override
        public Predicate<Map<String, Object>> put(Float key, Predicate<Map<String, Object>> value) {
            if (containsKey(key)) {
                final Predicate<Map<String, Object>> predicate = remove(key);
                return super.put(key, map -> predicate.test(map) || value.test(map));
            }
            return super.put(key, value);
        }
    };

    static {
        loadPaths();
        loadComponentDetectors();
        loadTagDetectors();
    }

    ItemData() {
    }

    /**
     * Convert old tag path into new component path.<br>
     * This method skips first key of tag path, it assumes the given path has "tag" at first element
     *
     * @param path the tag path to convert.
     * @return     a new component path representation of tag path.
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
     * @param path the component path to convert.
     * @return     an old tag path representation of component path.
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

    /**
     * Get current version number from item compound.
     *
     * @param compound NBTTagCompound that represent an item.
     * @return         A valid version number or null.
     */
    public static Float getItemVersion(Object compound) {
        if (compound == null) {
            return null;
        }
        final Map<String, Object> value = TagCompound.getValue(compound);
        if (value.isEmpty()) {
            return null;
        }
        Object providedVersion = TagBase.getValue(value.get("DataVersion"));
        if (providedVersion == null) {
            providedVersion = TagBase.getValue(value.get("v"));
        }

        if (providedVersion instanceof Number) {
            final int dataVersion = ((Number) providedVersion).intValue();
            final int release = ServerInstance.release(dataVersion);
            return Float.parseFloat(ServerInstance.verNumber(dataVersion) + "." + (release < 10 ? "0" : "") + release);
        }

        // Calculate the minimum version
        final Float detectedVersion = detectVersion(compound, value);

        // Get full item id
        String id = (String) TagBase.getValue(value.get("id"));
        if (id == null) {
            return null;
        }
        if (id.startsWith("minecraft:")) {
            id = id.substring(10);
        }
        if (detectedVersion != null && detectedVersion < 13f) {
            final int damage = Rtag.INSTANCE.getOptional(compound, "Damage").asInt(0);
            if (damage > 0) {
                id = id + ":" + damage;
            }
        }

        // Compare material version with detected version
        final Float materialVersion = findMaterialVersion(id, detectedVersion == null ? 8f : detectedVersion);
        final float finalVersion = Math.max(detectedVersion == null ? 0f : detectedVersion, materialVersion == null ? 0f : materialVersion);

        return finalVersion > 0 ? finalVersion : null;
    }

    private static Float detectVersion(Object compound, Map<String, Object> value) {
        // Detect by item components
        final Object components = value.get("components");
        if (components != null || TagBase.getValue(value.get("count")) instanceof Integer) {
            final Float version = detectComponentVersion(components);
            return version == null ? 20.04f : version;
        }

        // Detect by old tag value
        final Float version = detectTagVersion(value.get("tag"));
        if (version == null || version < 9.01f) {
            // Detect by old entity tag
            String entity = Rtag.INSTANCE.get(compound, "EntityTag", "id");
            if (entity != null) {
                if (entity.equals(entity.toLowerCase())) {
                    if (entity.startsWith("minecraft:")) {
                        return 11.01f;
                    } else {
                        return 12.01f;
                    }
                } else {
                    return 9.01f;
                }
            }
        }
        return version;
    }

    private static Float detectComponentVersion(Object compound) {
        if (compound == null) {
            return null;
        }
        Float result = null;
        final Map<String, Object> components = TagCompound.getValue(compound);
        if (components.containsKey("minecraft:bundle_contents")) {
            result = detectListVersion(components.get("minecraft:bundle_contents"), null);
        }
        if (components.containsKey("minecraft:container")) {
            result = maxVersion(result, detectListVersion(components.get("minecraft:container"), "item"));
        }
        if (components.containsKey("minecraft:charged_projectiles")) {
            result = maxVersion(result, detectListVersion(components.get("minecraft:charged_projectiles"), null));
        }
        if (components.containsKey("minecraft:use_remainder")) {
            result = maxVersion(result, getItemVersion(components.get("minecraft:use_remainder")));
        }
        return maxVersion(result, detectMapVersion(components, COMPONENT_DETECTORS));
    }

    private static Float detectTagVersion(Object compound) {
        if (compound == null) {
            return null;
        }
        Float result = null;
        final Map<String, Object> tag = TagCompound.getValue(compound);
        if (tag.containsKey("Items")) {
            result = detectListVersion(tag.get("Items"), null);
            // Bundles exists since 1.17
            if (result == null || result < 17.01f) {
                result = 17.01f;
            }
        }
        final Object blockEntityTag = tag.get("BlockEntityTag");
        if (TagCompound.isTagCompound(blockEntityTag)) {
            final Object items = TagCompound.get(blockEntityTag, "Items");
            result = maxVersion(result, detectListVersion(items, null));
            // BlockEntityTag was added on 1.9
            if (result == null || result < 9.01f) {
                result = 9.01f;
            }
        }
        return maxVersion(result, detectMapVersion(tag, TAG_DETECTORS));
    }

    private static Float detectListVersion(Object iterable, String key) {
        if (!TagList.isTagList(iterable)) {
            return null;
        }
        Float result = null;
        for (Object compound : TagList.getValue(iterable)) {
            final Float version;
            if (key == null) {
                version = getItemVersion(compound);
            } else {
                version = getItemVersion(TagCompound.get(compound, key));
            }
            result = maxVersion(result, version);
        }
        return result;
    }

    private static Float detectMapVersion(Map<String, Object> map, TreeMap<Float, Predicate<Map<String, Object>>> detectors) {
        for (var entry : detectors.entrySet()) {
            if (entry.getValue().test(map)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private static Float maxVersion(Float version1, Float version2) {
        if (version1 == null) {
            return version2;
        }
        if (version2 == null) {
            return version1;
        }
        return Math.max(version1, version2);
    }

    private static Float findMaterialVersion(String id, float minimumVersion) {
        final String mat = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
        for (ItemMaterialTag material : ItemMaterialTag.VALUES) {
            for (Map.Entry<Float, String> entry : material.getNames().entrySet()) {
                if (entry.getKey() < minimumVersion) {
                    continue;
                }
                if (entry.getValue().equalsIgnoreCase(mat)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    // Data initialization

    private static void loadPaths() {
        loadPath("minecraft:repair_cost", "tag", "RepairCost");
        loadPath("minecraft:unbreakable", "tag", "Unbreakable");
        loadPath("minecraft:stored_enchantments", "tag", "StoredEnchantments");
        loadPath("minecraft:custom_name", "tag", "display", "Name");
        loadPath("minecraft:lore", "tag", "display", "Lore");
        loadPath("minecraft:dyed_color", "tag", "display", "color");
        loadPath("minecraft:map_color", "tag", "display", "MapColor");
        loadPath("minecraft:map_decorations", "tag", "Decorations");
        loadPath("minecraft:map_id", "tag", "map");
        loadPath("minecraft:can_break", "tag", "CanDestroy");
        loadPath("minecraft:can_place_on", "tag", "CanPlaceOn");
        loadPath("minecraft:attribute_modifiers", "tag", "AttributeModifiers");
        loadPath("minecraft:charged_projectiles", "tag", "ChargedProjectiles");
        loadPath("minecraft:bundle_contents", "tag", "Items");
        loadPath("minecraft:custom_model_data", "tag", "CustomModelData");
        loadPath("minecraft:trim", "tag", "Trim");
        loadPath("minecraft:suspicious_stew_effects", "tag", "effects");
        loadPath("minecraft:debug_stick_state", "tag", "DebugProperty");
        loadPath("minecraft:entity_data", "tag", "EntityTag");
        loadPath("minecraft:instrument", "tag", "instrument");
        loadPath("minecraft:recipes", "tag", "Recipes");
        loadPath("minecraft:profile", new Object[] { "tag", "SkullOwner" }, Map.of(
                "name", "Name",
                "id", "Id",
                "properties", "Properties"
        ));
        loadPath("minecraft:note_block_sound", "tag", "BlockEntityTag", "note_block_sound");
        loadPath("minecraft:base_color", "tag", "BlockEntityTag", "Base");
        loadPath("minecraft:banner_patterns", "tag", "BlockEntityTag", "Patterns");
        loadPath("minecraft:pot_decorations", "tag", "BlockEntityTag", "sherds");
        loadPath("minecraft:container", "tag", "BlockEntityTag", "Items");
        loadPath("minecraft:bees", "tag", "BlockEntityTag", "Bees");
        loadPath("minecraft:lock", "tag", "BlockEntityTag", "Lock");
        loadPath("minecraft:container_loot", new Object[] { "tag", "BlockEntityTag" }, Map.of(
                "loot_table", "LootTable",
                "seed", "LootTableSeed"
        ));
        loadPath("minecraft:block_entity_data", "tag", "BlockEntityTag");
        loadPath("minecraft:block_state", "tag", "BlockStateTag");
        loadPath("minecraft:potion_contents", Map.of(
                "potion", "Potion",
                "custom_color", "CustomPotionColor",
                "custom_effects", "custom_potion_effects"
        ));
//        initPath("minecraft:writable_book_content", Map.of(
//                "filtered_pages", "filtered_pages",
//                "pages", "pages"
//        ));
        loadPath("minecraft:written_book_content", Map.of(
                "filtered_pages", "filtered_pages",
                "filtered_title", "filtered_title",
                "pages", "pages",
                "title", "title",
                "author", "author",
                "generation", "generation",
                "resolved", "resolved"
        ));
        loadPath("minecraft:bucket_entity_data", Map.of(
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
        loadPath("minecraft:lodestone_tracker", Map.of(
                "tracked", "LodestoneTracked",
                "target", Map.of(
                        "pos", "LodestonePos",
                        "dimension", "LodestoneDimension"
                )
        ));
        loadPath("minecraft:firework_explosion", new Object[] { "tag", "Explosion" }, Map.of(
                "shape", "Type",
                "colors", "Colors",
                "fade_colors", "FadeColors",
                "has_trail", "Trail",
                "has_twinkle", "Flicker"
        ));
        loadPath("minecraft:fireworks", new Object[] { "tag", "Fireworks" }, Map.of(
                "explosions", "Explosions",
                "flight_duration", "Flight"
        ));
        // -- Legacy
        //initPath("minecraft:damage", "Damage");
        //initPath("minecraft:enchantments", "tag", "ench");

        loadPath("minecraft:damage", "tag", "Damage");
        loadPath("minecraft:enchantments", "tag", "Enchantments");
        loadPath("minecraft:custom_data", "tag");

        // --- Not exist in old versions
        // 1.20.5 - 24w09a
        loadPath("minecraft:creative_slot_lock", "tag", "components", "minecraft:creative_slot_lock");
        loadPath("minecraft:intangible_projectile", "tag", "components", "minecraft:intangible_projectile");
        loadPath("minecraft:enchantment_glint_override", "tag", "components", "minecraft:enchantment_glint_override");
        loadPath("minecraft:map_post_processing", "tag", "components", "minecraft:map_post_processing");
        // 1.20.5 - 24w12a
        loadPath("minecraft:food", "tag", "components", "minecraft:food");
        loadPath("minecraft:max_stack_size", "tag", "components", "minecraft:max_stack_size");
        loadPath("minecraft:max_damage", "tag", "components", "minecraft:max_damage");
        loadPath("minecraft:fire_resistant", "tag", "components", "minecraft:fire_resistant"); // Renamed on 24w37a
        loadPath("minecraft:rarity", "tag", "components", "minecraft:rarity");
        loadPath("minecraft:tool", "tag", "components", "minecraft:tool");
        loadPath("minecraft:hide_tooltip", "tag", "components", "minecraft:hide_tooltip");
        // 1.20.5 - 24w13a
        loadPath("minecraft:item_name", "tag", "components", "minecraft:item_name");
        loadPath("minecraft:ominous_bottle_amplifier", "tag", "components", "minecraft:ominous_bottle_amplifier");
        // 1.21.1 - 24w21a
        loadPath("minecraft:jukebox_playable", "tag", "components", "minecraft:jukebox_playable");
        // 1.21.2 - 24w33a
        loadPath("minecraft:repairable", "tag", "components", "minecraft:repairable");
        loadPath("minecraft:enchantable", "tag", "components", "minecraft:enchantable");
        // 1.21.2 - 24w34a
        loadPath("minecraft:consumable", "tag", "components", "minecraft:consumable");
        loadPath("minecraft:use_cooldown", "tag", "components", "minecraft:use_cooldown");
        loadPath("minecraft:use_remainder", "tag", "components", "minecraft:use_remainder");
        // 1.21.2 - 24w36a
        loadPath("minecraft:item_model", "tag", "components", "minecraft:item_model");
        loadPath("minecraft:equippable", "tag", "components", "minecraft:equippable");
        loadPath("minecraft:glider", "tag", "components", "minecraft:glider");
        loadPath("minecraft:tooltip_style", "tag", "components", "minecraft:tooltip_style");
        // 1.21.2 - 24w37a
        loadPath("minecraft:death_protection", "tag", "components", "minecraft:death_protection");
        loadPath("minecraft:damage_resistant", "tag", "components", "minecraft:damage_resistant");
        // 1.21.5 - 25w02a
        loadPath("minecraft:weapon", "tag", "components", "minecraft:weapon");
        loadPath("minecraft:potion_duration_scale", "tag", "components", "minecraft:potion_duration_scale");
        // 1.21.5 - 25w04a
        loadPath("minecraft:blocks_attacks", "tag", "components", "minecraft:blocks_attacks");
        loadPath("minecraft:break_sound", "tag", "components", "minecraft:break_sound");
        loadPath("minecraft:provides_banner_patterns", "tag", "components", "minecraft:provides_banner_patterns");
        loadPath("minecraft:provides_trim_material", "tag", "components", "minecraft:provides_trim_material");
        loadPath("minecraft:tooltip_display", "tag", "components", "minecraft:tooltip_display");
        // --- Not supported
        // minecraft:hide_additional_tooltip = Same has 6th bit from tag.HideFlags
    }

    private static void loadPath(String name, Object... path) {
        COMPONENT_PATHS.put(name, path);
        loadTagPath(path, new Object[] { name });
    }

    private static void loadPath(String name, Map<String, Object> aliases) {
        loadPath(name, new Object[] { "tag" }, aliases);
    }

    private static void loadPath(String name, Object[] path, Map<String, Object> aliases) {
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

    private static void loadComponentDetector(float version, Predicate<Map<String, Object>> predicate) {
        COMPONENT_DETECTORS.put(version, predicate);
    }

    private static void loadComponentDetector(float version, String key, Predicate<Object> predicate) {
        loadComponentDetector(version, map -> {
            final Object value = map.get(key);
            if (value != null) {
                try {
                    return predicate.test(value);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            return false;
        });
    }

    private static void loadAttributeDetector(float version, Predicate<Map<String, Object>> predicate) {
        loadComponentDetector(version, map -> {
            final Object attributeModifiers = map.get("minecraft:attribute_modifiers");
            if (attributeModifiers == null) {
                return false;
            }
            final Object modifiers = TagCompound.get(attributeModifiers, "modifiers");
            if (modifiers == null) {
                return false;
            }
            for (Object modifier : TagList.getValue(modifiers)) {
                if (predicate.test(TagCompound.getValue(modifier))) {
                    return true;
                }
            }
            return false;
        });
    }

    private static void loadTextDetector(float version, Predicate<Object> predicate, String[]... paths) {
        loadComponentDetector(version, map -> {
            for (String[] path : paths) {
                Map<String, Object> parent = map;
                for (int i = 0; i < path.length; i++) {
                    final String key = path[i];
                    final Object value = parent.get(key);
                    if (i + 1 >= path.length) {
                        if (value != null) {
                            if (TagList.isTagList(value)) {
                                for (Object line : TagList.getValue(value)) {
                                    if (predicate.test(line)) {
                                        return true;
                                    }
                                }
                            } else if (predicate.test(parent.get(key))) {
                                return true;
                            }
                        }
                    } else {
                        if (TagCompound.isTagCompound(value)) {
                            parent = TagCompound.getValue(value);
                        }
                    }
                }
            }
            return false;
        });
    }

    private static void loadComponentDetectors() {
        // 1.21.5
        loadComponentDetector(21.04f, components ->
                components.containsKey("minecraft:weapon")
                        || components.containsKey("minecraft:potion_duration_scale")
                        || components.containsKey("minecraft:blocks_attacks")
                        || components.containsKey("minecraft:break_sound")
                        || components.containsKey("minecraft:provides_banner_patterns")
                        || components.containsKey("minecraft:provides_trim_material")
                        || components.containsKey("minecraft:tooltip_display")
        );
        loadComponentDetector(21.04f, "minecraft:tool", tool -> TagCompound.get(tool, "can_destroy_blocks_in_creative") != null);
        loadComponentDetector(21.04f, "minecraft:equippable", equippable -> TagCompound.get(equippable, "equip_on_interact") != null);
        loadComponentDetector(21.04f, "minecraft:enchantments", enchantments -> {
            final Map<String, Object> value = TagCompound.getValue(enchantments);
            return !value.isEmpty() && !value.containsKey("levels") && !value.containsKey("show_in_tooltip");
        });
        final Predicate<Object> canBuild = component -> {
            if (TagList.isTagList(component)) {
                return true;
            }
            final Map<String, Object> value = TagCompound.getValue(component);
            return !value.isEmpty() && !value.containsKey("predicates") && !value.containsKey("show_in_tooltip");
        };
        loadComponentDetector(21.04f, "minecraft:can_break", canBuild);
        loadComponentDetector(21.04f, "minecraft:can_place_on", canBuild);
        loadComponentDetector(21.04f, "minecraft:dyed_color", dyedColor -> !TagCompound.isTagCompound(dyedColor));
        loadComponentDetector(21.04f, "minecraft:attribute_modifiers", TagList::isTagList);
        loadTextDetector(21.04f, TagCompound::isTagCompound,
                new String[] {"minecraft:custom_name"},
                new String[] {"minecraft:item_name"},
                new String[] {"minecraft:lore"},
                new String[] {"minecraft:written_book_content", "pages"},
                new String[] {"minecraft:instrument", "description"}
        );
        // 1.21.4
        loadComponentDetector(21.03f, "minecraft:custom_model_data", TagCompound::isTagCompound);
        loadComponentDetector(21.03f, "minecraft:equippable", equippable -> TagCompound.get(equippable, "asset_id") != null);
        // 1.21.2
        loadComponentDetector(21.02f, components ->
                components.containsKey("minecraft:repairable")
                        || components.containsKey("minecraft:enchantable")
                        || components.containsKey("minecraft:consumable")
                        || components.containsKey("minecraft:use_cooldown")
                        || components.containsKey("minecraft:use_remainder")
                        || components.containsKey("minecraft:item_model")
                        || components.containsKey("minecraft:equippable")
                        || components.containsKey("minecraft:glider")
                        || components.containsKey("minecraft:tooltip_style")
                        || components.containsKey("minecraft:death_protection")
                        || components.containsKey("minecraft:damage_resistant")
        );
        loadAttributeDetector(21.02f, modifier -> {
            final String type = (String) TagBase.getValue(modifier.get("type"));
            return !type.startsWith("generic.") && !type.startsWith("player.") && !type.startsWith("zombie.");
        });
        loadComponentDetector(21.02f, "minecraft:potion_contents", potionContents -> TagCompound.get(potionContents, "custom_name") != null);
        // 1.21.1
        loadComponentDetector(21.01f, components -> components.containsKey("minecraft:jukebox_playable"));
        loadComponentDetector(21.01f, "minecraft:food", food -> TagCompound.get(food, "using_converts_to") != null);
        loadAttributeDetector(21.01f, modifier -> modifier.get("id") != null);
    }

    private static void loadTagDetectors() {
        TAG_DETECTORS.put(20.02f, tag ->
                tag.containsKey("custom_potion_effects")
                        || tag.containsKey("effects")
        );
        TAG_DETECTORS.put(19.03f, tag -> hasHideFlag(tag, 128));
        TAG_DETECTORS.put(16.02f, tag -> hasHideFlag(tag, 64));
        TAG_DETECTORS.put(16.01f, tag ->
                tag.containsKey("SkullOwner")
                        && TagBase.getValue(TagCompound.get(tag.get("SkullOwner"), "Id")) instanceof int[]
        );
        TAG_DETECTORS.put(14.01f, tag -> {
            if (tag.containsKey("CustomModelData") || tag.containsKey("BlockStateTag")) {
                return true;
            }
            if (tag.containsKey("display")) {
                final Object lore = TagCompound.get(tag.get("display"), "Lore");
                if (lore != null) {
                    for (Object line : TagList.getValue(lore)) {
                        if (!ChatComponent.isChatComponent(TagBase.getValue(line))) {
                            return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        });
        TAG_DETECTORS.put(13.01f, tag -> {
            if (tag.containsKey("Damage") || tag.containsKey("Enchantments")) {
                return true;
            }
            if (tag.containsKey("StoredEnchantments")) {
                final Object storedEnchantments = tag.get("StoredEnchantments");
                for (Object entry : TagList.getValue(storedEnchantments)) {
                    final Object id = TagBase.getValue(TagCompound.get(entry, "id"));
                    if (id instanceof String) {
                        return true;
                    }
                }
            }
            if (tag.containsKey("display")) {
                final Object name = TagCompound.get(tag.get("display"), "Name");
                if (name != null) {
                    return ChatComponent.isChatComponent(TagBase.getValue(name));
                }
            }
            return false;
        });
        TAG_DETECTORS.put(11.01f, tag -> hasEnchantment(tag, 10, 22, 49, 71));
        TAG_DETECTORS.put(9.01f, tag -> tag.containsKey("Potion") || hasEnchantment(tag, 9, 70));
        TAG_DETECTORS.put(8.01f, tag ->
                tag.containsKey("CanDestroy")
                        || tag.containsKey("HideFlags")
                        || tag.containsKey("BlockEntityTag")
        );
        TAG_DETECTORS.put(7.01f, tag -> tag.containsKey("Unbreakable"));
    }

    private static boolean hasHideFlag(Map<String, Object> tag, int flag) {
        if (tag.containsKey("HideFlags")) {
            final int flags = (int) TagBase.getValue(tag.get("HideFlags"));
            if ((flags & flag) != flag) {
                return false;
            }
            return flags > 0;
        }
        return false;
    }

    private static boolean hasEnchantment(Map<String, Object> tag, int... enchants) {
        final Object enchantments = tag.getOrDefault("ench", tag.get("StoredEnchantments"));
        if (enchantments != null) {
            for (Object entry : TagList.getValue(enchantments)) {
                final short id = (short) TagBase.getValue(TagCompound.get(entry, "id"));
                for (int enchant : enchants) {
                    if (id == enchant) {
                        return true;
                    }
                }
            }
        }
        return false;
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
