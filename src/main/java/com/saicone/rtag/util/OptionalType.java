package com.saicone.rtag.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

/**
 * Object container with automatic conversion to required
 * type when value is called.
 *
 * @author Rubenicos
 */
@SuppressWarnings("unchecked")
public class OptionalType extends IterableType<Object> {

    private static final OptionalType BLANK = new OptionalType(null);

    /**
     * Get current OptionalType from object value.
     *
     * @param value Saved value, can be null.
     * @return      An OptionalType with object value.
     */
    public static OptionalType of(Object value) {
        return value != null ? new OptionalType(value) : BLANK;
    }

    /**
     * Cast any object to required type.<br>
     * Take in count this method may produce {@link ClassCastException} if you want to cast inconvertible types.
     *
     * @param object Object to cast.
     * @return       The object as required type or null;
     * @param <T>    Required type to cast the object.
     */
    @SuppressWarnings("unused")
    public static <T> T cast(Object object) {
        try {
            return (T) object;
        } catch (ClassCastException e) {
            return null;
        }
    }

    private Object value;

    /**
     * Constructs an OptionalType with specified value.
     *
     * @param value Saved value, can be null.
     */
    public OptionalType(Object value) {
        this.value = value;
    }

    @Override
    protected Object getIterable() {
        return value;
    }

    @Override
    protected void setIterable(Object value) {
        this.value = value;
    }

    /**
     * Check if the existence of current value.
     *
     * @return true if current value is null.
     */
    public boolean isEmpty() {
        return value == null;
    }

    /**
     * Same has {@link #isEmpty()} but with inverted result.
     *
     * @return true if current value is not null.
     */
    public boolean isNotEmpty() {
        return !isEmpty();
    }

    /**
     * Check if the current value is instance of defined class.
     *
     * @param clazz Class type.
     * @return      true if value is instance of class.
     */
    public boolean isInstance(Class<?> clazz) {
        return clazz.isInstance(value);
    }

    /**
     * Same has {@link #isInstance(Class)} but with inverted result.
     *
     * @param clazz Class type.
     * @return      true if value is not instance of class.
     */
    public boolean isNotInstance(Class<?> clazz) {
        return !isInstance(clazz);
    }

    /**
     * Get actual value converted to required type.
     *
     * @param <T> Type to cast.
     * @return    Actual value or null if cast fails.
     */
    public <T> T value() {
        return cast(value);
    }

