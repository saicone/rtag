package com.saicone.rtag.item.mirror;

import com.saicone.rtag.Rtag;
import com.saicone.rtag.item.ItemMirror;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * IPotionMirror to convert item potions
 * across 1.8 and other versions.<br>
 * Mojang removes potion types inside item
 * damage since 1.9, this class provides an
 * clear conversion.
 *
 * @author Rubenicos
 */
public class IPotionMirror implements ItemMirror {

    private static final Map<Object, Object> cache = new HashMap<>();
    private static final Object POTION;
    private static final Object SPLASH_POTION;
    private static final Set<String> POTION_ITEMS = Set.of("minecraft:potion", "minecraft:lingering_potion", "minecraft:splash_potion", "minecraft:tipped_arrow");

    static {
        Object potion = null;
        Object splash = null;
        try {
            potion = TagBase.newTag("minecraft:potion");
            splash = TagBase.newTag("minecraft:splash_potion");
        } catch (Throwable t) {
            t.printStackTrace();
        }
        POTION = potion;
        SPLASH_POTION = splash;
    }

    @Override
    public void upgrade(Object compound, String id, Object tag, float from, float to) {
        if (from <= 20.01f && POTION_ITEMS.contains(id)) {
            final Map<String, Object> map = TagCompound.getValue(tag);
            if (map.containsKey("CustomPotionEffects")) {
                final Object customPotionEffects = map.remove("CustomPotionEffects");
                map.put("custom_potion_effects", customPotionEffects);
            }
            upgrade(compound, id, from, to);
        }
    }

    @Override
    public void upgrade(Object compound, String id, float from, float to) {
        if (to >= 9f && from < 9f && id.equals("minecraft:potion")) {
            upgrade(compound);
        }
    }

    private void upgrade(Object compound) {
        Short damage = (Short) TagBase.getValue(TagCompound.get(compound, "Damage"));
        if (damage == null || damage == 0) return;

        String potion = getPotion(damage);
        if (potion != null) {
            TagCompound.set(compound, "id", damage > 16000 ? SPLASH_POTION : POTION);
            TagCompound.set(compound, "Damage", TagBase.newTag((short) 0));
            Rtag.INSTANCE.set(compound, TagBase.newTag(potion), "tag", "Potion");
        }
    }

    @Override
    public void downgrade(Object compound, String id, Object tag, float from, float to) {
        if (to <= 20.01f && POTION_ITEMS.contains(id)) {
            final Map<String, Object> map = TagCompound.getValue(tag);
            if (map.containsKey("custom_potion_effects")) {
                final Object customPotionEffects = map.remove("custom_potion_effects");
                map.put("CustomPotionEffects", customPotionEffects);
            }
            if (from >= 9f && to < 9f && (id.equals("minecraft:potion") || id.equals("minecraft:splash_potion"))) {
                String potion = (String) TagBase.getValue(map.get("Potion"));
                if (potion == null || potion.equals("empty") || potion.equals("water")) return;

                short damage = (short) getDamage(id, potion);
                if (damage > 0) {
                    TagCompound.set(compound, "id", POTION);
                    TagCompound.set(compound, "Damage", TagBase.newTag(damage));
                    map.remove("Potion");
                }
            }
        }
    }

    /**
     * Get potion name from 1.8 damage.
     *
     * @param damage Potion item damage.
     * @return       A potion name.
     */
    public String getPotion(int damage) {
        return (String) cache.computeIfAbsent(damage, __ -> {
            for (PotionType type : PotionType.VALUES) {
                if (type.getPotion() == damage || type.getSplash() == damage) {
                    return "minecraft:" + type.name().toLowerCase();
                }
            }
            return null;
        });
    }

    /**
     * Get corresponding damage to 1.8 item from potion name.
     *
     * @param id     Item material ID.
     * @param potion Potion name.
     * @return       A item damage number.
     */
    public int getDamage(String id, String potion) {
        return (int) cache.computeIfAbsent(id + "=" + potion, __ -> {
            String potionType = potion.replace("minecraft:", "").toUpperCase();
            for (PotionType type : PotionType.VALUES) {
                if (type.name().equals(potionType)) {
                    if (id.equalsIgnoreCase("minecraft:splash_potion")) {
                        return type.getSplash();
                    } else {
                        return type.getPotion();
                    }
                }
            }
            return -1;
        });
    }

    /**
     * Potion type enum only with potion names that can
     * be translated to MC 1.8 item damage.
     *
     * @author Rubenicos
     */
    public enum PotionType {

        MUNDANE(64),
        THICK(32),
        AWKWARD(16),
        NIGHT_VISION(8230, 16422),
        INVISIBILITY(8238, 16430),
        LEAPING(8203, 16395),
        FIRE_RESISTANCE(8227, 16419),
        SWIFTNESS(8194, 16386),
        SLOWNESS(8234, 16426),
        WATER_BREATHING(8237, 16429),
        HEALING(8261, 16453),
        HARMING(8268, 16460),
        POISON(8196, 16388),
        REGENERATION(8193, 16385),
        STRENGTH(8201, 16393),
        WEAKNESS(8232, 16424),
        STRONG_LEAPING(8235, 16427),
        STRONG_SWIFTNESS(8226, 16418),
        STRONG_HEALING(8229, 16421),
        STRONG_HARMING(8236, 16428),
        STRONG_POISON(8228, 16420),
        STRONG_REGENERATION(8225, 16417),
        STRONG_STRENGTH(8233, 8233),
        LONG_NIGHT_VISION(8262, 16454),
        LONG_INVISIBILITY(8270, 16462),
        LONG_LEAPING(8267, 16459),
        LONG_FIRE_RESISTANCE(8259, 16451),
        LONG_SWIFTNESS(8258, 16450),
        LONG_SLOWNESS(8266, 16458),
        LONG_WATER_BREATHING(8269, 16461),
        LONG_POISON(8260, 16452),
        LONG_REGENERATION(8257, 16449),
        LONG_STRENGTH(8265, 16457),
        LONG_WEAKNESS(8264, 16456);

        public static final PotionType[] VALUES = values();

        private final int potion;
        private final int splash;

        PotionType(int potion) {
            this(potion, -1);
        }

        PotionType(int potion, int splash) {
            this.potion = potion;
            this.splash = splash;
        }

        /**
         * Get current normal potion damage.
         *
         * @return A item damage number.
         */
        public int getPotion() {
            return potion;
        }

        /**
         * Get current splash potion damage.
         *
         * @return A item damage number.
         */
        public int getSplash() {
            return splash;
        }
    }
}
