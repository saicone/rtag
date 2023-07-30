---
sidebar_position: 3
title: Minecraft Objects
description: Information about minecraft objects in Rtag
---

Here some utility classes to handle Minecraft server and Craftbukkit objects using simple methods.

## Item Object

The `ItemObject` is an utility class that allow to handle Bukkit and Minecraft items with simple methods across supported versions.

### Create

Create Minecraft `ItemStack` using `NBTTagCompound`, so it allow to get from different formats.

```java
// Create from compound
Object compound = ...;
Object item = ItemObject.newItem(compound);

// Create from SNBT (or json)
String snbt = "{id:\"minecraft:diamond_sword\"}";
Object item = ItemObject.newItem(TagCompound.newTag(snbt));
```

### Convert

Convert items from Bukkit and Minecraft.

```java
ItemStack item = ...;

// Convert to Minecraft ItemStack
Object mcItem = ItemObject.asNMSCopy(item);

// Convert to Bukkit ItemStack
ItemStack sameItem = ItemObject.asBukkitCopy(mcItem);
```

### Edit

Edit various things of Bukkit and Minecraft `ItemStack`.

```java
Object item = ItemObject.newItem(TagCompound.newTag("{id:\"minecraft:diamond_sword\"}"));

// Save into NBTTagCompound
Object compound = ItemObject.save(item);
// Load NBTTagCompound into item
ItemObject.load(item, compound);

// Get item tag as NBTTagCompound
Object tag = ItemObject.getTag(item);
// Set item tag
ItemObject.setTag(item, tag);


// Normal ItemStack or CraftItemStack
ItemStack item = ...;

// Get handle from CraftItemStack or convert Bukkit ItemStack to Minecraft ItemStack
Object mcItem = ItemObject.getHandle(item);
// Override handle of CraftItemStack or load Minecraft ItemStack into Bukkit ItemStack
ItemObject.setHandle(item, mcItem);
```

## Entity Object

The `EntityObject` is an utility class that allow to handle Bukkit and Minecraft entities with simple methods across supported versions.

### Convert

Convert entities from Bukkit and Minecraft.

```java
Entity entity = ...;

// Convert to Minecraft Entity
Object mcEntity = EntityObject.getHandle(entity);

// Convert to Bukkit Entity
Entity sameEntity = EntityObject.getEntity(mcEntity);
```

### Edit

Edit various things of Minecraft `Entity`.

```java
Object entity = ...;

// Save into NBTTagCompound
Object compound = EntityObject.save(entity);
// Load NBTTagCompound into entity
EntityObject.load(entity, compound);
```

## Block Object

The `BlockObject` is an utility class that allow to handle Bukkit and Minecraft blocks and tile entities with simple methods across supported versions.

### Convert

Convert Bukkit `Block` into Minecraft `TileEntity` (if it's aplicable).

```java
Block block = ...;

// Convert to Minecraft TileEntity
Object mcTileEntity = BlockObject.getTileEntity(block);
```

### Edit

Edit various things of Minecraft `TileEntity`.

```java
Object tileEntity = ...;

// Save into NBTTagCompound
Object compound = BlockObject.save(tileEntity);
// Load NBTTagCompound into tileEntity
BlockObject.load(tileEntity, compound);
```