package com.saicone.rtag;

import com.saicone.rtag.util.MC;
import org.bukkit.plugin.java.JavaPlugin;

public class RtagPlugin extends JavaPlugin {

    private static RtagPlugin instance;

    public static RtagPlugin get() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
        getLogger().info("Found version " + MC.version() + " (data version = " + MC.version().dataVersion().orElse(null) + ", package = " + MC.version().bukkitPackage() + ")");
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
        if (MC.version().isComponent()) {
            // Data
            loadRtagClass("data.ComponentType", "data.DataComponent");
        }
        // Registry
        loadRtagClass("registry.IOValue");
        // Stream
        loadRtagClass("stream.TStream", "stream.TStreamTools");
        // Util
        loadRtagClass("util.ProblemReporter");
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
