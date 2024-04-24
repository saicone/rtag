---
sidebar_position: 1
title: Explicación
description: Explicación profunda sobre Rtag
---

```mdx-code-block
import DocCard from '@theme/DocCard';
```

Rtag está hecho de múltiples classes para funcionar entre sí y manejar NBT.

## Estructura

![Rtag Instances](https://raw.githubusercontent.com/saicone/rtag/main/docs/images/rtag-instances.png)

## Objetos de tag

Rtag contiene varias classes para ejecutar métodos de los objetos NBT manteniendo la compatibilidad entre versiones, incluyendo la conversión del respectivo NBT a un objeto normal de Java y viceversa.

Para más información sobre objetos de tag:

```mdx-code-block
<DocCard item={{
  type: "link",
  href: "/es/rtag/advanced/tags/",
  label: "Objetos de Tag",
  description: "Información sobre Rtag manejando NBT"
  }}
/>
```

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

Incluyendo compatibilidad con objetos custom al registrar un (de)serializador.

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

Para más información sobre objetos custom:

```mdx-code-block
<DocCard item={{
  type: "link",
  href: "/es/rtag/feature/custom-objects/",
  label: "Objectos custom",
  description: "Como guardar y obtener objetos custom con Rtag"
  }}
/>
```

## Objetos de Minecraft

Varios tipos de classes útiles para interactuar sobre objetos de CraftBukkit y objetos de Minecraft utilizando métodos con reflection.

```mdx-code-block
<DocCard item={{
  type: "link",
  href: "/es/rtag/advanced/minecraft/",
  label: "Objetos de Minecraft",
  description: "Information sobre objetos de minecraft en Rtag"
  }}
/>
```

## Codecs de Mojang

Para implementar una conversión de la data vieja, Mojang creó la librería DataFixerUpper como una forma flexible para decodificar y codificar data.

```mdx-code-block
<DocCard item={{
  type: "link",
  href: "/es/rtag/advanced/codec/",
  label: "Codecs de Mojang",
  description: "Información sobre como utilizar codecs y respectiva serialización"
  }}
/>
```

## Data Components

Desde la versión 1.20.5 de Minecraft, el formato de los items cambió y Mojang introdujo los componentes de datos para manejar los tags vanilla de una forma más optimizada.

```mdx-code-block
<DocCard item={{
  type: "link",
  href: "/es/rtag/advanced/data-component/",
  label: "Componente de Datos",
  description: "Información sobre los objetos de componente de datos"
  }}
/>
```