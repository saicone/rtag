package com.saicone.rtag.item;

import com.saicone.rtag.item.mirror.*;
import com.saicone.rtag.stream.TStream;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.util.ChatComponent;
import com.saicone.rtag.util.ServerInstance;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ItemTagStream class to write/read {@link ItemStack} into/from bytes.
 *
 * @author Rubenicos
 */
public class ItemTagStream extends TStream<ItemStack> {

    /**
     * ItemTagStream public instance adapted for current server version.
     */
    public static final ItemTagStream INSTANCE = new ItemTagStream();

    static {
        List<ItemMirror> mirror = new ArrayList<>();
        mirror.add(new IPotionMirror());
        mirror.add(new ISkullOwnerMirror());
        mirror.add(new IMaterialMirror());
        mirror.add(new IDisplayMirror());

        if (ServerInstance.isLegacy) {
            // "Enchantments" -> "ench"
            // Enchant Name Enchant ID
            mirror.add(new IEnchantMirror(IEnchantMirror.fromString, "StoredEnchantments", "Enchantments", "ench"));
        } else {
            // "ench" -> "Enchantments"
            // Enchant ID -> Enchant Name
            mirror.add(new IEnchantMirror(IEnchantMirror.fromShort, "StoredEnchantments", "ench", "Enchantments"));
        }
        if (ServerInstance.verNumber >= 9) {
            mirror.add(new IShulkerMirror(INSTANCE));
            if (ServerInstance.verNumber >= 14) {
                mirror.add(new IEffectMirror());
                if (ServerInstance.verNumber >= 17) {
                    mirror.add(new IBundleMirror(INSTANCE));
                }
            }
        }
        INSTANCE.getMirror().addAll(mirror);
    }

    private final List<ItemMirror> mirror;
    private final float version;
    private final String versionKey;

    /**
     * Constructs a simple ItemTagStream without any {@link ItemMirror}.
     */
    public ItemTagStream() {
        this(new ArrayList<>());
    }

    /**
     * Constructs an ItemTagStream with specified {@link ItemMirror} list.
     *
     * @param mirror Mirror list.
     */
    public ItemTagStream(List<ItemMirror> mirror) {
        this(mirror, Float.parseFloat(ServerInstance.verNumber + "." + (ServerInstance.release < 10 ? "0" : "") + ServerInstance.release), "rtagDataVersion");
    }

    /**
     * Constructs an ItemTagStream with specified {@link ItemMirror} list
     * and additional parameters.
     *
     * @param mirror     Mirror list.
     * @param version    Server version.
     * @param versionKey Version key identifier from compound.
     */
    public ItemTagStream(List<ItemMirror> mirror, float version, String versionKey) {
        this.mirror = mirror;
        this.version = version;
        this.versionKey = versionKey;
    }

    /**
     * Get current {@link ItemMirror} list.
     *
     * @return A list of item mirror.
     */
    public List<ItemMirror> getMirror() {
        return mirror;
    }

    /**
     * Get current server version for this instance.
     *
     * @return Server version.
     */
    public float getVersion() {
        return version;
    }

    /**
     * Get current version number from compound.
     *
     * @param compound NBTTagCompound to read.
     * @return         A valid version number or null.
     */
    protected Float getVersion(Object compound) {
        final Map<String, Object> value = TagCompound.getValue(compound);
        Object version = TagBase.getValue(value.get(getVersionKey()));
        if (version instanceof Number) {
            return ((Number) version).floatValue();
        }

        version = TagBase.getValue(value.get("DataVersion"));
        if (version == null) {
            version = TagBase.getValue(value.get("v"));
        }

        if (version instanceof Number) {
            final int dataVersion = ((Number) version).intValue();
            final int release = ServerInstance.release(dataVersion);
            return Float.parseFloat(ServerInstance.verNumber(dataVersion) + "." + (release < 10 ? "0" : "") + release);
        }

        return null;
    }

    /**
     * Get current version key identifier.
     *
     * @return Version key identifier.
     */
    public String getVersionKey() {
        return versionKey;
    }

