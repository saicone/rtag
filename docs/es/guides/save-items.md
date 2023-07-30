---
sidebar_position: 2
title: Guardar data de items
description: Como guardar items en bases de datos o archivos de configuración
---

Al manejar los items probablemente has experimentado problemas al momento de guardarlos, eso es debido a que los items no están hechos para ser utilizados como objetos de una base de datos o archivos configurables en Minecraft vanilla.

Así que Rtag ofrece una forma fácil de manejar items en diferentes tipos de data utilizando la instancia de [item stream](feature/stream.md#itemtagstream), manteniendo compatibilidad a través de versiones a diferencia del serializador de Bukkit que no puede convertir el nuevo formato de los items en sus versiones viejas.

## Guardar en base de datos

Por ejemplo: tienes algún sistema de database en tu plugin como MySQL o archivos `.json`, así que necesitas guardar los items de una manera eficiente.

Al convertir los items en el formato de Base64 puedes tener toda la data de los items comprimida para ser utilizada cuando sea solicitada.

```java
List<ItemStack> items = ...;

// Comprimir
String data = ItemTagStream.INSTANCE.listToBase64(items);

// [ Para luego guardar la data en la base de datos ]
```

Ahora convertir la data guardada en el formato original

```java
// Obtener desde la base de datos
String data = ...;

List<ItemStack> sameItems = ItemTagStream.INSTANCE.listFromBase64(base64);
```

## Guardar como configurable

Para permitir que los usuarios vean los items como un archivo configurable (y configurarlo sin problema) puedes utilizar la conversión de map legible para convertir el nombre y lore del item como un string con color envés del formato de componente de chat, y así convertir ese `Map` en un objeto configurable.

```java
ItemStack item = ...;

// Convertir
Map<String, Object> map = ItemTagStream.INSTANCE.toReadableMap(item);

// [ Luego guardarlo en una configuración ]
```

Ahora convertir el `Map` en el item original

```java
// Obtener desde la configuración
Map<String, Object> map = ...;

ItemStack item = ItemTagStream.INSTANCE.fromReadableMap(map);
```