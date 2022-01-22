package com.saicone.rtag.entity;

import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.util.EasyLookup;
import com.saicone.rtag.util.ServerInstance;

import java.lang.invoke.MethodHandle;

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
            } if (ServerInstance.verNumber >= 12) {
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
        save = m1; load = m2;
    }

    public static Object saveTag(Object entity) throws Throwable {
        if (ServerInstance.verNumber >= 9) {
            return save.invoke(entity, TagCompound.newTag());
        } else {
            Object tag = TagCompound.newTag();
            save.invoke(entity, tag);
            return tag;
        }
    }

    public static void loadTag(Object entity, Object tag) throws Throwable {
        load.invoke(entity, tag);
    }
}
