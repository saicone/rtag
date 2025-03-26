package com.saicone.rtag.data;

import com.saicone.rtag.util.EasyLookup;
import com.saicone.rtag.util.ServerInstance;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.lang.invoke.MethodHandle;
import java.util.Optional;
import java.util.Set;

/**
 * Class to invoke methods from multiple DataComponent classes.
 *
 * @author Rubenicos
 */
@ApiStatus.Experimental
public class DataComponent {

    private static final Class<?> COMPONENT_HOLDER = EasyLookup.classById("DataComponentHolder");
    private static final Class<?> COMPONENT_MAP = EasyLookup.classById("DataComponentMap");
    private static final Class<?> COMPONENT_MAP_PATCH = EasyLookup.classById("PatchedDataComponentMap");
    private static final Class<?> COMPONENT_PATCH = EasyLookup.classById("DataComponentPatch");

    DataComponent() {
    }

    /**
     * Get a declared component type from data component object.
     *
     * @param component the data component to get component.
     * @param type      the DataComponentType instance that declares a component type.
     * @return          the component declared type from data component cache if exists, null otherwise.
     * @throws IllegalArgumentException if the provided component is not a compatible data component type.
     */
    public static Object get(Object component, Object type) throws IllegalArgumentException {
        if (COMPONENT_HOLDER.isInstance(component)) {
            return Holder.get(component, type);
        } else if (COMPONENT_MAP.isInstance(component)) {
            return Map.get(component, type);
        } else if (COMPONENT_PATCH.isInstance(component)) {
            return Patch.get(component, type).orElse(null);
        } else {
            throw new IllegalArgumentException("The object type " + component.getClass().getName() + " is not supported");
        }
    }

    /**
     * Get a declared component type from data component object wrapped as optional object.
     *
     * @param component the data component to get component.
     * @param type      the DataComponentType instance that declares a component type.
     * @return          the component declared type from data component cache wrapped into optional object.
     * @throws IllegalArgumentException if the provided component is not a compatible data component type.
     */
    public static Optional<Object> getOptional(Object component, Object type) throws IllegalArgumentException {
        if (COMPONENT_HOLDER.isInstance(component)) {
            return Optional.ofNullable(Holder.get(component, type));
        } else if (COMPONENT_MAP.isInstance(component)) {
            return Optional.ofNullable(Map.get(component, type));
        } else if (COMPONENT_PATCH.isInstance(component)) {
            return Patch.get(component, type);
        } else {
            throw new IllegalArgumentException("The object type " + component.getClass().getName() + " is not supported");
        }
    }

    /**
     * Class to invoke methods from DataComponentHolder.<br>
     * This type of data component object (as the name said) is just a holder for DataComponentMap to delegate methods.
     */
    @ApiStatus.Experimental
    public static class Holder {
        private static final MethodHandle GET_COMPONENTS;
        private static final MethodHandle GET;
        private static final MethodHandle HAS;

