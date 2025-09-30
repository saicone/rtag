package com.saicone.rtag.block;

import com.saicone.rtag.Rtag;
import com.saicone.rtag.registry.IOValue;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.util.EasyLookup;
import com.saicone.rtag.util.ProblemReporter;
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

    // Import reflected classes
    static {
        try {
            EasyLookup.addNMSClass("core.BlockPosition", "BlockPos");
            EasyLookup.addNMSClass("server.level.WorldServer", "ServerLevel");
            EasyLookup.addNMSClass("world.level.World", "Level");
            EasyLookup.addNMSClass("world.level.block.entity.TileEntity", "BlockEntity");
            EasyLookup.addNMSClass("world.level.block.state.IBlockData", "BlockState");
            if (ServerInstance.MAJOR_VERSION >= 16) {
                EasyLookup.addNMSClass("core.IRegistryCustom", "RegistryAccess");
                EasyLookup.addNMSClass("world.level.IWorldReader", "LevelReader");
            }

            EasyLookup.addOBCClass("CraftWorld");
            EasyLookup.addOBCClass("block.CraftBlockState");

            if (ServerInstance.VERSION >= 21.05f) { // 1.21.6
                EasyLookup.addNMSClass("world.level.storage.ValueInput");
                EasyLookup.addNMSClass("world.level.storage.ValueOutput");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static final Class<?> BLOCK_STATE = EasyLookup.classById("CraftBlockState");
    private static final Class<?> TILE_ENTITY = EasyLookup.classById("TileEntity");

    private static final MethodHandle newBlockPosition;
    private static final MethodHandle getTileEntity;
    private static final MethodHandle getHandle;
    // Only 1.16
    private static final MethodHandle getPosition;
    private static final MethodHandle getWorld; // Also +1.20.5
    private static final MethodHandle getType;
    // Only +1.20.5
    private static final MethodHandle getRegistry;

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
        MethodHandle method$getRegistry = null;
        MethodHandle method$save = null;
        MethodHandle method$load = null;
        try {
            // Old method names
            String getTileEntity = "getTileEntity";
            String save = "b";
            String load = "a";
            String getWorld = "i";
            String getRegistry = "H_";

            // New method names
            if (ServerInstance.Type.MOJANG_MAPPED) {
                getTileEntity = "getBlockEntity";
                save = "saveWithoutMetadata";
                load = "load";
                getWorld = "getLevel";
                getRegistry = "registryAccess";
                if (ServerInstance.Release.COMPONENT) {
                    load = "loadWithComponents";
                }
            } else {
                if (ServerInstance.MAJOR_VERSION >= 9) {
                    save = "save";
                }
                if (ServerInstance.MAJOR_VERSION >= 12) {
                    load = "load";
                }
                if (ServerInstance.MAJOR_VERSION >= 18) {
                    getTileEntity = "c_";
                    save = "m";
                    load = "a";
                }
                if (ServerInstance.VERSION >= 19.03) { // 1.19.4
                    save = "o";
                }
                if (ServerInstance.Release.COMPONENT) {
                    save = "d";
                    load = "c";
                }
                if (ServerInstance.VERSION >= 21.02f) { // 1.21.2
                    getRegistry = "K_";
                }
                if (ServerInstance.VERSION >= 21.04f) { // 1.21.5
                    getRegistry = "J_";
                }
                if (ServerInstance.VERSION >= 21.05f) { // 1.21.6
                    save = "e";
                    load = "b";
                    getRegistry = "K_";
                }
                if (ServerInstance.VERSION >= 21.06f) { // 1.21.9
                    getWorld = "j";
                    getRegistry = "L_";
                }
            }

            new$BlockPosition = EasyLookup.constructor("BlockPosition", int.class, int.class, int.class);
            method$getTileEntity = EasyLookup.method("World", getTileEntity, "TileEntity", "BlockPosition");
            method$getHandle = EasyLookup.method("CraftWorld", "getHandle", "WorldServer");

            if (ServerInstance.Release.COMPONENT) {
                method$getWorld = EasyLookup.method(TILE_ENTITY, getWorld, "World");
                method$getRegistry = EasyLookup.method("IWorldReader", getRegistry, "IRegistryCustom");
            }

            if (ServerInstance.Release.COMPONENT) {
                if (ServerInstance.VERSION >= 21.05f) { // 1.21.6
                    method$save = EasyLookup.method(TILE_ENTITY, save, void.class, "ValueOutput");
                } else {
                    method$save = EasyLookup.method(TILE_ENTITY, save, "NBTTagCompound", "HolderLookup.Provider");
                }
            } else if (ServerInstance.MAJOR_VERSION >= 18) {
                method$save = EasyLookup.method(TILE_ENTITY, save, "NBTTagCompound");
            } else {
                // (1.8) void method
                method$save = EasyLookup.method(TILE_ENTITY, save, "NBTTagCompound", "NBTTagCompound");
            }

            if (ServerInstance.VERSION >= 21.05f) { // 1.21.6
                method$load = EasyLookup.method(TILE_ENTITY, load, void.class, "ValueInput");
            } else if (ServerInstance.Release.COMPONENT) {
                method$load = EasyLookup.method(TILE_ENTITY, load, void.class, "NBTTagCompound", "HolderLookup.Provider");
            } else if (ServerInstance.MAJOR_VERSION == 16) {
                method$getPosition = EasyLookup.method(TILE_ENTITY, "getPosition", "BlockPosition");
                method$getWorld = EasyLookup.method(TILE_ENTITY, "getWorld", "World");
                method$getType = EasyLookup.method("World", "getType", "IBlockData", "BlockPosition");

                method$load = EasyLookup.method(TILE_ENTITY, load, void.class, "IBlockData", "NBTTagCompound");
            } else {
                method$load = EasyLookup.method(TILE_ENTITY, load, void.class, "NBTTagCompound");
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
        getRegistry = method$getRegistry;
        save = method$save;
        load = method$load;
    }

    BlockObject() {
    }

    /**
     * Check if the provided object is instance of Minecraft TileEntity.
     *
     * @param object the object to check.
     * @return       true if the object is an instance of Minecraft TileEntity.
     */
    public static boolean isTileEntity(Object object) {
        return TILE_ENTITY.isInstance(object);
    }

    /**
     * Get provided Bukkit Block and convert into Minecraft TileEntity.
     *
     * @param block Block to convert.
     * @return      A Minecraft TileEntity.
     * @throws IllegalArgumentException if block state is not a CraftBlockState.
     */
    public static Object getTileEntity(Block block) throws IllegalArgumentException {
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
     */
    public static Object save(Object tile) {
        try {
            if (ServerInstance.Release.COMPONENT) {
                final Object world = getWorld.invoke(tile);
                final Object registry = world != null ? getRegistry.invoke(world) : Rtag.getMinecraftRegistry();
                if (ServerInstance.VERSION >= 21.05f) { // 1.21.6
                    final Object output = IOValue.createOutput(ProblemReporter.DISCARDING, registry);
                    save.invoke(tile, output);
                    return IOValue.result(output);
                } else {
                    return save.invoke(tile, registry);
                }
            } else if (ServerInstance.MAJOR_VERSION >= 18) {
                return save.invoke(tile);
            } else if (ServerInstance.MAJOR_VERSION >= 9) {
                return save.invoke(tile, TagCompound.newTag());
            } else {
                Object tag = TagCompound.newTag();
                save.invoke(tile, tag);
                return tag;
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Load NBTTagCompound into Minecraft TileEntity.
     *
     * @param tile TileEntity instance.
     * @param tag  The NBTTagCompound to load.
     */
    public static void load(Object tile, Object tag) {
        try {
            if (ServerInstance.Release.COMPONENT) {
                final Object world = getWorld.invoke(tile);
                final Object registry = world != null ? getRegistry.invoke(world) : Rtag.getMinecraftRegistry();
                if (ServerInstance.VERSION >= 21.05f) { // 1.21.6
                    load.invoke(tile, IOValue.createInput(ProblemReporter.DISCARDING, registry, tag));
                } else {
                    load.invoke(tile, tag, registry);
                }
            } else if (ServerInstance.MAJOR_VERSION == 16) {
                Object blockData = getType.invoke(getWorld.invoke(tile), getPosition.invoke(tile));
                load.invoke(tile, tag, blockData);
            } else {
                load.invoke(tile, tag);
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
