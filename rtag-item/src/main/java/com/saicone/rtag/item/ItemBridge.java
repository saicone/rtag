package com.saicone.rtag.item;

import com.saicone.rtag.util.EasyLookup;
import com.saicone.rtag.util.ServerInstance;
import org.bukkit.inventory.ItemStack;

import java.lang.invoke.MethodHandle;

/**
 * Class to invoke CraftItemStack methods across versions.
 *
 * @author Rubenicos
 */
public class ItemBridge {

    private static final Class<?> itemStack = EasyLookup.classById("ItemStack");
    private static final Class<?> craftItemStack = EasyLookup.classById("CraftItemStack");

    private static final MethodHandle asBukkitCopy;
    private static final MethodHandle asNMSCopy;
    private static final MethodHandle getHandle;
    private static final MethodHandle setHandle;

    static {
        MethodHandle m1 = null, m2 = null, m3 = null, m4 = null;
        try {
            // Minecraft -> Bukkit
            m1 = EasyLookup.staticMethod("CraftItemStack", "asBukkitCopy", ItemStack.class, "ItemStack");
            // Bukkit -> Minecraft
            m2 = EasyLookup.staticMethod("CraftItemStack", "asNMSCopy", "ItemStack", ItemStack.class);

            // Private field
            m3 = EasyLookup.unreflectGetter("CraftItemStack", "handle");
            m4 = EasyLookup.unreflectSetter("CraftItemStack", "handle");
        } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        asBukkitCopy = m1;
        asNMSCopy = m2;
        getHandle = m3;
        setHandle = m4;
    }

    ItemBridge() {
    }

    /**
     * Convert Minecraft ItemStack into Bukkit ItemStack.<br>
     * Take in count this method creates a new ItemStack instance.
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
     * Convert Bukkit ItemStack into Minecraft ItemStack.<br>
     * Take in count this method copy the original ItemStack
     *
     * @param item Bukkit ItemStack.
     * @return     Minecraft ItemStack.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static Object asMinecraft(ItemStack item) throws Throwable {
        return asNMSCopy.invoke(item);
    }

    /**
     * Get defined Minecraft ItemStack inside CraftItemStack or
     * create new one.
     *
     * @param item Bukkit ItemStack.
     * @return     Minecraft ItemStack.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static Object getHandle(ItemStack item) throws Throwable {
        if (craftItemStack.isInstance(item)) {
            return getHandle.invoke(item);
        } else {
            return asMinecraft(item);
        }
    }

    /**
     * Replace defined Minecraft ItemStack inside CraftItemStack or
     * load changes into Bukkit ItemStack.
     *
     * @param item   Bukkit ItemStack.
     * @param handle Minecraft ItemStack.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    @SuppressWarnings("deprecation")
    public static void setHandle(ItemStack item, Object handle) throws Throwable {
        if (itemStack.isInstance(handle)) {
            if (craftItemStack.isInstance(item)) {
                setHandle.invoke(item, handle);
            } else {
                ItemStack copy = asBukkit(handle);
                if (copy != null) {
                    item.setType(copy.getType());
                    item.setAmount(copy.getAmount());
                    if (ServerInstance.isLegacy) {
                        item.setDurability(copy.getDurability());
                    }
                    item.setItemMeta(copy.getItemMeta());
                }
            }
        }
    }
}
