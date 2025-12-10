package com.saicone.rtag.util;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Utility class that provides general information of every supported Minecraft version.
 *
 * @author Rubenicos
 */
public final class MC implements Comparable<MC> {

    private static final AtomicInteger ORDINAL = new AtomicInteger(0);

    // Bountiful Update
    public static final MC
            V_1_8   = ver(1, 8).rev(1).protocol(47).resource(1),
            V_1_8_1 = ver(1, 8, 1).rev(1).protocol(47).resource(1),
            V_1_8_2 = ver(1, 8, 2).rev(1).protocol(47).resource(1),
            V_1_8_3 = ver(1, 8, 3).rev(2).protocol(47).resource(1),
            V_1_8_4 = ver(1, 8, 4).rev(2).protocol(47).resource(1),
            V_1_8_5 = ver(1, 8, 5).rev(2).protocol(47).resource(1),
            V_1_8_6 = ver(1, 8, 6).rev(2).protocol(47).resource(1),
            V_1_8_7 = ver(1, 8, 7).rev(2).protocol(47).resource(1),
            V_1_8_8 = ver(1, 8, 8).rev(3).protocol(47).resource(1),
            V_1_8_9 = ver(1, 8, 9).rev(3).protocol(47).resource(1);

    // Combat Update
    public static final MC
            V_1_9   = ver(1, 9).rev(1).data(169).protocol(107).resource(2),
            V_1_9_1 = ver(1, 9, 1).rev(1).data(175).protocol(108).resource(2),
            V_1_9_2 = ver(1, 9, 2).rev(1).data(176).protocol(109).resource(2),
            V_1_9_3 = ver(1, 9, 3).rev(2).data(183).protocol(110).resource(2),
            V_1_9_4 = ver(1, 9, 4).rev(2).data(184).protocol(110).resource(2);

    // Frostburn Update
    public static final MC
            V_1_10   = ver(1, 10).rev(1).data(510).protocol(210).resource(2),
            V_1_10_1 = ver(1, 10, 1).rev(1).data(511).protocol(210).resource(2),
            V_1_10_2 = ver(1, 10, 2).rev(1).data(512).protocol(210).resource(2);

    // Exploration Update
    public static final MC
            V_1_11   = ver(1, 11).rev(1).data(819).protocol(315).resource(3),
            V_1_11_1 = ver(1, 11, 1).rev(1).data(921).protocol(316).resource(3),
            V_1_11_2 = ver(1, 11, 2).rev(1).data(922).protocol(316).resource(3);

    // World of Color Update
    public static final MC
            V_1_12   = ver(1, 12).rev(1).data(1139).protocol(335).resource(3),
            V_1_12_1 = ver(1, 12, 1).rev(1).data(1241).protocol(338).resource(3, 3),
            V_1_12_2 = ver(1, 12, 2).rev(1).data(1343).protocol(340).resource(3, 3);

    // Aquatic Update
    public static final MC
            V_1_13   = ver(1, 13).rev(1).data(1519).protocol(393).resource(4, 4),
            V_1_13_1 = ver(1, 13, 1).rev(2).data(1628).protocol(401).resource(4, 4),
            V_1_13_2 = ver(1, 13, 2).rev(2).data(1631).protocol(404).resource(4, 4);

    // Village & Pillage, Texture Update
    public static final MC
            V_1_14   = ver(1, 14).rev(1).data(1952).protocol(477).resource(4, 4),
            V_1_14_1 = ver(1, 14, 1).rev(1).data(1957).protocol(480).resource(4, 4),
            V_1_14_2 = ver(1, 14, 2).rev(1).data(1963).protocol(485).resource(4, 4),
            V_1_14_3 = ver(1, 14, 3).rev(1).data(1968).protocol(490).resource(4, 4),
            V_1_14_4 = ver(1, 14, 4).rev(1).data(1976).protocol(498).resource(4, 4);

    // Buzzy Bees
    public static final MC
            V_1_15   = ver(1, 15).rev(1).data(2225).protocol(573).resource(5, 5),
            V_1_15_1 = ver(1, 15, 1).rev(1).data(2227).protocol(575).resource(5, 5),
            V_1_15_2 = ver(1, 15, 2).rev(1).data(2230).protocol(578).resource(5, 5);

