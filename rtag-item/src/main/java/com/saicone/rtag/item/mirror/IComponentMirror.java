package com.saicone.rtag.item.mirror;

import com.saicone.rtag.Rtag;
import com.saicone.rtag.item.ItemMirror;
import com.saicone.rtag.item.ItemObject;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;

import java.util.*;
import java.util.function.Function;

public class IComponentMirror implements ItemMirror {

    private static final Map<String, Transformation> TRANSFORMATIONS = new HashMap<>();

    static {
        TRANSFORMATIONS.put("minecraft:unbreakable", new Unbreakable());
        TRANSFORMATIONS.put("minecraft:enchantments", new Enchantments(0));
        TRANSFORMATIONS.put("minecraft:stored_enchantments", new Enchantments(5));
        TRANSFORMATIONS.put("minecraft:can_break", new CanBuild(3));
        TRANSFORMATIONS.put("minecraft:can_place_on", new CanBuild(4));
        TRANSFORMATIONS.put("minecraft:dyed_color", new DyedColor());
        TRANSFORMATIONS.put("minecraft:attribute_modifiers", new AttributeModifiers());
        TRANSFORMATIONS.put("minecraft:charged_projectiles", new ChargedProjectiles());
        TRANSFORMATIONS.put("minecraft:map_decorations", new MapDecorations());
        TRANSFORMATIONS.put("minecraft:potion_contents", new PotionContents());
        TRANSFORMATIONS.put("minecraft:writable_book_contents", new BookContents());
        TRANSFORMATIONS.put("minecraft:written_book_contents", new BookContents());
        TRANSFORMATIONS.put("minecraft:trim", new TooltipDowngrade(7));
        TRANSFORMATIONS.put("minecraft:hide_additional_tooltip", new Transformation() {
            @Override
            public boolean downgradeComponent(Object components, String id, Map<String, Object> value) {
                setFlag(components, 5);
                // Delete component on finish
                return false;
            }
        });
        TRANSFORMATIONS.put("minecraft:firework_explosion", new FireworkExplosion());
        TRANSFORMATIONS.put("minecraft:fireworks", new Fireworks());
        TRANSFORMATIONS.put("minecraft:base_color", new BaseColor());
        TRANSFORMATIONS.put("minecraft:banner_patterns", new BannerPatterns());
        TRANSFORMATIONS.put("minecraft:container", new Container());
        TRANSFORMATIONS.put("minecraft:bees", new Bees());

    }

    @Override
    public float getDeprecationVersion() {
        return 20.04f;
    }

    @Override
    public void upgrade(Object compound, String id, float from, float to) {
        if (to >= 20.04f) {
            for (Object[] path : extractPaths(compound)) {
                Rtag.INSTANCE.move(compound, path, ItemObject.getComponentPath(path), true);
            }
            if (TagCompound.hasKey(compound, "tag")) {
                Rtag.INSTANCE.move(compound, new Object[] { "tag" }, new Object[] { "minecraft:custom_data" }, true);
            }
            final Object components = TagCompound.get(compound, "components");
            if (components != null) {
                final Map<String, Object> value = TagCompound.getValue(compound);
                for (String key : new ArrayList<>(value.keySet())) {
                    final Transformation transformation = TRANSFORMATIONS.get(key);
                    if (transformation == null) continue;

                    final Object val = value.get(key);
                    final boolean result;
                    if (TagCompound.isTagCompound(val)) {
                        result = transformation.upgradeComponent(components, key, TagCompound.getValue(val));
                    } else if (TagList.isTagList(val)) {
                        result = transformation.upgradeList(components, key, TagList.getValue(val));
                    } else {
                        result = transformation.upgradeObject(components, key, TagBase.getValue(val));
                    }

                    if (!result) {
                        value.remove(key);
                    }
                }
            }
        }
    }

    @Override
    public void upgrade(Object compound, String id, Object components, float from, float to) {
        upgrade(compound, id, from, to);
    }

