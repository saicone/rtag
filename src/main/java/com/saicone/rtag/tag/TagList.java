package com.saicone.rtag.tag;

import com.saicone.rtag.Rtag;
import com.saicone.rtag.util.EasyLookup;
import com.saicone.rtag.util.ServerInstance;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;

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
    private static final MethodHandle listField;
    private static final MethodHandle typeField;

    static {
        MethodHandle m1 = null, m2 = null, m3 = null, m4 = null, m5 = null, m6 = null, m7 = null, m8 = null, m9 = null, m10 = null;
        try {
            Class<?> base = EasyLookup.classById("NBTBase");
            // Old names
            String size = "size", add = "add", remove = "a", set = "a", get = "g", getTypeId = "getTypeId";
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

            m1 = EasyLookup.constructor(nbtList);
            if (ServerInstance.verNumber >= 15) {
                m2 = EasyLookup.unreflectConstructor(nbtList, List.class, byte.class);
            } else {
                m9 = EasyLookup.unreflectSetter(nbtList, "list");
                m10 = EasyLookup.unreflectSetter(nbtList, "type");
            }
            m3 = EasyLookup.method(nbtList, size, int.class);
            if (ServerInstance.verNumber >= 14) {
                m4 = EasyLookup.method(nbtList, add, void.class, int.class, base);
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
        } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        newEmpty = m1; newList = m2; size = m3; add = m4; remove = m5; set = m6; get = m7; getTypeId = m8; listField = m9; typeField = m10;
    }

    public static Object newTag() throws Throwable {
        return newEmpty.invoke();
    }

    public static Object newTag(List<Object> list) throws Throwable {
        if (list.isEmpty()) return newEmpty.invoke();
        byte type = (byte) getTypeId.invoke(list.get(0));
        if (ServerInstance.verNumber >= 15) {
            return newList.invoke(list, type);
        } else {
            Object tag = newTag();
            typeField.invoke(tag, type);
            listField.invoke(tag, list);
            return tag;
        }
    }

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

    public static List<Object> getValue(Rtag rtag, Object tag) {
        List<Object> list = new ArrayList<>();
        try {
            int length = (int) size.invoke(tag);
            for (int i = 0; i < length; i++) {
                list.add(rtag.fromTagExact(get.invoke(tag, i)));
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return list;
    }

    public static int size(Object tag) throws Throwable {
        return (int) size.invoke(tag);
    }

    public static void add(Object listTag, Object tag) throws Throwable {
        if (ServerInstance.verNumber >= 14) {
            add.invoke(listTag, getTypeId.invoke(tag), tag);
        } else {
            add.invoke(listTag, tag);
        }
    }

    public static void add(Object listTag, Object... tag) throws Throwable {
        if (ServerInstance.verNumber >= 14) {
            for (Object o : tag) {
                add.invoke(listTag, getTypeId.invoke(o), o);
            }
        } else {
            for (Object o : tag) {
                add.invoke(listTag, o);
            }
        }
    }

    public static Object remove(Object tag, int index) throws Throwable {
        return remove.invoke(tag, index);
    }

    public static void set(Object listTag, int index, Object tag) throws Throwable {
        set.invoke(listTag, index, tag);
    }

    public static Object get(Object tag, int index) throws Throwable {
        return get.invoke(tag, index);
    }
}
