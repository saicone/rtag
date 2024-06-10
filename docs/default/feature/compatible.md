---
sidebar_position: 7
title: Compatible APIs
description: List of compatible APIs that can be used with Rtag
---

There are some compatible APIs that Rtag can interact with.

## Bukkit PersistentDataContainer

The `PersistentDataContainer` API was introduced in Bukkit 1.14 to save custom tags at different objects like items, entities and tile entities, making NBT tag editor easier for most developers, basically the same utility as Rtag but limited to custom tags, only with [compatible objects](../../intro/#compatible-objects) and plugin instance usage, so you need to understand NBT tags to make something functional.

With Rtag it's possible to edit objects from `PersistentDataContainer` because are saved in tag paths:

* Items and tile entities: `PublicBukkitValues -> <plugin>:<key>`.
* Entities: `BukkitValues -> <plugin>:<key>`.
* Chunks: `ChunkBukkitValues -> <plugin>:<key>`.

### Example

For example, if a plugin named "CoolPlugin" save a `String` value into "asd" key using `PersistentDataContainer` API, you can edit it by using the following method.

```java
// Using item
RtagItem tag = new RtagItem(item);
// Using block
RtagBlock tag = new RtagBlock(block);

// Get
String value = tag.get("PublicBukkitValues", "coolplugin:asd");
// Set
String str = "Hello";
tag.get(str, "PublicBukkitValues", "coolplugin:asd");


// Using entity
RtagEntity tag = new RtagEntity(entity);

// Get
String value = tag.get("BukkitValues", "coolplugin:asd");
// Set
String str = "Hello";
tag.get(str, "BukkitValues", "coolplugin:asd");
```

## NBT Injector from Item-NBT-API

The NBT injector feature from Item-NBT-API allows to save custom tags to entities and tile entities, same has `PersistentDataContainer` but it's compatible with versions older than Bukkit 1.14.

With Rtag it's possible to edit those custom tags because are saved at `__extraData` path.

### Example

Edits are the same, but all paths start with `__extraData`.

```java
// Using entity
RtagEntity tag = new RtagEntity(entity);
// Using block
RtagBlock tag = new RtagBlock(block);

String str = "My String";
// Set into "my -> saved -> object" path
tag.set(str, "__extraData", "my", "saved", "object");

// Get from "my <- save <- object"
String sameStr = tag.get("__extraData", "my", "saved", "object");

// Remove from path
tag.remove("__extraData", "my", "saved", "object");
```