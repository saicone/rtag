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
    private static final MethodHandle setListField;
    private static final MethodHandle getListField;

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
            if (ServerInstance.isMojangMapped) {
                clone = "copy";
            } else if (ServerInstance.isUniversal) {
                list = "c";
                type = "w";
                if (ServerInstance.fullVersion >= 11903) {
                    clone = "e";
                } else if (ServerInstance.verNumber >= 18) {
                    clone = "d";
                }
            } else if (ServerInstance.verNumber >= 10 && ServerInstance.verNumber <= 13) {
                clone = "d";
            }

            new$EmptyList = EasyLookup.constructor(NBT_LIST);
            if (ServerInstance.verNumber >= 15) {
                // Private constructor
                new$List = EasyLookup.constructor(NBT_LIST, List.class, byte.class);
            }
            // (1.8 -  1.9) return NBTBase
            method$clone = EasyLookup.method(NBT_LIST, clone, NBT_LIST);
            // Private fields
            get$type = EasyLookup.getter(NBT_LIST, type, byte.class);
            set$type = EasyLookup.setter(NBT_LIST, type, byte.class);
            get$list = EasyLookup.getter(NBT_LIST, list, List.class);
            set$list = EasyLookup.setter(NBT_LIST, list, List.class);
        } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        newEmpty = new$EmptyList;
        newList = new$List;
        clone = method$clone;
        setTypeField = set$type;
        getTypeField = get$type;
        setListField = set$list;
        getListField = get$list;
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
     * @param list List with NBTBase values.
     * @return     New NBTTagList instance.
     * @param <T>  List type parameter.
     */
    public static <T> Object newTag(List<T> list) {
        if (list == null || list.isEmpty()) {
            return newTag();
        }
        final byte type = TagBase.getTypeId(list.get(0));
        try {
            if (ServerInstance.verNumber >= 15) {
                return newList.invoke(list, type);
            } else {
                Object tag = newTag();
                setTypeField.invoke(tag, type);
                setListField.invoke(tag, list);
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
        return newTag(tags);
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
        return newTag(listCopy);
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
     * Get current tag type that reprsent tag list.
     *
     * @param tag NBTTagList instance.
     * @return    A NBTBase object type.
     *
     * @see TagBase#getTypeId(Object)
     */
    public static byte getType(Object tag) {
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
        final byte listType = getType(listTag);
        if (listType == 0) {
            setType(listTag, TagBase.getTypeId(tag));
        } else if (!TagBase.isTypeOf(tag, listType)) {
            return;
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
        final byte listType = getType(listTag);
        if (listType == 0) {
            setType(listTag, TagBase.getTypeId(tags[0]));
        } else if (!TagBase.isTypeOf(tags[0], getType(listTag))) {
            return;
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
    public static void setType(Object tag, byte type) {
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
            final byte type = TagBase.getTypeId(list.get(0));
            try {
                setTypeField.invoke(tag, type);
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
