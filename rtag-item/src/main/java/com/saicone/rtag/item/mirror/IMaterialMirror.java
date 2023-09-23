package com.saicone.rtag.item.mirror;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.saicone.rtag.Rtag;
import com.saicone.rtag.item.ItemMirror;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.util.ItemMaterialTag;

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

    private final Cache<String, String> cache;
    private final Object defaultMaterial;

    /**
     * Constructs an simple IMaterialMirror with cache duration of 3 hours.
     */
    public IMaterialMirror() {
        this(3, TimeUnit.HOURS, "minecraft:paper");
    }

    /**
     * Constructs an IMaterialMirror with specified paramaters.
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
    public void upgrade(Object compound, String id, Object tag, float from, float to) {
        resolveSaved(compound, id, getDamage(compound, tag, from), tag, from, to);
    }

    @Override
    public void downgrade(Object compound, String id, float from, float to) {
        // Compatibility with IPotionMirror
        if (from >= 9f && to < 9f && id.equals("minecraft:potion")) {
            return;
        }
        resolveMaterial(compound, id, getDamage(compound, null, from), null, from, to);
    }

    @Override
    public void downgrade(Object compound, String id, Object tag, float from, float to) {
        // Compatibility with IPotionMirror
        if (from >= 9f && to < 9f && id.equals("minecraft:potion")) {
            return;
        }
        resolveSaved(compound, id, getDamage(compound, tag, from), tag, from, to);
    }

    /**
     * Resolve an ItemStack in case of contains "savedID" inside tag.
     *
     * @param compound Item NBTTagCompound.
     * @param id       ID of the item.
     * @param damage   Damage amount.
     * @param tag      Item tag.
     * @param from     Version specified in compound.
     * @param to       Version to convert.
     */
    public void resolveSaved(Object compound, String id, int damage, Object tag, float from, float to) {
        // Check if item contains previously saved ID
        final String savedID = (String) TagBase.getValue(TagCompound.get(tag, "savedID"));
        if (savedID != null) {
            // Check if saved ID is supported by the current version
            String material = translate(savedID, from, to);
            if (!material.equals("null")) {
                resolveItem(compound, material, tag, from, to);
            }
        } else {
            resolveMaterial(compound, id, damage, tag, from, to);
        }
    }

    /**
     * Resolve material of the item, this method checks if the ID needs
     * to be converted.
     *
     * @param compound Item NBTTagCompound.
     * @param id       ID of the item.
     * @param damage   Damage amount.
     * @param tag      Item tag.
     * @param from     Version specified in compound.
     * @param to       Version to convert.
     */
    public void resolveMaterial(Object compound, String id, int damage, Object tag, float from, float to) {
        final String material;
        // Check if item is an egg with separated tag for entity type (1.9 - 1.12.2)
        final boolean isEgg = (from < 13f && from >= 9f) && id.equalsIgnoreCase("minecraft:spawn_egg");
        if (isEgg) {
            material = id + "=" + getEggEntity(compound, from);
        } else {
            material = id + (damage > 0 ? ":" + damage : "");
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
                // Use Rtag, is more easy
                Rtag.INSTANCE.set(compound, material, "tag", "savedID");
                setDamage(compound, tag, 0, from, to);
            } else {
                resolveItem(compound, newMaterial, tag, from, to);
            }
        }
    }

    /**
     * Resolver current item compound with new material to set.
     *
     * @param compound Item NBTTagCompound.
     * @param material Material to set.
     * @param tag      Item tag.
     * @param from     Version specified in compound.
     * @param to       Version to convert.
     */
    public void resolveItem(Object compound, String material, Object tag, float from, float to) {
        final String[] split;
        if (material.startsWith("spawn_egg=")) {
            split = material.split("=", 2);
            Rtag.INSTANCE.set(compound, split[1], "EntityTag", "id");
        } else {
            split = material.split(":", 2);
            setDamage(compound, tag, split.length > 1 ? Integer.parseInt(split[1]) : 0, from, to);
        }
        TagCompound.set(compound, "id", TagBase.newTag("minecraft:" + split[0]));
    }

    /**
     * Set item damage depending on item version, this
     * method removes old damage tag if the conversion
     * is across legacy-flat.
     *
     * @param compound Item NBTTagCompound.
     * @param tag      Item tag.
     * @param damage   Damage amount to set.
     * @param from     Version specified in compound
     * @param to       Version to convert.
     */
    public void setDamage(Object compound, Object tag, int damage, float from, float to) {
        if (to >= 13f) {
            if (from < 13f) {
                TagCompound.remove(compound, "Damage");
            }
            Rtag.INSTANCE.set(compound, damage, "tag", "Damage");
        } else {
            if (tag != null && from >= 13f) {
                TagCompound.remove(tag, "Damage");
            }
            TagCompound.set(compound, "Damage", TagBase.newTag((short) damage));
        }
    }

    /**
     * Get current item damage depending on item version.
     *
     * @param compound Item NBTTagCompound.
     * @param tag      Item tag.
     * @param version  Version of the item.
     * @return         A integer representing item damage.
     */
    public int getDamage(Object compound, Object tag, float version) {
        Object damage = null;
        // On legacy versions "Damage" is outside tag
        if (version < 13f) {
            damage = TagCompound.get(compound, "Damage");
        } else if (tag != null) {
            damage = TagCompound.get(tag, "Damage");
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
                            cache.put(key, ItemMaterialTag.changeNameCase(entry.getValue(), false));
                        }
                        return;
                    }
                }
            }
        }
        cache.put(key, "null");
    }
}
