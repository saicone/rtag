package com.saicone.rtag.item;

import com.mojang.serialization.Dynamic;
import com.saicone.rtag.Rtag;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;
import com.saicone.rtag.util.ChatComponent;
import com.saicone.rtag.util.MC;
import com.saicone.rtag.util.ServerInstance;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.fixes.References;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides a way to decode and encode items from/to NBT compounds, as well as update them between different Minecraft versions.
 *
 * @author Rubenicos
 */
@ApiStatus.Experimental
public abstract class ItemDataFix {

    private static final ItemStack EMPTY_ITEM = new ItemStack(Material.AIR);

    /**
     * Returns the Mojang implementation of the item data fix,
     *
     * @return an item data fix
     */
    @NotNull
    public static ItemDataFix mojang() {
        return MojangDataFix.INSTANCE;
    }

    /**
     * Returns the Rtag implementation of the item data fix,
     *
     * @return an item data fix
     */
    @NotNull
    public static ItemDataFix rtag() {
        return RtagDataFix.INSTANCE;
    }

    /**
     * Returns the safe implementation of the item data fix,
     *
     * @return an item data fix
     */
    @NotNull
    public static ItemDataFix safe() {
        return SafeDataFix.INSTANCE;
    }

    /**
     * Decodes an item from a NBT compound, updating it to the current Minecraft version if necessary.
     *
     * @param compound the NBT compound representing the item
     * @return         the decoded ItemStack
     */
    @NotNull
    public ItemStack decodeItem(@Nullable Object compound) {
        if (compound == null || isEmpty(compound)) {
            return EMPTY_ITEM;
        }

        final MC version = ItemData.lookupVersion(compound);
        if (version == null) {
            throw new IllegalArgumentException("Cannot find data version from: " + compound);
        }
        return decodeItem(compound, version);
    }

    /**
     * Decodes an item from a NBT compound, updating it to the current Minecraft version if necessary.
     *
     * @param compound the NBT compound representing the item
     * @param version  the Minecraft version of the item
     * @return         the decoded ItemStack
     */
    @NotNull
    public ItemStack decodeItem(@NotNull Object compound, @NotNull MC version) {
        // Copy
        compound = TagCompound.clone(compound);
        // Update
        compound = updateItem(compound, version, MC.version());

        // Fix rare serialization exception
        TagCompound.remove(compound, ItemData.VERSION_KEY);

        final Object item = ItemObject.newItem(compound);
        if (item == null) {
            throw new IllegalArgumentException("Cannot decode item: " + compound);
        }

        return ItemObject.asCraftMirror(item);
    }

    /**
     * Decodes an array of items from a NBT compound, updating them to the current Minecraft version if necessary.
     *
     * @param compound the NBT compound representing the container
     * @return         an array of decoded ItemStacks
     */
    @Nullable
    public ItemStack[] decodeContainer(@Nullable Object compound) {
        if (compound == null) {
            return null;
        }

        final Object sizeTag = TagCompound.get(compound, "size");
        if (sizeTag == null) {
            return null;
        }
        final int size = (int) TagBase.getValue(sizeTag);

        final ItemStack[] items = new ItemStack[size];

        final Object itemsTag = TagCompound.get(compound, "items");
        if (itemsTag != null) {
            for (Object itemCompound : TagList.getValue(itemsTag)) {
                final Map<String, Object> itemValue = TagCompound.getValue(itemCompound);

                Object slotTag = itemValue.get("slot");
                if (slotTag == null) {
                    slotTag = itemValue.get("Slot");
                }
                if (slotTag == null) {
                    continue;
                }
                final int slot = (int) TagBase.getValue(slotTag);

                if (slot >= 0 && slot < size) {
                    // Fix rare serialization exception
                    itemValue.remove("slot");
                    itemValue.remove("Slot");

                    items[slot] = decodeItem(itemCompound);
                } else {
                    throw new IllegalArgumentException("Invalid slot " + slot + " for container of size " + size);
                }
            }
        }
        return items;
    }

    /**
     * Encodes an item into a NBT compound.
     *
     * @param item    the item to encode
     * @return        an encoded NBT compound
     */
    @NotNull
    public Object encodeItem(@Nullable ItemStack item) {
        return encodeItem(item, MC.version());
    }

