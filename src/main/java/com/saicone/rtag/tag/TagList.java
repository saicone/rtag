package com.saicone.rtag.tag;

import com.saicone.rtag.RtagMirror;
import com.saicone.rtag.util.EasyLookup;
import com.saicone.rtag.util.ServerInstance;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * Class to invoke NBTTagList methods across versions.
 *
 * @author Rubenicos
 */
public class TagList {

    private static final Class<?> NBT_LIST = EasyLookup.classById("NBTTagList");

    private static final MethodHandle newEmpty;
    private static final MethodHandle newList;
    private static final MethodHandle clone;
    private static final MethodHandle getTypeField;
    private static final MethodHandle setTypeField;
    private static final MethodHandle getListField;
    private static final MethodHandle setListField;

    static {
        // Constructors
        MethodHandle new$EmptyList = null;
        MethodHandle new$List = null;
        // Methods
        MethodHandle method$clone = null;
        // Getters
        MethodHandle get$type = null;
        MethodHandle get$list = null;
        // Setters
        MethodHandle set$type = null;
        MethodHandle set$list = null;
        try {
            // Old names
            String clone = "clone";
            String type = "type";
            String list = "list";
            // New names
            if (ServerInstance.Type.MOJANG_MAPPED) {
                clone = "copy";
            } else {
                if (ServerInstance.MAJOR_VERSION >= 10 && ServerInstance.MAJOR_VERSION <= 13) {
                    clone = "d";
                }
                if (ServerInstance.Release.UNIVERSAL) {
                    type = "w";
                    list = "c";
                }
                if (ServerInstance.MAJOR_VERSION >= 18) {
                    clone = "d";
                }
                if (ServerInstance.VERSION >= 19.03) { // 1.19.4
                    clone = "e";
                }
                if (ServerInstance.VERSION >= 21.04) { // 1.21.5
                    list = "v";
                    clone = "g";
                }
            }

            new$EmptyList = EasyLookup.constructor(NBT_LIST);
            // Private constructor
            if (ServerInstance.VERSION >= 21.04) {
                new$List = EasyLookup.constructor(NBT_LIST, List.class);
            } else if (ServerInstance.MAJOR_VERSION >= 15) {
                new$List = EasyLookup.constructor(NBT_LIST, List.class, byte.class);
            }
            // (1.8 -  1.9) return NBTBase
            method$clone = EasyLookup.method(NBT_LIST, clone, NBT_LIST);
            // Private fields
            if (ServerInstance.VERSION < 21.04) {
                get$type = EasyLookup.getter(NBT_LIST, type, byte.class);
                set$type = EasyLookup.setter(NBT_LIST, type, byte.class);
            }
            get$list = EasyLookup.getter(NBT_LIST, list, List.class);
            set$list = EasyLookup.setter(NBT_LIST, list, List.class);
        } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        newEmpty = new$EmptyList;
        newList = new$List;
        clone = method$clone;
        getTypeField = get$type;
        setTypeField = set$type;
        getListField = get$list;
        setListField = set$list;
    }

    TagList() {
    }

