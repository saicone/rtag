package com.saicone.rtag.entity;

import com.saicone.rtag.util.EasyLookup;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.lang.invoke.MethodHandle;

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

    public static Entity asBukkit(Object entity) throws Throwable {
        if (entityClass.isInstance(entity)) {
            return (Entity) getEntity.invoke(Bukkit.getServer(), entity);
        } else {
            return null;
        }
    }

    public static Object asMinecraft(Entity entity) throws Throwable {
        return getHandle.invoke(entity);
    }
}
