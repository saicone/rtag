package com.saicone.rtag.util;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.ApiStatus;

import java.util.TreeMap;

/**
 * Server instance class to get information about current server.
 *
 * @author Rubenicos
 */
public class ServerInstance {

    /**
     * Get current server version using major version and release.<br>
     * v1_9_R2 -&gt; 9.02<br>
     * v1_13_R1 -&gt; 13.01<br>
     * v1_16_R3 -&gt; 16.03<br>
     */
    public static final float VERSION;
    /**
     * Current server version defined in craftbukkit package.
     */
    public static final String PACKAGE_VERSION;
    /**
     * Current data version number.
     */
    public static final int DATA_VERSION;
    /**
     * Current server version number simplified, for example:<br>
     * 1.8 -&gt; 8<br>
     * 1.12.2 -&gt; 12<br>
     * 1.17 -&gt; 17
     */
    public static final int MAJOR_VERSION;
    /**
     * Current release version number, for example:<br>
     * v1_9_R2 -&gt; 2<br>
     * v1_13_R1 -&gt; 1<br>
     * v1_16_R3 -&gt; 3<br>
     */
    public static final int RELEASE_VERSION;
    /**
     * Current server version number formatted, for example:<br>
     * v1_9_R2 -&gt; 10902<br>
     * v1_13_R1 -&gt; 11301<br>
     * v1_19_R2 -&gt; 11902
     */
    public static final int FULL_VERSION;

    private static final TreeMap<Integer, Integer[]> VERSION_MAP = new TreeMap<>();

    static {
        // Original data versions start by 100 until 15w32a
        VERSION_MAP.put(98, new Integer[] {8, 3, 10803}); // 1.8 doesn't have data version, so 98 will be used by default
        VERSION_MAP.put(169, new Integer[] {9, 1, 10901});
        VERSION_MAP.put(183, new Integer[] {9, 2, 10902});
        VERSION_MAP.put(510, new Integer[] {10, 1, 11001});
        VERSION_MAP.put(819, new Integer[] {11, 1, 11101});
        VERSION_MAP.put(1139, new Integer[] {12, 1, 11201});
        VERSION_MAP.put(1519, new Integer[] {13, 1, 11301});
        VERSION_MAP.put(1631, new Integer[] {13, 2, 11302});
        VERSION_MAP.put(1952, new Integer[] {14, 1, 11401});
        VERSION_MAP.put(2225, new Integer[] {15, 1, 11501});
        VERSION_MAP.put(2566, new Integer[] {16, 1, 11601});
        VERSION_MAP.put(2578, new Integer[] {16, 2, 11602});
        VERSION_MAP.put(2584, new Integer[] {16, 3, 11603});
        VERSION_MAP.put(2724, new Integer[] {17, 1, 11701});
        VERSION_MAP.put(2860, new Integer[] {18, 1, 11801});
        VERSION_MAP.put(2975, new Integer[] {18, 2, 11802});
        VERSION_MAP.put(3105, new Integer[] {19, 1, 11901});
        VERSION_MAP.put(3218, new Integer[] {19, 2, 11902});
        VERSION_MAP.put(3337, new Integer[] {19, 3, 11903});
        VERSION_MAP.put(3463, new Integer[] {20, 1, 12001});
        VERSION_MAP.put(3578, new Integer[] {20, 2, 12002});
        VERSION_MAP.put(3698, new Integer[] {20, 3, 12003});
        VERSION_MAP.put(3837, new Integer[] {20, 4, 12004});
        VERSION_MAP.put(3953, new Integer[] {21, 1, 12101});

        final String serverPackage = Bukkit.getServer().getClass().getPackage().getName();
        if (serverPackage.startsWith("org.bukkit.craftbukkit.v1_")) {
            PACKAGE_VERSION = serverPackage.split("\\.")[3];

            final String[] split = PACKAGE_VERSION.split("_");
            MAJOR_VERSION = Integer.parseInt(split[1]);

            // R1 -> 1  |  R2 - 2
            split[2] = split[2].substring(1);
            RELEASE_VERSION = Integer.parseInt(split[2]);

            // v1 -> 1
            split[0] = split[0].substring(1);
            // 8 -> 08  |  9 -> 09
            if (split[1].length() <= 1) {
                split[1] = '0' + split[1];
            }
            if (split[2].length() <= 1) {
                split[2] = '0' + split[2];
            }
            FULL_VERSION = Integer.parseInt(String.join("", split));

            if (MAJOR_VERSION >= 13) {
                DATA_VERSION = getDataVersion(serverPackage);
            } else {
                DATA_VERSION = dataVersion(FULL_VERSION);
            }
        } else {
            // CraftBukkit without relocation detected, let's get data version first (only for +1.13 servers)
            DATA_VERSION = getDataVersion(serverPackage);

            // Get all version values using data version
            PACKAGE_VERSION = version(DATA_VERSION);
            FULL_VERSION = fullVersion(DATA_VERSION);
            MAJOR_VERSION = verNumber(DATA_VERSION);
            RELEASE_VERSION = release(DATA_VERSION);
        }

        VERSION = Float.parseFloat(MAJOR_VERSION + "." + (RELEASE_VERSION < 10 ? "0" : "") + RELEASE_VERSION);

        VERSION_MAP.put(Integer.MIN_VALUE, new Integer[] {MAJOR_VERSION, RELEASE_VERSION, FULL_VERSION});
    }

