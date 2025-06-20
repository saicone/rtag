---
sidebar_position: 2
title: Objetos de Tag
description: Información sobre Rtag manejando NBT
---

Aquí algunas classes útiles para manejar objetos NBT utilizando métodos simples.

## TagBase

Es el class principal para manejar cualquier tipo de objeto NBT.

### Crear

Con el class `TagBase` puede crear objetos NBT con cualquier tipo de objeto (normal de java), por defecto solo tiene soporte para los [objetos compatibles](../intro.md#objetos-compatibles), `Boolean` guardado como `Byte` y `UUID` guardado como `String`.

```java
// Este método NO es compatible con Map o List
Object nbtObject = TagBase.newTag("Hello");

// Este método SI es compatible con Map, List y cualquier objeto deserializable utilizando Gson
RtagMirror mirror = ...;
Object nbtObject = TagBase.newTag(mirror, Map.of("greeting", "Hello"));

// Copiar el objeto NBT
Object nbtCopy = TagBase.clone(nbtObject);
```

### Obtener valores

El class `TagBase` tiene diferentes métodos para obtener el equivalente en un objeto de Java desde cualquier objeto NBT, el `Boolean` es obtenido como `Byte` y el `UUID` es obtenido como `String` o `int[]`.

```java
// Objeto NBT
Object nbtObject = ...;

// Obtener el ID que representa el tipo de NBT
byte type = TagBase.getTypeId(nbtObject);

// Obtener el equivalente en un objeto de Java
// Este método NO es compatible con Map o List
Object value = TagBase.getValue(nbtObject);

// Obtener el equivalente en un objeto de Java
// Este método SI es compatible con Map o List, pero
// solo proveé conversión con objetos serializables si
// la instancia de RtagMirror lo permite
RtagMirror mirror = ...;
Object value = TagBase.getValue(mirror, nbtObject);
```

## TagList

Es un class más específico para manejar cualquier `NBTTagList` como si fuera en java un `List<NBTBase>`, tambíén tiene los métodos mencionados anteriormente del class `TagBase`, pero solo funcionan para las listas de NBT.

```java
// Crear un NBTTagList
Object nbtList = TagList.newTag();

// Agregar un valor
TagList.add(nbtList, TagBase.newTag("Hello"));

// Obtener el tamaño de la lista
int size = TagList.size(nbtList);

// Obtener un valor desde su posición
Object nbtObject = TagList.get(nbtList, 0); // posición: 0

// Obtener el valor dentro de la instancia de NBTTagList, una lista de NBTBase
List<Object> value = TagList.getValue(nbtList);

// Limpiar la lista
TagList.clear(nbtList);
```

## TagCompound

Es un class más específico para manejar cualquier `NBTTagCompound` como si fuera en java un `Map<String, NBTBase>`, tambíén tiene los métodos mencionados anteriormente del class `TagBase`, pero solo funcionan para los NBTTagCompound.

```java
// Crear un NBTTagCompound
Object nbtCompound = TagCompound.newTag();
// O crear un NBTTagCompound utilizando un SNBT (también es compatible con json)
Object nbtCompound = TagCompound.newTag("{greeting:\"Hello\",someValue:123}");

// Agregar un valor
TagCompound.set(nbtCompound, "greeting", TagBase.newTag("Hello"));

// Obtener el tamaño del compound
int size = TagCompound.getValue(nbtCompound).size();

// Obtener un valor utilizando su key
Object nbtObject = TagCompound.get(nbtCompound, "greeting");

// Obtener el valor dentro de la instancia de NBTTagCompound, un Map de NBTBase
Map<String, Object> value = TagCompound.getValue(nbtCompound);

// Obtener el NBTTagCompound como si fuera un String en formato Json
String json = TagCompound.getJson(nbtCompound);

// Limpiar el compound
TagCompound.clear(nbtCompound);
```