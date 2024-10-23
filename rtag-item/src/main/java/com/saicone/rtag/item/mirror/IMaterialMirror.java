package com.saicone.rtag.item.mirror;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.saicone.rtag.Rtag;
import com.saicone.rtag.item.ItemMirror;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.util.ItemMaterialTag;

import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * IMaterialMirror to convert item material IDs
 * across version.<br>
 * This is probably the most complex mirror instance
 * in Rtag.
 *
 * @author Rubenicos
 *
 * @see ItemMaterialTag
 */
public class IMaterialMirror implements ItemMirror {

    private static final String SAVED_ID = "savedID";
    private static final Set<String> DAMAGEABLE = Set.of(
            "minecraft:bow",
            "minecraft:carrot_on_a_stick",
            "minecraft:chainmail_boots",
            "minecraft:chainmail_chestplate",
            "minecraft:chainmail_helmet",
            "minecraft:chainmail_leggings",
            "minecraft:diamond_axe",
            "minecraft:diamond_boots",
            "minecraft:diamond_chestplate",
            "minecraft:diamond_helmet",
            "minecraft:diamond_hoe",
            "minecraft:diamond_leggings",
            "minecraft:diamond_pickaxe",
            "minecraft:diamond_shovel",
            "minecraft:diamond_sword",
            "minecraft:elytra",
            "minecraft:fishing_rod",
            "minecraft:flint_and_steel",
            "minecraft:golden_axe",
            "minecraft:golden_boots",
            "minecraft:golden_chestplate",
            "minecraft:golden_helmet",
            "minecraft:golden_hoe",
            "minecraft:golden_leggings",
            "minecraft:golden_pickaxe",
            "minecraft:golden_shovel",
            "minecraft:golden_sword",
            "minecraft:iron_axe",
            "minecraft:iron_boots",
            "minecraft:iron_chestplate",
            "minecraft:iron_helmet",
            "minecraft:iron_hoe",
            "minecraft:iron_leggings",
            "minecraft:iron_pickaxe",
            "minecraft:iron_shovel",
            "minecraft:iron_sword",
            "minecraft:leather_boots",
            "minecraft:leather_chestplate",
            "minecraft:leather_helmet",
            "minecraft:leather_leggings",
            "minecraft:shears",
            "minecraft:shield",
            "minecraft:stone_axe",
            "minecraft:stone_hoe",
            "minecraft:stone_pickaxe",
            "minecraft:stone_shovel",
            "minecraft:stone_sword",
            "minecraft:wooden_axe",
            "minecraft:wooden_hoe",
            "minecraft:wooden_pickaxe",
            "minecraft:wooden_shovel",
            "minecraft:wooden_sword"
    );

    private final Cache<String, String> cache;
    private final Object defaultMaterial;

    /**
     * Constructs an simple IMaterialMirror with cache duration of 3 hours.
     */
    public IMaterialMirror() {
        this(3, TimeUnit.HOURS, "minecraft:paper");
    }

    /**
     * Constructs an IMaterialMirror with specified parameters.
     *
     * @param duration        Cache duration.
     * @param unit            Time unit for cache.
     * @param defaultMaterial Default material for incompatible IDs.
     */
    public IMaterialMirror(long duration, TimeUnit unit, String defaultMaterial) {
        cache = CacheBuilder.newBuilder().expireAfterAccess(duration, unit).build();
        Object tag;
        try {
            tag = TagBase.newTag(ItemMaterialTag.SERVER_VALUES.containsKey(defaultMaterial) ? defaultMaterial : "minecraft:paper");
        } catch (Throwable t) {
            tag = null;
        }
        this.defaultMaterial = tag;
    }

    @Override
    public void upgrade(Object compound, String id, float from, float to) {
        resolveMaterial(compound, id, getDamage(compound, null, from), null, from, to);
    }

    @Override
    public void upgrade(Object compound, String id, Object components, float from, float to) {
        resolveSaved(compound, id, getDamage(compound, components, from), components, from, to);
    }

    @Override
    public void downgrade(Object compound, String id, float from, float to) {
        // Compatibility with IPotionMirror
        if (from >= 9f && to < 9f && (id.equals("minecraft:potion") || id.equals("minecraft:splash_potion"))) {
            return;
        }
        resolveMaterial(compound, id, getDamage(compound, null, from), null, from, to);
    }

