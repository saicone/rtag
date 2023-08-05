---
sidebar_position: 6
title: Chat Component
description: How to use chat component class.
---

The `ChatComponent` class contains multiple utility methods to handle chat-related things.

## Conversion

With `ChatComponent` class you can convert (json) strings into chat components and viceversa.

```java
// To component
Object component = ChatComponent.fromJson("{\"bold\":true,\"italic\":false,\"color\":\"dark_purple\",\"text\":\"Colored text!\"}");
Object sameComponent = ChatComponent.fromString("§5§lColored text!");

// From component
String json = ChatComponent.toJson(component);
String string = ChatComponent.toString(component);

// Cross-compatibility
String json = ChatComponent.toJson("§5§lColored text!");
String string = ChatComponent.toString("{\"bold\":true,\"italic\":false,\"color\":\"dark_purple\",\"text\":\"Colored text!\"}");
```

## Pretty nbt formatter

Format any nbt object into single or multiple lines separated by `\n` with a color palette:

* `NBT_PALETTE` - The default color palette like Minecraft `/data` command (`[ "§f", "§b", "§a", "§6", "§c" ]`).
* `NBT_PALETTE_HEX` - Modified color palette for Bukkit 1.16 or upper using hex color format like `§#RRGGBB`.
* `NBT_PALETTE_BUNGEE` - Modified color palette for Bukkit 1.16 or upper using bungee hex color format like `§x§R§R§G§G§B§B`.
* `NBT_PALETTE_MINIMESSAGE` - Default color palette using [Adventure MiniMessage format](https://docs.advntr.dev/minimessage/index.html).

Example with default color palette:

![Pretty NBT](https://raw.githubusercontent.com/saicone/rtag/main/docs/images/pretty-nbt.png)

::: info

You can make your own color palette by providing a 5-length `String[]` with color in the order of:

1. Base color.
2. Key color (compound keys).
3. Strings.
4. Numbers.
5. Number suffix.

:::

Example with custom color palette:

![Pretty NBT with palette](https://raw.githubusercontent.com/saicone/rtag/main/docs/images/pretty-nbt-palette.png)

### Single line

To format into single line just use `null` for second argument.

```java
Object nbt = ...;

// Pretty format into chat component, json component, colored string
Object component = ChatComponent.toPrettyComponent(nbt, null);
String json = ChatComponent.toPrettyJson(nbt, null);
String colored = ChatComponent.toPrettyString(nbt, null);

// Using color palette (example with colored string)
String colored = ChatComponent.toPrettyString(nbt, null, ChatComponent.NBT_PALETTE_BUNGEE);
```

### Multiple lines

To format into multiple lines just provide a `String` to use as indent, for example `"  "` for 2 spaces indent.

```java
Object nbt = ...;

// Pretty format into chat component, json component, colored string
Object component = ChatComponent.toPrettyComponent(nbt, "  ");
String json = ChatComponent.toPrettyJson(nbt, "  ");
String colored = ChatComponent.toPrettyString(nbt, "  ");

// Using color palette (example with colored string)
String colored = ChatComponent.toPrettyString(nbt, "  ", ChatComponent.NBT_PALETTE_BUNGEE);
```