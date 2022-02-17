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
    public int getMinVersion() {
        return 17;
    }

    @Override
    public void upgrade(Object compound, String id, Object tag, int from, int to) throws Throwable {
        if (id.equals("bundle")) {
            processTag(tag);
        }
    }

    @Override
    public void downgrade(Object compound, String id, Object tag, int from, int to) throws Throwable {
        if (id.equals("bundle")) {
            processTag(tag);
        }
    }

    /**
     * Process current bundle tag to convert items inside.
     *
     * @param tag ItemStack tag.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public void processTag(Object tag) throws Throwable {
        Object items = TagCompound.get(tag, "Items");
        if (items != null) {
            int size = TagList.size(items);
            for (int i = 0; i < size; i++) {
                Object item = TagList.get(items, i);
                stream.onLoad(item);
            }
        }
    }
}
