package com.saicone.rtag.util;

import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Class to invoke CraftChatMessage methods across versions.
 *
 * @author Rubenicos
 */
public class ChatComponent {

    /**
     * Default color palette for pretty nbt.
     */
    public static final String[] NBT_PALETTE = new String[] { "§f", "§b", "§a", "§6", "§c" };
    /**
     * Color palette with hex colors introduced on MC 1.16.
     */
    public static final String[] NBT_PALETTE_HEX = new String[] { "§#FCFCFC", "§#55E3FF", "§#55FF71", "§#FFD500", "§#FF7155" };
    /**
     * Color palette with hex colors introduced on MC 1.16 using Bungeecord chat format.
     */
    public static final String[] NBT_PALETTE_BUNGEE = new String[] { "§x§F§C§F§C§F§C", "§x§5§5§E§3§F§F", "§x§5§5§F§F§7§1", "§x§F§F§D§5§0§0", "§x§F§F§7§1§5§5" };
    /**
     * Color palette using Adventure MiniMessage format.
     */
    public static final String[] NBT_PALETTE_MINIMESSAGE = new String[] { "<white>", "<aqua>", "<green>", "<gold>", "<red>" };

    private static final Class<?> CHAT_BASE_COMPONENT;
    private static final char[] RAW_SUFFIX = new char[] { '\0', 'b', 's', '\0', 'L', 'f', 'd', 'B', '\0', '\0', '\0', 'I', 'L' };
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

    /**
     * Format the provided tag into pretty string as chat component.
     *
     * @param tag     the tag to format.
     * @param indent  the indent to use for multi-line formatting, use null or empty
     *                string for single-line format.
     * @return        a pretty formated nbt object as chat component.
     */
    public static Object toPrettyComponent(Object tag, String indent) {
        return toPrettyComponent(tag, indent, NBT_PALETTE);
    }

    /**
     * Format the provided tag into pretty string as chat component.
     *
     * @param tag     the tag to format.
     * @param indent  the indent to use for multi-line formatting, use null or empty
     *                string for single-line format.
     * @param palette the color palette to use.
     * @return        a pretty formated nbt object as chat component.
     */
    public static Object toPrettyComponent(Object tag, String indent, String[] palette) {
        return fromString(toPrettyString(tag, indent, palette));
    }

    /**
     * Format the provided tag into pretty string as json component.
     *
     * @param tag     the tag to format.
     * @param indent  the indent to use for multi-line formatting, use null or empty
     *                string for single-line format.
     * @return        a pretty formated nbt object as json.
     */
    public static String toPrettyJson(Object tag, String indent) {
        return toPrettyJson(tag, indent, NBT_PALETTE);
    }

    /**
     * Format the provided tag into pretty string as json component.
     *
     * @param tag     the tag to format.
     * @param indent  the indent to use for multi-line formatting, use null or empty
     *                string for single-line format.
     * @param palette the color palette to use.
     * @return        a pretty formated nbt object as json.
     */
    public static String toPrettyJson(Object tag, String indent, String[] palette) {
        return toJson(toPrettyString(tag, indent, palette));
    }

    /**
     * Format the provided tag into pretty string.
     *
     * @param tag     the tag to format.
     * @param indent  the indent to use for multi-line formatting, use null or empty
     *                string for single-line format.
     * @return        a pretty formated nbt object as string.
     */
    public static String toPrettyString(Object tag, String indent) {
        return toPrettyString(tag, indent, NBT_PALETTE);
    }

    /**
     * Format the provided tag into pretty string.
     *
     * @param tag     the tag to format.
     * @param indent  the indent to use for multi-line formatting, use null or empty
     *                string for single-line format.
     * @param palette the color palette to use.
     * @return        a pretty formated nbt object as string.
     */
    public static String toPrettyString(Object tag, String indent, String[] palette) {
        return pretty(tag, 0, indent == null ? "" : indent, palette);
    }

    private static String pretty(Object tag, int count, String indent, String[] palette) {
        final byte id = TagBase.getTypeId(tag);

        // Not supported types
        if (id < 1 || id > 12) {
            return palette[4] + "null";
        }

        // Raw types
        if (id <= 6) {
            // <value><suffix>
            return palette[3] + TagBase.getValue(tag) + palette[4] + RAW_SUFFIX[id];
        }

        final StringJoiner joiner;
        switch (id) {
            case 7:  // byte[]
            case 11: // int[]
            case 12: // long[]
                String suffix = palette[4] + RAW_SUFFIX[id];
                // [<suffix>; <value><suffix>, <value><suffix>, <value><suffix>...]
                joiner = new StringJoiner(palette[0] + ", ", palette[0] + '[' + suffix + palette[0] + "; ", palette[0] + ']');
                if (id == 11) {
                    suffix = "";
                }
                for (Object o : OptionalType.of(TagBase.getValue(tag))) {
                    joiner.add(palette[3] + o + suffix);
                }
                return joiner.toString();
            case 8: // String
                // "<value>"
                return palette[0] + '"' + palette[2] + TagBase.getValue(tag) + palette[0] + '"';
            case 9: // List
                // [<pretty value>, <pretty value>, <pretty value>...]
                return prettyList(tag, count, indent, palette);
            case 10: // Compound
            default:
                // {<key>: <pretty value>, <key>: <pretty value>, <key>: <pretty value>...}
                return prettyCompound(tag, count, indent, palette);
        }
    }

    private static String prettyList(Object tag, int count, String indent, String[] palette) {
        final List<Object> list = TagList.getValue(tag);

        if (list.isEmpty()) {
            return palette[0] + "[]";
        }

        final StringJoiner joiner;
        final byte listId;
        if (indent.isEmpty() || (list.size() <= 8 && (listId = TagList.getType(tag)) >= 1 && listId <= 6)) {
            joiner = new StringJoiner(
                    palette[0] + ", ",
                    palette[0] + '[',
                    palette[0] + ']');
        } else {
            final String s = indent.repeat(count + 1);
            joiner = new StringJoiner(
                    palette[0] + ",\n" + s,
                    palette[0] + "[\n" + s,
                    palette[0] + '\n' + indent.repeat(count) + ']');
        }

        for (Object o : list) {
            joiner.add(pretty(o, count + 1, indent, palette));
        }
        return joiner.toString();
    }

    private static String prettyCompound(Object tag, int count, String indent, String[] palette) {
        final Map<String, Object> map = TagCompound.getValue(tag);

        if (map.isEmpty()) {
            return palette[0] + "{}";
        }

        final StringJoiner joiner;
        if (indent.isEmpty()) {
            joiner = new StringJoiner(
                    palette[0] + ", ",
                    palette[0] + '{',
                    palette[0] + '}');
        } else {
            final String s = indent.repeat(count + 1);
            joiner = new StringJoiner(
                    palette[0] + ",\n" + s,
                    palette[0] + "{\n" + s,
                    palette[0] + '\n' + indent.repeat(count) + '}');
        }

        for (var entry : map.entrySet()) {
            joiner.add(palette[1] + entry.getKey() + palette[0] + ": " + pretty(entry.getValue(), count + 1, indent, palette));
        }
        return joiner.toString();
    }
}