    @Override
    public Object extract(ItemStack object) {
        final Object compound = ItemObject.save(ItemObject.asNMSCopy(object));
        try {
            onSave(compound);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return compound;
    }

    @Override
    public ItemStack build(Object compound) {
        try {
            onLoad(compound);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return ItemObject.asBukkitCopy(ItemObject.newItem(compound));
    }

    /**
     * Convert item to readable map, making display name and lore
     * components as colored strings.
     *
     * @param item Item to convert.
     * @return     A readable map that represent the provided item.
     */
    public Map<String, Object> toReadableMap(ItemStack item) {
        return readable(toMap(item), true);
    }

    /**
     * Get item by read provided Map of objects and also convert
     * display name and lore to component if is required.
     *
     * @param map Map that represent the object.
     * @return    An item representation using readable Map as compound.
     */
    public ItemStack fromReadableMap(Map<String, Object> map) {
        return fromMap(readable(map, false));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readable(Map<String, Object> map, boolean forward) {
        final Map<String, Object> tag;
        final Map<String, Object> display;
        if (ServerInstance.verNumber >= 13 && (tag = (Map<String, Object>) map.get("tag")) != null && (display = (Map<String, Object>) tag.get("display")) != null) {
            // Process name
            final String name = (String) display.get("Name");
            if (name != null) {
                display.put("Name", readable(name, forward));
            }
            if (ServerInstance.verNumber >= 14) {
                // Process lore
                final List<String> lore = (List<String>) display.get("Lore");
                if (lore != null) {
                    lore.replaceAll(line -> readable(line, forward));
                }
            }
        }
        return map;
    }

    private String readable(String s, boolean forward) {
        if (ChatComponent.isChatComponent(s)) {
            if (forward) {
                return ChatComponent.toString(s);
            }
        } else if (!forward) {
            return ChatComponent.toJson(s);
        }
        return s;
    }

    /**
     * Executed method when NBTTagCompound is extracted from item.
     *
     * @param compound NBTTagCompound with item information.
     */
    public void onSave(Object compound) {
        if (compound != null) {
            TagCompound.set(compound, getVersionKey(), TagBase.newTag(getVersion()));
        }
    }

    /**
     * Executed method when NBTTagCompound used tu build an item.
     *
     * @param compound NBTTagCompound with item information.
     */
    public void onLoad(Object compound) {
        final Float version = getVersion(compound);
        if (version != null && !versionMatches(version, getVersion())) {
            onLoad(compound, version, getVersion());
        }
    }

    /**
     * Executed method when NBTTagCompound used tu build an item.
     *
     * @param compound NBTTagCompound with item information.
     * @param from     Version specified in compound.
     * @param to       Version to convert.
     */
    public void onLoad(Object compound, float from, float to) {
        String id = (String) TagBase.getValue(TagCompound.get(compound, "id"));
        if (id == null) return;

        Object tag = TagCompound.get(compound, "tag");
        if (from > to) {
            if (tag == null) {
                for (ItemMirror item : mirror) {
                    item.downgrade(compound, id, from, to);
                }
            } else {
                for (ItemMirror item : mirror) {
                    item.downgrade(compound, id, tag, from, to);
                }
            }
        } else {
            if (tag == null) {
                for (ItemMirror item : mirror) {
                    item.upgrade(compound, id, from, to);
                }
            } else {
                for (ItemMirror item : mirror) {
                    item.upgrade(compound, id, tag, from, to);
                }
            }
        }
    }

    /**
     * Check if the provided pair of versions matches.
     *
     * @param from Version specified in compound.
     * @param to   Version to convert.
     * @return     true if matches.
     */
    protected boolean versionMatches(float from, float to) {
        if (from < 19.02f && to < 19.02f) {
            return (int) from == (int) to;
        }
        return from == to;
    }

    @Override
    public String listToBase64(List<ItemStack> items) {
        List<ItemStack> filter = new ArrayList<>();
        for (ItemStack item : items) {
            if (item != null && item.getType() != Material.AIR) {
                filter.add(item);
            }
        }
        return super.listToBase64(filter);
    }
}
