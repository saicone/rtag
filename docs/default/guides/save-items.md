---
sidebar_position: 2
title: Save items data
description: How to save items in database or configuration files
---

When handling items you probably experienced problems at item saving, that because items are not mean to be used has database or configurable objects on vanilla Minecraft.

So Rtag offers an easier way to handle items into different types of data by using [item stream](feature/stream.md#itemtagstream) instance, maintaining compatibility across versions instead of Bukkit serializer that cannot convert new item format into old one.

## Save on database

For example: you have some database system in your plugin like MySQL or `.json` files, so you need to save items in an efficient way.

By convert items into Base64 format you can have all the items data compressed and ready to use when it is retrieved.

```java
List<ItemStack> items = ...;

// Compress
String data = ItemTagStream.INSTANCE.listToBase64(items);

// [ Then save data into database ]
```

Then convert saved data into original format

```java
// Get from database
String data = ...;

List<ItemStack> sameItems = ItemTagStream.INSTANCE.listFromBase64(base64);
```

## Save as configurable

To allow users view saved items as configuration file (and configurate it as well) you can use the readable map conversion that parse item name and lore as colored string instead of chat component format, then convert that `Map` as configuration object.

```java
ItemStack item = ...;

// Convert
Map<String, Object> map = ItemTagStream.INSTANCE.toReadableMap(item);

// [ Then save into configuration ]
```

Then convert saved `Map` into original item

```java
// Get from configuration
Map<String, Object> map = ...;

ItemStack item = ItemTagStream.INSTANCE.fromReadableMap(map);
```