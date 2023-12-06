package com.saicone.rtag.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Very simple class to get textured heads from:<br>
 * - Texture ID<br>
 * - Texture URL<br>
 * - Texture Base64<br>
 * - Player name<br>
 * - Player UUID<br>
 *
 * The main plan for this class was making textured heads by
 * edit item NBTTagCompound, but it's stupid because with
 * single reflected method is possible.
 *
 * @author Rubenicos
 */
@SuppressWarnings("deprecation")
public class SkullTexture {

    private static final String USER_API = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String SESSION_API = "https://sessionserver.mojang.com/session/minecraft/profile/";

    private static final String INVALID_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDZiYTYzMzQ0ZjQ5ZGQxYzRmNTQ4OGU5MjZiZjNkOWUyYjI5OTE2YTZjNTBkNjEwYmI0MGE1MjczZGM4YzgyIn19fQ==";
    private static final String LOADING_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzI0MzE5MTFmNDE3OGI0ZDJiNDEzYWE3ZjVjNzhhZTQ0NDdmZTkyNDY5NDNjMzFkZjMxMTYzYzBlMDQzZTBkNiJ9fX0=";

    private static final ItemStack PLAYER_HEAD;

    private static final MethodHandle getProfile;
    private static final MethodHandle setProfile;

    private static final Cache<String, String> TEXTURE_CACHE = CacheBuilder.newBuilder().expireAfterAccess(3, TimeUnit.HOURS).build();

    static {
        if (ServerInstance.verNumber >= 13) {
            PLAYER_HEAD = new ItemStack(Material.PLAYER_HEAD);
        } else {
            PLAYER_HEAD = new ItemStack(Material.getMaterial("SKULL_ITEM"), 1, (short) 3);
        }
        MethodHandle get$profile = null;
        MethodHandle set$profile = null;
        try {
            EasyLookup.addOBCClass("entity.CraftPlayer");
            EasyLookup.addOBCClass("inventory.CraftMetaSkull");

            get$profile = EasyLookup.method("CraftPlayer", "getProfile", GameProfile.class);
            // Unreflect reason:
            // Private method/field
            if (ServerInstance.verNumber >= 15) {
                set$profile = EasyLookup.unreflectMethod("CraftMetaSkull", "setProfile", GameProfile.class);
            } else {
                set$profile = EasyLookup.unreflectSetter("CraftMetaSkull", "profile");
            }
        } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        getProfile = get$profile;
        setProfile = set$profile;
    }

    SkullTexture() {
    }

    /**
     * Main method to get textured head and save into cache.
     *
     * @param texture Texture ID, URL, Base64, Player name or UUID.
     * @return        A ItemStack that represent the textured head.
     */
    public static ItemStack getTexturedHead(String texture) {
        return setTexture(new ItemStack(PLAYER_HEAD), getTextureValue(texture));
    }

    /**
     * Main method to get textured head and save into cache.
     *
     * @param texture  Texture ID, URL, Base64, Player name or UUID.
     * @param callback Function to execute if textured head is retrieved in async operation.
     * @return         A ItemStack that represent the textured head.
     */
    public static ItemStack getTexturedHead(String texture, Consumer<ItemStack> callback) {
        if (callback == null) {
            return getTexturedHead(texture);
        }
        return setTexture(new ItemStack(PLAYER_HEAD), getTextureValue(texture, value -> callback.accept(setTexture(new ItemStack(PLAYER_HEAD), value))));
    }

