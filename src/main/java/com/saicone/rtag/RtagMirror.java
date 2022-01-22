package com.saicone.rtag;

import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;
import com.saicone.rtag.util.EasyLookup;

import java.util.List;
import java.util.Map;

public class RtagMirror {

    private static final Class<?> nbtBase = EasyLookup.classById("NBTBase");
    private static final Class<?> tagCompound = EasyLookup.classById("NBTTagCompound");
    private static final Class<?> tagList = EasyLookup.classById("NBTTagList");

    @SuppressWarnings("unchecked")
    public Object toTag(Rtag rtag, Object object) {
        if (nbtBase.isInstance(object)) {
            return object;
        } else if (object instanceof Map) {
            return TagCompound.newTag(rtag, (Map<String, Object>) object);
        } else if (object instanceof List) {
            return TagList.newTag(rtag, (List<Object>) object);
        } else {
            return TagBase.newTag(object);
        }
    }

    public Object fromTag(Rtag rtag, Object tag) {
        if (tagCompound.isInstance(tag)) {
            return TagCompound.getValue(rtag, tag);
        } else if (tagList.isInstance(tag)) {
            return TagList.getValue(rtag, tag);
        } else {
            return TagBase.getValue(tag);
        }
    }
}
