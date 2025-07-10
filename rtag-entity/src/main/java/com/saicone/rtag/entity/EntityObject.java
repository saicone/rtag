package com.saicone.rtag.entity;

import com.saicone.rtag.Rtag;
import com.saicone.rtag.registry.IOValue;
import com.saicone.rtag.tag.TagBase;
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

    private static final String POSITION_KEY = "Pos";

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
    private static final MethodHandle getEncodeId;
    private static final MethodHandle saveAsPassenger;
    private static final MethodHandle saveWithoutId;
    private static final MethodHandle load;
    // Only +1.20.5
    private static final MethodHandle registryAccess;

    static {
        // Methods
        MethodHandle method$getEntity = null;
        MethodHandle method$getHandle = null;
        MethodHandle method$getEncodeId = null;
        MethodHandle method$saveAsPassenger = null;
        MethodHandle method$saveWithoutId = null;
        MethodHandle method$load = null;
        MethodHandle method$registryAccess = null;
        try {
            // Old method names
            String getEncodeId = "ag";
            String saveAsPassenger = "c";
            String saveWithoutId = "e";
            String load = "f";
            String registryAccess = "dR";

            // New method names
            if (ServerInstance.Type.MOJANG_MAPPED) {
                getEncodeId = "getEncodeId";
                saveAsPassenger = "saveAsPassenger";
                saveWithoutId = "saveWithoutId";
                load = "load";
                registryAccess = "registryAccess";
            } else {
                if (ServerInstance.MAJOR_VERSION >= 9) {
                    getEncodeId = "as";
                }
                if (ServerInstance.MAJOR_VERSION >= 10) {
                    getEncodeId = "at";
                }
                if (ServerInstance.MAJOR_VERSION >= 12) {
                    getEncodeId = "getSaveID";
                    saveWithoutId = "save";
                }
                if (ServerInstance.MAJOR_VERSION >= 16) {
                    saveAsPassenger = "a_";
                    load = "load";
                }
                if (ServerInstance.MAJOR_VERSION >= 17) {
                    saveAsPassenger = "d";
                }
                if (ServerInstance.MAJOR_VERSION >= 18) {
                    getEncodeId = "bk";
                    saveWithoutId = "f";
                    load = "g";
                }
                if (ServerInstance.MAJOR_VERSION >= 19) {
                    getEncodeId = "bn";
                }
                if (ServerInstance.VERSION >= 19.02f) { // 1.19.3
                    getEncodeId = "bq";
                }
                if (ServerInstance.VERSION >= 19.03f) { // 1.19.4
                    getEncodeId = "bp";
                }
                if (ServerInstance.MAJOR_VERSION >= 20) {
                    getEncodeId = "br";
                }
                if (ServerInstance.VERSION >= 20.02f) { // 1.20.2
                    getEncodeId = "bu";
                    saveAsPassenger = "saveAsPassenger";
                    saveWithoutId = "saveWithoutId";
                }
                if (ServerInstance.VERSION >= 20.03f) { // 1.20.3
                    getEncodeId = "bw";
                }
                if (ServerInstance.VERSION >= 20.04f) { // 1.20.5
                    getEncodeId = "bC";
                }
                if (ServerInstance.MAJOR_VERSION >= 21) {
                    getEncodeId = "bD";
                    registryAccess = "dQ";
                }
                if (ServerInstance.VERSION >= 21.02f) { // 1.21.2
                    getEncodeId = "bK";
                    registryAccess = "dY";
                }
                if (ServerInstance.VERSION >= 21.03f) { // 1.21.4
                    registryAccess = "dX";
                }
                if (ServerInstance.VERSION >= 21.04f) { // 1.21.5
                    getEncodeId = "bI";
                    load = "i";
                }
                if (ServerInstance.VERSION >= 21.05f) { // 1.21.6
                    getEncodeId = "bN";
                    load = "e";
                    registryAccess = "eb";
                }
            }

            method$getEntity = EasyLookup.staticMethod("CraftEntity", "getEntity", "CraftEntity", "CraftServer", "Entity");
            method$getHandle = EasyLookup.method("CraftEntity", "getHandle", "Entity");

            method$getEncodeId = EasyLookup.method("Entity", getEncodeId, String.class);

            if (ServerInstance.VERSION >= 21.05f) { // 1.21.6
                method$saveAsPassenger = EasyLookup.method("Entity", saveAsPassenger, boolean.class, "ValueOutput", boolean.class);
            } else if (ServerInstance.VERSION >= 20.02f) { // 1.20.2
                method$saveAsPassenger = EasyLookup.method("Entity", saveAsPassenger, boolean.class, "NBTTagCompound", boolean.class);
            } else {
                method$saveAsPassenger = EasyLookup.method("Entity", saveAsPassenger, boolean.class, "NBTTagCompound");
            }

            if (ServerInstance.VERSION >= 21.05f) { // 1.21.6
                method$saveWithoutId = EasyLookup.method("Entity", saveWithoutId, void.class, "ValueOutput", boolean.class);
            } else if (ServerInstance.VERSION >= 20.02f) { // 1.20.2
                method$saveWithoutId = EasyLookup.method("Entity", saveWithoutId, "NBTTagCompound", "NBTTagCompound", boolean.class);
            } else {
                // (1.8) void method
                method$saveWithoutId = EasyLookup.method("Entity", saveWithoutId, "NBTTagCompound", "NBTTagCompound");
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
        getEncodeId = method$getEncodeId;
        saveAsPassenger = method$saveAsPassenger;
        saveWithoutId = method$saveWithoutId;
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
     * Get encode ID from provided entity.
     *
     * @param entity the Entity to extract id.
     * @return       a namespaced String is provided Entity is valid and can be serialized, null otherwise.
     */
    public static String getEncodeId(Object entity) {
        try {
            return (String) getEncodeId.invoke(entity);
        }  catch (Throwable t) {
            throw new RuntimeException("Cannot get encode ID from Minecraft Entity", t);
        }
    }

    /**
     * Save provided entity into newly generated NBTTagCompound.<br>
     * This method will try to include entity ID as part of compound.
     *
     * @param entity the entity to save.
     * @return       a NBTTagCompound that represent the entity.
     */
    public static Object save(Object entity) {
        return save(entity, true);
    }

    /**
     * Save provided entity into newly generated NBTTagCompound.<br>
     * This method will try to include entity ID as part of compound.
     *
     * @param entity     the entity to save.
     * @param includeAll true to include entity position data.
     * @return           a NBTTagCompound that represent the entity.
     */
    public static Object save(Object entity, boolean includeAll) {
        final Object compound = saveWithoutId(entity, includeAll);
        final String id = getEncodeId(entity);
        if (id != null) {
            TagCompound.set(compound, "id", TagBase.newTag(id));
        }
        return compound;
    }

    /**
     * Save provided entity into provided compound as a passenger.<br>
     * This is an optional operation, the provided entity must be marked as 'savable',
     * persistent and should have a valid type.
     *
     * @param entity   the entity to save.
     * @param compound the tag compound that will receive entity data.
     * @return         true if the entity data was saved, false otherwise.
     */
    public static boolean saveAsPassenger(Object entity, Object compound) {
        return saveAsPassenger(entity, compound, true);
    }

    /**
     * Save provided entity into provided compound as a passenger.<br>
     * This is an optional operation, the provided entity must be marked as 'savable',
     * persistent and should have a valid type.
     *
     * @param entity     the entity to save.
     * @param compound   the tag compound that will receive entity data.
     * @param includeAll true to include entity position data.
     * @return           true if the entity data was saved, false otherwise.
     */
    public static boolean saveAsPassenger(Object entity, Object compound, boolean includeAll) {
        try {
            if (ServerInstance.VERSION >= 21.05f) { // 1.21.6
                final Object registry = registryAccess.invoke(entity);
                final Object output = IOValue.createOutput(ProblemReporter.DISCARDING, registry != null ? registry : Rtag.getMinecraftRegistry(), compound);
                return (boolean) saveAsPassenger.invoke(entity, output, includeAll);
            } else if (ServerInstance.VERSION >= 20.02f) { // 1.20.2
                return (boolean) saveAsPassenger.invoke(entity, TagCompound.newTag(), includeAll);
            } else {
                final boolean result = (boolean) saveAsPassenger.invoke(entity, TagCompound.newTag());
                if (result && !includeAll) {
                    TagCompound.remove(compound, POSITION_KEY);
                }
                return result;
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Save provided entity into newly generated NBTTagCompound.<br>
     * As its name says, this method doesn't generate the 'id' key.
     *
     * @param entity the entity instance.
     * @return       a NBTTagCompound that represent the entity.
     */
    public static Object saveWithoutId(Object entity) {
        return saveWithoutId(entity, true);
    }

    /**
     * Save provided entity into newly generated NBTTagCompound.<br>
     * As its name says, this method doesn't generate the 'id' key.
     *
     * @param entity     the entity instance.
     * @param includeAll true to include entity position data.
     * @return           a NBTTagCompound that represent the entity.
     */
    public static Object saveWithoutId(Object entity, boolean includeAll) {
        try {
            final Object tag;
            if (ServerInstance.VERSION >= 21.05f) { // 1.21.6
                final Object registry = registryAccess.invoke(entity);
                final Object output = IOValue.createOutput(ProblemReporter.DISCARDING, registry != null ? registry : Rtag.getMinecraftRegistry());
                saveWithoutId.invoke(entity, output, includeAll);
                return IOValue.result(output);
            } else if (ServerInstance.VERSION >= 20.02f) { // 1.20.2
                return saveWithoutId.invoke(entity, TagCompound.newTag(), includeAll);
            } else if (ServerInstance.MAJOR_VERSION >= 9) {
                tag = saveWithoutId.invoke(entity, TagCompound.newTag());
            } else {
                tag = TagCompound.newTag();
                saveWithoutId.invoke(entity, tag);
            }

            if (!includeAll) {
                TagCompound.remove(tag, POSITION_KEY);
            }
            return tag;
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
