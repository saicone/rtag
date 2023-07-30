---
sidebar_position: 2
title: RtagItem
description: Editar el NBT de los items
---

:::info Antes de continuar

Para entender esta página primero debes ver [la guía de RtagEditor](usage/editor.md).

Para entender sobre los tags comunes en los items se sugiere visitar la [página de la wiki de Minecraft](https://minecraft.fandom.com/wiki/Player.dat_format#Item_structure).

:::

El `RtagItem` es una instancia de `RtagEditor`, así que utiliza los mismos métodos para editar, cargar y actualizar los cambios como un editor de tags.

## Crear

Existen multiples maneras de crear una instancia de `RtagItem`.

### Instancia

Usando un constructor simple que acepta cualquier tipo de `ItemStack`.

```java
ItemStack item = ...;

RtagItem tag = new RtagItem(item);
```

O especificando la instancia de Rtag que se utilizará para manejar el NBT.

```java
ItemStack item = ...;
Rtag rtag = ...;

RtagItem tag = new RtagItem(rtag, item);
```

### Método

Usando un método simple que acepta cualquier tipo de `ItemStack`.

```java
ItemStack item = ...;

RtagItem tag = RtagItem.of(item);
```

O especificando la instancia de Rtag que se utilizará para manejar el NBT.

```java
ItemStack item = ...;
Rtag rtag = ...;

RtagItem tag = RtagItem.of(rtag, item);
```

### Función

Usando funciones es la forma más fácil de editar NBT manejando el `RtagItem`.

Puedes editar el `ItemStack` proporcionado sin necesidad de reemplazarlo.

```java
ItemStack item = ...;

// Editar el item
RtagItem.edit(item, tag -> {
	tag.set(123, "path");
});

// Especificar la instancia de Rtag
Rtag rtag = ...;
RtagItem.edit(rtag, item, tag -> {
	tag.set(123, "path");
});
```

Tomar en cuenta que el método devuelve el propio `ItemStack` con los cambios cargados.

```java
ItemStack item = ...;

// Editar el item
ItemStack sameItem = RtagItem.edit(item, tag -> {
	tag.set(123, "path");
});

// Especificar la instancia de Rtag
Rtag rtag = ...;
ItemStack sameItem = RtagItem.edit(rtag, item, tag -> {
	tag.set(123, "path");
});
```

Además puedes devolver cualquier tipo de objeto especificado en la función.

```java
ItemStack item = ...;

// Obtener una copia del item con los cambios cargados
ItemStack itemWithChanges = RtagItem.edit(item, tag -> {
	tag.set(123, "path");
	return tag.loadCopy();
});

// Obtenerlo como quieras desde la instancia del RtagEditor
int number = RtagItem.edit(item, tag -> {
	return tag.get("path");
});
```

## Editar

Existen algunos métodos dentro de `RtagItem` que el `RtagEditor` no tiene.

### Función

Como las funciones explicadas anteriormente, el `RtagItem` actual puede se editado utilizando una función que devuelve su propia instancia.

```java
RtagItem tag = ...;

tag.edit(tag -> {
	tag.set(123, "path");
	tag.set("Hello", "greeting");
});
```

### Métodos de instancia

Existen algunos métodos **fáciles de utilizar** para editar **tags conocidos del item** de una manera simple, teniendo soporte para una amplia variedad de versiones de Minecraft.

**Flags**: Mejor conocidas como HideFlags, en el RtagItem las flags son manejadas por sus valores ordinales.

0. Enchantments - Encantamientos
1. AttributeModifiers - Modificadores (como el daño)
2. Unbreakable - Estado de irrompibilidad
3. CanDestroy - Información sobre posibilidad de romper algo
4. CanPlaceOn - Información sobre posibilidad de colocarse en algún lugar
5. Other information - Encantamientos en libros, efectos de poción, generación, autor del libro, tipo de explosión y efectos de fuego artificial.
6. Dyed - Tintado del item
7. Palette information - El trim de las armaduras

```java
RtagItem tag = ...;

tag.addHideFlags(2, 4, 6);

boolean bool = tag.hasHideFlags(2, 6); // devuelve true

tag.removeHideFlags(6);

tag.setHideFlags(4);
```

**Encantamientos**: El RtagItem tiene soporte para cualquier encantamiento, ya sea manejado por el enum de `Enchantment`, el nombre en `String` o el id como un `Number` en cualquier versión de Minecraft compatible con Rtag.

```java
RtagItem tag = ...;

tag.addEnchantment("Mending", 1);

boolean bool = tag.hasEnchantment(70); // Devuelve true porque el ID del Mending es 70
// Lo mismo de arriba pero utilizando el nombre del encantamiento
boolean bool = tag.hasEnchantment("Mending");

// También puedes utilizar el enum Enchantment de Bukkit correctamente
tag.removeEnchantment(Enchantment.MENDING);

tag.addEnchantment("Mending", 1);

int level = tag.getEnchantmentLevel("Mending");

// Obtener todos los encantamientos como un Map
Map<EnchantmentTag, Integer> enchants = tag.getEnchantments();
```

**Irrompibilidad**: Manejar el estado de irrompibilidad del item (fue agregado en la versión 1.7 de Minecraft, pero solo puede ser editado con Bukkit desde la versión 1.11).

```java
RtagItem tag = ...;

tag.setUnbreakable(true);

boolean bool = tag.isUnbreakable();
```

**CustomModelData**: Editar el custom model data introducido en la versión 1.14 desde cualquier versión.

```java
RtagItem tag = ...;

tag.setCustomModelData(40);

int model = tag.getCustomModelData();
```

**Costo de reparación**: Editar el costo de reparación del item en el yunque.

```java
RtagItem tag = ...;

tag.setRepairCost(10);

int cost = tag.getRepairCost();
```

**Serialización**: Arreglar cualquier item mal serializado en Bukkit 1.14 o superior.

:::info

En Minecraft 1.14, los strings del lore del item fueron movidos a utilizar el formato de [componente de chat](feature/chat-component.md), así que los items serializados en Bukkit de alguna forma no pueden ser comparados con otros items utilizando el método `ItemStack#isSimilar()` ya que el componente de chat del lore del item serializado no contiene varios tags sin utilizar.

:::

```java
RtagItem tag = ...;

tag.fixSerialization();
```

## Cargar

Los cambios realizados en el `RtagItem` pueden ser cargados de formas diferentes a diferencia del `RtagEditor`.

### Obtener una copia

Envés de cargar los cambios en el ItemStack proporcionado, puedes crear una copia del item con los cambios cargados.

```java
ItemStack original = ...;

// Crear el editor
RtagItem tag = new RtagItem(original);

// Editar el tag
tag.set(123, "path");

// Obtener una copia del item con los cambios cargados
ItemStan newItem = tag.loadCopy();
```