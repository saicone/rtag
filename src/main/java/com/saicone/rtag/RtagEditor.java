package com.saicone.rtag;

import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.util.OptionalType;

import java.util.Map;

/**
 * RtagEditor abstract class who edit any object
 * with NBTTagCompound inside.<br>
 * Also provide methods to easy-edit object tags
 * using a {@link Rtag} instance.
 *
 * @author Rubenicos
 *
 * @param <T> Parent object type.
 */
public abstract class RtagEditor<T> {

    protected final Rtag rtag;
    protected final T typeObject;
    protected final Object literalObject;
    protected Object tag;

    /**
     * Constructs an NBTEditor.
     *
     * @param rtag       Rtag parent.
     * @param typeObject Editor type object that can be converted to literal object.
     */
    public RtagEditor(Rtag rtag, T typeObject) {
        this.rtag = rtag;
        this.typeObject = typeObject;
        this.literalObject = getLiteralObject(typeObject);
        this.tag = getTag(this.literalObject);
    }

    /**
     * Constructs an NBTEditor.
     *
     * @param rtag          Rtag parent.
     * @param typeObject    Editor type object that can be converted to literal object.
     * @param literalObject Object instance with NBTTagCompound inside.
     */
    public RtagEditor(Rtag rtag, T typeObject, Object literalObject) {
        this.rtag = rtag;
        this.typeObject = typeObject;
        this.literalObject = literalObject;
        this.tag = getTag(literalObject);
    }

    /**
     * Constructs an NBTEditor.
     *
     * @param rtag          Rtag parent.
     * @param typeObject    Editor type object that can be converted to literal object.
     * @param literalObject Object instance with NBTTagCompound inside.
     * @param tag           NBTTagCompound object to edit.
     */
    public RtagEditor(Rtag rtag, T typeObject, Object literalObject, Object tag) {
        this.rtag = rtag;
        this.typeObject = typeObject;
        this.literalObject = literalObject;
        this.tag = tag;
    }

    /**
     * Get current {@link Rtag} parent.
     *
     * @return A Rtag instance.
     */
    public Rtag getRtag() {
        return rtag;
    }

    /**
     * Get current object instance.
     *
     * @deprecated To get current object use {@link #getLiteralObject()} instead.
     * @see #getLiteralObject()
     *
     * @return An object with NBTTagCompound inside.
     */
    @Deprecated
    public Object getObject() {
        return literalObject;
    }

    /**
     * Get current type object defined on editor.
     *
     * @return An object that can be converted to literal object.
     */
    public T getTypeObject() {
        return typeObject;
    }

    /**
     * Get current literal object.<br>
     * In most cases this is a Minecraft server object.
     *
     * @return An object with NBTTagCompound inside.
     */
    public Object getLiteralObject() {
        return literalObject;
    }

    /**
     * Get type object as literal one.
     *
     * @param typeObject Editor type object that can be converted to literal object.
     * @return           An object with NBTTagCompound inside.
     */
    public abstract Object getLiteralObject(T typeObject);

    /**
     * Get current tag.
     *
     * @return A NBTTagCompound.
     */
    public Object getTag() {
        return tag;
    }

    /**
     * Get current tag inside type or literal object.
     *
     * @param object Object instance with NBTTagCompound inside.
     * @return       A NBTTagCompound.
     */
    public abstract Object getTag(Object object);

    /**
     * Load changes into object instance.
     *
     * @return The current instance type object.
     */
    public abstract T load();

    /**
     * Update the current tag using the original object type.
     */
    public void update() {
        update(getLiteralObject(typeObject));
    }

    /**
     * Update the current tag using the provided object type.
     *
     * @param object Object type according RtagEditor instance.
     */
    public void update(Object object) {
        set(getTag(object));
    }

    /**
     * Check if current object has tag.
     *
     * @return True if tag is not null.
     */
    public boolean hasTag() {
        return getTag() != null;
    }

    /**
     * Check if current tag contains a object in defined path.
     *
     * @param path Final value path to get.
     * @return     True if final value is not null.
     */
    public boolean hasTag(Object... path) {
        return getExact(path) != null;
    }

    /**
     * Same has {@link #hasTag()} but with inverted result.
     *
     * @return True if tag is null.
     */
    public boolean notHasTag() {
        return !hasTag();
    }

