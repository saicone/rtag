package com.saicone.rtag.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Minecraft enchantment tags with associated ID.<br>
 * Take in count Mojang discontinued IDs since MC 1.14.<br>
 *
 * @author Rubenicos
 */
public enum EnchantmentTag {

    AQUA_AFFINITY(0, 6, "WATER_WORKER"),
    BANE_OF_ARTHROPODS(0, 18, "DAMAGE_ARTHROPODS"),
    BINDING_CURSE(11, 10, "CURSE_OF_BINDING", "BINDING"),
    BLAST_PROTECTION(0, 3, "PROTECTION_EXPLOSIONS", "EXPLOSIONS_PROTECTION"),
    CHANNELING(13, 68),
    DEPTH_STRIDER(8, 8),
    EFFICIENCY(0, 32, "DIG_SPEED"),
    FEATHER_FALLING(0, 2, "PROTECTION_FALL", "FALL_PROTECTION"),
    FIRE_ASPECT(0, 20),
    FIRE_PROTECTION(0, 1, "PROTECTION_FIRE"),
    FLAME(1, 50, "ARROW_FIRE", "FIRE_ARROW", "FLAME_ARROW"),
    FORTUNE(0, 35, "LOOT_BONUS_BLOCKS"),
    FROST_WALKER(9, 9),
    IMPALING(13, 66),
    INFINITY(1, 51, "ARROW_INFINITE"),
    KNOCKBACK(0, 19),
    LOOTING(0, 21, "LOOT_BONUS_MOBS"),
    LOYALTY(13, 65),
    LUCK_OF_THE_SEA(7, 61, "LUCK", "LUCK_OF_SEA"),
    LURE(7, 62),
    MENDING(9, 70),
    MULTISHOT(14),
    PIERCING(14),
    POWER(1, 48, "ARROW_DAMAGE", "ARROW_POWER"),
    PROJECTILE_PROTECTION(0, 4, "PROTECTION_PROJECTILE"),
    PROTECTION(0, 0, "PROTECTION_ENVIRONMENTAL"),
    PUNCH(11, 49, "ARROW_KNOCKBACK"),
    QUICK_CHARGE(14, "QUICKCHARGE"),
    RESPIRATION(0, 5, "OXYGEN"),
    RIPTIDE(13, 67),
    SHARPNESS(0, 16, "DAMAGE_ALL"),
    SILK_TOUCH(0, 33),
    SMITE(0, 17, "DAMAGE_UNDEAD"),
    SOUL_SPEED(16),
    SWEEPING(11, 22, "SWEEPING_EDGE"),
    THORNS(4, 7),
    UNBREAKING(0, 34, "DURABILITY"),
    VANISHING_CURSE(11, 71, "CURSE_OF_VANISHING", "VANISHING");


    /**
     * Cached values of {@link EnchantmentTag#values()}.
     */
    public static final EnchantmentTag[] VALUES = values();
    /**
     * Cached values of {@link EnchantmentTag#values()} including
     * only pre 1.13 enchantments.
     */
    public static final EnchantmentTag[] LEGACY_VALUES;
    /**
     * Cached values of {@link EnchantmentTag#values()} including
     * only server version compatible enchantments.
     */
    public static final EnchantmentTag[] SERVER_VALUES;

    static {
        List<EnchantmentTag> legacy = new ArrayList<>();
        List<EnchantmentTag> server = new ArrayList<>();
        for (EnchantmentTag tag : VALUES) {
            if (tag.getVersion() <= 12) {
                legacy.add(tag);
            }
            if (tag.getVersion() <= ServerInstance.verNumber) {
                server.add(tag);
            }
        }
        LEGACY_VALUES = legacy.toArray(new EnchantmentTag[0]);
        SERVER_VALUES = server.toArray(new EnchantmentTag[0]);
    }

    private final int version;
    private final short id;
    private final String[] aliases;

    EnchantmentTag(int version, String... aliases) {
        this(version, -1, aliases);
    }

    EnchantmentTag(int version, int id, String... aliases) {
        this(version, (short) id, aliases);
    }

    EnchantmentTag(int version, short id, String... aliases) {
        this.version = version;
        this.id = id;
        this.aliases = aliases;
    }

    public int getVersion() {
        return version;
    }

    public short getId() {
        return id;
    }

    public String[] getAliases() {
        return aliases;
    }
}