    /**
     * Get actual value converted to required type class.
     *
     * @param clazz Class to check instance of.
     * @param <T>   Type to cast.
     * @return      Actual value or null if cast fails.
     */
    public <T> T value(Class<T> clazz) {
        return isInstance(clazz) ? (T) value : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OptionalType objects = (OptionalType) o;

        return Objects.equals(value, objects.value);
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    /**
     * Get actual value converted to required type or default defined value.
     *
     * @param def Default value if cast fails.
     * @param <T> Type to cast.
     * @return    Actual value or default if cast fails.
     */
    public <T> T or(T def) {
        T value = value();
        return value != null ? value : def;
    }

    /**
     * Get actual value converted to required type class or default defined value.
     *
     * @param clazz Class to check instance of.
     * @param def   Default value if cast fails.
     * @param <T>   Type to cast.
     * @return      Actual value or default if cast fails.
     */
    public <T> T or(Class<T> clazz, T def) {
        return isInstance(clazz) ? (T) value : def;
    }

    /**
     * Get actual value by function.
     *
     * @param function Function to process the value.
     * @return         The function result.
     * @param <T>      The required function result type.
     */
    public <T> T by(ThrowableFunction<Object, T> function) {
        try {
            return function.apply(value);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Get actual value by function with defined default value.
     *
     * @param function Function to process the actual non-null value.
     * @param def      Defined default value if the actual or final value is null.
     * @return         The function result or default value if the function return null object or throws exception.
     * @param <T>      The required result type.
     */
    public <T> T by(ThrowableFunction<Object, T> function, T def) {
        if (value == null) {
            return def;
        }
        final T obj;
        try {
            obj = function.apply(value);
        } catch (Throwable t) {
            return def;
        }
        return obj != null ? obj : def;
    }

    /**
     * Get actual value as array using function.
     *
     * @param array    The array to fill with objects.
     * @param function Function to convert the value or any element inside value to required type.
     * @return         The value as required array type.
     * @param <T>      The required type.
     */
    public <T> T[] asArray(T[] array, Function<OptionalType, T> function) {
        if (value == null) {
            return array;
        }
        try {
            return (T[]) value;
        } catch (ClassCastException ignored) { }

        return asList(function).toArray(array);
    }

    /**
     * Get actual value as list using function.
     *
     * @param function Function to convert the value or any element inside value to required type.
     * @return         The value as required list type.
     * @param <T>      The required type.
     */
    public <T> List<T> asList(Function<OptionalType, T> function) {
        return asList(new ArrayList<>(), function);
    }

    /**
     * Apply actual value to list using function.
     *
     * @param list     The function to add values.
     * @param function Function to convert the value or any element inside value to required type.
     * @return         The provided list.
     * @param <T>      The required type.
     */
    public <T> List<T> asList(List<T> list, Function<OptionalType, T> function) {
        if (value == null) {
            return list;
        }
        try {
            return (List<T>) value;
        } catch (ClassCastException ignored) { }
        if (isIterable()) {
            forEach(object -> list.add(function.apply(OptionalType.of(object))));
        } else {
            list.add(function.apply(this));
        }
        return list;
    }

    /**
     * Get the actual value as Object or null.
     *
     * @return An Object value or null.
     */
    public Object asObject() {
        return asObject(null);
    }

    /**
     * Get the actual value as Object or default value instead.
     *
     * @param def Default Object value.
     * @return    An Object value or default value.
     */
    public Object asObject(Object def) {
        return value != null ? value : def;
    }

    /**
     * Get the actual value as String or null.<br>
     * This method only convert the actual value to String if it isn't null.
     *
     * @return A String value or null.
     */
    public String asString() {
        return asString(null);
    }

    /**
     * Get the actual value as String if it isn't null or default value instead.
     *
     * @param def Default String value.
     * @return    A String value or default value.
     */
    public String asString(String def) {
        return value != null ? String.valueOf(value) : def;
    }

    /**
     * Get the actual value as Character or null if the conversion fails.
     *
     * @return A Character value or null.
     */
    public Character asChar() {
        return asChar(null);
    }

    /**
     * Get the actual value as Character or default value instead if the conversion fails.
     *
     * @param def Default Character value.
     * @return    A Character value or default value.
     */
    public Character asChar(Character def) {
        if (value instanceof Character) {
            return (Character) value;
        }
        return by(object -> {
            final String s = String.valueOf(object);
            return s.isBlank() ? null : s.charAt(0);
        }, def);
    }

    /**
     * Get the actual value as Boolean or null if the conversion fails.
     *
     * @return A Boolean value or null.
     */
    public Boolean asBoolean() {
        return asBoolean(null);
    }

    /**
     * Get the actual value as Boolean or default value instead if the conversion fails.
     *
     * @param def Default Boolean value.
     * @return    A Boolean value or default value.
     */
    public Boolean asBoolean(Boolean def) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return by(object -> {
            switch (String.valueOf(object).toLowerCase()) {
                case "true":
                case "1":
                case "1.0":
                case "yes":
                case "on":
                case "y":
                    return true;
                case "false":
                case "0":
                case "0.0":
                case "no":
                case "off":
                case "n":
                    return false;
                default:
                    return null;
            }
        }, def);
    }

    /**
     * Get the actual value as Byte or null if the conversion fails.
     *
     * @return A Byte value or null.
     */
    public Byte asByte() {
        return asByte(null);
    }

    /**
     * Get the actual value as Byte or default value instead if the conversion fails.
     *
     * @param def Default Byte value.
     * @return    A Byte value or default value.
     */
    public Byte asByte(Byte def) {
        if (value instanceof Byte) {
            return (Byte) value;
        }
        if (value instanceof Boolean) {
            return (byte) ((boolean) value ? 1 : 0);
        }
        return by(object -> Byte.parseByte(String.valueOf(object)), def);
    }

    /**
     * Get the actual value as Short or null if the conversion fails.
     *
     * @return A Short value or null.
     */
    public Short asShort() {
        return asShort(null);
    }

    /**
     * Get the actual value as Short or default value instead if the conversion fails.
     *
     * @param def Default Short value.
     * @return    A Short value or default value.
     */
    public Short asShort(Short def) {
        if (value instanceof Short) {
            return (Short) value;
        }
        if (value instanceof Boolean) {
            return (short) ((boolean) value ? 1 : 0);
        }
        return by(object -> Short.parseShort(String.valueOf(object)), def);
    }

    /**
     * Get the actual value as Integer or null if the conversion fails.
     *
     * @return A Integer value or null.
     */
    public Integer asInt() {
        return asInt(null);
    }

    /**
     * Get the actual value as Integer or default value instead if the conversion fails.
     *
     * @param def Default Integer value.
     * @return    A Integer value or default value.
     */
    public Integer asInt(Integer def) {
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Boolean) {
            return (boolean) value ? 1 : 0;
        }
        return by(object -> Integer.parseInt(String.valueOf(object)), def);
    }

    /**
     * Get the actual value as Float or null if the conversion fails.
     *
     * @return A Float value or null.
     */
    public Float asFloat() {
        return asFloat(null);
    }

    /**
     * Get the actual value as Float or default value instead if the conversion fails.
     *
     * @param def Default Float value.
     * @return    A Float value or default value.
     */
    public Float asFloat(Float def) {
        if (value instanceof Float) {
            return (Float) value;
        }
        if (value instanceof Boolean) {
            return (boolean) value ? 1F : 0F;
        }
        return by(object -> Float.parseFloat(String.valueOf(object)), def);
    }

    /**
     * Get the actual value as Long or null if the conversion fails.
     *
     * @return A Long value or null.
     */
    public Long asLong() {
        return asLong(null);
    }

    /**
     * Get the actual value as Long or default value instead if the conversion fails.
     *
     * @param def Default Long value.
     * @return    A Long value or default value.
     */
    public Long asLong(Long def) {
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Boolean) {
            return (boolean) value ? 1L : 0L;
        }
        return by(object -> Long.parseLong(String.valueOf(object)), def);
    }