    /**
     * Same has {@link #hasTag(Object...)} but with inverted result.
     *
     * @param path Final value path to get.
     * @return     True if final value is null.
     */
    public boolean notHasTag(Object... path) {
        return !hasTag(path);
    }

    /**
     * Check if current tag has Enum element in defined path.
     *
     * @param element Enum element to check.
     * @param path    Path with Integer storing enum ordinals.
     * @return        true if the tag contains a bit field with enum ordinal.
     * @param <E>     Enum element type.
     */
    public <E extends Enum<E>> boolean hasEnum(E element, Object... path) {
        return hasBitField(getBitField(path), element.ordinal());
    }

    /**
     * Check if current tag has Enum elements array in defined path.
     *
     * @param elements Enum elements to check.
     * @param path     Path with Integer storing enum ordinals.
     * @return         true if the tag contains a bit field with enum ordinals.
     * @param <E>      Enum element type.
     */
    public <E extends Enum<E>> boolean hasEnum(E[] elements, Object... path) {
        return hasBitField(getBitField(path), enumOrdinals(elements));
    }

    /**
     * Check if current tag has Enum ordinal in defined path.
     *
     * @param ordinal Enum ordinal to check.
     * @param path    Path with Integer storing enum ordinals.
     * @return        true if the tag contains a bit field with enum ordinal.
     */
    public boolean hasEnum(int ordinal, Object... path) {
        return hasBitField(getBitField(path), ordinal);
    }

    /**
     * Check if current tag has Enum ordinals in defined path.
     *
     * @param ordinals Enum ordinal to check.
     * @param path     Path with Integer storing enum ordinals.
     * @return         true if the tag contains a bit field with enum ordinals.
     */
    public boolean hasEnum(int[] ordinals, Object... path) {
        return hasBitField(getBitField(path), ordinals);
    }

    private boolean hasBitField(int bitField, int... ordinals) {
        for (int ordinal : ordinals) {
            final byte bit = (byte) (1 << ordinal);
            if ((bitField & bit) != bit) {
                return false;
            }
        }
        return bitField > 0;
    }

