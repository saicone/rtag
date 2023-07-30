---
sidebar_position: 3
title: RtagEntity
description: Edit entity NBT
---

:::info Before continue

To understand this page you should see [RtagEditor guide](/usage/editor.md).

For better understand about some entity tags it's suggested to visit the [Minecraft wiki page](https://minecraft.fandom.com/wiki/Entity_format).

:::

:::tip Looking for non-vanilla tags?

If you want to add non-vanilla tags to entities, take a look hover [compatible APIs](/feature/compatible.md) section.

For now Rtag does not offer a "safe way" to save non-vanilla tags in old Bukkit versions.

:::

`RtagEntity` is an instance of `RtagEditor`, so uses the same methods to edit, load and update changes as editor.

## Create

There are multiple ways to create a `RtagEntity` instance.

### Instance

Using the simple constructor that accept any `Entity`.

```java
Entity entity = ...;

RtagEntity tag = new RtagEntity(entity);
```

Or specify the Rtag instance to handle NBT.

```java
Entity entity = ...;
Rtag rtag = ...;

RtagEntity tag = new RtagEntity(rtag, entity);
```

### Method

Using the simple method that accept any `Entity`.

```java
Entity entity = ...;

RtagEntity tag = RtagEntity.of(entity);
```

Or specify the Rtag instance to handle NBT.

```java
Entity entity = ...;
Rtag rtag = ...;

RtagEntity tag = RtagEntity.of(rtag, entity);
```

### Function

Using functions to edit NBT is the most easy way to handle `RtagEntity`.

You can edit the provided `Entity` without replacing it.

```java
Entity entity = ...;

// Edit entity
RtagEntity.edit(entity, tag -> {
	tag.set(123, "path");
});

// Specify Rtag instance
Rtag rtag = ...;
RtagEntity.edit(rtag, entity, tag -> {
	tag.set(123, "path");
});
```

Take in count that method return the `Entity` itself with changes loaded.

```java
Entity entity = ...;

// Edit entity
Entity sameEntity = RtagEntity.edit(entity, tag -> {
	tag.set(123, "path");
});

// Specify Rtag instance
Rtag rtag = ...;
Entity sameEntity = RtagEntity.edit(rtag, entity, tag -> {
	tag.set(123, "path");
});
```

And you can return any type of object specified on the same function method.

```java
Entity entity = ...;

// Return as you want from RtagEditor instance
int number = RtagEntity.edit(entity, tag -> {
	return tag.get("path");
});
```

## Edit

There is some methods that `RtagEntity` contains and `RtagEditor` don't.

### Function

As above functions the current `RtagEntity` can be edited with a function that return the instance itself.

```java
RtagEntity tag = ...;

tag.edit(tag -> {
	tag.set(123, "path");
	tag.set("Hello", "greeting");
});
```

### Instance methods

There are **easy to use** methods to edit **entity known tags** in a simple way, having a wide Minecraft version support.

**Health**: Edit entity health.

```java
RtagEntity tag = ...;

tag.setHealth(170f);

float health = tag.getHealth();
```

**Attributes**: Handle attributes values.

```java
RtagEntity tag = ...;

tag.setAttributeBase("generic.attackDamage", 0.5d);
// Same has above
tag.setAttributeValue("generic.attackDamage", "Base", 0.5d);

double damage = tag.getAttributeBase("generic.attackDamage");
```