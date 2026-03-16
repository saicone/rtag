package com.saicone.rtag.entity;

import com.saicone.rtag.Rtag;
import com.saicone.rtag.registry.IOValue;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.util.MC;
import com.saicone.rtag.util.ProblemReporter;
import com.saicone.rtag.util.ServerInstance;
import com.saicone.rtag.util.reflect.Lookup;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Modifier;

/**
 * Class to invoke Entity methods across versions.
 *
 * @author Rubenicos
 */
public class EntityObject {

    // import
    private static final Lookup.AClass<?> RegistryAccess = Lookup.SERVER.importClass("net.minecraft.core.RegistryAccess");
    private static final Lookup.AClass<?> CompoundTag = Lookup.SERVER.importClass("net.minecraft.nbt.CompoundTag");
    private static final Lookup.AClass<?> MC_Entity = Lookup.SERVER.importClass("net.minecraft.world.entity.Entity");
    private static final Lookup.AClass<?> ValueInput = Lookup.SERVER.importClass("net.minecraft.world.level.storage.ValueInput");
    private static final Lookup.AClass<?> ValueOutput = Lookup.SERVER.importClass("net.minecraft.world.level.storage.ValueOutput");
    private static final Lookup.AClass<?> CraftServer = Lookup.SERVER.importClass("org.bukkit.craftbukkit.CraftServer");
    private static final Lookup.AClass<?> CraftEntity = Lookup.SERVER.importClass("org.bukkit.craftbukkit.entity.CraftEntity");

    private static final MethodHandle Entity_getEncodeId;
    static {
        if (MC.version().isNewerThanOrEquals(MC.V_1_21_4) && ServerInstance.Platform.PAPER) {
            Entity_getEncodeId = MC_Entity.method(String.class, "getEncodeId", boolean.class).handle();
        } else {
            Entity_getEncodeId = MC_Entity.method(String.class, "getEncodeId").handle();
        }
    }
    private static final MethodHandle Entity_saveAsPassenger;
    static {
        if (MC.version().isNewerThanOrEquals(MC.V_1_21_6)) {
            if (ServerInstance.Platform.PAPER) {
                Entity_saveAsPassenger = MC_Entity.method(boolean.class, "saveAsPassenger", ValueOutput, boolean.class, boolean.class, boolean.class).handle();
            } else {
                Entity_saveAsPassenger = MC_Entity.method(boolean.class, "saveAsPassenger", ValueOutput, boolean.class).handle();
            }
        } else if (MC.version().isNewerThanOrEquals(MC.V_1_21_4) && ServerInstance.Platform.PAPER) {
            Entity_saveAsPassenger = MC_Entity.method(boolean.class, "saveAsPassenger", CompoundTag, boolean.class, boolean.class, boolean.class).handle();
        } else if (MC.version().isNewerThanOrEquals(MC.V_1_20_2)) {
            Entity_saveAsPassenger = MC_Entity.method(boolean.class, "saveAsPassenger", CompoundTag, boolean.class).handle();
        } else {
            Entity_saveAsPassenger = MC_Entity.method(boolean.class, "saveAsPassenger", CompoundTag).handle();
        }
    }
    private static final MethodHandle Entity_saveWithoutId;
    static {
        if (MC.version().isNewerThanOrEquals(MC.V_1_21_6)) {
            if (ServerInstance.Platform.PAPER) {
                Entity_saveWithoutId = MC_Entity.method(void.class, "saveWithoutId", ValueOutput, boolean.class, boolean.class, boolean.class).handle();
            } else {
                Entity_saveWithoutId = MC_Entity.method(void.class, "saveWithoutId", ValueOutput, boolean.class).handle();
            }
        } else if (MC.version().isNewerThanOrEquals(MC.V_1_21_4) && ServerInstance.Platform.PAPER) {
            Entity_saveWithoutId = MC_Entity.method(CompoundTag, "saveWithoutId", CompoundTag, boolean.class, boolean.class, boolean.class).handle();
        } else if (MC.version().isNewerThanOrEquals(MC.V_1_20_2)) {
            Entity_saveWithoutId = MC_Entity.method(CompoundTag, "saveWithoutId", CompoundTag, boolean.class).handle();
        } else if (MC.version().isNewerThanOrEquals(MC.V_1_9)) {
            Entity_saveWithoutId = MC_Entity.method(CompoundTag, "saveWithoutId", CompoundTag).handle();
        } else {
            Entity_saveWithoutId = MC_Entity.method(void.class, "saveWithoutId", CompoundTag).handle();
        }
    }
    private static final MethodHandle Entity_load;
    static {
        if (MC.version().isNewerThanOrEquals(MC.V_1_21_6)) {
            Entity_load = MC_Entity.method(void.class, "load", ValueInput).handle();
        } else {
            Entity_load = MC_Entity.method(void.class, "load", CompoundTag).handle();
        }
    }
    private static final MethodHandle Entity_registryAccess;
    static {
        if (MC.version().isComponent()) {
            Entity_registryAccess = MC_Entity.method(RegistryAccess, "registryAccess").handle();
        } else {
            Entity_registryAccess = null;
        }
    }

