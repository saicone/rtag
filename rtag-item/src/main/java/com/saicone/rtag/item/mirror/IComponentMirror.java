package com.saicone.rtag.item.mirror;

import com.saicone.rtag.item.ItemMirror;

public class IComponentMirror implements ItemMirror {

    @Override
    public float getDeprecationVersion() {
        return 20.4f;
    }

    @Override
    public void upgrade(Object compound, String id, Object tag, float from, float to) {
        // TODO: implement version upgrade
    }

    @Override
    public void downgrade(Object compound, String id, Object tag, float from, float to) {
        // TODO: implement version downgrade
    }
}
