---
sidebar_position: 1
title: RtagEditor
description: El objeto principal para editar NBT fácilmente
---

Las instancias de `RtagEditor` (editor de tags) convierten un objeto de Bukkit en uno del servidor de Minecraft y proveen una forma de editar el tag (NBTTagCompound) dentro, usando una instancia de Rtag para manejar las operaciones con NBT.

## Editar

Para entender el `RtagEditor` primero es necesario que conozcas la forma de editar el tag actual.

### Métodos simples

La instancia del editor simplifica cualquier edición mediante una conversión automática con el uso de una estructura de datos de arbol para obtener, establecer y remover objetos utilizando rutas.

```java
// Cualquier instancia de RtagEditor (RtagItem, RtagEntity, RtagBlock)
RtagEditor tag = ...;

String str = "My String";
// Establecer en la ruta "my -> saved -> object"
tag.set(str, "my", "saved", "object");

// Obtener desde "my <- save <- object"
String sameStr = tag.get("my", "saved", "object");

// Eliminar desde la ruta
tag.remove("my", "saved", "object");
```

Probablemente notaste que no existe un establecimiento u obtención explícita como `setString` o `getString`, eso es porque Rtag hace "magia" al convertir cualquier objeto NBT en un objeto normal de java.

Tomar en cuenta que Rtag solo devuelve el objeto convertido, por sí mismo el no sabe si quieres un String, Integer, Float... etc, por lo que en operación normales de obtención **debes saber con seguridad que tipo objeto es el que estás obteniendo** o de lo contrario el objeto obtenido será null luego de fallar su conversión o sencillamente no existe un objeto en la ruta especificada.

Si no sabes cual tipo de objeto estás obteniendo, o la instancia de Rtag sencillamente no tiene una conversión ya que los [objetos compatibles](intro/#objetos-compatibles) son limitados, puedes utilizar una obtención de [objeto opcional](feature/types/) la cual tiene una amplia variedad de conversiones para objetos además de revisiones para tus necesidades.

```java
// Cualquier instancia de RtagEditor (RtagItem, RtagEntity, RtagBlock)
RtagEditor tag = ...;

String str = "123";
// Establecer en la ruta "my -> saved -> object"
tag.set(str, "my", "saved", "object");

// Trata de obtener el objeto como String (cast) o devuelve "456" en caso de fallar
String s = tag.getOptional("my", "saved", "object").or("456");

// Lo mismo de arriba, pero el objeto opcional tratará de convertir cualquier
// tipo de objeto obtenido en un String
String s = tag.getOptional("my", "saved", "object").asString("456");

// También funciona para convertirlo en otros tipos de objetos
int numValue = tag.getOptional("my", "saved", "object").asInt(-1); // -1 por defecto
```

:::tip

Revisa la **[guía de objetos custom](feature/custom-objects/)** si quieres guardar cualquier clase de objeto serializable.

:::

### Colecciones

Con Rtag es bastante fácil manejar listas de objetos, con el método `add` puedes agregar objetos dentro de las listas en las rutas establecidas, además que si la lista no existe se encargará de crear una nueva.

```java
// Cualquier instancia de RtagEditor (RtagItem, RtagEntity, RtagBlock)
RtagEditor tag = ...;

// Crear una lista en la ruta "my -> saved -> list"
tag.set(new ArrayList(), "my", "saved", "list");

byte num = 3;
// Añadirlo en la lista
tag.add(num, "my", "saved", "list");

// Obtener la lista
List<Byte> list = tag.get("my", "saved", "list");

// Si no sabes que tipo de lista es, puedes hacer una conversión con un objeto opcional
List<Byte> list = tag.getOptional("my", "saved", "list").asList(OptionalType::asByte);
```

:::info

Tomar en cuenta que las listas de NBT aceptan cualquier tipo de objeto al estar vacías, esto es debido a la regla de que **el primer objeto define el tipo de NBT que tendrá la lista**.

:::

### Revisar

Para revisar si cualquier tag existe o no dentro de la ruta establecida, puedes utilizar los métodos `hasTag` o `hasNotTag`.

```java
// Cualquier instancia de RtagEditor (RtagItem, RtagEntity, RtagBlock)
RtagEditor tag = ...;

// Revisar SI existe
boolean exist = tag.hasTag("my", "saved", "object");
// Revisar si NO existe
boolean notExist = tag.notHasTag("my", "saved", "object");
```

### Enums

:::info

Esta es una característica avanzada, puedes ignorar esta parte ya que es raramente utilizada.

:::

Las instancias de `RtagEditor` pueden manejar Enums como si fueran "bit fields", al tener una forma fácil de leer y guardar cualquier tipo de objeto con un valor ordinal establecido.

Por ejemplo, si `MyEnum` tiene los valores `FIRE, GLOW, INVISIBLE` puedes tener un `Set` de esos valores en el mismo lugar guardados como un Integer empezando con la definición de que el valor ordinal de `FIRE` es `0`, el de `GLOW` es `1` y el de `INVISIBLE` es `2`.

```java
// Cualquier instancia de RtagEditor (RtagItem, RtagEntity, RtagBlock)
RtagEditor tag = ...;

// Agregar los valores en un Set del enum en la ruta "my -> saved -> enum"
tag.addEnum(MyEnum.FIRE, "my", "saved", "enum");
tag.addEnum(MyEnum.INVISIBLE, "my", "saved", "enum");

// Obtener el Set del enum
Set<MyEnum> set = tag.getOptional("my", "saved", "enum").asEnumSet(MyEnum.class);
```

## Cargar

Luego de editar el tag es necesario cargar los cambios dentro del objeto del servidor de Minecraft y el objeto de Bukkit.

### Guardar ediciones

El método `load` guarda los cambios (los carga dentro).

```java
// Cualquier instancia de RtagEditor (RtagItem, RtagEntity, RtagBlock)
RtagEditor tag = ...;

String str = "My String";
// Establecer en la ruta "my -> saved -> object"
tag.set(str, "my", "saved", "object");

// Cargarlo en el objeto proporcionado
tag.load();
```

## Actualizar

Si quieres editar el objeto proporcionado (dependiendo la instancia de RtagEditor), es necesario actualizar el tag actual para seguirlo editando en la misma instancia del `RtagEditor`.

### Obtener cambios

```java
// Cualquier instancia de RtagEditor (RtagItem, RtagEntity, RtagBlock)
RtagEditor tag = ...;

// <El objeto proporcionado fue editado (item, entidad o bloque)>

// Actualizar el tag que se está editando en el RtagEditor
tag.update();
```