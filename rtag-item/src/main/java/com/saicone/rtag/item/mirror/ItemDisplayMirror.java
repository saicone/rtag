package com.saicone.rtag.item.mirror;

import com.saicone.rtag.item.ItemMirror;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;
import com.saicone.rtag.util.ChatComponent;

/**
 * ItemDisplayMirror class to convert item display
 * across versions.
 *
 * @author Rubenicos
 */
public class ItemDisplayMirror implements ItemMirror {

    /**
     * ItemDisplayMirror public instance adapted for current server version.
     */
    public static final ItemDisplayMirror INSTANCE = new ItemDisplayMirror();

    @Override
    public void upgrade(Object compound, Object tag, int from, int to) throws Throwable {
        if (tag != null) {
            processDisplay(tag, from, true);
        }
    }

    @Override
    public void downgrade(Object compound, Object tag, int from, int to) throws Throwable {
        if (tag != null) {
            processDisplay(tag, to, false);
        }
    }

    public void processDisplay(Object tag, int version, boolean toJson) throws Throwable {
        if (version <= 13) {
            Object display = TagCompound.get(tag, "display");
            if (display == null) return;

            // Since 1.14
            // display.Lore = Json text component
            processLore(display, toJson);
            if (version <= 12) {
                // Since 1.13
                // display.Name = Json text component
                processName(display, toJson);
            }
        }
    }

    public void processLore(Object display, boolean toJson) throws Throwable {
        Object displayLore = TagCompound.get(display, "Lore");
        if (displayLore != null) {
            int size = TagList.size(displayLore);
            for (int i = 0; i < size; i++) {
                Object tag = processTag(TagList.get(displayLore, i), toJson);
                if (tag != null) {
                    TagList.set(displayLore, i, tag);
                }
            }
        }
    }

    public void processName(Object display, boolean toJson) throws Throwable {
        Object tag = processTag(TagCompound.get(display, "Name"), toJson);
        if (tag != null) {
            TagCompound.set(display, "Name", tag);
        }
    }

    public Object processTag(Object tag, boolean toJson) throws Throwable {
        Object tagValue = TagBase.getValue(tag);
        if (tagValue != null) {
            String string = String.valueOf(tagValue);
            if (!string.isBlank()) {
                return TagBase.newTag(processString(string, toJson));
            }
        }
        return null;
    }

    public Object processString(String string, boolean toJson) throws Throwable {
        return TagBase.newTag(toJson ? ChatComponent.toJsonOrNull(string) : ChatComponent.toString(string));
    }
}
