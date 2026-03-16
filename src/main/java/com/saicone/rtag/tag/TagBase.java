package com.saicone.rtag.tag;

import com.saicone.rtag.RtagMirror;
import com.saicone.rtag.util.MC;
import com.saicone.rtag.util.reflect.Lookup;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Class to invoke methods inside classes that extends Tag.
 *
 * @author Rubenicos
 */
public class TagBase {

    // import
    private static final Lookup.AClass<?> ByteTag = Lookup.SERVER.importClass("net.minecraft.nbt.ByteTag");
    private static final Lookup.AClass<?> ByteArrayTag = Lookup.SERVER.importClass("net.minecraft.nbt.ByteArrayTag");
    private static final Lookup.AClass<?> DoubleTag = Lookup.SERVER.importClass("net.minecraft.nbt.DoubleTag");
    private static final Lookup.AClass<?> FloatTag = Lookup.SERVER.importClass("net.minecraft.nbt.FloatTag");
    private static final Lookup.AClass<?> IntTag = Lookup.SERVER.importClass("net.minecraft.nbt.IntTag");
    private static final Lookup.AClass<?> IntArrayTag = Lookup.SERVER.importClass("net.minecraft.nbt.IntArrayTag");
    private static final Lookup.AClass<?> LongTag = Lookup.SERVER.importClass("net.minecraft.nbt.LongTag");
    private static final Lookup.AClass<?> LongArrayTag = Lookup.SERVER.importClass("net.minecraft.nbt.LongArrayTag");
    private static final Lookup.AClass<?> ShortTag = Lookup.SERVER.importClass("net.minecraft.nbt.ShortTag");
    private static final Lookup.AClass<?> StringTag = Lookup.SERVER.importClass("net.minecraft.nbt.StringTag");
    private static final Lookup.AClass<?> Tag = Lookup.SERVER.importClass("net.minecraft.nbt.Tag");

    // declare
    private static final MethodHandle ByteArrayTag$new = ByteArrayTag.constructor(byte[].class).handle();
    private static final MethodHandle IntArrayTag$new = IntArrayTag.constructor(int[].class).handle();
    private static final MethodHandle LongArrayTag$new;
    static {
        if (MC.version().isNewerThanOrEquals(MC.V_1_12)) {
            LongArrayTag$new = LongArrayTag.constructor(long[].class).handle();
        } else {
            LongArrayTag$new = null;
        }
    }

    private static final MethodHandle ByteTag_valueOf;
    private static final MethodHandle ByteTag_valueOf$boolean;
    private static final MethodHandle DoubleTag_valueOf;
    private static final MethodHandle FloatTag_valueOf;
    private static final MethodHandle IntTag_valueOf;
    private static final MethodHandle LongTag_valueOf;
    private static final MethodHandle ShortTag_valueOf;
    private static final MethodHandle StringTag_valueOf;
    static {
        if (MC.version().isNewerThanOrEquals(MC.V_1_15)) {
            ByteTag_valueOf = ByteTag.method(Modifier.STATIC, ByteTag, "valueOf", byte.class).handle();
            ByteTag_valueOf$boolean = ByteTag.method(Modifier.STATIC, ByteTag, "valueOf", boolean.class).handle();
            DoubleTag_valueOf = DoubleTag.method(Modifier.STATIC, DoubleTag, "valueOf", double.class).handle();
            FloatTag_valueOf = FloatTag.method(Modifier.STATIC, FloatTag, "valueOf", float.class).handle();
            IntTag_valueOf = IntTag.method(Modifier.STATIC, IntTag, "valueOf", int.class).handle();
            LongTag_valueOf = LongTag.method(Modifier.STATIC, LongTag, "valueOf", long.class).handle();
            ShortTag_valueOf = ShortTag.method(Modifier.STATIC, ShortTag, "valueOf", short.class).handle();
            StringTag_valueOf = StringTag.method(Modifier.STATIC, StringTag, "valueOf", String.class).handle();
        } else {
            ByteTag_valueOf = ByteTag.constructor(byte.class).handle();
            ByteTag_valueOf$boolean = null;
            DoubleTag_valueOf = DoubleTag.constructor(double.class).handle();
            FloatTag_valueOf = FloatTag.constructor(float.class).handle();
            IntTag_valueOf = IntTag.constructor(int.class).handle();
            LongTag_valueOf = LongTag.constructor(long.class).handle();
            ShortTag_valueOf = ShortTag.constructor(short.class).handle();
            StringTag_valueOf = StringTag.constructor(String.class).handle();
        }
    }

    private static final MethodHandle ByteArrayTag$get_data = ByteArrayTag.field(byte[].class, "data").getter();
    private static final MethodHandle IntArrayTag$get_data = IntArrayTag.field(int[].class, "data").getter();
    private static final MethodHandle LongArrayTag$get_data;
    static {
        if (MC.version().isNewerThanOrEquals(MC.V_1_12)) {
            LongArrayTag$get_data = LongArrayTag.field(long[].class, "data").getter();
        } else {
            LongArrayTag$get_data = null;
        }
    }

