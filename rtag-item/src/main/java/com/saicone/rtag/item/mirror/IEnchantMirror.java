package com.saicone.rtag.item.mirror;

import com.saicone.rtag.item.ItemMirror;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;
import com.saicone.rtag.util.EnchantmentTag;
import com.saicone.rtag.util.MC;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * IEnchantMirror class to convert item enchants
 * across versions.
 *
 * @author Rubenicos
 */
public class IEnchantMirror implements ItemMirror {

    /**
     * Map to get TagBase enchantment ID from String.
     */
    public static final Map<Object, Object> fromString = new HashMap<>();
    /**
     * Map to get TagBase enchantment namespaced key from Short ID.
     */
    public static final Map<Object, Object> fromShort = new HashMap<>();

    static {
        try {
            for (EnchantmentTag enchant : EnchantmentTag.LEGACY_VALUES) {
                String name = "minecraft:" + enchant.name().toLowerCase();
                fromShort.put(enchant.getId(), TagBase.newTag(name));
                fromString.put(name, TagBase.newTag(enchant.getId()));
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private final Map<Object, Object> map;
    private final String bookKey;
    private final String fromKey;
    private final String toKey;

    /**
     * Constructs an IEnchantMirror with specified server versions to create keys automatically.
     *
     * @param version The server version to create keys for.
     */
    @Deprecated(since = "1.5.14", forRemoval = true)
    public IEnchantMirror(float version) {
        this(MC.findReverse(MC::featRevision, version));
    }

    /**
     * Constructs an IEnchantMirror with specified server versions to create keys automatically.
     *
     * @param version the server version to create keys for.
     */
    public IEnchantMirror(@NotNull MC version) {
        if (version.isOlderThan(MC.V_1_13)) {
            // "Enchantments" -> "ench"
            // Enchant Name Enchant ID
            this.map = fromString;
            this.bookKey = "StoredEnchantments";
            this.fromKey = "Enchantments";
            this.toKey = "ench";
        } else {
            // "ench" -> "Enchantments"
            // Enchant ID -> Enchant Name
            this.map = fromShort;
            this.bookKey = "StoredEnchantments";
            this.fromKey = "ench";
            this.toKey = "Enchantments";
        }
    }

    /**
     * Constructs an IEnchantMirror with specified parameters.
     *
     * @param map     Enchantments map with IDs and names.
     * @param bookKey Key to handle with enchanted book.
     * @param fromKey Key to get current item enchants.
     * @param toKey   New key to set converted enchants.
     */
    public IEnchantMirror(@NotNull Map<Object, Object> map, @NotNull String bookKey, @NotNull String fromKey, @NotNull String toKey) {
        this.map = map;
        this.bookKey = bookKey;
        this.fromKey = fromKey;
        this.toKey = toKey;
    }

    /**
     * Get current enchantments map.
     *
     * @return A map with enchantments names and IDs.
     */
    public Map<Object, Object> getMap() {
        return map;
    }

    @Override
    public @NotNull MC getMaximumVersion() {
        return MC.V_1_13;
    }

    @Override
    public void upgrade(@NotNull Object compound, @NotNull String id, @NotNull Object components, @NotNull MC from, @NotNull MC to) {
        if (to.isNewerThanOrEquals(MC.V_1_13) && from.isOlderThan(MC.V_1_13)) {
            processEnchants(components, id.equalsIgnoreCase("minecraft:enchanted_book"));
        }
    }

    @Override
    public void downgrade(@NotNull Object compound, @NotNull String id, @NotNull Object components, @NotNull MC from, @NotNull MC to) {
        if (from.isNewerThanOrEquals(MC.V_1_13) && to.isOlderThan(MC.V_1_13)) {
            processEnchants(components, id.equalsIgnoreCase("minecraft:enchanted_book"));
        }
    }

    /**
     * Process current item enchants inside tag.
     *
     * @param tag  ItemStack tag.
     * @param book True if the tag is from enchanted book.
     */
    @ApiStatus.Internal
    public void processEnchants(@NotNull Object tag, boolean book) {
        Object enchants = TagCompound.get(tag, book ? bookKey : fromKey);
        if (enchants != null) {
            int size = TagList.size(enchants);
            for (int i = 0; i < size; i++) {
                Object enchant = TagList.get(enchants, i);
                Object value = TagBase.getValue(TagCompound.get(enchant, "id"));
                if (value != null) {
                    Object id = map.get(value);
                    if (id != null) {
                        TagCompound.set(enchant, "id", id);
                    }
                }
            }
            if (!book) {
                TagCompound.set(tag, toKey, enchants);
                TagCompound.remove(tag, fromKey);
            }
        }
    }
}
