package com.saicone.rtag.item;

import com.saicone.rtag.util.ServerInstance;

/**
 * ItemMirror interface to make item NBTTagCompound
 * compatible with actual server version.
 *
 * @author Rubenicos
 */
public interface ItemMirror {

    /**
     * Get the minimum version where compatibility is deprecated.
     *
     * @return A version number.
     */
    default float getDeprecationVersion() {
        return ServerInstance.verNumber + 1;
    }

    /**
     * Get the minimum version compatibility.
     *
     * @return A version number.
     */
    default float getMinVersion() {
        return 8;
    }

    /**
     * Upgrade current NBTTagCompound from lower version.
     *
     * @param compound Item NBTTagCompound.
     * @param id       Item material identifier.
     * @param from     Version specified in compound.
     * @param to       Version to convert.
     */
    default void upgrade(Object compound, String id, float from, float to) {
        // empty default method
    }

    /**
     * Upgrade current NBTTagCompound from lower version.
     *
     * @param compound Item NBTTagCompound.
     * @param id       Item material identifier.
     * @param tag      Item tag.
     * @param from     Version specified in compound.
     * @param to       Version to convert.
     */
    default void upgrade(Object compound, String id, Object tag, float from, float to) {
        // empty default method
    }

    /**
     * Downgrade current NBTTagCompound from upper version.
     *
     * @param compound Item NBTTagCompound.
     * @param id       Item material identifier.
     * @param from     Version specified in compound.
     * @param to       Version to convert.
     */
    default void downgrade(Object compound, String id, float from, float to) {
        // empty default method
    }

    /**
     * Downgrade current NBTTagCompound from upper version.
     *
     * @param compound Item NBTTagCompound.
     * @param id       Item material identifier.
     * @param tag      Item tag.
     * @param from     Version specified in compound.
     * @param to       Version to convert.
     */
    default void downgrade(Object compound, String id, Object tag, float from, float to) {
        // empty default method
    }
}