    private static final MethodHandle ByteTag$get_value;
    private static final MethodHandle DoubleTag$get_value;
    private static final MethodHandle FloatTag$get_value;
    private static final MethodHandle IntTag$get_value;
    private static final MethodHandle LongTag$get_value;
    private static final MethodHandle ShortTag$get_value;
    private static final MethodHandle StringTag$get_value;
    static {
        if (MC.version().isNewerThanOrEquals(MC.V_1_21_5)) {
            ByteTag$get_value = ByteTag.field(byte.class, "value").getter();
            DoubleTag$get_value = DoubleTag.field(double.class, "value").getter();
            FloatTag$get_value = FloatTag.field(float.class, "value").getter();
            IntTag$get_value = IntTag.field(int.class, "value").getter();
            LongTag$get_value = LongTag.field(long.class, "value").getter();
            ShortTag$get_value = ShortTag.field(short.class, "value").getter();
            StringTag$get_value = StringTag.field(String.class, "value").getter();
        } else {
            ByteTag$get_value = ByteTag.field(byte.class, "data").getter();
            DoubleTag$get_value = DoubleTag.field(double.class, "data").getter();
            FloatTag$get_value = FloatTag.field(float.class, "data").getter();
            IntTag$get_value = IntTag.field(int.class, "data").getter();
            LongTag$get_value = LongTag.field(long.class, "data").getter();
            ShortTag$get_value = ShortTag.field(short.class, "data").getter();
            StringTag$get_value = StringTag.field(String.class, "data").getter();
        }
    }

    private static final MethodHandle Tag_getId = Tag.method(byte.class, "getId").handle();
    private static final MethodHandle Tag_copy = Tag.method(Tag, "copy").handle();

    TagBase() {
    }

    /**
     * Constructs a Tag directly associated with Java object.<br>
     * For example Float -&gt;  FloatTag
     *
     * @see TagCompound#newTag(Map) 
     * @see TagList#newTag(List) 
     *
     * @param object java object that can be represented as Tag.
     * @return       a Tag associated with provided object.
     * @throws IllegalArgumentException if the object is not supported by this method.
     */
    @SuppressWarnings("unchecked")
    public static Object newTag(Object object) throws IllegalArgumentException {
        if (object == null) {
            return null;
        } else if (object instanceof Byte) { // id 1
            return Lookup.invoke(ByteTag_valueOf, object);
        } else if (object instanceof Boolean) { // id 1
            if (ByteTag_valueOf$boolean != null) {
                return Lookup.invoke(ByteTag_valueOf$boolean, object);
            } else {
                return Lookup.invoke(ByteTag_valueOf, (Boolean) object ? (byte) 1 : (byte) 0);
            }
        } else if (object instanceof Short) { // id 2
            return Lookup.invoke(ShortTag_valueOf, object);
        } else if (object instanceof Integer) { // id 3
            return Lookup.invoke(IntTag_valueOf, object);
        } else if (object instanceof Long) { // id 4
            return Lookup.invoke(LongTag_valueOf, object);
        } else if (object instanceof Float) { // id 5
            return Lookup.invoke(FloatTag_valueOf, object);
        } else if (object instanceof Double) { // id 6
            return Lookup.invoke(DoubleTag_valueOf, object);
        } else if (object instanceof byte[]) { // id 7
            return Lookup.invoke(ByteArrayTag$new, object);
        } else if (object instanceof String) { // id 8
            return Lookup.invoke(StringTag_valueOf, object);
        } else if (object instanceof List) { // id 9
            return TagList.newUncheckedTag((List<?>) object);
        } else if (object instanceof Map) { // id 10
            return TagCompound.newUncheckedTag((Map<String, Object>) object);
        } else if (object instanceof int[]) { // id 11
            return Lookup.invoke(IntArrayTag$new, object);
        } else if (object instanceof long[]) { // id 12
            return Lookup.invoke(LongArrayTag$new, object);
        } else if (object instanceof UUID) { // id 8
            return Lookup.invoke(StringTag_valueOf, object.toString());
        } else {
            throw new IllegalArgumentException("The object type " + object.getClass().getName() + " cannot be used to create a net.minecraft.nbt.Tag");
        }
    }

    /**
     * Constructs a Tag recursively with provided object.<br>
     * For example List -&gt; ListTag
     *
     * @param mirror RtagMirror to convert objects into tags.
     * @param object Object that can be represented as Tag.
     * @return       a Tag associated with provided object.
     * @throws IllegalArgumentException if the object is not supported.
     */
    public static Object newTag(RtagMirror mirror, Object object) throws IllegalArgumentException {
        if (object instanceof List) {
            return TagList.newTag(mirror, (List<?>) object);
        } else if (object instanceof Map) {
            return TagCompound.newTag(mirror, object);
        } else {
            return newTag(object);
        }
    }

