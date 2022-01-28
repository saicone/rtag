package com.saicone.rtag.item;

import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.util.EasyLookup;
import com.saicone.rtag.util.ServerInstance;

import java.lang.invoke.MethodHandle;

/**
 * Class to invoke Minecraft ItemStack methods across versions.
 *
 * @author Rubenicos
 */
public class ItemTag {

    private static final MethodHandle getTag;
    private static final MethodHandle setTag;
    private static final MethodHandle save;
    private static final MethodHandle createStack;

    static {
        MethodHandle m1 = null, m2 = null, m3 = null, m4 = null;
        try {
            // Old method names
            String getTag = "getTag", setTag = "setTag", save = "save", createStack = "createStack";
            // New method names
            if (ServerInstance.verNumber >= 13) {
                createStack = "a";
                if (ServerInstance.verNumber >= 18) {
                    getTag = "s";
                    setTag = "c";
                    save = "b";
                }
            }

            m1 = EasyLookup.method("ItemStack", getTag, "NBTTagCompound");
            m2 = EasyLookup.method("ItemStack", setTag, void.class, "NBTTagCompound");
            m3 = EasyLookup.method("ItemStack", save, "NBTTagCompound", "NBTTagCompound");
            if (ServerInstance.verNumber >= 13 || ServerInstance.verNumber <= 10) {
                m4 = EasyLookup.staticMethod("ItemStack", createStack, "ItemStack", "NBTTagCompound");
            } else {
                // (1.11 - 1.12) Only by public constructor
                m4 = EasyLookup.constructor("ItemStack", "NBTTagCompound");
            }
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        getTag = m1; setTag = m2; save = m3; createStack = m4;
    }

    /**
     * Get current NBTTagCompound.
     *
     * @param item ItemStack instance.
     * @return     The NBTTagCompound inside provided item.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static Object getTag(Object item) throws Throwable {
        return getTag.invoke(item);
    }

    /**
     * Overwrite current NBTTagCompound
     *
     * @param item ItemStack instance.
     * @param tag  NBTTagCompound to put into item.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static void setTag(Object item, Object tag) throws Throwable {
        setTag.invoke(item, tag);
    }

    /**
     * Save current NBTTagCompound into new one.
     *
     * @param item ItemStack instance.
     * @return     A NBTTagCompound that represent the item.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static Object saveTag(Object item) throws Throwable {
        return save.invoke(item, TagCompound.newTag());
    }

    /**
     * Create ItemStack from NBTTagCompound.
     *
     * @param compound NBTTagCompound that represent the item.
     * @return         A new ItemStack.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static Object createStack(Object compound) throws Throwable {
        return createStack.invoke(compound);
    }
}
