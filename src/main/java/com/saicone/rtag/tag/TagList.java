package com.saicone.rtag.tag;

import com.saicone.rtag.Rtag;
import com.saicone.rtag.util.EasyLookup;
import com.saicone.rtag.util.ServerInstance;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to invoke NBTTagList methods across versions.
 *
 * @author Rubenicos
 */
public class TagList {

    private static final Class<?> nbtList = EasyLookup.classById("NBTTagList");

    private static final MethodHandle newEmpty;
    private static final MethodHandle newList;
    private static final MethodHandle size;
    private static final MethodHandle add;
    private static final MethodHandle remove;
    private static final MethodHandle set;
    private static final MethodHandle get;
    private static final MethodHandle getTypeId;
    private static final MethodHandle isTypeId;
    private static final MethodHandle typeField;
    private static final MethodHandle setListField;
    private static final MethodHandle getListField;

    static {
        MethodHandle m1 = null, m2 = null, m3 = null, m4 = null, m5 = null, m6 = null, m7 = null, m8 = null, m9 = null, m10 = null, m11 = null, m12 = null;
        try {
            Class<?> base = EasyLookup.classById("NBTBase");
            // Old names
            String size = "size", add = "add", remove = "a", set = "a", get = "g", getTypeId = "getTypeId", list = "list";
            // New names
            if (ServerInstance.verNumber >= 18) {
                add = "c";
                remove = "c";
                set = "d";
                get = "k";
                getTypeId = "a";
            } else if (ServerInstance.verNumber >= 9) {
                remove = "remove";
                if (ServerInstance.verNumber >= 13) {
                    set = "set";
                    get = "get";
                } else if (ServerInstance.verNumber >= 12) {
                    get = "i";
                } else {
                    get = "h";
                }
            }
            if (ServerInstance.isUniversal) {
                list = "c";
            }

            m1 = EasyLookup.constructor(nbtList);
            if (ServerInstance.verNumber >= 15) {
                // Private constructor
                m2 = EasyLookup.unreflectConstructor(nbtList, List.class, byte.class);
            } else {
                // Private fields
                m10 = EasyLookup.unreflectSetter(nbtList, "type");
                m11 = EasyLookup.unreflectSetter(nbtList, list);
            }
            m3 = EasyLookup.method(nbtList, size, int.class);
            if (ServerInstance.verNumber >= 14) {
                m4 = EasyLookup.method(nbtList, add, void.class, int.class, base);
                // Private method
                m9 = EasyLookup.unreflectMethod(nbtList, "a", base);
            } else {
                // Unreflect reason:
                // (1.12 - 1.13) return boolean
                // Void method in other versions
                m4 = EasyLookup.unreflectMethod(nbtList, add, base);
            }
            m5 = EasyLookup.method(nbtList, remove, base, int.class);
            // Unreflect reason:
            // (1.8 -  1.12) void method
            // Other versions return NBTBase
            m6 = EasyLookup.unreflectMethod(nbtList, set, int.class, base);
            m7 = EasyLookup.method(nbtList, get, base, int.class);
            m8 = EasyLookup.method(base, getTypeId, byte.class);
            // Private field
            m12 = EasyLookup.unreflectGetter(nbtList, list);
        } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        newEmpty = m1;
        newList = m2;
        size = m3;
        add = m4;
        remove = m5;
        set = m6;
        get = m7;
        getTypeId = m8;
        isTypeId = m9;
        typeField = m10;
        setListField = m11;
        getListField = m12;
    }

    TagList() {
    }

    /**
     * Constructs an empty NBTTagList.
     *
     * @return New NBTTagList instance.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static Object newTag() throws Throwable {
        return newEmpty.invoke();
    }

    /**
     * Constructs and NBTTagList with provided List of NBTBase.
     *
     * @param list List with NBTBase values.
     * @return     New NBTTagList instance.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static Object newTag(List<Object> list) throws Throwable {
        if (list.isEmpty()) return newEmpty.invoke();
        byte type = (byte) getTypeId.invoke(list.get(0));
        if (ServerInstance.verNumber >= 15) {
            return newList.invoke(list, type);
        } else {
            Object tag = newTag();
            typeField.invoke(tag, type);
            setListField.invoke(tag, list);
            return tag;
        }
    }

    /**
     * Constructs and NBTTagList with provided List of NBTBase
     * and required {@link Rtag} to convert Objects.
     *
     * @param rtag Rtag parent to convert objects into tags.
     * @param list List with objects.
     * @return     New NBTTagList instance.
     */
    public static Object newTag(Rtag rtag, List<Object> list) {
        Object finalObject = null;
        try {
            if (list.isEmpty()) {
                finalObject = newEmpty.invoke();
            } else {
                List<Object> tags = new ArrayList<>();
                for (Object value : list) {
                    tags.add(rtag.toTag(value));
                }
                finalObject = newTag(tags);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return finalObject;
    }

    /**
     * Get current tag list.
     *
     * @param rtag Rtag parent to convert tags.
     * @param tag  NBTTagList instance.
     * @return     A list of objects.
     */
    public static List<Object> getValue(Rtag rtag, Object tag) {
        List<Object> list = new ArrayList<>();
        if (tag != null) {
            try {
                int length = (int) size.invoke(tag);
                for (int i = 0; i < length; i++) {
                    list.add(rtag.fromTagExact(get.invoke(tag, i)));
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return list;
    }

    /**
     * Get the size of elements inside list.
     *
     * @param tag NBTTagList instance.
     * @return    Size of list inside.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static int size(Object tag) throws Throwable {
        return (int) size.invoke(tag);
    }

    /**
     * Add NBTBase tag.
     *
     * @param listTag NBTTagList instance.
     * @param tag     NBTBase tag to add.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    @SuppressWarnings("unchecked")
    public static void add(Object listTag, Object tag) throws Throwable {
        if (ServerInstance.verNumber >= 14) {
            if ((boolean) isTypeId.invoke(listTag, tag)) {
                ((List<Object>) getListField.invoke(listTag)).add(tag);
            }
        } else {
            add.invoke(listTag, tag);
        }
    }

    /**
     * Add multiple NBTBase tags.
     *
     * @param listTag NBTTagList instance.
     * @param tags    NBTBase tags to add.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static void add(Object listTag, Object... tags) throws Throwable {
        for (Object tag : tags) {
            add(listTag, tag);
        }
    }

    /**
     * Remove NBTBase tag.
     *
     * @param tag   NBTTagList instance.
     * @param index The index of the tag to be removed
     * @return      Removed tag.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static Object remove(Object tag, int index) throws Throwable {
        return remove.invoke(tag, index);
    }

    /**
     * Set NBTBase tag at index.
     *
     * @param listTag NBTTagList instance.
     * @param index   Index of element to replace.
     * @param tag     Tag value to set.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static void set(Object listTag, int index, Object tag) throws Throwable {
        set.invoke(listTag, index, tag);
    }

    /**
     * Get NBTBase tag from index.
     *
     * @param tag   NBTTagList instance.
     * @param index Index of the tag to return.
     * @return      A NBTBase instance.
     * @throws Throwable if any error occurs on reflected method invoking.
     */
    public static Object get(Object tag, int index) throws Throwable {
        return get.invoke(tag, index);
    }
}
