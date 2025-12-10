package com.saicone.rtag.util;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.ApiStatus;

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
     *
     * @deprecated Use {@link MC#version()} methods instead.
     */
    @Deprecated(since = "1.5.14")
    public static final float VERSION = MC.version().featRevision();
    /**
     * Current server version defined in craftbukkit package.
     *
     * @deprecated Use {@link MC#version()} methods instead.
     */
    @Deprecated(since = "1.5.14")
    public static final String PACKAGE_VERSION = MC.version().bukkitPackage();
    /**
     * Current data version number.
     *
     * @deprecated Use {@link MC#version()} methods instead.
     */
    @Deprecated(since = "1.5.14")
    public static final int DATA_VERSION = MC.version().dataVersion().orElse(98);
    /**
     * Current server version number simplified, for example:<br>
     * 1.8 -&gt; 8<br>
     * 1.12.2 -&gt; 12<br>
     * 1.17 -&gt; 17
     *
     * @deprecated Use {@link MC#version()} methods instead.
     */
    @Deprecated(since = "1.5.14")
    public static final int MAJOR_VERSION = MC.version().feature();
    /**
     * Current release version number, for example:<br>
     * v1_9_R2 -&gt; 2<br>
     * v1_13_R1 -&gt; 1<br>
     * v1_16_R3 -&gt; 3<br>
     *
     * @deprecated Use {@link MC#version()} methods instead.
     */
    @Deprecated(since = "1.5.14")
    public static final int RELEASE_VERSION = MC.version().revision();
    /**
     * Current server version number formatted, for example:<br>
     * v1_9_R2 -&gt; 10902<br>
     * v1_13_R1 -&gt; 11301<br>
     * v1_19_R2 -&gt; 11902
     *
     * @deprecated Use {@link MC#version()} methods instead.
     */
    @Deprecated(since = "1.5.14")
    public static final int FULL_VERSION = MC.version().fullRevision();

    /**
     * Server releases subclass with versions that introduces major changes.
     *
     * @deprecated Use {@link MC#version()} methods instead.
     */
    @Deprecated(since = "1.5.14")
    public static class Release {
        /**
         * Return true if server version is 1.12.2 or below.
         *
         * @deprecated Use {@link MC#version()} methods instead.
         */
        @Deprecated(since = "1.5.14")
        public static final boolean LEGACY = MC.version().isLegacy();
        /**
         * Return true if server version is 1.13 or upper.
         *
         * @deprecated Use {@link MC#version()} methods instead.
         */
        @Deprecated(since = "1.5.14")
        public static final boolean FLAT = MC.version().isFlat();
        /**
         * Return true if server version is 1.17 or upper.
         *
         * @deprecated Use {@link MC#version()} methods instead.
         */
        @Deprecated(since = "1.5.14")
        public static final boolean UNIVERSAL = MC.version().isUniversal();
        /**
         * Return true if server version is 1.20.5 or upper.
         *
         * @deprecated Use {@link MC#version()} methods instead.
         */
        @Deprecated(since = "1.5.14")
        public static final boolean COMPONENT = MC.version().isComponent();
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
     * @deprecated Use {@link MC#version()} methods instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.6.0")
    @Deprecated
    public static final String version = MC.version().bukkitPackage();
    /**
     * @deprecated Use {@link MC#version()} methods instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.6.0")
    @Deprecated
    public static final int fullVersion = MC.version().fullRevision();
    /**
     * @deprecated Use {@link MC#version()} methods instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.6.0")
    @Deprecated
    public static final int verNumber = MC.version().feature();
    /**
     * @deprecated Use {@link MC#version()} methods instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.6.0")
    @Deprecated
    public static final int release = MC.version().revision();
    /**
     * @deprecated Use {@link MC#version()} methods instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.6.0")
    @Deprecated
    public static final int dataVersion = MC.version().dataVersion().orElse(98);

    /**
     * @deprecated Use {@link MC#version()} methods instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.6.0")
    @Deprecated
    public static final boolean isLegacy = MC.version().isLegacy();
    /**
     * @deprecated Use {@link MC#version()} methods instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.6.0")
    @Deprecated
    public static final boolean isUniversal = MC.version().isUniversal();

    /**
     * @deprecated Use {@link Platform#SPIGOT} instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.6.0")
    @Deprecated
    public static final boolean isSpigot = Platform.SPIGOT;
    /**
     * @deprecated Use {@link Platform#PAPER} instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.6.0")
    @Deprecated
    public static final boolean isPaper = Platform.PAPER;
    /**
     * @deprecated Use {@link Platform#FOLIA} instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.6.0")
    @Deprecated
    public static final boolean isFolia = Platform.FOLIA;

    /**
     * @deprecated Use {@link Type#MOJANG_MAPPED} instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.6.0")
    @Deprecated
    public static final boolean isMojangMapped = Type.MOJANG_MAPPED;

    /**
     * Convert data version into defined craftbukkit package.
     *
     * @param dataVersion A minecraft data version.
     * @return            The provided data version as craftbukkit package.
     */
    public static String version(int dataVersion) {
        return MC.findReverse(version -> version.dataVersion().orElse(null), dataVersion).bukkitPackage();
    }

    /**
     * Convert data version into formatted server version number.
     *
     * @param dataVersion A minecraft data version.
     * @return            The provided data version as formatted server version.
     */
    public static int fullVersion(int dataVersion) {
        return MC.findReverse(version -> version.dataVersion().orElse(null), dataVersion).fullRevision();
    }

    /**
     * Convert data version into simplified server version number.
     *
     * @param dataVersion A minecraft data version.
     * @return            The provided data version as simplified server version.
     */
    public static int verNumber(int dataVersion) {
        return MC.findReverse(version -> version.dataVersion().orElse(null), dataVersion).feature();
    }

    /**
     * Convert data version into release version number.
     *
     * @param dataVersion A minecraft data version.
     * @return            The provided data version as release version.
     */
    public static int release(int dataVersion) {
        return MC.findReverse(version -> version.dataVersion().orElse(null), dataVersion).revision();
    }

    /**
     * Convert server version into data version number.
     *
     * @param version A simplified or formatted server version number.
     * @return        A minecraft data version or {@link Integer#MIN_VALUE} if fails.
     */
    public static int dataVersion(int version) {
        final MC result;
        if (version >= 10000) {
            result = MC.findReverse(MC::fullRevision, version);
        } else if (version < 8) {
            result = MC.findReverse(MC::revision, version);
        } else {
            result = MC.findReverse(MC::feature, version);
        }

        if (result == null) {
            return Integer.MIN_VALUE;
        }

        return result.dataVersion().orElse(98);
    }
}
