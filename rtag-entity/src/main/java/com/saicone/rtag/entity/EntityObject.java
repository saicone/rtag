package com.saicone.rtag.entity;

import com.saicone.rtag.Rtag;
import com.saicone.rtag.registry.IOValue;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.util.EasyLookup;
import com.saicone.rtag.util.ProblemReporter;
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

    // Import reflected classes
    static {
        try {
            EasyLookup.addNMSClass("world.entity.Entity");

            EasyLookup.addOBCClass("CraftServer");
            EasyLookup.addOBCClass("entity.CraftEntity");

            if (ServerInstance.MAJOR_VERSION >= 16) {
                EasyLookup.addNMSClass("core.IRegistryCustom", "RegistryAccess");
            }

            if (ServerInstance.VERSION >= 21.05f) { // 1.21.6
                EasyLookup.addNMSClass("world.level.storage.ValueInput");
                EasyLookup.addNMSClass("world.level.storage.ValueOutput");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static final Class<?> MC_ENTITY = EasyLookup.classById("Entity");
    private static final Class<?> CRAFT_ENTITY = EasyLookup.classById("CraftEntity");

    private static final MethodHandle getEntity;
    private static final MethodHandle getHandle;
    private static final MethodHandle save;
    private static final MethodHandle load;
    // Only +1.20.5
    private static final MethodHandle registryAccess;

    static {
        // Methods
        MethodHandle method$getEntity = null;
        MethodHandle method$getHandle = null;
        MethodHandle method$save = null;
        MethodHandle method$load = null;
        MethodHandle method$registryAccess = null;
        try {
            // Old method names
            String save = "e";
            String load = "f";
            String registryAccess = "dR";

            // New method names
            if (ServerInstance.Type.MOJANG_MAPPED) {
                save = "saveWithoutId";
                load = "load";
                registryAccess = "registryAccess";
            } else {
                if (ServerInstance.MAJOR_VERSION >= 12) {
                    save = "save";
                }
                if (ServerInstance.MAJOR_VERSION >= 16) {
                    load = "load";
                }
                if (ServerInstance.MAJOR_VERSION >= 18) {
                    save = "f";
                    load = "g";
                }
                if (ServerInstance.MAJOR_VERSION >= 21) {
                    registryAccess = "dQ";
                }
                if (ServerInstance.VERSION >= 21.02f) { // 1.21.2
                    registryAccess = "dY";
                }
                if (ServerInstance.VERSION >= 21.03f) { // 1.21.4
                    registryAccess = "dX";
                }
                if (ServerInstance.VERSION >= 21.04f) { // 1.21.5
                    save = "h";
                    load = "i";
                }
                if (ServerInstance.VERSION >= 21.05f) { // 1.21.6
                    save = "d";
                    load = "e";
                    registryAccess = "eb";
                }
            }

            method$getEntity = EasyLookup.staticMethod("CraftEntity", "getEntity", "CraftEntity", "CraftServer", "Entity");
            method$getHandle = EasyLookup.method("CraftEntity", "getHandle", "Entity");

            if (ServerInstance.VERSION >= 21.05f) { // 1.21.6
                method$save = EasyLookup.method("Entity", save, void.class, "ValueOutput");
            } else {
                // (1.8) void method
                // (1.20.3) Note: New method to save entity as compound excluding world position data
                method$save = EasyLookup.method("Entity", save, "NBTTagCompound", "NBTTagCompound");
            }

            if (ServerInstance.VERSION >= 21.05f) { // 1.21.6
                method$load = EasyLookup.method("Entity", load, void.class, "ValueInput");
            } else {
                method$load = EasyLookup.method("Entity", load, void.class, "NBTTagCompound");
            }

            if (ServerInstance.Release.COMPONENT) {
                method$registryAccess = EasyLookup.method("Entity", registryAccess, "IRegistryCustom");
            }
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        getEntity = method$getEntity;
        getHandle = method$getHandle;
        save = method$save;
        load = method$load;
        registryAccess = method$registryAccess;
    }

    EntityObject() {
    }

    /**
     * Check if the provided object is instance of Minecraft Entity.
     *
     * @param object the object to check.
     * @return       true if the object is an instance of Minecraft Entity.
     */
    public static boolean isMinecraftEntity(Object object) {
        return MC_ENTITY.isInstance(object);
    }

    /**
     * Check if the provided object is instance of CraftEntity.
     *
     * @param object the object to check.
     * @return       true if the object is an instance of CraftEntity.
     */
    public static boolean isCraftEntity(Object object) {
        return CRAFT_ENTITY.isInstance(object);
    }

    /**
     * Get provided Minecraft Entity as Bukkit Entity.
     *
     * @param entity Entity to convert.
     * @return       A Bukkit Entity.
     * @throws IllegalArgumentException if entity is not a Minecraft Entity.
     */
    public static Entity getEntity(Object entity) throws IllegalArgumentException {
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
     */
    public static Object getHandle(Entity entity) {
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
     */
    public static Object save(Object entity) {
        try {
            if (ServerInstance.VERSION >= 21.05f) { // 1.21.6
                final Object registry = registryAccess.invoke(entity);
                final Object output = IOValue.createOutput(ProblemReporter.DISCARDING, registry != null ? registry : Rtag.getMinecraftRegistry());
                save.invoke(entity, output);
                return IOValue.result(output);
            } else if (ServerInstance.MAJOR_VERSION >= 9) {
                return save.invoke(entity, TagCompound.newTag());
            } else {
                Object tag = TagCompound.newTag();
                save.invoke(entity, tag);
                return tag;
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Load NBTTagCompound into entity.
     *
     * @param entity Entity instance.
     * @param tag    The NBTTagCompound to load.
     */
    public static void load(Object entity, Object tag) {
        try {
            if (ServerInstance.VERSION >= 21.05f) { // 1.21.6
                final Object registry = registryAccess.invoke(entity);
                load.invoke(entity, IOValue.createInput(ProblemReporter.DISCARDING, registry != null ? registry : Rtag.getMinecraftRegistry(), tag));
            } else {
                load.invoke(entity, tag);
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
