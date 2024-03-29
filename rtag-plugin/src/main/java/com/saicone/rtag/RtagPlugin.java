package com.saicone.rtag;

import org.bukkit.plugin.java.JavaPlugin;

public class RtagPlugin extends JavaPlugin {

    private static RtagPlugin instance;

    public static RtagPlugin get() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
        // Utils
        loadRtagClass("util.EasyLookup",
                "util.ChatComponent",
                "util.EnchantmentTag",
                "util.ItemMaterialTag",
                "util.SkullTexture");
        // Main
        loadRtagClass("Rtag", "RtagMirror");
        // Tag
        loadRtagClass("tag.TagBase", "tag.TagList", "tag.TagCompound");
        // Stream
        loadRtagClass("stream.TStream", "stream.TStreamTools");
        // Block
        loadRtagClass("block.BlockObject");
        // Entity
        loadRtagClass("entity.EntityObject");
        // Item
        loadRtagClass("item.ItemObject", "item.ItemTagStream");
    }

    private void loadRtagClass(String... names) {
        for (String name : names) {
            try {
                Class.forName("com.saicone.rtag." + name);
            } catch (ClassNotFoundException ignored) { }
        }
    }
}
