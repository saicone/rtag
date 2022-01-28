package com.saicone.rtag.block;

import com.saicone.rtag.util.EasyLookup;
import com.saicone.rtag.util.ServerInstance;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.lang.invoke.MethodHandle;

/**
 * Class to invoke Minecraft World methods across versions.
 *
 * @author Rubenicos
 */
public class TileBridge {

    private static final Class<?> craftBlockState = EasyLookup.classById("CraftBlockState");

    private static final MethodHandle getHandle;
    private static final MethodHandle getTileEntity;
    private static final MethodHandle newBlockPosition;

    static {
        MethodHandle m1 = null, m2 = null, m3 = null;
        try {
            m1 = EasyLookup.method("CraftWorld", "getHandle", "WorldServer");
            m2 = EasyLookup.method("World", ServerInstance.verNumber >= 18 ? "c_" : "getTileEntity", "TileEntity", "BlockPosition");
            m3 = EasyLookup.constructor("BlockPosition", int.class, int.class, int.class);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        getHandle = m1;
        getTileEntity = m2;
        newBlockPosition = m3;
    }

    /**
     * Get provided Bukkit Block and convert into Minecraft TileEntity.
     *
     * @param block Block to convert.
     * @return      A Minecraft TileEntity.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static Object asMinecraft(Block block) throws Throwable {
        if (craftBlockState.isInstance(block.getState())) {
            Location loc = block.getLocation();
            return getTileEntity.invoke(asMinecraft(loc.getWorld()), newBlockPosition.invoke(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        } else {
            return null;
        }
    }

    /**
     * Get provided Bukkit World and convert into Minecraft World.
     *
     * @param world World to convert.
     * @return      A Minecraft World.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static Object asMinecraft(World world) throws Throwable {
        return getHandle.invoke(world);
    }
}
