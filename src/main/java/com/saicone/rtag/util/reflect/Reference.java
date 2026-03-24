package com.saicone.rtag.util.reflect;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

@ApiStatus.Internal
public class Reference {

    private static final Map<String, Class<?>> PRIMITIVE = Map.of(
            boolean.class.getName(), boolean.class,
            byte.class.getName(), byte.class,
            short.class.getName(), short.class,
            int.class.getName(), int.class,
            long.class.getName(), long.class,
            char.class.getName(), char.class,
            float.class.getName(), float.class,
            double.class.getName(), double.class,
            void.class.getName(), void.class
    );
    private static final Map<String, Reference> CACHE = new HashMap<>();

    @NotNull
    public static Reference valueOf(@NotNull String s) {
        Object clazz;
        Object type = null;
        String name = null;
        Object[] parameters = null;
        if (s.contains(":")) {
            final int index = s.indexOf(':');
            type = fromString(s.substring(index + 1));
            s = s.substring(0, index);
        }
        if (s.contains("(")) {
            final int index = s.indexOf('(');
            parameters = arrayFromString(s.substring(index + 1));
            s = s.substring(0, index);
        }
        if (s.contains("#")) {
            final int index = s.indexOf('#');
            name = s.substring(index + 1);
            s = s.substring(0, index);
        }
        clazz = fromString(s);
        return new Reference(clazz, type, name, parameters);
    }

    @NotNull
    public static Reference valueOf(@NotNull Class<?> clazz) {
        return new Reference(clazz, null, null, null);
    }

    @NotNull
    public static Reference valueOf(@NotNull Constructor<?> constructor) {
        return new Reference(constructor.getDeclaringClass(), null, null, constructor.getParameterTypes());
    }

    @NotNull
    public static Reference valueOf(@NotNull Method method) {
        return new Reference(method.getDeclaringClass(), method.getReturnType(), method.getName(), method.getParameterTypes());
    }

    @NotNull
    public static Reference valueOf(@NotNull Field field) {
        return new Reference(field.getDeclaringClass(), field.getType(), field.getName(), null);
    }

    @NotNull
    public static Reference clazz(@NotNull Object clazz) {
        return new Reference(clazz, null, null, null);
    }

    @NotNull
    public static Reference constructor(@NotNull Object clazz, @NotNull Object... parameters) {
        return new Reference(clazz, null, null, parameters);
    }

    @NotNull
    public static Reference method(@NotNull Object clazz, @NotNull Object type, @NotNull String name, @NotNull Object... parameters) {
        return new Reference(clazz, type, name, parameters);
    }

    @NotNull
    public static Reference field(@NotNull Object clazz, @NotNull Object type, @NotNull String name) {
        return new Reference(clazz, type, name, null);
    }

    private final Object parent;
    private final Object type;
    private final String name;
    private final Object[] parameters;

    private String str;

    protected Reference(@NotNull Object parent, @Nullable Object type, @Nullable String name, @Nullable Object[] parameters) {
        this.parent = parent;
        this.type = type;
        this.name = name;
        this.parameters = parameters;
    }

    public boolean isClass() {
        return type == null && name == null && parameters == null;
    }

    public boolean isConstructor() {
        return type == null && name == null && parameters != null;
    }

    public boolean isMethod() {
        return type != null && name != null && parameters != null;
    }

    public boolean isField() {
        return type != null && name != null && parameters == null;
    }

    @NotNull
    public Object getParent() {
        return parent;
    }

    public Object getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Object[] getParameters() {
        return parameters;
    }

    @Override
    public final boolean equals(Object object) {
        if (!(object instanceof Reference)) return false;

        Reference reference = (Reference) object;
        return toString().equals(reference.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        if (str == null) {
            final StringBuilder builder = new StringBuilder();
            builder.append(toString(parent));
            if (name != null) {
                builder.append("#").append(name);
            }
            if (parameters != null) {
                builder.append(toString(parameters));
            }
            if (type != null) {
                builder.append(":").append(toString(type));
            }
            str = builder.toString();
        }
        return str;
    }

    @NotNull
    private static String toString(@NotNull Object type) {
        if (type instanceof Class) {
            return ((Class<?>) type).getName();
        } else {
            return String.valueOf(type);
        }
    }

    @NotNull
    private static String toString(@NotNull Object[] types) {
        final StringJoiner joiner = new StringJoiner(",", "(", ")");
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
    private static Object fromString(@NotNull String s) {
        final Class<?> primitive = PRIMITIVE.get(s);
        if (primitive != null) {
            return primitive;
        }
        try {
            return Class.forName(s);
        } catch (Throwable t) {
            return s;
        }
    }

    @NotNull
    private static Object[] arrayFromString(@NotNull String s) {
        if (s.length() < 3) {
            return new Object[0];
        }
        s = s.substring(1, s.length() - 1);
        final String[] split = s.split(",");
        final Object[] array = new Object[split.length];
        for (int i = 0; i < split.length; i++) {
            array[i] = fromString(split[i]);
        }
        return array;
    }
}
