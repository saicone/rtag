---
sidebar_position: 3
title: Rutas funcionales
description: Como manejar rutas con funciones
---

:::info Antes de continuar

Esta es una característica avanzada, puedes ignorar esto ya que es raramente utilizado.

Para entender esta página deberías ver la información sobre [Objetos de Tag](advanced/tags/).

:::


Las rutas con una estructura de datos de árbol utilizadas en Rtag son bastante simples, así que hay una forma más compleja de manejar las ediciones mediante funciones que aceptan cualquier objeto de `NBTTagCompound` y `NBTTagList` mientras la ruta es resuelta.

## Como usarlo

Por ejemplo, supongamos que tenemos la siguiente data en un NBT:

```yaml
main:
  list:
  	- id: EPIC
  	  value: 40
  	- id: RARE
  	  value: 30
  	- id: LEGENDARY
  	  value: 50
my:
  custom:
  	list:
  	  - 15
  	  - 40
  	  - 39
  path: "Hello"
```

Es imposible para una estructura de datos de árbol obtener el valor `value` o el ID `EPIC`, esto debido a que está dentro de una lista de objetos complejos.

En un caso normal deberías obtener la lista entera e iterar sobre esta para revisar cual valor tiene `EPIC` en el `id`, lo cual gastaría muchos recursos debido a que Rtag convetirá la lista entera en objetos normales incluyendo valores que no se utilizarán en la operación.

Para resolver eso puedes utilizar rutas funcionales al proveer una función que itera el `NBTTagList` directamente.

```java
// Preparar la función
ThrowableFunction<Object, Object> function = nbtList -> {
	for (Object nbtCompound : TagList.getValue(nbtList)) {
		Object id = TagCompound.get(nbtCompound, "id");
		if ("EPIC".equals(TagBase.getValue(id))) {
			return nbtCompound;
		}
	}
	return null;
};


// --- Usando una instancia de Rtag
Rtag rtag = ...;
Object compound = ...;
// Obtener la ruta del 'value' para el objeto con el id `EPIC`
int value = rtag.get(compound, "main", "list", function, "value");
// O reemplazar el 'value'
rtag.set(compound, 45, "main", "list", function, "value");


// --- Usando una instancia de RtagEditor
RtagEditor tag = ...;
// Obtener la ruta del 'value' para el objeto con el id `EPIC`
int value = tag.get("main", "list", function, "value");
// O reemplazar el 'value'
tag.set(45, "main", "list", function, "value");
```

Si quieres obtener el `value` desde objetos cuyo id es `RARE` o `LEGENDARY` puedes registrar una función proporcionada con un método.

```java
private ThrowableFunction<Object, Object> getFunction(String type) {
	return nbtList -> {
		for (Object nbtCompound : TagList.getValue(nbtList)) {
			Object id = TagCompound.get(nbtCompound, "id");
			if (type.equals(TagBase.getValue(id))) {
				return nbtCompound;
			}
		}
		return null;
	};
}
```

Y usarla así:

```java
// --- Usando una instancia de Rtag
Rtag rtag = ...;
Object compound = ...;
// Obtener la ruta del 'value' para el objeto con el id `RARE`
int value = rtag.get(compound, "main", "list", getFunction("RARE"), "value");
// O reemplazar el 'value'
rtag.set(compound, 35, "main", "list", getFunction("RARE"), "value");


// --- Usando una instancia de RtagEditor
RtagEditor tag = ...;
// Obtener la ruta del 'value' para el objeto con el id `LEGENDARY`
int value = tag.get("main", "list", getFunction("LEGENDARY"), "value");
// O reemplazar el 'value'
tag.set(55, "main", "list", getFunction("LEGENDARY"), "value");
```