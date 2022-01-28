package com.saicone.rtag;

import java.util.Map;

/**
 * An deserializer that converts any {@link Map} of objects to assigned
 * object type.
 *
 * @author Rubenicos
 *
 * @param <T> Deserializable object type.
 */
public interface RtagDeserializer<T> {

    /**
     * The unique ID to detect any NBTTagCompound who can be converted
     * to object type on {@link Rtag#fromTag(Object)} operations.<br>
     * "Out" means the value inside will be obtained from tag.<br>
     *
     * This ID must be the same has "In" ID on {@link RtagSerializer}
     * if you want to read the saved object with same type.
     *
     * @return Object type ID.
     */
    String getOutID();

    /**
     * Method who be invoked to convert any {@link Map} to
     * current object type.
     *
     * @param compound A NBTTagCompound as {@link Map} instance.
     * @return         A deserialized object.
     */
    T deserialize(Map<String, Object> compound);
}
