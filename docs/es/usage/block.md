---
sidebar_position: 4
title: RtagBlock
description: Editar el NBT de los bloques
---

:::caution Limitaciones actuales

Por ahora el RtagBlock solo puede ser utilizado para editar los tags de "tile entities", así que **está planeado** utilizar el PersistentDataContainer de los chunks (agregado en la versión 1.16 de Bukkit) para guardar tags en cualquier bloque.

:::

:::info Antes de continuar

Para entender esta página primero debes ver [la guía de RtagEditor](usage/editor.md).

Para entender sobre los tags comunes en las "tile entities" se sugiere visitar la [página de la wiki de Minecraft](https://minecraft.fandom.com/wiki/Chunk_format#Block_entity_format).

:::

:::tip ¿Buscando tags no-vanilla?

Si quieres agregar tags no-vanilla a las entidades, echa un vistazo en la sección de [APIs compatibles](feature/compatible.md).

Por ahora Rtag no ofrece una "forma segura" de guardar tags no-vanilla en versiones viejas de Bukkit.

:::

El `RtagBlock` es una instancia de `RtagEditor`, así que utiliza los mismos métodos para editar, cargar y actualizar los cambios como un editor de tags.

## Crear

Existen multiples maneras de crear una instancia de `RtagBlock`.

### Instancia

Usando un constructor simple que acepta cualquier tipo de `Block`.

```java
Block block = ...;

RtagBlock tag = new RtagBlock(block);
```

O especificando la instancia de Rtag que se utilizará para manejar el NBT.

```java
Block block = ...;
Rtag rtag = ...;

RtagBlock tag = new RtagBlock(rtag, block);
```

### Método

Usando un método simple que acepta cualquier tipo de `Block`.

```java
Block block = ...;

RtagBlock tag = RtagBlock.of(block);
```

O especificando la instancia de Rtag que se utilizará para manejar el NBT.

```java
Block block = ...;
Rtag rtag = ...;

RtagBlock tag = RtagBlock.of(rtag, block);
```

### Función

Usando funciones es la forma más fácil de editar NBT manejando el `RtagBlock`.

Puedes editar el `Block` proporcionado sin necesidad de reemplazarlo.

```java
Block block = ...;

// Editar el bloque
RtagBlock.edit(block, tag -> {
	tag.set(123, "path");
});

// Especificar la instancia de Rtag
Rtag rtag = ...;
RtagBlock.edit(rtag, block, tag -> {
	tag.set(123, "path");
});
```

Tomar en cuenta que el método devuelve el propio `Block` con los cambios cargados.

```java
Block block = ...;

// Editar el bloque
Block sameBlock = RtagBlock.edit(block, tag -> {
	tag.set(123, "path");
});

// Especificar la instancia de Rtag
Rtag rtag = ...;
Block sameBlock = RtagBlock.edit(rtag, block, tag -> {
	tag.set(123, "path");
});
```

Además puedes devolver cualquier tipo de objeto especificado en la función.

```java
Block block = ...;

// Obtenerlo como quieras desde la instancia del RtagEditor
int number = RtagBlock.edit(block, tag -> {
	return tag.get("path");
});
```

## Editar

Existen algunos métodos dentro de `RtagBlock` que el `RtagEditor` no tiene.

### Función

Como las funciones explicadas anteriormente, el `RtagBlock` actual puede se editado utilizando una función que devuelve su propia instancia.

```java
RtagBlock tag = ...;

tag.edit(tag -> {
	tag.set(123, "path");
	tag.set("Hello", "greeting");
});
```

### Métodos de instancia

Existen algunos métodos **fáciles de utilizar** para editar **tags conocidos del bloque** de una manera simple, teniendo soporte para una amplia variedad de versiones de Minecraft.

**Nombre custom**: Editar el nombre mostrado para la "tile entity" (como por ejemplo un cofre).

```java
RtagBlock tag = ...;

// Establecer utilizando un String con el formato de chat de colores o el formato
// de componente de chat en json
tag.setCustomName("§eColored name");

// Obtener en formato de chat de colores
String name = tag.getCustomName();
```