    /**
     * Check if the provided object is instance of Tag class.
     *
     * @param object the object to check.
     * @return       true if the object is an instance of Tag class.
     */
    public static boolean isTag(Object object) {
        return Tag.isInstance(object);
    }

    /**
     * Check if Tag object type is equals to provided type id.
     *
     * @param tag  the Tag object to check.
     * @param type the nbt type id.
     * @return     true if Tag type is equals.
     */
    public static boolean isTypeOf(Object tag, byte type) {
        return getTypeId(tag) == type;
    }

    /**
     * Check if two Tag objects has the same type.
     *
     * @param tag1 the first Tag object to check.
     * @param tag2 the second Tag object to check.
     * @return     true if the two Tag type are equals.
     */
    public static boolean isTypeOf(Object tag1, Object tag2) {
        return getTypeId(tag1) == getTypeId(tag2);
    }

    /**
     * Copy provided Tag object into new one.
     *
     * @param tag the Tag to copy.
     * @return    a Tag with the same value.
     */
    public static Object clone(Object tag) {
        return Lookup.invoke(Tag_copy, tag);
    }

    /**
     * Get current tag type ID.<br>
     * Byte = 1 | Short = 2 | Int = 3 | Long = 4 | Float = 5 | Double = 6 |
     * ByteArray = 7 | String = 8 | List = 9 | Compound = 10 | IntArray = 11 | LongArray = 12
     *
     * @param tag TagBase instance to get the ID.
     * @return    An ID that represents the tag type.
     */
    public static byte getTypeId(Object tag) {
        return Lookup.invoke(Tag_getId, tag);
    }

    /**
     * Get Java value of Tag tag.<br>
     * For example StringTag -&gt; String.
     *
     * @see TagCompound#getValue(RtagMirror, Object)
     * @see TagList#getValue(RtagMirror, Object)
     *
     * @param tag the Tag to extract value.
     * @return    a java object inside Tag tag.
     * @throws IllegalArgumentException if tag is not supported by this class.
     */
    public static Object getValue(Object tag) throws IllegalArgumentException {
        if (tag == null) {
            return null;
        }

        final byte id = getTypeId(tag);
        return getValue0(id, tag);
    }

    /**
     * Get Java value recursively of any Tag tag.<br>
     * For example CompoundTag -&gt; Map.
     *
     * @param mirror RtagMirror to convert objects into tags.
     * @param tag    Tag to extract value.
     * @return       a java object represented as Tag.
     * @throws IllegalArgumentException if tag is not supported.
     */
    public static Object getValue(RtagMirror mirror, Object tag) throws IllegalArgumentException {
        if (tag == null) {
            return null;
        }

        final byte id = getTypeId(tag);
        if (id == 9) {
            return TagList.getValue(mirror, tag);
        } else if (id == 10) {
            return TagCompound.getValue(mirror, tag);
        } else {
            return getValue0(id, tag);
        }
    }

    private static Object getValue0(byte id, @NotNull Object tag) throws IllegalArgumentException {
        switch (id) {
            case 0:
                return null;
            case 1:
                return Lookup.invoke(ByteTag$get_value, tag);
            case 2:
                return Lookup.invoke(ShortTag$get_value, tag);
            case 3:
                return Lookup.invoke(IntTag$get_value, tag);
            case 4:
                return Lookup.invoke(LongTag$get_value, tag);
            case 5:
                return Lookup.invoke(FloatTag$get_value, tag);
            case 6:
                return Lookup.invoke(DoubleTag$get_value, tag);
            case 7:
                return Lookup.invoke(ByteArrayTag$get_data, tag);
            case 8:
                return Lookup.invoke(StringTag$get_value, tag);
            case 9:
                return TagList.getValue(tag);
            case 10:
                return TagCompound.getValue(tag);
            case 11:
                return Lookup.invoke(IntArrayTag$get_data, tag);
            case 12:
                return Lookup.invoke(LongArrayTag$get_data, tag);
            default:
                throw new IllegalArgumentException("The object type " + tag.getClass().getName() + " is not supported by Tag class");
        }
    }

    /**
     * Get the size of elements inside CompoundTag or ListTag.
     *
     * @param tag the Tag instance.
     * @return    size of map or list inside.
     */
    public static int size(Object tag) {
        if (TagCompound.isTagCompound(tag)) {
            return TagCompound.getValue(tag).size();
        } else if (TagList.isTagList(tag)) {
            return TagList.size(tag);
        } else {
            return -1;
        }
    }

    /**
     * Clear the provided CompoundTag or ListTag.
     *
     * @param tag the Tag instance.
     */
    public static void clear(Object tag) {
        if (TagCompound.isTagCompound(tag)) {
            TagCompound.clear(tag);
        } else if (TagList.isTagList(tag)) {
            TagList.clear(tag);
        }
    }
}