    @Override
    public void downgrade(Object compound, String id, Object components, float from, float to) {
        // Compatibility with IPotionMirror
        if (from >= 9f && to < 9f && (id.equals("minecraft:potion") || id.equals("minecraft:splash_potion"))) {
            return;
        }
        resolveSaved(compound, id, getDamage(compound, components, from), components, from, to);
    }

    /**
     * Resolve an ItemStack if it contains "savedID" inside custom data component.
     *
     * @param compound   Item NBTTagCompound.
     * @param id         ID of the item.
     * @param damage     Damage amount.
     * @param components Item components.
     * @param from       Version specified in compound.
     * @param to         Version to convert.
     */
    public void resolveSaved(Object compound, String id, int damage, Object components, float from, float to) {
        // Check if item contains previously saved ID
        final String savedID = getSavedId(components, from, to);
        if (savedID != null) {
            // Check if saved ID is supported by the current version
            String material = translate(savedID, from, to);
            if (material.equals("null")) {
                final String[] split = savedID.split(":", 3);
                if (split.length > 2) {
                    material = translate(savedID.substring(0, savedID.lastIndexOf(':')), from, to);
                    if (!material.equals("null") && from >= 13f && to >= 13f) {
                        material = material + ":" + split[2];
                    }
                }
            }
            if (!material.equals("null")) {
                resolveItem(compound, material, components, from, to);
                setSavedId(compound, null, from, to);
            }
        } else {
            resolveMaterial(compound, id, damage, components, from, to);
        }
    }

    /**
     * Resolve material of the item, this method checks if the ID needs
     * to be converted.
     *
     * @param compound   Item NBTTagCompound.
     * @param id         ID of the item.
     * @param damage     Damage amount.
     * @param components Item components.
     * @param from       Version specified in compound.
     * @param to         Version to convert.
     */
    public void resolveMaterial(Object compound, String id, int damage, Object components, float from, float to) {
        final String material;
        // Check if item is an egg with separated tag for entity type (1.9 - 1.12.2)
        final boolean isEgg = (from < 13f && from >= 9f) && id.equalsIgnoreCase("minecraft:spawn_egg");
        final boolean saveDamage = from >= 13f || DAMAGEABLE.contains(id.toLowerCase());
        if (isEgg) {
            material = id + "=" + getEggEntity(compound, from);
        } else {
            material = id + (!saveDamage && damage > 0 ? ":" + damage : "");
        }

        // Try to translate material
        final String newMaterial = translate(material, from, to);
        if (!material.equals(newMaterial)) {
            // Remove entity type if the current version don't use separated to for entity type (old - 1.8.9 | 1.13 - latest)
            if (isEgg && (to >= 13f || to < 9f)) {
                TagCompound.remove(compound, "EntityTag");
            }
            // Check if the material cannot be translated and save ID for future conversion
            if (newMaterial.equals("null")) {
                TagCompound.set(compound, "id", defaultMaterial);
                setSavedId(compound, material, from, to);
                setDamage(compound, components, 0, from, to);
                return;
            } else {
                resolveItem(compound, newMaterial, components, from, to);
            }
        }
        if (saveDamage && damage > 0) {
            setDamage(compound, components, damage, from, to);
        }
    }

    /**
     * Resolver current item compound with new material to set.
     *
     * @param compound   Item NBTTagCompound.
     * @param material   Material to set.
     * @param components Item components.
     * @param from       Version specified in compound.
     * @param to         Version to convert.
     */
    public void resolveItem(Object compound, String material, Object components, float from, float to) {
        final String id;
        if (material.startsWith("minecraft:spawn_egg=")) {
            final String[] split = material.split("=", 2);
            id = split[0];
            Rtag.INSTANCE.set(compound, split[1], "EntityTag", "id");
        } else {
            final String[] split = material.split(":", 3);
            id = split[0] + ":" + split[1];
            try {
                setDamage(compound, components, split.length > 2 ? Integer.parseInt(split[2]) : 0, from, to);
            } catch (NumberFormatException ignored) { }
        }
        TagCompound.set(compound, "id", TagBase.newTag(id));
    }

