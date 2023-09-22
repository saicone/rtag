package com.saicone.rtag.item.mirror;

import com.saicone.rtag.item.ItemMirror;
import com.saicone.rtag.item.ItemTagStream;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;

/**
 * IBundleMirror class to convert items inside
 * bundles across versions.
 *
 * @author Rubenicos
 */
public class IBundleMirror implements ItemMirror {

    private final ItemTagStream stream;

    /**
     * Constructs an IBundleMirror with specified {@link ItemTagStream}
     * to convert loaded items.
     *
     * @param stream ItemTagStream instance.
     */
    public IBundleMirror(ItemTagStream stream) {
        this.stream = stream;
    }

    @Override
    public float getMinVersion() {
        return 17;
    }

    @Override
    public void upgrade(Object compound, String id, Object tag, float from, float to) {
        if (id.equals("minecraft:bundle")) {
            processTag(tag, from, to);
        }
    }

    @Override
    public void downgrade(Object compound, String id, Object tag, float from, float to) {
        if (id.equals("minecraft:bundle")) {
            processTag(tag, from, to);
        }
    }

    /**
     * Process current bundle tag to convert items inside.
     * @param tag  ItemStack tag.
     * @param from Version specified in compound.
     * @param to   Version to convert.
     */
    public void processTag(Object tag, float from, float to) {
        Object items = TagCompound.get(tag, "Items");
        if (items != null) {
            int size = TagList.size(items);
            for (int i = 0; i < size; i++) {
                Object item = TagList.get(items, i);
                stream.onLoad(item, from, to);
            }
        }
    }
}
