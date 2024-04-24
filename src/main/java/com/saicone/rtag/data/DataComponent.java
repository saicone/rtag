package com.saicone.rtag.data;

import com.saicone.rtag.util.EasyLookup;
import com.saicone.rtag.util.ServerInstance;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.lang.invoke.MethodHandle;
import java.util.Optional;
import java.util.Set;

@ApiStatus.Experimental
@SuppressWarnings("javadoc")
public class DataComponent {

    private static final Class<?> COMPONENT_HOLDER = EasyLookup.classById("DataComponentHolder");
    private static final Class<?> COMPONENT_MAP = EasyLookup.classById("DataComponentMap");
    private static final Class<?> COMPONENT_MAP_PATCH = EasyLookup.classById("PatchedDataComponentMap");
    private static final Class<?> COMPONENT_PATCH = EasyLookup.classById("DataComponentPatch");

    DataComponent() {
    }

    public static Object get(Object component, Object type) {
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

    public static Optional<Object> getOptional(Object component, Object type) {
        if (COMPONENT_HOLDER.isInstance(component)) {
            return Optional.of(Holder.get(component, type));
        } else if (COMPONENT_MAP.isInstance(component)) {
            return Optional.of(Map.get(component, type));
        } else if (COMPONENT_PATCH.isInstance(component)) {
            return Patch.get(component, type);
        } else {
            throw new IllegalArgumentException("The object type " + component.getClass().getName() + " is not supported");
        }
    }

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

        public static Object getComponents(Object holder) {
            try {
                return GET_COMPONENTS.invoke(holder);
            } catch (Throwable t) {
                throw new RuntimeException("Cannot get component map from component holder", t);
            }
        }

        public static Object get(Object holder, Object type) {
            try {
                return GET.invoke(holder, type);
            } catch (Throwable t) {
                throw new RuntimeException("Cannot get component type from component holder", t);
            }
        }

        public static boolean has(Object holder, Object type) {
            try {
                return (boolean) HAS.invoke(holder, type);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    @ApiStatus.Experimental
    public static class Map {

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

                    method$get = EasyLookup.method(COMPONENT_MAP, get, Object.class, "DataComponentType");
                    method$keySet = EasyLookup.method(COMPONENT_MAP, keySet, Set.class);
                    method$builder = EasyLookup.staticMethod(COMPONENT_MAP, builder, "DataComponentMap.Builder");
                    method$builder$build = EasyLookup.method("DataComponentMap.Builder", builder$build, COMPONENT_MAP);
                } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
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

        public static int size(Object map) {
            return keySet(map).size();
        }

        public static boolean isEmpty(Object map) {
            return keySet(map).isEmpty();
        }

        public static Object get(Object map, Object type) {
            try {
                return GET.invoke(map, type);
            } catch (Throwable t) {
                throw new RuntimeException("Cannot get component type from component map", t);
            }
        }

        public static boolean has(Object map, Object type) {
            return get(map, type) != null;
        }

        @SuppressWarnings("unchecked")
        public static Reference2ObjectMap<Object, Object> getValue(Object map) {
            try {
                return (Reference2ObjectMap<Object, Object>) GET_MAP.invoke(map);
            } catch (Throwable t) {
                throw new RuntimeException("Cannot get map field from component map", t);
            }
        }

        @SuppressWarnings("unchecked")
        public static Set<Object> keySet(Object map) {
            try {
                return (Set<Object>) KEY_SET.invoke(map);
            } catch (Throwable t) {
                throw new RuntimeException("Cannot get keySet from component map", t);
            }
        }
    }

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

        @SuppressWarnings("unchecked")
        public static Reference2ObjectMap<Object, Object> getValue(Object map) {
            try {
                return (Reference2ObjectMap<Object, Object>) GET_MAP_FIELD.invoke(map);
            } catch (Throwable t) {
                throw new RuntimeException("Cannot get map field from component map", t);
            }
        }

        public static Object set(Object map, Object type, Object value) {
            try {
                return SET.invoke(map, type, value);
            } catch (Throwable t) {
                throw new RuntimeException("Cannot set component type value to component map", t);
            }
        }

        public static void setValue(Object map, Reference2ObjectMap<Object, Object> value) {
            try {
                SET_MAP_FIELD.invoke(map, value);
            } catch (Throwable t) {
                throw new RuntimeException("Cannot set map field to component map", t);
            }
        }

        public static Object remove(Object map, Object type) {
            try {
                return REMOVE.invoke(map, type);
            } catch (Throwable t) {
                throw new RuntimeException("Cannot remove component type value from component map", t);
            }
        }
    }

    @ApiStatus.Experimental
    public static class Patch {

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

        @SuppressWarnings("unchecked")
        public static Optional<Object> get(Object patch, Object type) {
            return (Optional<Object>) getValue(patch).get(type);
        }

        @SuppressWarnings("unchecked")
        public static Reference2ObjectMap<Object, Optional<?>> getValue(Object patch) {
            try {
                return (Reference2ObjectMap<Object, Optional<?>>) GET_MAP_FIELD.invoke(patch);
            } catch (Throwable t) {
                throw new RuntimeException("Cannot get map field from component patch", t);
            }
        }

        public static Set<java.util.Map.Entry<Object, Optional<?>>> entrySet(Object patch) {
            return getValue(patch).entrySet();
        }

        public static int size(Object patch) {
            return getValue(patch).size();
        }

        public static boolean isEmpty(Object patch) {
            return getValue(patch).isEmpty();
        }

        public static void setValue(Object patch, Reference2ObjectMap<Object, Optional<?>> value) {
            try {
                SET_MAP_FIELD.invoke(patch, value);
            } catch (Throwable t) {
                throw new RuntimeException("Cannot set map field to component patch", t);
            }
        }
    }

    @ApiStatus.Experimental
    public static class Builder<V> {

        private final Object builder;
        private final Reference2ObjectMap<Object, V> map;

        public Builder(Object builder, Reference2ObjectMap<Object, V> map) {
            this.builder = builder;
            this.map = map;
        }

        public Object getBuilder() {
            return builder;
        }

        public Reference2ObjectMap<Object, V> getMap() {
            return map;
        }

        public boolean has(Object type) {
            return map.containsKey(type);
        }

        public V get(Object type) {
            return map.get(type);
        }

        @Contract("_, _ -> this")
        public Builder<V> set(Object type, V value) {
            map.put(type, value);
            return this;
        }

        @Contract("_ -> this")
        public Builder<V> remove(Object type) {
            map.remove(type);
            return this;
        }

        public Object build() {
            throw new RuntimeException("Object build not supported");
        }
    }
}