    @Override
    public void downgrade(Object compound, String id, float from, float to) {
        final Object components;
        if (from >= 20.04f && (components = TagCompound.get(compound, "components")) != null) {
            final Map<String, Object> value = TagCompound.getValue(compound);
            for (String key : new ArrayList<>(value.keySet())) {
                final Transformation transformation = TRANSFORMATIONS.get(key);
                if (transformation == null) continue;

                final Object val = value.get(key);
                final boolean result;
                if (TagCompound.isTagCompound(val)) {
                    result = transformation.downgradeComponent(components, key, TagCompound.getValue(val));
                } else if (TagList.isTagList(val)) {
                    result = transformation.downgradeList(components, key, TagList.getValue(val));
                } else {
                    result = transformation.downgradeObject(components, key, TagBase.getValue(val));
                }

                if (!result) {
                    value.remove(key);
                }
            }
            for (Object[] path : extractPaths(compound)) {
                Rtag.INSTANCE.move(compound, path, ItemObject.getTagPath(path), true);
            }
        }
    }

    @Override
    public void downgrade(Object compound, String id, Object components, float from, float to) {
        downgrade(compound, id, from, to);
    }

    private Set<Object[]> extractPaths(Object compound) {
        final Map<String, Object> value = TagCompound.getValue(compound);
        if (value.isEmpty()) {
            return Set.of();
        }
        final Set<Object[]> paths = new HashSet<>();
        for (Map.Entry<String, Object> entry : value.entrySet()) {
            if (TagCompound.isTagCompound(entry.getValue())) {
                for (Object[] path : extractPaths(entry.getValue())) {
                    final Object[] subPath = new Object[path.length + 1];
                    subPath[0] = entry.getKey();
                    System.arraycopy(path, 0, subPath, 1, path.length);
                    paths.add(subPath);
                }
            } else {
                paths.add(new Object[] { entry.getKey() });
            }
        }
        return paths;
    }

    public interface Transformation {

        default boolean upgradeComponent(Object components, String id, Map<String, Object> value) {
            return true;
        }

        default boolean upgradeList(Object components, String id, List<Object> value) {
            return true;
        }

        default boolean upgradeObject(Object components, String id, Object value) {
            return true;
        }

        default boolean downgradeComponent(Object components, String id, Map<String, Object> value) {
            return true;
        }

        default boolean downgradeList(Object components, String id, List<Object> value) {
            return true;
        }

        default boolean downgradeObject(Object components, String id, Object value) {
            return true;
        }

        default void move(Map<String, Object> map, String fromKey, String toKey) {
            move(map, fromKey, toKey, null);
        }

        default void move(Map<String, Object> map, String fromKey, String toKey, Function<Object, Object> transformation) {
            Object value = map.get(fromKey);
            map.remove(fromKey);
            if (value != null && transformation != null) {
                value = transformation.apply(TagBase.getValue(value));
                if (value != null) {
                    value = TagBase.newTag(value);
                } else {
                    return;
                }
            }
            if (value != null) {
                map.put(toKey, value);
            }
        }

        default void setFlag(Object components, int ordinal) {
            int bitField = Rtag.INSTANCE.getOptional(components, "minecraft:custom_data", "HideFlags").asInt(0);
            final byte bit = (byte) (1 << ordinal);
            bitField |= bit;
            Rtag.INSTANCE.set(components, bitField, "minecraft:custom_data", "HideFlags");
        }
    }

    public static class TooltipDowngrade implements Transformation {
        private final int ordinal;

        public TooltipDowngrade(int ordinal) {
            this.ordinal = ordinal;
        }

        @Override
        public boolean downgradeComponent(Object components, String id, Map<String, Object> value) {
            downgradeTooltip(components, value);
            return true;
        }

        public void downgradeTooltip(Object components, Map<String, Object> value) {
            if (Boolean.FALSE.equals(TagBase.getValue(value.get("show_in_tooltip")))) {
                setFlag(components, ordinal);
                value.remove("show_in_tooltip");
            }
        }
    }

    public static class Unbreakable extends TooltipDowngrade {
        public Unbreakable() {
            super(2);
        }

