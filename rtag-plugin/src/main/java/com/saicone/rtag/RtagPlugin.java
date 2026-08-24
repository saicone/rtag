package com.saicone.rtag;

import com.saicone.rtag.util.MC;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class RtagPlugin extends JavaPlugin {

    private static final String[] KNOWN_CLASSES = new String[] {
            // rtag
            "com.saicone.rtag.Rtag",
            "com.saicone.rtag.RtagDeserializer",
            "com.saicone.rtag.RtagEditor",
            "com.saicone.rtag.RtagMirror",
            "com.saicone.rtag.RtagSerializer",
            "com.saicone.rtag.data.ComponentType",
            "com.saicone.rtag.data.DataComponent",
            "com.saicone.rtag.registry.IOValue",
            "com.saicone.rtag.stream.TStream",
            "com.saicone.rtag.stream.TStreamTools",
            "com.saicone.rtag.tag.TagBase",
            "com.saicone.rtag.tag.TagCompound",
            "com.saicone.rtag.tag.TagList",
            "com.saicone.rtag.util.ChatComponent",
            "com.saicone.rtag.util.ProblemReporter",
            // rtag-block
            "com.saicone.rtag.RtagBlock",
            "com.saicone.rtag.block.BlockObject",
            // rtag-entity
            "com.saicone.rtag.RtagEntity",
            "com.saicone.rtag.entity.EntityObject",
            // rtag-item
            "com.saicone.rtag.RtagItem",
            "com.saicone.rtag.item.ItemData",
            "com.saicone.rtag.item.ItemMirror",
            "com.saicone.rtag.item.ItemObject",
            "com.saicone.rtag.item.ItemTagStream",
    };

    private static RtagPlugin instance;

    public static RtagPlugin get() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
        getLogger().info("Found version " + MC.version() + " (data version = " + MC.version().dataVersion().orElse(null) + ", package = " + MC.version().bukkitPackage() + ")");
        for (String name : KNOWN_CLASSES) {
            try {
                final Class<?> clazz = Class.forName(name);
                for (Class<?> declared : clazz.getDeclaredClasses()) {
                    Class.forName(declared.getName());
                }
            } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
                // ignored exception
            } catch (Throwable t) {
                getLogger().log(Level.WARNING, "Failed to initialize class " + name, t);
            }
        }
    }
}
