package com.saicone.rtag.item;

import com.saicone.rtag.data.TagData;
import org.bukkit.inventory.ItemStack;

public class ItemTagData extends TagData<ItemStack> {

    @Override
    protected Object extract(ItemStack object) {
        try {
            return ItemTag.saveTag(ItemBridge.asMinecraft(object));
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    @Override
    protected ItemStack build(Object object) {
        try {
            return ItemBridge.asBukkit(ItemTag.createStack(object));
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }
}
