package com.saicone.rtag;

import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;
import com.saicone.rtag.util.EasyLookup;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class Rtag {

    private static final Class<?> tagCompound = EasyLookup.classById("NBTTagCompound");
    private static final Class<?> tagList = EasyLookup.classById("NBTTagList");

    private final RtagMirror mirror;
    private final Map<String, RtagDeserializer<Object>> deserializers = new HashMap<>();
    private final Map<Class<?>, RtagSerializer<Object>> serializers = new HashMap<>();

    public Rtag() {
        this(new RtagMirror());
    }

    public Rtag(RtagMirror mirror) {
        this.mirror = mirror;
    }

    public <T> Rtag putDeserializer(RtagDeserializer<T> deserializer) {
        deserializers.put(deserializer.getOutID(), (RtagDeserializer<Object>) deserializer);
        return this;
    }

    public <T> Rtag putSerializer(Class<T> type, RtagSerializer<T> serializer) {
        serializers.put(type, (RtagSerializer<Object>) serializer);
        return this;
    }

    public boolean add(Object tag, Object value, Object... path) throws Throwable {
        if (path.length == 0) {
            return false;
        }
        Object finalTag = tag;
        for (int i = 0; i < path.length; i++) {
            Object key = path[i];
            if (key instanceof Integer && tagList.isInstance(finalTag)) {
                if (TagList.size(finalTag) >= (int) key) {
                    finalTag = TagList.get(finalTag, (int) key);
                } else {
                    // Out of bounds
                    return false;
                }
            } else if (tagCompound.isInstance(finalTag)) {
                String keyString = String.valueOf(key);
                // Create tag if not exists
                if (TagCompound.notHasKey(finalTag, keyString)) {
                    int i1 = i + 1;
                    if (path.length == i1 || path[i1] instanceof Integer) {
                        TagCompound.set(finalTag, keyString, TagList.newTag());
                    } else {
                        TagCompound.set(finalTag, keyString, TagCompound.newTag());
                    }
                }
                finalTag = TagCompound.get(finalTag, keyString);
            } else {
                // Incompatible tag
                return false;
            }
        }
        if (tagList.isInstance(finalTag)) {
            Object valueTag = toTag(value);
            if (valueTag != null) {
                TagList.add(finalTag, valueTag);
                return true;
            }
        }
        // Incompatible tag or value
        return false;
    }

    public boolean set(Object tag, Object value, Object... path) throws Throwable {
        if (path.length == 0) {
            return false;
        }
        Object finalTag = tag;
        for (int i = 0; i < path.length - 1; i++) {
            Object key = path[i];
            if (key instanceof Integer && tagList.isInstance(finalTag)) {
                if (TagList.size(finalTag) >= (int) key) {
                    finalTag = TagList.get(finalTag, (int) key);
                } else {
                    // Out of bounds
                    return false;
                }
            } else if (tagCompound.isInstance(finalTag)) {
                String keyString = String.valueOf(key);
                // Create tag if not exists
                if (TagCompound.notHasKey(finalTag, keyString)) {
                    TagCompound.set(finalTag, keyString, path[i + 1] instanceof Integer ? TagList.newTag() : TagCompound.newTag());
                }
                finalTag = TagCompound.get(finalTag, keyString);
            } else {
                // Incompatible tag
                return false;
            }
        }
        Object key = path[path.length - 1];
        if (key instanceof Integer && tagList.isInstance(finalTag)) {
            if (value == null) {
                TagList.remove(finalTag, (int) key);
            } else {
                Object valueTag = toTag(value);
                if (valueTag != null) {
                    TagList.set(finalTag, (int) key, valueTag);
                } else {
                    // Fail on convert
                    return false;
                }
            }
        } else if (tagCompound.isInstance(finalTag)) {
            if (value == null) {
                TagCompound.remove(finalTag, String.valueOf(key));
            } else {
                Object valueTag = toTag(value);
                if (valueTag != null) {
                    TagCompound.set(finalTag, String.valueOf(key), valueTag);
                } else {
                    // Fail on convert
                    return false;
                }
            }
        } else {
            // Incompatible tag
            return false;
        }
        return true;
    }

    public <T> T get(Object tag, Object... path) throws Throwable {
        return fromTag(getExact(tag, path));
    }

    public Object getExact(Object tag, Object... path) throws Throwable {
        Object finalTag = tag;
        for (Object key : path) {
            if (key instanceof Integer && tagList.isInstance(finalTag)) {
                if (TagList.size(finalTag) >= (int) key) {
                    finalTag = TagList.get(finalTag, (int) key);
                } else {
                    // Out of bounds
                    return null;
                }
            } else if (tagCompound.isInstance(finalTag)) {
                finalTag = TagCompound.get(finalTag, String.valueOf(key));
                if (finalTag == null) {
                    // Unknown path
                    return null;
                }
            } else {
                // Incompatible tag
                return null;
            }
        }
        return finalTag;
    }

    public Object toTag(Object object) {
        if (serializers.containsKey(object.getClass())) {
            RtagSerializer<Object> serializer = serializers.get(object.getClass());
            Map<String, Object> map = serializer.serialize(object);
            map.put("rtag==", serializer.getInID());
            return toTag(map);
        } else {
            return mirror.toTag(this, object);
        }
    }

    public <T> T fromTag(Object tag) {
        try {
            return (T) fromTagExact(tag);
        } catch (ClassCastException e) {
            return null;
        }
    }

    public Object fromTagExact(Object tag) {
        Object object = mirror.fromTag(this, tag);
        if (object instanceof Map) {
            Object type = ((Map<String, Object>) object).get("rtag==");
            if (type instanceof String && deserializers.containsKey((String) type)) {
                return deserializers.get((String) type).deserialize((Map<String, Object>) object);
            }
        }
        return object;
    }
}
