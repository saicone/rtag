package com.saicone.rtag.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class EasyLookup {

    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private static final Map<Class<?>, MethodHandles.Lookup> privateLookups = new HashMap<>();
    private static final Map<String, Class<?>> classes = new HashMap<>();

    static {
        try {
            // Java
            addClass("byte[]", "[B");
            addClass("int[]", "[I");
            addClass("long[]", "[J");
            // Minecraft Server
            addNMSClass("nbt.NBTBase");
            addNMSClass("nbt.NBTTagByte");
            addNMSClass("nbt.NBTTagByteArray");
            addNMSClass("nbt.NBTTagCompound");
            addNMSClass("nbt.NBTTagDouble");
            addNMSClass("nbt.NBTTagFloat");
            addNMSClass("nbt.NBTTagInt");
            addNMSClass("nbt.NBTTagIntArray");
            addNMSClass("nbt.NBTTagList");
            addNMSClass("nbt.NBTTagLong");
            if (ServerInstance.verNumber >= 12) {
                addNMSClass("nbt.NBTTagLongArray");
            }
            addNMSClass("nbt.NBTTagShort");
            addNMSClass("nbt.NBTTagString");
            addNMSClass("nbt.NBTCompressedStreamTools");
            addNMSClass("world.item.ItemStack");
            addNMSClass("world.entity.Entity");
            addNMSClass("world.level.block.entity.TileEntity");
            addNMSClass("world.level.block.state.IBlockData");
            addNMSClass("core.BlockPosition");
            addNMSClass("world.level.World");
            addNMSClass("server.level.WorldServer");
            // Bukkit Server
            addOBCClass("CraftServer");
            addOBCClass("inventory.CraftItemStack");
            addOBCClass("inventory.CraftMetaSkull");
            addOBCClass("entity.CraftEntity");
            addOBCClass("block.CraftBlockState");
            addOBCClass("CraftWorld");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Avoid rare ClassCastException with single argument as Object[]
    public static Object safeInvoke(MethodHandle method, Object arg) {
        try {
            return method.invoke(arg);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    public static Object safeInvoke(MethodHandle method, Object... args) {
        try {
            return method.invoke(args);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    public static Class<?> classById(String id) {
        return classes.get(id);
    }

    public static Class<?> classOf(Object object) {
        if (object instanceof Class) {
            return (Class<?>) object;
        } else {
            return classById(String.valueOf(object));
        }
    }

    public static Class<?>[] classesOf(Object... classes) {
        Class<?>[] array = new Class<?>[classes.length];
        for (int i = 0; i < classes.length; i++) {
            array[i] = classOf(classes[i]);
        }
        return array;
    }

    public static void addClass(String name) throws ClassNotFoundException {
        addClass(name.substring(name.lastIndexOf('.') + 1), name);
    }

    public static void addClass(String id, String name) throws ClassNotFoundException {
        if (!classes.containsKey(id)) {
            classes.put(id, Class.forName(name));
        }
    }

    public static void addNMSClass(String name) throws ClassNotFoundException {
        addClass(nmsClass(name));
    }

    public static void addNMSClass(String id, String name) throws ClassNotFoundException {
        addClass(id, nmsClass(name));
    }

    private static String nmsClass(String name) {
        if (ServerInstance.isUniversal) {
            return "net.minecraft." + name;
        } else {
            return "net.minecraft.server." + ServerInstance.version + "." + (name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : name);
        }
    }

    public static void addOBCClass(String name) throws ClassNotFoundException {
        addClass(obcClass(name));
    }

    public static void addOBCClass(String id, String name) throws ClassNotFoundException {
        addClass(id, obcClass(name));
    }

    private static String obcClass(String name) {
        return "org.bukkit.craftbukkit." + ServerInstance.version + "." + name;
    }

    public static MethodHandles.Lookup privateLookup(Class<?> clazz) {
        return privateLookups.computeIfAbsent(clazz, c -> {
            try {
                return MethodHandles.privateLookupIn(clazz, lookup);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public static MethodHandle constructor(Object clazz, Object... classes) throws NoSuchMethodException, IllegalAccessException {
        return lookup.findConstructor(classOf(clazz), type(void.class, classes));
    }

    public static MethodHandle unreflectConstructor(Object clazz, Object... classes) throws NoSuchMethodException, IllegalAccessException {
        Constructor<?> c = classOf(clazz).getDeclaredConstructor(classesOf(classes));
        c.setAccessible(true);
        return lookup.unreflectConstructor(c);
    }

    public static MethodHandle method(Object clazz, String name, Object returnType, Object... classes) throws NoSuchMethodException, IllegalAccessException {
        return lookup.findVirtual(classOf(clazz), name, type(returnType, classes));
    }

    public static MethodHandle unreflectMethod(Object clazz, String name, Object... classes) throws NoSuchMethodException, IllegalAccessException {
        Method m = classOf(clazz).getMethod(name, classesOf(classes));
        m.setAccessible(true);
        return lookup.unreflect(m);
    }

    public static MethodHandle staticMethod(Object clazz, String name, Object returnType, Object... classes) throws NoSuchMethodException, IllegalAccessException {
        return lookup.findStatic(classOf(clazz), name, type(returnType, classes));
    }

    public static MethodHandle getter(Object clazz, String name, Object returnType) throws NoSuchFieldException, IllegalAccessException {
        return lookup.findGetter(classOf(clazz), name, classOf(returnType));
    }

    public static MethodHandle unreflectGetter(Object clazz, String name) throws NoSuchFieldException, IllegalAccessException {
        return lookup.unreflectGetter(field(classOf(clazz), name));
    }

    public static MethodHandle staticGetter(Object clazz, String name, Object returnType) throws NoSuchFieldException, IllegalAccessException {
        return lookup.findStaticGetter(classOf(clazz), name, classOf(returnType));
    }

    public static MethodHandle setter(Object clazz, String name, Object returnType) throws NoSuchFieldException, IllegalAccessException {
        return lookup.findSetter(classOf(clazz), name, classOf(returnType));
    }

    public static MethodHandle unreflectSetter(Object clazz, String name) throws NoSuchFieldException, IllegalAccessException {
        return lookup.unreflectSetter(field(classOf(clazz), name));
    }

    public static MethodHandle staticSetter(Object clazz, String name, Object returnType) throws NoSuchFieldException, IllegalAccessException {
        return lookup.findStaticSetter(classOf(clazz), name, classOf(returnType));
    }

    private static Field field(Class<?> clazz, String field) throws NoSuchFieldException {
        Field f = clazz.getDeclaredField(field);
        f.setAccessible(true);
        return f;
    }

    private static MethodType type(Object returnType, Object... classes) {
        return type(classOf(returnType), classesOf(classes));
    }

    private static MethodType type(Class<?> returnType, Class<?>... classes) {
        switch (classes.length) {
            case 0:
                return MethodType.methodType(returnType);
            case 1:
                return MethodType.methodType(returnType, classes[0]);
            default:
                return MethodType.methodType(returnType, classes[0], Arrays.copyOfRange(classes, 1, classes.length));
        }
    }
}
