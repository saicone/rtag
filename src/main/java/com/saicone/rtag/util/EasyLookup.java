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

/**
 * Class to handle reflection lookups in a easy way.
 *
 * @author Rubenicos
 */
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

    EasyLookup() {
    }

    /**
     * Invoke {@link MethodHandle} without exception.
     *
     * @param method MethodHandle to invoke.
     * @param arg    Single argument
     * @return       MethodHandle result.
     */
    public static Object safeInvoke(MethodHandle method, Object arg) {
        try {
            return method.invoke(arg);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Invoke {@link MethodHandle} without exception.
     *
     * @param method MethodHandle to invoke.
     * @param args   Arguments.
     * @return       MethodHandle result.
     */
    public static Object safeInvoke(MethodHandle method, Object... args) {
        try {
            return method.invoke(args);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Get previously saved class by it ID.
     *
     * @param id Class ID.
     * @return   A class represented by provided ID or null.
     */
    public static Class<?> classById(String id) {
        return classes.get(id);
    }

    /**
     * Get class represented by Object.<br>
     * If object is instance of Class will return itself,
     * otherwise return {@link #classById(String)}.
     *
     * @param object Class or String.
     * @return       A class represented by provided object or null.
     */
    public static Class<?> classOf(Object object) {
        if (object instanceof Class) {
            return (Class<?>) object;
        } else {
            return classById(String.valueOf(object));
        }
    }

    /**
     * Same has {@link #classOf(Object)} but for multiple objects.
     *
     * @param classes Classes objects.
     * @return        A array of classes represented by objects.
     */
    public static Class<?>[] classesOf(Object... classes) {
        Class<?>[] array = new Class<?>[classes.length];
        for (int i = 0; i < classes.length; i++) {
            array[i] = classOf(classes[i]);
        }
        return array;
    }

    /**
     * Same has {@link Class#forName(String)} but save the class into memory.
     *
     * @param name Class name.
     * @return     Added class.
     * @throws ClassNotFoundException if the class cannot be located.
     */
    public static Class<?> addClass(String name) throws ClassNotFoundException {
        return addClass(name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : name, name);
    }

    /**
     * Same has {@link Class#forName(String)} but save the class into memory
     * with provided ID to get from {@link #classById(String)}.
     *
     * @param id   Class ID.
     * @param name Class name.
     * @return     Added class.
     * @throws ClassNotFoundException if the class cannot be located.
     */
    public static Class<?> addClass(String id, String name) throws ClassNotFoundException {
        return addClass(id, Class.forName(name));
    }

    /**
     * Save class into memory with provided ID to get from {@link #classById(String)}.
     *
     * @param id    Class ID.
     * @param clazz Class object.
     * @return      Added class.
     */
    public static Class<?> addClass(String id, Class<?> clazz) {
        if (!classes.containsKey(id)) {
            classes.put(id, clazz);
        }
        return clazz;
    }

    /**
     * Save the typically net.minecraft.server class into memory.<br>
     * For +1.17 servers compatibility the name must be the full class path
     * after "net.minecraft."
     *
     * @param name Class name.
     * @return     Added class.
     * @throws ClassNotFoundException if the class cannot be located.
     */
    public static Class<?> addNMSClass(String name) throws ClassNotFoundException {
        return addClass(nmsClass(name));
    }

    /**
     * Save the typically net.minecraft.server class into memory with specified ID.<br>
     * For +1.17 servers compatibility the name must be the full class path
     * after "net.minecraft."
     *
     * @param id   Class ID.
     * @param name Class name.
     * @return     Added class.
     * @throws ClassNotFoundException if the class cannot be located.
     */
    public static Class<?> addNMSClass(String id, String name) throws ClassNotFoundException {
        return addClass(id, nmsClass(name));
    }

    private static String nmsClass(String name) {
        if (ServerInstance.isUniversal) {
            return "net.minecraft." + name;
        } else {
            return "net.minecraft.server." + ServerInstance.version + "." + (name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : name);
        }
    }

    /**
     * Save the typically org.bukkit.craftbukkit class into memory.<br>
     * Name must be the full path after "org.bukkit.craftbukkit.{@link ServerInstance#version}."
     *
     * @param name Class name.
     * @return     Added class.
     * @throws ClassNotFoundException if the class cannot be located.
     */
    public static Class<?> addOBCClass(String name) throws ClassNotFoundException {
        return addClass(obcClass(name));
    }

    /**
     * Save the typically org.bukkit.craftbukkit class into memory with specified ID.<br>
     * Name must be the full path after "org.bukkit.craftbukkit.{@link ServerInstance#version}."
     *
     * @param id   Class ID.
     * @param name Class name.
     * @return     Added class.
     * @throws ClassNotFoundException if the class cannot be located.
     */
    public static Class<?> addOBCClass(String id, String name) throws ClassNotFoundException {
        return addClass(id, obcClass(name));
    }

    private static String obcClass(String name) {
        return "org.bukkit.craftbukkit." + ServerInstance.version + "." + name;
    }

    /**
     * Same has {@link MethodHandles#privateLookupIn(Class, MethodHandles.Lookup)} but save the result into memory.
     *
     * @param clazz Target class.
     * @return      Private lookup or null.
     */
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

    /**
     * Easy way to invoke {@link MethodHandles.Lookup#findConstructor(Class, MethodType)} without
     * creating a MethodType, it also provide void class at first argument has default.<br>
     *
     * Required classes can be Strings to get by {@link #classById(String)}.<br>
     *
     * See also {@link #unreflectConstructor(Object, Object...)} for private constructors.
     *
     * @param clazz   Class to find constructor.
     * @param classes Required classes in constructor.
     * @return        A MethodHandle representing class constructor.
     * @throws NoSuchMethodException  if the constructor does not exist.
     * @throws IllegalAccessException if access checking fails or if the method's variable arity
     *                                modifier bit is set and asVarargsCollector fails.
     */
    public static MethodHandle constructor(Object clazz, Object... classes) throws NoSuchMethodException, IllegalAccessException {
        return lookup.findConstructor(classOf(clazz), type(void.class, classes));
    }

    /**
     * Easy way to invoke {@link MethodHandles.Lookup#unreflectConstructor(Constructor)},
     * this method creates an accessible {@link Constructor} and unreflect
     * it, useful for private constructors.<br>
     *
     * Required classes can be Strings to get by {@link #classById(String)}.
     *
     * @param clazz   Class to find constructor.
     * @param classes Required classes in constructor.
     * @return        A MethodHandle representing class constructor.
     * @throws NoSuchMethodException  if a matching method is not found.
     * @throws IllegalAccessException if access checking fails or if the method's variable arity
     *                                modifier bit is set and asVarargsCollector fails.
     */
    public static MethodHandle unreflectConstructor(Object clazz, Object... classes) throws NoSuchMethodException, IllegalAccessException {
        Constructor<?> c = classOf(clazz).getDeclaredConstructor(classesOf(classes));
        c.setAccessible(true);
        return lookup.unreflectConstructor(c);
    }

    /**
     * Easy way to invoke {@link MethodHandles.Lookup#findVirtual(Class, String, MethodType)} without
     * creating a MethodType, this method require to specify the return type class of reflected {@link Method}
     * and its only compatible with instance public methods, see {@link #staticMethod(Object, String, Object, Object...)}
     * for static public methods.<br>
     *
     * Required classes can be Strings to get by {@link #classById(String)}.<br>
     *
     * See also {@link #unreflectMethod(Object, String, Object...)} for private methods.
     *
     * @param clazz      Class to find public method.
     * @param name       Method name.
     * @param returnType Return type class for method.
     * @param classes    Required classes in method.
     * @return           A MethodHandle representing a instance method for provided class.
     * @throws NoSuchMethodException  if the method does not exist.
     * @throws IllegalAccessException if access checking fails, or if the method is static, or if the method's
     *                                variable arity modifier bit is set and asVarargsCollector fails.
     */
    public static MethodHandle method(Object clazz, String name, Object returnType, Object... classes) throws NoSuchMethodException, IllegalAccessException {
        return lookup.findVirtual(classOf(clazz), name, type(returnType, classes));
    }

    /**
     * Easy way to invoke {@link MethodHandles.Lookup#unreflect(Method)},
     * this method creates and accessible {@link Method} and
     * unreflect it, can be static or instance method.<br>
     *
     * Required classes can be Strings to get by {@link #classById(String)}.
     *
     * @param clazz   Class to find method.
     * @param name    Method name.
     * @param classes Required classes in method.
     * @return        A MethodHandle representing a method for provided class.
     * @throws NoSuchMethodException  if a matching method is not found or if the name is "&lt;init&gt;"or "&lt;clinit&gt;".
     * @throws IllegalAccessException if access checking fails or if the method's variable arity
     *                                modifier bit is set and asVarargsCollector fails.
     */
    public static MethodHandle unreflectMethod(Object clazz, String name, Object... classes) throws NoSuchMethodException, IllegalAccessException {
        Method m = classOf(clazz).getDeclaredMethod(name, classesOf(classes));
        m.setAccessible(true);
        return lookup.unreflect(m);
    }

    /**
     * Easy way to invoke {@link MethodHandles.Lookup#findStatic(Class, String, MethodType)} without
     * creating a MethodType, this method require to specify the return type class of reflected {@link Method}
     * and only compatible with static methods.<br>
     *
     * Required classes can be Strings to get by {@link #classById(String)}.<br>
     *
     * See also {@link #unreflectMethod(Object, String, Object...)} for private methods.
     *
     * @param clazz      Class to find method.
     * @param name       Method name.
     * @param returnType Return type class for method.
     * @param classes    Required classes in method.
     * @return           A MethodHandle representing a static method for provided class.
     * @throws NoSuchMethodException  if the method does not exist.
     * @throws IllegalAccessException if access checking fails, or if the method is not static, or if the method's
     *                                variable arity modifier bit is set and asVarargsCollector fails.
     */
    public static MethodHandle staticMethod(Object clazz, String name, Object returnType, Object... classes) throws NoSuchMethodException, IllegalAccessException {
        return lookup.findStatic(classOf(clazz), name, type(returnType, classes));
    }

    /**
     * Easy way to invoke {@link MethodHandles.Lookup#findGetter(Class, String, Class)} without
     * creating a MethodType, this method require to specify the return type class of {@link Field}
     * and its only compatible with public instance fields, see {@link #staticGetter(Object, String, Object)}
     * for static getter.<br>
     *
     * Required classes can be Strings to get by {@link #classById(String)}.<br>
     *
     * See also {@link #unreflectGetter(Object, String)} for private getters.
     *
     * @param clazz      Class to find getter.
     * @param name       Field name.
     * @param returnType Return type class for provided field name.
     * @return           A MethodHandle representing a field getter for provided class.
     * @throws NoSuchFieldException   if the field does not exist.
     * @throws IllegalAccessException  if access checking fails, or if the field is static.
     */
    public static MethodHandle getter(Object clazz, String name, Object returnType) throws NoSuchFieldException, IllegalAccessException {
        return lookup.findGetter(classOf(clazz), name, classOf(returnType));
    }

    /**
     * Easy way to invoke {@link MethodHandles.Lookup#unreflectGetter(Field)},
     * this method creates a accessible {@link Field} and unreflect it,
     * can be static or instance field.<br>
     *
     * Required classes can be Strings to get by {@link #classById(String)}.
     *
     * @param clazz Class to find getter.
     * @param name  Field name.
     * @return      A MethodHandle representing a field getter for provided class.
     * @throws NoSuchFieldException   if a field with the specified name is not found.
     * @throws IllegalAccessException if access checking fails.
     */
    public static MethodHandle unreflectGetter(Object clazz, String name) throws NoSuchFieldException, IllegalAccessException {
        return lookup.unreflectGetter(field(classOf(clazz), name));
    }

    /**
     * Easy way to invoke {@link MethodHandles.Lookup#findStaticGetter(Class, String, Class)} without
     * creating a MethodType, this method require to specify the return type class of {@link Field}
     * and its only compatible with instance fields<br>
     *
     * Required classes can be Strings to get by {@link #classById(String)}.<br>
     *
     * See also {@link #unreflectGetter(Object, String)} for private getters.
     *
     * @param clazz      Class to find getter.
     * @param name       Field name.
     * @param returnType Return type class for provided field name.
     * @return           A MethodHandle representing a field getter for provided class.
     * @throws NoSuchFieldException   if the field does not exist.
     * @throws IllegalAccessException if access checking fails, or if the field is not static.
     */
    public static MethodHandle staticGetter(Object clazz, String name, Object returnType) throws NoSuchFieldException, IllegalAccessException {
        return lookup.findStaticGetter(classOf(clazz), name, classOf(returnType));
    }

    /**
     * Easy way to invoke {@link MethodHandles.Lookup#findSetter(Class, String, Class)} without
     * creating a MethodType, this method require to specify the return type class of {@link Field}
     * and its only compatible with public instance fields, see {@link #staticSetter(Object, String, Object)}
     * for static setter.<br>
     *
     * Required classes can be Strings to get by {@link #classById(String)}.<br>
     *
     * See also {@link #unreflectSetter(Object, String)} for private setters.
     *
     * @param clazz      Class to find setter.
     * @param name       Field name.
     * @param returnType Return type class for provided field name.
     * @return           A MethodHandle representing a field setter for provided class.
     * @throws NoSuchFieldException   if the field does not exist.
     * @throws IllegalAccessException  if access checking fails, or if the field is static.
     */
    public static MethodHandle setter(Object clazz, String name, Object returnType) throws NoSuchFieldException, IllegalAccessException {
        return lookup.findSetter(classOf(clazz), name, classOf(returnType));
    }

    /**
     * Easy way to invoke {@link MethodHandles.Lookup#unreflectSetter(Field)},
     * this method creates a accessible {@link Field} and unreflect it,
     * can be static or instance field.<br>
     *
     * Required classes can be Strings to get by {@link #classById(String)}.
     *
     * @param clazz Class to find setter.
     * @param name  Field name.
     * @return      A MethodHandle representing a field setter for provided class.
     * @throws NoSuchFieldException   if a field with the specified name is not found.
     * @throws IllegalAccessException if access checking fails.
     */
    public static MethodHandle unreflectSetter(Object clazz, String name) throws NoSuchFieldException, IllegalAccessException {
        return lookup.unreflectSetter(field(classOf(clazz), name));
    }

    /**
     * Easy way to invoke {@link MethodHandles.Lookup#findStaticSetter(Class, String, Class)} without
     * creating a MethodType, this method require to specify the return type class of {@link Field}
     * and its only compatible with instance fields<br>
     *
     * Required classes can be Strings to get by {@link #classById(String)}.<br>
     *
     * See also {@link #unreflectSetter(Object, String)} for private setters.
     *
     * @param clazz      Class to find setter.
     * @param name       Field name.
     * @param returnType Return type class for provided field name.
     * @return           A MethodHandle representing a field setter for provided class.
     * @throws NoSuchFieldException   if the field does not exist.
     * @throws IllegalAccessException if access checking fails, or if the field is not static.
     */
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
