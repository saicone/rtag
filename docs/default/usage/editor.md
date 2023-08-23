---
sidebar_position: 1
title: RtagEditor
description: Main object to edit NBT easily
---

The `RtagEditor` instances convert a Bukkit object into Minecraft server object and provide an easy way to edit the tag (NBTTagCompound) inside, using a Rtag instance as helper.

## Edit

To understand `RtagEditor` you first need known how to edit the current tag.

### Simple methods

The editor instance simplify any edit with automatic conversion and providing an tree-like format to set, get and remove objects using paths.

```java
// Any RtagEditor instance (RtagItem, RtagEntity, RtagBlock)
RtagEditor tag = ...;

String str = "My String";
// Set into "my -> saved -> object" path
tag.set(str, "my", "saved", "object");

// Get from "my <- save <- object"
String sameStr = tag.get("my", "saved", "object");

// Remove from path
tag.remove("my", "saved", "object");
```

You probably noticed there's not an explicit setter and getter like `setString` or `getString`, that because Rtag do "magic" by convert any NBT object into normal java object.

Take in count Rtag only return the converted object, it doesn't known if you want a String, Integer, Float... etc, on normal getter operations **you should be sure that the object you want is that type of object** or the returned object will be null after fail cast or simply it doesn't exist at provided path.

If you don't known what type of object is it, or the Rtag instance doesn't provide a conversion because the [compatible objects](../../intro/#compatible-objects) are limited, you can use an [optional type](../../feature/types/) getter that provide a wide variety of conversions and null check for your needs.

```java
// Any RtagEditor instance (RtagItem, RtagEntity, RtagBlock)
RtagEditor tag = ...;

String str = "123";
// Set into "my -> saved -> object" path
tag.set(str, "my", "saved", "object");

// Cast the object as String (original) or get "456" if doesn't exist or cast fails
String s = tag.getOptional("my", "saved", "object").or("456");

// Same as above, but the optional type try to convert any type of object to String
String s = tag.getOptional("my", "saved", "object").asString("456");

// So it works to convert as other object type
int numValue = tag.getOptional("my", "saved", "object").asInt(-1); // -1 as default int
```

:::tip

Check out **[custom objects guide](../../feature/custom-objects/)** if you want to save any type of serializable object.

:::

### Transformation

Using transformation methods you can merge and move paths easily.

```java
// Any RtagEditor instance (RtagItem, RtagEntity, RtagBlock)
RtagEditor tag = ...;

// Merge values into tag
tag.merge(Map.of("asd", 123, "someKey", 41), true);

// Move from path to any path
tag.move(new Object[] {"asd"}, new Object[] {"my", "saved", "object"});
```

### Collections

With Rtag is easy to handle list of objects, with `add` method you can add objects to list at provided path, so if the List doesn't exist it will be created.

```java
// Any RtagEditor instance (RtagItem, RtagEntity, RtagBlock)
RtagEditor tag = ...;

// Create list at "my -> saved -> list" path
tag.set(new ArrayList(), "my", "saved", "list");

byte num = 3;
// Add into list
tag.add(num, "my", "saved", "list");

// Get the list
List<Byte> list = tag.get("my", "saved", "list");

// If you don't known what type of list is, you can make a conversion with optional type
List<Byte> list = tag.getOptional("my", "saved", "list").asList(OptionalType::asByte);
```

:::info

Take in count the NBT lists accept any type of object if the list is empty, because **the first object define the NBT list type**.

:::

### Check

To check if any tag exist or not at defined path, you can use `hasTag` or `hasNotTag` methods.

```java
// Any RtagEditor instance (RtagItem, RtagEntity, RtagBlock)
RtagEditor tag = ...;

boolean exist = tag.hasTag("my", "saved", "object");
boolean notExist = tag.notHasTag("my", "saved", "object");
```

### Enums

:::info

This is an advanced feature, you can ignore this because is rarely used.

:::

RtagEditor instances can handle Enums as bit fields, providing an easy way to read and write any type of object with ordinal value.

For example, if `MyEnum` has the values `FIRE, GLOW, INVISIBLE` you can have a `Set` of those enum values in the same field saved as Integer starting with the definition that `FIRE` ordinal is `0`, `GLOW` is `1` and `INVISIBLE` is `2`.

```java
// Any RtagEditor instance (RtagItem, RtagEntity, RtagBlock)
RtagEditor tag = ...;

// Add values to enum set at "my -> saved -> enum" path
tag.addEnum(MyEnum.FIRE, "my", "saved", "enum");
tag.addEnum(MyEnum.INVISIBLE, "my", "saved", "enum");

// Get enum set of values
Set<MyEnum> set = tag.getOptional("my", "saved", "enum").asEnumSet(MyEnum.class);
```

## Load

After edit tag is necessary to load changes into Minecraft server object and provided Bukkit object.

### Save edits

The `load` method save the changes (load into).

```java
// Any RtagEditor instance (RtagItem, RtagEntity, RtagBlock)
RtagEditor tag = ...;

String str = "My String";
// Set into "my -> saved -> object" path
tag.set(str, "my", "saved", "object");

// Load into provided object
tag.load();
```

## Update

If you edit the provided object, is need to update the current tag to continue to use it in the same `RtagEditor` instance.

### Get changes

```java
// Any RtagEditor instance (RtagItem, RtagEntity, RtagBlock)
RtagEditor tag = ...;

// <The provided object (item, entity or block) was edited>

// Update current tag
tag.update();
```