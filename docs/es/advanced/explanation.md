---
sidebar_position: 1
title: Explicación
description: Explicación profunda sobre Rtag
---

Rtag está hecho de múltiples classes para funcionar entre sí y manejar NBT.

## Estructura

![Rtag Instances](https://raw.githubusercontent.com/saicone/rtag/main/docs/images/rtag-instances.png)

## Objetos de tag

Rtag contiene varias classes para ejecutar métodos de los objetos NBT manteniendo la compatibilidad entre versiones, incluyendo la conversión del respectivo NBT a un objeto normal de Java y viceversa.

Ve hacia la [sección de objetos de tag](advanced/tags/) para más información.

## Espejo de tags

Es la instancia principal de Rtag para convertir cualquier objeto normal de Java (String, Integer, List, Map) en NBT y viceversa de manera simple sin necesidad de acceder a las classes de objetos de tag.

```java
// Crear un espejo
RtagMirror mirror = new RtagMirror();
// Usar la instancia pública
RtagMirror mirror = RtagMirror.INSTANCE;

// Crear un objeto
String myObject = "Hello";

// Convertirlo a NBT
Object nbtTag = mirror.newTag(myObject);
// Copiar un NBT
Object nbtTagCopy = mirror.copy(nbtTag);

// Obtener el valor equivalente a un objeto de java desde un NBT
String sameObject = (String) mirror.getTagValue(nbtTag);
```

## Instancia principal de Rtag

Es el propio Rtag, con esta instancia puedes editar cualquier `NBTTagCompound` y `NBTTagList` de una manera simple, puedes agregar y remover objetos utilizando una estructura de datos de árbol para las rutas.

Incluyendo compatibilidad con objetos custom al registrar un (de)serializador, para más información visita la página que explica los [objetos custom](feature/custom-objects/).

```java
// Crear un Rtag
Rtag rtag = new Rtag();
// Usar la instancia pública
Rtag rtag = Rtag.INSTANCE;

// Un objeto NBTTagCompound
Object compound = ...;

String str = "My String";
// Establecer en la ruta "my -> saved -> object"
rtag.set(compound, str, "my", "saved", "object");

// Obtener desde "my <- save <- object"
String sameStr = rtag.get(compound, "my", "saved", "object");

// Eliminar desde la ruta
rtag.remove(compound, "my", "saved", "object");
```

## Objetos de Minecraft

Varios tipos de classes útiles para interactuar sobre objetos de CraftBukkit y objetos de Minecraft utilizando métodos con reflection.


Ve hacia la [sección de objetos de Minecraft](advanced/minecraft/) para más información.