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
        this(stream, ServerInstance.VERSION >= 20.04f);
    }

    /**
     * Constructs an IBundleMirror with specified {@link ItemTagStream}
     * to convert loaded items.
     *
     * @param stream    ItemTagStream instance.
     * @param component True if bundle contents component should be used.
     */
    public IBundleMirror(ItemTagStream stream, boolean component) {
        this.stream = stream;
        if (component) {
            key = "minecraft:bundle_contents";
        } else {
            key = "Items";
        }
    }

    @Override
    public float getMinVersion() {
        return 17;
    }

    @Override
    public void upgrade(Object compound, String id, Object components, float from, float to) {
        if (matches(id, from)) {
            processTag(components, from, to);
        }
    }

    @Override
    public void downgrade(Object compound, String id, Object components, float from, float to) {
        if (matches(id, from)) {
            processTag(components, from, to);
        }
    }

    private boolean matches(String id, float from) {
        if (from >= 21.02f) {
            return id.endsWith("_bundle");
        }
        return id.equals("minecraft:bundle");
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
