package com.saicone.rtag.item.mirror;

import com.saicone.rtag.item.ItemMirror;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * IAttributeMirror class to convert attribute names across versions.
 *
 * @author Rubenicos
 */
public class IAttributeMirror implements ItemMirror {

    private static final String PREFIX = "minecraft:";
    private static final Map<String, String> NEW_NAMES;
    private static final Map<String, String> OLD_NAMES;

    static {
        final Map<String, String> names = new HashMap<>();
        names.put("generic.maxHealth", "generic.max_health");
        names.put("zombie.spawnReinforcements", "zombie.spawn_reinforcements");
        names.put("horse.jumpStrength", "horse.jump_strength");
        names.put("generic.followRange", "generic.follow_range");
        names.put("generic.knockbackResistance", "generic.knockback_resistance");
        names.put("generic.movementSpeed", "generic.movement_speed");
        names.put("generic.flyingSpeed", "generic.flying_speed");
        names.put("generic.attackDamage", "generic.attack_damage");
        names.put("generic.attackKnockback", "generic.attack_knockback");
        names.put("generic.attackSpeed", "generic.attack_speed");
        names.put("generic.armorToughness", "generic.armor_toughness");

        final Map<String, String> oldNames = new HashMap<>();
        for (Map.Entry<String, String> entry : names.entrySet()) {
            oldNames.put(entry.getValue(), entry.getKey());
        }
        NEW_NAMES = Collections.unmodifiableMap(names);
        OLD_NAMES = Collections.unmodifiableMap(oldNames);
    }

    @Override
    public float getDeprecationVersion() {
        return 16;
    }

    @Override
    public void upgrade(Object compound, String id, Object components, float from, float to) {
        if (to >= 16f && from < 16f) {
            rename(components, NEW_NAMES);
        }
    }

    @Override
    public void downgrade(Object compound, String id, Object components, float from, float to) {
        if (from >= 16f && to < 16f && id.equals("minecraft:player_head")) {
            rename(components, OLD_NAMES);
        }
    }

    protected void rename(Object compound, Map<String, String> names) {
        Object modifiers = TagCompound.get(compound, "AttributeModifiers");
        if (modifiers == null) return;

        for (Object modifier : TagList.getValue(modifiers)) {
            final Map<String, Object> map = TagCompound.getValue(modifier);
            final Object name = map.remove("AttributeName");
            if (name != null) {
                map.put("type", TagBase.newTag(rename((String) TagBase.getValue(name), nameValue -> names.getOrDefault(nameValue, nameValue))));
            }
        }
    }

    @NotNull
    public static String rename(@NotNull String type, @NotNull Function<String, String> function) {
        final boolean starts = type.startsWith(PREFIX);
        final String renamed = function.apply(starts ? type.substring(PREFIX.length()) : type);
        return starts ? PREFIX + renamed : renamed;
    }
}
