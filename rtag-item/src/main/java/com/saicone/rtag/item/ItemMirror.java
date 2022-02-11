package com.saicone.rtag.item;

/**
 * ItemMirror interface to make item NBTTagCompound
 * compatible with actual server version.
 *
 * @author Rubenicos
 */
public interface ItemMirror {

    /**
     * Upgrade current NBTTagCompound from lower version.
     *
     * @param compound Item NBTTagCompound.
     * @param tag      Item tag.
     * @param from     Version specified in compound.
     * @param to       Current server version.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    default void upgrade(Object compound, Object tag, int from, int to) throws Throwable { }

    /**
     * Downgrade current NBTTagCompound from upper version.
     *
     * @param compound Item NBTTagCompound.
     * @param tag      Item tag.
     * @param from     Version specified in compound.
     * @param to       Current server version.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    default void downgrade(Object compound, Object tag, int from, int to) throws Throwable { }
}
