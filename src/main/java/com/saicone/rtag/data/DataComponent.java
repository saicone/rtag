package com.saicone.rtag.data;

import com.saicone.rtag.util.MC;
import com.saicone.rtag.util.reflect.Lookup;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.Set;

/**
 * Class to invoke methods from multiple DataComponent classes.
 *
 * @author Rubenicos
 */
@ApiStatus.Experimental
public class DataComponent {

    // import
    private static final Lookup.AClass<?> DataComponentGetter = Lookup.SERVER.importClass("net.minecraft.core.component.DataComponentGetter");
    private static final Lookup.AClass<?> DataComponentHolder = Lookup.SERVER.importClass("net.minecraft.core.component.DataComponentHolder");
    private static final Lookup.AClass<?> DataComponentMap = Lookup.SERVER.importClass("net.minecraft.core.component.DataComponentMap");
    private static final Lookup.AClass<?> DataComponentMap$Builder = Lookup.SERVER.importClass("net.minecraft.core.component.DataComponentMap$Builder");
    private static final Lookup.AClass<?> DataComponentMap$Builder$SimpleMap = Lookup.SERVER.importClass("net.minecraft.core.component.DataComponentMap$Builder$SimpleMap");
    private static final Lookup.AClass<?> DataComponentPatch = Lookup.SERVER.importClass("net.minecraft.core.component.DataComponentPatch");
    private static final Lookup.AClass<?> DataComponentPatch$Builder = Lookup.SERVER.importClass("net.minecraft.core.component.DataComponentPatch$Builder");
    private static final Lookup.AClass<?> DataComponentType = Lookup.SERVER.importClass("net.minecraft.core.component.DataComponentType");
    private static final Lookup.AClass<?> PatchedDataComponentMap = Lookup.SERVER.importClass("net.minecraft.core.component.PatchedDataComponentMap");

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
        if (DataComponentHolder.isInstance(component)) {
            return Holder.get(component, type);
        } else if (DataComponentMap.isInstance(component)) {
            return Map.get(component, type);
        } else if (DataComponentPatch.isInstance(component)) {
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
        if (DataComponentHolder.isInstance(component)) {
            return Optional.ofNullable(Holder.get(component, type));
        } else if (DataComponentMap.isInstance(component)) {
            return Optional.ofNullable(Map.get(component, type));
        } else if (DataComponentPatch.isInstance(component)) {
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

        // lock
        static {
            Lookup.SERVER.require(MC.version().isComponent());
        }

        // declare
        private static final MethodHandle DataComponentHolder_getComponents = DataComponentHolder.method(DataComponentMap, "getComponents").handle();
        private static final MethodHandle DataComponentHolder_get = DataComponentHolder.method(Object.class, "get", DataComponentType).handle();
        private static final MethodHandle DataComponentHolder_has = DataComponentHolder.method(boolean.class, "has", DataComponentType).handle();

        Holder() {
        }

        /**
         * Get DataComponentMap from provided holder.
         *
         * @param holder the component holder to get map from.
         * @return       a DataComponentMap provided by holder.
         */
        public static Object getComponents(Object holder) {
            return Lookup.invoke(DataComponentHolder_getComponents, holder);
        }

        /**
         * Get a declared component type from provided holder.
         *
         * @param holder the component holder to get component from.
         * @param type   the DataComponentType instance that declares a component type.
         * @return       the component declared type from data component cache if exists, null otherwise.
         */
        public static Object get(Object holder, Object type) {
            return Lookup.invoke(DataComponentHolder_get, holder, type);
        }

        /**
         * Check if the provided holder has any type of DataComponentType inside.
         *
         * @param holder the component holder to check content.
         * @param type   the DataComponentType instance.
         * @return       true if holder contains the provided component type.
         */
        public static boolean has(Object holder, Object type) {
            return Lookup.invoke(DataComponentHolder_has, holder, type);
        }
    }

    /**
     * Class to invoke methods from DataComponentMap.<br>
     * A component map acts like an immutable map in java with the regular conception of components by Mojang.
     */
    @ApiStatus.Experimental
    public static class Map {

        // lock
        static {
            Lookup.SERVER.require(MC.version().isComponent());
        }

        /**
         * An empty DataComponentMap instance.
         */
        public static final Object EMPTY = DataComponentMap.field(Modifier.STATIC, DataComponentMap, "EMPTY").getValue();

        // declare
        private static final MethodHandle DataComponentGetter_get;
        static {
            if (MC.version().isNewerThanOrEquals(MC.V_1_21_5)) {
                DataComponentGetter_get = DataComponentGetter.method(Object.class, "get", DataComponentType).handle();
            } else {
                DataComponentGetter_get = DataComponentMap.method(Object.class, "get", DataComponentType).handle();
            }
        }

        private static final MethodHandle DataComponentMap_keySet = DataComponentMap.method(Set.class, "keySet").handle();
        private static final MethodHandle DataComponentMap_builder = DataComponentMap.method(Modifier.STATIC, DataComponentMap$Builder, "builder").handle();

        private static final MethodHandle DataComponentMap$Builder$get_map = DataComponentMap$Builder.field(Reference2ObjectMap.class, "map").getter();
        private static final MethodHandle DataComponentMap$Builder_build = DataComponentMap$Builder.method(DataComponentMap, "build").handle();

        private static final MethodHandle DataComponentMap$Builder$SimpleMap$get_map = DataComponentMap$Builder$SimpleMap.field(Reference2ObjectMap.class, "map").getter();

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
                final Object build = DataComponentMap_builder.invoke();
                return new Builder<>(build, (Reference2ObjectMap<Object, Object>) DataComponentMap$Builder$get_map.invoke(build)) {
                    @Override
                    public Object build() {
                        return Lookup.invoke(DataComponentMap$Builder_build, build);
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
            return Lookup.invoke(DataComponentGetter_get, map, type);
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
        public static Reference2ObjectMap<Object, Object> getValue(Object map) {
            return Lookup.invoke(DataComponentMap$Builder$SimpleMap$get_map, map);
        }

        /**
         * Get a DataComponentType key set from component map.
         *
         * @param map the map to get the key set.
         * @return    set full of DataComponentType objects.
         */
        public static Set<Object> keySet(Object map) {
            return Lookup.invoke(DataComponentMap_keySet, map);
        }
    }

    /**
     * Class to invoke methods from PatchedDataComponentMap.<br>
     * A patched map is a subclass of DataComponentMap but mutable, that means all the methods from component
     * map are applicable in a patched map.
     */
    @ApiStatus.Experimental
    public static class MapPatch {

        // lock
        static {
            Lookup.SERVER.require(MC.version().isComponent());
        }

        // declare
        private static final MethodHandle PatchedDataComponentMap$get_patch = PatchedDataComponentMap.field(Reference2ObjectMap.class, "patch").getter();
        private static final MethodHandle PatchedDataComponentMap$set_patch = PatchedDataComponentMap.field(Reference2ObjectMap.class, "patch").setter();
        private static final MethodHandle PatchedDataComponentMap_set = PatchedDataComponentMap.method(Object.class, "set", DataComponentType, Object.class).handle();
        private static final MethodHandle PatchedDataComponentMap_remove = PatchedDataComponentMap.method(Object.class, "remove", DataComponentType).handle();

        MapPatch() {
        }

        /**
         * Get the map value from patched map.
         *
         * @param map the patched map to get the value itself.
         * @return    a Reference2ObjectMap inside patched map.
         */
        public static Reference2ObjectMap<Object, Object> getValue(Object map) {
            return Lookup.invoke(PatchedDataComponentMap$get_patch, map);
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
            return Lookup.invoke(PatchedDataComponentMap_set, map, type, value);
        }

        /**
         * Replace the map value into patched map.
         *
         * @param map   the patched map to set the map value.
         * @param value a Reference2ObjectMap with DataComponentType as keys and declared objects has values.
         */
        public static void setValue(Object map, Reference2ObjectMap<Object, Object> value) {
            Lookup.invoke(PatchedDataComponentMap$set_patch, map, value);
        }

        /**
         * Remove the provided DataComponentType mapping from patched map.
         *
         * @param map  the patched map to remove component type.
         * @param type the DataComponentType instance to remove.
         * @return     the value that was set before in patched map, null otherwise.
         */
        public static Object remove(Object map, Object type) {
            return Lookup.invoke(PatchedDataComponentMap_remove, map, type);
        }
    }

    /**
     * Class to invoke methods from DataComponentPatch.<br>
     * Instead of patched map, a component patch acts like a cloneable object that can be
     * introduced into maps and also hold empty values to future deletion.
     */
    @ApiStatus.Experimental
    public static class Patch {

        // lock
        static {
            Lookup.SERVER.require(MC.version().isComponent());
        }

        /**
         * An empty DataComponentPatch instance.
         */
        public static final Object EMPTY = DataComponentPatch.field(Modifier.STATIC, DataComponentPatch, "EMPTY").getValue();

        // declare
        private static final MethodHandle DataComponentPatch$get_map = DataComponentPatch.field(Reference2ObjectMap.class, "map").getter();
        private static final MethodHandle DataComponentPatch$set_map = DataComponentPatch.field(Reference2ObjectMap.class, "map").setter();
        private static final MethodHandle DataComponentPatch_builder = DataComponentPatch.method(Modifier.STATIC, DataComponentPatch$Builder, "builder").handle();

        private static final MethodHandle DataComponentPatch$Builder$get_map = DataComponentPatch$Builder.field(Reference2ObjectMap.class, "map").getter();
        private static final MethodHandle DataComponentPatch$Builder_build = DataComponentPatch$Builder.method(DataComponentPatch, "build").handle();

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
                final Object build = DataComponentPatch_builder.invoke();
                return new Builder<>(build, (Reference2ObjectMap<Object, Optional<?>>) DataComponentPatch$Builder$get_map.invoke(build)) {
                    @Override
                    public Builder<Optional<?>> remove(Object type) {
                        getMap().put(type, Optional.empty());
                        return this;
                    }

                    @Override
                    public Object build() {
                        return Lookup.invoke(DataComponentPatch$Builder_build, build);
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
        public static Reference2ObjectMap<Object, Optional<?>> getValue(Object patch) {
            return Lookup.invoke(DataComponentPatch$get_map, patch);
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
            Lookup.invoke(DataComponentPatch$set_map, patch, value);
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
