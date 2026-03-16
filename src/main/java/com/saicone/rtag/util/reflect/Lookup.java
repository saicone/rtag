package com.saicone.rtag.util.reflect;

import com.saicone.rtag.util.MC;
import com.saicone.rtag.util.ServerInstance;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;

@ApiStatus.Internal
public class Lookup {

    public static final Runtime RUNTIME = new Runtime(Lookup.class.getClassLoader());

    public static final Runtime SERVER = new Runtime(
            Lookup.class.getClassLoader(),
            ServerInstance.Type.MOJANG_MAPPED ? reference -> null : Remapper.mojangToSpigot(MC.version())
    ) {
        private final String minecraftPackage = "net.minecraft.server." + MC.version().bukkitPackage() + ".";
        private final String craftbukkitPackage = "org.bukkit.craftbukkit." + MC.version().bukkitPackage() + ".";

        @Override
        public @Nullable Reference map(@NotNull Reference reference) {
            Reference result = super.map(reference);
            if (result != null && result.isClass() && result.getParent() instanceof String) {
                final String mapped = mapClass((String) result.getParent());
                if (mapped != null) {
                    return Reference.clazz(mapped);
                }
            } else if (result == null && reference.isClass() && reference.getParent() instanceof String) {
                final String mapped = mapClass((String) reference.getParent());
                if (mapped != null) {
                    return Reference.clazz(mapped);
                }
            }
            return result;
        }

        @Nullable
        private String mapClass(@NotNull String name) {
            if (!MC.version().isUniversal() && name.startsWith("net.minecraft.") && !name.startsWith(minecraftPackage)) {
                return minecraftPackage + simpleName(name);
            }
            if (!ServerInstance.Type.CRAFTBUKKIT_RELOCATED && name.startsWith("org.bukkit.craftbukkit.") && !name.startsWith(craftbukkitPackage)) {
                return craftbukkitPackage + name.substring("org.bukkit.craftbukkit.".length());
            }
            return null;
        }
    };

