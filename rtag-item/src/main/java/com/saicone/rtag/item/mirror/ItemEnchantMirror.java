package com.saicone.rtag.item.mirror;

import com.saicone.rtag.item.ItemMirror;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;
import com.saicone.rtag.util.EnchantmentTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ItemEnchantMirror class to convert item enchants
 * across versions.
 *
 * @author Rubenicos
 */
public class ItemEnchantMirror implements ItemMirror {

    /**
     * ItemDisplayMirror public instance for legacy versions.
     */
    public static final ItemEnchantMirror LEGACY;
    /**
     * ItemDisplayMirror public instance for flat versions (+1.13).
     */
    public static final ItemEnchantMirror INSTANCE;

    static {
        Map<Object, Object> map = new HashMap<>();
        for (EnchantmentTag enchant : EnchantmentTag.LEGACY_VALUES) {
            map.put(enchant.getId(), "minecraft:" + enchant.name().toLowerCase());
        }
        INSTANCE = new ItemEnchantMirror(map, "ench", "Enchantments");
        Map<Object, Object> legacyMap = new HashMap<>();
        map.forEach((key, value) -> legacyMap.put(value, key));
        LEGACY = new ItemEnchantMirror(legacyMap, "Enchantments", "ench");
    }

    private final Map<Object, Object> map;
    private final String fromKey;
    private final String toKey;

    public ItemEnchantMirror(Map<Object, Object> map, String fromKey, String toKey) {
        this.map = map;
        this.fromKey = fromKey;
        this.toKey = toKey;
    }

    public Map<Object, Object> getMap() {
        return map;
    }

    @Override
    public void upgrade(Object compound, Object tag, int from, int to) throws Throwable {
        // "ench" -> "Enchantments"
        // Enchant ID -> Enchant Name
        if (tag != null && from <= 12) {
            processEnchants(tag);
        }
    }

    @Override
    public void downgrade(Object compound, Object tag, int from, int to) throws Throwable {
        // "Enchantments" -> "ench"
        // Enchant Name Enchant ID
        if (tag != null && from > 12) {
            processEnchants(tag);
        }
    }

    public void processEnchants(Object tag) throws Throwable {
        Object enchants = TagCompound.get(tag, fromKey);
        if (enchants != null) {
            int size = TagList.size(enchants);
            List<Object> fromKeyList = new ArrayList<>();
            List<Object> toKeyList = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                Object enchant = TagList.get(enchants, i);
                Object value = TagBase.getValue(TagCompound.get(enchant, "id"));
                if (value != null) {
                    Object object = map.get(value);
                    if (object != null) {
                        Object id = TagBase.newTag(object);
                        if (id != null) {
                            TagCompound.set(enchant, "id", id);
                            toKeyList.add(enchant);
                            continue;
                        }
                    }
                }
                fromKeyList.add(enchant);
            }
            if (toKeyList.size() == size) {
                TagCompound.set(tag, toKey, enchants);
                TagCompound.remove(tag, fromKey);
            } else {
                TagCompound.set(tag, toKey, TagList.newTag(toKeyList));
                TagCompound.set(tag, fromKey, TagList.newTag(fromKeyList));
            }
        }
    }
}
