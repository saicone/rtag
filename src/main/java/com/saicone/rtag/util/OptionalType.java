package com.saicone.rtag.util;

/**
 * Object container with automatic conversion to required
 * type when value is called.
 *
 * @author Rubenicos
 */
@SuppressWarnings("unchecked")
public class OptionalType {

    private static final OptionalType BLANK = new OptionalType(null);

    public static OptionalType of(Object value) {
        return value != null ? new OptionalType(value) : BLANK;
    }

    private final Object value;

    /**
     * Constructs an OptionalType with specified value.
     *
     * @param value Saved value, can be null.
     */
    public OptionalType(Object value) {
        this.value = value;
    }

    /**
     * Check if the existence of current value.
     *
     * @return True if current value is null.
     */
    public boolean isEmpty() {
        return value == null;
    }

    /**
     * Same has {@link #isEmpty()} but with inverted result.
     *
     * @return True if current value is not null.
     */
    public boolean isNotEmpty() {
        return !isEmpty();
    }

    /**
     * Check if the current value is instance of defined class.
     *
     * @param clazz Class type.
     * @return      True is value is instance of class.
     */
    public boolean isInstance(Class<?> clazz) {
        return clazz.isInstance(value);
    }

    /**
     * Same has {@link #isInstance(Class)} but with inverted result.
     *
     * @param clazz Class type.
     * @return      True is value is not instance of class.
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
        try {
            return (T) value;
        } catch (ClassCastException e) {
            return null;
        }
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
}