        @Override
        public boolean upgradeObject(Object components, String id, Object value) {
            if (Boolean.TRUE.equals(value)) {
                return Rtag.INSTANCE.set(components, Map.of("show_in_tooltip", true), id);
            } else {
                return false;
            }
        }

        @Override
        public boolean downgradeComponent(Object components, String id, Map<String, Object> value) {
            downgradeTooltip(components, value);
            return Rtag.INSTANCE.set(components, true, id);
        }
    }

    public static class Enchantments extends TooltipDowngrade {
        public Enchantments(int ordinal) {
            super(ordinal);
        }

        @Override
        public boolean upgradeList(Object components, String id, List<Object> value) {
            final Map<String, Object> levels = new HashMap<>();
            for (Object enchantment : value) {
                levels.put(
                        (String) TagBase.getValue(TagCompound.get(enchantment, "id")),
                        TagBase.newTag(Integer.parseInt(String.valueOf(TagCompound.get(enchantment, "lvl"))))
                );
            }
            return Rtag.INSTANCE.set(components, levels, id, "levels");
        }

        @Override
        public boolean downgradeComponent(Object components, String id, Map<String, Object> value) {
            downgradeTooltip(components, value);
            if (value.containsKey("levels")) {
                final List<Object> enchantments = new ArrayList<>();
                for (Map.Entry<String, Object> entry : TagCompound.getValue(value.get("levels")).entrySet()) {
                    final int level = (int) TagBase.getValue(entry.getValue());
                    enchantments.add(TagCompound.newTag(Map.of(
                            "id", TagBase.newTag(entry.getKey()),
                            "lvl", TagBase.newTag(level > Short.MAX_VALUE ? Short.MAX_VALUE : (short) level)
                    )));
                }
                return Rtag.INSTANCE.set(components, enchantments, id);
            } else {
                return false;
            }
        }
    }

    public static class CanBuild extends TooltipDowngrade {

        public CanBuild(int ordinal) {
            super(ordinal);
        }

        @Override
        public boolean upgradeList(Object components, String id, List<Object> value) {
            // TODO: Convert block predicate
            return false;
        }

        @Override
        public boolean downgradeComponent(Object components, String id, Map<String, Object> value) {
            downgradeTooltip(components, value);
            // TODO: Convert block predicate
            return false;
        }
    }

    public static class DyedColor extends TooltipDowngrade {
        public DyedColor() {
            super(6);
        }

        @Override
        public boolean upgradeObject(Object components, String id, Object value) {
            return Rtag.INSTANCE.set(components, value, id, "rgb");
        }

        @Override
        public boolean downgradeComponent(Object components, String id, Map<String, Object> value) {
            downgradeTooltip(components, value);
            if (value.containsKey("rgb")) {
                return Rtag.INSTANCE.set(components, value.get("rgb"), id);
            } else {
                return false;
            }
        }
    }

    public static class AttributeModifiers extends TooltipDowngrade {
        public AttributeModifiers() {
            super(1);
        }

        @Override
        public boolean upgradeList(Object components, String id, List<Object> value) {
            for (Object modifier : value) {
                final Map<String, Object> map = TagCompound.getValue(modifier);
                move(map, "AttributeName", "type");
                move(map, "Slot", "slot");
                move(map, "UUID", "uuid");
                move(map, "Name", "name");
                move(map, "Amount", "amount");
                move(map, "Operation", "operation", type -> {
                    switch ((int) type) {
                        case 0:
                            return "add_value";
                        case 1:
                            return "add_multiplied_base";
                        case 2:
                            return "add_multiplied_total";
                        default:
                            return null;
                    }
                });
            }
            return Rtag.INSTANCE.set(components, value, id, "modifiers");
        }

