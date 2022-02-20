package com.saicone.rtag.util;

import java.lang.invoke.MethodHandle;

/**
 * Class to invoke CraftChatMessage methods across versions.
 *
 * @author Rubenicos
 */
public class ChatComponent {

    private static final MethodHandle fromString;
    private static final MethodHandle fromComponent;
    private static final MethodHandle fromJson;
    private static final MethodHandle toJson;

    static {
        MethodHandle m1 = null, m2 = null, m3 = null, m4 = null;
        try {
            Class<?> clazz = EasyLookup.addNMSClass("network.chat.IChatBaseComponent");
            EasyLookup.addClass("ChatSerializer", clazz.getDeclaredClasses()[0]);
            EasyLookup.addOBCClass("util.CraftChatMessage");

            if (ServerInstance.verNumber >= 13) {
                m1 = EasyLookup.staticMethod("CraftChatMessage", "fromStringOrNull", "IChatBaseComponent", String.class);
            } else {
                // Unreflect reason:
                // Return IChatBaseComponent array
                m1 = EasyLookup.unreflectMethod("CraftChatMessage", "fromString", String.class);
            }
            m2 = EasyLookup.staticMethod("CraftChatMessage", "fromComponent", String.class, "IChatBaseComponent");

            // Unreflect reason:
            // (1.8 - 1.15) return IChatBaseComponent
            // Other versions return IChatMutableComponent
            m3 = EasyLookup.unreflectMethod("ChatSerializer", "a", String.class);
            m4 = EasyLookup.staticMethod("ChatSerializer", "a", String.class, "IChatBaseComponent");
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        fromString = m1;
        fromComponent = m2;
        fromJson = m3;
        toJson = m4;
    }

    ChatComponent() {
    }

    /**
     * Convert json component string to IChatBaseComponent.
     *
     * @param json Json to convert.
     * @return     A IChatBaseComponent instance.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static Object fromJson(String json) throws Throwable {
        return json == null || json.isEmpty() ? null : fromJson.invoke(json);
    }

    /**
     * Convert string to IChatBaseComponent.
     *
     * @param string Json to convert.
     * @return       A IChatBaseComponent instance.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static Object fromString(String string) throws Throwable {
        if (ServerInstance.verNumber >= 13) {
            return fromString.invoke(string);
        } else {
            return string == null || string.isEmpty() ? null : ((Object[]) fromString.invoke(string))[0];
        }
    }

    /**
     * Convert IChatBaseComponent to json component.
     *
     * @param component IChatBaseComponent to convert.
     * @return          A json component.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static String toJson(Object component) throws Throwable {
        return (String) toJson.invoke(component);
    }

    /**
     * Convert string to json component string.
     *
     * @param string String to convert.
     * @return       A json component.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static String toJson(String string) throws Throwable {
        return toJson(fromString(string));
    }

    /**
     * Convert IChatBaseComponent to json component.
     *
     * @param component IChatBaseComponent to convert.
     * @return          A json component or null.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static String toJsonOrNull(Object component) throws Throwable {
        return component == null ? null : toJson(component);
    }

    /**
     * Convert string to json component.
     *
     * @param string String to convert.
     * @return       A json component or null.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static String toJsonOrNull(String string) throws Throwable {
        return toJsonOrNull(fromString(string));
    }

    /**
     * Convert IChatBaseComponent to string.
     *
     * @param component IChatBaseComponent to convert.
     * @return          A string with old format.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static String toString(Object component) throws Throwable {
        return (String) fromComponent.invoke(component);
    }

    /**
     * Convert json component to string.
     *
     * @param json Json to convert.
     * @return     A string with old format.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static String toString(String json) throws Throwable {
        return toString(fromJson(json));
    }

    /**
     * Convert IChatBaseComponent to string.
     *
     * @param component IChatBaseComponent to convert.
     * @return          A string with old format or null.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static String toStringOrNull(Object component) throws Throwable {
        return component == null ? null : toString(component);
    }

    /**
     * Convert json component to string.
     *
     * @param json Json to convert.
     * @return     A string with old format or null.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static String toStringOrNull(String json) throws Throwable {
        return toStringOrNull(fromJson(json));
    }
}
