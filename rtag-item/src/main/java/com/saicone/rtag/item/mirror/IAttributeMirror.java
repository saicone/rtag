package com.saicone.rtag.item.mirror;

import com.saicone.rtag.item.ItemMirror;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;
import com.saicone.rtag.util.MC;
import org.jetbrains.annotations.ApiStatus;
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
    public @NotNull MC getMaximumVersion() {
        return MC.V_1_16;
    }

    @Override
    public void upgrade(@NotNull Object compound, @NotNull String id, @NotNull Object components, @NotNull MC from, @NotNull MC to) {
        if (to.isNewerThanOrEquals(MC.V_1_16) && from.isOlderThan(MC.V_1_16)) {
            rename(components, NEW_NAMES);
        }
    }

    @Override
    public void downgrade(@NotNull Object compound, @NotNull String id, @NotNull Object components, @NotNull MC from, @NotNull MC to) {
        if (from.isNewerThanOrEquals(MC.V_1_16) && to.isOlderThan(MC.V_1_16) && id.equals("minecraft:player_head")) {
            rename(components, OLD_NAMES);
        }
    }

    @ApiStatus.Internal
    protected void rename(@NotNull Object compound, @NotNull Map<String, String> names) {
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
    @ApiStatus.Internal
    public static String rename(@NotNull String type, @NotNull Function<String, String> function) {
        final boolean starts = type.startsWith(PREFIX);
        final String renamed = function.apply(starts ? type.substring(PREFIX.length()) : type);
        return starts ? PREFIX + renamed : renamed;
    }
}
