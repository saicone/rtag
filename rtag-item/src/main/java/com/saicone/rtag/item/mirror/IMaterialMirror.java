package com.saicone.rtag.item.mirror;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.saicone.rtag.Rtag;
import com.saicone.rtag.item.ItemMirror;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.util.ItemMaterialTag;
import com.saicone.rtag.util.MC;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    private final Cache<ItemMaterialTag.Data, ItemMaterialTag.Data> cache;
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
    public IMaterialMirror(long duration, @NotNull TimeUnit unit, @NotNull String defaultMaterial) {
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
    public void upgrade(@NotNull Object compound, @NotNull String id, @NotNull MC from, @NotNull MC to) {
        resolveMaterial(compound, id, getDamage(compound, null, from), null, from, to);
    }

    @Override
    public void upgrade(@NotNull Object compound, @NotNull String id, @NotNull Object components, @NotNull MC from, @NotNull MC to) {
        resolveSaved(compound, id, getDamage(compound, components, from), components, from, to);
    }

    @Override
    public void downgrade(@NotNull Object compound, @NotNull String id, @NotNull MC from, @NotNull MC to) {
        // Compatibility with IPotionMirror
        if (from.isNewerThanOrEquals(MC.V_1_9) && to.isOlderThan(MC.V_1_9) && (id.equals("minecraft:potion") || id.equals("minecraft:splash_potion"))) {
            return;
        }
        resolveMaterial(compound, id, getDamage(compound, null, from), null, from, to);
    }

    @Override
    public void downgrade(@NotNull Object compound, @NotNull String id, @NotNull Object components, @NotNull MC from, @NotNull MC to) {
        // Compatibility with IPotionMirror
        if (from.isNewerThanOrEquals(MC.V_1_9) && to.isOlderThan(MC.V_1_9) && (id.equals("minecraft:potion") || id.equals("minecraft:splash_potion"))) {
            return;
        }
        resolveSaved(compound, id, getDamage(compound, components, from), components, from, to);
    }

    /**
     * Resolve an ItemStack if it contains "savedID" inside custom data component.
     *
     * @param compound   the tag compound that represent item data.
     * @param id         the id of the item.
     * @param damage     the item damage amount.
     * @param components the item components, on older versions this can be item tag.
     * @param from       the original version from item.
     * @param to         the version to convert item.
     */
    @ApiStatus.Internal
    public void resolveSaved(@NotNull Object compound, @NotNull String id, int damage, @NotNull Object components, @NotNull MC from, @NotNull MC to) {
        // Check if item contains previously saved ID
        final ItemMaterialTag.Data savedID = getSavedId(components, from, to);
        if (!savedID.isEmpty()) {
            // Check if saved ID is supported by the current version
            ItemMaterialTag.Data material = translate(savedID, from, to);
            if (material.isEmpty()) {
                final Short dmg = savedID.damage();
                if (dmg != null) {
                    material = translate(savedID.withDamage(null), from, to);
                    if (!material.isEmpty() && from.isNewerThanOrEquals(MC.V_1_13) && to.isNewerThanOrEquals(MC.V_1_13)) {
                        material = material.withDamage(dmg);
                    }
                }
            }
            if (!material.isEmpty()) {
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
    @ApiStatus.Internal
    public void resolveMaterial(@NotNull Object compound, @NotNull String id, int damage, @Nullable Object components, @NotNull MC from, @NotNull MC to) {
        final ItemMaterialTag.Data material;
        // Check if item is an egg with separated tag for entity type (1.9 - 1.12.2)
        final boolean isEgg = (from.isOlderThan(MC.V_1_13) && from.isNewerThanOrEquals(MC.V_1_9)) && id.equalsIgnoreCase("minecraft:spawn_egg");
        final boolean saveDamage = from.isNewerThanOrEquals(MC.V_1_13) || DAMAGEABLE.contains(id.toLowerCase());
        if (isEgg) {
            material = new ItemMaterialTag.Data(id, null, getEggEntity(compound, from));
        } else if (!saveDamage && damage > 0) {
            material = new ItemMaterialTag.Data(id, (short) damage, null);
        } else {
            material = new ItemMaterialTag.Data(id, null, null);
        }

        // Try to translate material
        final ItemMaterialTag.Data newMaterial = translate(material, from, to);
        if (!material.equals(newMaterial)) {
            // Remove entity type if the current version don't use separated to for entity type (old - 1.8.9 | 1.13 - latest)
            if (isEgg && (to.isNewerThanOrEquals(MC.V_1_13) || to.isOlderThan(MC.V_1_9))) {
                TagCompound.remove(compound, "EntityTag");
            }
            // Check if the material cannot be translated and save ID for future conversion
            if (newMaterial.isEmpty()) {
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
    @ApiStatus.Internal
    public void resolveItem(@NotNull Object compound, @NotNull ItemMaterialTag.Data material, @Nullable Object components, @NotNull MC from, @NotNull MC to) {
        if (material.entity() != null) {
            Rtag.INSTANCE.set(compound, material.entity(), "EntityTag", "id");
        } else {
            setDamage(compound, components, material.damage() != null ? material.damage().intValue() : 0, from, to);
        }
        TagCompound.set(compound, "id", TagBase.newTag(material.id()));
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
    @ApiStatus.Internal
    public void setDamage(@NotNull Object compound, @Nullable Object components, int damage, @NotNull MC from, @NotNull MC to) {
        if (from.isNewerThanOrEquals(MC.V_1_20_5) && to.isNewerThanOrEquals(MC.V_1_20_5)) {
            TagCompound.set(components, "minecraft:damage", TagBase.newTag(damage));
        } else if (to.isNewerThanOrEquals(MC.V_1_13)) {
            if (from.isOlderThan(MC.V_1_13)) {
                TagCompound.remove(compound, "Damage");
            }
            Rtag.INSTANCE.set(compound, damage, "tag", "Damage");
        } else {
            if (components != null && from.isNewerThanOrEquals(MC.V_1_13)) {
                TagCompound.remove(components, "Damage");
            }
            TagCompound.set(compound, "Damage", TagBase.newTag((short) damage));
        }
    }

    private static void setSavedId(@NotNull Object compound, @Nullable ItemMaterialTag.Data savedID, @NotNull MC from, @NotNull MC to) {
        // Use Rtag, is more easy
        if (from.isNewerThanOrEquals(MC.V_1_20_5) && to.isNewerThanOrEquals(MC.V_1_20_5)) {
            Rtag.INSTANCE.set(compound, savedID == null ? null : savedID.toString(), "components", "minecraft:custom_data", SAVED_ID);
        } else {
            Rtag.INSTANCE.set(compound, savedID == null ? null : savedID.toString(), "tag", SAVED_ID);
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
    @ApiStatus.Internal
    public int getDamage(@NotNull Object compound, @Nullable Object components, @NotNull MC version) {
        Object damage;
        if (version.isNewerThanOrEquals(MC.V_1_20_5)) {
            damage = components == null ? null : TagCompound.get(components, "minecraft:damage");
        } else if (version.isNewerThanOrEquals(MC.V_1_13)) {
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

    @NotNull
    private static ItemMaterialTag.Data getSavedId(@NotNull Object components, @NotNull MC from, @NotNull MC to) {
        final Object savedId;
        if (from.isNewerThanOrEquals(MC.V_1_20_5) && to.isNewerThanOrEquals(MC.V_1_20_5)) {
            savedId = Rtag.INSTANCE.getExact(components, "minecraft:custom_data", SAVED_ID);
        } else {
            savedId = TagCompound.get(components, SAVED_ID);
        }
        return savedId == null ? ItemMaterialTag.Data.empty() : ItemMaterialTag.Data.valueOf((String) TagBase.getValue(savedId));
    }

    /**
     * Get current item entity, method for legacy SPAWN_EGG items.
     *
     * @param compound Item NBTTagCompound.
     * @param version  Item version
     * @return         A string representing entity id.
     */
    @NotNull
    @ApiStatus.Internal
    public String getEggEntity(@NotNull Object compound, @NotNull MC version) {
        String entity = Rtag.INSTANCE.get(compound, "EntityTag", "id");
        if (entity != null) {
            return entity;
        }
        // Return another entity to avoid blank SPAWN_EGG
        if (version.isNewerThanOrEquals(MC.V_1_12)) {
            return "pig";
        } else if (version.isNewerThanOrEquals(MC.V_1_11)) {
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
    @NotNull
    @ApiStatus.Internal
    public ItemMaterialTag.Data translate(@NotNull ItemMaterialTag.Data material, @NotNull MC from, @NotNull MC to) {
        ItemMaterialTag.Data mat = cache.getIfPresent(material);
        if (mat == null) {
            if (ItemMaterialTag.SERVER_VALUES.containsKey(material.id())) {
                cache.put(material, material);
            } else {
                compute(material, from, to);
            }
            mat = cache.getIfPresent(material);
        }
        return mat;
    }

    private void compute(@NotNull ItemMaterialTag.Data material, @NotNull MC from, @NotNull MC to) {
        for (ItemMaterialTag tag : ItemMaterialTag.SERVER_VALUES.values()) {
            final TreeMap<MC, ItemMaterialTag.Data> dataMap = tag.getDataMap();
            for (MC version : dataMap.descendingKeySet()) {
                if (version.isOlderThanOrEquals(from)) {
                    final ItemMaterialTag.Data data = dataMap.get(version);
                    if (data.equals(material)) {
                        final var entry = dataMap.floorEntry(to);
                        if (entry == null) {
                            cache.put(material, ItemMaterialTag.Data.empty());
                        } else {
                            cache.put(material, entry.getValue());
                        }
                        return;
                    }
                }
            }
        }
        cache.put(material, ItemMaterialTag.Data.empty());
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
    @Deprecated(since = "1.5.14", forRemoval = true)
    public void resolveSaved(Object compound, String id, int damage, Object components, float from, float to) {
        resolveSaved(compound, id, damage, components, MC.findReverse(MC::featRevision, from), MC.findReverse(MC::featRevision, to));
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
    @Deprecated(since = "1.5.14", forRemoval = true)
    public void resolveMaterial(Object compound, String id, int damage, Object components, float from, float to) {
        resolveMaterial(compound, id, damage, components, MC.findReverse(MC::featRevision, from), MC.findReverse(MC::featRevision, to));
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
    @Deprecated(since = "1.5.14", forRemoval = true)
    public void resolveItem(Object compound, String material, Object components, float from, float to) {
        resolveItem(compound, ItemMaterialTag.Data.valueOf(material), components, MC.findReverse(MC::featRevision, from), MC.findReverse(MC::featRevision, to));
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
    @Deprecated(since = "1.5.14", forRemoval = true)
    public void setDamage(Object compound, Object components, int damage, float from, float to) {
        setDamage(compound, components, damage, MC.findReverse(MC::featRevision, from), MC.findReverse(MC::featRevision, to));
    }

    /**
     * Get current item damage depending on item version.
     *
     * @param compound   Item NBTTagCompound.
     * @param components Item components.
     * @param version    Version of the item.
     * @return           A integer representing item damage.
     */
    @Deprecated(since = "1.5.14", forRemoval = true)
    public int getDamage(Object compound, Object components, float version) {
        return getDamage(compound, components, MC.findReverse(MC::featRevision, version));
    }

    /**
     * Get current item entity, method for legacy SPAWN_EGG items.
     *
     * @param compound Item NBTTagCompound.
     * @param version  Item version
     * @return         A string representing entity id.
     */
    @Deprecated(since = "1.5.14", forRemoval = true)
    public String getEggEntity(Object compound, float version) {
        return getEggEntity(compound, MC.findReverse(MC::featRevision, version));
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
    @Deprecated(since = "1.5.14", forRemoval = true)
    public String translate(String material, float from, float to) {
        return translate(ItemMaterialTag.Data.valueOf(material), MC.findReverse(MC::featRevision, from), MC.findReverse(MC::featRevision, to)).id();
    }
}