    /**
     * Encodes an item into a NBT compound, adding the data version of the specified Minecraft version.
     *
     * @param item    the item to encode
     * @param version the Minecraft version to use for the data version
     * @return        an encoded NBT compound
     */
    @NotNull
    public Object encodeItem(@Nullable ItemStack item, @NotNull MC version) {
        final Object mcItem;
        if (item == null) {
            mcItem = null;
        } else {
            final ItemStack craftItem = ItemObject.getCraftStack(item);
            if (craftItem == null) {
                mcItem = ItemObject.asNMSCopy(item);
            } else {
                mcItem = ItemObject.getUncheckedHandle(craftItem);
            }
        }

        final Object compound = ItemObject.save(mcItem);
        final Map<String, Object> value = TagCompound.getValue(compound);
        if (!isEmpty(value)) {
            value.put(ItemData.VERSION_KEY, TagBase.newTag(version.dataVersion().orElse(98)));
        }

        return compound;
    }

    /**
     * Encodes an array of items into a NBT compound.
     *
     * @param items   the array of items to encode
     * @return        an encoded NBT compound
     */
    @NotNull
    public Object encodeContainer(@Nullable ItemStack[] items) {
        return encodeContainer(items, MC.version());
    }

    /**
     * Encodes an array of items into a NBT compound, adding the data version of the specified Minecraft version.
     *
     * @param items   the array of items to encode
     * @param version the Minecraft version to use for the data version
     * @return        an encoded NBT compound
     */
    @NotNull
    public Object encodeContainer(@Nullable ItemStack[] items, @NotNull MC version) {
        final Object compound = TagCompound.newTag();
        if (items == null) {
            return compound;
        }

        TagCompound.set(compound, "size", TagBase.newTag(items.length));

        final List<Object> list = new ArrayList<>();
        for (int slot = 0; slot < items.length; slot++) {
            final Object itemCompound = encodeItem(items[slot], version);
            if (isEmpty(itemCompound)) {
                continue;
            }

            // TODO: Consider changing this key to "slot"
            TagCompound.set(itemCompound, "Slot", TagBase.newTag(slot));
            list.add(itemCompound);
        }

        TagCompound.set(compound, "items", TagList.newUncheckedTag(list));

        return compound;
    }

    /**
     * Updates an item NBT compound from one Minecraft version to another.
     *
     * @param compound   the NBT compound representing the item
     * @param version    the current Minecraft version of the item
     * @param newVersion the target Minecraft version to update the item to
     * @return           the updated NBT compound
     */
    @NotNull
    public abstract Object updateItem(@NotNull Object compound, @NotNull MC version, @NotNull MC newVersion);

    private static boolean isEmpty(@NotNull Object compound) {
        return isEmpty(TagCompound.getValue(compound));
    }

    private static boolean isEmpty(@NotNull Map<String, Object> value) {
        if (value.isEmpty()) {
            return true;
        }
        if (!value.containsKey("id")) {
            return true;
        }

        final Object idTag = TagBase.getValue(value.get("id"));
        if (idTag == null) {
            return true;
        }
        final String id = String.valueOf(idTag);
        return id.equals("air") || id.equals("minecraft:air");
    }

    /**
     * Mojang implementation of the item data fix, using the DataFixerUpper system to update items between different Minecraft versions.
     * For now, this implementation is only suitable for Mojang-mapped server instances.
     */
    @ApiStatus.Experimental
    public static class MojangDataFix extends ItemDataFix {

        public static final MojangDataFix INSTANCE = new MojangDataFix();

        @Override
        public @NotNull Object updateItem(@NotNull Object compound, @NotNull MC version, @NotNull MC newVersion) {
            if (version == newVersion) {
                return compound;
            }
            // TODO: Change this with reflection and also provide data fix for pre-flat server instances
            return MinecraftServer.getServer().getFixerUpper().update(References.ITEM_STACK, new Dynamic<>(NbtOps.INSTANCE, (Tag) compound), version.dataVersion().orElse(99), newVersion.dataVersion().get()).getValue();
        }
    }

    /**
     * Rtag implementation of the item data fix, using the Rtag system to update items between different Minecraft versions.
     */
    @ApiStatus.Experimental
    public static class RtagDataFix extends ItemDataFix {

        public static final RtagDataFix INSTANCE = new RtagDataFix();

        private final Map<MC, ItemTagStream> cache = new HashMap<>();

