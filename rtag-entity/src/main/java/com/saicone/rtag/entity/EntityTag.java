package com.saicone.rtag.entity;

import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.util.EasyLookup;
import com.saicone.rtag.util.ServerInstance;

import java.lang.invoke.MethodHandle;

/**
 * Class to invoke Minecraft Entity methods across versions.
 *
 * @author Rubenicos
 */
public class EntityTag {

    private static final MethodHandle save;
    private static final MethodHandle load;

    static {
        MethodHandle m1 = null, m2 = null;
        try {
            // Old method names
            String save = "e", load = "f";
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

            // Unreflect reason:
            // (1.8) void method
            // Other versions return NBTTagCompound
            m1 = EasyLookup.unreflectMethod("Entity", save, "NBTTagCompound");
            m2 = EasyLookup.method("Entity", load, void.class, "NBTTagCompound");
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        save = m1;
        load = m2;
    }

    /**
     * Save current NBTTagCompound into new one entity.
     *
     * @param entity Entity instance.
     * @return       A NBTTagCompound that represent the tile.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static Object saveTag(Object entity) throws Throwable {
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
    public static void loadTag(Object entity, Object tag) throws Throwable {
        load.invoke(entity, tag);
    }
}
