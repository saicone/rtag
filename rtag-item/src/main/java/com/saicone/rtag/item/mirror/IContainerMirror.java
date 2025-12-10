package com.saicone.rtag.item.mirror;

import com.saicone.rtag.Rtag;
import com.saicone.rtag.item.ItemMirror;
import com.saicone.rtag.item.ItemTagStream;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;
import com.saicone.rtag.util.MC;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

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
    public @NotNull MC getMinimumVersion() {
        return MC.V_1_9;
    }

    @Override
    public void upgrade(@NotNull Object compound, @NotNull String id, @NotNull Object components, @NotNull MC from, @NotNull MC to) {
        if (isContainer(from, id)) {
            processContent(components, from, to);
        }
    }

    @Override
    public void downgrade(@NotNull Object compound, @NotNull String id, @NotNull Object components, @NotNull MC from, @NotNull MC to) {
        if (isContainer(to, id)) {
            processContent(components, from, to);
        }
    }

    private boolean isContainer(@NotNull MC version, @NotNull String id) {
        if (version.isOlderThan(MC.V_1_20_5)) {
            return id.contains("shulker_box");
        } else {
            return CONTAINERS.contains(id);
        }
    }

    /**
     * Process current item content.
     *
     * @param tag  the item components or tag as tag compound object.
     * @param from the initial version of item.
     * @param to   the version to convert its contents.
     */
    @ApiStatus.Internal
    public void processContent(@NotNull Object tag, @NotNull MC from, @NotNull MC to) {
        Object container = Rtag.INSTANCE.getExact(tag, path);
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

    /**
     * Process current item container components to convert items inside.
     *
     * @param components ItemStack components.
     * @param from       Version specified in compound.
     * @param to         Version to convert.
     */
    @Deprecated(since = "1.5.14", forRemoval = true)
    public void processComponents(@NotNull Object components, float from, float to) {
        processContent(components, MC.findReverse(MC::featRevision, from), MC.findReverse(MC::featRevision, to));
    }
}
