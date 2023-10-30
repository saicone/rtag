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
            if (ServerInstance.isMojangMapped) {
                createStack = "of";
                load = "load";
            } else if (ServerInstance.verNumber >= 11) {
                load = "load";
                if (ServerInstance.verNumber >= 13) {
                    createStack = "a";
                    if (ServerInstance.verNumber >= 18) {
                        save = "b";
                        setTag = "c";
                        if (ServerInstance.verNumber >= 20) {
                            getTag = "v";
                        } else if (ServerInstance.verNumber >= 19) {
                            getTag = "u";
                        } else {
                            getTag = ServerInstance.release >= 2 ? "t" : "s";
                        }
                    }
                }
            }

            if (ServerInstance.verNumber >= 13 || ServerInstance.verNumber <= 10) {
                new$ItemStack = EasyLookup.staticMethod(MC_ITEM, createStack, "ItemStack", "NBTTagCompound");
            } else {
                // (1.11 - 1.12) Only by public constructor
                new$ItemStack = EasyLookup.constructor(MC_ITEM, "NBTTagCompound");
            }

            // Private field
            get$handle = EasyLookup.getter(CRAFT_ITEM, "handle", MC_ITEM);
            set$handle = EasyLookup.setter(CRAFT_ITEM, "handle", MC_ITEM);

            method$save = EasyLookup.method(MC_ITEM, save, "NBTTagCompound", "NBTTagCompound");
            // Private method
            method$load = EasyLookup.method(MC_ITEM, load, void.class, "NBTTagCompound");
            method$getTag = EasyLookup.method(MC_ITEM, getTag, "NBTTagCompound");
            method$setTag = EasyLookup.method(MC_ITEM, setTag, void.class, "NBTTagCompound");
            method$asBukkitCopy = EasyLookup.staticMethod(CRAFT_ITEM, "asBukkitCopy", ItemStack.class, "ItemStack");
            // Bukkit -> Minecraft
            method$asNMSCopy = EasyLookup.staticMethod(CRAFT_ITEM, "asNMSCopy", "ItemStack", ItemStack.class);
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
     */
    public static Object newItem(Object compound) {
        try {
            return newItem.invoke(compound);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Check if the provided object is instance of Minecraft ItemStack.
     *
     * @param object the object to check.
     * @return       true if the object is an instance of Minecraft ItemStack.
     */
    public static boolean isMinecraftItem(Object object) {
        return MC_ITEM.isInstance(object);
    }

    /**
     * Check if the provided object is instance of CraftItemStack.
     *
     * @param object the object to check.
     * @return       true if the object is an instance of CraftItemStack.
     */
    public static boolean isCraftItem(Object object) {
        return CRAFT_ITEM.isInstance(object);
    }

    /**
     * Save current Item tag into new NBTTagCompound.
     *
     * @param item ItemStack instance.
     * @return     A NBTTagCompound that represent the item.
     */
    public static Object save(Object item) {
        final Object compound =  TagCompound.newTag();
        try {
            return item == null ? compound : save.invoke(item, compound);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Load NBTTagCompound into ItemStack.
     *
     * @param item     ItemStack instance.
     * @param compound The NBTTagCompound to load.
     */
    public static void load(Object item, Object compound) {
        try {
            load.invoke(item, compound);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Get defined Minecraft ItemStack inside CraftItemStack or
     * create new one.
     *
     * @param item Bukkit ItemStack.
     * @return     Minecraft ItemStack.
     */
    public static Object getHandle(ItemStack item) {
        if (CRAFT_ITEM.isInstance(item)) {
            try {
                return getHandleField.invoke(item);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        } else {
            return asNMSCopy(item);
        }
    }

    /**
     * Get current NBTTagCompound.
     *
     * @param item ItemStack instance.
     * @return     The NBTTagCompound inside provided item.
     */
    public static Object getTag(Object item) {
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
     */
    @SuppressWarnings("deprecation")
    public static void setHandle(ItemStack item, Object handle) {
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
    }

    /**
     * Overwrite current NBTTagCompound
     *
     * @param item ItemStack instance.
     * @param tag  NBTTagCompound to put into item.
     */
    public static void setTag(Object item, Object tag) {
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
     */
    public static ItemStack asBukkitCopy(Object item) {
        try {
            return (ItemStack) asBukkitCopy.invoke(item);
        } catch (Throwable t) {
            throw new RuntimeException("Cannot convert Minecraft ItemStack into Bukkit ItemStack", t);
        }
    }

    /**
     * Convert Bukkit ItemStack into Minecraft ItemStack.<br>
     * Take in count this method copy the original ItemStack
     *
     * @param item Bukkit ItemStack.
     * @return     Minecraft ItemStack.
     */
    public static Object asNMSCopy(ItemStack item) {
        try {
            return asNMSCopy.invoke(item);
        } catch (Throwable t) {
            throw new RuntimeException("Cannot convert Bukkit ItemStack into Minecraft ItemStack", t);
        }
    }
}
