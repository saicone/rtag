package com.saicone.rtag.item;

import com.saicone.rtag.item.mirror.ItemDisplayMirror;
import com.saicone.rtag.item.mirror.ItemEnchantMirror;
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
    public static final ItemTagStream INSTANCE;

    static {
        List<ItemMirror> mirror = new ArrayList<>();
        mirror.add(ItemDisplayMirror.INSTANCE);

        if (ServerInstance.isLegacy) {
            mirror.add(ItemEnchantMirror.LEGACY);
        } else {
            mirror.add(ItemEnchantMirror.INSTANCE);
        }
        INSTANCE = new ItemTagStream(mirror);
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
        try {
            Object compound = ItemTag.saveTag(ItemBridge.asMinecraft(object));
            onSave(compound);
            return compound;
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    @Override
    public ItemStack build(Object compound) {
        try {
            onLoad(compound);
            return ItemBridge.asBukkit(ItemTag.createStack(compound));
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Executed method when NBTTagCompound is extracted from item.
     *
     * @param compound NBTTagCompound with item information.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public void onSave(Object compound) throws Throwable {
        if (compound != null) {
            TagCompound.set(compound, getVersionKey(), TagBase.newTag(getVersion()));
        }
    }

    /**
     * Executed method when NBTTagCompound used tu build an item.
     *
     * @param compound NBTTagCompound with item information.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public void onLoad(Object compound) throws Throwable {
        Integer version = (Integer) TagBase.getValue(TagCompound.get(compound, getVersionKey()));
        if (version != null && version != getVersion()) {
            Object tag = TagCompound.get(compound, "tag");
            if (version > getVersion()) {
                for (ItemMirror item : mirror) {
                    item.downgrade(compound, tag, version, getVersion());
                }
            } else {
                for (ItemMirror item : mirror) {
                    item.upgrade(compound, tag, version, getVersion());
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