    ServerInstance() {
    }

    private static int getDataVersion(String serverPackage) {
        try {
            final Class<?> magicNumbersClass = Class.forName(serverPackage + ".util.CraftMagicNumbers");
            final Object craftMagicNumbers = magicNumbersClass.getDeclaredField("INSTANCE").get(null);
            return (int) magicNumbersClass.getDeclaredMethod("getDataVersion").invoke(craftMagicNumbers);
        } catch (Throwable t) {
            t.printStackTrace();
            return Integer.MAX_VALUE;
        }
    }

    /**
     * Server releases subclass with versions that introduces major changes.
     */
    public static class Release {
        /**
         * Return true if server version is 1.12.2 or below.
         */
        public static final boolean LEGACY;
        /**
         * Return true if server version is 1.13 or upper.
         */
        public static final boolean FLAT;
        /**
         * Return true if server version is 1.17 or upper.
         */
        public static final boolean UNIVERSAL;
        /**
         * Return true if server version is 1.20.5 or upper.
         */
        public static final boolean COMPONENT;

        static {
            LEGACY = MAJOR_VERSION <= 12;
            FLAT = MAJOR_VERSION >= 13;
            UNIVERSAL = MAJOR_VERSION >= 17;
            COMPONENT = VERSION >= 20.04;
        }
    }

    /**
     * Server type subclass with major changes in compiled instance.
     */
    public static class Type {
        /**
         * Return true if server instance is mojang mapped.
         */
        public static final boolean MOJANG_MAPPED;
        /**
         * Return true if server instance has craftbukkit package relocated.
         */
        public static final boolean CRAFTBUKKIT_RELOCATED;

        static {
            boolean mojangMapped = false;
            try {
                Class.forName("net.minecraft.nbt.CompoundTag");
                mojangMapped = true;
            } catch (ClassNotFoundException ignored) { }
            MOJANG_MAPPED = mojangMapped;

            final String serverPackage = Bukkit.getServer().getClass().getPackage().getName();
            CRAFTBUKKIT_RELOCATED = serverPackage.startsWith("org.bukkit.craftbukkit.v1_");
        }
    }

    /**
     * Server platform subclass with different supported platforms.
     */
    public static class Platform {
        /**
         * Return true if server instance is a SpigotMC server.<br>
         * <a href="https://www.spigotmc.org/">SpigotMC.org</a>
         */
        public static final boolean SPIGOT;
        /**
         * Return true if server instance is a PaperMC server.<br>
         * <a href="https://papermc.io/software/paper">PaperMC.io</a>
         */
        public static final boolean PAPER;
        /**
         * Return true if server instance is a Folia server.<br>
         * <a href="https://papermc.io/software/folia">PaperMC.io</a>
         */
        public static final boolean FOLIA;

