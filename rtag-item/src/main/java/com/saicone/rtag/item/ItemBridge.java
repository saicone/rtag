package com.saicone.rtag.item;

import com.saicone.rtag.util.EasyLookup;
import org.bukkit.inventory.ItemStack;

import java.lang.invoke.MethodHandle;

/**
 * Class to invoke CraftItemStack methods across versions.
 *
 * @author Rubenicos
 */
public class ItemBridge {

    private static final Class<?> itemStack = EasyLookup.classById("ItemStack");

    private static final MethodHandle asBukkitCopy;
    private static final MethodHandle asNMSCopy;

    static {
        MethodHandle m1 = null, m2 = null;
        try {
            // Minecraft -> Bukkit
            m1 = EasyLookup.staticMethod("CraftItemStack", "asBukkitCopy", ItemStack.class, "ItemStack");
            // Bukkit -> Minecraft
            m2 = EasyLookup.staticMethod("CraftItemStack", "asNMSCopy", "ItemStack", ItemStack.class);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        asBukkitCopy = m1; asNMSCopy = m2;
    }

    /**
     * Get provided Minecraft ItemStack and convert into Bukkit ItemStack.
     *
     * @param item Minecraft ItemStack.
     * @return     Bukkit ItemStack.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static ItemStack asBukkit(Object item) throws Throwable {
        if (itemStack.isInstance(item)) {
            return (ItemStack) asBukkitCopy.invoke(item);
        } else {
            return null;
        }
    }

    /**
     * Get provided Bukkit ItemStack and convert into Minecraft ItemStack.
     *
     * @param item Bukkit ItemStack.
     * @return     Minecraft ItemStack.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static Object asMinecraft(ItemStack item) throws Throwable {
        return asNMSCopy.invoke(item);
    }
}
