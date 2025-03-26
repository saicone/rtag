package com.saicone.rtag.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.saicone.rtag.RtagMirror;
import com.saicone.rtag.data.ComponentType;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Class to invoke CraftChatMessage methods across versions.
 *
 * @author Rubenicos
 */
@SuppressWarnings("deprecation")
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

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final char[] RAW_SUFFIX = new char[] { '\0', 'b', 's', '\0', 'L', 'f', 'd', 'B', '\0', '\0', '\0', 'I', 'L' };

    private static final MethodHandle fromString;
    private static final MethodHandle fromComponent;
    // pre 1.20.5
    private static final MethodHandle fromJson;
    private static final MethodHandle toJson;
    // since 1.20.5
    private static final Codec<Object> COMPONENT_CODEC;

    static {
        // CraftChatMessage util class
        MethodHandle method$fromString = null;
        MethodHandle method$fromComponent = null;
        // ChatSerializer MC class
        MethodHandle method$fromJson = null;
        MethodHandle method$toJson = null;
        // ComponentSerialization
        MethodHandle component$codec = null;
        try {
            EasyLookup.addNMSClass("network.chat.IChatBaseComponent", "Component");
            EasyLookup.addNMSClassId("ChatSerializer", "network.chat.IChatBaseComponent$ChatSerializer", "network.chat.Component$Serializer");
            EasyLookup.addOBCClass("util.CraftChatMessage");
            if (ServerInstance.Release.COMPONENT) {
                EasyLookup.addNMSClass("network.chat.ComponentSerialization");
            }

            // Old names
            String fromJson = "a";
            String toJson = "a";
            String codec = "a";
            // New names
            if (ServerInstance.Type.MOJANG_MAPPED) {
                fromJson = "fromJson";
                toJson = "toJson";
                codec = "CODEC";
            }

            if (ServerInstance.MAJOR_VERSION >= 13) {
                method$fromString = EasyLookup.staticMethod("CraftChatMessage", "fromStringOrNull", "IChatBaseComponent", String.class);
            } else {
                // Unreflect reason:
                // Return IChatBaseComponent array
                method$fromString = EasyLookup.unreflectMethod("CraftChatMessage", "fromString", String.class);
            }
            method$fromComponent = EasyLookup.staticMethod("CraftChatMessage", "fromComponent", String.class, "IChatBaseComponent");

            if (ServerInstance.Release.COMPONENT) {
                component$codec = EasyLookup.staticGetter("ComponentSerialization", codec, Codec.class);
            } else {
                // Unreflect reason:
                // (1.8 - 1.15) return IChatBaseComponent
                // Other versions return IChatMutableComponent
                method$fromJson = EasyLookup.unreflectMethod("ChatSerializer", fromJson, String.class);
                method$toJson = EasyLookup.staticMethod("ChatSerializer", toJson, String.class, "IChatBaseComponent");
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        CHAT_BASE_COMPONENT = EasyLookup.classById("IChatBaseComponent");
        fromString = method$fromString;
        fromComponent = method$fromComponent;
        fromJson = method$fromJson;
        toJson = method$toJson;

        Codec<Object> componentCodec = null;
        if (component$codec != null) {
            try {
                componentCodec = (Codec<Object>) component$codec.invoke();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        COMPONENT_CODEC = componentCodec;
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
     * Convert json component string to IChatBaseComponent.<br>
     * Supports any old representation of text component.
     *
     * @param json json to convert.
     * @return     a IChatBaseComponent instance.
     */
    public static Object fromJson(String json) {
        try {
            if (json == null || json.isEmpty()) {
                return null;
            }
            if (ServerInstance.Release.COMPONENT) {
                JsonElement element;
                try {
                    element = new JsonParser().parse(json);
                } catch (JsonSyntaxException e) {
                    // Plain text compatibility
                    element = new JsonObject();
                    element.getAsJsonObject().addProperty("text", json);
                }
                if (element == null) {
                    return null;
                }
                if (ServerInstance.VERSION >= 21.04f) {
                    updateJson(element);
                }
                return COMPONENT_CODEC.parse(ComponentType.createGlobalContext(JsonOps.INSTANCE), element).getOrThrow(JsonParseException::new);
            } else {
                return fromJson.invoke(json);
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private static void updateJson(JsonElement element) {
        if (element == null) return;
        if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();

            final JsonElement hoverEvent = object.remove("hoverEvent");
            if (hoverEvent != null && hoverEvent.isJsonObject()) {
                if (updateHoverEvent(hoverEvent.getAsJsonObject())) {
                    object.add("hover_event", hoverEvent);
                }
            }

            final JsonElement clickEvent = object.remove("clickEvent");
            if (clickEvent != null && clickEvent.isJsonObject()) {
                if (updateClickEvent(clickEvent.getAsJsonObject())) {
                    object.add("click_event", clickEvent);
                }
            }

            updateJson(object.get("extra"));
        } else if (element.isJsonArray()) {
            for (JsonElement e : element.getAsJsonArray()) {
                updateJson(e);
            }
        }
    }

    private static boolean updateHoverEvent(JsonObject hoverEvent) {
        final JsonElement jsonAction = hoverEvent.get("action");
        if (jsonAction == null || !jsonAction.isJsonPrimitive()) {
            return false;
        }
        final String action = jsonAction.getAsJsonPrimitive().getAsString();

        // Update very old component
        JsonElement value = hoverEvent.remove("value");
        if (value != null && !value.isJsonNull()) {
            switch (action) {
                case "show_text":
                    hoverEvent.add("contents", value);
                    break;
                case "show_item":
                    if (value.isJsonPrimitive()) {
                        hoverEvent.add("contents", tagToJson(TagCompound.newTag(value.getAsString())));
                    }
                    break;
                case "show_entity":
                    if (value.isJsonPrimitive()) {
                        value = tagToJson(TagCompound.newTag(value.getAsString()));
                        if (value.isJsonObject()) {
                            final JsonElement name = value.getAsJsonObject().remove("name");
                            if (name != null && name.isJsonPrimitive()) {
                                value.getAsJsonObject().add("name", new JsonParser().parse(name.getAsString()));
                            }
                            hoverEvent.add("contents", value);
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        final JsonElement contents = hoverEvent.remove("contents");
        if (contents == null) {
            return false;
        }
        switch (action) {
            case "show_text":
                hoverEvent.add("value", contents);
                break;
            case "show_item":
                if (contents.isJsonPrimitive()) {
                    hoverEvent.add("id", contents);
                } else if (contents.isJsonObject()) {
                    for (Map.Entry<String, JsonElement> entry : contents.getAsJsonObject().entrySet()) {
                        hoverEvent.add(entry.getKey(), entry.getValue());
                    }
                }
                break;
            case "show_entity":
                if (contents.isJsonObject()) {
                    for (Map.Entry<String, JsonElement> entry : contents.getAsJsonObject().entrySet()) {
                        if (entry.getKey().equals("id")) {
                            hoverEvent.add("uuid", entry.getValue());
                        } else if (entry.getKey().equals("type")) {
                            hoverEvent.add("id", entry.getValue());
                        } else {
                            hoverEvent.add(entry.getKey(), entry.getValue());
                        }
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    private static boolean updateClickEvent(JsonObject clickEvent) {
        final JsonElement jsonAction = clickEvent.get("action");
        final JsonElement jsonValue = clickEvent.remove("value");
        if (jsonAction == null || !jsonAction.isJsonPrimitive() || jsonValue == null || !jsonValue.isJsonPrimitive()) {
            return false;
        }
        final String action = jsonAction.getAsString();
        final String value = jsonValue.getAsString();
        switch (action) {
            case "open_url":
                final String s = value.toLowerCase();
                if (!s.startsWith("https://") && !s.startsWith("http://")) {
                    return false;
                }
                clickEvent.add("url", jsonValue);
                break;
            case "open_file":
                clickEvent.add("path", jsonValue);
                break;
            case "run_command":
            case "suggest_command":
                for (int i = 0; i < value.length(); i++) {
                    char c = value.charAt(i);
                    if (c == '\u00A7' || c < ' ' || c == 127) {
                        return false;
                    }
                }
                clickEvent.add("command", jsonValue);
                break;
            case "change_page":
                final int page;
                try {
                    page = Integer.parseInt(value);
                } catch (Throwable t) {
                    return false;
                }
                clickEvent.addProperty("page", Math.max(page, 1));
                break;
            case "copy_to_clipboard":
                clickEvent.add("value", jsonValue);
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * Convert string to IChatBaseComponent.
     *
     * @param string Json to convert.
     * @return       A IChatBaseComponent instance.
     */
    public static Object fromString(String string) {
        try {
            if (ServerInstance.MAJOR_VERSION >= 13) {
                return fromString.invoke(string);
            } else {
                return string == null || string.isEmpty() ? null : ((Object[]) fromString.invoke(string))[0];
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Convert nbt to IChatBaseComponent.<br>
     * Supports newer representation of text component on older server versions.
     *
     * @param tag nbt to convert.
     * @return    a IChatBaseComponent instance.
     */
    public static Object fromTag(Object tag) {
        final Object currentTag;
        if (ServerInstance.VERSION < 21.04f) {
            if (TagList.isTagList(tag)) {
                currentTag = downgradeTagList(TagList.clone(tag));
            } else if (TagCompound.isTagCompound(tag)) {
                currentTag = downgradeTagCompound(TagCompound.clone(tag));
            } else {
                currentTag = tag;
            }
        } else {
            currentTag = tag;
        }
        if (ServerInstance.Release.COMPONENT) {
            return COMPONENT_CODEC.parse(ComponentType.createGlobalContext(ComponentType.NBT_OPS), currentTag).getOrThrow();
        } else {
            return fromJson(GSON.toJson(tagToJson(currentTag)));
        }
    }

    private static void downgradeTag(Object tag) {
        if (TagCompound.isTagCompound(tag)) {
            downgradeTagCompound(tag);
        } else if (TagList.isTagList(tag)) {
            downgradeTagList(tag);
        }
    }

    private static Object downgradeTagList(Object tag) {
        for (Object element : TagList.getValue(tag)) {
            downgradeTag(element);
        }
        return tag;
    }

    private static Object downgradeTagCompound(Object tag) {
        final Map<String, Object> map = TagCompound.getValue(tag);
        final Object hoverEvent = map.remove("hover_event");
        if (TagCompound.isTagCompound(hoverEvent) && downgradeHoverEvent(TagCompound.getValue(hoverEvent))) {
            map.put("hoverEvent", hoverEvent);
        }

        final Object clickEvent = map.remove("click_event");
        if (TagCompound.isTagCompound(clickEvent) && downgradeClickEvent(TagCompound.getValue(clickEvent))) {
            map.put("clickEvent", clickEvent);
        }

        downgradeTag(map.get("extra"));
        return tag;
    }

    private static boolean downgradeHoverEvent(Map<String, Object> hoverEvent) {
        final Object tagAction = hoverEvent.get("action");
        if (tagAction == null || TagBase.getTypeId(tagAction) != 8) {
            return false;
        }
        final String action = (String) TagBase.getValue(tagAction);
        final Map<String, Object> contents;
        switch (action) {
            case "show_text":
                final Object value = hoverEvent.get("value");
                if (value == null) {
                    return false;
                }
                hoverEvent.put("contents", value);
                return true;
            case "show_item":
                contents = new HashMap<>();
                hoverEvent.entrySet().removeIf(entry -> {
                   if (entry.getKey().equals("action")) {
                       return false;
                   }
                   contents.put(entry.getKey(), entry.getValue());
                   return true;
                });
                break;
            case "show_entity":
                contents = new HashMap<>();
                hoverEvent.entrySet().removeIf(entry -> {
                    switch (entry.getKey()) {
                        case "action":
                            return false;
                        case "uuid":
                            contents.put("id", entry.getValue());
                            break;
                        case "id":
                            contents.put("type", entry.getValue());
                            break;
                        default:
                            contents.put(entry.getKey(), entry.getValue());
                            break;
                    }
                    return true;
                });
                break;
            default:
                return true;
        }
        if (contents.isEmpty()) {
            return false;
        }
        hoverEvent.put("contents", contents);
        return true;
    }

    private static boolean downgradeClickEvent(Map<String, Object> clickEvent) {
        final Object tagAction = clickEvent.get("action");
        if (tagAction == null || TagBase.getTypeId(tagAction) != 8) {
            return false;
        }
        final String action = (String) TagBase.getValue(tagAction);
        final String key;
        switch (action) {
            case "open_url":
                key = "url";
                break;
            case "open_file":
                key = "path";
                break;
            case "run_command":
            case "suggest_command":
                key = "command";
                break;
            case "change_page":
                final Object page = clickEvent.get("page");
                if (page == null) {
                    return false;
                }
                clickEvent.put("value", TagBase.newTag(String.valueOf(TagBase.getValue(page))));
                return true;
            case "copy_to_clipboard":
            default:
                return true;
        }
        final Object value = clickEvent.get(key);
        if (value == null) {
            return false;
        }
        clickEvent.put("value", value);
        return true;
    }

    // Taken from https://github.com/saicone/nbt
    private static JsonElement tagToJson(Object tag) {
        if (tag == null) {
            return JsonNull.INSTANCE;
        }
        final Object value = TagBase.getValue(RtagMirror.INSTANCE, tag);
        if (value instanceof Boolean) {
            return new JsonPrimitive((Boolean) value);
        };
        switch (TagBase.getTypeId(tag)) {
            case 0:
                return JsonNull.INSTANCE;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                return new JsonPrimitive((Number) value);
            case 8:
                return new JsonPrimitive((String) value);
            case 7:
            case 11:
            case 12:
                final int size = Array.getLength(value);
                final JsonArray array = new JsonArray(size);
                for (int i = 0; i < size; i++) {
                    final Object element = Array.get(value, i);
                    array.add(new JsonPrimitive((Number) element));
                }
                return array;
            case 9:
                final JsonArray list = new JsonArray(((List<Object>) value).size());
                for (Object o : (List<Object>) value) {
                    final JsonElement element = tagToJson(o);
                    list.add(element);
                }
                return list;
            case 10:
                final JsonObject map = new JsonObject();
                for (var entry : ((Map<Object, Object>) value).entrySet()) {
                    final JsonElement element = tagToJson(entry.getValue());
                    map.add(String.valueOf(entry.getKey()), element);
                }
                return map;
            default:
                throw new IllegalArgumentException("Invalid tag type: " + tag);
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
            final Object o = fromString((String) component);
            if (o == null) {
                return "{\"text\":\"\"}";
            }
            return toJson(o);
        }
        Objects.requireNonNull(component, "The provided object cannot be null");
        if (!CHAT_BASE_COMPONENT.isInstance(component)) {
            throw new IllegalArgumentException("The provided object isn't an IChatBaseComponent");
        }
        try {
            if (ServerInstance.Release.COMPONENT) {
                final JsonElement element = COMPONENT_CODEC.encodeStart(ComponentType.createGlobalContext(JsonOps.INSTANCE), component).getOrThrow(JsonParseException::new);
                return GSON.toJson(element);
            } else {
                return (String) toJson.invoke(component);
            }
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
            final Object o = fromJson((String) component);
            if (o == null) {
                return "";
            }
            return toString(o);
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
     * Convert IChatBaseComponent to nbt.
     *
     * @param component IChatBaseComponent to convert.
     * @return          a nbt object.
     * @throws IllegalArgumentException if component is not a valid ChatComponent.
     */
    public static Object toTag(Object component) {
        Objects.requireNonNull(component, "The provided object cannot be null");
        if (!CHAT_BASE_COMPONENT.isInstance(component)) {
            throw new IllegalArgumentException("The provided object isn't an IChatBaseComponent");
        }
        if (ServerInstance.Release.COMPONENT) {
            try {
                return COMPONENT_CODEC.encodeStart(ComponentType.createGlobalContext(ComponentType.NBT_OPS), component).getOrThrow(IllegalArgumentException::new);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        } else {
            final String json = toJson(component);
            return tagFromJson(new JsonParser().parse(json));
        }
    }

    /**
     * Convert IChatBaseComponent to nbt.
     *
     * @param component IChatBaseComponent to convert.
     * @return          a nbt object or null.
     * @throws IllegalArgumentException if component is not a valid ChatComponent.
     */
    public static Object toTagOrNull(Object component) {
        return component == null ? null : toTag(component);
    }

    // Taken from https://github.com/saicone/nbt
    public static Object tagFromJson(JsonElement element) {
        if (element.isJsonPrimitive()) {
            final JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return TagBase.newTag(primitive.getAsBoolean());
            } else if (primitive.isNumber()) {
                return TagBase.newTag(primitive.getAsNumber());
            } else if (primitive.isString()) {
                return TagBase.newTag(primitive.getAsString());
            }
        } else if (element.isJsonArray()) {
            final JsonArray array = element.getAsJsonArray();
            if (array.isEmpty()) {
                return TagList.newTag();
            }
            final JsonElement first = array.get(0);
            if (first.isJsonPrimitive()) {
                final JsonPrimitive primitive = first.getAsJsonPrimitive();
                if (primitive.isNumber()) {
                    final Number number = primitive.getAsNumber();
                    if (number instanceof Byte) {
                        final byte[] bytes = new byte[array.size()];
                        int i = 0;
                        for (JsonElement e : array) {
                            bytes[i] = e.getAsByte();
                            i++;
                        }
                        return TagBase.newTag(bytes);
                    } else if (number instanceof Integer) {
                        final int[] integers = new int[array.size()];
                        int i = 0;
                        for (JsonElement e : array) {
                            integers[i] = e.getAsInt();
                            i++;
                        }
                        return TagBase.newTag(integers);
                    } else if (number instanceof Long) {
                        final long[] longs = new long[array.size()];
                        int i = 0;
                        for (JsonElement e : array) {
                            longs[i] = e.getAsLong();
                            i++;
                        }
                        return TagBase.newTag(longs);
                    }
                }
            }
            final List<Object> list = new ArrayList<>();
            for (JsonElement e : array) {
                final Object t = tagFromJson(e);
                if (t != null) {
                    list.add(t);
                }
            }
            return TagList.newUncheckedTag(list);
        } else if (element.isJsonObject()) {
            final JsonObject json = element.getAsJsonObject();
            final Map<String, Object> map = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                final Object e = tagFromJson(entry.getValue());
                if (e != null) {
                    map.put(entry.getKey(), e);
                }
            }
            return TagCompound.newUncheckedTag(map);
        }
        throw new IllegalArgumentException("Cannot get value from json: " + element);
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
        if (indent.isEmpty() || (list.size() <= 8 && TagList.getValue(tag).stream().anyMatch(value -> {
            final int id = TagBase.getTypeId(value);
            return id >= 1 && id <= 6;
        }))) {
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
