package com.saicone.rtag.data;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.util.EasyLookup;
import com.saicone.rtag.util.ServerInstance;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ApiStatus.Experimental
public class ComponentType {

    private static final Class<?> COMPONENT_TYPE = EasyLookup.classById("DataComponentType");

    private static final Map<String, Object> TYPES = new HashMap<>();
    private static final Map<String, Codec<Object>> CODECS = new HashMap<>();

    @ApiStatus.Experimental
    public static final DynamicOps<Object> NBT_OPS;
    // Get by reflection due newer DataFixerUpper versions require Java 17
    @ApiStatus.Experimental
    public static final DynamicOps<Object> JAVA_OPS;

    static {
        DynamicOps<Object> nbtOps = null;
        DynamicOps<Object> javaOps = null;
        if (ServerInstance.Release.COMPONENT) {
            try {
                // Maybe move registry handling into separated utility class
                EasyLookup.addNMSClass("net.minecraft.core.RegistryMaterials", "MappedRegistry");
                EasyLookup.addNMSClass("net.minecraft.core.Holder");

                // Old names
                String registry$components = "at";
                String registry$map = "f";
                String resource$key = "b";
                String holder$value = "a";
                String codec = "d";
                String nbtOps$instance = "a";

                // New names
                if (ServerInstance.Type.MOJANG_MAPPED) {
                    registry$components = "DATA_COMPONENT_TYPE";
                    registry$map = "byLocation";
                    resource$key = "getNamespace";
                    holder$value = "value";
                    codec = "codec";
                    nbtOps$instance = "INSTANCE";
                }

                final Object componentsRegistry = EasyLookup.classById("BuiltInRegistries").getDeclaredField(registry$components).get(null);
                final Map<Object, Object> componentsMap = (Map<Object, Object>) EasyLookup.field("MappedRegistry", registry$map).get(componentsRegistry);
                final Method keyMethod = EasyLookup.classById("MinecraftKey").getDeclaredMethod(resource$key);
                final Method valueMethod = EasyLookup.classById("Holder").getDeclaredMethod(holder$value);
                final Method codecMethod = EasyLookup.classById("DataComponentType").getDeclaredMethod(codec);
                for (var entry : componentsMap.entrySet()) {
                    if (entry.getValue() == null) {
                        continue;
                    }
                    final String key = (String) keyMethod.invoke(entry.getKey());
                    final Object value = valueMethod.invoke(entry.getValue());

                    TYPES.put(key(key), value);

                    final Codec<Object> valueCodec = (Codec<Object>) codecMethod.invoke(value);
                    if (valueCodec != null) {
                        CODECS.put(key(key), valueCodec);
                    }
                }

                nbtOps = (DynamicOps<Object>) EasyLookup.classById("DynamicOpsNBT").getDeclaredField(nbtOps$instance).get(null);
                javaOps = (DynamicOps<Object>) Class.forName("com.mojang.serialization.JavaOps").getDeclaredField("INSTANCE").get(null);
            } catch (NoSuchFieldException | ClassNotFoundException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        NBT_OPS = nbtOps;
        JAVA_OPS = javaOps;
    }

    ComponentType() {
    }

    public static Object of(Object type) {
        if (type instanceof String) {
            return of((String) type);
        } else {
            return type;
        }
    }

    public static Object of(String name) {
        return TYPES.get(key(name));
    }

    public static Codec<Object> codec(String name) {
        return CODECS.get(key(name));
    }

    public static String key(String name) {
        if (name.startsWith("minecraft:") || name.contains(":")) {
            return name;
        } else {
            return "minecraft:" + name;
        }
    }

    public static boolean isType(Object object) {
        return COMPONENT_TYPE.isInstance(object);
    }

    public static boolean exists(String name) {
        return TYPES.containsKey(key(name));
    }

    public static Optional<Object> parse(String name, Object object) {
        if (TagBase.isTag(object)) {
            return parseNbt(name, object);
        } else if (object instanceof JsonElement) {
            return parseJson(name, (JsonElement) object);
        } else {
            return parseJava(name, object);
        }
    }

    public static Optional<Object> parseNbt(String name, Object nbt) {
        final Codec<Object> codec = codec(name);
        return codec == null ? Optional.empty() : codec.parse(NBT_OPS, nbt).result();
    }

    public static Optional<Object> parseJson(String name, JsonElement json) {
        final Codec<Object> codec = codec(name);
        return codec == null ? Optional.empty() : codec.parse(JsonOps.INSTANCE, json).result();
    }

    public static Optional<Object> parseJava(String name, Object object) {
        final Codec<Object> codec = codec(name);
        return codec == null ? Optional.empty() : codec.parse(JAVA_OPS, object).result();
    }

    public static Optional<Object> encodeNbt(String name, Object component) {
        final Codec<Object> codec = codec(name);
        return codec == null ? Optional.empty() : codec.encodeStart(NBT_OPS, component).result();
    }

    public static Optional<JsonElement> encodeJson(String name, Object component) {
        final Codec<Object> codec = codec(name);
        return codec == null ? Optional.empty() : codec.encodeStart(JsonOps.INSTANCE, component).result();
    }

    public static Optional<Object> encodeJava(String name, Object component) {
        final Codec<Object> codec = codec(name);
        return codec == null ? Optional.empty() : codec.encodeStart(JAVA_OPS, component).result();
    }
}
