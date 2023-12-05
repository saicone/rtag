package com.saicone.rtag.util;

import org.bukkit.Bukkit;

import java.util.TreeMap;

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
     * Current data version number.
     */
    public static final int dataVersion;

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
     * <a href="https://papermc.io/software/paper">PaperMC.io</a>
     */
    public static final boolean isPaper;
    /**
     * Return true if server instance is a Folia server.<br>
     * <a href="https://papermc.io/software/folia">PaperMC.io</a>
     */
    public static final boolean isFolia;
    /**
     * Return true if server instance is mojang mapped.
     */
    public static final boolean isMojangMapped;

    private static final TreeMap<Integer, Integer[]> DATA_VERSION = new TreeMap<>();

    static {
        // Original data versions start by 100 until 15w32a
        DATA_VERSION.put(98, new Integer[] {8, 3, 10803}); // 1.8 doesn't have data version, so 98 will be used by default
        DATA_VERSION.put(169, new Integer[] {9, 1, 10901});
        DATA_VERSION.put(183, new Integer[] {9, 2, 10902});
        DATA_VERSION.put(510, new Integer[] {10, 1, 11001});
        DATA_VERSION.put(819, new Integer[] {11, 1, 11101});
        DATA_VERSION.put(1139, new Integer[] {12, 1, 11201});
        DATA_VERSION.put(1519, new Integer[] {13, 1, 11301});
        DATA_VERSION.put(1631, new Integer[] {13, 2, 11302});
        DATA_VERSION.put(1952, new Integer[] {14, 1, 11401});
        DATA_VERSION.put(2225, new Integer[] {15, 1, 11501});
        DATA_VERSION.put(2566, new Integer[] {16, 1, 11601});
        DATA_VERSION.put(2578, new Integer[] {16, 2, 11602});
        DATA_VERSION.put(2584, new Integer[] {16, 3, 11603});
        DATA_VERSION.put(2724, new Integer[] {17, 1, 11701});
        DATA_VERSION.put(2860, new Integer[] {18, 1, 11801});
        DATA_VERSION.put(2975, new Integer[] {18, 2, 11802});
        DATA_VERSION.put(3105, new Integer[] {19, 1, 11901});
        DATA_VERSION.put(3218, new Integer[] {19, 2, 11902});
        DATA_VERSION.put(3337, new Integer[] {19, 3, 11903});
        DATA_VERSION.put(3463, new Integer[] {20, 1, 12001});
        DATA_VERSION.put(3578, new Integer[] {20, 2, 12002});
        DATA_VERSION.put(3698, new Integer[] {20, 3, 12003});

        final String serverPackage = Bukkit.getServer().getClass().getPackage().getName();
        if (serverPackage.startsWith("org.bukkit.craftbukkit.v1_")) {
            version = serverPackage.split("\\.")[3];

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

            dataVersion = dataVersion(fullVersion);
        } else {
            // CraftBukkit without relocation detected, let's get data version first (only for +1.13 servers)
            int data = Integer.MAX_VALUE;
            try {
                final Class<?> magicNumbersClass = Class.forName(serverPackage + ".util.CraftMagicNumbers");
                final Object craftMagicNumbers = magicNumbersClass.getDeclaredField("INSTANCE").get(null);
                data = (int) magicNumbersClass.getDeclaredMethod("getDataVersion").invoke(craftMagicNumbers);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            dataVersion = data;

            // Get all version values using data version
            version = version(dataVersion);
            fullVersion = fullVersion(dataVersion);
            verNumber = verNumber(dataVersion);
            release = release(dataVersion);
        }

        DATA_VERSION.put(Integer.MIN_VALUE, new Integer[] {verNumber, release, fullVersion});

        isLegacy = verNumber <= 12;
        isUniversal = verNumber >= 17;
        boolean spigot = false;
        boolean paper = false;
        boolean folia = false;
        boolean mojmap = false;
        try {
            Class.forName("org.spigotmc.SpigotConfig");
            spigot = true;
        } catch (ClassNotFoundException ignored) { }
        try {
            Class.forName("com.destroystokyo.paper.Title");
            paper = true;
        } catch (ClassNotFoundException ignored) { }
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException ignored) { }
        try {
            Class.forName("net.minecraft.nbt.CompoundTag");
            mojmap = true;
        } catch (ClassNotFoundException ignored) { }
        isSpigot = spigot;
        isPaper = paper;
        isFolia = folia;
        isMojangMapped = mojmap;
    }

    ServerInstance() {
    }

    /**
     * Convert data version into defined craftbukkit package.
     *
     * @param dataVersion A minecraft data version.
     * @return            The provided data version as craftbukkit package.
     */
    public static String version(int dataVersion) {
        final Integer[] value = DATA_VERSION.floorEntry(dataVersion).getValue();
        return "v1_" + value[0] + "_R" + value[1];
    }

    /**
     * Convert data version into formatted server version number.
     *
     * @param dataVersion A minecraft data version.
     * @return            The provided data version as formatted server version.
     */
    public static int fullVersion(int dataVersion) {
        return DATA_VERSION.floorEntry(dataVersion).getValue()[2];
    }

    /**
     * Convert data version into simplified server version number.
     *
     * @param dataVersion A minecraft data version.
     * @return            The provided data version as simplified server version.
     */
    public static int verNumber(int dataVersion) {
        return DATA_VERSION.floorEntry(dataVersion).getValue()[0];
    }

    /**
     * Convert data version into release version number.
     *
     * @param dataVersion A minecraft data version.
     * @return            The provided data version as release version.
     */
    public static int release(int dataVersion) {
        return DATA_VERSION.floorEntry(dataVersion).getValue()[1];
    }

    /**
     * Convert server version into data version number.
     *
     * @param version A simplified or formatted server version number.
     * @return        A minecraft data version or {@link Integer#MIN_VALUE} if fails.
     */
    public static int dataVersion(int version) {
        if (version >= 10000) {
            for (Integer key : DATA_VERSION.descendingKeySet()) {
                final Integer[] value = DATA_VERSION.get(key);
                if (version >= value[2]) {
                    return key;
                }
            }
        } else {
            final int ver;
            final int rel;
            if (version < 8) {
                ver = verNumber;
                rel = version;
            } else {
                ver = version;
                rel = 1;
            }
            for (Integer key : DATA_VERSION.descendingKeySet()) {
                final Integer[] value = DATA_VERSION.get(key);
                if (ver >= value[0] && rel >= value[1]) {
                    return key;
                }
            }
        }
        return Integer.MIN_VALUE;
    }
}