    // Nether Update
    public static final MC
            V_1_16   = ver(1, 16).rev(1).data(2566).protocol(735).resource(5, 5),
            V_1_16_1 = ver(1, 16, 1).rev(1).data(2567).protocol(736).resource(5, 5),
            V_1_16_2 = ver(1, 16, 2).rev(2).data(2578).protocol(751).resource(6, 6),
            V_1_16_3 = ver(1, 16, 3).rev(2).data(2580).protocol(753).resource(6, 6),
            V_1_16_4 = ver(1, 16, 4).rev(3).data(2584).protocol(754).resource(6, 6),
            V_1_16_5 = ver(1, 16, 5).rev(3).data(2586).protocol(754).resource(6, 6);

    // Caves & Cliffs - Part I
    public static final MC
            V_1_17   = ver(1, 17).rev(1).data(2724).protocol(755).resource(7, 7),
            V_1_17_1 = ver(1, 17, 1).rev(1).data(2730).protocol(756).resource(7, 7);

    // Caves & Cliffs - Part 2
    public static final MC
            V_1_18   = ver(1, 18).rev(1).data(2860).protocol(757).resource(8, 8),
            V_1_18_1 = ver(1, 18, 1).rev(1).data(2866).protocol(757).resource(8, 8),
            V_1_18_2 = ver(1, 18, 2).rev(2).data(2975).protocol(758).resource(8, 9);

    // The Wild Update
    public static final MC
            V_1_19   = ver(1, 19).rev(1).data(3105).protocol(759).resource(9, 10),
            V_1_19_1 = ver(1, 19, 1).rev(1).data(3117).protocol(760).resource(9, 10),
            V_1_19_2 = ver(1, 19, 2).rev(1).data(3120).protocol(760).resource(9, 10),
            V_1_19_3 = ver(1, 19, 3).rev(2).data(3218).protocol(761).resource(12, 10),
            V_1_19_4 = ver(1, 19, 4).rev(3).data(3337).protocol(762).resource(13, 12);

    // Trails & Tales
    public static final MC
            V_1_20   = ver(1, 20).rev(1).data(3463).protocol(763).resource(15, 15),
            V_1_20_1 = ver(1, 20, 1).rev(1).data(3465).protocol(763).resource(15, 15),
            V_1_20_2 = ver(1, 20, 2).rev(2).data(3578).protocol(764).resource(18, 18);

    // Bats and Pots
    public static final MC
            V_1_20_3 = ver(1, 20, 3).rev(3).data(3698).protocol(765).resource(22, 26),
            V_1_20_4 = ver(1, 20, 4).rev(3).data(3700).protocol(765).resource(22, 26);

    // Armored Paws
    public static final MC
            V_1_20_5 = ver(1, 20, 5).rev(4).data(3837).protocol(766).resource(32, 41),
            V_1_20_6 = ver(1, 20, 6).rev(4).data(3839).protocol(766).resource(32, 41);

    // Tricky Trials
    public static final MC
            V_1_21   = ver(1, 21).rev(1).data(3953).protocol(767).resource(34, 48),
            V_1_21_1 = ver(1, 21, 1).rev(1).data(3955).protocol(767).resource(34, 48);

    // Bundles of Bravery
    public static final MC
            V_1_21_2 = ver(1, 21, 2).rev(2).data(4080).protocol(768).resource(42, 57),
            V_1_21_3 = ver(1, 21, 3).rev(2).data(4082).protocol(768).resource(42, 57);

    // The Garden Awakens
    public static final MC
            V_1_21_4 = ver(1, 21, 4).rev(3).data(4189).protocol(769).resource(46, 61);

    // Spring to Life
    public static final MC
            V_1_21_5 = ver(1, 21, 5).rev(4).data(4324).protocol(770).resource(55, 71);

    // Chase the Skies
    public static final MC
            V_1_21_6 = ver(1, 21, 6).rev(5).data(4435).protocol(771).resource(63, 80),
            V_1_21_7 = ver(1, 21, 7).rev(5).data(4438).protocol(772).resource(64, 81),
            V_1_21_8 = ver(1, 21, 8).rev(5).data(4440).protocol(772).resource(64, 81);

    // The Copper Age
    public static final MC
            V_1_21_9  = ver(1, 21, 9).rev(6).data(4554).protocol(773).resource(69.0f, 88.0f),
            V_1_21_10 = ver(1, 21, 10).rev(6).data(4556).protocol(773).resource(69.0f, 88.0f);

    @NotNull
    private static MC ver(int major, int feature) {
        return new MC(major, feature);
    }

    @NotNull
    private static MC ver(int major, int feature, int minor) {
        return new MC(major, feature, minor);
    }

    private static final List<MC> VALUES = new ArrayList<>();