        @NotNull
        private ItemTagStream stream(@NotNull MC version) {
            if (MC.version() == version) {
                return ItemTagStream.INSTANCE;
            }
            ItemTagStream stream = cache.get(version);
            if (stream == null) {
                stream = ItemTagStream.valueOf(MC.first(), version);
                cache.put(version, stream);
            }
            return stream;
        }

        @Override
        public @NotNull Object updateItem(@NotNull Object compound, @NotNull MC version, @NotNull MC newVersion) {
            if (version == newVersion) {
                return compound;
            }

            stream(newVersion).onLoad(compound, version, newVersion);
            return compound;
        }
    }

    /**
     * A safe item data fix implementation, made specifically to fix invalid items across serialization formats.
     */
    @ApiStatus.Experimental
    public static class SafeDataFix extends ItemDataFix {

        public static final SafeDataFix INSTANCE = new SafeDataFix();

        /**
         * For some reason, since 1.20.5 Mojang didn't clean or fix invalid items that pass the DataFixerUpper,
         * this method aims to fix that in a safe way.
         *
         * @param compound the compound that represent the item.
         * @param version  the version of the item.
         * @return         a fixed item compound.
         */
        @NotNull
        public Object fixItemComponents(@NotNull Object compound, @NotNull MC version) {
            // Fix enchantments with invalid levels, can be produced by:
            // - Plugins that previously use invalid levels to only display glow color on items
            final Object enchantments;
            if (version.isNewerThanOrEquals(MC.V_1_21_5)) {
                enchantments = Rtag.INSTANCE.getExact(compound, "components", "minecraft:enchantments");
            } else {
                enchantments = Rtag.INSTANCE.getExact(compound, "components", "minecraft:enchantments", "levels");
            }
            if (enchantments != null) {
                for (Map.Entry<String, Object> entry : TagCompound.getValue(enchantments).entrySet()) {
                    final Number level = (Number) TagBase.getValue(entry.getValue());
                    if (level.intValue() < 1) {
                        entry.setValue(TagBase.newTag(1));
                    }
                }
            }

            // Fix sub-items
            final Object container = Rtag.INSTANCE.getExact(compound, "components", "minecraft:container");
            if (container != null) {
                for (Object element : TagList.getValue(container)) {
                    for (Map.Entry<String, Object> entry : TagCompound.getValue(element).entrySet()) {
                        if (entry.getKey().equals("item")) {
                            fixItemComponents(entry.getValue(), version);
                        }
                    }
                }
            }

            return compound;
        }

        /**
         * Good-ol data fix for items before 1.20.5.<br>
         * This method provides fixes for a typical bad item encoding.
         *
         * @param compound the compound that represent the item.
         * @param version  the version of the item.
         * @return         a fixed item compound.
         */
        @NotNull
        public Object fixItemTag(@NotNull Object compound, @NotNull MC version) {
            // Fix bad item JSON lore that breaks the method ItemStack#isSimilar() with "identical" items, can be produced by:
            // - Bukkit serialization
            if (version.isNewerThanOrEquals(MC.V_1_14)) {
                final Object tag = Rtag.INSTANCE.getExact(compound, "display", "Lore");
                if (tag != null) {
                    final List<Object> lore = TagList.getValue(tag);
                    for (int i = 0; i < lore.size(); i++) {
                        // StringTag -> component as JSON -> colored string -> fixed component as JSON -> StringTag
                        final String line = (String) TagBase.getValue(lore.get(i));
                        if (ChatComponent.isChatComponent(line)) {
                            lore.set(i, TagBase.newTag(ChatComponent.toJson(ChatComponent.toString(line))));
                        }
                    }
                }
            }

            return compound;
        }

        @Override
        public @NotNull Object updateItem(@NotNull Object compound, @NotNull MC version, @NotNull MC newVersion) {
            Object result;
            if (MC.version().isComponent() && ServerInstance.Type.MOJANG_MAPPED) {
                result = ItemDataFix.mojang().updateItem(compound, version, newVersion);
            } else {
                result = ItemDataFix.rtag().updateItem(compound, version, newVersion);
            }

            if (newVersion.isComponent()) {
                result = fixItemComponents(result, newVersion);
            } else {
                result = fixItemTag(result, newVersion);
            }

            return result;
        }
    }
}
