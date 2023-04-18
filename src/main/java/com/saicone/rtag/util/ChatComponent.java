package com.saicone.rtag.util;

import java.lang.invoke.MethodHandle;
import java.util.Objects;

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
     * Check if the provided object is instance of IChatBaseComponent<br>
     * or is a String that follow the ChatComponent format.
     *
     * @param object the object to check.
     * @return       true if the object is ChatComponent.
     */
    public static boolean isChatComponent(Object object) {
        if (object instanceof String) {
            final String s = (String) object;
            if (s.length() > 11) {
                final int index;
                if (s.charAt(0) == '{' && s.charAt(s.length() - 1) == '}' && (index = s.indexOf("\"text\":\"")) > 0) {
                    final int i = s.indexOf('"', index + 8) + 1;
                    final char c;
                    return i + 1 >= s.length() || (c = s.charAt(i)) == '}' || c == ',';
                }
            } else if (s.length() == 11) {
                return s.equals("{\"text\":\"\"}");
            }
            return false;
        } else {
            return CHAT_BASE_COMPONENT.isInstance(object);
        }
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
     * Convert String or IChatBaseComponent to json component.
     *
     * @param component String or IChatBaseComponent to convert.
     * @return          A json component.
     * @throws IllegalArgumentException if component is not a valid ChatComponent.
     */
    public static String toJson(Object component) throws IllegalArgumentException {
        if (component instanceof String) {
            return toJson(fromString((String) component));
        }
        Objects.requireNonNull(component, "The provided object cannot be null");
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
     * Convert String or IChatBaseComponent to json component.
     *
     * @param component String or IChatBaseComponent to convert.
     * @return          A json component or null.
     * @throws IllegalArgumentException if component is not a valid ChatComponent.
     */
    public static String toJsonOrNull(Object component) throws IllegalArgumentException {
        if (component instanceof String) {
            return toJsonOrNull(fromString((String) component));
        }
        return component == null ? null : toJson(component);
    }

    /**
     * Convert json String or IChatBaseComponent to string.
     *
     * @param component Json String or IChatBaseComponent to convert.
     * @return          A string with old format.
     * @throws IllegalArgumentException if component is not a valid ChatComponent.
     */
    public static String toString(Object component) throws IllegalArgumentException {
        if (component instanceof String) {
            return toString(fromJson((String) component));
        }
        Objects.requireNonNull(component, "The provided object cannot be null");
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
     * Convert json String or IChatBaseComponent to string.
     *
     * @param component Json String or IChatBaseComponent to convert.
     * @return          A string with old format or null.
     * @throws IllegalArgumentException if component is not a valid ChatComponent.
     */
    public static String toStringOrNull(Object component) throws IllegalArgumentException {
        if (component instanceof String) {
            return toStringOrNull(fromJson((String) component));
        }
        return component == null ? null : toString(component);
    }
}
