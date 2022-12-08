package com.saicone.rtag.item;

import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.util.EasyLookup;
import com.saicone.rtag.util.ServerInstance;
import org.bukkit.inventory.ItemStack;

import java.lang.invoke.MethodHandle;

/**
 * Class to invoke ItemStack methods across versions.
 *
 * @author Rubenicos
 */
public class ItemObject {

    private static final Class<?> MC_ITEM = EasyLookup.classById("ItemStack");
    private static final Class<?> CRAFT_ITEM = EasyLookup.classById("CraftItemStack");

    private static final MethodHandle newItem;
    private static final MethodHandle getHandleField;
    private static final MethodHandle setHandleField;
    private static final MethodHandle save;
    private static final MethodHandle load;
    private static final MethodHandle getTag;
    private static final MethodHandle setTag;
    private static final MethodHandle asBukkitCopy;
    private static final MethodHandle asNMSCopy;

    static {
        // Constructors
        MethodHandle new$ItemStack = null;
        // Getters
        MethodHandle get$handle = null;
        // Setters
        MethodHandle set$handle = null;
        // Methods
        MethodHandle method$save = null;
        MethodHandle method$load = null;
        MethodHandle method$getTag = null;
        MethodHandle method$setTag = null;
        MethodHandle method$asBukkitCopy = null;
        MethodHandle method$asNMSCopy = null;
        try {
            // Old method names
            String createStack = "createStack";
            String save = "save";
            String load = "c";
            String getTag = "getTag";
            String setTag = "setTag";

            // New method names
            if (ServerInstance.verNumber >= 11) {
                load = "load";
                if (ServerInstance.verNumber >= 13) {
                    createStack = "a";
                    if (ServerInstance.verNumber >= 18) {
                        save = "b";
                        setTag = "c";
                        if (ServerInstance.verNumber >= 19) {
                            getTag = "u";
                        } else {
                            getTag = ServerInstance.release >= 2 ? "t" : "s";
                        }
                    }
                }
            }

            if (ServerInstance.verNumber >= 13 || ServerInstance.verNumber <= 10) {
                new$ItemStack = EasyLookup.staticMethod("ItemStack", createStack, "ItemStack", "NBTTagCompound");
            } else {
                // (1.11 - 1.12) Only by public constructor
                new$ItemStack = EasyLookup.constructor("ItemStack", "NBTTagCompound");
            }

            // Private field
            get$handle = EasyLookup.unreflectGetter("CraftItemStack", "handle");
            set$handle = EasyLookup.unreflectSetter("CraftItemStack", "handle");

            method$save = EasyLookup.method("ItemStack", save, "NBTTagCompound", "NBTTagCompound");
            // Private method
            method$load = EasyLookup.unreflectMethod("ItemStack", load, "NBTTagCompound");
            method$getTag = EasyLookup.method("ItemStack", getTag, "NBTTagCompound");
            method$setTag = EasyLookup.method("ItemStack", setTag, void.class, "NBTTagCompound");
            method$asBukkitCopy = EasyLookup.staticMethod("CraftItemStack", "asBukkitCopy", ItemStack.class, "ItemStack");
            // Bukkit -> Minecraft
            method$asNMSCopy = EasyLookup.staticMethod("CraftItemStack", "asNMSCopy", "ItemStack", ItemStack.class);
        } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        newItem = new$ItemStack;
        getHandleField = get$handle;
        setHandleField = set$handle;
        save = method$save;
        load = method$load;
        getTag = method$getTag;
        setTag = method$setTag;
        asBukkitCopy = method$asBukkitCopy;
        asNMSCopy = method$asNMSCopy;
    }

    ItemObject() {
    }

