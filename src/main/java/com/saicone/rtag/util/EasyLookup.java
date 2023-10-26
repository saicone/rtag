package com.saicone.rtag.util;

import org.bukkit.Bukkit;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
    private static final MethodPredicate[] methodPredicates = new MethodPredicate[] {
            (m, type, params) -> m.getReturnType().equals(type) && Arrays.equals(m.getParameterTypes(), params),
            (m, type, params) -> m.getReturnType().isAssignableFrom(type) && Arrays.equals(m.getParameterTypes(), params),
            (m, type, params) -> m.getReturnType().equals(type) && isAssignableFrom(m.getParameterTypes(), params),
            (m, type, params) -> m.getReturnType().isAssignableFrom(type) && isAssignableFrom(m.getParameterTypes(), params),
            (m, type, params) -> Arrays.equals(m.getParameterTypes(), params),
            (m, type, params) -> isAssignableFrom(m.getParameterTypes(), params)
    };
    private static final String nmsPackage = ServerInstance.isUniversal ? "net.minecraft." : ("net.minecraft.server." + ServerInstance.version + ".");
    private static final String obcPackage = Bukkit.getServer().getClass().getPackage().getName() + ".";

    static {
        try {
            // Java
            addClassId("byte[]", "[B");
            addClassId("int[]", "[I");
            addClassId("long[]", "[J");
            // Minecraft Server
            addNMSClass("nbt.NBTBase", "Tag");
            addNMSClass("nbt.NBTTagByte", "ByteTag");
            addNMSClass("nbt.NBTTagByteArray", "ByteArrayTag");
            addNMSClass("nbt.NBTTagCompound", "CompoundTag");
            addNMSClass("nbt.NBTTagDouble", "DoubleTag");
            addNMSClass("nbt.NBTTagFloat", "FloatTag");
            addNMSClass("nbt.NBTTagInt", "IntTag");
            addNMSClass("nbt.NBTTagIntArray", "IntArrayTag");
            addNMSClass("nbt.NBTTagList", "ListTag");
            addNMSClass("nbt.NBTTagLong", "LongTag");
            if (ServerInstance.verNumber >= 12) {
                addNMSClass("nbt.NBTTagLongArray", "LongArrayTag");
            }
            addNMSClass("nbt.NBTTagShort", "ShortTag");
            addNMSClass("nbt.NBTTagString", "StringTag");
            addNMSClass("world.item.ItemStack");
            addNMSClass("world.entity.Entity");
            addNMSClass("world.level.block.entity.TileEntity", "BlockEntity");
            addNMSClass("world.level.block.state.IBlockData", "BlockState");
            addNMSClass("core.BlockPosition", "BlockPos");
            addNMSClass("world.level.World", "Level");
            addNMSClass("server.level.WorldServer", "ServerLevel");
            // Bukkit Server
            addOBCClass("CraftServer");
            addOBCClass("inventory.CraftItemStack");
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
     * Test the availability of provided class name using {@link Class#forName(String)}.
     *
     * @param name Class name.
     * @return     true if the provided class exists.
     */
    public static boolean testClass(String name) {
        boolean test = false;
        try {
            Class.forName(name);
            test = true;
        } catch (Throwable ignored) { }
        return test;
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
     * @param name    Class name.
     * @return        Added class.
     * @throws ClassNotFoundException if the class cannot be located.
     */
    public static Class<?> addClass(String name) throws ClassNotFoundException {
        return addClass(name, new String[0]);
    }

    /**
     * Same has {@link Class#forName(String)} but save the class into memory.
     *
     * @param name    Class name.
     * @param aliases Alternative class names.
     * @return        Added class.
     * @throws ClassNotFoundException if the class cannot be located.
     */
    public static Class<?> addClass(String name, String... aliases) throws ClassNotFoundException {
        return addClassId(name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : name, name, aliases);
    }

    /**
     * Same has {@link Class#forName(String)} but save the class into memory
     * with provided ID to get from {@link #classById(String)}.
     *
     * @param id      Class ID.
     * @param name    Class name.
     * @param aliases Alternative class names.
     * @return        Added class.
     * @throws ClassNotFoundException if the class cannot be located.
     */
    public static Class<?> addClassId(String id, String name, String... aliases) throws ClassNotFoundException {
        String finalName = null;
        if (testClass(name)) {
            finalName = name;
        } else if (aliases.length > 0) {
            final String pkg = name.contains(".") ? name.substring(0, name.lastIndexOf('.') + 1) : "";
            for (String alias : aliases) {
                final String aliasName = (alias.contains(".") ? "" : pkg) + alias;
                if (testClass(aliasName)) {
                    finalName = aliasName;
                    break;
                }
            }
        }

        if (finalName == null) {
            throw new ClassNotFoundException(name);
        }
        return addClassId(id, Class.forName(finalName));
    }

    /**
     * Save class into memory with provided ID to get from {@link #classById(String)}.
     *
     * @param id    Class ID.
     * @param clazz Class object.
     * @return      Added class.
     */
    public static Class<?> addClassId(String id, Class<?> clazz) {
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
     * @param name    Class name.
     * @return        Added class.
     * @throws ClassNotFoundException if the class cannot be located.
     */
    public static Class<?> addNMSClass(String name) throws ClassNotFoundException {
        return addNMSClass(name, new String[0]);
    }

    /**
     * Save the typically net.minecraft.server class into memory.<br>
     * For +1.17 servers compatibility the name must be the full class path
     * after "net.minecraft."
     *
     * @param name    Class name.
     * @param aliases Alternative class names.
     * @return        Added class.
     * @throws ClassNotFoundException if the class cannot be located.
     */
    public static Class<?> addNMSClass(String name, String... aliases) throws ClassNotFoundException {
        return addClass(nmsClass(name, aliases), aliases);
    }

    /**
     * Save the typically net.minecraft.server class into memory with specified ID.<br>
     * For +1.17 servers compatibility the name must be the full class path
     * after "net.minecraft."
     *
     * @param id      Class ID.
     * @param name    Class name.
     * @param aliases Alternative class names.
     * @return        Added class.
     * @throws ClassNotFoundException if the class cannot be located.
     */
    public static Class<?> addNMSClassId(String id, String name, String... aliases) throws ClassNotFoundException {
        return addClassId(id, nmsClass(name, aliases), aliases);
    }

    private static String nmsClass(String name, String... aliases) {
        for (int i = 0; i < aliases.length; i++) {
            final String alias = aliases[i];
            if (alias.contains(".")) {
                aliases[i] = nmsPackage + alias;
            }
        }
        if (ServerInstance.isUniversal) {
            return nmsPackage + name;
        } else {
            return nmsPackage + (name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : name);
        }
    }

    /**
     * Save the typically org.bukkit.craftbukkit class into memory.<br>
     * Name must be the full path after craftbukkit package.
     *
     * @param name    Class name.
     * @return        Added class.
     * @throws ClassNotFoundException if the class cannot be located.
     */
    public static Class<?> addOBCClass(String name) throws ClassNotFoundException {
        return addOBCClass(name, new String[0]);
    }

    /**
     * Save the typically org.bukkit.craftbukkit class into memory.<br>
     * Name must be the full path after craftbukkit package.
     *
     * @param name    Class name.
     * @param aliases Alternative class names.
     * @return        Added class.
     * @throws ClassNotFoundException if the class cannot be located.
     */
    public static Class<?> addOBCClass(String name, String... aliases) throws ClassNotFoundException {
        return addClass(obcClass(name, aliases), aliases);
    }

    /**
     * Save the typically org.bukkit.craftbukkit class into memory with specified ID.<br>
     * Name must be the full path after craftbukkit package.
     *
     * @param id      Class ID.
     * @param name    Class name.
     * @param aliases Alternative class names.
     * @return        Added class.
     * @throws ClassNotFoundException if the class cannot be located.
     */
    public static Class<?> addOBCClassId(String id, String name, String... aliases) throws ClassNotFoundException {
        return addClassId(id, obcClass(name, aliases), aliases);
    }

    private static String obcClass(String name, String... aliases) {
        for (int i = 0; i < aliases.length; i++) {
            final String alias = aliases[i];
            if (alias.contains(".")) {
                aliases[i] = obcPackage + alias;
            }
        }
        return obcPackage + name;
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
     * @param clazz          Class to find constructor.
     * @param parameterTypes Required classes in constructor.
     * @return               A MethodHandle representing class constructor.
     * @throws NoSuchMethodException  if the constructor does not exist.
     * @throws IllegalAccessException if access checking fails or if the method's variable arity
     *                                modifier bit is set and asVarargsCollector fails.
     */
    public static MethodHandle constructor(Object clazz, Object... parameterTypes) throws NoSuchMethodException, IllegalAccessException {
        final Class<?> from = classOf(clazz);
        try {
            return lookup.findConstructor(from, type(void.class, parameterTypes));
        } catch (IllegalAccessException e) {
            return unreflectConstructor(from, parameterTypes);
        } catch (NoSuchMethodException e) {
            return unreflectConstructor(findConstructor(from, classesOf(parameterTypes)));
        }
    }

    /**
     * Same has {@link MethodHandles.Lookup#unreflectConstructor(Constructor)},
     * but this method makes the constructor accessible if unreflection fails.
     *
     * @param constructor The reflected constructor.
     * @return            A MethodHandle representing constructor.
     * @throws IllegalAccessException if access checking fails or if the method's variable arity
     *                                modifier bit is set and asVarargsCollector fails.
     */
    public static MethodHandle unreflectConstructor(Constructor<?> constructor) throws IllegalAccessException {
        try {
            return lookup.unreflectConstructor(constructor);
        } catch (IllegalAccessException e) {
            constructor.setAccessible(true);
            return lookup.unreflectConstructor(constructor);
        }
    }

    /**
     * Easy way to invoke {@link MethodHandles.Lookup#unreflectConstructor(Constructor)},
     * this method creates an accessible {@link Constructor} and unreflect
     * it, useful for private constructors.<br>
     *
     * Required classes can be Strings to get by {@link #classById(String)}.
     *
     * @param clazz          Class to find constructor.
     * @param parameterTypes Required classes in constructor.
     * @return               A MethodHandle representing class constructor.
     * @throws NoSuchMethodException  if a matching method is not found.
     * @throws IllegalAccessException if access checking fails or if the method's variable arity
     *                                modifier bit is set and asVarargsCollector fails.
     */
    public static MethodHandle unreflectConstructor(Object clazz, Object... parameterTypes) throws NoSuchMethodException, IllegalAccessException {
        Constructor<?> c = classOf(clazz).getDeclaredConstructor(classesOf(parameterTypes));
        c.setAccessible(true);
        return lookup.unreflectConstructor(c);
    }

    /**
     * Find constructor inside class using recursive searching and
     * invoke {@link MethodHandles.Lookup#unreflectConstructor(Constructor)}.<br>
     *
     * Required classes can be Strings to get by {@link #classById(String)}.
     *
     * @param from           Class to find constructor.
     * @param parameterTypes Required classes in constructor.
     * @return               A constructor for provided class.
     * @throws NoSuchMethodException  if a matching method is not found.
     * @throws IllegalAccessException if access checking fails or if the method's variable arity
     *                                modifier bit is set and asVarargsCollector fails.
     */
    public static Constructor<?> findConstructor(Class<?> from, Class<?>... parameterTypes) throws NoSuchMethodException, IllegalAccessException {
        // Find with reflection
        try {
            return from.getDeclaredConstructor(parameterTypes);
        } catch (NoSuchMethodException ignored) { }
        // Find using constructor parameters
        for (Constructor<?> constructor : from.getDeclaredConstructors()) {
            if (isAssignableFrom(constructor.getParameterTypes(), parameterTypes)) {
                return constructor;
            }
        }
        throw new NoSuchMethodException("Cannot find a constructor like '" + from.getName() + '(' + String.join(", ", names(parameterTypes)) + ")'");
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
     * @param clazz          Class to find public method.
     * @param name           Method name.
     * @param returnType     Return type class for method.
     * @param parameterTypes Required classes in method.
     * @return               A MethodHandle representing a instance method for provided class.
     * @throws NoSuchMethodException  if the method does not exist.
     * @throws IllegalAccessException if access checking fails, or if the method is static, or if the method's
     *                                variable arity modifier bit is set and asVarargsCollector fails.
     */
    public static MethodHandle method(Object clazz, String name, Object returnType, Object... parameterTypes) throws NoSuchMethodException, IllegalAccessException {
        final Class<?> from = classOf(clazz);
        try {
            return lookup.findVirtual(from, name, type(returnType, parameterTypes));
        } catch (IllegalAccessException e) {
            return unreflectMethod(from, name, parameterTypes);
        } catch (NoSuchMethodException e) {
            return unreflectMethod(findMethod(from, false, name, classOf(returnType), classesOf(parameterTypes)));
        }
    }

    /**
     * Easy way to invoke {@link MethodHandles.Lookup#unreflect(Method)},
     * but this method makes the method accessible if unreflection fails.
     *
     * @param method Method to unreflect.
     * @return       A MethodHandle representing a method.
     * @throws IllegalAccessException if access checking fails or if the method's variable arity
     *                                modifier bit is set and asVarargsCollector fails.
     */
    public static MethodHandle unreflectMethod(Method method) throws IllegalAccessException {
        try {
            return lookup.unreflect(method);
        } catch (IllegalAccessException e) {
            method.setAccessible(true);
            return lookup.unreflect(method);
        }
    }

    /**
     * Easy way to invoke {@link MethodHandles.Lookup#unreflect(Method)},
     * this method creates and accessible {@link Method} and
     * unreflect it, can be static or instance method.<br>
     *
     * Required classes can be Strings to get by {@link #classById(String)}.
     *
     * @param clazz          Class to find method.
     * @param name           Method name.
     * @param parameterTypes Required classes in method.
     * @return               A MethodHandle representing a method for provided class.
     * @throws NoSuchMethodException  if a matching method is not found or if the name is "&lt;init&gt;"or "&lt;clinit&gt;".
     * @throws IllegalAccessException if access checking fails or if the method's variable arity
     *                                modifier bit is set and asVarargsCollector fails.
     */
    public static MethodHandle unreflectMethod(Object clazz, String name, Object... parameterTypes) throws NoSuchMethodException, IllegalAccessException {
        Method m = classOf(clazz).getDeclaredMethod(name, classesOf(parameterTypes));
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
     * @param clazz          Class to find method.
     * @param name           Method name.
     * @param returnType     Return type class for method.
     * @param parameterTypes Required classes in method.
     * @return               A MethodHandle representing a static method for provided class.
     * @throws NoSuchMethodException  if the method does not exist.
     * @throws IllegalAccessException if access checking fails, or if the method is not static, or if the method's
     *                                variable arity modifier bit is set and asVarargsCollector fails.
     */
    public static MethodHandle staticMethod(Object clazz, String name, Object returnType, Object... parameterTypes) throws NoSuchMethodException, IllegalAccessException {
        final Class<?> from = classOf(clazz);
        try {
            return lookup.findStatic(from, name, type(returnType, parameterTypes));
        } catch (IllegalAccessException e) {
            return unreflectMethod(from, name, parameterTypes);
        } catch (NoSuchMethodException e) {
            return unreflectMethod(findMethod(from, true, name, classOf(returnType), classesOf(parameterTypes)));
        }
    }

    /**
     * Find method inside class using recursirve searching and
     * invoke {@link MethodHandles.Lookup#unreflect(Method)}.<br>
     *
     * Required classes can be Strings to get by {@link #classById(String)}.
     *
     * @param from           Class to find method.
     * @param isStatic       True if method is static.
     * @param name           Method name.
     * @param returnType     Return type class for method.
     * @param parameterTypes Required classes in method.
     * @return               A method from provided class.
     * @throws NoSuchMethodException  if the method does not exist.
     * @throws IllegalAccessException if access checking fails, or if the method is not static, or if the method's
     *                                variable arity modifier bit is set and asVarargsCollector fails.
     */
    public static Method findMethod(Class<?> from, boolean isStatic, String name, Class<?> returnType, Class<?>... parameterTypes) throws NoSuchMethodException, IllegalAccessException {
        // Find with reflection
        try {
            return from.getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException ignored) { }
        // Find using method information
        final Method[] declaredMethods = Arrays.stream(from.getDeclaredMethods()).filter(m -> Modifier.isStatic(m.getModifiers()) == isStatic).toArray(Method[]::new);

        Method foundMethod = null;
        for (MethodPredicate predicate : methodPredicates) {
            for (Method method : declaredMethods) {
                if (predicate.test(method, returnType, parameterTypes)) {
                    if (method.getName().equals(name)) {
                        return method;
                    } else if (foundMethod == null) {
                        foundMethod = method;
                    }
                }
            }
            if (foundMethod != null) {
                break;
            }
        }
        if (foundMethod != null) {
            return foundMethod;
        }
        throw new NoSuchMethodException("Cannot find a method like '" + (isStatic ? "static " : "") + returnType.getName() + ' ' + name + '(' + String.join(", ", names(parameterTypes)) + ")' inside class " + from.getName());
    }

    /**
     * Same has {@link Class#isAssignableFrom(Class)} but using class arrays.
     *
     * @param methodParameters The Class array that check.
     * @param parameterTypes   The Class array to be checked.
     * @return                 true if parameterTypes can be assigned to methodParameters in respecting order.
     */
    public static boolean isAssignableFrom(Class<?>[] methodParameters, Class<?>[] parameterTypes) {
        if (methodParameters.length != parameterTypes.length) {
            return false;
        }
        for (int i = 0; i < methodParameters.length; i++) {
            if (!methodParameters[i].isAssignableFrom(parameterTypes[i])) {
                return false;
            }
        }
        return true;
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
        final Class<?> from = classOf(clazz);
        final Class<?> type = classOf(returnType);
        try {
            return lookup.findGetter(from, name, type);
        } catch (IllegalAccessException e) {
            return unreflectGetter(from, name);
        } catch (NoSuchFieldException e) {
            return unreflectGetter(findField(from, false, name, type));
        }
    }

    /**
     * Same has {@link MethodHandles.Lookup#unreflectGetter(Field)},
     * but this method makes the field accessible if unreflection fails.<br>
     *
     * Required classes can be Strings to get by {@link #classById(String)}.
     *
     * @param field Field to unreflect.
     * @return      A MethodHandle representing a field getter for provided class.
     * @throws IllegalAccessException if access checking fails.
     */
    public static MethodHandle unreflectGetter(Field field) throws IllegalAccessException {
        try {
            return lookup.unreflectGetter(field);
        } catch (IllegalAccessException e) {
            field.setAccessible(true);
            return lookup.unreflectGetter(field);
        }
    }

    /**
     * Easy way to invoke {@link MethodHandles.Lookup#unreflectGetter(Field)},
     * this method creates an accessible {@link Field} and unreflect it,
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
        final Class<?> from = classOf(clazz);
        final Class<?> type = classOf(returnType);
        try {
            return lookup.findStaticGetter(from, name, type);
        } catch (IllegalAccessException e) {
            return unreflectGetter(from, name);
        } catch (NoSuchFieldException e) {
            return unreflectGetter(findField(from, true, name, type));
        }
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
        final Class<?> from = classOf(clazz);
        final Class<?> type = classOf(returnType);
        try {
            return lookup.findSetter(from, name, type);
        } catch (IllegalAccessException e) {
            return unreflectSetter(from, name);
        } catch (NoSuchFieldException e) {
            return unreflectSetter(findField(from, false, name, type));
        }
    }

    /**
     * Same has {@link MethodHandles.Lookup#unreflectSetter(Field)},
     * but this method makes the field accessible if unreflection fails.<br>
     *
     * Required classes can be Strings to get by {@link #classById(String)}.
     *
     * @param field Field to unreflect.
     * @return      A MethodHandle representing a field setter for provided class.
     * @throws IllegalAccessException if access checking fails.
     */
    public static MethodHandle unreflectSetter(Field field) throws IllegalAccessException {
        try {
            return lookup.unreflectSetter(field);
        } catch (IllegalAccessException e) {
            field.setAccessible(true);
            return lookup.unreflectSetter(field);
        }
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
        final Class<?> from = classOf(clazz);
        final Class<?> type = classOf(returnType);
        try {
            return lookup.findStaticSetter(from, name, type);
        } catch (IllegalAccessException e) {
            return unreflectSetter(from, name);
        } catch (NoSuchFieldException e) {
            return unreflectSetter(findField(from, true, name, type));
        }
    }

    /**
     * Find field inside class using recursive searching and return it<br>
     *
     * @param from     Class to find the field.
     * @param isStatic True if field is static.
     * @param name     Field name.
     * @param type     Return type class for provided field name.
     * @return           A field from provided class.
     * @throws NoSuchFieldException   if the field does not exist.
     * @throws IllegalAccessException if access checking fails, or if the field is not static.
     */
    public static Field findField(Class<?> from, boolean isStatic, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException {
        // Find with name
        try {
            final Field field = from.getDeclaredField(name);
            if (Modifier.isStatic(field.getModifiers()) == isStatic && field.getType().isAssignableFrom(type)) {
                return field;
            }
        } catch (NoSuchFieldException ignored) { }
        // Find using field type
        for (Field field : from.getDeclaredFields()) {
            if (field.getType().equals(type)) {
                return field;
            }
        }
        for (Field field : from.getDeclaredFields()) {
            if (field.getType().isAssignableFrom(type)) {
                return field;
            }
        }
        throw new NoSuchFieldException("Cannot find a field like '" + (isStatic ? "static " : "") + type.getName() + ' ' + name + "' inside class " + from.getName());
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

    private static String[] names(Class<?>[] classes) {
        final String[] names = new String[classes.length];
        for (int i = 0; i < classes.length; i++) {
            names[i] = classes[i].getName();
        }
        return names;
    }

    /**
     * Boolean valued function to compare a method with return and parameter types.
     *
     * @author Rubenicos
     */
    @FunctionalInterface
    public interface MethodPredicate {

        /**
         * Eval this predicate with current arguments.
         *
         * @param method         method to compare.
         * @param returnType     method return type.
         * @param parameterTypes method parameter types as array.
         * @return               true if method matches.
         */
        boolean test(Method method, Class<?> returnType, Class<?>[] parameterTypes);
    }
}
