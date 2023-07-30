---
sidebar_position: 1
title: Objetos opcionales
description: Información sobre el OptionalType y sus classes principales
---

La librería Rtag proveé el class OptionalType, cuya instancia permite manejar de manera flexible los tags mediante diferentes tipos de objetos.

```java
// Crear directamente con cualquier tipo de objeto
OptionalType type = OptionalType.of("123");

// --- Obtenerlo desde una instancia de Rtag
Rtag rtag = ...;
Object compount = ...;
OptionalType type = rtag.getOptional(compound, "my", "object", "path");


// --- Obtenerlo desde una instancia de RtagEditor
RtagEditor tag = ...;
OptionalType type = tag.getOptional("my", "object", "path");
```

## Revisión de tipo

Obtener el valor del OptionalType como si fuera el tipo de dato que quieres o proveer un objeto por defecto.

```java
OptionalType type = ...;

// Obtener como el objeto requerido, este método asume que sabes el tipo del dato
// Si el valor no es del tipo que requieres, se devolverá null
String string = type.value();
// Especificar el class (opcional)
String string = type.value(String.class);

// Obtener como el objeto requerido o especificar un objeto por defecto
int num = type.or(-1); // default: -1
// Especificar el class (opcional)
int num = type.or(int.class, -1);
```

## Conversión

Convertir el valor del `OptionalType` en diferentes tipos de objetos, bastante útil si no sabes el tipo de dato es o simplemente requieres convertir cualquier objeto, por ejemplo obtener un `String` como si fuera un `Integer` al proveer un valor por defecto en caso de fallar en la conversión.

### Conversión única

Por defecto el `OptionalType` tiene las siguientes conversiones de objetos únicos para obtener el valor como:

* Object
* String - Convertir cualquier objeto no nulo en `String`
* Char - Extraer el primer carácter de cualquier objeto no nulo convertido en `String`
* Boolean - Revisar si el objeto no nulo convertido en `String` es `"true"` o `"false"` (`"1"` o `"0"` | `"yes"` o `"no"` | `"on"` o `"off"` | `"y"` o `"n"`)
* Byte
* Short
* Integer (Int)
* Float
* Long
* Double
* UUID - Convertir un `String` o un `int[]` de 4 valores como un `UUID`
* Cualquier tipo de objeto serializable - Utilizando Gson

Y solo si el valor es un bit field:

* `Set<? extends Enum>` - Convertir al proveer un class de Enum.
* `Set<E>` - Convertir al proveer los valores ordinales de un tipo de elemento mediante el uso de una función.
* `Set<Integer>` - Convertir al proveer el máximo valor ordinal.

```java
OptionalType type = OptionalType.of("1");

// Convertir en objetos normales
String string = type.asString();
int num = type.asInt(-1); // por defecto: -1
boolean bool = type.asBoolean(); // devuelve true

// Convertir en un objeto serializable como MyObject
MyObject myObj = type.getAs(MyObject.class);
```

:::info Compatibilidad con Boolean

Si el tipo de dato requerido es una instancia de `Number` y el valor actual es un `Boolean`, será convertido en `1` al ser `true` y `0` al ser `false`.

:::

### Conversión múltiple

La instancia de `OptionalType` permite convertir cualquier tipo de valor en uno "coleccionable" (Collection o Array), al iterar sobre el y nunca devolver un valor nulo del tipo de colección o array requerido.

```java
OptionalType type = OptionalType.of(List.of("1", "value2", "2", "3", "3"));

// Convertir a cualquier tipo de array (debes proveer una muestra del array)
// Resultado: ["1", "value2", "2", "3", "3"]
String[] array = type.asArray(new String[0], OptionalType::asString);

// Convertir en cualquier tipo de lista
// Resultado: [1, 2, 3, 3]
List<Short> list = type.asList(OptionalType::asShort);

// Convertir en cualquier tipo de colección (debes proveer el Collection para agregarle los valores)
// Resultado: [1, 2, 3]
Set<Integer> set = type.asCollection(new HashSet(), OptionalType::asInt);
```

:::info

Tomar en cuenta que la conversión de múltiples objetos no hace "magia" para convertir los objetos, está limitada a convertirlos según la función proporcionada, y cualquier valor que no pueda ser convertido será ignorado.

:::

:::caution Limitaciones actuales

La instancia de `OptionalType` tiene conversión de objetos únicos a objetos múltiples como colecciones o array, pero no puede convertir colecciones y arrays en objetos únicos.

:::

### Conversión custom

Además puedes implementar tu propia función de conversión con diferentes tipos de métodos.

```java
OptionalType type = ...;

// Convertir simplemente en MyObject
MyObject myObj = type.by(value -> {
	// Conversión
});
```

Regresar un valor por defecto si:

* El valor actual es `null`
* La conversión produce una excepción
* La conversión devuelve `null`

```java
MyObject def = ...;
MyObject myObj = type.by(value -> {
	// Conversión
}, def);
```

Proporcionar un class para revisar si el valor actual es una instancia del tipo de objeto requerido y de esta manera evitar la función de conversión (por ser redundante).

```java
MyObject def = ...;
MyObject myObj = type.by(MyObject.class, value -> {
	// Conversión
}, def);
```

## Iterator

La instancia de `OptionalType` se extiende del `IterableType`, el cual permite iterar sobre el tipo de valor:

* Array: Iterar sobre los valores del array
* Colección: Iterar sobre los valores de la colección.
* Objeto único: Iterar 1 vez sobre el único valor dentro del `OptionalType`.

```java
OptionalType type = ...;

for (Object o : type) {
	// Valores del Array|Collection o simplemente el objeto único
}

type.forEach(o -> {
	// Valores del Array|Collection o simplemente el objeto único
});
```