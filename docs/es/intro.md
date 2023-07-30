---
sidebar_position: 2
title: Info Básica
description: Información básica para entender el NBT
---

## ¿Qué is NBT?

El NBT por sus siglas en inglés significa "Etiqueta Binaria con Nombre" (Named Binary Tag), el cual es un formato al estilo de una estructura de datos de árbol usado por Minecraft para guardar datos en diferentes formatos como si fuera bytes, un NBT es solamente un objeto normal de java (String, Integer, List.. etc) con una key (llave) definida.

El formato de NBT es comúnmente utilizado para guardar objetos de Minecraft como mundos, items, entidades... etc.

Los classes de NBT dentro del código de Minecraft están extendidos por `NBTBase` y hacen referencia a objetos normales de Java: `NBTTagString`, `NBTTagInt`, `NBTTagLong`, `NBTTagList`... etc. El class principal de las estructuras de datos NBT sería el `NBTTagCompound` que se refiere a un Map en Java y es el objeto básico para guardar objetos NBT con sus respectivas keys (llaves) en un `Map<String, NBTBase>`.

## Objetos compatibles

Los objetos NBT definidos por su número de ID son:

1. **NBTTagByte**: Lo mismo que `byte` en Java.
2. **NBTTagShort**: Lo mismo que `short` en Java.
3. **NBTTagInt**: Lo mismo que `int` en Java.
4. **NBTTagLong**: Lo mismo que `long` en Java.
5. **NBTTagFloat**: Lo mismo que `float` en Java.
6. **NBTTagDouble**: Lo mismo que `double` en Java.
7. **NBTTagByteArray**: Lo mismo que `byte[]` en Java.
8. **NBTTagString**: Lo mismo que `String` en Java.
9. **NBTTagList**: Lo mismo que `List<NBTBase>` en Java.
10. **NBTTagCompound**: Lo mismo que `Map<String, NBTBase>` en Java.
11. **NBTTagIntArray**: Lo mismo que `int[]` en Java.
12. **NBTTagLongArray**: Lo mismo que `long[]` en Java (Agregado en MC 1.12).

:::info ¿Y el objeto de boolean?

Seguramente notaste que los booleans no son compatibles, eso es porque son guardados como `byte` (`NBTTagByte`).

Así que toma en cuenta que Java por defecto no puede convertir un `byte` en `boolean` de manera automática (pero si al revés jaja), verás una mejor explicación después.

:::

## Sobre Bukkit

Primero que nada, el "NMS" hace referencia al package `net.minecraft.server` que se utilizaba en versiones viejas de Bukkit antes de la "universalización" (Lo que sucedió en el Bukkit 1.17 con sus mappings) el cual contiene diferentes classes del servidor original de Minecraft distribuído por Mojang, utilizado por Bukkit para funcionar como un servidor.

Las versiones más recientes de Bukkit (desde la 1.17) tiene diferentes packages para los classes del código de Minecraft, por ejemplo `net.minecraft.world.level.World`.

El NMS es conocido popularmente por utilizarse con [reflection](https://www.oracle.com/technical-resources/articles/java/javareflection.html) ya que Mojang cambia constantemente los métodos y utiliza [ofuscadores](https://www.javatpoint.com/java-obfuscator) en su código.

Así que ha sido un problema frecuente para los desarrolladores que utilizan el NBT directamente envés de los métodos ofrecidos por Bukkit, como el PersistentDataContainer introducido en la versión 1.14 de Bukkit.

## La magia de Rtag

Para simplificar el uso de NBT, la librería Rtag explicada en inglés como "Tag legible" (readable tag) proveé una forma fácil para manejar los NBTTagCompound y los otros objetos compatibles.

Rtag hace (o lo intenta) una conversión de cualquier objeto de Java en un `NBTBase` y viceversa, y de esta manera manejar los NBT como si fueran objetos normales de java en una forma fácil.

```java
// OBTENER - Lo verás como un objeto normal
NBTBase -> Object
// ESTABLECER - El servidor lo guardará como un objeto NBTBase
Object -> NBTBase
```

Actualmente limitado por la conversión de `boolean`, así que es sugerido obtenerlos como `byte` o ejecutar una [conversión explícita](feature/types/#conversion).

```java
// obtener desde la ruta
byte data = rtag.get(compound, "path");
// convertir en boolean
boolean bool = data == (byte) 1;

// o convertir utilizando una conversión explícita
boolean bool = rtag.getOptional(compound, "path").asBoolean(false); // false por defecto
```