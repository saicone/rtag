---
sidebar_position: 3
title: RtagEntity
description: Editar el NBT de las entidades
---

:::info Antes de continuar

Para entender esta página primero debes ver [la guía de RtagEditor](../../usage/editor/).

Para entender sobre los tags comunes en las entidades se sugiere visitar la [página de la wiki de Minecraft](https://minecraft.wiki/w/Entity_format).

:::

:::tip ¿Buscando tags no-vanilla?

Si quieres agregar tags no-vanilla a las entidades, echa un vistazo en la sección de [APIs compatibles](../../feature/compatible/).

Por ahora Rtag no ofrece una "forma segura" de guardar tags no-vanilla en versiones viejas de Bukkit.

:::

El `RtagEntity` es una instancia de `RtagEditor`, así que utiliza los mismos métodos para editar, cargar y actualizar los cambios como un editor de tags.

## Crear

Existen multiples maneras de crear una instancia de `RtagEntity`.

### Instancia

Usando un constructor simple que acepta cualquier tipo de `Entity`.

```java
Entity entity = ...;

RtagEntity tag = new RtagEntity(entity);
```

O especificando la instancia de Rtag que se utilizará para manejar el NBT.

```java
Entity entity = ...;
Rtag rtag = ...;

RtagEntity tag = new RtagEntity(rtag, entity);
```

### Método

Usando un método simple que acepta cualquier tipo de `Entity`.

```java
Entity entity = ...;

RtagEntity tag = RtagEntity.of(entity);
```

O especificando la instancia de Rtag que se utilizará para manejar el NBT.

```java
Entity entity = ...;
Rtag rtag = ...;

RtagEntity tag = RtagEntity.of(rtag, entity);
```

### Función

Usando funciones es la forma más fácil de editar NBT manejando el `RtagEntity`.

Puedes editar el `Entity` proporcionado sin necesidad de reemplazarlo.

```java
Entity entity = ...;

// Editar la entidad
RtagEntity.edit(entity, tag -> {
	tag.set(123, "path");
});

// Especificar la instancia de Rtag
Rtag rtag = ...;
RtagEntity.edit(rtag, entity, tag -> {
	tag.set(123, "path");
});
```

Tomar en cuenta que el método devuelve el propio `Entity` con los cambios cargados.

```java
Entity entity = ...;

// Editar la entidad
Entity sameEntity = RtagEntity.edit(entity, tag -> {
	tag.set(123, "path");
});

// Especificar la instancia de Rtag
Rtag rtag = ...;
Entity sameEntity = RtagEntity.edit(rtag, entity, tag -> {
	tag.set(123, "path");
});
```

Además puedes devolver cualquier tipo de objeto especificado en la función.

```java
Entity entity = ...;

// Obtenerlo como quieras desde la instancia del RtagEditor
int number = RtagEntity.edit(entity, tag -> {
	return tag.get("path");
});
```

## Editar

Existen algunos métodos dentro de `RtagEntity` que el `RtagEditor` no tiene.

### Función

Como las funciones explicadas anteriormente, el `RtagEntity` actual puede se editado utilizando una función que devuelve su propia instancia.

```java
RtagEntity tag = ...;

tag.edit(tag -> {
	tag.set(123, "path");
	tag.set("Hello", "greeting");
});
```

### Métodos de instancia

Existen algunos métodos **fáciles de utilizar** para editar **tags conocidos dla entidad** de una manera simple, teniendo soporte para una amplia variedad de versiones de Minecraft.

**Vida**: Editar la vida de la entidad

```java
RtagEntity tag = ...;

tag.setHealth(170f);

float health = tag.getHealth();
```

**Atributos**: Manejar los valores de los atributos.

```java
RtagEntity tag = ...;

tag.setAttributeBase("generic.attackDamage", 0.5d);
// Lo mismo que el de arriba
tag.setAttributeValue("generic.attackDamage", "Base", 0.5d);

double damage = tag.getAttributeBase("generic.attackDamage");
// Lo mismo que el de arriba
Object value = tag.getAttributeValue("generic.attackDamage", "Base");
```