package com.saicone.rtag.item.mirror;

import com.saicone.rtag.item.ItemMirror;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;
import com.saicone.rtag.util.EnchantmentTag;

import java.util.HashMap;
import java.util.Map;

/**
 * IEnchantMirror class to convert item enchants
 * across versions.
 *
 * @author Rubenicos
 */
public class IEnchantMirror implements ItemMirror {

    public static final Map<Object, Object> fromString = new HashMap<>();
    public static final Map<Object, Object> fromShort = new HashMap<>();

    static {
        for (EnchantmentTag enchant : EnchantmentTag.LEGACY_VALUES) {
            fromShort.put(enchant.getId(), "minecraft:" + enchant.name().toLowerCase());
        }
        fromShort.forEach((key, value) -> fromString.put(value, key));
    }

    private final Map<Object, Object> map;
    private final String bookKey;
    private final String fromKey;
    private final String toKey;

    /**
     * Constructs an IEnchantMirror with specified parameters.
     *
     * @param map     Enchantments map with IDs and names.
     * @param bookKey Key to handle with enchanted book.
     * @param fromKey Key to get current item enchants.
     * @param toKey   New key to set converted enchants.
     */
    public IEnchantMirror(Map<Object, Object> map, String bookKey, String fromKey, String toKey) {
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
    public int getDeprecationVersion() {
        return 13;
    }

    @Override
    public void upgrade(Object compound, String id, Object tag, int from, int to) throws Throwable {
        if (from <= 12) {
            processEnchants(tag, id.equalsIgnoreCase("enchanted_book"));
        }
    }

    @Override
    public void downgrade(Object compound, String id, Object tag, int from, int to) throws Throwable {
        if (from > 12) {
            processEnchants(tag, id.equalsIgnoreCase("enchanted_book"));
        }
    }

    /**
     * Process current item enchants inside tag.
     *
     * @param tag  ItemStack tag.
     * @param book True if the tag is from enchanted book.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public void processEnchants(Object tag, boolean book) throws Throwable {
        Object enchants = TagCompound.get(tag, book ? bookKey : fromKey);
        if (enchants != null) {
            int size = TagList.size(enchants);
            for (int i = 0; i < size; i++) {
                Object enchant = TagList.get(enchants, i);
                Object value = TagBase.getValue(TagCompound.get(enchant, "id"));
                if (value != null) {
                    Object object = map.get(value);
                    if (object != null) {
                        Object id = TagBase.newTag(object);
                        if (id != null) {
                            TagCompound.set(enchant, "id", id);
                        }
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