        @Override
        public boolean downgradeComponent(Object components, String id, Map<String, Object> value) {
            downgradeTooltip(components, value);
            final Object modifiers = value.get("modifiers");
            if (modifiers != null) {
                for (Object modifier : TagList.getValue(modifiers)) {
                    final Map<String, Object> map = TagCompound.getValue(modifier);
                    move(map, "type", "AttributeName");
                    move(map, "slot", "Slot");
                    move(map, "uuid", "UUID");
                    move(map, "name", "Name");
                    move(map, "amount", "Amount");
                    move(map, "operation", "Operation", type -> {
                        switch ((String) type) {
                            case "add_value":
                                return 0;
                            case "add_multiplied_base":
                                return 1;
                            case "add_multiplied_total":
                                return 2;
                            default:
                                return null;
                        }
                    });
                }
                return Rtag.INSTANCE.set(components, modifiers, id);
            } else {
                return false;
            }
        }
    }

    public static class ChargedProjectiles implements Transformation {
        @Override
        public boolean upgradeList(Object components, String id, List<Object> value) {
            Rtag.INSTANCE.set(components, null, "minecraft:custom_data", "Charged");
            return true;
        }

        @Override
        public boolean downgradeList(Object components, String id, List<Object> value) {
            return Rtag.INSTANCE.set(components, !value.isEmpty(), "minecraft:custom_data", "Charged");
        }
    }

    public static class MapDecorations implements Transformation {
        @Override
        public boolean upgradeList(Object components, String id, List<Object> value) {
            final Map<String, Object> decorations = new HashMap<>();
            for (Object decoration : value) {
                final Map<String, Object> map = TagCompound.getValue(decoration);
                final String key = (String) TagBase.getValue(map.remove("id"));
                move(map, "type", "type", type -> Type.VALUES[Byte.valueOf((byte) type).intValue()].name().toLowerCase());
                move(map, "rot", "rotation", rot -> Double.valueOf((double) rot).floatValue());
                decorations.put(key, decoration);
            }
            // Load saved decorations
            final Object saved = Rtag.INSTANCE.getExact(components, "minecraft:custom_data", "savedDecorations");
            if (saved != null) {
                Rtag.INSTANCE.set(components, null, "minecraft:custom_data", "savedDecorations");
                decorations.putAll(TagCompound.getValue(saved));
            }
            return Rtag.INSTANCE.set(components, decorations, id);
        }

        @Override
        public boolean downgradeComponent(Object components, String id, Map<String, Object> value) {
            final List<Object> decorations = new ArrayList<>();
            final Map<String, Object> saved = new HashMap<>();
            for (Map.Entry<String, Object> decoration : value.entrySet()) {
                final Map<String, Object> map = TagCompound.getValue(decoration.getValue());
                final String type = (String) TagBase.getValue(map.get("type"));
                if (Type.ORDINALS.containsKey(type)) {
                    map.put("id", TagBase.newTag(decoration.getKey()));
                    map.put("type", TagBase.newTag(Type.ORDINALS.get(type)));
                    move(map, "rot", "rotation", rot -> Float.valueOf((float) rot).doubleValue());
                    decorations.add(decoration.getValue());
                } else {
                    saved.put(decoration.getKey(), decoration.getValue());
                }
            }
            if (!saved.isEmpty()) {
                Rtag.INSTANCE.set(components, saved, "minecraft:custom_data", "savedDecorations");
            }
            return Rtag.INSTANCE.set(components, decorations, id);
        }

        private enum Type {
            PLAYER,
            FRAME,
            RED_MARKER,
            BLUE_MARKER,
            TARGET_X,
            TARGET_POINT,
            PLAYER_OFF_MAP,
            PLAYER_OFF_LIMITS,
            MANSION,
            MONUMENT,
            BANNER_WHITE,
            BANNER_ORANGE,
            BANNER_MAGENTA,
            BANNER_LIGHT_BLUE,
            BANNER_YELLOW,
            BANNER_LIME,
            BANNER_PINK,
            BANNER_GRAY,
            BANNER_LIGHT_GRAY,
            BANNER_CYAN,
            BANNER_PURPLE,
            BANNER_BLUE,
            BANNER_BROWN,
            BANNER_GREEN,
            BANNER_RED,
            BANNER_BLACK,
            RED_X,
            VILLAGE_DESERT,
            VILLAGE_PLAINS,
            VILLAGE_SAVANNA,
            VILLAGE_SNOWY,
            VILLAGE_TAIGA,
            JUNGLE_TEMPLE,
            SWAMP_HUT;

