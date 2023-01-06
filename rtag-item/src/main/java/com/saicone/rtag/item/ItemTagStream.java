package com.saicone.rtag.item;

import com.saicone.rtag.item.mirror.*;
import com.saicone.rtag.stream.TStream;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.util.ServerInstance;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

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
            if (ServerInstance.verNumber >= 17) {
                mirror.add(new IBundleMirror(INSTANCE));
            }
        }
        INSTANCE.getMirror().addAll(mirror);
    }

    private final List<ItemMirror> mirror;
    private final int version;
    private final String versionKey;

    /**
     * Constructs an simple Rtag without any {@link ItemMirror}.
     */
    public ItemTagStream() {
        this(new ArrayList<>());
    }

    /**
     * Constructs an Rtag with specified {@link ItemMirror} list.
     *
     * @param mirror Mirror list.
     */
    public ItemTagStream(List<ItemMirror> mirror) {
        this(mirror, ServerInstance.verNumber, "rtagDataVersion");
    }

    /**
     * Constructs an Rtag with specified {@link ItemMirror} list
     * and additional parameters.
     *
     * @param mirror     Mirror list.
     * @param version    Server version.
     * @param versionKey Version key identifier from compound.
     */
    public ItemTagStream(List<ItemMirror> mirror, int version, String versionKey) {
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
    public int getVersion() {
        return version;
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
        Integer version = (Integer) TagBase.getValue(TagCompound.get(compound, getVersionKey()));
        if (version != null && version != getVersion()) {
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
    public void onLoad(Object compound, int from, int to) {
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
