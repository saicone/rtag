---
sidebar_position: 2
title: RtagItem
description: Edit item NBT
---

:::info Before continue

To understand this page you should see [RtagEditor guide](usage/editor.md).

For better understand about some item tags it's suggested to visit the [Minecraft wiki page](https://minecraft.fandom.com/wiki/Player.dat_format#Item_structure).

:::

`RtagItem` is an instance of `RtagEditor`, so uses the same methods to edit, load and update changes as editor.

## Create

There are multiple ways to create a `RtagItem` instance.

### Instance

Using the simple constructor that accept any `ItemStack`.

```java
ItemStack item = ...;

RtagItem tag = new RtagItem(item);
```

Or specify the Rtag instance to handle NBT.

```java
ItemStack item = ...;
Rtag rtag = ...;

RtagItem tag = new RtagItem(rtag, item);
```

### Method

Using the simple method that accept any `ItemStack`.

```java
ItemStack item = ...;

RtagItem tag = RtagItem.of(item);
```

Or specify the Rtag instance to handle NBT.

```java
ItemStack item = ...;
Rtag rtag = ...;

RtagItem tag = RtagItem.of(rtag, item);
```

### Function

Using functions to edit NBT is the most easy way to handle `RtagItem`.

You can edit the provided `ItemStack` without replacing it.

```java
ItemStack item = ...;

// Edit item
RtagItem.edit(item, tag -> {
	tag.set(123, "path");
});

// Specify Rtag instance
Rtag rtag = ...;
RtagItem.edit(rtag, item, tag -> {
	tag.set(123, "path");
});
```

Take in count that method return the `ItemStack` itself with changes loaded.

```java
ItemStack item = ...;

// Edit item
ItemStack sameItem = RtagItem.edit(item, tag -> {
	tag.set(123, "path");
});

// Specify Rtag instance
Rtag rtag = ...;
ItemStack sameItem = RtagItem.edit(rtag, item, tag -> {
	tag.set(123, "path");
});
```

And you can return any type of object specified on the same function method.

```java
ItemStack item = ...;

// Get an item copy with changes loaded
ItemStack itemWithChanges = RtagItem.edit(item, tag -> {
	tag.set(123, "path");
	return tag.loadCopy();
});

// Return as you want from RtagEditor instance
int number = RtagItem.edit(item, tag -> {
	return tag.get("path");
});
```

## Edit

There is some methods that `RtagItem` contains and `RtagEditor` don't.

### Function

As above functions the current `RtagItem` can be edited with a function that return the instance itself.

```java
RtagItem tag = ...;

tag.edit(tag -> {
	tag.set(123, "path");
	tag.set("Hello", "greeting");
});
```

### Instance methods

There are **easy to use** methods to edit **item known tags** in a simple way, having a wide Minecraft version support.

**Flags**: Better known as HideFlags, in RtagItem the flags are handled by ordinal values.

0. Enchantments
1. AttributeModifiers
2. Unbreakable
3. CanDestroy
4. CanPlaceOn
5. Other information (stored enchants, potion effects, generation, author, explosion and fireworks)
6. Dyed
7. Palette information (armor trim)

```java
RtagItem tag = ...;

tag.addHideFlags(2, 4, 6);

boolean bool = tag.hasHideFlags(2, 6); // return true

tag.removeHideFlags(6);

tag.setHideFlags(4);
```

**Enchantments**: RtagItem support any enchantment handling by `Enchantment` enum, name `String` or id `Number` on any supported version.

```java
RtagItem tag = ...;

tag.addEnchantment("Mending", 1);

boolean bool = tag.hasEnchantment(70); // Return true because Mending ID is 70
// Same as above but using enchantment name
boolean bool = tag.hasEnchantment("Mending");

// You can use Enchantment enum from Bukkit as well
tag.removeEnchantment(Enchantment.MENDING);

tag.addEnchantment("Mending", 1);

int level = tag.getEnchantmentLevel("Mending");

// Get all enchantments as Map
Map<EnchantmentTag, Integer> enchants = tag.getEnchantments();
```

**Unbreakable**: Handle item unbreakable state (added on MC 1.7, but it can be edited using ItemMeta by Bukkit until MC 1.11).

```java
RtagItem tag = ...;

tag.setUnbreakable(true);

boolean bool = tag.isUnbreakable();
```

**CustomModelData**: Edit custom model data introduced on 1.14 in any version.

```java
RtagItem tag = ...;

tag.setCustomModelData(40);

int model = tag.getCustomModelData();
```

**RepairCost**: Edit item anvil repair cost.

```java
RtagItem tag = ...;

tag.setRepairCost(10);

int cost = tag.getRepairCost();
```

**Serialization**: Fix any bad serialized item on Bukkit 1.14 or higher.

:::info

On Minecraft 1.14, the item lore strings was moved to [chat component](feature/chat-component.md) format, so Bukkit serialized items in some way cannot be compared with other items using `ItemStack#isSimilar()` because the chat component of serialized item lore doesn't contains various unused tags.

:::

```java
RtagItem tag = ...;

tag.fixSerialization();
```

## Load

`RtagItem` changes can be loaded in additional ways, instead of `RtagEditor`.

### Get copy

Instead of load the changes into provided ItemStack, you can create a item copy with changes loaded.

```java
ItemStack original = ...;

// Create editor
RtagItem tag = new RtagItem(original);

// Edit tag
tag.set(123, "path");

// Get a copy with changes loaded
ItemStan newItem = tag.loadCopy();
```