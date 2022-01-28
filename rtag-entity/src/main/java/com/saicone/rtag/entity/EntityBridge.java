package com.saicone.rtag.entity;

import com.saicone.rtag.util.EasyLookup;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.lang.invoke.MethodHandle;

/**
 * Class to invoke CraftEntity methods across versions.
 *
 * @author Rubenicos
 */
public class EntityBridge {

    private static final Class<?> entityClass = EasyLookup.classById("Entity");

    private static final MethodHandle getEntity;
    private static final MethodHandle getHandle;

    static {
        MethodHandle m1 = null, m2 = null;
        try {
            // Minecraft -> Bukkit
            m1 = EasyLookup.staticMethod("CraftEntity", "getEntity", "CraftEntity", "CraftServer", "Entity");
            // Bukkit -> Minecraft
            m2 = EasyLookup.method("CraftEntity", "getHandle", "Entity");
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        getEntity = m1; getHandle = m2;
    }

    /**
     * Get provided Minecraft Entity and convert into Bukkit Entity.
     *
     * @param entity Entity to convert.
     * @return       A Bukkit Entity.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static Entity asBukkit(Object entity) throws Throwable {
        if (entityClass.isInstance(entity)) {
            return (Entity) getEntity.invoke(Bukkit.getServer(), entity);
        } else {
            return null;
        }
    }

    /**
     * Get provided Bukkit Entity and convert into Minecraft Entity.
     *
     * @param entity Entity to convert.
     * @return       A Minecraft Entity.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static Object asMinecraft(Entity entity) throws Throwable {
        return getHandle.invoke(entity);
    }
}
