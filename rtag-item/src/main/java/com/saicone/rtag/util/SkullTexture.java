package com.saicone.rtag.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    private static final Map<String, ItemStack> cache = new HashMap<>();

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
            // Private field
            set$profile = EasyLookup.unreflectSetter("CraftMetaSkull", "profile");
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
    public static ItemStack getTextureHead(String texture) {
        return cache.computeIfAbsent(texture, SkullTexture::buildHead);
    }

    private static ItemStack buildHead(String texture) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", getTextureValue(texture)));

        ItemStack item = new ItemStack(PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        try {
            setProfile.invoke(meta, profile);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Get Base64 texture from the given texture parameter,
     * can be player name, player uuid, texture id, url or base64.
     *
     * @param texture Texture type.
     * @return        A Base64 encoded text.
     */
    public static String getTextureValue(String texture) {
        if (texture.length() <= 20) {
            return getPlayerTexture(texture, Bukkit.getOfflinePlayer(texture));
        } else if (texture.length() == 36) {
            return getPlayerTexture(texture, Bukkit.getOfflinePlayer(UUID.fromString(texture)));
        } else if (texture.length() == 64) {
            return parseTextureUrl("http://textures.minecraft.net/texture/" + texture);
        } else if (texture.startsWith("http")) {
            return parseTextureUrl(texture);
        } else {
            return texture;
        }
    }

    /**
     * Get Base64 texture from online player, or compute textured head
     * via making a request to Mojang API.
     *
     * @param key    Map key to put
     * @param player A player, can be offline.
     * @return       A Base64 encoded text.
     */
    public static String getPlayerTexture(String key, OfflinePlayer player) {
        if (player.isOnline()) {
            try {
                GameProfile profile = ((GameProfile) getProfile.invoke(player));
                for (Property texture : profile.getProperties().get("textures")) {
                    if (texture != null) {
                        return texture.getValue();
                    }
                }
            } catch (Throwable t) {
                new RuntimeException("Error when getting online player texture, trying to get from Mojang API... ", t).printStackTrace();
            }
        }
        // Async operation
        new Thread(() -> computePlayerTexture(key, player.getName())).start();
        return LOADING_TEXTURE;
    }

    /**
     * Compute textured head via making a request to Mojang API,
     * it's suggested to call this method in async environment.
     *
     * @param key  Map key to put
     * @param name Player name
     */
    public static void computePlayerTexture(String key, String name) {
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
                            String url = texture.get("textures").getAsJsonObject().get("SKIN").getAsJsonObject().get("url").getAsString();
                            cache.put(key, buildHead(url));
                            return;
                        }
                    }
                }
            }
        }
        cache.put(key, buildHead(INVALID_TEXTURE));
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
