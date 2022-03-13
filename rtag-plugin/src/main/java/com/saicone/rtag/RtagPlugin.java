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
}