    /**
     * Get the actual value as Double or null if the conversion fails.
     *
     * @return A Double value or null.
     */
    public Double asDouble() {
        return asDouble(null);
    }

    /**
     * Get the actual value as Double or default value instead if the conversion fails.
     *
     * @param def Default Double value.
     * @return    A Double value or default value.
     */
    public Double asDouble(Double def) {
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Boolean) {
            return (boolean) value ? 1D : 0D;
        }
        return by(object -> Double.parseDouble(String.valueOf(object)), def);
    }

    /**
     * Get the actual value as UUID or null if the conversion fails.
     *
     * @return An UUID value or null.
     */
    public UUID asUuid() {
        return asUuid(null);
    }

    /**
     * Get the actual value as UUID or default value instead if the conversion fails.
     *
     * @param def Default UUID value.
     * @return    An UUID value or default value.
     */
    public UUID asUuid(UUID def) {
        if (value instanceof UUID) {
            return (UUID) value;
        }
        return by((object) -> {
            if (object instanceof int[]) {
                return getUUID((int[]) object);
            } else if (object instanceof String) {
                return UUID.fromString((String) object);
            } else {
                return null;
            }
        }, def);
    }

    private static UUID getUUID(int[] array) {
        if (array.length == 4) {
            StringBuilder builder = new StringBuilder();
            for (int i : array) {
                String hex = Integer.toHexString(i);
                builder.append(new String(new char[8 - hex.length()]).replace('\0', '0')).append(hex);
            }
            if (builder.length() == 32) {
                builder.insert(20, '-').insert(16, '-').insert(12, '-').insert(8, '-');
                return UUID.fromString(builder.toString());
            } else {
                throw new IllegalArgumentException("The final converted UUID '" + builder + "' doesn't is a 32-length string");
            }
        }
        throw new IllegalArgumentException("The provided int array isn't a 4-length array");
    }
}