            static final Type[] VALUES = values();
            static final Map<String, Byte> ORDINALS = new HashMap<>();

            static {
                for (Type value : VALUES) {
                    ORDINALS.put(value.name().toLowerCase(), (byte) value.ordinal());
                }
            }
        }
    }

    public static class PotionContents implements Transformation {
        @Override
        public boolean upgradeComponent(Object components, String id, Map<String, Object> value) {
            move(value, "Potion", "potion");
            move(value, "CustomPotionColor", "custom_color");
            move(value, "custom_potion_effects", "custom_effects");
            return true;
        }

        @Override
        public boolean downgradeComponent(Object components, String id, Map<String, Object> value) {
            move(value, "potion", "Potion");
            move(value, "custom_color", "CustomPotionColor");
            move(value, "custom_effects", "custom_potion_effects");
            return true;
        }
    }

    public static class BookContents implements Transformation {
        @Override
        public boolean upgradeComponent(Object components, String id, Map<String, Object> value) {
            if (value.containsKey("pages")) {
                final List<Object> pages = new ArrayList<>();
                final Map<String, Object> filtered_pages;
                if (value.containsKey("filtered_pages")) {
                    filtered_pages = TagCompound.getValue(value.get("filtered_pages"));
                    value.remove("filtered_pages");
                } else {
                    filtered_pages = Map.of();
                }
                int index = 0;
                for (Object text : TagList.getValue(value.get("pages"))) {
                    final Map<String, Object> page = new HashMap<>();
                    page.put("text", text);
                    final Object filtered = filtered_pages.get(String.valueOf(index));
                    if (filtered != null) {
                        page.put("filtered", filtered);
                    }
                    pages.add(TagCompound.newTag(page));
                    index++;
                }
                value.put("pages", TagList.newTag(pages));
            } else {
                value.remove("filtered_pages");
            }

            if (value.containsKey("title")) {
                final Map<String, Object> title = new HashMap<>();
                title.put("text", value.get("title"));
                if (value.containsKey("filtered_title")) {
                    title.put("filtered", value.get("filtered_title"));
                }
                value.put("title", TagCompound.getValue(title));
            } else {
                value.remove("filtered_title");
            }

            return true;
        }

        @Override
        public boolean downgradeComponent(Object components, String id, Map<String, Object> value) {
            if (value.containsKey("pages")) {
                final List<Object> pages = new ArrayList<>();
                final Map<String, Object> filtered_pages = new HashMap<>();
                int index = 0;
                for (Object page : TagList.getValue(value.get("pages"))) {
                    final Map<String, Object> map = TagCompound.getValue(page);
                    pages.add(map.get("text"));
                    if (map.containsKey("filtered")) {
                        filtered_pages.put(String.valueOf(index), map.get("filtered"));
                    }
                    index++;
                }
                value.put("pages", TagList.newTag(pages));
                if (!filtered_pages.isEmpty()) {
                    value.put("filtered_pages", TagCompound.newTag(filtered_pages));
                }
            }

            if (value.containsKey("title")) {
                final Map<String, Object> title = TagCompound.getValue(value.get("title"));
                value.put("title", title.get("text"));
                if (title.containsKey("filtered")) {
                    value.put("filtered_title", title.get("filtered"));
                }
            }

            return true;
        }
    }

    public static class FireworkExplosion implements Transformation {
        @Override
        public boolean upgradeComponent(Object components, String id, Map<String, Object> value) {
            upgradeExplosion(value);
            return true;
        }

        public void upgradeExplosion(Map<String, Object> explosion) {
            move(explosion, "shape", "shape", shape -> Shape.VALUES[(int) shape].name().toLowerCase());
        }

