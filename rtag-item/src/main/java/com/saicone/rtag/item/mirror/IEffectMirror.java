package com.saicone.rtag.item.mirror;

import com.saicone.rtag.item.ItemMirror;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.util.MC;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * IEffectMirror class to convert item effect format across versions.
 *
 * @author Rubenicos
 */
public class IEffectMirror implements ItemMirror {

    private static final Map<Integer, String> ALIASES = Map.of(
            24, "glowing",
            25, "levitation",
            26, "luck",
            27, "unluck",
            28, "slow_falling",
            29, "conduit_power",
            30, "dolphins_grace",
            31, "bad_omen",
            32, "hero_of_the_village",
            33, "darkness"
    );

    private final String fromId;
    private final String toId;
    private final String fromDuration;
    private final String toDuration;

    /**
     * Constructs an IEffectMirror for current server instance version.
     */
    public IEffectMirror() {
        this(MC.version());
    }

    /**
     * Constructs an IEffectMirror with specified server version to generate map names.
     *
     * @param version The server version to create keys for.
     */
    @Deprecated(since = "1.5.14", forRemoval = true)
    public IEffectMirror(float version) {
        this(MC.findReverse(MC::featRevision, version));
    }

    /**
     * Constructs an IEffectMirror with specified server version to generate map names.
     *
     * @param version the server version to create keys for.
     */
    public IEffectMirror(@NotNull MC version) {
        if (version.isNewerThanOrEquals(MC.V_1_20_2)) {
            this.fromId = "EffectId";
            this.toId = "id";
            this.fromDuration = "EffectDuration";
            this.toDuration = "duration";
        } else {
            this.fromId = "id";
            this.toId = "EffectId";
            this.fromDuration = "duration";
            this.toDuration = "EffectDuration";
        }
    }

    /**
     * Constructs an IEffectMirror with specified key names.
     *
     * @param fromId       Effect ID key to get.
     * @param toId         Effect ID key to set.
     * @param fromDuration Effect duration key to get.
     * @param toDuration   Effect duration key to set.
     */
    public IEffectMirror(@NotNull String fromId, @NotNull String toId, @NotNull String fromDuration, @NotNull String toDuration) {
        this.fromId = fromId;
        this.toId = toId;
        this.fromDuration = fromDuration;
        this.toDuration = toDuration;
    }

    @Override
    public @NotNull MC getMinimumVersion() {
        return MC.V_1_14;
    }

    @Override
    public void upgrade(@NotNull Object compound, @NotNull String id, @NotNull Object components, @NotNull MC from, @NotNull MC to) {
        if (from.isOlderThan(MC.V_1_20_2) && to.isNewerThanOrEquals(MC.V_1_20_2) && id.equals("minecraft:suspicious_stew")) {
            processEffects(components, "Effects", "effects", true);
        }
    }

    @Override
    public void downgrade(@NotNull Object compound, @NotNull String id, @NotNull Object components, @NotNull MC from, @NotNull MC to) {
        if (to.isOlderThan(MC.V_1_20_2) && from.isNewerThanOrEquals(MC.V_1_20_2) && id.equals("minecraft:suspicious_stew")) {
            processEffects(components, "effects", "Effects", false);
        }
    }

    /**
     * Process current potion effects inside tag.
     *
     * @param tag     ItemStack tag.
     * @param fromKey The initial key to read effects.
     * @param toKey   The final key to save effects.
     * @param useKey  true to convert effect ids to namespaced key.
     */
    @ApiStatus.Internal
    @SuppressWarnings("unchecked")
    public void processEffects(@NotNull Object tag, @NotNull String fromKey, @NotNull String toKey, boolean useKey) {
        final List<Object> effects;
        try {
            effects = (List<Object>) TagCompound.remove(tag, fromKey);
        } catch (ClassCastException e) {
            e.printStackTrace();
            return;
        }
        if (effects == null) {
            return;
        }
        for (Object effect : effects) {
            if (!TagCompound.isTagCompound(effect)) {
                continue;
            }
            final Map<String, Object> map = TagCompound.getValue(effect);
            Object id = TagBase.getValue(map.remove(fromId));
            if (id != null) {
                if (useKey) {
                    if (id instanceof Number) {
                        id = "minecraft:" + getEffectKey(((Number) id).intValue());
                    }
                } else if (id instanceof String) {
                    id = getEffectId((String) id).byteValue();
                }
                map.put(toId, TagBase.newTag(id));
            }
            final Object duration = map.remove(fromDuration);
            if (duration != null) {
                map.put(toDuration, duration);
            }
        }
        TagCompound.set(tag, toKey, effects);
    }

    /**
     * Get potion effect key by id.
     *
     * @param id The effect id.
     * @return   An effect key.
     */
    @NotNull
    @ApiStatus.Internal
    @SuppressWarnings("deprecation")
    public String getEffectKey(int id) {
        final PotionEffectType type = PotionEffectType.getById(id);
        if (type != null) {
            return type.getName().toLowerCase();
        }
        return ALIASES.getOrDefault(id, "speed");
    }

    /**
     * Get potion effect id by key.
     *
     * @param key The effect key.
     * @return    A effect id.
     */
    @NotNull
    @ApiStatus.Internal
    @SuppressWarnings("deprecation")
    public Integer getEffectId(@NotNull String key) {
        final String name = key.replace("minecraft:", "");
        final PotionEffectType type = PotionEffectType.getByName(name);
        if (type != null) {
            return type.getId();
        }
        for (Map.Entry<Integer, String> entry : ALIASES.entrySet()) {
            if (entry.getValue().equals(name)) {
                return entry.getKey();
            }
        }
        return 1;
    }
}
