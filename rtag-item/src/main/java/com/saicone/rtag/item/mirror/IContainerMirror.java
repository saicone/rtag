package com.saicone.rtag.item.mirror;

import com.saicone.rtag.Rtag;
import com.saicone.rtag.item.ItemMirror;
import com.saicone.rtag.item.ItemTagStream;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;

import java.util.List;
import java.util.Set;

/**
 * IContainerMirror class to convert items inside
 * item form of container across versions.
 *
 * @author Rubenicos
 */
public class IContainerMirror implements ItemMirror {

    private static final Set<String> CONTAINERS = Set.of(
            "minecraft:chiseled_bookshelf",
            "minecraft:chest",
            "minecraft:furnace",
            "minecraft:ender_chest",
            "minecraft:shulker_box",
            "minecraft:white_shulker_box",
            "minecraft:orange_shulker_box",
            "minecraft:magenta_shulker_box",
            "minecraft:light_blue_shulker_box",
            "minecraft:yellow_shulker_box",
            "minecraft:lime_shulker_box",
            "minecraft:pink_shulker_box",
            "minecraft:gray_shulker_box",
            "minecraft:light_gray_shulker_box",
            "minecraft:cyan_shulker_box",
            "minecraft:purple_shulker_box",
            "minecraft:blue_shulker_box",
            "minecraft:brown_shulker_box",
            "minecraft:green_shulker_box",
            "minecraft:red_shulker_box",
            "minecraft:black_shulker_box",
            "minecraft:hopper",
            "minecraft:dispenser",
            "minecraft:dropper",
            "minecraft:trapped_chest",
            "minecraft:crafter",
            "minecraft:brewing_stand",
            "minecraft:barrel",
            "minecraft:smoker",
            "minecraft:blast_furnace",
            "minecraft:campfire",
            "minecraft:soul_campfire"
    );

    private final ItemTagStream stream;
    private final Object[] path;
    private final boolean slotList;

    /**
     * Constructs an IContainerMirror with specified {@link ItemTagStream}
     * to convert loaded items.
     *
     * @param stream    ItemTagStream instance.
     * @param component True if container component should be used.
     */
    public IContainerMirror(ItemTagStream stream, boolean component) {
        this.stream = stream;
        if (component) {
            path = new Object[] { "minecraft:container" };
            slotList = true;
        } else {
            path = new Object[] { "BlockEntityTag", "Items" };
            slotList = false;
        }
    }

    @Override
    public float getMinVersion() {
        return 9;
    }

    @Override
    public void upgrade(Object compound, String id, Object components, float from, float to) {
        if (isContainer(from, id)) {
            processComponents(components, from, to);
        }
    }

    @Override
    public void downgrade(Object compound, String id, Object components, float from, float to) {
        if (isContainer(to, id)) {
            processComponents(components, from, to);
        }
    }

    private boolean isContainer(float version, String id) {
        if (version <= 20.03f) {
            return id.contains("shulker_box");
        } else {
            return CONTAINERS.contains(id);
        }
    }

    /**
     * Process current item container components to convert items inside.
     *
     * @param components ItemStack components.
     * @param from       Version specified in compound.
     * @param to         Version to convert.
     */
    public void processComponents(Object components, float from, float to) {
        Object container = Rtag.INSTANCE.getExact(components, path);
        if (container != null) {
            final List<Object> items = TagList.getValue(container);
            if (slotList) {
                for (Object slot : items) {
                    Object item = TagCompound.get(slot, "item");
                    stream.onLoad(item, from, to);
                }
            } else {
                for (Object item : items) {
                    stream.onLoad(item, from, to);
                }
            }
        }
    }
}
