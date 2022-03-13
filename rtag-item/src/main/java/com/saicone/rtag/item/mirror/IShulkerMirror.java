package com.saicone.rtag.item.mirror;

import com.saicone.rtag.Rtag;
import com.saicone.rtag.item.ItemMirror;
import com.saicone.rtag.item.ItemTagStream;
import com.saicone.rtag.tag.TagList;

/**
 * IShulkerMirror class to convert items inside
 * shulkers across versions.
 *
 * @author Rubenicos
 */
public class IShulkerMirror implements ItemMirror {

    private final ItemTagStream stream;

    /**
     * Constructs an IShulkerMirror with specified {@link ItemTagStream}
     * to convert loaded items.
     *
     * @param stream ItemTagStream instance.
     */
    public IShulkerMirror(ItemTagStream stream) {
        this.stream = stream;
    }

    @Override
    public int getMinVersion() {
        return 9;
    }

    @Override
    public void upgrade(Object compound, String id, Object tag, int from, int to) throws Throwable {
        if (id.contains("shulker_box")) {
            processTag(tag, from, to);
        }
    }

    @Override
    public void downgrade(Object compound, String id, Object tag, int from, int to) throws Throwable {
        if (id.contains("shulker_box")) {
            processTag(tag, from, to);
        }
    }

    /**
     * Process current shulker tag to convert items inside.
     * @param tag  ItemStack tag.
     * @param from Version specified in compound.
     * @param to   Version to convert.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public void processTag(Object tag, int from, int to) throws Throwable {
        Object items = Rtag.INSTANCE.getExact(tag, "BlockEntityTag", "Items");
        if (items != null) {
            int size = TagList.size(items);
            for (int i = 0; i < size; i++) {
                Object item = TagList.get(items, i);
                stream.onLoad(item, from, to);
            }
        }
    }
}