        @Override
        public boolean downgradeComponent(Object components, String id, Map<String, Object> value) {
            downgradeExplosion(value);
            return true;
        }

        public void downgradeExplosion(Map<String, Object> explosion) {
            move(explosion, "shape", "shape", shape -> Shape.ORDINALS.get((String) shape));
        }

        private enum Shape {
            SMALL_BALL,
            LARGE_BALL,
            STAR,
            CREEPER,
            BURST;

            static final Shape[] VALUES = values();
            static final Map<String, Byte> ORDINALS = new HashMap<>();

            static {
                for (Shape value : VALUES) {
                    ORDINALS.put(value.name().toLowerCase(), (byte) value.ordinal());
                }
            }
        }
    }

    public static class Fireworks extends FireworkExplosion {
        @Override
        public boolean upgradeComponent(Object components, String id, Map<String, Object> value) {
            for (Object explosion : TagList.getValue(value.get("explosions"))) {
                final Map<String, Object> map = TagCompound.getValue(explosion);
                move(map, "Type", "shape");
                move(map, "Colors", "colors");
                move(map, "FadeColors", "fade_colors");
                move(map, "Trail", "has_trail");
                move(map, "Flicker", "has_twinkle");
                upgradeExplosion(map);
            }
            return true;
        }

        @Override
        public boolean downgradeComponent(Object components, String id, Map<String, Object> value) {
            for (Object explosion : TagList.getValue(value.get("explosions"))) {
                final Map<String, Object> map = TagCompound.getValue(explosion);
                move(map, "shape", "Type");
                move(map, "colors", "Colors");
                move(map, "fade_colors", "FadeColors");
                move(map, "has_trail", "Trail");
                move(map, "has_twinkle", "Flicker");
                downgradeExplosion(map);
            }
            return true;
        }
    }

    public static class BaseColor implements Transformation {
        @Override
        public boolean upgradeObject(Object components, String id, Object value) {
            return Rtag.INSTANCE.set(components, Color.VALUES[(int) value].name().toLowerCase(), id);
        }

        @Override
        public boolean downgradeObject(Object components, String id, Object value) {
            return Rtag.INSTANCE.set(components, Color.ORDINALS.get((String) value), id);
        }
    }

    public static class BannerPatterns implements Transformation {
        @Override
        public boolean upgradeComponent(Object components, String id, Map<String, Object> value) {
            for (Object compound : TagList.getValue(value.get("Patterns"))) {
                final Map<String, Object> map = TagCompound.getValue(compound);
                move(map, "Pattern", "pattern", pattern -> Pattern.NAMES.get((String) pattern));
                move(map, "Color", "color", color -> Color.VALUES[(int) color].name().toLowerCase());
            }
            return true;
        }

        @Override
        public boolean downgradeComponent(Object components, String id, Map<String, Object> value) {
            for (Object compound : TagList.getValue(value.get("Patterns"))) {
                final Map<String, Object> map = TagCompound.getValue(compound);
                move(map, "pattern", "Pattern", pattern -> Pattern.SHORT_NAMES.get((String) pattern));
                move(map, "color", "Color", color -> Color.ORDINALS.get((String) color));
            }
            return true;
        }

