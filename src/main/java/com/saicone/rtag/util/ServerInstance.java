package com.saicone.rtag.util;

import org.bukkit.Bukkit;

public class ServerInstance {

    public static final String version;
    public static final int verNumber;

    public static final boolean isLegacy;
    public static final boolean isUniversal;
    public static final boolean isSpigot;
    public static final boolean isPaper;

    static {
        version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        verNumber = Integer.parseInt(version.split("_")[1]);
        isLegacy = verNumber <= 12;
        isUniversal = verNumber >= 17;
        boolean spigot = false;
        boolean paper = false;
        try {
            Class.forName("org.spigotmc.SpigotConfig");
            spigot = true;
        } catch (ClassNotFoundException ignored) { }
        try {
            Class.forName("com.destroystokyo.paper.Title");
            paper = true;
        } catch (ClassNotFoundException ignored) { }
        isSpigot = spigot;
        isPaper = paper;
    }
}
