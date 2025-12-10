package com.saicone.rtag.item;

import com.saicone.rtag.item.mirror.*;
import com.saicone.rtag.stream.TStream;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.util.ChatComponent;
import com.saicone.rtag.util.MC;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public static final ItemTagStream INSTANCE = valueOf(MC.first(), MC.version());

    @NotNull
    public static ItemTagStream valueOf(@NotNull MC min, @NotNull MC target) {
        if (min.isNewerThan(target)) {
            throw new IllegalArgumentException("The minimum supported version cannot be less than target version");
        }

        final List<ItemMirror> mirrors = new ArrayList<>();
        final ItemTagStream instance = new ItemTagStream(mirrors, target);

        if (min.isComponent()) {
            mirrors.add(new IComponentMirror());
            mirrors.add(new IMaterialMirror());
            mirrors.add(new IContainerMirror(instance, true));
            mirrors.add(new IBundleMirror(instance, true));
        } else { // pre-components mirrors
            if (min.isOlderThan(MC.V_1_20_2)) {
                mirrors.add(new IPotionMirror(min.isOlderThan(MC.V_1_9)));
            }
            if (min.isOlderThan(MC.V_1_16)) {
                mirrors.add(new ISkullOwnerMirror());
                mirrors.add(new IAttributeMirror());
            }
            mirrors.add(new IMaterialMirror());
            if (min.isOlderThan(MC.V_1_14)) {
                mirrors.add(new IDisplayMirror(min.isOlderThan(MC.V_1_13)));
            }
            if (min.isOlderThan(MC.V_1_13) || target.isOlderThan(MC.V_1_13)) {
                mirrors.add(new IEnchantMirror(target));
            }

            if (target.isNewerThanOrEquals(MC.V_1_9)) {
                mirrors.add(new IContainerMirror(instance, false));
                if (target.isNewerThanOrEquals(MC.V_1_14)) {
                    mirrors.add(new IEffectMirror(target));
                    if (target.isNewerThanOrEquals(MC.V_1_17)) {
                        mirrors.add(new IBundleMirror(instance, false));
                    }
                }
            }

            if (target.isComponent()) {
                // Upgrade any old tag, then convert into component
                mirrors.add(new IComponentMirror());
            } else {
                // Convert from component, then downgrade any tag
                mirrors.add(0, new IComponentMirror());
            }
        }

        return instance;
    }

    private final List<ItemMirror> mirror;
    private final MC targetVersion;

    private String versionKey;

    /**
     * Constructs a simple ItemTagStream instance.
     */
    public ItemTagStream() {
        this(new ArrayList<>());
    }

    /**
     * Constructs an ItemTagStream instance with specified parameters.
     *
     * @param mirror a list containing item mirrors to apply.
     */
    public ItemTagStream(@NotNull List<ItemMirror> mirror) {
        this(mirror, MC.version());
    }

    /**
     * Constructs an ItemTagStream instance with specified parameters.
     *
     * @param mirror        a list containing item mirrors to apply.
     * @param targetVersion the target version to convert items.
     */
    public ItemTagStream(@NotNull List<ItemMirror> mirror, @NotNull MC targetVersion) {
        this.mirror = mirror;
        this.targetVersion = targetVersion;
        this.versionKey = null;
    }

    /**
     * Constructs an ItemTagStream with specified {@link ItemMirror} list
     * and additional parameters.
     *
     * @param mirror     Mirror list.
     * @param version    Server version.
     * @param versionKey Version key identifier from compound.
     */
    @Deprecated(since = "1.5.14", forRemoval = true)
    public ItemTagStream(@NotNull List<ItemMirror> mirror, float version, @NotNull String versionKey) {
        this.mirror = mirror;
        this.targetVersion = MC.findReverse(MC::featRevision, version);
        this.versionKey = versionKey;
    }

    /**
     * Get current {@link ItemMirror} list.
     *
     * @return a list of item mirror.
     */
    @NotNull
    public List<ItemMirror> getMirror() {
        return mirror;
    }

    /**
     * The target version to convert items.
     *
     * @return a version object.
     */
    @NotNull
    public MC getTargetVersion() {
        return targetVersion;
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
    @NotNull
    public Map<String, Object> toReadableMap(@NotNull ItemStack item) {
        return parseMap(toMap(item), getTargetVersion(), true);
    }

    /**
     * Get item by read provided Map of objects and also convert
     * display name and lore to component if is required.
     *
     * @param map Map that represent the object.
     * @return    An item representation using readable Map as compound.
     */
    @NotNull
    public ItemStack fromReadableMap(@NotNull Map<String, Object> map) {
        final MC version = ItemData.lookupVersion(map);
        if (version != null) {
            return fromMap(parseMap(map, version, false));
        } else {
            return fromMap(map);
        }
    }

    @NotNull
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseMap(@NotNull Map<String, Object> map, @NotNull MC version, boolean readable) {
        if (version.isLegacy()) {
            return map;
        }
        final Map<String, Object> components;
        if (version.isComponent()) {
            components = (Map<String, Object>) map.get("components");
        } else {
            final Map<String, Object> tag = (Map<String, Object>) map.get("tag");
            if (tag == null) return map;
            components = (Map<String, Object>) tag.get("display");
        }

        if (components != null) {
            // Process name
            final String nameKey = version.isComponent() ? "minecraft:custom_name" : "Name";
            final String name = (String) components.get(nameKey);
            if (name != null) {
                components.put(nameKey, parseText(name, readable));
            }
            if (version.isNewerThanOrEquals(MC.V_1_14)) {
                // Process lore
                final List<String> lore = (List<String>) components.get(version.isComponent() ? "minecraft:lore" : "Lore");
                if (lore != null) {
                    lore.replaceAll(line -> parseText(line, readable));
                }
            }
        }
        return map;
    }

    @NotNull
    private String parseText(@NotNull String s, boolean readable) {
        if (ChatComponent.isChatComponent(s)) {
            if (readable) {
                return ChatComponent.toString(s);
            }
        } else if (!readable) {
            return ChatComponent.toJson(s);
        }
        return s;
    }

    /**
     * Executed method when NBTTagCompound is extracted from item.
     *
     * @param compound NBTTagCompound with item information.
     */
    public void onSave(@Nullable Object compound) {
        if (compound != null) {
            TagCompound.set(compound, ItemData.VERSION_KEY, TagBase.newTag(getTargetVersion().dataVersion()));
        }
    }

    /**
     * Executed method when NBTTagCompound used tu build an item.
     *
     * @param compound NBTTagCompound with item information.
     */
    public void onLoad(@Nullable Object compound) {
        final MC version = ItemData.lookupVersion(compound);
        if (version != null && !versionMatches(version, getTargetVersion())) {
            // Fix rare serialization exception
            TagCompound.remove(compound, getVersionKey());

            onLoad(compound, version, getTargetVersion());
        }
    }

    /**
     * Executed method when NBTTagCompound used tu build an item.
     *
     * @param compound NBTTagCompound with item information.
     * @param from     Version specified in compound.
     * @param to       Version to convert.
     */
    public void onLoad(@NotNull Object compound, @NotNull MC from, @NotNull MC to) {
        String id = (String) TagBase.getValue(TagCompound.get(compound, "id"));
        if (id == null) return;

        Object components = TagCompound.get(compound, from.isComponent() ? "components" : "tag");
        if (from.isNewerThan(to)) {
            if (components == null) {
                for (ItemMirror item : mirror) {
                    item.downgrade(compound, id, from, to);
                }
            } else {
                for (ItemMirror item : mirror) {
                    item.downgrade(compound, id, components, from, to);
                }
            }
        } else {
            if (components == null) {
                for (ItemMirror item : mirror) {
                    item.upgrade(compound, id, from, to);
                }
            } else {
                for (ItemMirror item : mirror) {
                    item.upgrade(compound, id, components, from, to);
                }
            }
        }
    }

    private boolean versionMatches(@NotNull MC from, @NotNull MC to) {
        if (from.isOlderThan(MC.V_1_19_3) && to.isOlderThan(MC.V_1_19_3)) {
            return from.feature() == to.feature();
        }
        return from.equals(to);
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

    /**
     * Create an item tag stream for provided version range.
     *
     * @param minVersion     The minimum version to support.
     * @param currentVersion The current server version.
     * @return               A newly generated item tag stream.
     */
    @NotNull
    @Deprecated(since = "1.5.14", forRemoval = true)
    public static ItemTagStream ofVersion(float minVersion, float currentVersion) {
        return ofVersion(minVersion, currentVersion, "rtagDataVersion");
    }

    /**
     * Create an item tag stream for provided version range.
     *
     * @param minVersion     The minimum version to support.
     * @param currentVersion The current server version.
     * @param versionKey     Version key identifier from compound.
     * @return               A newly generated item tag stream.
     */
    @NotNull
    @Deprecated(since = "1.5.14", forRemoval = true)
    public static ItemTagStream ofVersion(float minVersion, float currentVersion, @Nullable String versionKey) {
        final ItemTagStream stream = valueOf(MC.findReverse(MC::featRevision, minVersion), MC.findReverse(MC::featRevision, currentVersion));
        stream.versionKey = versionKey;
        return stream;
    }

    /**
     * Get current server version for this instance.
     *
     * @return Server version.
     */
    @Deprecated(since = "1.5.14", forRemoval = true)
    public float getVersion() {
        return targetVersion.featRevision();
    }

    /**
     * Get current version number from item compound.
     *
     * @param compound NBTTagCompound that represent an item.
     * @return         A valid version number or null.
     */
    @Nullable
    @Deprecated(since = "1.5.14", forRemoval = true)
    public Float getVersion(Object compound) {
        Object version = TagBase.getValue(TagCompound.get(compound, getVersionKey()));
        if (version instanceof Number) {
            return ((Number) version).floatValue();
        }
        return ItemData.getItemVersion(compound);
    }

    /**
     * Get current version key identifier.
     *
     * @return Version key identifier.
     */
    @NotNull
    @Deprecated(since = "1.5.14", forRemoval = true)
    public String getVersionKey() {
        return versionKey == null ? "rtagDataVersion" : versionKey;
    }

    /**
     * Executed method when NBTTagCompound used tu build an item.
     *
     * @param compound NBTTagCompound with item information.
     * @param from     Version specified in compound.
     * @param to       Version to convert.
     */
    @Deprecated(since = "1.5.14", forRemoval = true)
    @SuppressWarnings("all")
    public void onLoad(Object compound, float from, float to) {
        String id = (String) TagBase.getValue(TagCompound.get(compound, "id"));
        if (id == null) return;

        Object components = TagCompound.get(compound, from >= 20.04f ? "components" : "tag");
        if (from > to) {
            if (components == null) {
                for (ItemMirror item : mirror) {
                    item.downgrade(compound, id, from, to);
                }
            } else {
                for (ItemMirror item : mirror) {
                    item.downgrade(compound, id, components, from, to);
                }
            }
        } else {
            if (components == null) {
                for (ItemMirror item : mirror) {
                    item.upgrade(compound, id, from, to);
                }
            } else {
                for (ItemMirror item : mirror) {
                    item.upgrade(compound, id, components, from, to);
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
    @Deprecated(since = "1.5.14", forRemoval = true)
    protected boolean versionMatches(float from, float to) {
        if (from < 19.02f && to < 19.02f) {
            return (int) from == (int) to;
        }
        return from == to;
    }
}
