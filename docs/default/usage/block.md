---
sidebar_position: 4
title: RtagBlock
description: Edit block NBT
---

:::info Before continue

To understand this page you should see [RtagEditor guide](../../usage/editor/).

For better understand about some tile entity tags it's suggested to visit the [Minecraft wiki page](https://minecraft.fandom.com/wiki/Chunk_format#Block_entity_format).

:::

:::caution Current limitations

For now RtagBlock only can be used to edit tile entities tags, so **it's planned** to use Chunk PersistentDataContainer (added on Bukkit 1.16) to save any block tag.

:::

:::tip Looking for non-vanilla tags?

If you want to add non-vanilla tags to tile entities, take a look hover [compatible APIs](../../feature/compatible/) section.

For now Rtag does not offer a "safe way" to save non-vanilla tags in old Bukkit versions.

:::

`RtagBlock` is an instance of `RtagEditor`, so uses the same methods to edit, load and update changes as editor.

## Create

There are multiple ways to create a `RtagBlock` instance.

### Instance

Using the simple constructor that accept any `Block`.

```java
Block block = ...;

RtagBlock tag = new RtagBlock(block);
```

Or specify the Rtag instance to handle NBT.

```java
Block block = ...;
Rtag rtag = ...;

RtagBlock tag = new RtagBlock(rtag, block);
```

### Method

Using the simple method that accept any `Block`.

```java
Block block = ...;

RtagBlock tag = RtagBlock.of(block);
```

Or specify the Rtag instance to handle NBT.

```java
Block block = ...;
Rtag rtag = ...;

RtagBlock tag = RtagBlock.of(rtag, block);
```

### Function

Using functions to edit NBT is the most easy way to handle `RtagBlock`.

You can edit the provided `Block` without replacing it.

```java
Block block = ...;

// Edit block
RtagBlock.edit(block, tag -> {
	tag.set(123, "path");
});

// Specify Rtag instance
Rtag rtag = ...;
RtagBlock.edit(rtag, block, tag -> {
	tag.set(123, "path");
});
```

Take in count that method return the `Block` itself with changes loaded.

```java
Block block = ...;

// Edit block
Block sameBlock = RtagBlock.edit(block, tag -> {
	tag.set(123, "path");
});

// Specify Rtag instance
Rtag rtag = ...;
Block sameBlock = RtagBlock.edit(rtag, block, tag -> {
	tag.set(123, "path");
});
```

And you can return any type of object specified on the same function method.

```java
Block block = ...;

// Return as you want from RtagEditor instance
int number = RtagBlock.edit(block, tag -> {
	return tag.get("path");
});
```

## Edit

There is some methods that `RtagBlock` contains and `RtagEditor` don't.

### Function

As above functions the current `RtagBlock` can be edited with a function that return the instance itself.

```java
RtagBlock tag = ...;

tag.edit(tag -> {
	tag.set(123, "path");
	tag.set("Hello", "greeting");
});
```

### Instance methods

There are **easy to use** methods to edit **tile entity known tags** in a simple way, having a wide Minecraft version support.

**Custom name**: Edit tile entity display name (chest for example).

```java
RtagBlock tag = ...;

// Set using String with chat color or chat component json format
tag.setCustomName("§eColored name");

// Get as chat color format
String name = tag.getCustomName();
```