package com.saicone.rtag.item.mirror;

import com.saicone.rtag.item.ItemMirror;
import com.saicone.rtag.item.ItemTagStream;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;
import com.saicone.rtag.util.ServerInstance;

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
    public IBundleMirror(ItemTagStream stream) {
        this(stream, ServerInstance.VERSION);
    }

    /**
     * Constructs an IBundleMirror with specified {@link ItemTagStream}
     * to convert loaded items.
     *
     * @param stream  ItemTagStream instance.
     * @param version The current version to apply any conversion.
     */
    public IBundleMirror(ItemTagStream stream, float version) {
        this.stream = stream;
        if (version <= 20.03f) {
            key = "Items";
        } else {
            key = "minecraft:bundle_contents";
        }
    }

    @Override
    public float getMinVersion() {
        return 17;
    }

    @Override
    public void upgrade(Object compound, String id, Object components, float from, float to) {
        if (id.equals("minecraft:bundle")) {
            processTag(components, from, to);
        }
    }

    @Override
    public void downgrade(Object compound, String id, Object components, float from, float to) {
        if (id.equals("minecraft:bundle")) {
            processTag(components, from, to);
        }
    }

    /**
     * Process current bundle tag to convert items inside.
     * @param tag  ItemStack tag.
     * @param from Version specified in compound.
     * @param to   Version to convert.
     */
    public void processTag(Object tag, float from, float to) {
        Object contents = TagCompound.get(tag, key);
        if (contents != null) {
            final List<Object> items = TagList.getValue(contents);
            for (Object item : items) {
                stream.onLoad(item, from, to);
            }
        }
    }
}
