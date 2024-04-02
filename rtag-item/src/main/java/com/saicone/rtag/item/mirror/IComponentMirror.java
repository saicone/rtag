package com.saicone.rtag.item.mirror;

import com.saicone.rtag.Rtag;
import com.saicone.rtag.item.ItemMirror;
import com.saicone.rtag.item.ItemObject;
import com.saicone.rtag.tag.TagCompound;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class IComponentMirror implements ItemMirror {

    private static final Map<String, Consumer<Object>> TRANSFORMATIONS = new HashMap<>();

    @Override
    public float getDeprecationVersion() {
        return 20.04f;
    }

    @Override
    public void upgrade(Object compound, String id, float from, float to) {
        if (to >= 20.04f) {
            for (Object[] path : extractPaths(compound)) {
                Rtag.INSTANCE.move(compound, path, ItemObject.getComponentPath(path));
            }
            if (TagCompound.hasKey(compound, "components")) {
                // TODO: Apply transformations into created components
            }
        }
    }

    @Override
    public void upgrade(Object compound, String id, Object components, float from, float to) {
        upgrade(compound, id, from, to);
    }

    @Override
    public void downgrade(Object compound, String id, float from, float to) {
        if (from >= 20.04f && TagCompound.hasKey(compound, "components")) {
            // TODO: Apply transformations from current components
            for (Object[] path : extractPaths(compound)) {
                Rtag.INSTANCE.move(compound, path, ItemObject.getTagPath(path));
            }
        }
    }

    @Override
    public void downgrade(Object compound, String id, Object components, float from, float to) {
        downgrade(compound, id, from, to);
    }

    private Set<Object[]> extractPaths(Object compound) {
        final Map<String, Object> value = TagCompound.getValue(compound);
        if (value.isEmpty()) {
            return Set.of();
        }
        final Set<Object[]> paths = new HashSet<>();
        for (Map.Entry<String, Object> entry : value.entrySet()) {
            if (TagCompound.isTagCompound(entry.getValue())) {
                for (Object[] path : extractPaths(entry.getValue())) {
                    final Object[] subPath = new Object[path.length + 1];
                    subPath[0] = entry.getKey();
                    System.arraycopy(path, 0, subPath, 1, path.length);
                    paths.add(subPath);
                }
            } else {
                paths.add(new Object[] { entry.getKey() });
            }
        }
        return paths;
    }
}
