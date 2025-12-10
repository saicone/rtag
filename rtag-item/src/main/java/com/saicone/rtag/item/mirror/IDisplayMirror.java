package com.saicone.rtag.item.mirror;

import com.saicone.rtag.item.ItemMirror;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;
import com.saicone.rtag.util.ChatComponent;
import com.saicone.rtag.util.MC;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * IDisplayMirror class to convert item display
 * across versions.
 *
 * @author Rubenicos
 */
public class IDisplayMirror implements ItemMirror {

    private final boolean convertName;

    /**
     * Constructs an IDisplayMirror with default options.
     */
    public IDisplayMirror() {
        this(true);
    }

    /**
     * Constructs an IDisplayMirror with specified name conversion option.
     *
     * @param convertName true to convert names between versions.
     */
    public IDisplayMirror(boolean convertName) {
        this.convertName = convertName;
    }

    @Override
    public @NotNull MC getMaximumVersion() {
        return MC.V_1_14;
    }

    @Override
    public void upgrade(@NotNull Object compound, @NotNull String id, @NotNull Object components, @NotNull MC from, @NotNull MC to) {
        // display.Name to json text component
        if (convertName && to.isNewerThanOrEquals(MC.V_1_13) && from.isOlderThan(MC.V_1_13) && !processName(TagCompound.get(components, "display"), true)) {
            return;
        }
        if (to.isNewerThanOrEquals(MC.V_1_14) && from.isOlderThan(MC.V_1_14)) {
            // display.Lore to json text component
            processLore(TagCompound.get(components, "display"), true);
        }
    }

    @Override
    public void downgrade(@NotNull Object compound, @NotNull String id, @NotNull Object components, @NotNull MC from, @NotNull MC to) {
        // display.Name to legacy text
        if (from.isNewerThanOrEquals(MC.V_1_13) && to.isOlderThan(MC.V_1_13) && !processName(TagCompound.get(components, "display"), false)) {
            return;
        }
        if (from.isNewerThanOrEquals(MC.V_1_14) && to.isOlderThan(MC.V_1_14)) {
            // display.Lore to legacy text
            processLore(TagCompound.get(components, "display"), false);
        }
    }

    /**
     * Process current display lore tag.
     *
     * @param display Display tag.
     * @param toJson  True to convert texts into Json component.
     */
    @ApiStatus.Internal
    public void processLore(@Nullable Object display, boolean toJson) {
        if (display == null) {
            return;
        }
        final Object displayLore = TagCompound.get(display, "Lore");
        if (displayLore != null) {
            final int size = TagList.size(displayLore);
            for (int i = 0; i < size; i++) {
                final Object tag = processTag(TagList.get(displayLore, i), toJson);
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
     * @return        true if the display tag was processed.
     */
    @ApiStatus.Internal
    public boolean processName(@Nullable Object display, boolean toJson) {
        if (display == null) {
            return false;
        }
        final Object tag = processTag(TagCompound.get(display, "Name"), toJson);
        if (tag != null) {
            TagCompound.set(display, "Name", tag);
        }
        return true;
    }

    /**
     * Process current NBT tag that contains text inside.
     *
     * @param tag    NBTTagString instance.
     * @param toJson True to convert text into Json component.
     * @return       A new tag with converted text value.
     */
    @Nullable
    @ApiStatus.Internal
    public Object processTag(@Nullable Object tag, boolean toJson) {
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
    @Nullable
    @ApiStatus.Internal
    public Object processString(@Nullable String string, boolean toJson) {
        return toJson ? ChatComponent.toJsonOrNull(string) : ChatComponent.toString(string);
    }
}
