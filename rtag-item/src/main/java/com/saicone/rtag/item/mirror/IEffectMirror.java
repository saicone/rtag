package com.saicone.rtag.item.mirror;

import com.saicone.rtag.item.ItemMirror;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.util.ServerInstance;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;

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

    public IEffectMirror() {
        if (ServerInstance.fullVersion >= 12002) {
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

    public IEffectMirror(String fromId, String toId, String fromDuration, String toDuration) {
        this.fromId = fromId;
        this.toId = toId;
        this.fromDuration = fromDuration;
        this.toDuration = toDuration;
    }

    @Override
    public float getMinVersion() {
        return 14;
    }

    @Override
    public void upgrade(Object compound, String id, Object tag, float from, float to) {
        if (from <= 20.01f && id.equals("minecraft:suspicious_stew")) {
            processEffects(tag, "Effects", "effects", true);
        }
    }

    @Override
    public void downgrade(Object compound, String id, Object tag, float from, float to) {
        if (to <= 20.01f && id.equals("minecraft:suspicious_stew")) {
            processEffects(tag, "effects", "Effects", false);
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
    @SuppressWarnings("unchecked")
    public void processEffects(Object tag, String fromKey, String toKey, boolean useKey) {
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
    @SuppressWarnings("deprecation")
    public Integer getEffectId(String key) {
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
