---
sidebar_position: 3
title: Objetos de Minecraft
description: Information sobre objetos de minecraft en Rtag
---

Aquí algunas classes útiles para manejar objetos de Minecraft y objetos de CraftBukkit utilizando métodos simples.

## Objeto de item

El class `ItemObject` sirve para manejar items de Bukkit y Minecraft utilizando métodos simples compatibles con varias versiones.

### Crear

Crear un `ItemStack` de Minecraft utilizando un `NBTTagCompound`, también mediante otros formatos.

```java
// Crear utilizando un compound
Object compound = ...;
Object item = ItemObject.newItem(compound);

// Crear utilizando un SNBT (o json)
String snbt = "{id:\"minecraft:diamond_sword\"}";
Object item = ItemObject.newItem(TagCompound.newTag(snbt));
```

### Convertir

Convertir items de Bukkit y Minecraft.

```java
ItemStack item = ...;

// Convertir en un ItemStack de Minecraft
Object mcItem = ItemObject.asNMSCopy(item);

// Convertir en un ItemStack de Bukkit
ItemStack sameItem = ItemObject.asBukkitCopy(mcItem);
```

### Editar

Editar varias cosas de los `ItemStack` de Bukkit y Minecraft.

```java
Object item = ItemObject.newItem(TagCompound.newTag("{id:\"minecraft:diamond_sword\"}"));

// Guardar los datos del item en un NBTTagCompound
Object compound = ItemObject.save(item);

// Obtener el tag del item como un NBTTagCompound
Object tag = ItemObject.getCustomDataTag(item);
// Reemplazar el tag del item
ItemObject.setCustomDataTag(item, tag);


// Cualquier ItemStack o CraftItemStack
ItemStack item = ...;

// Obtener el ItemStack manejado por un CraftItemStack
// O convertir el ItemStack de Bukkit en un ItemStack de Minecraft
Object mcItem = ItemObject.getHandle(item);
// Reemplazar el ItemStack manejado por el CraftItemStack
// O cargar el ItemStack de Minecraft dentro del ItemStack de Bukkit
ItemObject.setHandle(item, mcItem);
```

## Objeto de entidad

El class `EntityObject` sirve para manejar entidades de Bukkit y Minecraft utilizando métodos simples compatibles con varias versiones.

### Convertir

Convertir entidades de Bukkit y Minecraft.

```java
Entity entity = ...;

// Convertir en un Entity de Minecraft
Object mcEntity = EntityObject.getHandle(entity);

// Convertir en un Entity de Bukkit
Entity sameEntity = EntityObject.getEntity(mcEntity);
```

### Editar

Editar varias cosas de los `Entity` de Minecraft.

```java
Object entity = ...;

// Guardar los datos de la entidad en un NBTTagCompound
Object compound = EntityObject.save(entity);
// Cargar los datos en una entidad utilizando un NBTTagCompound
EntityObject.load(entity, compound);
```

## Objeto de bloque

El class `BlockObject` sirve para manejar bloques de Bukkit y Minecraft utilizando métodos simples compatibles con varias versiones.

### Convertir

Convertir cualquier `Block` de Bukkit en un `TileEntity` de Minecraft (solo si es aplicable).

```java
Block block = ...;

// Convertir en un TileEntity de Minecraft
Object mcTileEntity = BlockObject.getTileEntity(block);
```

### Editar

Editar varias cosas de los `TileEntity` de Minecraft.

```java
Object tileEntity = ...;

// Guardar los datos del TileEntity en un NBTTagCompound
Object compound = BlockObject.save(tileEntity);
// Cargar los datos en un TileEntity utilizando un NBTTagCompound
BlockObject.load(tileEntity, compound);
```