    static {
        try {
            for (Field field : MC.class.getDeclaredFields()) {
                if (field.getName().startsWith("V_") && field.getType().equals(MC.class) && Modifier.isStatic(field.getModifiers())) {
                    VALUES.add((MC) field.get(null));
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static final MC VERSION;

    static {
        MC serverVersion = null;
        try {
            serverVersion = fromString(Bukkit.getServer().getBukkitVersion());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        // For some reason the server version cannot be parsed, so internal methods will be used
        if (serverVersion == null) {
            final String serverPackage = Bukkit.getServer().getClass().getPackage().getName();
            if (serverPackage.startsWith("org.bukkit.craftbukkit.v1_")) {
                // Bad accuracy
                serverVersion = findReverse(MC::bukkitPackage, serverPackage.split("\\.")[3]);
            } else {
                serverVersion = findReverse(MC::dataVersion, getDataVersion(serverPackage));
            }
        }
        VERSION = serverVersion;
    }

    private static int getDataVersion(@NotNull String serverPackage) {
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
     * Get current server version.
     *
     * @return the version that server is running on.
     */
    @NotNull
    public static MC version() {
        return VERSION;
    }

    /**
     * Get first supported version.
     *
     * @return the minimum supported version.
     */
    @NotNull
    public static MC first() {
        return VALUES.get(0);
    }

    /**
     * Get last supported version.
     *
     * @return the maximum supported version.
     */
    @NotNull
    public static MC last() {
        return VALUES.get(VALUES.size() - 1);
    }

    // - base
    private final int major;
    private final int feature;
    private final int minor;

    // - bukkit related
    private int revision;
    private float featRevision;
    private int fullRevision;
    private String bukkitPackage;

    // - minecraft related
    @SuppressWarnings("all")
    private Optional<Integer> dataVersion = Optional.empty();
    private int protocol;
    private float resourcePackFormat;
    @SuppressWarnings("all")
    private Optional<Float> dataPackFormat = Optional.empty();

    // - metadata
    private final int ordinal;
    private final String name;
    private final boolean legacy;
    private final boolean flat;
    private final boolean universal;
    private boolean component;

    MC(int major, int feature) {
        this(major, feature, 0);
    }

    MC(int major, int feature, int minor) {
        this.major = major;
        this.feature = feature;
        this.minor = minor;

        this.ordinal = ORDINAL.getAndIncrement();
        this.name = major + "." + feature + "." + minor;
        this.legacy = feature <= 12;
        this.flat = feature >= 13;
        this.universal = feature >= 17;
    }

    @NotNull
    @Contract("_ -> this")
    MC rev(int revision) {
        this.revision = revision;

        final String featureFormatted = this.feature >= 10 ? String.valueOf(this.feature) : "0" + this.feature;
        final String revisionFormatted = revision >= 10 ? String.valueOf(revision) : "0" + revision;

        this.featRevision = Float.parseFloat(this.feature + "." + revisionFormatted);
        this.fullRevision = Integer.parseInt(this.major + featureFormatted + revisionFormatted);
        this.bukkitPackage = "v" + this.major + "_" + this.feature + "_R" + revision;
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    MC data(int dataVersion) {
        this.dataVersion = Optional.of(dataVersion);

        this.component = dataVersion >= 3837;
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    MC protocol(int protocol) {
        this.protocol = protocol;
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    MC resource(float resourcePackFormat) {
        this.resourcePackFormat = resourcePackFormat;
        return this;
    }

    @NotNull
    @Contract("_, _ -> this")
    MC resource(float resourcePackFormat, float dataPackFormat) {
        this.resourcePackFormat = resourcePackFormat;
        this.dataPackFormat = Optional.of(dataPackFormat);
        return this;
    }

    /**
     * Get major version number.
     *
     * @return the major version number.
     */
    public int major() {
        return major;
    }

    /**
     * Get feature version number.
     *
     * @return the feature version number.
     */
    public int feature() {
        return feature;
    }

    /**
     * Get minor version number.
     *
     * @return the minor version number.
     */
    public int minor() {
        return minor;
    }

    /**
     * Get version revision number.<br>
     * Generally represents the "R" number in the craftbukkit package.
     *
     * @return the version revision number.
     */
    public int revision() {
        return revision;
    }

    /**
     * Get craftbukkit package version as float value, for example:<br>
     * v1_9_R2 -&gt; 9.02<br>
     * v1_13_R1 -&gt; 13.01<br>
     * v1_16_R3 -&gt; 16.03<br>
     *
     * @return the craftbukkit package version as float value.
     */
    @ApiStatus.Experimental
    public float featRevision() {
        return featRevision;
    }

    /**
     * Get craftbukkit package version as int value, for example:<br>
     * v1_9_R2 -&gt; 10902<br>
     * v1_13_R1 -&gt; 11301<br>
     * v1_19_R2 -&gt; 11902
     *
     * @return the craftbukkit package version as int value.
     */
    @ApiStatus.Experimental
    public int fullRevision() {
        return fullRevision;
    }

    /**
     * Get version as defined in craftbukkit package
     *
     * @return the craftbukkit package version string.
     */
    @NotNull
    @ApiStatus.Experimental
    public String bukkitPackage() {
        return bukkitPackage;
    }

    /**
     * Get data version number as optional value.<br>
     * On versions before 1.9 this value will be empty.
     *
     * @return the data version number.
     */
    @NotNull
    public Optional<Integer> dataVersion() {
        return dataVersion;
    }

    /**
     * Get protocol version number.
     *
     * @return the protocol version number.
     */
    public int protocol() {
        return protocol;
    }

    /**
     * Get resource pack format number.
     *
     * @return the resource pack format number.
     */
    public float resourcePackFormat() {
        return resourcePackFormat;
    }

    /**
     * Get data pack format number as optional value.<br>
     * On versions before 1.12.1 this value will be empty.
     *
     * @return the data pack format number.
     */
    @NotNull
    public Optional<Float> dataPackFormat() {
        return dataPackFormat;
    }

    /**
     * Get version name as string.
     *
     * @return the version name.
     */
    @NotNull
    private String name() {
        return name;
    }

    /**
     * Get version ordinal number (internal value).
     *
     * @return the version ordinal number.
     */
    private int ordinal() {
        return ordinal;
    }

    /**
     * Check if version is considered legacy (1.12.2 and below).
     *
     * @return true if version is legacy.
     */
    public boolean isLegacy() {
        return legacy;
    }

    /**
     * Check if version is considered flat (1.13 and above).
     *
     * @return true if version is flat.
     */
    public boolean isFlat() {
        return flat;
    }

    /**
     * Check if version is considered universal (1.17 and above).
     *
     * @return true if version is universal.
     */
    public boolean isUniversal() {
        return universal;
    }

    /**
     * Check if version includes data component-based architecture (1.20.5 and above).
     *
     * @return true if version includes data component-based architecture.
     */
    public boolean isComponent() {
        return component;
    }

    /**
     * Check if this version is older than the given version.
     *
     * @param version the version to compare.
     * @return        true if this version is older.
     */
    public boolean isOlderThan(@NotNull MC version) {
        return this.ordinal() < version.ordinal();
    }

    /**
     * Check if this version is older than or equals to the given version.
     *
     * @param version the version to compare.
     * @return        true if this version is older or equals.
     */
    public boolean isOlderThanOrEquals(@NotNull MC version) {
        return this.ordinal() <= version.ordinal();
    }

    /**
     * Check if this version is newer than the given version.
     *
     * @param version the version to compare.
     * @return        true if this version is newer.
     */
    public boolean isNewerThan(@NotNull MC version) {
        return this.ordinal() > version.ordinal();
    }

    /**
     * Check if this version is newer than or equals to the given version.
     *
     * @param version the version to compare.
     * @return        true if this version is newer or equals.
     */
    public boolean isNewerThanOrEquals(@NotNull MC version) {
        return this.ordinal() >= version.ordinal();
    }

    /**
     * Check if this version is between the given versions (inclusive).
     *
     * @param version1 the first version to compare.
     * @param version2 the second version to compare.
     * @return         true if this version is between.
     */
    public boolean isBetween(@NotNull MC version1, @NotNull MC version2) {
        return (this.ordinal() >= version1.ordinal() && this.ordinal() <= version2.ordinal()) || (this.ordinal() >= version2.ordinal() && this.ordinal() <= version1.ordinal());
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Get version from string representation.
     *
     * @param s the string representation of the version.
     * @return  the MC version or null if not found.
     */
    @Nullable
    @ApiStatus.Internal
    public static MC fromString(@NotNull String s) {
        final String[] split = s.replace("MC:", "").trim().split("\\.");
        if (split.length < 2) {
            return null;
        }
        final int major = Integer.parseInt(split[0]);
        final int feature;
        final int minor;
        if (split[1].contains("-") || split[1].contains("_")) {
            feature = Integer.parseInt(split[1].split("[-_]")[0]);
            minor = 0;
        } else {
            feature = Integer.parseInt(split[1]);
            minor = split.length > 2 ? Integer.parseInt(split[2].split("[-_]")[0]) : 0;
        }
        for (MC value : VALUES) {
            if (value.major == major && value.feature == feature && value.minor == minor) {
                return value;
            }
        }
        return null;
    }

    /**
     * Get version from any object representation.
     *
     * @param object the object representation of the version.
     * @return       the MC version or null if not found.
     */
    @Nullable
    @ApiStatus.Internal
    public static MC fromAny(@Nullable Object object) {
        if (object instanceof Integer) {
            final Integer integer = (Integer) object;
            if (integer >= 100) {
                return MC.findReverse(version -> version.dataVersion().orElse(null), integer);
            } else if (integer == 98) { // Backwards compatibility
                return V_1_8;
            } else {
                return MC.findReverse(MC::feature, integer);
            }
        } else if (object instanceof Number) {
            final float num = ((Number) object).floatValue();
            if (num % 1 >= 0.01f) {
                return MC.findReverse(MC::featRevision, num);
            } else {
                return MC.findReverse(MC::feature, (int) num);
            }
        } else if (object instanceof String) {
            return MC.fromString((String) object);
        } else {
            return null;
        }
    }

    /**
     * Get version from a specific getter function and target value.
     *
     * @param getter the function to get the value from each version.
     * @param t      the target value to match.
     * @param <T>    the type of the target value.
     * @return       the MC version or null if not found.
     */
    @Nullable
    @ApiStatus.Internal
    public static <T> MC find(@NotNull Function<@NotNull MC, @Nullable T> getter, @Nullable T t) {
        final boolean isNumber = t instanceof Number;
        for (MC value : VALUES) {
            final T obj = getter.apply(value);
            if (isNumber && obj instanceof Number) {
                if (((Number) obj).doubleValue() >= ((Number) t).doubleValue()) {
                    return value;
                }
            } else if (Objects.equals(t, getter.apply(value))) {
                return value;
            }
        }
        return null;
    }

    /**
     * Get version from a specific getter function and target value using a reverse lookup.
     *
     * @param getter the function to get the value from each version.
     * @param t      the target value to match.
     * @param <T>    the type of the target value.
     * @return       the MC version or null if not found.
     */
    @Nullable
    @ApiStatus.Internal
    public static <T> MC findReverse(@NotNull Function<@NotNull MC, @Nullable T> getter, @Nullable T t) {
        final boolean isNumber = t instanceof Number;
        for (int i = VALUES.size(); i-- > 0; ) {
            final MC value = VALUES.get(i);
            final T obj = getter.apply(value);
            if (isNumber && obj instanceof Number) {
                if (((Number) obj).doubleValue() <= ((Number) t).doubleValue()) {
                    return value;
                }
            } else if (Objects.equals(t, getter.apply(value))) {
                return value;
            }
        }
        return null;
    }

    @Override
    public int compareTo(@NotNull MC mc) {
        return Integer.compare(this.ordinal(), mc.ordinal());
    }

    /**
     * Compare two MC versions.
     *
     * @param version1 the first version to compare.
     * @param version2 the second version to compare.
     * @return         a negative integer, zero, or a positive integer as the first version is older than, equal to, or newer than the second.
     */
    @ApiStatus.Experimental
    public static int compare(@NotNull MC version1, @NotNull MC version2) {
        return Integer.compare(version1.ordinal(), version2.ordinal());
    }

    /**
     * Get the maximum of two MC versions.
     *
     * @param version1 the first version to compare.
     * @param version2 the second version to compare.
     * @return         the maximum version, or null if both are null.
     */
    @Nullable
    @Contract("null, !null -> param2; !null, null -> param1; null")
    @ApiStatus.Experimental
    public static MC max(@Nullable MC version1, @Nullable MC version2) {
        if (version1 == null) {
            return version2;
        } else if (version2 == null) {
            return version1;
        }
        return version1.ordinal() > version2.ordinal() ? version1 : version2;
    }

    /**
     * Get the minimum of two MC versions.
     *
     * @param version1 the first version to compare.
     * @param version2 the second version to compare.
     * @return         the minimum version, or null if both are null.
     */
    @Nullable
    @Contract("null, !null -> param2; !null, null -> param1")
    @ApiStatus.Experimental
    public static MC min(@Nullable MC version1, @Nullable MC version2) {
        if (version1 == null) {
            return version2;
        } else if (version2 == null) {
            return version1;
        }
        return version1.ordinal() < version2.ordinal() ? version1 : version2;
    }
}
