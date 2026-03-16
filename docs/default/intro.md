---
sidebar_position: 2
title: Introduction
description: Basic information about NBT tags
---

## What is NBT

The Named Binary Tag (NBT) format is a tree data structure used by Minecraft to save data in different formats starting with bytes, an NBT is just a simple Java object (String, Integer, List.. etc) defined with a key.

The NBT format is commonly used to save Minecraft objects like Worlds, Items, Entities... etc.

The NBT classes inside Minecraft code extends `Tag` and reference the common Java objects: `StringTag`, `IntTag`, `LongTag`, `ListTag`... etc. The main NBT class would be `CompoundTag` which refers to a Java Map and is the basic object to store NBT objects with their respective key (`Map<String, Tag>`).

## Compatible objects

The NBT objects defined by their type IDs are:

1. **ByteTag**: Same has `byte` in Java.
2. **ShortTag**: Same has `short` in Java.
3. **IntTag**: Same has `int` in Java.
4. **LongTag**: Same has `long` in Java.
5. **FloatTag**: Same has `float` in Java.
6. **DoubleTag**: Same has `double` in Java.
7. **ByteArrayTag**: Same has `byte[]` in Java.
8. **StringTag**: Same has `String` in Java.
9. **ListTag**: Same has `List<Tag>` in Java.
10. **CompoundTag**: Same has `Map<String, Tag>` in Java.
11. **IntArrayTag**: Same has `int[]` in Java.
12. **LongArrayTag**: Same has `long[]` in Java (Added on MC 1.12).

:::info Boolean object

As you can see the booleans are not compatible, because are saved as `byte` (`ByteTag`).

So take in count that Java by default cannot convert `byte` as `boolean` (You will see a better explanation next).

:::

## About Bukkit

First of all, "NMS" means `net.minecraft.server`, it's a package on old Bukkit versions before "universalization" (Bukkit 1.17 mappings) which contains all classes from the original Minecraft server distributed by Mojang, Bukkit uses it to work as server.

Most recent versions of Bukkit (from Bukkit 1.17) have different paths for Minecraft server classes, for example `net.minecraft.world.level.World`.

The NMS is usually known by using it with [reflection](https://www.oracle.com/technical-resources/articles/java/javareflection.html) because Mojang constantly changes the methods names and uses [obfuscators](https://www.javatpoint.com/java-obfuscator) in his code.

So it has been a frequent problem for developers that use NBT directly instead of methods provided by Bukkit, like PersistentDataContainer introduced on Bukkit 1.14.

## Rtag Magic

To simplify NBT usage, the Rtag (readable tag) library provide an easy way to handle tags.

Rtag (try to) convert any Java object into `Tag` and viceversa, in order to handle NBT as normal objects in a easy way.

```java
// GET - You will see as normal object
Tag -> Object
// SET - The server will save as Tag object
Object -> Tag
```

Currently limited by `boolean` conversion, so it's suggested to get them as `byte` or run an [explicit conversion](feature/types.md#conversion).

```java
// get from path
byte data = rtag.get(compound, "path");
// convert to boolean
boolean bool = data == (byte) 1;

// or get by explicit conversion
boolean bool = rtag.getOptional(compound, "path").asBoolean(false); // false by default
```