    /**
     * Set item damage depending on item version, this
     * method removes old damage tag if the conversion
     * is across legacy-flat.
     *
     * @param compound   Item NBTTagCompound.
     * @param components Item components.
     * @param damage     Damage amount to set.
     * @param from       Version specified in compound
     * @param to         Version to convert.
     */
    public void setDamage(Object compound, Object components, int damage, float from, float to) {
        if (from >= 20.04f && to >= 20.04f) {
            TagCompound.set(components, "minecraft:damage", TagBase.newTag(damage));
        } else if (to >= 13f) {
            if (from < 13f) {
                TagCompound.remove(compound, "Damage");
            }
            Rtag.INSTANCE.set(compound, damage, "tag", "Damage");
        } else {
            if (components != null && from >= 13f) {
                TagCompound.remove(components, "Damage");
            }
            TagCompound.set(compound, "Damage", TagBase.newTag((short) damage));
        }
    }

    private static void setSavedId(Object compound, String savedID, float from, float to) {
        // Use Rtag, is more easy
        if (from >= 20.04f && to >= 20.04f) {
            Rtag.INSTANCE.set(compound, savedID, "components", "minecraft:custom_data", SAVED_ID);
        } else {
            Rtag.INSTANCE.set(compound, savedID, "tag", SAVED_ID);
        }
    }

    /**
     * Get current item damage depending on item version.
     *
     * @param compound   Item NBTTagCompound.
     * @param components Item components.
     * @param version    Version of the item.
     * @return           A integer representing item damage.
     */
    public int getDamage(Object compound, Object components, float version) {
        Object damage;
        if (version >= 20.04f) {
            damage = components == null ? null : TagCompound.get(components, "minecraft:damage");
        } else if (version >= 13f) {
            damage = components == null ? null : TagCompound.get(components, "Damage");
        } else {
            // On legacy versions "Damage" is outside tag
            damage = TagCompound.get(compound, "Damage");
        }
        if ((damage = TagBase.getValue(damage)) != null) {
            // Avoid any rare error
            if (damage instanceof Number) {
                return ((Number) damage).intValue();
            } else {
                // WTH happens with damage tag!?
                try {
                    return Integer.parseInt(String.valueOf(damage));
                } catch (NumberFormatException ignored) { }
            }
        }
        return 0;
    }

    private static String getSavedId(Object components, float from, float to) {
        final Object savedId;
        if (from >= 20.04f && to >= 20.04f) {
            savedId = Rtag.INSTANCE.getExact(components, "minecraft:custom_data", SAVED_ID);
        } else {
            savedId = TagCompound.get(components, SAVED_ID);
        }
        return (String) TagBase.getValue(savedId);
    }

    /**
     * Get current item entity, method for legacy SPAWN_EGG items.
     *
     * @param compound Item NBTTagCompound.
     * @param version  Item version
     * @return         A string representing entity id.
     */
    public String getEggEntity(Object compound, float version) {
        String entity = Rtag.INSTANCE.get(compound, "EntityTag", "id");
        if (entity != null) {
            return entity;
        }
        // Return another entity to avoid blank SPAWN_EGG
        if (version >= 12f) {
            return "pig";
        } else if (version >= 11f) {
            return "minecraft:pig";
        } else {
            return "Pig";
        }
    }

    /**
     * Translate given material and version pair into
     * current server version.
     *
     * @param material Material to translate.
     * @param from     Version specified in compound
     * @param to       Version to convert.
     * @return         A string representing current server version material.
     */
    public String translate(String material, float from, float to) {
        String mat = cache.getIfPresent(material);
        if (mat == null) {
            if (ItemMaterialTag.SERVER_VALUES.containsKey(material)) {
                cache.put(material, material);
            } else {
                compute(material, ItemMaterialTag.changeNameCase(material.replace("minecraft:", ""), true), from, to);
            }
            mat = cache.getIfPresent(material);
        }
        return mat;
    }

    private void compute(String key, String value, float from, float to) {
        for (ItemMaterialTag tag : ItemMaterialTag.SERVER_VALUES.values()) {
            final TreeMap<Float, String> names = tag.getNames();
            for (Float tagVersion : names.descendingKeySet()) {
                if (tagVersion <= from) {
                    final String tagName = names.get(tagVersion);
                    if (tagName.equals(value)) {
                        final var entry = names.floorEntry(to);
                        if (entry == null) {
                            cache.put(key, "null");
                        } else {
                            cache.put(key, "minecraft:" + ItemMaterialTag.changeNameCase(entry.getValue(), false));
                        }
                        return;
                    }
                }
            }
        }
        cache.put(key, "null");
    }
}
