package com.saicone.rtag.item;

import com.saicone.rtag.util.MC;
import org.jetbrains.annotations.NotNull;

/**
 * ItemMirror interface to make item NBTTagCompound
 * compatible with actual server version.
 *
 * @author Rubenicos
 */
public interface ItemMirror {

    /**
     * Get the maximum compatible version, exclusive.
     *
     * @return a version.
     */
    default @NotNull MC getMaximumVersion() {
        return MC.last();
    }

    /**
     * Get the minimum compatible version, inclusive.
     *
     * @return a version.
     */
    default @NotNull MC getMinimumVersion() {
        return MC.first();
    }

    /**
     * Upgrade current item tag compound from lower version.
     *
     * @param compound the tag compound that represents item data.
     * @param id       the item material id.
     * @param from     the initial version of item data.
     * @param to       the version to upgrade item data.
     */
    default void upgrade(@NotNull Object compound, @NotNull String id, @NotNull MC from, @NotNull MC to) {
        // empty default method
    }

    /**
     * Upgrade current item tag compound from lower version.
     *
     * @param compound   the tag compound that represents item data.
     * @param id         the item material id.
     * @param components the item components, on older versions this may be the item tag.
     * @param from       the initial version of item data.
     * @param to         the version to upgrade item data.
     */
    default void upgrade(@NotNull Object compound, @NotNull String id, @NotNull Object components, @NotNull MC from, @NotNull MC to) {
        // empty default method
    }

    /**
     * Downgrade current item tag compound from upper version.
     *
     * @param compound the tag compound that represents item data.
     * @param id       the item material id.
     * @param from     the initial version of item data.
     * @param to       the version to downgrade item data.
     */
    default void downgrade(@NotNull Object compound, @NotNull String id, @NotNull MC from, @NotNull MC to) {
        // empty default method
    }

    /**
     * Downgrade current item tag compound from upper version.
     *
     * @param compound   the tag compound that represents item data.
     * @param id         the item material id.
     * @param components the item components, on older versions this may be the item tag.
     * @param from       the initial version of item data.
     * @param to         the version to downgrade item data.
     */
    default void downgrade(@NotNull Object compound, @NotNull String id, @NotNull Object components, @NotNull MC from, @NotNull MC to) {
        // empty default method
    }

    /**
     * Get the minimum version where compatibility is deprecated.
     *
     * @return A version number.
     */
    @Deprecated(since = "1.5.14", forRemoval = true)
    default float getDeprecationVersion() {
        return getMaximumVersion().featRevision();
    }

    /**
     * Get the minimum version compatibility.
     *
     * @return A version number.
     */
    @Deprecated(since = "1.5.14", forRemoval = true)
    default float getMinVersion0() {
        return getMinimumVersion().featRevision();
    }

    /**
     * Upgrade current NBTTagCompound from lower version.
     *
     * @param compound Item NBTTagCompound.
     * @param id       Item material identifier.
     * @param from     Version specified in compound.
     * @param to       Version to convert.
     */
    @Deprecated(since = "1.5.14", forRemoval = true)
    default void upgrade(@NotNull Object compound, @NotNull String id, float from, float to) {
        upgrade(compound, id, MC.findReverse(MC::featRevision, from), MC.findReverse(MC::featRevision, to));
    }

    /**
     * Upgrade current NBTTagCompound from lower version.
     *
     * @param compound   Item NBTTagCompound.
     * @param id         Item material identifier.
     * @param components Item components.
     * @param from       Version specified in compound.
     * @param to         Version to convert.
     */
    @Deprecated(since = "1.5.14", forRemoval = true)
    default void upgrade(@NotNull Object compound, @NotNull String id, @NotNull Object components, float from, float to) {
        upgrade(compound, id, components, MC.findReverse(MC::featRevision, from), MC.findReverse(MC::featRevision, to));
    }

    /**
     * Downgrade current NBTTagCompound from upper version.
     *
     * @param compound Item NBTTagCompound.
     * @param id       Item material identifier.
     * @param from     Version specified in compound.
     * @param to       Version to convert.
     */
    @Deprecated(since = "1.5.14", forRemoval = true)
    default void downgrade(@NotNull Object compound, @NotNull String id, float from, float to) {
        downgrade(compound, id, MC.findReverse(MC::featRevision, from), MC.findReverse(MC::featRevision, to));
    }

    /**
     * Downgrade current NBTTagCompound from upper version.
     *
     * @param compound   Item NBTTagCompound.
     * @param id         Item material identifier.
     * @param components Item components.
     * @param from       Version specified in compound.
     * @param to         Version to convert.
     */
    @Deprecated(since = "1.5.14", forRemoval = true)
    default void downgrade(@NotNull Object compound, @NotNull String id, @NotNull Object components, float from, float to) {
        downgrade(compound, id, components, MC.findReverse(MC::featRevision, from), MC.findReverse(MC::featRevision, to));
    }
}