        static {
            // Methods
            MethodHandle method$getComponents = null;
            MethodHandle method$get = null;
            MethodHandle method$has = null;
            if (ServerInstance.Release.COMPONENT) {
                // Old names
                String getComponents = "a";
                String get = "a";
                String has = "b";
                // New names
                if (ServerInstance.Type.MOJANG_MAPPED) {
                    getComponents = "getComponents";
                    get = "get";
                    has = "has";
                } else {
                    if (ServerInstance.VERSION >= 21.04f) { // 1.21.5
                        has = "c";
                    }
                }

                try {
                    method$getComponents = EasyLookup.method(COMPONENT_HOLDER, getComponents, COMPONENT_MAP);
                    method$get = EasyLookup.method(COMPONENT_HOLDER, get, Object.class, "DataComponentType");
                    method$has = EasyLookup.method(COMPONENT_HOLDER, has, boolean.class, "DataComponentType");
                } catch (NoSuchMethodException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            GET_COMPONENTS = method$getComponents;
            GET = method$get;
            HAS = method$has;
        }

        Holder() {
        }

        /**
         * Get DataComponentMap from provided holder.
         *
         * @param holder the component holder to get map from.
         * @return       a DataComponentMap provided by holder.
         */
        public static Object getComponents(Object holder) {
            try {
                return GET_COMPONENTS.invoke(holder);
            } catch (Throwable t) {
                throw new RuntimeException("Cannot get component map from component holder", t);
            }
        }

        /**
         * Get a declared component type from provided holder.
         *
         * @param holder the component holder to get component from.
         * @param type   the DataComponentType instance that declares a component type.
         * @return       the component declared type from data component cache if exists, null otherwise.
         */
        public static Object get(Object holder, Object type) {
            try {
                return GET.invoke(holder, type);
            } catch (Throwable t) {
                throw new RuntimeException("Cannot get component type from component holder", t);
            }
        }

        /**
         * Check if the provided holder has any type of DataComponentType inside.
         *
         * @param holder the component holder to check content.
         * @param type   the DataComponentType instance.
         * @return       true if holder contains the provided component type.
         */
        public static boolean has(Object holder, Object type) {
            try {
                return (boolean) HAS.invoke(holder, type);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    /**
     * Class to invoke methods from DataComponentMap.<br>
     * A component map acts like an immutable map in java with the regular conception of components by Mojang.
     */
    @ApiStatus.Experimental
    public static class Map {

        /**
         * An empty DataComponentMap instance.
         */
        public static final Object EMPTY;

        private static final MethodHandle GET_MAP;
        private static final MethodHandle GET;
        private static final MethodHandle KEY_SET;
        private static final MethodHandle BUILDER;
        private static final MethodHandle BUILDER_MAP;
        private static final MethodHandle BUILDER_BUILD;

        static {
            // Constants
            Object const$empty = null;
            // Getters
            MethodHandle get$map = null;
            MethodHandle get$builder$map = null;
            // Methods
            MethodHandle method$get = null;
            MethodHandle method$keySet = null;
            MethodHandle method$builder = null;
            MethodHandle method$builder$build = null;
            if (ServerInstance.Release.COMPONENT) {
                // Old names
                String empty = "a";
                String map = "c";
                String get = "a";
                String keySet = "b";

                String builder = "a";
                String builder$map = "a";
                String builder$build = "a";

                // New names
                if (ServerInstance.Type.MOJANG_MAPPED) {
                    empty = "EMPTY";
                    map = "map";
                    get = "get";
                    keySet = "keySet";

                    builder = "builder";
                    builder$map = "map";
                    builder$build = "build";
                }

                try {
                    const$empty = COMPONENT_MAP.getDeclaredField(empty).get(null);

                    // Private field
                    get$map = EasyLookup.unreflectGetter("DataComponentMap.SimpleMap", map);
                    // Private field
                    get$builder$map = EasyLookup.unreflectSetter("DataComponentMap.Builder", builder$map);

                    if (ServerInstance.VERSION >= 21.04f) {
                        EasyLookup.addNMSClass("core.component.DataComponentGetter");
                        method$get = EasyLookup.method("DataComponentGetter", get, Object.class, "DataComponentType");
                    } else {
                        method$get = EasyLookup.method(COMPONENT_MAP, get, Object.class, "DataComponentType");
                    }
                    method$keySet = EasyLookup.method(COMPONENT_MAP, keySet, Set.class);
                    method$builder = EasyLookup.staticMethod(COMPONENT_MAP, builder, "DataComponentMap.Builder");
                    method$builder$build = EasyLookup.method("DataComponentMap.Builder", builder$build, COMPONENT_MAP);
                } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }
            EMPTY = const$empty;
            GET_MAP = get$map;
            BUILDER_MAP = get$builder$map;
            GET = method$get;
            KEY_SET = method$keySet;
            BUILDER = method$builder;
            BUILDER_BUILD = method$builder$build;
        }

        Map() {
        }

        /**
         * Create a DataComponentMap builder to set values and then wrap into component map.
         *
         * @return a newly generated builder to edit.
         */
        @SuppressWarnings("unchecked")
        public static Builder<Object> builder() {
            try {
                final Object build = BUILDER.invoke();
                return new Builder<>(build, (Reference2ObjectMap<Object, Object>) BUILDER_MAP.invoke(build)) {
                    @Override
                    public Object build() {
                        try {
                            return BUILDER_BUILD.invoke(build);
                        } catch (Throwable t) {
                            throw new RuntimeException("Cannot build component map from builder", t);
                        }
                    }
                };
            } catch (Throwable t) {
                throw new RuntimeException("Cannot create component map builder", t);
            }
        }

        /**
         * Get the size of provided component map.
         *
         * @param map the component map to get size.
         * @return    the number of data type elements in provided component map.
         */
        public static int size(Object map) {
            return keySet(map).size();
        }

        /**
         * Check if the provided component map is empty.
         *
         * @param map the component map to check.
         * @return    true if the component map has no elements.
         */
        public static boolean isEmpty(Object map) {
            return keySet(map).isEmpty();
        }

        /**
         * Get a declared component type from provided component map.
         *
         * @param map  the component map to get component from.
         * @param type the DataComponentType instance that declares a component type.
         * @return     the component declared type from data component cache if exists, null otherwise.
         */
        public static Object get(Object map, Object type) {
            try {
                return GET.invoke(map, type);
            } catch (Throwable t) {
                throw new RuntimeException("Cannot get component type from component map", t);
            }
        }

        /**
         * Check if the provided component map has any type of DataComponentType inside.
         *
         * @param map  the component map to check content.
         * @param type the DataComponentType instance.
         * @return     true if holder contains the provided component type.
         */
        public static boolean has(Object map, Object type) {
            return get(map, type) != null;
        }

        /**
         * Get the map value from component map.
         *
         * @param map the map to get the value itself.
         * @return    a Reference2ObjectMap inside component map.
         */
        @SuppressWarnings("unchecked")
        public static Reference2ObjectMap<Object, Object> getValue(Object map) {
            try {
                return (Reference2ObjectMap<Object, Object>) GET_MAP.invoke(map);
            } catch (Throwable t) {
                throw new RuntimeException("Cannot get map field from component map", t);
            }
        }

        /**
         * Get a DataComponentType key set from component map.
         *
         * @param map the map to get the key set.
         * @return    set full of DataComponentType objects.
         */
        @SuppressWarnings("unchecked")
        public static Set<Object> keySet(Object map) {
            try {
                return (Set<Object>) KEY_SET.invoke(map);
            } catch (Throwable t) {
                throw new RuntimeException("Cannot get keySet from component map", t);
            }
        }
    }

    /**
     * Class to invoke methods from PatchedDataComponentMap.<br>
     * A patched map is a subclass of DataComponentMap but mutable, that means all the methods from component
     * map are applicable in a patched map.
     */
    @ApiStatus.Experimental
    public static class MapPatch {
        private static final MethodHandle SET_MAP_FIELD;
        private static final MethodHandle GET_MAP_FIELD;
        private static final MethodHandle SET;
        private static final MethodHandle REMOVE;

        static {
            // Getters
            MethodHandle get$map = null;
            // Setters
            MethodHandle set$map = null;
            // Methods
            MethodHandle method$set = null;
            MethodHandle method$remove = null;
            if (ServerInstance.Release.COMPONENT) {
                // Old names
                String map = "d";
                String set = "b";
                String remove = "d";

                // New names
                if (ServerInstance.Type.MOJANG_MAPPED) {
                    map = "patch";
                    set = "set";
                    remove = "remove";
                } else {
                    if (ServerInstance.VERSION >= 21.03f) { // 1.21.3
                        remove = "e";
                    }
                }

                try {
                    // Private field
                    get$map = EasyLookup.unreflectGetter(COMPONENT_MAP_PATCH, map);
                    set$map = EasyLookup.unreflectSetter(COMPONENT_MAP_PATCH, map);

                    method$set = EasyLookup.method(COMPONENT_MAP_PATCH, set, Object.class, "DataComponentType", Object.class);
                    method$remove = EasyLookup.method(COMPONENT_MAP_PATCH, remove, Object.class, "DataComponentType");
                } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }
            SET_MAP_FIELD = set$map;
            GET_MAP_FIELD = get$map;
            SET = method$set;
            REMOVE = method$remove;
        }

        MapPatch() {
        }

        /**
         * Get the map value from patched map.
         *
         * @param map the patched map to get the value itself.
         * @return    a Reference2ObjectMap inside patched map.
         */
        @SuppressWarnings("unchecked")
        public static Reference2ObjectMap<Object, Object> getValue(Object map) {
            try {
                return (Reference2ObjectMap<Object, Object>) GET_MAP_FIELD.invoke(map);
            } catch (Throwable t) {
                throw new RuntimeException("Cannot get map field from component map", t);
            }
        }

        /**
         * Set provided object type by a DataComponentType declaration of object itself into patched map.
         *
         * @param map   the patched map to put value into.
         * @param type  the DataComponentType instance.
         * @param value the object value declared by component type.
         * @return      the value that was set before in patched map, null otherwise.
         */
        public static Object set(Object map, Object type, Object value) {
            try {
                return SET.invoke(map, type, value);
            } catch (Throwable t) {
                throw new RuntimeException("Cannot set component type value to component map", t);
            }
        }

        /**
         * Replace the map value into patched map.
         *
         * @param map   the patched map to set the map value.
         * @param value a Reference2ObjectMap with DataComponentType as keys and declared objects has values.
         */
        public static void setValue(Object map, Reference2ObjectMap<Object, Object> value) {
            try {
                SET_MAP_FIELD.invoke(map, value);
            } catch (Throwable t) {
                throw new RuntimeException("Cannot set map field to component map", t);
            }
        }

        /**
         * Remove the provided DataComponentType mapping from patched map.
         *
         * @param map  the patched map to remove component type.
         * @param type the DataComponentType instance to remove.
         * @return     the value that was set before in patched map, null otherwise.
         */
        public static Object remove(Object map, Object type) {
            try {
                return REMOVE.invoke(map, type);
            } catch (Throwable t) {
                throw new RuntimeException("Cannot remove component type value from component map", t);
            }
        }
    }

    /**
     * Class to invoke methods from DataComponentPatch.<br>
     * Instead of patched map, a component patch acts like a cloneable object that can be
     * introduced into maps and also hold empty values to future deletion.
     */
    @ApiStatus.Experimental
    public static class Patch {

        /**
         * An empty DataComponentPatch instance.
         */
        public static final Object EMPTY;

        private static final MethodHandle SET_MAP_FIELD;
        private static final MethodHandle GET_MAP_FIELD;
        private static final MethodHandle BUILDER;
        private static final MethodHandle BUILDER_MAP;
        private static final MethodHandle BUILDER_BUILD;

        static {
            // Constants
            Object const$empty = null;
            // Getters
            MethodHandle get$map = null;
            MethodHandle get$builder$map = null;
            // Setters
            MethodHandle set$map = null;
            // Methods
            MethodHandle method$builder = null;
            MethodHandle method$builder$build = null;
            if (ServerInstance.Release.COMPONENT) {
                // Old names
                String empty = "a";
                String map = "d";

                String builder = "a";
                String builder$map = "a";
                String builder$build = "a";

                // New names
                if (ServerInstance.Type.MOJANG_MAPPED) {
                    empty = "EMPTY";
                    map = "map";

                    builder = "builder";
                    builder$map = "map";
                    builder$build = "build";
                } else {
                    if (ServerInstance.VERSION >= 21.04f) { // 1.21.5
                        map = "e";
                    }
                }

                try {
                    const$empty = COMPONENT_PATCH.getDeclaredField(empty).get(null);

                    // Private field
                    get$map = EasyLookup.unreflectGetter(COMPONENT_PATCH, map);
                    set$map = EasyLookup.unreflectSetter(COMPONENT_PATCH, map);
                    // Private field
                    get$builder$map = EasyLookup.unreflectGetter("DataComponentPatch.Builder", builder$map);

                    method$builder = EasyLookup.staticMethod(COMPONENT_PATCH, builder, "DataComponentPatch.Builder");
                    method$builder$build = EasyLookup.method("DataComponentPatch.Builder", builder$build, COMPONENT_PATCH);
                } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }
            EMPTY = const$empty;
            SET_MAP_FIELD = set$map;
            GET_MAP_FIELD = get$map;
            BUILDER = method$builder;
            BUILDER_MAP = get$builder$map;
            BUILDER_BUILD = method$builder$build;
        }

        Patch() {
        }

        /**
         * Create a DataComponentPatch builder to set values and then wrap into component patch.
         *
         * @return a newly generated builder to edit.
         */
        @SuppressWarnings("unchecked")
        public static Builder<Optional<?>> builder() {
            try {
                final Object build = BUILDER.invoke();
                return new Builder<>(build, (Reference2ObjectMap<Object, Optional<?>>) BUILDER_MAP.invoke(build)) {
                    @Override
                    public Builder<Optional<?>> remove(Object type) {
                        getMap().put(type, Optional.empty());
                        return this;
                    }

                    @Override
                    public Object build() {
                        try {
                            return BUILDER_BUILD.invoke(build);
                        } catch (Throwable t) {
                            throw new RuntimeException("Cannot build component patch from builder", t);
                        }
                    }
                };
            } catch (Throwable t) {
                throw new RuntimeException("Cannot create component patch builder", t);
            }
        }

        /**
         * Get a declared component type from provided component patch.
         *
         * @param patch the component patch to get component from.
         * @param type  the DataComponentType instance that declares a component type.
         * @return      the component declared type from data component cache wrapped into optional object.
         */
        @SuppressWarnings("unchecked")
        public static Optional<Object> get(Object patch, Object type) {
            return (Optional<Object>) getValue(patch).get(type);
        }

        /**
         * Get the map value from component patch.
         *
         * @param patch the patch to get the value itself.
         * @return      a Reference2ObjectMap inside component patch.
         */
        @SuppressWarnings("unchecked")
        public static Reference2ObjectMap<Object, Optional<?>> getValue(Object patch) {
            try {
                return (Reference2ObjectMap<Object, Optional<?>>) GET_MAP_FIELD.invoke(patch);
            } catch (Throwable t) {
                throw new RuntimeException("Cannot get map field from component patch", t);
            }
        }

        /**
         * Get a DataComponentType key set from component patch.
         *
         * @param patch the patch to get the key set.
         * @return      set full of DataComponentType objects.
         */
        public static Set<java.util.Map.Entry<Object, Optional<?>>> entrySet(Object patch) {
            return getValue(patch).entrySet();
        }

        /**
         * Get the size of provided component patch.
         *
         * @param patch the component patch to get size.
         * @return      the number of data type elements in provided component patch.
         */
        public static int size(Object patch) {
            return getValue(patch).size();
        }

        /**
         * Check if the provided component patch is empty.
         *
         * @param patch the component patch to check.
         * @return      true if the component patch has no elements.
         */
        public static boolean isEmpty(Object patch) {
            return getValue(patch).isEmpty();
        }

        /**
         * Replace the map value into component patch.
         *
         * @param patch the component patch to set the map value.
         * @param value a Reference2ObjectMap with DataComponentType as keys and wrapped declared objects has values.
         */
        public static void setValue(Object patch, Reference2ObjectMap<Object, Optional<?>> value) {
            try {
                SET_MAP_FIELD.invoke(patch, value);
            } catch (Throwable t) {
                throw new RuntimeException("Cannot set map field to component patch", t);
            }
        }
    }

    /**
     * Class wrapper to invoke methods from map and patch data component types builder.<br>
     * This is not the real class, it's just a bridge between to set and get values before build.
     */
    @ApiStatus.Experimental
    public static class Builder<V> {

        private final Object builder;
        private final Reference2ObjectMap<Object, V> map;

        /**
         * Constructs a builder with provided real builder, and it's map inside.
         *
         * @param builder the real data component builder.
         * @param map     the map inside provided builder.
         */
        public Builder(Object builder, Reference2ObjectMap<Object, V> map) {
            this.builder = builder;
            this.map = map;
        }

        /**
         * Return the real data component builder that is wrapped inside this class.
         *
         * @return a map or patch data component builder.
         */
        public Object getBuilder() {
            return builder;
        }

        /**
         * Return the map inside real component builder.
         *
         * @return a Reference2ObjectMap with DataComponentType as keys and declared objects type in this wrapper as values.
         */
        public Reference2ObjectMap<Object, V> getMap() {
            return map;
        }

        /**
         * Check if the current builder contains a DataComponentType.<br>
         * On a component patch builder this method may not work to check the availability of provided type
         * due the map can contain empty values.
         *
         * @param type the DataComponentType to check if exist inside.
         * @return     true if component type exists.
         */
        public boolean has(Object type) {
            return map.containsKey(type);
        }

        /**
         * Get a declared component type from builder.
         *
         * @param type the DataComponentType instance that declares a component type.
         * @return     the component declared type from data component cache as value type.
         */
        public V get(Object type) {
            return map.get(type);
        }

        /**
         * Set provided object type by a DataComponentType declaration of object itself as value type
         * into builder and return this instance.
         *
         * @param type  the DataComponentType instance.
         * @param value the object value declared by component type as value type.
         * @return      the builder itself.
         */
        @Contract("_, _ -> this")
        public Builder<V> set(Object type, V value) {
            map.put(type, value);
            return this;
        }

        /**
         * Remove the provided DataComponentType mapping from builder and return this instance.
         *
         * @param type the DataComponentType instance to remove.
         * @return     the builder itself.
         */
        @Contract("_ -> this")
        public Builder<V> remove(Object type) {
            map.remove(type);
            return this;
        }

        /**
         * Build the required instance of data component.<br>
         * By default, this method throws an exception, so should be overridden.
         *
         * @return an object instance that was building this builder.
         */
        public Object build() {
            throw new RuntimeException("Object build not supported");
        }
    }
}
