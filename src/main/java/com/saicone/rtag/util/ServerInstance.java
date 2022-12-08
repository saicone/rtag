package com.saicone.rtag.util;

import org.bukkit.Bukkit;

/**
 * Server instance class to get information about current server.
 *
 * @author Rubenicos
 */
public class ServerInstance {

    /**
     * Current server version defined in craftbukkit package.
     */
    public static final String version;
    /**
     * Current server version number formatted, for example:<br>
     * v1_9_R2 -&gt; 10902<br>
     * v1_13_R1 -&gt; 11301<br>
     * v1_19_R2 -&gt; 11902
     */
    public static final int fullVersion;
    /**
     * Current server version number simplified, for example:<br>
     * 1.8 -&gt; 8<br>
     * 1.12.2 -&gt; 12<br>
     * 1.17 -&gt; 17
     */
    public static final int verNumber;
    /**
     * Current release version number, for example:<br>
     * v1_9_R2 -&gt; 2<br>
     * v1_13_R1 -&gt; 1<br>
     * v1_16_R3 -&gt; 3<br>
     */
    public static final int release;

    /**
     * Return true if server version is 1.12.2 or below.
     */
    public static final boolean isLegacy;
    /**
     * Return true if server version is 1.17 or upper.
     */
    public static final boolean isUniversal;
    /**
     * Return true if server instance is a SpigotMC server.<br>
     * <a href="https://www.spigotmc.org/">SpigotMC.org</a>
     */
    public static final boolean isSpigot;
    /**
     * Return true if server instance is a PaperMC server.<br>
     * <a href="https://papermc.io/">PaperMC.io</a>
     */
    public static final boolean isPaper;

    static {
        version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        final String[] split = version.split("_");
        verNumber = Integer.parseInt(split[1]);
        // R1 -> 1  |  R2 - 2
        split[2] = split[2].substring(1);
        release = Integer.parseInt(split[2]);
        // v1 -> 1
        split[0] = split[0].substring(1);
        // 8 -> 08  |  9 -> 09
        if (split[1].length() <= 1) {
            split[1] = '0' + split[1];
        }
        if (split[2].length() <= 1) {
            split[2] = '0' + split[2];
        }
        fullVersion = Integer.parseInt(String.join("", split));
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

    ServerInstance() {
    }
}
