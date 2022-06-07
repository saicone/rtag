package com.saicone.rtag.block;

import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.util.EasyLookup;
import com.saicone.rtag.util.ServerInstance;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.lang.invoke.MethodHandle;

/**
 * Class to invoke Block/Tile methods across versions.
 *
 * @author Rubenicos
 */
public class BlockObject {

    private static final Class<?> BLOCK_STATE = EasyLookup.classById("CraftBlockState");

    private static final MethodHandle newBlockPosition;
    private static final MethodHandle getTileEntity;
    private static final MethodHandle getHandle;
    // Only 1.16
    private static final MethodHandle getPosition;
    private static final MethodHandle getWorld;
    private static final MethodHandle getType;

    private static final MethodHandle save;
    private static final MethodHandle load;

    static {
        // Constructors
        MethodHandle new$BlockPosition = null;
        // Methods
        MethodHandle method$getTileEntity = null;
        MethodHandle method$getHandle = null;
        MethodHandle method$getPosition = null;
        MethodHandle method$getWorld = null;
        MethodHandle method$getType = null;
        MethodHandle method$save = null;
        MethodHandle method$load = null;
        try {
            // Old method names
            String getTileEntity = "getTileEntity";
            String save = "b";
            String load = "a";
            // New method names
            if (ServerInstance.verNumber >= 18) {
                getTileEntity = "c_";
                save = "m";
            } else if (ServerInstance.verNumber >= 9) {
                save = "save";
                if (ServerInstance.verNumber >= 12) {
                    load = "load";
                }
            }

            new$BlockPosition = EasyLookup.constructor("BlockPosition", int.class, int.class, int.class);
            method$getTileEntity = EasyLookup.method("World", getTileEntity, "TileEntity", "BlockPosition");
            method$getHandle = EasyLookup.method("CraftWorld", "getHandle", "WorldServer");

            if (ServerInstance.verNumber >= 18) {
                method$save = EasyLookup.method("TileEntity", save, "NBTTagCompound");
            } else {
                // Unreflect reason:
                // (1.8) void method
                // Other versions return NBTTagCompound
                method$save = EasyLookup.unreflectMethod("TileEntity", save, "NBTTagCompound");
            }

            if (ServerInstance.verNumber == 16) {
                method$getPosition = EasyLookup.method("TileEntity", "getPosition", "BlockPosition");
                method$getWorld = EasyLookup.method("TileEntity", "getWorld", "World");
                method$getType = EasyLookup.method("World", "getType", "IBlockData", "BlockPosition");

                method$load = EasyLookup.method("TileEntity", load, void.class, "IBlockData", "NBTTagCompound");
            } else {
                method$load = EasyLookup.method("TileEntity", load, void.class, "NBTTagCompound");
            }
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        newBlockPosition = new$BlockPosition;
        getTileEntity = method$getTileEntity;
        getHandle = method$getHandle;
        getPosition = method$getPosition;
        getWorld = method$getWorld;
        getType = method$getType;
        save = method$save;
        load = method$load;
    }

    BlockObject() {
    }

    /**
     * Get provided Bukkit Block and convert into Minecraft TileEntity.
     *
     * @param block Block to convert.
     * @return      A Minecraft TileEntity.
     * @throws IllegalArgumentException if block state is not a CraftBlockState.
     * @throws RuntimeException         if any error occurs on reflected method invoking.
     */
    public static Object getTileEntity(Block block) throws IllegalArgumentException, RuntimeException {
        if (BLOCK_STATE.isInstance(block.getState())) {
            Location loc = block.getLocation();
            try {
                return getTileEntity.invoke(getHandle.invoke(loc.getWorld()), newBlockPosition.invoke(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
            } catch (Throwable t) {
                throw new RuntimeException("Cannot convert Bukkit Block into Minecraft TileEntity", t);
            }
        } else {
            throw new IllegalArgumentException("The provided block state isn't a CraftBlockState");
        }
    }

    /**
     * Save Minecraft TileEntity into new NBTTagCompound.
     *
     * @param tile TileEntity instance.
     * @return     A NBTTagCompound that represent the tile.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static Object save(Object tile) throws Throwable {
        if (ServerInstance.verNumber >= 18) {
            return save.invoke(tile);
        } else if (ServerInstance.verNumber >= 9) {
            return save.invoke(tile, TagCompound.newTag());
        } else {
            Object tag = TagCompound.newTag();
            save.invoke(tile, tag);
            return tag;
        }
    }

    /**
     * Load NBTTagCompound into Minecraft TileEntity.
     *
     * @param tile TileEntity instance.
     * @param tag  The NBTTagCompound to load.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static void load(Object tile, Object tag) throws Throwable {
        if (ServerInstance.verNumber == 16) {
            Object blockData = getType.invoke(getWorld.invoke(tile), getPosition.invoke(tile));
            load.invoke(tile, tag, blockData);
        } else {
            load.invoke(tile, tag);
        }
    }
}
