package com.saicone.rtag.entity;

import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.util.EasyLookup;
import com.saicone.rtag.util.ServerInstance;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.lang.invoke.MethodHandle;

/**
 * Class to invoke Entity methods across versions.
 *
 * @author Rubenicos
 */
public class EntityObject {

    private static final Class<?> MC_ENTITY = EasyLookup.classById("Entity");

    private static final MethodHandle getEntity;
    private static final MethodHandle getHandle;
    private static final MethodHandle save;
    private static final MethodHandle load;

    static {
        // Methods
        MethodHandle method$getEntity = null;
        MethodHandle method$getHandle = null;
        MethodHandle method$save = null;
        MethodHandle method$load = null;
        try {
            // Old method names
            String save = "e";
            String load = "f";
            // New method names
            if (ServerInstance.verNumber >= 18) {
                save = "f";
                load = "g";
            } else if (ServerInstance.verNumber >= 12) {
                save = "save";
                if (ServerInstance.verNumber >= 16) {
                    load = "load";
                }
            }

            method$getEntity = EasyLookup.staticMethod("CraftEntity", "getEntity", "CraftEntity", "CraftServer", "Entity");
            method$getHandle = EasyLookup.method("CraftEntity", "getHandle", "Entity");
            // Unreflect reason:
            // (1.8) void method
            // Other versions return NBTTagCompound
            method$save = EasyLookup.unreflectMethod("Entity", save, "NBTTagCompound");
            method$load = EasyLookup.method("Entity", load, void.class, "NBTTagCompound");
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        getEntity = method$getEntity;
        getHandle = method$getHandle;
        save = method$save;
        load = method$load;
    }

    EntityObject() {
    }

    /**
     * Get provided Minecraft Entity as Bukkit Entity.
     *
     * @param entity Entity to convert.
     * @return       A Bukkit Entity.
     * @throws IllegalArgumentException if entity is not a Minecraft Entity.
     * @throws RuntimeException if any error occurs on reflected method invoking.
     */
    public static Entity getEntity(Object entity) throws IllegalArgumentException, RuntimeException {
        if (MC_ENTITY.isInstance(entity)) {
            try {
                return (Entity) getEntity.invoke(Bukkit.getServer(), entity);
            } catch (Throwable t) {
                throw new RuntimeException("Cannot convert Minecraft Entity into Bukkit Entity", t);
            }
        } else {
            throw new IllegalArgumentException("The provided object isn't a Minecraft entity");
        }
    }

    /**
     * Get provided Bukkit Entity as Minecraft Entity.
     *
     * @param entity Entity to convert.
     * @return       A Minecraft Entity.
     * @throws RuntimeException if any error occurs on reflected method invoking.
     */
    public static Object getHandle(Entity entity) throws RuntimeException {
        try {
            return getHandle.invoke(entity);
        } catch (Throwable t) {
            throw new RuntimeException("Cannot convert Bukkit Entity into Minecraft Entity", t);
        }
    }

    /**
     * Save current NBTTagCompound into new one entity.
     *
     * @param entity Entity instance.
     * @return       A NBTTagCompound that represent the tile.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static Object save(Object entity) throws Throwable {
        if (ServerInstance.verNumber >= 9) {
            return save.invoke(entity, TagCompound.newTag());
        } else {
            Object tag = TagCompound.newTag();
            save.invoke(entity, tag);
            return tag;
        }
    }

    /**
     * Load NBTTagCompound into entity.
     *
     * @param entity Entity instance.
     * @param tag    The NBTTagCompound to load.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static void load(Object entity, Object tag) throws Throwable {
        load.invoke(entity, tag);
    }
}