    /**
     * Create ItemStack from NBTTagCompound.
     *
     * @param compound NBTTagCompound that represent the item.
     * @return         A new ItemStack.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static Object newItem(Object compound) throws Throwable {
        return newItem.invoke(compound);
    }

    /**
     * Save current Item tag into new NBTTagCompound.
     *
     * @param item ItemStack instance.
     * @return     A NBTTagCompound that represent the item.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static Object save(Object item) throws Throwable {
        Object compound =  TagCompound.newTag();
        return item == null ? compound : save.invoke(item, compound);
    }

    /**
     * Load NBTTagCompound into ItemStack.
     *
     * @param item     ItemStack instance.
     * @param compound The NBTTagCompound to load.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static void load(Object item, Object compound) throws Throwable {
        load.invoke(item, compound);
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
        if (CRAFT_ITEM.isInstance(item)) {
            return getHandleField.invoke(item);
        } else {
            return asNMSCopy(item);
        }
    }

    /**
     * Get current NBTTagCompound.
     *
     * @param item ItemStack instance.
     * @return     The NBTTagCompound inside provided item.
     * @throws RuntimeException if any error occurs on reflected method invoking.
     */
    public static Object getTag(Object item) throws RuntimeException {
        try {
            return getTag.invoke(item);
        } catch (Throwable t) {
            throw new RuntimeException("Cannot get tag from Minecraft ItemStack", t);
        }
    }

    /**
     * Replace defined Minecraft ItemStack inside CraftItemStack or
     * load changes into Bukkit ItemStack.
     *
     * @param item   Bukkit ItemStack.
     * @param handle Minecraft ItemStack.
     * @throws IllegalArgumentException if handle is not a Minecraft ItemStack.
     * @throws RuntimeException         if any error occurs on reflected method invoking.
     */
    @SuppressWarnings("deprecation")
    public static void setHandle(ItemStack item, Object handle) throws IllegalArgumentException, RuntimeException {
        if (MC_ITEM.isInstance(handle)) {
            if (CRAFT_ITEM.isInstance(item)) {
                try {
                    setHandleField.invoke(item, handle);
                } catch (Throwable t) {
                    throw new RuntimeException("Cannot set handle into CraftItemStack", t);
                }
            } else {
                ItemStack copy = asBukkitCopy(handle);
                if (copy != null) {
                    item.setType(copy.getType());
                    item.setAmount(copy.getAmount());
                    if (ServerInstance.isLegacy) {
                        item.setDurability(copy.getDurability());
                    }
                    item.setItemMeta(copy.getItemMeta());
                }
            }
        } else {
            throw new IllegalArgumentException("The provided object isn't a Minecraft itemStack");
        }
    }

    /**
     * Overwrite current NBTTagCompound
     *
     * @param item ItemStack instance.
     * @param tag  NBTTagCompound to put into item.
     * @throws RuntimeException if any error occurs on reflected method invoking.
     */
    public static void setTag(Object item, Object tag) throws RuntimeException {
        try {
            setTag.invoke(item, tag);
        } catch (Throwable t) {
            throw new RuntimeException("Cannot set tag to Minecraft ItemStack", t);
        }
    }

    /**
     * Convert Minecraft ItemStack into Bukkit ItemStack.<br>
     * Take in count this method creates a new ItemStack instance.
     *
     * @param item Minecraft ItemStack.
     * @return     Bukkit ItemStack.
     * @throws IllegalArgumentException if item is not a Minecraft ItemStack.
     * @throws RuntimeException         if any error occurs on reflected method invoking.
     */
    public static ItemStack asBukkitCopy(Object item) throws IllegalArgumentException, RuntimeException {
        if (MC_ITEM.isInstance(item)) {
            try {
                return (ItemStack) asBukkitCopy.invoke(item);
            } catch (Throwable t) {
                throw new RuntimeException("Cannot convert Minecraft ItemStack into Bukkit ItemStack", t);
            }
        } else {
            throw new IllegalArgumentException("The provided object isn't a Minecraft itemStack");
        }
    }

    /**
     * Convert Bukkit ItemStack into Minecraft ItemStack.<br>
     * Take in count this method copy the original ItemStack
     *
     * @param item Bukkit ItemStack.
     * @return     Minecraft ItemStack.
     * @throws RuntimeException if any error occurs on reflected method invoking.
     */
    public static Object asNMSCopy(ItemStack item) throws RuntimeException {
        try {
            return asNMSCopy.invoke(item);
        } catch (Throwable t) {
            throw new RuntimeException("Cannot convert Bukkit ItemStack into Minecraft ItemStack", t);
        }
    }
}