    /**
     * Add value to an NBTTagList on specified path inside current object tag.<br>
     * See {@link Rtag#add(Object, Object, Object...)} for more information.
     *
     * @param value Value to add.
     * @param path  Final list path to add the specified value.
     * @return      True if the value was added.
     */
    public boolean add(Object value, Object... path) {
        try {
            return rtag.add(tag, value, path);
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    /**
     * Add Enum element into bit field on specified path.
     *
     * @param element Enum element to add.
     * @param path    Path with Integer storing enum ordinals.
     * @return        true if the Enum element was added successfully.
     * @param <E>     Enum element type.
     */
    public <E extends Enum<E>> boolean addEnum(E element, Object... path) {
        return addEnum(new int[] {element.ordinal()}, path);
    }

    /**
     * Add Enum elements into bit field on specified path.
     *
     * @param elements Enum elements to add.
     * @param path     Path with Integer storing enum ordinals.
     * @return         true if the Enum elements was added successfully.
     * @param <E>      Enum element type.
     */
    public <E extends Enum<E>> boolean addEnum(E[] elements, Object... path) {
        return addEnum(enumOrdinals(elements), path);
    }

    /**
     * Add Enum ordinal into bit field on specified path.
     *
     * @param ordinal Enum ordinal to add.
     * @param path    Path with Integer storing enum ordinals.
     * @return        true if the Enum ordinal was added successfully.
     */
    public boolean addEnum(int ordinal, Object... path) {
        return addEnum(new int[] {ordinal}, path);
    }

    /**
     * Add Enum ordinals into bit field on specified path.
     *
     * @param ordinals Enum ordinals to add.
     * @param path     Path with Integer storing enum ordinals.
     * @return         true if the Enum ordinals was added successfully.
     */
    public boolean addEnum(int[] ordinals, Object... path) {
        int bitField = getBitField(path);
        for (int ordinal : ordinals) {
            final byte bit = (byte) (1 << ordinal);
            bitField |= bit;
        }
        return set(bitField, path);
    }

    /**
     * Change object tag into new one.<br>
     * Value must be Map&lt;String, Object&gt; or NBTTagCompound.
     *
     * @param value Object to replace current tag.
     * @return      True if tag has replaced.
     */
    public boolean set(Object value) {
        Object tag = rtag.newTag(value);
        if (TagCompound.isTagCompound(tag)) {
            this.tag = tag;
            return true;
        }
        return false;
    }

    /**
     * Set value to specified path inside current tag.<br>
     * See {@link Rtag#set(Object, Object, Object...)} for more information.
     * 
     * @param value Value to set.
     * @param path  Final value path to set.
     * @return      True if the value is set.
     */
    public boolean set(Object value, Object... path) {
        try {
            return rtag.set(tag, value, path);
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    /**
     * Set Enum element has bit field on specified path.
     *
     * @param element Enum element to set.
     * @param path    Path to store enum ordinals has integer.
     * @return        true if the Enum element was set successfully.
     * @param <E>     Enum element type.
     */
    public <E extends Enum<E>> boolean setEnum(E element, Object... path) {
        return setEnum(new int[] {element.ordinal()}, path);
    }

    /**
     * Set Enum elements has bit field on specified path.
     *
     * @param elements Enum elements to set.
     * @param path     Path to store enum ordinals has integer.
     * @return         true if the Enum elements was set successfully.
     * @param <E>      Enum element type.
     */
    public <E extends Enum<E>> boolean setEnum(E[] elements, Object... path) {
        return setEnum(enumOrdinals(elements), path);
    }

    /**
     * Set Enum ordinal has bit field on specified path.
     *
     * @param ordinal Enum ordinal to set.
     * @param path    Path to store enum ordinals has integer.
     * @return        true if the Enum ordinal was set successfully.
     */
    public boolean setEnum(int ordinal, Object... path) {
        return setEnum(new int[] {ordinal}, path);
    }

    /**
     * Set Enum ordinals has bit field on specified path.
     *
     * @param ordinals Enum ordinals to set.
     * @param path     Path to store enum ordinals has integer.
     * @return         true if the Enum ordinals was set successfully.
     */
    public boolean setEnum(int[] ordinals, Object... path) {
        int bitField = 0;
        for (int ordinal : ordinals) {
            final byte bit = (byte) (1 << ordinal);
            bitField |= bit;
        }
        return set(bitField, path);
    }

    /**
     * Merge the provided value into current tag.
     *
     * @param value   The value to merge.
     * @param replace True to replace the repeated values inside current tag.
     * @return        true if the value was merged.
     */
    public boolean merge(Object value, boolean replace) {
        return TagCompound.merge(tag, rtag.newTag(value), replace);
    }

    /**
     * Merge the provided value at provided path.
     *
     * @param value   The value to merge.
     * @param replace True to replace the repeated values inside NBTTagCompound.
     * @param path    Final value path to merge into.
     * @return        true if the value was merged.
     */
    public boolean merge(Object value, boolean replace, Object... path) {
        return rtag.merge(tag, value, replace, path);
    }

    /**
     * Merge the provided value into current tag using deep method.
     *
     * @param value   The value to merge.
     * @param replace True to replace the repeated values inside current tag.
     * @return        true if the value was merged.
     */
    public boolean deepMerge(Object value, boolean replace) {
        return TagCompound.merge(tag, rtag.newTag(value), replace, true);
    }

    /**
     * Merge the provided value at provided path using deep method.
     *
     * @param value   The value to merge.
     * @param replace True to replace the repeated values inside NBTTagCompound.
     * @param path    Final value path to merge into.
     * @return        true if the value was merged.
     */
    public boolean deepMerge(Object value, boolean replace, Object... path) {
        return rtag.deepMerge(tag, value, replace, path);
    }

    /**
     * Move tag from specified path to any path.
     *
     * @param from  Path to get the value.
     * @param to    Path to set the value.
     * @return      true if the value was moved.
     */
    public boolean move(Object[] from, Object[] to) {
        return rtag.move(tag, from, to);
    }

    /**
     * Move tag from specified path to any path.
     *
     * @param from  Path to get the value.
     * @param to    Path to set the value.
     * @param clear True to clear empty paths.
     * @return      true if the value was moved.
     */
    public boolean move(Object[] from, Object[] to, boolean clear) {
        return rtag.move(tag, from, to, clear);
    }

    /**
     * Remove value to specified path inside current tag.<br>
     * See {@link Rtag#set(Object, Object, Object...)} for more information.
     *
     * @param path Final value path to remove.
     * @return     True if the value associated with the path exists and is removed.
     */
    public boolean remove(Object... path) {
        try {
            return rtag.set(tag, null, path);
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    /**
     * Remove Enum element from bit field on specified path.
     *
     * @param element Enum element to remove.
     * @param path    Path with Integer storing enum ordinals.
     * @return        true if the Enum element was removed or the bit field doesn't exist.
     * @param <E>     Enum element type.
     */
    public <E extends Enum<E>> boolean removeEnum(E element, Object... path) {
        return removeEnum(new int[] {element.ordinal()}, path);
    }

    /**
     * Remove Enum elements from bit field on specified path.
     *
     * @param elements Enum elements to remove.
     * @param path     Path with Integer storing enum ordinals.
     * @return         true if the Enum elements was removed or the bit field doesn't exist.
     * @param <E>      Enum element type.
     */
    public <E extends Enum<E>> boolean removeEnum(E[] elements, Object... path) {
        return removeEnum(enumOrdinals(elements), path);
    }

    /**
     * Remove Enum ordinal from bit field on specified path.
     *
     * @param ordinal Enum ordinal to remove.
     * @param path    Path with Integer storing enum ordinals.
     * @return        true if the Enum ordinal was removed or the bit field doesn't exist.
     */
    public boolean removeEnum(int ordinal, Object... path) {
        return removeEnum(new int[] {ordinal}, path);
    }

    /**
     * Remove Enum ordinals from bit field on specified path.
     *
     * @param ordinals Enum ordinals to remove.
     * @param path     Path with Integer storing enum ordinals.
     * @return         true if the Enum ordinals was removed or the bit field doesn't exist.
     */
    public boolean removeEnum(int[] ordinals, Object... path) {
        int bitField = getBitField(path);
        if (bitField < 1) {
            return true;
        }
        for (int ordinal : ordinals) {
            final byte bit = (byte) (1 << ordinal);
            bitField &= ~bit;
        }
        return set(bitField, path);
    }

    /**
     * Get a converted version of current tag.
     *
     * @return A Map with objects converted by Rtag parent.
     */
    public Map<String, Object> get() {
        return OptionalType.cast(rtag.getTagValue(tag));
    }

    /**
     * Get value from the specified path inside current tag.<br>
     * See {@link Rtag#get(Object, Object...)} for more information.
     *
     * @param path Final value path to get.
     * @param <V>  Object type to cast the value.
     * @return     The value assigned to specified path, null if not
     *             exist or a ClassCastException occurs.
     */
    public <V> V get(Object... path) {
        try {
            return rtag.get(tag, path);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Same has {@link #get(Object...)} but save the value into {@link OptionalType}.
     *
     * @param path Final value path to get.
     * @return     The value assigned to specified path has {@link OptionalType}.
     */
    public OptionalType getOptional(Object... path) {
        Object value;
        try {
            value = rtag.getTagValue(rtag.getExact(tag, path));
        } catch (Throwable t) {
            t.printStackTrace();
            value = null;
        }
        return OptionalType.of(value);
    }

    /**
     * Get exact NBTBase value without any conversion,
     * from the specified path inside current tag.<br>
     * See {@link Rtag#getExact(Object, Object...)} for more information.
     *
     * @param path Final value path to get.
     * @return     The value assigned to specified path, null if not exist.
     */
    public Object getExact(Object... path) {
        try {
            return rtag.getExact(tag, path);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Get Integer bit field from specified path.
     *
     * @param path Final value path to get.
     * @return     The value assigned to specified path, 0 if not exist.
     */
    public int getBitField(Object... path) {
        return getOptional(path).asInt(0);
    }

    private <E extends Enum<E>> int[] enumOrdinals(E[] elements) {
        final int[] ordinals = new int[elements.length];
        for (int i = 0; i < elements.length; i++) {
            ordinals[i] = elements[i].ordinal();
        }
        return ordinals;
    }
}
