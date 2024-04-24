package com.saicone.rtag;

import com.saicone.rtag.util.ServerInstance;
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
        if (ServerInstance.Release.COMPONENT) {
            // Data
            loadRtagClass("data.ComponentType", "data.DataComponent");
        }
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
                final Class<?> clazz = Class.forName("com.saicone.rtag." + name);
                for (Class<?> declared : clazz.getDeclaredClasses()) {
                    Class.forName(declared.getName());
                }
            } catch (ClassNotFoundException ignored) { }
        }
    }
}
