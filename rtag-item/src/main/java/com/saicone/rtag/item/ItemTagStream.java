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
    public static final ItemTagStream INSTANCE = ofVersion(8f, ServerInstance.VERSION);

    private final List<ItemMirror> mirror;
    private final float version;
    private final String versionKey;

    /**
     * Create an item tag stream for provided version range.
     *
     * @param minVersion     The minimum version to support.
     * @param currentVersion The current server version.
     * @return               A newly generated item tag stream.
     */
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
    public static ItemTagStream ofVersion(float minVersion, float currentVersion, String versionKey) {
        if (minVersion > currentVersion) {
            throw new IllegalArgumentException("The minimum supported version cannot be less than current server version");
        }

        final List<ItemMirror> mirrors = new ArrayList<>();
        final ItemTagStream instance = new ItemTagStream(mirrors, currentVersion, versionKey);

        // Pre-components mirrors
        if (minVersion <= 20.03f) {
            if (minVersion <= 20.01f) {
                mirrors.add(new IPotionMirror(minVersion < 9f));
            }
            if (minVersion < 16f) {
                mirrors.add(new ISkullOwnerMirror());
            }
            mirrors.add(new IMaterialMirror());
            if (minVersion < 14f) {
                mirrors.add(new IDisplayMirror(minVersion < 13f));
            }
            if (minVersion < 13f || currentVersion < 13f) {
                mirrors.add(new IEnchantMirror(currentVersion));
            }

            if (currentVersion >= 9f) {
                mirrors.add(new IContainerMirror(instance, false));
                if (currentVersion >= 14) {
                    mirrors.add(new IEffectMirror(currentVersion));
                    if (currentVersion >= 17f) {
                        mirrors.add(new IBundleMirror(instance, false));
                    }
                }
            }

            if (currentVersion >= 20.04f) {
                // Upgrade any old tag, then convert into component
                mirrors.add(new IComponentMirror());
            } else {
                // Convert from component, then downgrade any tag
                mirrors.add(0, new IComponentMirror());
            }
        } else {
            mirrors.add(new IMaterialMirror());
            mirrors.add(new IContainerMirror(instance, true));
            mirrors.add(new IBundleMirror(instance, true));
        }

        return instance;
    }

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
        this(mirror, ServerInstance.VERSION, "rtagDataVersion");
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
        return parseMap(toMap(item), getVersion(), true);
    }

    /**
     * Get item by read provided Map of objects and also convert
     * display name and lore to component if is required.
     *
     * @param map Map that represent the object.
     * @return    An item representation using readable Map as compound.
     */
    public ItemStack fromReadableMap(Map<String, Object> map) {
        return fromMap(parseMap(map, (float) map.getOrDefault(getVersionKey(), getVersion()), false));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseMap(Map<String, Object> map, float version, boolean readable) {
        if (version < 13f) {
            return map;
        }
        final boolean useComponent = version >= 20.04f;
        final Map<String, Object> components;
        if (useComponent) {
            components = (Map<String, Object>) map.get("components");
        } else {
            final Map<String, Object> tag = (Map<String, Object>) map.get("tag");
            if (tag == null) return map;
            components = (Map<String, Object>) tag.get("display");
        }

        if (components != null) {
            // Process name
            final String nameKey = useComponent ? "minecraft:custom_name" : "Name";
            final String name = (String) components.get(nameKey);
            if (name != null) {
                components.put(nameKey, parseText(name, readable));
            }
            if (version >= 14f) {
                // Process lore
                final List<String> lore = (List<String>) components.get(useComponent ? "minecraft:lore" : "Lore");
                if (lore != null) {
                    lore.replaceAll(line -> parseText(line, readable));
                }
            }
        }
        return map;
    }

    private String parseText(String s, boolean readable) {
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