    /**
     * Set encoded texture value into skull meta.
     *
     * @see #getTextureValue(String)
     *
     * @param head    Skull item to set the texture.
     * @param texture Encoded texture value.
     * @return        The provided item.
     * @throws IllegalArgumentException if the provided item isn't a player head.
     */
    public static ItemStack setTexture(ItemStack head, String texture) throws IllegalArgumentException {
        // Since 1.20.2: The Mojang AuthLib version used by spigot require non-null name for game profile
        GameProfile profile = new GameProfile(UUID.randomUUID(), "null");
        profile.getProperties().put("textures", new Property("textures", getTextureValue(texture)));

        ItemMeta meta = head.getItemMeta();
        if (!(meta instanceof SkullMeta)) {
            throw new IllegalArgumentException("The provided item isn't a player head");
        }
        try {
            setProfile.invoke(meta, profile);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        head.setItemMeta(meta);
        return head;
    }

    /**
     * Get Base64 encoded texture from the given texture parameter,
     * can be player name, player uuid, texture id, url or base64.
     *
     * @param texture Texture type.
     * @return        A Base64 encoded text.
     */
    public static String getTextureValue(String texture) {
        return getTextureValue(texture, null);
    }

    /**
     * Get Base64 encoded texture from the given texture parameter,
     * can be player name, player uuid, texture id, url or base64.
     *
     * @param texture  Texture type.
     * @param callback Function to execute if texture value is retrieved in async operation.
     * @return         A Base64 encoded text.
     */
    public static String getTextureValue(String texture, Consumer<String> callback) {
        String cachedTexture = TEXTURE_CACHE.getIfPresent(texture);
        if (cachedTexture != null) {
            return cachedTexture;
        }

        if (texture.length() <= 20) {
            cachedTexture = getPlayerTexture(texture, texture, null, callback);
        } else if (texture.length() == 36) {
            cachedTexture = getPlayerTexture(texture, null, UUID.fromString(texture), callback);
        } else if (texture.length() == 64) {
            cachedTexture = parseTextureUrl("http://textures.minecraft.net/texture/" + texture);
        } else if (texture.startsWith("http")) {
            cachedTexture = parseTextureUrl(texture);
        } else {
            cachedTexture = texture;
        }

        TEXTURE_CACHE.put(texture, cachedTexture);
        return cachedTexture;
    }

    private static String getPlayerTexture(String key, String name, UUID uuid, Consumer<String> callback) {
        final Player player = name != null ? Bukkit.getPlayer(name) : Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            try {
                GameProfile profile = ((GameProfile) getProfile.invoke(player));
                for (Property texture : profile.getProperties().get("textures")) {
                    if (texture != null) {
                        return texture.getValue();
                    }
                }
            } catch (Throwable t) {
                new RuntimeException("Error when getting online player texture of '" + player.getName() + "', trying to get from Mojang API... ", t).printStackTrace();
            }
        }
        // Async operation
        new Thread(() -> {
            final String texture = computePlayerTexture(key, name != null ? name : Bukkit.getOfflinePlayer(uuid).getName());
            if (callback != null) {
                callback.accept(texture);
            }
        }).start();
        return LOADING_TEXTURE;
    }

    /**
     * Compute textured head via making a request to Mojang API,
     * it's suggested to call this method in async environment.
     *
     * @param name The player name.
     * @return     A Base64 encoded text.
     */
    public static String computePlayerTexture(String name) {
        return computePlayerTexture(name, name);
    }

    /**
     * Compute textured head via making a request to Mojang API,
     * it's suggested to call this method in async environment.
     *
     * @param key  Map key to put.
     * @param name The player name.
     * @return     A Base64 encoded text.
     */
    public static String computePlayerTexture(String key, String name) {
        String texture = requestTextureUrl(name);
        if (texture != null) {
            texture = parseTextureUrl(texture);
            TEXTURE_CACHE.put(key, texture);
            return texture;
        } else {
            TEXTURE_CACHE.put(key, INVALID_TEXTURE);
            return INVALID_TEXTURE;
        }
    }

    /**
     * Request player texture url using Mojang API.
     *
     * @param name The player name.
     * @return     A Mojang texture url if the player profile exists, null otherwise.
     */
    public static String requestTextureUrl(String name) {
        JsonObject user = urlJson(USER_API + name);
        if (user != null && user.has("id")) {
            String uuid = user.get("id").getAsString();
            JsonObject session = urlJson(SESSION_API + uuid);
            if (session != null) {
                for (JsonElement element : session.getAsJsonArray("properties")) {
                    JsonObject property = element.getAsJsonObject();
                    if (property.get("name").getAsString().equals("textures")) {
                        String value = property.get("value").getAsString();
                        JsonObject texture = parseJsonObject(new String(Base64.getDecoder().decode(value)));
                        if (texture != null) {
                            return texture.get("textures").getAsJsonObject().get("SKIN").getAsJsonObject().get("url").getAsString();
                        }
                    }
                }
            }
        }
        return null;
    }

    private static JsonObject urlJson(String url) {
        String text = urlText(url);
        if (text.isBlank()) {
            return null;
        } else {
            return parseJsonObject(text);
        }
    }

    private static String urlText(String url) {
        try (InputStream stream = new URL(url).openStream()) {
            return new String(stream.readAllBytes());
        } catch (Exception e) {
            return "";
        }
    }

    private static String parseTextureUrl(String url) {
        return new String(Base64.getEncoder().encode(("{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}").getBytes()));
    }

    private static JsonObject parseJsonObject(String text) {
        try {
            return new JsonParser().parse(text).getAsJsonObject();
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }
}
