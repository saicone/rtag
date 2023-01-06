package com.saicone.rtag.item.mirror;

import com.saicone.rtag.item.ItemMirror;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;
import com.saicone.rtag.util.ChatComponent;

/**
 * IDisplayMirror class to convert item display
 * across versions.
 *
 * @author Rubenicos
 */
public class IDisplayMirror implements ItemMirror {

    @Override
    public int getDeprecationVersion() {
        return 14;
    }

    @Override
    public void upgrade(Object compound, String id, Object tag, int from, int to) {
        processDisplay(tag, from, true);
    }

    @Override
    public void downgrade(Object compound, String id, Object tag, int from, int to) {
        processDisplay(tag, to, false);
    }

    /**
     * Process current display inside item tag.
     *
     * @param tag     ItemStack tag.
     * @param version Version to convert.
     * @param toJson  True to convert texts into Json component.
     */
    public void processDisplay(Object tag, int version, boolean toJson) {
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

    /**
     * Process current display lore tag.
     *
     * @param display Display tag.
     * @param toJson  True to convert texts into Json component.
     */
    public void processLore(Object display, boolean toJson) {
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

    /**
     * Process current display name tag.
     * @param display Display tag.
     * @param toJson  True to convert texts into Json component.
     */
    public void processName(Object display, boolean toJson) {
        Object tag = processTag(TagCompound.get(display, "Name"), toJson);
        if (tag != null) {
            TagCompound.set(display, "Name", tag);
        }
    }

    /**
     * Process current NBT tag that contains text inside.
     *
     * @param tag    NBTTagString instance.
     * @param toJson True to convert text into Json component.
     * @return       A new tag with converted text value.
     */
    public Object processTag(Object tag, boolean toJson) {
        Object tagValue = TagBase.getValue(tag);
        if (tagValue != null) {
            String string = String.valueOf(tagValue);
            if (!string.isBlank()) {
                return TagBase.newTag(processString(string, toJson));
            }
        }
        return null;
    }

    /**
     * Process current text and convert into Json component or not.
     *
     * @param string Text to convert.
     * @param toJson True to convert text into Json component.
     * @return       A new converted text.
     */
    public Object processString(String string, boolean toJson) {
        return toJson ? ChatComponent.toJsonOrNull(string) : ChatComponent.toString(string);
    }
}