    Lookup() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T invoke(@NotNull MethodHandle handle) {
        try {
            return (T) handle.invoke();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invoke(@NotNull MethodHandle handle, @Nullable Object arg1) {
        try {
            return (T) handle.invoke(arg1);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invoke(@NotNull MethodHandle handle, @Nullable Object arg1, @Nullable Object arg2) {
        try {
            return (T) handle.invoke(arg1, arg2);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invoke(@NotNull MethodHandle handle, @Nullable Object arg1, @Nullable Object arg2, @Nullable Object arg3) {
        try {
            return (T) handle.invoke(arg1, arg2, arg3);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invoke(@NotNull MethodHandle handle, @Nullable Object arg1, @Nullable Object arg2, @Nullable Object arg3, @Nullable Object arg4) {
        try {
            return (T) handle.invoke(arg1, arg2, arg3, arg4);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invoke(@NotNull MethodHandle handle, @Nullable Object arg1, @Nullable Object arg2, @Nullable Object arg3, @Nullable Object arg4, @Nullable Object arg5) {
        try {
            return (T) handle.invoke(arg1, arg2, arg3, arg4, arg5);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invoke(@NotNull MethodHandle handle, @Nullable Object arg1, @Nullable Object arg2, @Nullable Object arg3, @Nullable Object arg4, @Nullable Object arg5, @Nullable Object arg6) {
        try {
            return (T) handle.invoke(arg1, arg2, arg3, arg4, arg5, arg6);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invoke(@NotNull MethodHandle handle, @Nullable Object arg1, @Nullable Object arg2, @Nullable Object arg3, @Nullable Object arg4, @Nullable Object arg5, @Nullable Object arg6, @Nullable Object arg7) {
        try {
            return (T) handle.invoke(arg1, arg2, arg3, arg4, arg5, arg6, arg7);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invoke(@NotNull MethodHandle handle, @Nullable Object arg1, @Nullable Object arg2, @Nullable Object arg3, @Nullable Object arg4, @Nullable Object arg5, @Nullable Object arg6, @Nullable Object arg7, @Nullable Object arg8) {
        try {
            return (T) handle.invoke(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invoke(@NotNull MethodHandle handle, @Nullable Object arg1, @Nullable Object arg2, @Nullable Object arg3, @Nullable Object arg4, @Nullable Object arg5, @Nullable Object arg6, @Nullable Object arg7, @Nullable Object arg8, @Nullable Object arg9) {
        try {
            return (T) handle.invoke(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invoke(@NotNull MethodHandle handle, @Nullable Object arg1, @Nullable Object arg2, @Nullable Object arg3, @Nullable Object arg4, @Nullable Object arg5, @Nullable Object arg6, @Nullable Object arg7, @Nullable Object arg8, @Nullable Object arg9, @Nullable Object arg10) {
        try {
            return (T) handle.invoke(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @NotNull
    public static <T> AClass<T> aClass(@NotNull Class<T> clazz) {
        return RUNTIME.importClass(clazz);
    }

    @NotNull
    public static MethodType methodType(@NotNull Class<?> returnType, @NotNull Class<?>... classes) {
        switch (classes.length) {
            case 0:
                return MethodType.methodType(returnType);
            case 1:
                return MethodType.methodType(returnType, classes[0]);
            default:
                return MethodType.methodType(returnType, classes[0], Arrays.copyOfRange(classes, 1, classes.length));
        }
    }

    public static int score(int[] expected, int actual) {
        int score = 0;
        for (int mod : expected) {
            if ((actual & mod) != 0) {
                score++;
            }
        }
        return score;
    }

    public static int score(@NotNull Class<?> expected, @NotNull Class<?> actual) {
        if (expected.equals(actual)) {
            return 30;
        } else if (expected.isAssignableFrom(actual)) {
            return 20;
        } else if (actual.isAssignableFrom(expected)) {
            return 10;
        } else {
            return 0;
        }
    }

    public static int score(@NotNull Class<?>[] expected, @NotNull Class<?>[] actual) {
        if (expected.length != actual.length) {
            return 0;
        }

        int score = 0;
        for (int i = 0; i < expected.length; i++) {
            final Class<?> expectedClass = expected[i];
            final Class<?> actualClass = actual[i];
            if (expectedClass.equals(actualClass)) {
                score += 100;
            } else if (expectedClass.isAssignableFrom(actualClass)) {
                score += 70;
            } else if (actualClass.isAssignableFrom(expectedClass)) {
                score += 40;
            } else {
                return 0;
            }
        }

        return score;
    }

    public static class Runtime {

        private final ClassLoader parent;
        private final UnaryOperator<Reference> remapper;

        private final Map<String, AClass<?>> classPath = new HashMap<>();
        private static final Map<String, AClass<?>> staticClassPath = new HashMap<>();

        public Runtime(@NotNull ClassLoader parent) {
            this(parent, reference -> null);
        }

        public Runtime(@NotNull ClassLoader parent, @NotNull UnaryOperator<Reference> remapper) {
            this.parent = parent;
            this.remapper = remapper;
        }

        @NotNull
        public ClassLoader getClassLoader() {
            return parent;
        }

        @NotNull
        @SuppressWarnings("unchecked")
        public <T> AClass<T> getAClass(@NotNull String key) {
            AClass<?> result = classPath.get(key);

            if (result == null) {
                result = staticClassPath.get(key);
            }

            if (result == null) {
                throw new NullPointerException("Cannot find class for key '" + key + "' on runtime or static classpath");
            }

            return (AClass<T>) result;
        }

        @NotNull
        public Class<?> getClass(@NotNull Object type) {
            if (type instanceof AClass) {
                return ((AClass<?>) type).get();
            } else if (type instanceof Class) {
                return (Class<?>) type;
            } else if (type instanceof String) {
                return importClass((String) type).get();
            } else {
                throw new IllegalArgumentException("The object type " + type.getClass().getName() + " cannot be converted to Class");
            }
        }

        @NotNull
        public Class<?>[] getClasses(@NotNull Object[] types) {
            final Class<?>[] classes = new Class[types.length];
            for (int i = 0; i < types.length; i++) {
                classes[i] = getClass(types[i]);
            }
            return classes;
        }

        @NotNull
        @SuppressWarnings("unchecked")
        public <T> AClass<T> importClass(@NotNull Class<T> clazz) {
            if (classPath.containsKey(clazz.getName())) {
                return (AClass<T>) classPath.get(clazz.getName());
            }

            final AClass<T> result = new AClass<>(this, clazz.getName(), clazz);

            classPath.put(clazz.getName(), result);
            classPath.put(clazz.getSimpleName(), result);
            staticClassPath.put(clazz.getName(), result);
            staticClassPath.put(clazz.getSimpleName(), result);

            return result;
        }

        @NotNull
        @SuppressWarnings("unchecked")
        public <T> AClass<T> importClass(@NotNull String name) {
            if (classPath.containsKey(name)) {
                return (AClass<T>) classPath.get(name);
            }

            final AClass<T> result = new AClass<>(this, name, null);

            final String key = simpleName(name);
            final String key1 = key.replace('$', '.');

            classPath.put(key, result);
            classPath.put(key1, result);
            classPath.put(name, result);
            staticClassPath.put(key, result);
            staticClassPath.put(key1, result);

            return result;
        }

        @Nullable
        public Reference map(@NotNull Reference reference) {
            return remapper.apply(reference);
        }
    }

    public static class AClass<T> {

        private final Runtime parent;
        private final String name;
        private Class<T> clazz;

        private MethodHandles.Lookup lookup;

        public AClass(@NotNull Lookup.Runtime parent, @NotNull String name, @Nullable Class<T> clazz) {
            this.parent = parent;
            this.name = name;
            this.clazz = clazz;
        }

        public boolean isInstance(@NotNull Object object) {
            return get().isInstance(object);
        }

        @NotNull
        @SuppressWarnings("unchecked")
        public Class<T> get() {
            if (clazz == null) {
                final Reference reference = parent.map(toReference());
                if (reference != null && reference.isClass()) {
                    Object type = reference.getParent();
                    if (type instanceof Class) {
                        clazz = (Class<T>) type;
                        return clazz;
                    }
                    if (type instanceof String) {
                        type = parent.importClass((String) type);
                    }
                    if (type instanceof AClass && type != this) {
                        clazz = (Class<T>) ((AClass<?>) type).get();
                        return clazz;
                    }
                }
                try {
                    clazz = (Class<T>) Class.forName(name, true, parent.getClassLoader());
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            return clazz;
        }

        @NotNull
        public Class<?> getArray() {
            try {
                return arrayType(get());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        @NotNull
        public Lookup.Runtime getPackage() {
            return parent;
        }

        @NotNull
        public String getName() {
            return name;
        }

        @NotNull
        public MethodHandles.Lookup getLookup() {
            if (lookup == null) {
                try {
                    lookup = MethodHandles.privateLookupIn(get(), MethodHandles.lookup());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            return lookup;
        }

        @NotNull
        public Object[] getEnumValues() {
            try {
                return (Object[]) getClass().getDeclaredMethod("values").invoke(null);
            } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        @NotNull
        public AConstructor<T> constructor() {
            return new AConstructor<>(this);
        }

        @NotNull
        public AConstructor<T> constructor(@NotNull Object... parameters) {
            return constructor().params(parameters);
        }

        @NotNull
        public AMethod method() {
            return new AMethod(this);
        }

        @NotNull
        public AMethod method(@MagicConstant(flagsFromClass = Modifier.class) int modifiers, @NotNull Object type, @NotNull String name, @NotNull Object... parameters) {
            return new AMethod(this).mods(modifiers).type(type).name(name).params(parameters);
        }

        @NotNull
        public AMethod method(@NotNull Object type, @NotNull String name, @NotNull Object... parameters) {
            return new AMethod(this).type(type).name(name).params(parameters);
        }

        @NotNull
        public AField field() {
            return new AField(this);
        }

        @NotNull
        public AField field(@MagicConstant(flagsFromClass = Modifier.class) int modifiers, @NotNull Object type, @NotNull String name) {
            return new AField(this).mods(modifiers).type(type).name(name);
        }

        @NotNull
        public AField field(@NotNull Object type, @NotNull String name) {
            return new AField(this).type(type).name(name);
        }

        @Override
        public String toString() {
            return name;
        }

        @NotNull
        public Reference toReference() {
            return Reference.clazz(name);
        }
    }

    public static class AConstructor<T> {

        private final AClass<T> parent;
        private int[] modifiers = new int[0];
        private Object[] parameters = new Object[0];

        private Constructor<T> constructor;

        public AConstructor(@NotNull AClass<T> parent) {
            this.parent = parent;
        }

        @NotNull
        @SuppressWarnings("unchecked")
        public Constructor<T> get() {
            if (constructor == null) {
                Constructor<?> currentConstructor = null;
                int currentScore = 0;

                final Class<?>[] parameters;
                final Reference reference = parent.getPackage().map(toReference());
                if (reference != null) {
                    parameters = parent.getPackage().getClasses(reference.getParameters());
                } else {
                    parameters = parent.getPackage().getClasses(this.parameters);
                }

                for (Constructor<?> constructor : parent.get().getDeclaredConstructors()) {
                    final int score = score(constructor, parameters);
                    if (score > currentScore) {
                        currentConstructor = constructor;
                        currentScore = score;
                    }
                }

                if (currentConstructor == null) {
                    throw new IllegalStateException("Cannot find any constructor matching '" + this + "'");
                }

                try {
                    currentConstructor.setAccessible(true);
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }

                constructor = (Constructor<T>) currentConstructor;
            }
            return constructor;
        }

        @NotNull
        public T invoke() {
            return Lookup.invoke(handle());
        }

        @NotNull
        public T invoke(@Nullable Object instance) {
            return Lookup.invoke(handle(), instance);
        }

        protected int score(@NotNull Constructor<?> constructor, @NotNull Class<?>[] parameters) {
            if (constructor.getParameterCount() != parameters.length) {
                return 0;
            } else if (Arrays.equals(constructor.getParameterTypes(), parameters)) {
                return Integer.MAX_VALUE;
            } else {
                int score = Lookup.score(parameters, constructor.getParameterTypes());
                if (score == 0) {
                    return 0;
                }
                score += Lookup.score(modifiers, constructor.getModifiers());
                return score;
            }
        }

        @NotNull
        public MethodHandle handle() {
            try {
                try {
                    return handle0();
                } catch (IllegalAccessException | NoSuchMethodException e) {
                    return MethodHandles.lookup().unreflectConstructor(get());
                }
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }

        @Nullable
        public MethodHandle handleIf(boolean condition) {
            if (condition) {
                return handle();
            } else {
                return null;
            }
        }

        @Nullable
        public MethodHandle handleIfExist() {
            try {
                try {
                    return handle0();
                } catch (IllegalAccessException e) {
                    return MethodHandles.lookup().unreflectConstructor(get());
                } catch (NoSuchMethodException e) {
                    return null;
                }
            } catch (Throwable t) {
                t.printStackTrace();
                return null;
            }
        }

        @NotNull
        public MethodHandle handle0() throws NoSuchMethodException, IllegalAccessException {
            return parent.getLookup().findConstructor(parent.get(), methodType(void.class, parent.getPackage().getClasses(parameters)));
        }

        @NotNull
        @Contract("_ -> this")
        public AConstructor<T> mods(@MagicConstant(flagsFromClass = Modifier.class) int modifiers) {
            return mods(decompose(modifiers));
        }

        @NotNull
        @Contract("_ -> this")
        public AConstructor<T> mods(@MagicConstant(valuesFromClass = Modifier.class) int... modifiers) {
            this.modifiers = modifiers;
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public AConstructor<T> params(@NotNull Object... parameters) {
            this.parameters = parameters;
            return this;
        }

        @Override
        public String toString() {
            return (modifiers.length == 0 ? "" : format(modifiers) + " ") + parent + "(" + format(parameters) + ")";
        }

        @NotNull
        public Reference toReference() {
            return Reference.constructor(parent, parameters);
        }
    }

    public static class AMethod {

        private final AClass<?> parent;
        private int[] modifiers = new int[0];
        private Object type;
        private String name;
        private Object[] parameters = new Object[0];

        private boolean staticMod;
        private Method method;

        public AMethod(@NotNull AClass<?> parent) {
            this.parent = parent;
        }

        @NotNull
        public Method get() {
            if (method == null) {
                Method currentMethod = null;
                int currentScore = 0;

                final Class<?> type;
                final String name;
                final Class<?>[] parameters;
                final Reference reference = parent.getPackage().map(toReference());
                if (reference != null) {
                    type = parent.getPackage().getClass(reference.getType());
                    name = reference.getName();
                    parameters = parent.getPackage().getClasses(reference.getParameters());
                } else {
                    type = parent.getPackage().getClass(this.type);
                    name = this.name;
                    parameters = parent.getPackage().getClasses(this.parameters);
                }

                for (Method method : parent.get().getDeclaredMethods()) {
                    final int score = score(method, type, name, parameters);
                    if (score > currentScore) {
                        currentMethod = method;
                        currentScore = score;
                    }
                }

                if (currentMethod == null) {
                    throw new IllegalStateException("Cannot find any method matching '" + this + "' inside class " + parent);
                }

                try {
                    currentMethod.setAccessible(true);
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }

                method = currentMethod;
            }
            return method;
        }

        public <T> T invoke() {
            return Lookup.invoke(handle(), null);
        }

        public <T> T invoke(@Nullable Object instance) {
            return Lookup.invoke(handle(), instance);
        }

        protected int score(@NotNull Method method, @NotNull Class<?> type, @NotNull String name, @NotNull Class<?>[] parameters) {
            if (Modifier.isStatic(method.getModifiers()) != staticMod || method.getParameterCount() != parameters.length) {
                return 0;
            } else if (method.getName().equals(name)) {
                if (Arrays.equals(method.getParameterTypes(), parameters)) {
                    return Integer.MAX_VALUE;
                }
                final int score = Lookup.score(parameters, method.getParameterTypes());
                if (score > 0) {
                    return Integer.MAX_VALUE / 2 + score;
                } else {
                    return 0;
                }
            } else {
                int score = Lookup.score(parameters, method.getParameterTypes());
                if (score == 0) {
                    return 0;
                }
                score += Lookup.score(type, method.getReturnType());
                score += Lookup.score(modifiers, method.getModifiers());
                return score;
            }
        }

        @NotNull
        public MethodHandle handle() {
            try {
                try {
                    if (staticMod) {
                        return parent.getLookup().findStatic(parent.get(), name, methodType(parent.getPackage().getClass(type), parent.getPackage().getClasses(parameters)));
                    } else {
                        return parent.getLookup().findVirtual(parent.get(), name, methodType(parent.getPackage().getClass(type), parent.getPackage().getClasses(parameters)));
                    }
                } catch (IllegalAccessException | NoSuchMethodException e) {
                    return MethodHandles.lookup().unreflect(get());
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }

        @NotNull
        @Contract("_ -> this")
        public AMethod mods(@MagicConstant(flagsFromClass = Modifier.class) int modifiers) {
            return mods(decompose(modifiers));
        }

        @NotNull
        @Contract("_ -> this")
        public AMethod mods(@MagicConstant(valuesFromClass = Modifier.class) int... modifiers) {
            this.modifiers = modifiers;
            this.staticMod = false;
            for (int mod : modifiers) {
                if (mod == Modifier.STATIC) {
                    this.staticMod = true;
                    break;
                }
            }
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public AMethod type(@NotNull Object type) {
            this.type = type;
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public AMethod name(@NotNull String name) {
            this.name = name;
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public AMethod params(@NotNull Object... parameters) {
            this.parameters = parameters;
            return this;
        }

        @Override
        public String toString() {
            return (modifiers.length == 0 ? "" : format(modifiers) + " ") + format(type) + " " + name + "(" + format(parameters) + ")";
        }

        @NotNull
        public Reference toReference() {
            return Reference.method(parent, type, name, parameters);
        }
    }

    public static class AField {

        private final AClass<?> parent;
        private int[] modifiers = new int[0];
        private Object type;
        private String name;

        private boolean staticMod;
        private Field field;

        public AField(@NotNull AClass<?> parent) {
            this.parent = parent;
        }

        @NotNull
        public Field get() {
            if (field == null) {
                Field currentField = null;
                int currentScore = 0;

                final Class<?> type;
                final String name;
                final Reference reference = parent.getPackage().map(toReference());
                if (reference != null) {
                    type = parent.getPackage().getClass(reference.getType());
                    name = reference.getName();
                } else {
                    type = parent.getPackage().getClass(this.type);
                    name = this.name;
                }

                for (Field field : parent.get().getDeclaredFields()) {
                    final int score = score(field, type, name);
                    if (score > currentScore) {
                        currentField = field;
                        currentScore = score;
                    }
                }

                if (currentField == null) {
                    throw new IllegalStateException("Cannot find any field matching '" + this + "' inside class " + parent);
                }

                try {
                    currentField.setAccessible(true);
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }

                field = currentField;
            }
            return field;
        }

        public <T> T getValue() {
            return Lookup.invoke(getter(), null);
        }

        public <T> T getValue(@Nullable Object instance) {
            return Lookup.invoke(getter(), instance);
        }

        public void setValue(@Nullable Object value) {
            Lookup.invoke(setter(), null, value);
        }

        public void setValue(@Nullable Object instance, @Nullable Object value) {
            Lookup.invoke(setter(), instance, value);
        }

        protected int score(@NotNull Field field, @NotNull Class<?> type, @NotNull String name) {
            if (Modifier.isStatic(field.getModifiers()) != staticMod) {
                return 0;
            } else if (field.getName().equals(name)) {
                if (field.getType().equals(type)) {
                    return Integer.MAX_VALUE;
                }
                final int score = Lookup.score(type, field.getType());
                if (score > 0) {
                    return Integer.MAX_VALUE / 2 + score;
                } else {
                    return 0;
                }
            } else {
                int score = Lookup.score(type, field.getType());
                if (score == 0) {
                    return 0;
                }
                score += Lookup.score(modifiers, field.getModifiers());
                return score;
            }
        }

        @NotNull
        public MethodHandle getter() {
            try {
                try {
                    if (staticMod) {
                        return parent.getLookup().findStaticGetter(parent.get(), name, parent.getPackage().getClass(type));
                    } else {
                        return parent.getLookup().findGetter(parent.get(), name, parent.getPackage().getClass(type));
                    }
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    return MethodHandles.lookup().unreflectGetter(get());
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }

        @NotNull
        public MethodHandle setter() {
            try {
                try {
                    if (staticMod) {
                        return parent.getLookup().findStaticSetter(parent.get(), name, parent.getPackage().getClass(type));
                    } else {
                        return parent.getLookup().findSetter(parent.get(), name, parent.getPackage().getClass(type));
                    }
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    return MethodHandles.lookup().unreflectSetter(get());
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }

        @NotNull
        @Contract("_ -> this")
        public AField mods(@MagicConstant(flagsFromClass = Modifier.class) int modifiers) {
            return mods(decompose(modifiers));
        }

        @NotNull
        @Contract("_ -> this")
        public AField mods(@MagicConstant(valuesFromClass = Modifier.class) int... modifiers) {
            this.modifiers = modifiers;
            this.staticMod = false;
            for (int mod : modifiers) {
                if (mod == Modifier.STATIC) {
                    this.staticMod = true;
                    break;
                }
            }
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public AField type(@NotNull Object type) {
            this.type = type;
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public AField name(@NotNull String name) {
            this.name = name;
            return this;
        }

        @Override
        public String toString() {
            return (modifiers.length == 0 ? "" : format(modifiers) + " ") + format(type) + " " + name;
        }

        @NotNull
        public Reference toReference() {
            return Reference.field(parent, type, name);
        }
    }

    @NotNull
    private static String simpleName(@NotNull String name) {
        if (name.contains(".")) {
            return name.substring(name.lastIndexOf('.') + 1);
        } else {
            return name;
        }
    }

    // Method taken from saicone/types, licensed under MIT License
    @NotNull
    private static Class<?> arrayType(@NotNull Class<?> componentType) throws ClassNotFoundException {
        // This can be simplified by using Class#descriptorString().replace('/', '.') from Java +12
        if (componentType.isArray()) {
            return Class.forName("[" + componentType.getName());
        } else if (componentType.isPrimitive()) {
            // This can be simplified by using Wrapper#forPrimitiveType() from sun API
            if (componentType == long.class) {
                return Class.forName("[J");
            } else if (componentType == boolean.class) {
                return Class.forName("[Z");
            } else {
                return Class.forName("[" + Character.toUpperCase(componentType.getSimpleName().charAt(0)));
            }
        } else {
            return Class.forName("[L" + componentType.getName() + ";");
        }
    }

    private static int[] decompose(int modifiers) {
        final List<Integer> list = new ArrayList<>();
        while (modifiers != 0) {
            int bit = modifiers & -modifiers;
            list.add(bit);
            modifiers ^= bit;
        }
        final int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    @NotNull
    private static String format(@NotNull Object type) {
        if (type instanceof Class) {
            return ((Class<?>) type).getName();
        } else {
            return String.valueOf(type);
        }
    }

    @NotNull
    private static String format(@NotNull Object[] types) {
        final StringJoiner joiner = new StringJoiner(", ");
        for (Object type : types) {
            if (type instanceof Class) {
                joiner.add(((Class<?>) type).getName());
            } else {
                joiner.add(String.valueOf(type));
            }
        }
        return joiner.toString();
    }

    @NotNull
    private static String format(int[] modifiers) {
        final StringJoiner joiner = new StringJoiner(" ");
        for (int modifier : modifiers) {
            joiner.add(format(modifier));
        }
        return joiner.toString();
    }

    @NotNull
    private static String format(int modifier) {
        switch (modifier) {
            case Modifier.PUBLIC:
                return "public";
            case Modifier.PRIVATE:
                return "private";
            case Modifier.PROTECTED:
                return "protected";
            case Modifier.STATIC:
                return "static";
            case Modifier.FINAL:
                return "final";
            case Modifier.SYNCHRONIZED:
                return "synchronized";
            case Modifier.VOLATILE:
                return "volatile";
            case Modifier.TRANSIENT:
                return "transient";
            case Modifier.NATIVE:
                return "native";
            case Modifier.INTERFACE:
                return "interface";
            case Modifier.ABSTRACT:
                return "abstract";
            case Modifier.STRICT:
                return "strictfp";
            default:
                return "";
        }
    }
}
