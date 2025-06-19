package com.saicone.rtag.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Simple utility class to get textured heads from:<br>
 * - Texture ID<br>
 * - Texture URL<br>
 * - Texture Base64<br>
 * - Player<br>
 * - Player name<br>
 * - Player UUID<br>
 *
 * @author Rubenicos
 */
@SuppressWarnings("deprecation")
public class SkullTexture {

    // Constants

    @Deprecated
    private static final String INVALID_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDZiYTYzMzQ0ZjQ5ZGQxYzRmNTQ4OGU5MjZiZjNkOWUyYjI5OTE2YTZjNTBkNjEwYmI0MGE1MjczZGM4YzgyIn19fQ==";
    @Deprecated
    private static final String LOADING_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzI0MzE5MTFmNDE3OGI0ZDJiNDEzYWE3ZjVjNzhhZTQ0NDdmZTkyNDY5NDNjMzFkZjMxMTYzYzBlMDQzZTBkNiJ9fX0=";

    private static final String TEXTURE_URL = "http://textures.minecraft.net/texture/";

    static {
        // Add reflected classes
        try {
            EasyLookup.addOBCClass("entity.CraftPlayer");
            EasyLookup.addOBCClass("inventory.CraftMetaSkull");
            if (ServerInstance.Release.COMPONENT) {
                EasyLookup.addNMSClass("world.item.component.ResolvableProfile");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Reflected methods

    private static final MethodHandle NEW_PROFILE;
    private static final MethodHandle GET_PROFILE;
    private static final MethodHandle SET_PROFILE;
    private static final MethodHandle GET_VALUE;
    private static final MethodHandle GET_SIGNATURE;

    static {
        MethodHandle new$ResolvableProfile = null;
        MethodHandle get$profile = null;
        MethodHandle set$profile = null;
        MethodHandle get$value = null;
        MethodHandle get$signature = null;
        try {
            if (ServerInstance.Release.COMPONENT) {
                for (Method method : EasyLookup.classOf("CraftMetaSkull").getDeclaredMethods()) {
                    if (method.getName().equals("setProfile") && method.getParameters().length == 1) {
                        if (method.getParameters()[0].getType().getSimpleName().equals("ResolvableProfile")) {
                            new$ResolvableProfile = EasyLookup.constructor("ResolvableProfile", GameProfile.class);
                        }
                    }
                }
            }

            get$profile = EasyLookup.method("CraftPlayer", "getProfile", GameProfile.class);

            // Unreflect reason:
            // Private method/field
            if (new$ResolvableProfile != null) {
                set$profile = EasyLookup.unreflectMethod("CraftMetaSkull", "setProfile", "ResolvableProfile");
            } else if (ServerInstance.MAJOR_VERSION >= 15) {
                set$profile = EasyLookup.unreflectMethod("CraftMetaSkull", "setProfile", GameProfile.class);
            } else {
                set$profile = EasyLookup.unreflectSetter("CraftMetaSkull", "profile");
            }

            String value = "value";
            for (Method method : Property.class.getDeclaredMethods()) {
                if (method.getName().equals("getValue")) {
                    // Old name found
                    value = "getValue";
                    break;
                }
            }
            get$value = EasyLookup.method(Property.class, value, String.class);

            String signature = "signature";
            for (Method method : Property.class.getDeclaredMethods()) {
                if (method.getName().equals("getSignature")) {
                    // Old name found
                    signature = "getSignature";
                    break;
                }
            }
            get$signature = EasyLookup.method(Property.class, signature, String.class);
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        NEW_PROFILE = new$ResolvableProfile;
        GET_PROFILE = get$profile;
        SET_PROFILE = set$profile;
        GET_VALUE = get$value;
        GET_SIGNATURE = get$signature;
    }

    // Providers

    private static final JsonParser JSON_PARSER = new JsonParser();
    private static final Supplier<ItemStack> PLAYER_HEAD = () -> {
        if (ServerInstance.Release.FLAT) {
            return new ItemStack(Material.PLAYER_HEAD);
        } else {
            return new ItemStack(Material.getMaterial("SKULL_ITEM"), 1, (short) 3);
        }
    };

    // Instances

    /**
     * Get a SkullTexture instance that retrieves texture values from Mojang API
     * by providing a player name or unique id.
     *
     * @return a SkullTexture instance that use Mojang API.
     */
    public static SkullTexture mojang() {
        return Mojang.INSTANCE;
    }

    /**
     * Get a SkullTexture instance that retrieves texture values from PlayerDB API
     * by providing a player name or unique id.
     *
     * @return a SkullTexture instance that use PlayerDB API.
     */
    public static SkullTexture playerDB() {
        return PlayerDB.INSTANCE;
    }

    /**
     * Get a SkullTexture instance that retrieves texture values from CraftHead API
     * by providing a player name or unique id.
     *
     * @return a SkullTexture instance that use PlayerDB API.
     */
    public static SkullTexture craftHead() {
        return CraftHead.INSTANCE;
    }

    // The class itself

    /**
     * Cache object to save profiles.
     */
    protected final Cache<String, Profile> cache;
    /**
     * Default executor to use on async operations.
     */
    protected final Executor executor;

    /**
     * Constructs a SkullTexture instance with default parameters.
     */
    public SkullTexture() {
        this(CacheBuilder.newBuilder().expireAfterAccess(3, TimeUnit.HOURS).build());
    }

    /**
     * Constructs a SkullTexture instance with provided cache object.
     *
     * @param cache the cache to save profiles.
     */
    public SkullTexture(@Nullable Cache<String, Profile> cache) {
        this(cache, CompletableFuture.completedFuture(null).defaultExecutor());
    }

    /**
     * Constructs a SkullTexture instance with provided executor.
     *
     * @param executor the default executor to use in async operations.
     */
    public SkullTexture(@NotNull Executor executor) {
        this(CacheBuilder.newBuilder().expireAfterAccess(3, TimeUnit.HOURS).build(), executor);
    }

    /**
     * Constructs a SkullTexture instance with provided cache object and executor.
     *
     * @param cache    the cache to save profiles.
     * @param executor the default executor to use in async operations.
     */
    public SkullTexture(@Nullable Cache<String, Profile> cache, @NotNull Executor executor) {
        this.cache = cache;
        this.executor = executor;
    }

    /**
     * Return profiles value from cache or save it by supplier.<br>
     * If supplied value is null, this method will cache the {@link Profile#empty()} object.
     *
     * @param key      the key to get texture from cache.
     * @param supplier the supplier that provide a non-cached value.
     * @return         a cached profiles value, {@link Profile#empty()} if supplier return a null object.
     */
    @NotNull
    protected Profile caching(@NotNull Object key, @NotNull Supplier<Profile> supplier) {
        if (this.cache == null) {
            return supplier.get();
        }
        Profile value = this.cache.getIfPresent(String.valueOf(key));
        if (value == null) {
            try {
                value = supplier.get();
            } catch (Throwable ignored) { } // Safe profile getter
            if (value == null) {
                value = Profile.empty();
            }
            this.cache.put(String.valueOf(key), value);
        }
        return value;
    }

    /**
     * Get a player head with provided texture value.<br>
     * This method may fetch textures using external APIs,
     * use it in non-async operations if you know what are you doing.
     *
     * @param object   the object to convert into encoded texture, or the encoded texture itself.
     * @return         a player head with provided texture value if found, normal head otherwise.
     */
    @NotNull
    public ItemStack item(@NotNull Object object) {
        return setProfile(PLAYER_HEAD.get(), profileFrom(object));
    }

    /**
     * Get a player head asynchronously with provided texture value.<br>
     * If encoded texture is already cached, a completed future will be return.
     *
     * @param object   the object to convert into encoded texture, or the encoded texture itself.
     * @return         a completable future containing player head with provided texture value if found, normal head otherwise.
     */
    @NotNull
    public CompletableFuture<ItemStack> itemAsync(@NotNull Object object) {
        return itemAsync(object, this.executor);
    }

    /**
     * Get a player head asynchronously with provided texture value and executor.<br>
     * If encoded texture is already cached, a completed future will be return.
     *
     * @param object   the object to convert into encoded texture, or the encoded texture itself.
     * @param executor the executor to use for asynchronous execution
     * @return         a completable future containing player head with provided texture value if found, normal head otherwise.
     */
    @NotNull
    public CompletableFuture<ItemStack> itemAsync(@NotNull Object object, @NotNull Executor executor) {
        final Object key;
        if (object instanceof Player) {
            key = ((Player) object).getUniqueId();
        } else {
            key = object;
        }
        final Profile profile = this.cache.getIfPresent(String.valueOf(key));
        if (profile != null) {
            return CompletableFuture.completedFuture(setProfile(PLAYER_HEAD.get(), profile));
        }
        return CompletableFuture.supplyAsync(() -> setProfile(PLAYER_HEAD.get(), profileFrom(object)), executor);
    }

    /**
     * Get profile value from any supported object.<br>
     * This method automatically caches the result to reduce request times.<br>
     * Instead of other profile-getting methods, this method ignores any exception.
     *
     * @param object the object to convert into profile value, or the profile value itself.
     * @return       a profile value if found and any error occurs, {@link Profile#empty()} otherwise.
     */
    @NotNull
    public Profile profileFrom(@NotNull Object object) {
        if (object instanceof Profile) {
            return caching(((Profile) object).getUniqueId(), () -> (Profile) object);
        } else if (object instanceof Player) {
            return caching(((Player) object).getUniqueId(), () -> profileFromPlayer((Player) object));
        } else if (object instanceof UUID) {
            return caching(object, () -> profileFromId((UUID) object));
        } else {
            final String value = String.valueOf(object);
            return caching(value, () -> {
                if (value.length() < 32) {
                    return profileFromName(value);
                } else if (value.length() == 32 || value.length() == 36) {
                    return profileFromId(value);
                } else if (value.length() == 64) {
                    return Profile.valueOf(encodeUrlId(value));
                } else if (value.startsWith("http")) {
                    return Profile.valueOf(encodeUrl(value));
                } else {
                    return Profile.valueOf(value);
                }
            });
        }
    }

    /**
     * Get profile value from online player.
     *
     * @param player the online player.
     * @return       a profile value, {@link Profile#empty()} if player texture cannot be found.
     */
    @NotNull
    public Profile profileFromPlayer(@Nullable Player player) {
        return getProfile(player);
    }

    /**
     * Get profile value from player name.
     *
     * @param name the player name.
     * @return     a profile value, {@link Profile#empty()} if player texture cannot be found.
     */
    @NotNull
    public Profile profileFromName(@NotNull String name) {
        final Profile profile = profileFromPlayer(Bukkit.getPlayer(name));
        return profile.isEmpty() || profile.isOffline() ? fetchProfile(name) : profile;
    }

    /**
     * Get profile value from player unique id.<br>
     * This method accept raw ids without dashes.
     *
     * @param uniqueId the player unique id.
     * @return         a profile value, {@link Profile#empty()} if player texture cannot be found.
     */
    @NotNull
    public Profile profileFromId(@NotNull String uniqueId) {
        if (uniqueId.length() == 32) {
            return profileFromId(UUID.fromString(new StringBuilder(uniqueId)
                    .insert(20, '-').insert(16, '-').insert(12, '-').insert(8, '-')
                    .toString()));
        } else {
            return profileFromId(UUID.fromString(uniqueId));
        }
    }

    /**
     * Get profile value from player unique id.
     *
     * @param uniqueId the player unique id.
     * @return         a profile value, {@link Profile#empty()} if player texture cannot be found.
     */
    @NotNull
    public Profile profileFromId(@NotNull UUID uniqueId) {
        final Profile profile = profileFromPlayer(Bukkit.getPlayer(uniqueId));
        return profile.isEmpty() || profile.isOffline() ? fetchProfile(uniqueId) : profile;
    }

    /**
     * Encode provided texture url into minecraft profile property format.
     *
     * @param url the url value to encode.
     * @return    an encoded texture value.
     */
    @NotNull
    public String encodeUrl(@NotNull String url) {
        return new String(Base64.getEncoder().encode(("{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}").getBytes()));
    }

    /**
     * Get encoded texture value from minecraft texture ID.
     *
     * @param id the minecraft texture ID.
     * @return   an encoded texture value.
     */
    @NotNull
    public String encodeUrlId(@NotNull String id) {
        return encodeUrl(TEXTURE_URL + id);
    }

    /**
     * Get url data as json object.
     *
     * @param url the url to connect.
     * @return    an optional json object that represent the url data.
     */
    @NotNull
    protected Optional<JsonObject> fetchJson(@NotNull String url) {
        // Only compatible with Java +9
        // Older Java versions require a more complex implementation using URL connection with header, and input stream reader
        try (InputStream stream = new URL(url).openStream()) {
            final String content = new String(stream.readAllBytes());
            if (content.isBlank()) {
                return Optional.empty();
            }
            final JsonObject json = JSON_PARSER.parse(content).getAsJsonObject();
            return Optional.of(json);
        } catch (IOException e) {
            throw new RuntimeException("Cannot read json from url", e);
        }
    }

    /**
     * Fetch profile value using player name.
     *
     * @param name the player name to find.
     * @return     a profile value if found, {@link Profile#empty()} otherwise.
     */
    @NotNull
    public Profile fetchProfile(@NotNull String name) {
        throw new IllegalStateException("Current SkullTexture instance doesn't provide texture lookup using player name");
    }

    /**
     * Fetch profile value using player unique id.
     *
     * @param uniqueId the unique id to find.
     * @return         a profile value if found, {@link Profile#empty()} otherwise.
     */
    @NotNull
    public Profile fetchProfile(@NotNull UUID uniqueId) {
        throw new IllegalStateException("Current SkullTexture instance doesn't provide texture lookup using player id");
    }

    /**
     * Parse profile session object as profile value.
     *
     * @param session the json object representation of player session.
     * @param idKey   the key containing raw unique id.
     * @param nameKey the key containing player name.
     * @return        a newly generated profile value.
     */
    @NotNull
    public Profile profileSession(@NotNull JsonObject session, @NotNull String idKey, @NotNull String nameKey) {
        final JsonPrimitive id = session.getAsJsonPrimitive(idKey);
        final JsonPrimitive name = session.getAsJsonPrimitive(nameKey);

        final UUID uniqueId;
        if (id == null || id.isJsonNull()) {
            uniqueId = Profile.EMPTY_ID;
        } else {
            uniqueId = UUID.fromString(new StringBuilder(id.getAsString())
                    .insert(20, '-')
                    .insert(16, '-')
                    .insert(12, '-')
                    .insert(8, '-')
                    .toString());
        }

        final JsonArray properties = session.getAsJsonArray("properties");
        if (properties != null) {
            for (JsonElement element : properties) {
                final JsonObject property = element.getAsJsonObject();
                if (property.getAsJsonPrimitive("name").getAsString().equalsIgnoreCase("textures")) {
                    final JsonPrimitive value = property.getAsJsonPrimitive("value");
                    final JsonPrimitive signature = property.getAsJsonPrimitive("signature");
                    return Profile.valueOf(
                            uniqueId,
                            name != null ? name.getAsString() : "null",
                            value != null ? value.getAsString() : null,
                            signature != null ? signature.getAsString() : null
                    );
                }
            }
        }

        return Profile.valueOf(uniqueId, name != null ? name.getAsString() : "null");
    }

    // Static methods

    /**
     * Set encoded texture value into skull meta.
     *
     * @param head    skull item to set the texture.
     * @param texture encoded texture value.
     * @return        the provided item.
     * @throws IllegalArgumentException if the provided item isn't a player head.
     */
    @NotNull
    @Contract("_, _ -> param1")
    public static ItemStack setTexture(@NotNull ItemStack head, @Nullable String texture) throws IllegalArgumentException {
        if (texture == null) {
            return head;
        }
        return setProfile(head, Profile.valueOf(texture));
    }

    /**
     * Set game profile value into skull meta.
     *
     * @param head    skull item to set the profile.
     * @param profile profile value.
     * @return        the provided item.
     * @throws IllegalArgumentException if the provided item isn't a player head.
     */
    @NotNull
    @Contract("_, _ -> param1")
    public static ItemStack setProfile(@NotNull ItemStack head, @NotNull Profile profile) throws IllegalArgumentException {
        if (profile.isEmpty()) {
            return head;
        }
        final ItemMeta meta = head.getItemMeta();
        if (!(meta instanceof SkullMeta)) {
            throw new IllegalArgumentException("The provided item isn't a player head");
        }
        try {
            if (NEW_PROFILE != null) {
                SET_PROFILE.invoke(meta, NEW_PROFILE.invoke(profile.getProfile()));
            } else {
                SET_PROFILE.invoke(meta, profile.getProfile());
            }
        } catch (Throwable t) {
            throw new RuntimeException("Cannot set profile value to ItemStack", t);
        }
        head.setItemMeta(meta);
        return head;
    }

    /**
     * Get profile value from online player.
     *
     * @param player the player to get the profile from.
     * @return       a profile value, {@link Profile#empty()} if player is null.
     */
    @NotNull
    public static Profile getProfile(@Nullable Player player) {
        if (player == null) {
            return Profile.empty();
        }
        try {
            return new Profile((GameProfile) GET_PROFILE.invoke(player));
        } catch (Throwable t) {
            throw new RuntimeException("Cannot get online player profile from '" + player.getName() + "'", t);
        }
    }

    // Deprecated/old methods

    /**
     * Main method to get textured head and save into cache.
     *
     * @deprecated use {@link SkullTexture#mojang()}{@code .}{@link SkullTexture#item(Object)} instead.
     *
     * @param texture texture ID, URL, Base64, Player name or UUID.
     * @return        a ItemStack that represent the textured head.
     */
    @Deprecated
    public static ItemStack getTexturedHead(String texture) {
        return setTexture(PLAYER_HEAD.get(), getTextureValue(texture));
    }

    /**
     * Main method to get textured head and save into cache.
     *
     * @deprecated use {@link SkullTexture#mojang()}{@code .}{@link SkullTexture#itemAsync(Object)} instead.
     *
     * @param texture  texture ID, URL, Base64, Player name or UUID.
     * @param callback function to execute if textured head is retrieved in async operation.
     * @return         a ItemStack that represent the textured head.
     */
    @Deprecated
    public static ItemStack getTexturedHead(String texture, Consumer<ItemStack> callback) {
        if (callback == null) {
            return getTexturedHead(texture);
        }
        return setTexture(PLAYER_HEAD.get(), getTextureValue(texture, value -> callback.accept(setTexture(PLAYER_HEAD.get(), value))));
    }

    /**
     * Get Base64 encoded texture from the given texture parameter,
     * can be player name, player uuid, texture id, url or base64.
     *
     * @deprecated use {@link SkullTexture#mojang()}{@code .}{@link SkullTexture#profileFrom(Object)} instead.
     *
     * @param texture texture type.
     * @return        a Base64 encoded text.
     */
    @Deprecated
    public static String getTextureValue(String texture) {
        return getTextureValue(texture, null);
    }

    /**
     * Get Base64 encoded texture from the given texture parameter,
     * can be player name, player uuid, texture id, url or base64.
     *
     * @deprecated use {@link SkullTexture#mojang()}{@code .}{@link SkullTexture#profileFrom(Object)} instead.
     *
     * @param texture  texture type.
     * @param callback function to execute if texture value is retrieved in async operation.
     * @return         a Base64 encoded text.
     */
    @Deprecated
    public static String getTextureValue(String texture, Consumer<String> callback) {
        if (texture.length() <= 20 || texture.length() == 36) {
            final Profile profile = mojang().cache.getIfPresent(texture);
            if (profile == null) {
                mojang().cache.put(texture, Profile.valueOf(LOADING_TEXTURE));
                CompletableFuture.supplyAsync(() -> {
                    if (texture.length() == 36) {
                        return mojang().profileFromId(texture);
                    } else {
                        return mojang().profileFromName(texture);
                    }
                }).thenAccept(result -> {
                    if (!result.isEmpty()) {
                        mojang().cache.put(texture, result);
                        if (callback != null) {
                            result.getTexture().ifPresent(callback);
                        }
                    }
                });
            }
            return LOADING_TEXTURE;
        }
        return mojang().profileFrom(texture).getTexture().orElse(null);
    }

    /**
     * Compute textured head via making a request to Mojang API,
     * it's suggested to call this method in async environment.
     *
     * @deprecated use {@link SkullTexture#mojang()}{@code .}{@link SkullTexture#fetchProfile(String)} instead.
     *
     * @param name the player name.
     * @return     a Base64 encoded text.
     */
    @Deprecated
    public static String computePlayerTexture(@NotNull String name) {
        return computePlayerTexture(name, name);
    }

    /**
     * Compute textured head via making a request to Mojang API,
     * it's suggested to call this method in async environment.
     *
     * @deprecated use {@link SkullTexture#mojang()}{@code .}{@link SkullTexture#fetchProfile(String)} instead.
     *
     * @param key  map key to put.
     * @param name the player name.
     * @return     a Base64 encoded text.
     */
    @Deprecated
    public static String computePlayerTexture(@NotNull String key, @NotNull String name) {
        String texture = requestTextureUrl(name);
        if (texture != null) {
            texture = mojang().encodeUrl(texture);
            mojang().cache.put(key, Profile.valueOf(texture));
            return texture;
        } else {
            mojang().cache.put(key, Profile.valueOf(INVALID_TEXTURE));
            return INVALID_TEXTURE;
        }
    }

    /**
     * Request player texture url using Mojang API.
     *
     * @deprecated use {@link SkullTexture#mojang()}{@code .}{@link SkullTexture#fetchProfile(String)} instead.
     *
     * @param name the player name.
     * @return     a Mojang texture url if the player profile exists, null otherwise.
     */
    @Deprecated
    public static String requestTextureUrl(@NotNull String name) {
        final Profile profile = mojang().fetchProfile(name);
        if (profile == null) {
            return null;
        }
        return profile.getTexture().orElse(null);
    }

    /**
     * Abstract implementation of game profile, mainly focused on immutable texture-related data.
     */
    public static class Profile {

        private static final UUID EMPTY_ID = new UUID(0, 0);
        private static final Profile EMPTY = createProfile(EMPTY_ID, "null", null, null);

        private final GameProfile profile;

        /**
         * Get empty profile representation value.<br>
         * This profile will always return {@code false} on {@link Profile#isValid()}.
         *
         * @return an empty profile.
         */
        @NotNull
        public static Profile empty() {
            return EMPTY;
        }

        /**
         * Create a profile value with provided parameters.<br>
         * This profile will always return {@code false} on {@link Profile#isValid()}.
         *
         * @param texture a base64-encoded texture value.
         * @return        a newly generated profile value.
         */
        @NotNull
        public static Profile valueOf(@Nullable String texture) {
            return valueOf(EMPTY_ID, "null", texture);
        }

        /**
         * Create a profile value with provided parameters.
         *
         * @param uniqueId the player id.
         * @param name     the player name.
         * @return         a newly generated profile value.
         */
        @NotNull
        public static Profile valueOf(@NotNull UUID uniqueId, @NotNull String name) {
            return valueOf(uniqueId, name, null, null);
        }

        /**
         * Create a profile value with provided parameters.
         *
         * @param uniqueId the player id.
         * @param name     the player name.
         * @param texture  a base64-encoded texture value.
         * @return         a newly generated profile value.
         */
        @NotNull
        public static Profile valueOf(@NotNull UUID uniqueId, @NotNull String name, @Nullable String texture) {
            return valueOf(uniqueId, name, texture, null);
        }

        /**
         * Create a profile value with provided parameters.
         *
         * @param uniqueId  the player id.
         * @param name      the player name.
         * @param texture   a base64-encoded texture value.
         * @param signature a signature for texture value.
         * @return          a newly generated profile value.
         */
        @NotNull
        public static Profile valueOf(@NotNull UUID uniqueId, @NotNull String name, @Nullable String texture, @Nullable String signature) {
            if (uniqueId.equals(EMPTY_ID) && name.equals("null") && texture == null) {
                return empty();
            }
            return createProfile(uniqueId, name, texture, signature);
        }

        @NotNull
        private static Profile createProfile(@NotNull UUID uniqueId, @NotNull String name, @Nullable String texture, @Nullable String signature) {
            final GameProfile profile = new GameProfile(uniqueId, name);
            if (texture != null) {
                profile.getProperties().put("textures", new Property("textures", texture, signature));
            }
            return new Profile(profile);
        }

        private Profile(@NotNull GameProfile profile) {
            this.profile = profile;
        }

        /**
         * Check if this profile correspond to a randomly-generate unique id, normally used on MC Java accounts.
         *
         * @return true if the current profile id is version 4.
         */
        public boolean isJava() {
            return getUniqueId().version() == 4;
        }

        /**
         * Check if this profile correspond to a name-generated unique id, normally used on offline Java accounts.
         *
         * @return true if the current profile id is version 3.
         */
        public boolean isOffline() {
            return getUniqueId().version() == 3;
        }

        /**
         * Check if this profile correspond to a Bedrock account.
         *
         * @return true if the current profile id is an xbox id.
         */
        public boolean isBedrock() {
            return getUniqueId().toString().startsWith("00000000-0000-0000-0009");
        }

        /**
         * Check if this profile is provided by a real id.
         *
         * @return true if this profile is valid.
         */
        public boolean isValid() {
            return !getUniqueId().equals(EMPTY_ID);
        }

        /**
         * Check if this profile doesn't contain a texture value.
         *
         * @return true if this profiles is empty.
         */
        public boolean isEmpty() {
            return profile.getProperties().isEmpty();
        }

        @NotNull
        private GameProfile getProfile() {
            return profile;
        }

        /**
         * Gets the unique ID of this profile.
         *
         * @return an unique id.
         */
        @NotNull
        public UUID getUniqueId() {
            return profile.getId();
        }

        /**
         * Gets the display name of this profile.
         *
         * @return a player name.
         */
        @NotNull
        public String getName() {
            return profile.getName();
        }

        /**
         * Gets the texture value of this profile.
         *
         * @return an optional base64-encoded texture value.
         */
        @NotNull
        public Optional<String> getTexture() {
            for (Property texture : profile.getProperties().get("textures")) {
                if (texture != null) {
                    try {
                        return Optional.ofNullable((String) GET_VALUE.invoke(texture));
                    } catch (Throwable t) {
                        throw new RuntimeException("Cannot get texture value from Property object");
                    }
                }
            }
            return Optional.empty();
        }

        /**
         * Gets the json representation of this profile texture value.
         *
         * @return an optional json texture value.
         */
        @NotNull
        public Optional<JsonObject> getTextureJson() {
            return getTexture().map(base64 -> {
                String value;
                try {
                    value = new String(Base64.getDecoder().decode(base64));
                } catch (IllegalArgumentException e) {
                    // Already decoded
                    value = base64;
                }
                return JSON_PARSER.parse(value).getAsJsonObject();
            });
        }

        /**
         * Gets the signature of this profile texture value.
         *
         * @return an optional signature value.
         */
        @NotNull
        public Optional<String> getSignature() {
            for (Property texture : profile.getProperties().get("textures")) {
                if (texture != null) {
                    try {
                        return Optional.ofNullable((String) GET_SIGNATURE.invoke(texture));
                    } catch (Throwable t) {
                        throw new RuntimeException("Cannot get texture signature from Property object");
                    }
                }
            }
            return Optional.empty();
        }

        /**
         * Gets the skin url of this profile texture value.
         *
         * @return an optional extracted url from texture value.
         */
        @NotNull
        public Optional<URL> getSkinUrl() {
            return getTextureJson().map(json -> {
                final JsonObject textures = json.getAsJsonObject("textures");
                if (textures != null) {
                    final JsonObject skin = textures.getAsJsonObject("SKIN");
                    if (skin != null) {
                        final JsonPrimitive url = skin.getAsJsonPrimitive("url");
                        if (url != null) {
                            try {
                                return new URL(url.getAsString());
                            } catch (MalformedURLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
                return null;
            });
        }

        /**
         * Gets the cape url of this profile texture value.
         *
         * @return an optional extracted url from texture value.
         */
        @NotNull
        public Optional<URL> getCapeUrl() {
            return getTextureJson().map(json -> {
                final JsonObject textures = json.getAsJsonObject("textures");
                if (textures != null) {
                    final JsonObject skin = textures.getAsJsonObject("CAPE");
                    if (skin != null) {
                        final JsonPrimitive url = skin.getAsJsonPrimitive("url");
                        if (url != null) {
                            try {
                                return new URL(url.getAsString());
                            } catch (MalformedURLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
                return null;
            });
        }
    }

    /**
     * Mojang SkullTexture implementation that retrieves profiles using Mojang API.
     */
    @ApiStatus.Experimental
    public static class Mojang extends SkullTexture {

        private static final Mojang INSTANCE = new Mojang();

        private static final String USER_API = "https://api.mojang.com/users/profiles/minecraft/";
        private static final String SESSION_API = "https://sessionserver.mojang.com/session/minecraft/profile/";

        /**
         * Constructs a SkullTexture instance with default parameters.
         */
        public Mojang() {
            super();
        }

        /**
         * Constructs a SkullTexture instance with provided cache object.
         *
         * @param cache the cache to save encoded textures.
         */
        public Mojang(@Nullable Cache<String, Profile> cache) {
            super(cache);
        }

        /**
         * Constructs a SkullTexture instance with provided executor.
         *
         * @param executor the default executor to use in async operations.
         */
        public Mojang(@NotNull Executor executor) {
            super(executor);
        }

        /**
         * Constructs a SkullTexture instance with provided cache object and executor.
         *
         * @param cache    the cache to save encoded textures.
         * @param executor the default executor to use in async operations.
         */
        public Mojang(@Nullable Cache<String, Profile> cache, @NotNull Executor executor) {
            super(cache, executor);
        }

        @Override
        public @NotNull Profile fetchProfile(@NotNull String name) {
            return fetchJson(USER_API + name).flatMap(user -> {
                final JsonPrimitive id = user.getAsJsonPrimitive("id");
                if (id != null) {
                    return fetchJson(SESSION_API + id.getAsString()).map(session -> profileSession(session, "id", "name"));
                }
                return Optional.empty();
            }).orElse(Profile.empty());
        }

        @Override
        public @NotNull Profile fetchProfile(@NotNull UUID uniqueId) {
            return fetchJson(SESSION_API + uniqueId.toString().replace('-', '\0')).map(session ->
                    profileSession(session, "id", "name")
            ).orElse(Profile.empty());
        }
    }

    /**
     * PlayerDB SkullTexture implementation that retrieves profiles using PlayerDB API.
     */
    @ApiStatus.Experimental
    public static class PlayerDB extends SkullTexture {

        private static final PlayerDB INSTANCE = new PlayerDB();

        private static final String API = "https://playerdb.co/api/player/minecraft/";

        /**
         * Constructs a SkullTexture instance with default parameters.
         */
        public PlayerDB() {
            super();
        }

        /**
         * Constructs a SkullTexture instance with provided cache object.
         *
         * @param cache the cache to save encoded textures.
         */
        public PlayerDB(@Nullable Cache<String, Profile> cache) {
            super(cache);
        }

        /**
         * Constructs a SkullTexture instance with provided executor.
         *
         * @param executor the default executor to use in async operations.
         */
        public PlayerDB(@NotNull Executor executor) {
            super(executor);
        }

        /**
         * Constructs a SkullTexture instance with provided cache object and executor.
         *
         * @param cache    the cache to save encoded textures.
         * @param executor the default executor to use in async operations.
         */
        public PlayerDB(@Nullable Cache<String, Profile> cache, @NotNull Executor executor) {
            super(cache, executor);
        }

        @Override
        public @NotNull Profile fetchProfile(@NotNull String name) {
            return fetchAny(name);
        }

        @Override
        public @NotNull Profile fetchProfile(@NotNull UUID uniqueId) {
            return fetchAny(uniqueId.toString());
        }

        /**
         * Fetch player profile using player name or id.
         *
         * @param any the name or id to find.
         * @return    a profile value if found, {@link Profile#empty()} otherwise.
         */
        @NotNull
        protected Profile fetchAny(@NotNull String any) {
            return fetchJson(API + any).map(json -> {
                final JsonObject data = json.getAsJsonObject("data");
                if (data != null) {
                    final JsonObject player = data.getAsJsonObject("player");
                    if (player != null) {
                        return profileSession(player, "raw_id", "username");
                    }
                }
                return null;
            }).orElse(Profile.empty());
        }
    }

    /**
     * CraftHead SkullTexture implementation that retrieves profiles using CraftHead API.
     */
    @ApiStatus.Experimental
    public static class CraftHead extends SkullTexture {

        private static final CraftHead INSTANCE = new CraftHead();

        private static final String API = "https://crafthead.net/profile/";

        /**
         * Constructs a SkullTexture instance with default parameters.
         */
        public CraftHead() {
            super();
        }

        /**
         * Constructs a SkullTexture instance with provided cache object.
         *
         * @param cache the cache to save encoded textures.
         */
        public CraftHead(@Nullable Cache<String, Profile> cache) {
            super(cache);
        }

        /**
         * Constructs a SkullTexture instance with provided executor.
         *
         * @param executor the default executor to use in async operations.
         */
        public CraftHead(@NotNull Executor executor) {
            super(executor);
        }

        /**
         * Constructs a SkullTexture instance with provided cache object and executor.
         *
         * @param cache    the cache to save encoded textures.
         * @param executor the default executor to use in async operations.
         */
        public CraftHead(@Nullable Cache<String, Profile> cache, @NotNull Executor executor) {
            super(cache, executor);
        }

        @Override
        public @NotNull Profile fetchProfile(@NotNull String name) {
            return fetchJson(API + name).map(session -> profileSession(session, "id", "name")).orElse(Profile.empty());
        }

        @Override
        public @NotNull Profile fetchProfile(@NotNull UUID uniqueId) {
            return fetchJson(API + uniqueId).map(session -> profileSession(session, "id", "name")).orElse(Profile.empty());
        }
    }
}
