---
sidebar_position: 1
title: Explanation
description: Deep explanation about Rtag
---

Rtag is made of multiple classes that work together to handle NBT.

## Structure
![Rtag Instances](https://raw.githubusercontent.com/saicone/rtag/main/docs/images/rtag-instances.png)

## Tag Objects

Rtag contains multiple utility classes to handle NBT objects maintaining a cross-version compatibility, including the conversion of respective object to Java and vice versa.

Go to [Tag Objects section](/advanced/tags.md) for more information.

## Tag Mirror

It's the superclass of Rtag to convert any object (String, Integer, List, Map) into NBT and vice versa in a simple way using TagObject classes.

```java
// Create mirror
RtagMirror mirror = new RtagMirror();
// Use public instance
RtagMirror mirror = RtagMirror.INSTANCE;

// Create object
String myObject = "Hello";

// Convert to NBT
Object nbtTag = mirror.newTag(myObject);
// Copy NBT
Object nbtTagCopy = mirror.copy(nbtTag);

// Get the java value from NBT object
String sameObject = (String) mirror.getTagValue(nbtTag);
```

## Main Rtag instance

It's the Rtag itself, with this instance you can edit any `NBTTagCompound` and `NBTTagList` in a simple way, you can add and remove objects using a tree-like path format.

Including compatibility with custom objects by registering an object (de)serializer, for more information visit the [custom object](/feature/custom-objects.md) feature page.

```java
// Create Rtag
Rtag rtag = new Rtag();
// Use public instance
Rtag rtag = Rtag.INSTANCE;

// NBTTagCompound object
Object compound = ...;

String str = "My String";
// Set into "my -> saved -> object" path
rtag.set(compound, str, "my", "saved", "object");

// Get from "my <- save <- object"
String sameStr = rtag.get(compound, "my", "saved", "object");

// Remove from path
rtag.remove(compound, "my", "saved", "object");
```

## Minecraft Objects

Various types of utility classes to interact hover CraftBukkit objects and Minecraft objects using reflected methods.

Go to [Minecraft Objects section](/advanced/minecraft.md) for more information.