    private static final MethodHandle CraftEntity_getEntity = CraftEntity.method(Modifier.STATIC, CraftEntity, "getEntity", CraftServer, MC_Entity).handle();
    private static final MethodHandle CraftEntity_getHandle = CraftEntity.method(MC_Entity, "getHandle").handle();

    private static final String POSITION_KEY = "Pos";

    EntityObject() {
    }

    /**
     * Check if the provided object is instance of Minecraft Entity.
     *
     * @param object the object to check.
     * @return       true if the object is an instance of Minecraft Entity.
     */
    public static boolean isMinecraftEntity(Object object) {
        return MC_Entity.isInstance(object);
    }

    /**
     * Check if the provided object is instance of CraftEntity.
     *
     * @param object the object to check.
     * @return       true if the object is an instance of CraftEntity.
     */
    public static boolean isCraftEntity(Object object) {
        return CraftEntity.isInstance(object);
    }

    /**
     * Get provided Minecraft Entity as Bukkit Entity.
     *
     * @param entity Entity to convert.
     * @return       A Bukkit Entity.
     * @throws IllegalArgumentException if entity is not a Minecraft Entity.
     */
    public static Entity getEntity(Object entity) throws IllegalArgumentException {
        if (MC_Entity.isInstance(entity)) {
            try {
                return (Entity) CraftEntity_getEntity.invoke(Bukkit.getServer(), entity);
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
            return CraftEntity_getHandle.invoke(entity);
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
        return getEncodeId(entity, false);
    }

    /**
     * Get encode ID from provided entity.
     *
     * @param entity             the Entity to extract id.
     * @param includeNonSaveable true to include entity id even if it should not be serialized.
     * @return                   a namespaced String is provided Entity is valid and can be serialized, null otherwise.
     */
    public static String getEncodeId(Object entity, boolean includeNonSaveable) {
        try {
            if (MC.version().isNewerThanOrEquals(MC.V_1_21_4) && ServerInstance.Platform.PAPER) {
                return (String) Entity_getEncodeId.invoke(entity, includeNonSaveable);
            } else {
                return (String) Entity_getEncodeId.invoke(entity);
            }
        }  catch (Throwable t) {
            throw new RuntimeException("Cannot get encode ID from Minecraft Entity", t);
        }
    }

    /**
     * Save provided entity into newly generated CompoundTag.<br>
     * This method will try to include entity ID as part of compound.
     *
     * @param entity the entity to save.
     * @return       a compound tag that represent the entity.
     */
    public static Object save(Object entity) {
        return save(entity, TagCompound.newTag(), true, false, false);
    }

    /**
     * Save provided entity into newly generated CompoundTag.<br>
     * This method will try to include entity ID as part of compound.
     *
     * @param entity             the entity to save.
     * @param compound           the tag compound that will receive entity data.
     * @param includeAll         true to include entity position data.
     * @param includeNonSaveable true to include any entity id even if it should not be serialized.
     * @param forceSerialization save any entity even
     * @return                   a compound tag that represent the entity.
     */
    public static Object save(Object entity, Object compound, boolean includeAll, boolean includeNonSaveable, boolean forceSerialization) {
        saveWithoutId(entity, compound, includeAll, includeNonSaveable, forceSerialization);
        final String id = getEncodeId(entity, includeNonSaveable);
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
        return saveAsPassenger(entity, compound, true, false, false);
    }

    /**
     * Save provided entity into provided compound as a passenger.<br>
     * This is an optional operation, the provided entity must be marked as 'savable',
     * persistent and should have a valid type.
     *
     * @param entity             the entity to save.
     * @param compound           the tag compound that will receive entity data.
     * @param includeAll         true to include entity position data.
     * @param includeNonSaveable true to include any entity id even if it should not be serialized.
     * @param forceSerialization true to include non-persistent entity data.
     * @return                   true if the entity data was saved, false otherwise.
     */
    public static boolean saveAsPassenger(Object entity, Object compound, boolean includeAll, boolean includeNonSaveable, boolean forceSerialization) {
        try {
            if (MC.version().isNewerThanOrEquals(MC.V_1_21_6)) {
                final Object registry = Entity_registryAccess.invoke(entity);
                final Object output = IOValue.createOutputWrapping(ProblemReporter.DISCARDING, registry != null ? registry : Rtag.getMinecraftRegistry(), compound);
                if (ServerInstance.Platform.PAPER) {
                    return (boolean) Entity_saveAsPassenger.invoke(entity, output, includeAll, includeNonSaveable, forceSerialization);
                } else {
                    return (boolean) Entity_saveAsPassenger.invoke(entity, output, includeAll);
                }
            } else if (MC.version().isNewerThanOrEquals(MC.V_1_21_4) && ServerInstance.Platform.PAPER) {
                return (boolean) Entity_saveAsPassenger.invoke(entity, TagCompound.newTag(), includeAll, includeNonSaveable, forceSerialization);
            } else if (MC.version().isNewerThanOrEquals(MC.V_1_20_2)) {
                return (boolean) Entity_saveAsPassenger.invoke(entity, TagCompound.newTag(), includeAll);
            } else {
                final boolean result = (boolean) Entity_saveAsPassenger.invoke(entity, TagCompound.newTag());
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
     * Save provided entity into newly generated CompoundTag.<br>
     * As its name says, this method doesn't generate the 'id' key.
     *
     * @param entity   the entity instance.
     * @param compound the tag compound that will receive entity data.
     * @return         a compound tag that represent the entity.
     */
    public static Object saveWithoutId(Object entity, Object compound) {
        return saveWithoutId(entity, compound, true, false, false);
    }

    /**
     * Save provided entity into newly generated CompoundTag.<br>
     * As its name says, this method doesn't generate the 'id' key.
     *
     * @param entity             the entity instance.
     * @param compound           the tag compound that will receive entity data.
     * @param includeAll         true to include entity position data.
     * @param includeNonSaveable true to include any entity id even if it should not be serialized.
     * @param forceSerialization true to include non-persistent entity data.
     * @return                   a compound tag that represent the entity.
     */
    public static Object saveWithoutId(Object entity, Object compound, boolean includeAll, boolean includeNonSaveable, boolean forceSerialization) {
        try {
            if (MC.version().isNewerThanOrEquals(MC.V_1_21_6)) {
                final Object registry = Entity_registryAccess.invoke(entity);
                final Object output = IOValue.createOutputWrapping(ProblemReporter.DISCARDING, registry != null ? registry : Rtag.getMinecraftRegistry(), compound);
                if (ServerInstance.Platform.PAPER) {
                    Entity_saveWithoutId.invoke(entity, output, includeAll, includeNonSaveable, forceSerialization);
                } else {
                    Entity_saveWithoutId.invoke(entity, output, includeAll);
                }
                return IOValue.result(output);
            } else if (MC.version().isNewerThanOrEquals(MC.V_1_21_4) && ServerInstance.Platform.PAPER) {
                return Entity_saveWithoutId.invoke(entity, compound, includeAll, includeNonSaveable, forceSerialization);
            } else if (MC.version().isNewerThanOrEquals(MC.V_1_20_2)) {
                return Entity_saveWithoutId.invoke(entity, compound, includeAll);
            } else {
                Entity_saveWithoutId.invoke(entity, compound);
                if (!includeAll) {
                    TagCompound.remove(compound, POSITION_KEY);
                }
                return compound;
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Load CompoundTag into entity.
     *
     * @param entity Entity instance.
     * @param tag    The CompoundTag to load.
     */
    public static void load(Object entity, Object tag) {
        try {
            if (MC.version().isNewerThanOrEquals(MC.V_1_21_6)) {
                final Object registry = Entity_registryAccess.invoke(entity);
                Entity_load.invoke(entity, IOValue.createInput(ProblemReporter.DISCARDING, registry != null ? registry : Rtag.getMinecraftRegistry(), tag));
            } else {
                Entity_load.invoke(entity, tag);
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
