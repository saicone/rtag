package com.saicone.rtag.util;

import java.lang.invoke.MethodHandle;

/**
 * Class to invoke CraftChatMessage methods across versions.
 *
 * @author Rubenicos
 */
public class ChatComponent {

    private static final Class<?> CHAT_BASE_COMPONENT;
    private static final MethodHandle fromString;
    private static final MethodHandle fromComponent;
    private static final MethodHandle fromJson;
    private static final MethodHandle toJson;

    static {
        // CraftChatMessage util class
        MethodHandle method$fromString = null;
        MethodHandle method$fromComponent = null;
        // ChatSerializer MC class
        MethodHandle method$fromJson = null;
        MethodHandle method$toJson = null;
        try {
            Class<?> clazz = EasyLookup.addNMSClass("network.chat.IChatBaseComponent");
            EasyLookup.addClass("ChatSerializer", clazz.getDeclaredClasses()[0]);
            EasyLookup.addOBCClass("util.CraftChatMessage");

            if (ServerInstance.verNumber >= 13) {
                method$fromString = EasyLookup.staticMethod("CraftChatMessage", "fromStringOrNull", "IChatBaseComponent", String.class);
            } else {
                // Unreflect reason:
                // Return IChatBaseComponent array
                method$fromString = EasyLookup.unreflectMethod("CraftChatMessage", "fromString", String.class);
            }
            method$fromComponent = EasyLookup.staticMethod("CraftChatMessage", "fromComponent", String.class, "IChatBaseComponent");

            // Unreflect reason:
            // (1.8 - 1.15) return IChatBaseComponent
            // Other versions return IChatMutableComponent
            method$fromJson = EasyLookup.unreflectMethod("ChatSerializer", "a", String.class);
            method$toJson = EasyLookup.staticMethod("ChatSerializer", "a", String.class, "IChatBaseComponent");
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        CHAT_BASE_COMPONENT = EasyLookup.classById("IChatBaseComponent");
        fromString = method$fromString;
        fromComponent = method$fromComponent;
        fromJson = method$fromJson;
        toJson = method$toJson;
    }

    ChatComponent() {
    }

    /**
     * Convert json component string to IChatBaseComponent.
     *
     * @param json Json to convert.
     * @return     A IChatBaseComponent instance.
     */
    public static Object fromJson(String json) {
        try {
            return json == null || json.isEmpty() ? null : fromJson.invoke(json);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Convert string to IChatBaseComponent.
     *
     * @param string Json to convert.
     * @return       A IChatBaseComponent instance.
     */
    public static Object fromString(String string) {
        try {
            if (ServerInstance.verNumber >= 13) {
                return fromString.invoke(string);
            } else {
                return string == null || string.isEmpty() ? null : ((Object[]) fromString.invoke(string))[0];
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Convert IChatBaseComponent to json component.
     *
     * @param component IChatBaseComponent to convert.
     * @return          A json component.
     * @throws IllegalArgumentException if component is not an IChatBaseComponent.
     */
    public static String toJson(Object component) throws IllegalArgumentException {
        if (!CHAT_BASE_COMPONENT.isInstance(component)) {
            throw new IllegalArgumentException("The provided object isn't an IChatBaseComponent");
        }
        try {
            return (String) toJson.invoke(component);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Convert string to json component string.
     *
     * @param string String to convert.
     * @return       A json component.
     */
    public static String toJson(String string) {
        return toJson(fromString(string));
    }

    /**
     * Convert IChatBaseComponent to json component.
     *
     * @param component IChatBaseComponent to convert.
     * @return          A json component or null.
     * @throws IllegalArgumentException if component is not an IChatBaseComponent.
     */
    public static String toJsonOrNull(Object component) throws IllegalArgumentException {
        return component == null ? null : toJson(component);
    }

    /**
     * Convert string to json component.
     *
     * @param string String to convert.
     * @return       A json component or null.
     */
    public static String toJsonOrNull(String string) {
        return toJsonOrNull(fromString(string));
    }

    /**
     * Convert IChatBaseComponent to string.
     *
     * @param component IChatBaseComponent to convert.
     * @return          A string with old format.
     * @throws IllegalArgumentException if component is not an IChatBaseComponent.
     */
    public static String toString(Object component) throws IllegalArgumentException {
        if (!CHAT_BASE_COMPONENT.isInstance(component)) {
            throw new IllegalArgumentException("The provided object isn't an IChatBaseComponent");
        }
        try {
            return (String) fromComponent.invoke(component);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Convert json component to string.
     *
     * @param json Json to convert.
     * @return     A string with old format.
     */
    public static String toString(String json) {
        return toString(fromJson(json));
    }

    /**
     * Convert IChatBaseComponent to string.
     *
     * @param component IChatBaseComponent to convert.
     * @return          A string with old format or null.
     * @throws IllegalArgumentException if component is not an IChatBaseComponent.
     */
    public static String toStringOrNull(Object component) throws IllegalArgumentException {
        return component == null ? null : toString(component);
    }

    /**
     * Convert json component to string.
     *
     * @param json Json to convert.
     * @return     A string with old format or null.
     */
    public static String toStringOrNull(String json) {
        return toStringOrNull(fromJson(json));
    }
}
