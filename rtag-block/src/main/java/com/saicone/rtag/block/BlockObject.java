package com.saicone.rtag.block;

import com.saicone.rtag.Rtag;
import com.saicone.rtag.registry.IOValue;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.util.MC;
import com.saicone.rtag.util.ProblemReporter;
import com.saicone.rtag.util.reflect.Lookup;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.lang.invoke.MethodHandle;

/**
 * Class to invoke Block/Tile methods across versions.
 *
 * @author Rubenicos
 */
public class BlockObject {

    // import
    private static final Lookup.AClass<?> BlockPos = Lookup.SERVER.importClass("net.minecraft.core.BlockPos");
    private static final Lookup.AClass<?> HolderLookup$Provider = Lookup.SERVER.importClass("net.minecraft.core.HolderLookup$Provider");
    private static final Lookup.AClass<?> RegistryAccess = Lookup.SERVER.importClass("net.minecraft.core.RegistryAccess");
    private static final Lookup.AClass<?> CompoundTag = Lookup.SERVER.importClass("net.minecraft.nbt.CompoundTag");
    private static final Lookup.AClass<?> ServerLevel = Lookup.SERVER.importClass("net.minecraft.server.level.ServerLevel");
    private static final Lookup.AClass<?> Level = Lookup.SERVER.importClass("net.minecraft.world.level.Level");
    private static final Lookup.AClass<?> LevelReader = Lookup.SERVER.importClass("net.minecraft.world.level.LevelReader");
    private static final Lookup.AClass<?> BlockEntity = Lookup.SERVER.importClass("net.minecraft.world.level.block.entity.BlockEntity");
    private static final Lookup.AClass<?> BlockState = Lookup.SERVER.importClass("net.minecraft.world.level.block.state.BlockState");
    private static final Lookup.AClass<?> ValueInput = Lookup.SERVER.importClass("net.minecraft.world.level.storage.ValueInput");
    private static final Lookup.AClass<?> ValueOutput = Lookup.SERVER.importClass("net.minecraft.world.level.storage.ValueOutput");
    private static final Lookup.AClass<?> CraftWorld = Lookup.SERVER.importClass("org.bukkit.craftbukkit.CraftWorld");
    private static final Lookup.AClass<?> CraftBlockState = Lookup.SERVER.importClass("org.bukkit.craftbukkit.block.CraftBlockState");

    // declare
    private static final MethodHandle BlockPos$new = BlockPos.constructor(int.class, int.class, int.class).handle();

    private static final MethodHandle Level_getBlockState = Level.method(BlockState, "getBlockState", BlockPos).handle();
    private static final MethodHandle Level_getBlockEntity = Level.method(BlockEntity, "getBlockEntity", BlockPos).handle();

    private static final MethodHandle LevelReader_registryAccess;
    static {
        if (MC.version().isComponent()) {
            LevelReader_registryAccess = LevelReader.method(RegistryAccess, "registryAccess").handle();
        } else {
            LevelReader_registryAccess = null;
        }
    }

    private static final MethodHandle BlockEntity_getLevel = BlockEntity.method(Level, "getLevel").handle();
    private static final MethodHandle BlockEntity_getBlockPos = BlockEntity.method(BlockPos, "getBlockPos").handle(); // only used on 1.16.x
    private static final MethodHandle BlockEntity_saveWithoutMetadata;
    static {
        if (MC.version().isNewerThanOrEquals(MC.V_1_21_6)) {
            BlockEntity_saveWithoutMetadata = BlockEntity.method(void.class, "saveWithoutMetadata", ValueOutput).handle();
        } else if (MC.version().isComponent()) {
            BlockEntity_saveWithoutMetadata = BlockEntity.method(CompoundTag, "saveWithoutMetadata", HolderLookup$Provider).handle();
        } else if (MC.version().isNewerThanOrEquals(MC.V_1_18)) {
            BlockEntity_saveWithoutMetadata = BlockEntity.method(CompoundTag, "saveWithoutMetadata").handle();
        } else if (MC.version().isNewerThanOrEquals(MC.V_1_9)) {
            BlockEntity_saveWithoutMetadata = BlockEntity.method(CompoundTag, "saveWithoutMetadata", CompoundTag).handle();
        } else {
            BlockEntity_saveWithoutMetadata = BlockEntity.method(void.class, "saveWithoutMetadata", CompoundTag).handle();
        }
    }
    private static final MethodHandle BlockEntity_loadWithComponents;
    static {
        if (MC.version().isNewerThanOrEquals(MC.V_1_21_6)) {
            BlockEntity_loadWithComponents = BlockEntity.method(void.class, "loadWithComponents", ValueInput).handle();
        } else if (MC.version().isComponent()) {
            BlockEntity_loadWithComponents = BlockEntity.method(void.class, "loadWithComponents", CompoundTag, HolderLookup$Provider).handle();
        } else if (MC.version().isNewerThanOrEquals(MC.V_1_17)) {
            BlockEntity_loadWithComponents = BlockEntity.method(void.class, "load", CompoundTag).handle();
        } else if (MC.version().isNewerThanOrEquals(MC.V_1_16)) {
            BlockEntity_loadWithComponents = BlockEntity.method(void.class, "load", BlockState, CompoundTag).handle();
        } else {
            BlockEntity_loadWithComponents = BlockEntity.method(void.class, "load", CompoundTag).handle();
        }
    }

