---
sidebar_position: 2
title: Tag Objects
description: Information about Rtag handling NBT
---

Here some utility classes to handle NBT objects using simple methods.

## TagBase

It's tha main class to handle any type of NBT object.

### Create

With `TagBase` class you can create NBT objects with any type of (normal java) object, by default it only provide support to [compatible objects](intro/#compatible-objects), `Boolean` saved as `Byte` and `UUID` saved as `String`.

```java
// Not compatible with Map or List
Object nbtObject = TagBase.newTag("Hello");

// Compatible with Map, List, and any deserializable object using Gson
RtagMirror mirror = ...;
Object nbtObject = TagBase.newTag(mirror, Map.of("greeting", "Hello"));

// Copy nbt object
Object nbtCopy = TagBase.clone(nbtObject);
```

### Get values

The `TagBase` class provide different methods to get the java object value from any NBT object, `Boolean` is get as `Byte` and `UUID` is get as `String` or `int[]`.

```java
// NBT object
Object nbtObject = ...;

// Get type ID
byte type = TagBase.getTypeId(nbtObject);

// Get value inside NBT, not compatible with Map or List
Object value = TagBase.getValue(nbtObject);

// Get value inside NBT, compatible with Map or List
// but it only provide conversion to serializable objects
// if the RtagMirror instance allows it.
RtagMirror mirror = ...;
Object value = TagBase.getValue(mirror, nbtObject);
```

## TagList

It's a more specific class to handle `NBTTagList` objects as java `List<NBTBase>`, so it has the same methods mentioned on `TagBase` but only for NBT lists.

```java
// Create NBTTagList object
Object nbtList = TagList.newTag();

// Add value
TagList.add(nbtList, TagBase.newTag("Hello"));

// Get size
int size = TagList.size(nbtList);

// Get from index
Object nbtObject = TagList.get(nbtList, 0); // index: 0

// Get the value inside NBTTagList, a List of NBTBase
List<Object> value = TagList.getValue(nbtList);

// Clear list
TagList.clear(nbtList);
```

## TagCompound

It's a more specific class to handle `NBTTagCompound` objects as java `Map<String, NBTBase>`, so it has the same methods mentioned on `TagBase` but only for NBT compounds.

```java
// Create NBTTagCompound object
Object nbtCompound = TagCompound.newTag();
// Or create NBTTagCompound using SNBT (it's also compatible with Json)
Object nbtCompound = TagCompound.newTag("{greeting:\"Hello\",someValue:123}");

// Add value
TagCompound.set(nbtCompound, "greeting", TagBase.newTag("Hello"));

// Get size
int size = TagCompound.getValue(nbtCompound).size();

// Get from key
Object nbtObject = TagCompound.get(nbtCompound, "greeting");

// Get the value inside NBTTagCompound, a Map of NBTBase
Map<String, Object> value = TagCompound.getValue(nbtCompound);

// Get the NBTTagCompound as Json String
String json = TagCompound.getJson(nbtCompound);

// Clear compound
TagCompound.clear(nbtCompound);
```