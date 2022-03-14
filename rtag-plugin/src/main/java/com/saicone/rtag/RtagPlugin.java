package com.saicone.rtag;

import org.bukkit.plugin.java.JavaPlugin;

public class RtagPlugin extends JavaPlugin {

    private static RtagPlugin instance;

    public static RtagPlugin get() {
        return instance;
    }

    public RtagPlugin() {
        instance = this;
    }

    @Override
    public void onLoad() {
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
        loadRtagClass("block.TileBridge", "block.TileTag");
        // Entity
        loadRtagClass("entity.EntityBridge", "entity.EntityTag");
        // Item
        loadRtagClass("item.ItemBridge", "item.ItemTag", "item.ItemTagStream");
    }

    private void loadRtagClass(String... names) {
        for (String name : names) {
            try {
                Class.forName("com.saicone.rtag." + name);
            } catch (ClassNotFoundException ignored) { }
        }
    }
}