        private enum Pattern {
            BASE("b"),
            SQUARE_BOTTOM_LEFT("bl"),
            SQUARE_BOTTOM_RIGHT("br"),
            SQUARE_TOP_LEFT("tl"),
            SQUARE_TOP_RIGHT("tr"),
            STRIPE_BOTTOM("bs"),
            STRIPE_TOP("ts"),
            STRIPE_LEFT("ls"),
            STRIPE_RIGHT("rs"),
            STRIPE_CENTER("cs"),
            STRIPE_MIDDLE("ms"),
            STRIPE_DOWNRIGHT("drs"),
            STRIPE_DOWNLEFT("dls"),
            SMALL_STRIPES("ss"),
            CROSS("cr"),
            STRAIGHT_CROSS("sc"),
            TRIANGLE_BOTTOM("bt"),
            TRIANGLE_TOP("tt"),
            TRIANGLES_BOTTOM("bts"),
            TRIANGLES_TOP("tts"),
            DIAGONAL_LEFT("ld"),
            DIAGONAL_UP_RIGHT("rd"),
            DIAGONAL_UP_LEFT("lud"),
            DIAGONAL_RIGHT("rud"),
            CIRCLE("mc"),
            RHOMBUS("mr"),
            HALF_VERTICAL("vh"),
            HALF_HORIZONTAL("hh"),
            HALF_VERTICAL_RIGHT("vhr"),
            HALF_HORIZONTAL_BOTTOM("hhb"),
            BORDER("bo"),
            CURLY_BORDER("cbo"),
            GRADIENT("gra"),
            GRADIENT_UP("gru"),
            BRICKS("bri"),
            GLOBE("glb"),
            CREEPER("cre"),
            SKULL("sku"),
            FLOWER("flo"),
            MOJANG("moj"),
            PIGLIN("pig");

            static final Map<String, String> NAMES = new HashMap<>();
            static final Map<String, String> SHORT_NAMES = new HashMap<>();

            static {
                for (Pattern value : values()) {
                    final String name = "minecraft:" + value.name().toLowerCase();
                    NAMES.put(value.getShortName(), name);
                    SHORT_NAMES.put(name, value.getShortName());
                }
            }

            private final String shortName;

            Pattern(String shortName) {
                this.shortName = shortName;
            }

            public String getShortName() {
                return shortName;
            }
        }
    }

    public static class Container implements Transformation {
        @Override
        public boolean upgradeList(Object components, String id, List<Object> value) {
            for (int i = 0; i < value.size(); i++) {
                final Object item = value.get(i);
                final Map<String, Object> itemValue = TagCompound.getValue(item);
                final Map<String, Object> slot = new HashMap<>();
                slot.put("slot", TagBase.newTag(Byte.valueOf((byte) TagBase.getValue(itemValue.get("Slot"))).intValue()));
                itemValue.remove("Slot");
                slot.put("item", item);
                value.set(i, TagCompound.newTag(slot));
            }
            return true;
        }

        @Override
        public boolean downgradeList(Object components, String id, List<Object> value) {
            for (int i = 0; i < value.size(); i++) {
                final Object slot = value.get(i);
                final Map<String, Object> slotValue = TagCompound.getValue(slot);
                final Object item = slotValue.get("item");
                TagCompound.set(item, "Slot", TagBase.newTag(Integer.valueOf((int) TagBase.getValue(slotValue.get("slot"))).byteValue()));
                value.set(i, item);
            }
            return true;
        }
    }

    public static class Bees implements Transformation {
        @Override
        public boolean upgradeList(Object components, String id, List<Object> value) {
            for (Object bee : value) {
                final Map<String, Object> map = TagCompound.getValue(bee);
                move(map, "EntityData", "entity_data");
                move(map, "TicksInHive", "ticks_in_hive");
                move(map, "MinOccupationTicks", "min_ticks_in_hive");
            }
            return true;
        }

        @Override
        public boolean downgradeList(Object components, String id, List<Object> value) {
            for (Object bee : value) {
                final Map<String, Object> map = TagCompound.getValue(bee);
                move(map, "entity_data", "EntityData");
                move(map, "ticks_in_hive", "TicksInHive");
                move(map, "min_ticks_in_hive", "MinOccupationTicks");
            }
            return true;
        }
    }

    private enum Color {
        WHITE,
        ORANGE,
        MAGENTA,
        LIGHT_BLUE,
        YELLOW,
        LIME,
        PINK,
        GRAY,
        LIGHT_GRAY,
        CYAN,
        PURPLE,
        BLUE,
        BROWN,
        GREEN,
        RED,
        BLACK;

        static final Color[] VALUES = values();
        static final Map<String, Byte> ORDINALS = new HashMap<>();

        static {
            for (Color value : VALUES) {
                ORDINALS.put(value.name().toLowerCase(), (byte) value.ordinal());
            }
        }
    }
}