    private static final MethodHandle CraftWorld_getHandle = CraftWorld.method(ServerLevel, "getHandle").handle();

    BlockObject() {
    }

    /**
     * Check if the provided object is instance of Minecraft BlockEntity.
     *
     * @param object the object to check.
     * @return       true if the object is an instance of Minecraft BlockEntity.
     */
    public static boolean isTileEntity(Object object) {
        return BlockEntity.isInstance(object);
    }

    /**
     * Get provided Bukkit Block and convert into Minecraft BlockEntity.
     *
     * @param block Block to convert.
     * @return      A Minecraft BlockEntity.
     * @throws IllegalArgumentException if block state is not a CraftBlockState.
     */
    public static Object getTileEntity(Block block) throws IllegalArgumentException {
        if (CraftBlockState.isInstance(block.getState())) {
            Location loc = block.getLocation();
            try {
                return Level_getBlockEntity.invoke(CraftWorld_getHandle.invoke(loc.getWorld()), BlockPos$new.invoke(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
            } catch (Throwable t) {
                throw new RuntimeException("Cannot convert Bukkit Block into Minecraft BlockEntity", t);
            }
        } else {
            throw new IllegalArgumentException("The provided block state isn't a CraftBlockState");
        }
    }

    /**
     * Save Minecraft BlockEntity into new CompoundTag.
     *
     * @param tile BlockEntity instance.
     * @return     a compound tag that represent the tile.
     */
    public static Object save(Object tile) {
        try {
            if (MC.version().isComponent()) {
                final Object world = BlockEntity_getLevel.invoke(tile);
                final Object registry = world != null ? LevelReader_registryAccess.invoke(world) : Rtag.getMinecraftRegistry();
                if (MC.version().isNewerThanOrEquals(MC.V_1_21_6)) {
                    final Object output = IOValue.createOutput(ProblemReporter.DISCARDING, registry);
                    BlockEntity_saveWithoutMetadata.invoke(tile, output);
                    return IOValue.result(output);
                } else {
                    return BlockEntity_saveWithoutMetadata.invoke(tile, registry);
                }
            } else if (MC.version().isNewerThanOrEquals(MC.V_1_18)) {
                return BlockEntity_saveWithoutMetadata.invoke(tile);
            } else if (MC.version().isNewerThanOrEquals(MC.V_1_9)) {
                return BlockEntity_saveWithoutMetadata.invoke(tile, TagCompound.newTag());
            } else {
                Object tag = TagCompound.newTag();
                BlockEntity_saveWithoutMetadata.invoke(tile, tag);
                return tag;
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Load CompoundTag into Minecraft BlockEntity.
     *
     * @param tile BlockEntity instance.
     * @param tag  The CompoundTag to load.
     */
    public static void load(Object tile, Object tag) {
        try {
            if (MC.version().isComponent()) {
                final Object world = BlockEntity_getLevel.invoke(tile);
                final Object registry = world != null ? LevelReader_registryAccess.invoke(world) : Rtag.getMinecraftRegistry();
                if (MC.version().isNewerThanOrEquals(MC.V_1_21_6)) {
                    BlockEntity_loadWithComponents.invoke(tile, IOValue.createInput(ProblemReporter.DISCARDING, registry, tag));
                } else {
                    BlockEntity_loadWithComponents.invoke(tile, tag, registry);
                }
            } else if (MC.version().feature() == 16) {
                Object blockData = Level_getBlockState.invoke(BlockEntity_getLevel.invoke(tile), BlockEntity_getBlockPos.invoke(tile));
                BlockEntity_loadWithComponents.invoke(tile, tag, blockData);
            } else {
                BlockEntity_loadWithComponents.invoke(tile, tag);
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
