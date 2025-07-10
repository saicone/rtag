package com.saicone.rtag.data;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;
import com.saicone.rtag.Rtag;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.util.EasyLookup;
import com.saicone.rtag.util.ServerInstance;
import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
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

    // Import reflected classes
    static {
        try {
            if (ServerInstance.MAJOR_VERSION >= 13) {
                EasyLookup.addNMSClass("nbt.DynamicOpsNBT", "NbtOps");
            }
            if (ServerInstance.VERSION >= 18.02f) {
                EasyLookup.addNMSClass("resources.RegistryOps");
            }
            if (ServerInstance.Release.COMPONENT) {
                EasyLookup.addNMSClass("core.Holder");
                EasyLookup.addNMSClass("core.RegistryMaterials", "MappedRegistry");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static final Class<?> COMPONENT_TYPE = EasyLookup.classById("DataComponentType");
    private static final Class<?> REGISTRY_OPS_TYPE = EasyLookup.classById("RegistryOps");

    private static final Map<String, Object> TYPES = new HashMap<>();
    private static final Map<String, Codec<Object>> CODECS = new HashMap<>();

    /**
     * NbtOps public instance from Minecraft code.
     */
    @ApiStatus.Experimental
    public static final Object NBT_OPS;
    /**
     * JavaOps public instance from DataFixerUpper library.
     */
    @ApiStatus.Experimental
    public static final Object JAVA_OPS = JavaOps.INSTANCE;

    private static final MethodHandle CREATE;
    private static final MethodHandle CODEC;

    // Maybe move this into other place
    private static DynamicOps<Object> REGISTRY_NBT_OPS;
    private static DynamicOps<JsonElement> REGISTRY_JSON_OPS;
    private static DynamicOps<Object> REGISTRY_JAVA_OPS;

    static {
        // Instances
        DynamicOps<Object> nbtOps = null;
        // Methods
        MethodHandle method$create = null;
        MethodHandle method$codec = null;
        if (ServerInstance.Release.COMPONENT) {
            try {
                // Old names
                String nbtOps$instance = "a";
                String registry$create = "a";
                String registry$components = "as";
                String registry$map = "f";
                String resource$key = "a";
                String holder$value = "a";
                String codec = "b";

                // New names
                if (ServerInstance.Type.MOJANG_MAPPED) {
                    nbtOps$instance = "INSTANCE";
                    registry$create = "create";
                    registry$components = "DATA_COMPONENT_TYPE";
                    registry$map = "byLocation";
                    resource$key = "getPath";
                    holder$value = "value";
                    codec = "codec";
                } else {
                    if (ServerInstance.MAJOR_VERSION >= 21) {
                        registry$components = "aq";
                    }
                    if (ServerInstance.VERSION >= 21.02f) { // 1.21.2
                        registry$components = "ao";
                        registry$map = "e";
                    }
                    if (ServerInstance.VERSION >= 21.04f) { // 1.21.5
                        registry$components = "am";
                    }
                }

                nbtOps = (DynamicOps<Object>) EasyLookup.classById("DynamicOpsNBT").getDeclaredField(nbtOps$instance).get(null);

                method$create = EasyLookup.staticMethod("RegistryOps", registry$create, "RegistryOps", DynamicOps.class, "HolderLookup.Provider");

                final Object componentsRegistry = EasyLookup.classById("BuiltInRegistries").getDeclaredField(registry$components).get(null);
                final Map<Object, Object> componentsMap = (Map<Object, Object>) EasyLookup.field("RegistryMaterials", registry$map).get(componentsRegistry);
                final Method keyMethod = EasyLookup.classById("MinecraftKey").getDeclaredMethod(resource$key);
                final Method valueMethod = EasyLookup.classById("Holder").getDeclaredMethod(holder$value);
                final Method codecMethod = EasyLookup.classById("DataComponentType").getDeclaredMethod(codec);

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
            } catch (NoSuchFieldException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        NBT_OPS = nbtOps;
        CREATE = method$create;
        CODEC = method$codec;

        try {
            REGISTRY_NBT_OPS = createGlobalContext(NBT_OPS);
            REGISTRY_JSON_OPS = createGlobalContext(JsonOps.INSTANCE);
            REGISTRY_JAVA_OPS = createGlobalContext(JAVA_OPS);
        } catch (Throwable t) {
            t.printStackTrace();
        }
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
     * Get all DataComponentType objects associated with its ID.
     *
     * @return an unmodifiable view of all types.
     */
    public static Map<String, Object> all() {
        return Collections.unmodifiableMap(TYPES);
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
     * Create a serialization context using a globalized registry.
     *
     * @param dynamicOps the DynamicOps instance to wrap.
     * @return           a serialization context.
     * @param <T> the expected type of object.
     */
    @ApiStatus.Internal
    public static <T> T createGlobalContext(Object dynamicOps) {
        return createSerializationContext(dynamicOps, Rtag.getMinecraftRegistry());
    }

    /**
     * Create a serialization context using HolderLookup.Provider.
     *
     * @param dynamicOps the DynamicOps instance to wrap.
     * @param provider   the access provider.
     * @return           a serialization context.
     * @param <T> the expected type of object.
     */
    @ApiStatus.Internal
    @SuppressWarnings("unchecked")
    public static <T> T createSerializationContext(Object dynamicOps, Object provider) {
        if (REGISTRY_OPS_TYPE.isInstance(dynamicOps)) {
            return (T) dynamicOps;
        } else {
            try {
                return (T) CREATE.invoke(dynamicOps, provider);
            } catch (Throwable e) {
                throw new RuntimeException("Cannot create serialization context", e);
            }
        }
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
    public static <T> Optional<Object> parse(Object type, DynamicOps<T> dynamicOps, T object) {
        final Codec<Object> codec = codec(type);
        if (codec == null) {
            return Optional.empty();
        }
        try {
            final DataResult<Object> dataResult = codec.parse(createGlobalContext(dynamicOps), object);
            if (dataResult.isError()) {
                throw new IllegalArgumentException("" + dataResult);
            }
            return dataResult.result();
        } catch (Throwable t) {
            throw new RuntimeException("Cannot parse component" + (type instanceof String ? " '" + type + "'" : ""), t);
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
        return parse(type, REGISTRY_NBT_OPS, nbt);
    }

    /**
     * Parse provided json into new component declared type.
     *
     * @param type the DataComponentType instance or ID with declared type.
     * @param json the json object to parse into component object type.
     * @return     an optional object with the result of conversion.
     */
    public static Optional<Object> parseJson(Object type, JsonElement json) {
        return parse(type, REGISTRY_JSON_OPS, json);
    }

    /**
     * Parse provided regular Java object into new component declared type.
     *
     * @param type   the DataComponentType instance or ID with declared type.
     * @param object the object to parse into component object type.
     * @return       an optional object with the result of conversion.
     */
    public static Optional<Object> parseJava(Object type, Object object) {
        return parse(type, REGISTRY_JAVA_OPS, object);
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
            final DataResult<T> dataResult = codec.encodeStart(createGlobalContext(dynamicOps), component);
            if (dataResult.isError()) {
                throw new IllegalArgumentException("" + dataResult);
            }
            return dataResult.result();
        } catch (Throwable t) {
            throw new RuntimeException("Cannot encode component" + (type instanceof String ? " '" + type + "'" : ""), t);
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
        return encode(type, REGISTRY_NBT_OPS, component);
    }

    /**
     * Encode declared component object type into json object.
     *
     * @param type      the DataComponentType instance or ID with declared type.
     * @param component the declared component object to encode.
     * @return          an optional object with the result of conversion.
     */
    public static Optional<JsonElement> encodeJson(Object type, Object component) {
        return encode(type, REGISTRY_JSON_OPS, component);
    }

    /**
     * Encode declared component object type into regular Java object.
     *
     * @param type      the DataComponentType instance or ID with declared type.
     * @param component the declared component object to encode.
     * @return          an optional object with the result of conversion.
     */
    public static Optional<Object> encodeJava(Object type, Object component) {
        return encode(type, REGISTRY_JAVA_OPS, component);
    }
}
