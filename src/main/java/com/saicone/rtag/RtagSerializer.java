package com.saicone.rtag;

import java.util.Map;

/**
 * An Serializer that converts any Object to {@link Map}.
 *
 * @author Rubenicos
 *
 * @param <T> Serializable object type.
 */
public interface RtagSerializer<T> {

    /**
     * The unique ID to save with NBTTagCompound on {@link Rtag#newTag(Object)} operations.<br>
     * "In" means the value inside will be put into tag.<br>
     *
     * This ID must be the same has "Out" ID on {@link RtagDeserializer}
     * if you want to obtain the saved object as current type in a future.
     *
     * @return Object type ID.
     */
    String getInID();

    /**
     * Method who be invoked to convert any Object to {@link Map}.
     *
     * @param object A Object as &lt;T&gt; instance.
     * @return       A serialized {@link Map}.
     */
    Map<String, Object> serialize(T object);
}