        static {
            boolean spigot = false;
            boolean paper = false;
            boolean folia = false;
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
            SPIGOT = spigot;
            PAPER = paper;
            FOLIA = folia;
        }
    }

    /**
     * @deprecated Use {@link ServerInstance#PACKAGE_VERSION} instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @Deprecated
    public static final String version = PACKAGE_VERSION;
    /**
     * @deprecated Use {@link ServerInstance#FULL_VERSION} instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @Deprecated
    public static final int fullVersion = FULL_VERSION;
    /**
     * @deprecated Use {@link ServerInstance#MAJOR_VERSION} instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @Deprecated
    public static final int verNumber = MAJOR_VERSION;
    /**
     * @deprecated Use {@link ServerInstance#RELEASE_VERSION} instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @Deprecated
    public static final int release = RELEASE_VERSION;
    /**
     * @deprecated Use {@link ServerInstance#DATA_VERSION} instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @Deprecated
    public static final int dataVersion = DATA_VERSION;

    /**
     * @deprecated Use {@link Release#LEGACY} instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @Deprecated
    public static final boolean isLegacy = Release.LEGACY;
    /**
     * @deprecated Use {@link Release#UNIVERSAL} instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @Deprecated
    public static final boolean isUniversal = Release.UNIVERSAL;

    /**
     * @deprecated Use {@link Platform#SPIGOT} instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @Deprecated
    public static final boolean isSpigot = Platform.SPIGOT;
    /**
     * @deprecated Use {@link Platform#PAPER} instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @Deprecated
    public static final boolean isPaper = Platform.PAPER;
    /**
     * @deprecated Use {@link Platform#FOLIA} instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @Deprecated
    public static final boolean isFolia = Platform.FOLIA;

    /**
     * @deprecated Use {@link Type#MOJANG_MAPPED} instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @Deprecated
    public static final boolean isMojangMapped = Type.MOJANG_MAPPED;

    /**
     * Convert data version into defined craftbukkit package.
     *
     * @param dataVersion A minecraft data version.
     * @return            The provided data version as craftbukkit package.
     */
    public static String version(int dataVersion) {
        final Integer[] value = VERSION_MAP.floorEntry(dataVersion).getValue();
        return "v1_" + value[0] + "_R" + value[1];
    }

    /**
     * Convert data version into formatted server version number.
     *
     * @param dataVersion A minecraft data version.
     * @return            The provided data version as formatted server version.
     */
    public static int fullVersion(int dataVersion) {
        return VERSION_MAP.floorEntry(dataVersion).getValue()[2];
    }

    /**
     * Convert data version into simplified server version number.
     *
     * @param dataVersion A minecraft data version.
     * @return            The provided data version as simplified server version.
     */
    public static int verNumber(int dataVersion) {
        return VERSION_MAP.floorEntry(dataVersion).getValue()[0];
    }

    /**
     * Convert data version into release version number.
     *
     * @param dataVersion A minecraft data version.
     * @return            The provided data version as release version.
     */
    public static int release(int dataVersion) {
        return VERSION_MAP.floorEntry(dataVersion).getValue()[1];
    }

    /**
     * Convert server version into data version number.
     *
     * @param version A simplified or formatted server version number.
     * @return        A minecraft data version or {@link Integer#MIN_VALUE} if fails.
     */
    public static int dataVersion(int version) {
        if (version >= 10000) {
            for (Integer key : VERSION_MAP.descendingKeySet()) {
                final Integer[] value = VERSION_MAP.get(key);
                if (version >= value[2]) {
                    return key;
                }
            }
        } else {
            final int ver;
            final int rel;
            if (version < 8) {
                ver = MAJOR_VERSION;
                rel = version;
            } else {
                ver = version;
                rel = 1;
            }
            for (Integer key : VERSION_MAP.descendingKeySet()) {
                final Integer[] value = VERSION_MAP.get(key);
                if (ver >= value[0] && rel >= value[1]) {
                    return key;
                }
            }
        }
        return Integer.MIN_VALUE;
    }
}
