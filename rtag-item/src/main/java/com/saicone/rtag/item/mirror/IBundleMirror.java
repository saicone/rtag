package com.saicone.rtag.item.mirror;

import com.saicone.rtag.item.ItemMirror;
import com.saicone.rtag.item.ItemTagStream;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;
import com.saicone.rtag.util.MC;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * IBundleMirror class to convert items inside
 * bundles across versions.
 *
 * @author Rubenicos
 */
public class IBundleMirror implements ItemMirror {

    private final ItemTagStream stream;
    private final String key;

    /**
     * Constructs an IBundleMirror with specified {@link ItemTagStream}
     * to convert loaded items.
     *
     * @param stream ItemTagStream instance.
     */
    public IBundleMirror(@NotNull ItemTagStream stream) {
        this(stream, MC.version().isNewerThanOrEquals(MC.V_1_20_5));
    }

    /**
     * Constructs an IBundleMirror with specified {@link ItemTagStream}
     * to convert loaded items.
     *
     * @param stream    ItemTagStream instance.
     * @param component True if bundle contents component should be used.
     */
    public IBundleMirror(@NotNull ItemTagStream stream, boolean component) {
        this.stream = stream;
        if (component) {
            key = "minecraft:bundle_contents";
        } else {
            key = "Items";
        }
    }

    @Override
    public @NotNull MC getMinimumVersion() {
        return MC.V_1_17;
    }

    @Override
    public void upgrade(@NotNull Object compound, @NotNull String id, @NotNull Object components, @NotNull MC from, @NotNull MC to) {
        if (matches(id, from)) {
            processContent(components, from, to);
        }
    }

    @Override
    public void downgrade(@NotNull Object compound, @NotNull String id, @NotNull Object components, @NotNull MC from, @NotNull MC to) {
        if (matches(id, from)) {
            processContent(components, from, to);
        }
    }

    private boolean matches(@NotNull String id, @NotNull MC from) {
        if (from.isNewerThanOrEquals(MC.V_1_21_2)) {
            return id.endsWith("_bundle");
        }
        return id.equals("minecraft:bundle");
    }

    /**
     * Process current bundle content.
     *
     * @param tag  the item components or tag as tag compound object.
     * @param from the initial version of item.
     * @param to   the version to convert its contents.
     */
    @ApiStatus.Internal
    public void processContent(@NotNull Object tag, @NotNull MC from, @NotNull MC to) {
        Object contents = TagCompound.get(tag, key);
        if (contents != null) {
            final List<Object> items = TagList.getValue(contents);
            for (Object item : items) {
                stream.onLoad(item, from, to);
            }
        }
    }

    /**
     * Process current bundle tag to convert items inside.
     *
     * @param tag  ItemStack tag.
     * @param from Version specified in compound.
     * @param to   Version to convert.
     */
    @Deprecated(since = "1.5.14", forRemoval = true)
    public void processTag(Object tag, float from, float to) {
        processContent(tag, MC.findReverse(MC::featRevision, from), MC.findReverse(MC::featRevision, to));
    }
}
