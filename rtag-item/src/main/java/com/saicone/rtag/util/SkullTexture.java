package com.saicone.rtag.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.invoke.MethodHandle;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkullTexture {

    private static final Material PLAYER_HEAD;
    private static final MethodHandle setProfile;

    private static final Map<String, ItemStack> cache = new HashMap<>();

    static {
        if (ServerInstance.verNumber >= 13) {
            PLAYER_HEAD = Material.PLAYER_HEAD;
        } else {
            PLAYER_HEAD = Material.getMaterial("SKULL_ITEM");
        }
        MethodHandle m1 = null;
        try {
            m1 = EasyLookup.unreflectSetter("CraftMetaSkull", "profile");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        setProfile = m1;
    }

    public static ItemStack getTextureHead(String texture) {
        return cache.computeIfAbsent(texture, SkullTexture::buildHead);
    }

    @SuppressWarnings("deprecation")
    private static ItemStack buildHead(String texture) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", getTextureValue(texture)));

        ItemStack item = new ItemStack(PLAYER_HEAD, 1, (short) 3);
        ItemMeta meta = item.getItemMeta();
        try {
            setProfile.invoke(meta, profile);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        item.setItemMeta(meta);
        return item;
    }

    public static String getTextureValue(String texture) {
        if (texture.length() == 64) {
            return urlTexture("http://textures.minecraft.net/texture/" + texture);
        } else if (texture.startsWith("http")) {
            return urlTexture(texture);
        } else {
            return texture;
        }
    }

    private static String urlTexture(String url) {
        return new String(Base64.getEncoder().encode(("{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}").getBytes()));
    }
}
