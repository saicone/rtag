package com.saicone.rtag.util;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Minecraft enchantment tags with associated ID.<br>
 * Take in count Mojang discontinued IDs since MC 1.14,
 * any ID for new enchantments is not official.
 *
 * @author Rubenicos
 */
@SuppressWarnings("javadoc")
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
    MULTISHOT(14, 52),
    PIERCING(14, 53),
    POWER(1, 48, "ARROW_DAMAGE", "ARROW_POWER"),
    PROJECTILE_PROTECTION(0, 4, "PROTECTION_PROJECTILE"),
    PROTECTION(0, 0, "PROTECTION_ENVIRONMENTAL"),
    PUNCH(11, 49, "ARROW_KNOCKBACK"),
    QUICK_CHARGE(14, 54, "QUICKCHARGE"),
    RESPIRATION(0, 5, "OXYGEN"),
    RIPTIDE(13, 67),
    SHARPNESS(0, 16, "DAMAGE_ALL"),
    SILK_TOUCH(0, 33),
    SMITE(0, 17, "DAMAGE_UNDEAD"),
    SOUL_SPEED(16, 11),
    SWEEPING(11, 22, "SWEEPING_EDGE"),
    SWIFT_SNEAK(19, 12),
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

    /**
     * Current key where enchantments are inside items tag.
     */
    public static final String TAG_KEY = ServerInstance.isLegacy ? "ench" : "Enchantments";
    /**
     * Current key where stored enchantments are inside items tag.
     */
    public static final String STORED_KEY = "StoredEnchantments";

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

    private final Enchantment enchantment;

    EnchantmentTag(int version, int id, String... aliases) {
        this(version, (short) id, aliases);
    }

    @SuppressWarnings("deprecation")
    EnchantmentTag(int version, short id, String... aliases) {
        this.version = version;
        this.id = id;
        this.aliases = aliases;
        if (ServerInstance.isLegacy) {
            this.enchantment = Enchantment.getByName(this.name());
        } else {
            this.enchantment = Enchantment.getByKey(NamespacedKey.minecraft(this.name().toLowerCase()));
        }
    }

    /**
     * Get added version for this enchant.
     *
     * @return A server version number.
     */
    public int getVersion() {
        return version;
    }

    /**
     * Get enchant numeric ID.
     *
     * @return A number representing the enchantment.
     */
    public short getId() {
        return id;
    }

    /**
     * Get current enchant aliases.
     *
     * @return A string array with enchant aliases.
     */
    public String[] getAliases() {
        return aliases;
    }

    /**
     * Get current enchant as {@link Enchantment}.
     *
     * @return A Bukkit enchantment if exist on the current version, null otherwise.
     */
    public Enchantment getEnchantment() {
        return enchantment;
    }

    /**
     * Get current enchant key depending on current Bukkit version.
     *
     * @return Short id for legacy versions, namespaced key otherwise.
     */
    public Object getKey() {
        if (ServerInstance.isLegacy) {
            return id;
        } else {
            return "minecraft:" + name().toLowerCase();
        }
    }

    /**
     * Check if the current EnchantmentTag correspond to name object.
     *
     * @param name Enchantment name, alias or short id.
     * @return     true if the EnchantmentTag is assigned to name object.
     */
    public boolean compare(Object name) {
        if (name instanceof Short) {
            return (short) name == id;
        } else if (name instanceof String) {
            final String s = (String) name;
            if (name().equalsIgnoreCase(s)) {
                return true;
            }
            for (String alias : aliases) {
                if (alias.equalsIgnoreCase(s)) {
                    return true;
                }
            }
        } else if (name instanceof Number) {
            try {
                return compareKey(Short.parseShort(String.valueOf(name)));
            } catch (Exception ignored) { }
        }
        return false;
    }

    /**
     * Check if the current EnchantmentTag correspond to key object.
     *
     * @param key Namespaced key or short id.
     * @return    true if the EnchantmentTag is assigned to key object.
     */
    public boolean compareKey(Object key) {
        if (key instanceof Short) {
            return (short) key == id;
        } else if (key instanceof String) {
            return name().equalsIgnoreCase(((String) key).replace("minecraft:", ""));
        } else if (key instanceof Number) {
            try {
                return compareKey(Short.parseShort(String.valueOf(key)));
            } catch (Exception ignored) { }
        }
        return false;
    }

    /**
     * Parse the current object name into the most convenient format for EnchantmentTag.
     *
     * @param name Object representing the EnchantmentTag comparison.
     * @return     Enchantment name, alias or short id.
     */
    public static Object parseName(Object name) {
        if (name instanceof Short) {
            return name;
        } else if (name instanceof Number) {
            try {
                return Short.parseShort(String.valueOf(name));
            } catch (Exception ignored) { }
        }
        return String.valueOf(name).replace("minecraft:", "").toUpperCase().replace(' ', '_');
    }

    /**
     * Get the current key where enchantments are stored in the item tag depending on item meta or server version.
     *
     * @param item Item to check enchantment key.
     * @return     A String key.
     */
    public static String getEnchantmentKey(ItemStack item) {
        if (item.getItemMeta() instanceof EnchantmentStorageMeta) {
            return STORED_KEY;
        }
        return TAG_KEY;
    }

    /**
     * Get the EnchantmentTag of provided name, short id or {@link Enchantment}.
     *
     * @param name Enchantment name, alias or short id.
     * @return     The EnchantmentTag assigned to key object, null if not exist.
     */
    @SuppressWarnings("deprecation")
    public static EnchantmentTag of(Object name) {
        if (name instanceof EnchantmentTag) {
            return (EnchantmentTag) name;
        }
        if (name instanceof Enchantment) {
            return of(((Enchantment) name).getName());
        }
        final Object finalName = parseName(name);
        for (EnchantmentTag value : VALUES) {
            if (value.compare(finalName)) {
                return value;
            }
        }
        return null;
    }
}
