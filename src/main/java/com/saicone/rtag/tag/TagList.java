package com.saicone.rtag.tag;

import com.saicone.rtag.RtagMirror;
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

    private static final Class<?> NBT_LIST = EasyLookup.classById("NBTTagList");

    private static final MethodHandle newEmpty;
    private static final MethodHandle newList;
    private static final MethodHandle clone;
    private static final MethodHandle size;
    private static final MethodHandle add;
    private static final MethodHandle remove;
    private static final MethodHandle set;
    private static final MethodHandle get;
    private static final MethodHandle isTypeId;
    private static final MethodHandle typeField;
    private static final MethodHandle setListField;
    private static final MethodHandle getListField;

    static {
        // Constructors
        MethodHandle new$EmptyList = null;
        MethodHandle new$List = null;
        // Methods
        MethodHandle method$clone = null;
        MethodHandle method$size = null;
        MethodHandle method$add = null;
        MethodHandle method$remove = null;
        MethodHandle method$set = null;
        MethodHandle method$get = null;
        MethodHandle method$isTypeId = null;
        // Getters
        MethodHandle get$list = null;
        // Setters
        MethodHandle set$type = null;
        MethodHandle set$list = null;
        try {
            // Old names
            String clone = "clone";
            String size = "size";
            String add = "add";
            String remove = "a";
            String set = "a";
            String get = "g";
            String list = "list";
            // New names
            if (ServerInstance.verNumber >= 18) {
                clone = "d";
                add = "c";
                remove = "c";
                set = "d";
                get = "k";
                list = "c";
            } else if (ServerInstance.verNumber >= 9) {
                // Clone
                if (ServerInstance.verNumber >= 10 && ServerInstance.verNumber <= 13) {
                    clone = "d";
                }
                // Remove
                remove = "remove";
                // Set & Get
                if (ServerInstance.verNumber >= 13) {
                    set = "set";
                    get = "get";
                    if (ServerInstance.isUniversal) {
                        list = "c";
                    }
                } else if (ServerInstance.verNumber >= 12) {
                    get = "i";
                } else {
                    get = "h";
                }
            }

            new$EmptyList = EasyLookup.constructor(NBT_LIST);
            // Unreflect reason:
            // (1.8 -  1.9) return NBTBase
            // Other versions return NBTTagList
            method$clone = EasyLookup.unreflectMethod(NBT_LIST, clone);
            method$size = EasyLookup.method(NBT_LIST, size, int.class);
            method$remove = EasyLookup.method(NBT_LIST, remove, "NBTBase", int.class);
            // Unreflect reason:
            // (1.8 -  1.12) void method
            // Other versions return NBTBase
            method$set = EasyLookup.unreflectMethod(NBT_LIST, set, int.class, "NBTBase");
            method$get = EasyLookup.method(NBT_LIST, get, "NBTBase", int.class);
            // Private field
            get$list = EasyLookup.unreflectGetter(NBT_LIST, list);

            if (ServerInstance.verNumber >= 15) {
                // Private constructor
                new$List = EasyLookup.unreflectConstructor(NBT_LIST, List.class, byte.class);
            } else {
                // Private fields
                set$type = EasyLookup.unreflectSetter(NBT_LIST, "type");
                set$list = EasyLookup.unreflectSetter(NBT_LIST, list);
            }
            if (ServerInstance.verNumber >= 14) {
                method$add = EasyLookup.method(NBT_LIST, add, void.class, int.class, "NBTBase");
                // Private method
                method$isTypeId = EasyLookup.unreflectMethod(NBT_LIST, "a", "NBTBase");
            } else {
                // Unreflect reason:
                // (1.12 - 1.13) return boolean
                // Void method in other versions
                method$add = EasyLookup.unreflectMethod(NBT_LIST, add, "NBTBase");
            }
        } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        newEmpty = new$EmptyList;
        newList = new$List;
        clone = method$clone;
        size = method$size;
        add = method$add;
        remove = method$remove;
        set = method$set;
        get = method$get;
        isTypeId = method$isTypeId;
        typeField = set$type;
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
     * Constructs and NBTTagList with provided List of NBTBase.
     *
     * @param list List with NBTBase values.
     * @return     New NBTTagList instance.
     */
    public static Object newTag(List<?> list) {
        if (list == null || list.isEmpty()) {
            return newTag();
        }
        final byte type = TagBase.getTypeId(list.get(0));
        try {
            if (ServerInstance.verNumber >= 15) {
                return newList.invoke(list, type);
            } else {
                Object tag = newTag();
                typeField.invoke(tag, type);
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
     */
    public static Object newTag(RtagMirror mirror, List<?> list) {
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
            try {
                int length = (int) size.invoke(tag);
                for (int i = 0; i < length; i++) {
                    list.add(mirror.getTagValue(get.invoke(tag, i)));
                }
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
        return list;
    }

    /**
     * Get the size of elements inside list.
     *
     * @param tag NBTTagList instance.
     * @return    Size of list inside.
     */
    public static int size(Object tag) {
        try {
            return (int) size.invoke(tag);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
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
        if (ServerInstance.verNumber >= 14) {
            try {
                if ((boolean) isTypeId.invoke(listTag, tag)) {
                    getValue(listTag).add(tag);
                }
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        } else {
            try {
                add.invoke(listTag, tag);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    /**
     * Add multiple NBTBase tags.
     *
     * @param listTag NBTTagList instance.
     * @param tags    NBTBase tags to add.
     */
    public static void add(Object listTag, Object... tags) {
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
     */
    public static Object remove(Object tag, int index) {
        try {
            return remove.invoke(tag, index);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Set NBTBase tag at index.
     *
     * @param listTag NBTTagList instance.
     * @param index   Index of element to replace.
     * @param tag     Tag value to set.
     */
    public static void set(Object listTag, int index, Object tag) {
        try {
            set.invoke(listTag, index, tag);
        } catch (Throwable t) {
            throw new RuntimeException(t);
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
        try {
            return get.invoke(tag, index);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
