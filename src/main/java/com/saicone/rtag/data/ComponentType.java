package com.saicone.rtag.data;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.util.EasyLookup;
import com.saicone.rtag.util.ServerInstance;
import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Class to invoke methods inside DataComponentType and handle types in a flexible way.
 *
 * @author Rubenicos
 */
@ApiStatus.Experimental
public class ComponentType {

    private static final Class<?> COMPONENT_TYPE = EasyLookup.classById("DataComponentType");

    private static final Map<String, Object> TYPES = new HashMap<>();
    private static final Map<String, Codec<Object>> CODECS = new HashMap<>();

    /**
     * DynamicOpsNBT public instance from Minecraft code.
     */
    @ApiStatus.Experimental
    public static final DynamicOps<Object> NBT_OPS;
    /**
     * JavaOps public instance from DataFixerUpper library.
     */
    // Get by reflection due newer DataFixerUpper versions require Java 17
    @ApiStatus.Experimental
    public static final DynamicOps<Object> JAVA_OPS;

    // Use reflection due newer DataFixerUpper is compiled different
    private static final MethodHandle RESULT;
    private static final MethodHandle CODEC;

    static {
        DynamicOps<Object> nbtOps = null;
        DynamicOps<Object> javaOps = null;
        // Methods
        MethodHandle method$result = null;
        MethodHandle method$codec = null;
        if (ServerInstance.Release.COMPONENT) {
            try {
                // Maybe move registry handling into separated utility class
                EasyLookup.addNMSClass("core.RegistryMaterials", "MappedRegistry");
                EasyLookup.addNMSClass("core.Holder");

                // Old names
                String registry$components = "as";
                String registry$map = "f";
                String resource$key = "a";
                String holder$value = "a";
                String codec = "b";
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
                final Map<Object, Object> componentsMap = (Map<Object, Object>) EasyLookup.field("RegistryMaterials", registry$map).get(componentsRegistry);
                final Method keyMethod = EasyLookup.classById("MinecraftKey").getDeclaredMethod(resource$key);
                final Method valueMethod = EasyLookup.classById("Holder").getDeclaredMethod(holder$value);
                final Method codecMethod = EasyLookup.classById("DataComponentType").getDeclaredMethod(codec);

                method$result = EasyLookup.unreflectMethod(DataResult.class.getDeclaredMethod("result"));
                method$codec = EasyLookup.unreflectMethod(codecMethod);

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
        RESULT = method$result;
        CODEC = method$codec;
    }

    ComponentType() {
    }

    /**
     * Get provided DataComponentType or parse from ID.<br>
     * This method may return a null object if the provided ID doesn't exist.
     *
     * @param type the type to get.
     * @return     the same DataComponentType or a new one from cache.
     */
    public static Object of(Object type) {
        if (COMPONENT_TYPE.isInstance(type)) {
            return type;
        }
        return of(String.valueOf(type));
    }

    /**
     * Parse DataComponentType from ID.
     *
     * @param name the type name to get.
     * @return     a DataComponentType from cache or null if provided ID doesn't exist.
     */
    public static Object of(String name) {
        return TYPES.get(key(name));
    }

    /**
     * Get declared codec from DataComponentType instance or ID.<br>
     * Take in count that not all the component types provide a codec instance.
     *
     * @param type the data type to get codec.
     * @return     a codec from DataComponentType or from cache by ID.
     */
    @SuppressWarnings("unchecked")
    public static Codec<Object> codec(Object type) {
        if (COMPONENT_TYPE.isInstance(type)) {
            try {
                return (Codec<Object>) CODEC.invoke(type);
            } catch (Throwable t) {
                throw new RuntimeException("Cannot get codec from component type", t);
            }
        }
        return codec(String.valueOf(type));
    }

    /**
     * Get declared codec from DataComponentType ID.<br>
     * Take in count that not all the component types provide a codec instance.
     *
     * @param name the type name to get codec.
     * @return     a codec from cache by type ID.
     */
    public static Codec<Object> codec(String name) {
        return CODECS.get(key(name));
    }

    /**
     * Parse provided component key into namespaced format.
     *
     * @param name the name key to parse.
     * @return     a namespaced key.
     */
    public static String key(String name) {
        if (name.startsWith("minecraft:") || name.contains(":")) {
            return name;
        } else {
            return "minecraft:" + name;
        }
    }

    /**
     * Check if the provided object is instance of DataComponentType.
     *
     * @param object the object to check.
     * @return       true if the object is a DataComponentType.
     */
    public static boolean isType(Object object) {
        return COMPONENT_TYPE.isInstance(object);
    }

    /**
     * Check if the provided DataComponentType ID exists.
     *
     * @param name the type name to check.
     * @return     true if the component type exists.
     */
    public static boolean exists(String name) {
        return TYPES.containsKey(key(name));
    }

    /**
     * Parse provided object into new component declared type.<br>
     * This method allows any NBT, JsonElement and regular Java object to parse.
     *
     * @param type   the DataComponentType instance or ID with declared type.
     * @param object the object to parse into component object type.
     * @return       an optional object with the result of conversion.
     */
    public static Optional<Object> parse(Object type, Object object) {
        if (TagBase.isTag(object)) {
            return parseNbt(type, object);
        } else if (object instanceof JsonElement) {
            return parseJson(type, (JsonElement) object);
        } else {
            return parseJava(type, object);
        }
    }

    /**
     * Parse provided object by object provider into new component declared type.
     *
     * @param type       the DataComponentType instance or ID with declared type.
     * @param dynamicOps the object provider to parse object.
     * @param object     the object to parse into component object type.
     * @return           an optional object with the result of conversion.
     * @param <T>        the declared type.
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<Object> parse(Object type, DynamicOps<T> dynamicOps, T object) {
        final Codec<Object> codec = codec(type);
        if (codec == null) {
            return Optional.empty();
        }
        try {
            return (Optional<Object>) RESULT.invoke(codec.parse(dynamicOps, object));
        } catch (Throwable t) {
            throw new RuntimeException("Cannot parse component", t);
        }
    }

    /**
     * Parse provided tag into new component declared type.
     *
     * @param type the DataComponentType instance or ID with declared type.
     * @param nbt  the nbt object to parse into component object type.
     * @return     an optional object with the result of conversion.
     */
    public static Optional<Object> parseNbt(Object type, Object nbt) {
        return parse(type, NBT_OPS, nbt);
    }

    /**
     * Parse provided json into new component declared type.
     *
     * @param type the DataComponentType instance or ID with declared type.
     * @param json the json object to parse into component object type.
     * @return     an optional object with the result of conversion.
     */
    public static Optional<Object> parseJson(Object type, JsonElement json) {
        return parse(type, JsonOps.INSTANCE, json);
    }

    /**
     * Parse provided regular Java object into new component declared type.
     *
     * @param type   the DataComponentType instance or ID with declared type.
     * @param object the object to parse into component object type.
     * @return       an optional object with the result of conversion.
     */
    public static Optional<Object> parseJava(Object type, Object object) {
        return parse(type, JAVA_OPS, object);
    }

    /**
     * Encode declared component object type into declared object from object provider type.
     *
     * @param type       the DataComponentType instance or ID with declared type.
     * @param dynamicOps the object provider to create objects from type.
     * @param component  the declared component object to encode.
     * @return           an optional object with the result of conversion.
     * @param <T>        the declared type.
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> encode(Object type, DynamicOps<T> dynamicOps, Object component) {
        if (component == null) {
            return Optional.empty();
        }
        final Codec<Object> codec = codec(type);
        if (codec == null) {
            return Optional.empty();
        }
        try {
            return (Optional<T>) RESULT.invoke(codec.encodeStart(dynamicOps, component));
        } catch (Throwable t) {
            throw new RuntimeException("Cannot encode component", t);
        }
    }

    /**
     * Encode declared component object type into tag object.
     *
     * @param type      the DataComponentType instance or ID with declared type.
     * @param component the declared component object to encode.
     * @return          an optional object with the result of conversion.
     */
    public static Optional<Object> encodeNbt(Object type, Object component) {
        return encode(type, NBT_OPS, component);
    }

    /**
     * Encode declared component object type into json object.
     *
     * @param type      the DataComponentType instance or ID with declared type.
     * @param component the declared component object to encode.
     * @return          an optional object with the result of conversion.
     */
    public static Optional<JsonElement> encodeJson(Object type, Object component) {
        return encode(type, JsonOps.INSTANCE, component);
    }

    /**
     * Encode declared component object type into regular Java object.
     *
     * @param type      the DataComponentType instance or ID with declared type.
     * @param component the declared component object to encode.
     * @return          an optional object with the result of conversion.
     */
    public static Optional<Object> encodeJava(Object type, Object component) {
        return encode(type, JAVA_OPS, component);
    }
}
