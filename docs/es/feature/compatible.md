---
sidebar_position: 7
title: APIs compatibles
description: Lista de APIs que pueden ser utilizadas con Rtag
---

Existen algunas APIs compatibles las cuales Rtag puede interactuar con estas.

## PersistentDataContainer de Bukkit

La api de `PersistentDataContainer` fue introducida en la versión 1.14 de Bukkit para guardar tags custom en diferentes objetos como items, entidades y tile entity, haciendo la edición de los tags de NBT más fácil para muchos desarrolladores, básicamente la misma utilidad que Rtag pero limitada a solo editar tags custom, solamente compatible con los [objetos compatibles](intro.md#objetos-compatibles) y utilizando la instancia de un plugin, además de ser necesario el saber editar NBT para hacer algo funcional.

Con Rtag es posible editar objetos del `PersistentDataContainer` debido a que son guardados en la ruta `PublicBukkitValues -> <plugin>:<key>`.

### Ejemplo

Por ejemplo, si un plugin llamado "CoolPlugin" guarda el valor de un `String` en el key "asd" utilizando el API de `PersistentDataContainer`, podrás editarlo usando el siguiente método.

```java
// Usando un item
RtagItem tag = new RtagItem(item);
// Usando una entidad
RtagEntity tag = new RtagEntity(entity);
// Usando un bloque
RtagBlock tag = new RtagBlock(block);

// Obtener
String value = tag.get("PublicBukkitValues", "coolplugin:asd");

// Establecer
String str = "Hello";
tag.get(str, "PublicBukkitValues", "coolplugin:asd");
```

## Inyector de NBT del Item-NBT-API

La característica del inyector del Item-NBT-API permite guardar tags custom en las entidades y tile entity, lo mismo que `PersistentDataContainer` pero compatible con versiones más viejas que la 1.14 de Bukkit.

Con Rtag es posible editar esos tags custom debido a que están guardados en la ruta `__extraData`.

### Ejemplo

Las ediciones son las mismas, pero todas las rutas empiezan con `__extraData`.

```java
// Usando una entidad
RtagEntity tag = new RtagEntity(entity);
// Usando un bloque
RtagBlock tag = new RtagBlock(block);

String str = "My String";
// Establecer en la ruta "my -> saved -> object"
tag.set(str, "__extraData", "my", "saved", "object");

// Obtener desde "my <- save <- object"
String sameStr = tag.get("__extraData", "my", "saved", "object");

// Obtener desde la ruta
tag.remove("__extraData", "my", "saved", "object");
```