    /**
     * Constructs an empty NBTTagList.
     *
     * @return New NBTTagList instance.
     */
    public static Object newTag() {
        try {
            return newEmpty.invoke();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Constructs an NBTTagList with provided List of NBTBase.
     *
     * @param list List with NBTBase values.
     * @return     New NBTTagList instance.
     * @param <T>  List type parameter.
     */
    public static <T> Object newTag(List<T> list) {
        if (list == null || list.isEmpty()) {
            return newTag();
        }

        // Check if list is mutable
        try {
            list.addAll(List.of());
        } catch (UnsupportedOperationException e) {
            return newTag(new ArrayList<>(list));
        }

        return newUncheckedTag(list);
    }

    /**
     * Constructs an NBTTagList with provided List of NBTBase.<br>
     * This method doesn't provide any safe check and assumes that the provided
     * list is completely usable to create a new NBTTagList.
     *
     * @param list List with NBTBase values.
     * @return     New NBTTagList instance.
     * @param <T>  List type parameter.
     */
    public static <T> Object newUncheckedTag(List<T> list) {
        try {
            // List are heterogeneous since 1.21.5
            if (ServerInstance.VERSION >= 21.04) {
                return newList.invoke(list);
            }
            final byte type = TagBase.getTypeId(list.get(0));
            if (ServerInstance.MAJOR_VERSION >= 15) {
                return newList.invoke(list, type);
            } else {
                Object tag = newTag();
                setTypeField.invoke(tag, type);
                try {
                    setListField.invoke(tag, list);
                } catch (ClassCastException e) {
                    getValue(tag).addAll(list);
                }
                return tag;
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Constructs an NBTTagList with provided List of any type
     * and required {@link RtagMirror} to convert Objects.
     *
     * @param mirror RtagMirror to convert objects into tags.
     * @param list   List with objects.
     * @return       New NBTTagList instance.
     * @param <T>    List type parameter.
     */
    public static <T> Object newTag(RtagMirror mirror, List<T> list) {
        if (list == null || list.isEmpty()) {
            return newTag();
        }
        final List<Object> tags = new ArrayList<>();
        for (Object value : list) {
            tags.add(mirror.newTag(value));
        }
        return newUncheckedTag(tags);
    }

    /**
     * Check if the provided object is instance of NBTTagList class.
     *
     * @param object the object to check.
     * @return       true if the object is an instance of NBTTagList class.
     */
    public static boolean isTagList(Object object) {
        return NBT_LIST.isInstance(object);
    }

    /**
     * Copy provided NBTTagList into new one.
     *
     * @param tag NBTTagList instance.
     * @return    A copy of original NBTTagList.
     */
    public static Object clone(Object tag) {
        try {
            return clone.invoke(tag);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Copy provided NBTTagList into new one using a list filter.
     *
     * @param tag    NBTTagList instance.
     * @param filter Object filter.
     * @return       A filtered copy of original NBTTagList.
     */
    public static Object clone(Object tag, BiPredicate<Object, Integer> filter) {
        final List<Object> list = getValue(tag);
        if (list.isEmpty()) {
            return newTag();
        }
        final List<Object> listCopy = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            final Object o = list.get(i);
            if (filter.test(o, i)) {
                listCopy.add(o);
            }
        }
        return newUncheckedTag(listCopy);
    }

    /**
     * Get current tag list.
     *
     * @param tag NBTTagList instance.
     * @return    A list of NBTBase objects.
     */
    @SuppressWarnings("unchecked")
    public static List<Object> getValue(Object tag) {
        try {
            return (List<Object>) getListField.invoke(tag);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Get current tag list with values converted.
     *
     * @see #getValue(Object)
     *
     * @param mirror RtagMirror to convert tags.
     * @param tag    NBTTagList instance.
     * @return       A list of objects.
     */
    public static List<Object> getValue(RtagMirror mirror, Object tag) {
        List<Object> list = new ArrayList<>();
        if (tag != null) {
            for (Object object : getValue(tag)) {
                list.add(mirror.getTagValue(object));
            }
        }
        return list;
    }

    /**
     * Get current tag type that represent tag list.
     *
     * @param tag NBTTagList instance.
     * @return    A NBTBase object type.
     *
     * @see TagBase#getTypeId(Object)
     */
    @Deprecated
    public static byte getType(Object tag) {
        // List are heterogeneous since 1.21.5
        if (ServerInstance.VERSION >= 21.04) {
            for (Object o : getValue(tag)) {
                return TagBase.getTypeId(o);
            }
            return 0;
        } else {
            return getType0(tag);
        }
    }

    private static byte getType0(Object tag) {
        try {
            return (byte) getTypeField.invoke(tag);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Get the size of elements inside list.
     *
     * @param tag NBTTagList instance.
     * @return    Size of list inside.
     */
    public static int size(Object tag) {
        return getValue(tag).size();
    }

    /**
     * Check if current NBTTagList value contains NBTBase object.
     *
     * @param listTag NBTTagList instance.
     * @param tag     NBTBase object.
     * @return        true if the list contains the NBTBase object.
     */
    public static boolean contains(Object listTag, Object tag) {
        return getValue(listTag).contains(tag);
    }

    /**
     * Add NBTBase tag.
     *
     * @param listTag NBTTagList instance.
     * @param tag     NBTBase tag to add.
     */
    public static void add(Object listTag, Object tag) {
        // List are heterogeneous since 1.21.5
        if (ServerInstance.VERSION < 21.04) {
            final byte listType = getType0(listTag);
            if (listType == 0) {
                setType0(listTag, TagBase.getTypeId(tag));
            } else if (!TagBase.isTypeOf(tag, listType)) {
                return;
            }
        }
        getValue(listTag).add(tag);
    }

    /**
     * Add multiple NBTBase tags.
     *
     * @param listTag NBTTagList instance.
     * @param tags    NBTBase tags to add.
     */
    public static void add(Object listTag, Object... tags) {
        // List are heterogeneous since 1.21.5
        if (ServerInstance.VERSION < 21.04) {
            final byte listType = getType0(listTag);
            if (listType == 0) {
                setType0(listTag, TagBase.getTypeId(tags[0]));
            } else if (!TagBase.isTypeOf(tags[0], getType0(listTag))) {
                return;
            }
        }
        final List<Object> list = getValue(listTag);
        for (Object tag : tags) {
            list.add(tag);
        }
    }

    /**
     * Remove NBTBase tag.
     *
     * @param tag   NBTTagList instance.
     * @param index The index of the tag to be removed
     * @return      Removed tag.
     */
    public static Object remove(Object tag, int index) {
        return getValue(tag).remove(index);
    }

    /**
     * Set NBTBase tag at index.
     *
     * @param listTag NBTTagList instance.
     * @param index   Index of element to replace.
     * @param tag     Tag value to set.
     */
    public static void set(Object listTag, int index, Object tag) {
        getValue(listTag).set(index, tag);
    }

    /**
     * Change the current NBTBase type inside NBTTagList.
     *
     * @param tag  NBTTagList instance.
     * @param type NBTBase object type.
     *
     * @see TagBase#getTypeId(Object)
     */
    @Deprecated
    public static void setType(Object tag, byte type) {
        // List are heterogeneous since 1.21.5
        if (ServerInstance.VERSION >= 21.04) {
            return;
        }
        setType0(tag, type);
    }

    private static void setType0(Object tag, byte type) {
        try {
            setTypeField.invoke(tag, type);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Override the current NBTBase list inside NBTTagList.
     *
     * @param tag  NBTTagList instance.
     * @param list List with NBTBase values.
     */
    public static void setValue(Object tag, List<Object> list) {
        if (list.isEmpty()) {
            clear(tag);
        } else {
            try {
                if (ServerInstance.VERSION < 21.04) {
                    final byte type = TagBase.getTypeId(list.get(0));
                    setTypeField.invoke(tag, type);
                }
                setListField.invoke(tag, list);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    /**
     * Get NBTBase tag from index.
     *
     * @param tag   NBTTagList instance.
     * @param index Index of the tag to return.
     * @return      A NBTBase instance.
     */
    public static Object get(Object tag, int index) {
        final List<Object> value = getValue(tag);
        return value.get(index >= 0 ? index : value.size() + index);
    }

    /**
     * Clear a NBTTagList and reset list type.
     *
     * @param tag NBTTagList instance.
     */
    public static void clear(Object tag) {
        getValue(tag).clear();
        setType(tag, (byte) 0);
    }
}
