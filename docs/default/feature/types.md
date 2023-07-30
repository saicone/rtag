---
sidebar_position: 1
title: Optional Types
description: Information about OptionalType and it superclasses
---

Rtag library provide the OptionalType class instance to allow flexible tag handling with different types of objects.

```java
// Create directly with any type of object
OptionalType type = OptionalType.of("123");

// --- Get from Rtag instance
Rtag rtag = ...;
Object compount = ...;
OptionalType type = rtag.getOptional(compound, "my", "object", "path");


// --- Get from RtagEditor instance
RtagEditor tag = ...;
OptionalType type = tag.getOptional("my", "object", "path");
```

## Type check

Get the value of OptionalType as the type of data has you want or provide a default object.

```java
OptionalType type = ...;

// Get as required object, this method assumes that you known the type of data
// If the value isn't the required type will return null
String string = type.value();
// Specify the class (optional)
String string = type.value(String.class);

// Get as required object or return default provided object
int num = type.or(-1); // default: -1
// Specify the class (optional)
int num = type.or(int.class, -1);
```

## Conversion

Convert `OptionalType` value into different types of objects, pretty useful if you don't known what type of data is it or simply require to parse any object, for example get a `String` as `Integer` providing a default value if the conversion fails.

### Single conversions

By default `OptionalType` as the following single-object conversions to get the current value as:

* Object
* String - Convert any non-null object to `String`
* Char - Extract the first char from any non-null object converted to `String`
* Boolean - Check if the non-null object as `String` is `"true"` or `"false"` (`"1"` or `"0"` | `"yes"` or `"no"` | `"on"` or `"off"` | `"y"` or `"n"`)
* Byte
* Short
* Integer (Int)
* Float
* Long
* Double
* UUID - Parse `String` or 4-length `int[]` as `UUID`
* Any type of serializable object - By using Gson

And only if the current value is a bit field:

* `Set<? extends Enum>` - Convert by providing Enum type class.
* `Set<E>` - Convert by providing element ordinal value by function.
* `Set<Integer>` - Convert by providing maximum ordinal value.

```java
OptionalType type = OptionalType.of("1");

// Convert to normal objects
String string = type.asString();
int num = type.asInt(-1); // default: -1
boolean bool = type.asBoolean(); // return true

// Convert to serializable object like MyObject
MyObject myObj = type.getAs(MyObject.class);
```

:::info Boolean compatibility

If the required conversion type is a `Number` and the current value is a `Boolean`, it will be parsed as `1` for `true` and `0` for `false`.

:::

### Multiple conversions

The `OptionalType` instance allows to convert any value into collective one, by iterating hover it and never return null value of required collection or array.

```java
OptionalType type = OptionalType.of(List.of("1", "value2", "2", "3", "3"));

// Convert to any type of array (you should provide array sample)
// Result: ["1", "value2", "2", "3", "3"]
String[] array = type.asArray(new String[0], OptionalType::asString);

// Convert to any type of list
// Result: [1, 2, 3, 3]
List<Short> list = type.asList(OptionalType::asShort);

// Convert to any type of collection (you should provide collection to add values)
// Result: [1, 2, 3]
Set<Integer> set = type.asCollection(new HashSet(), OptionalType::asInt);
```

:::info

Take in count the multiple-type conversion doesn't do "magic" to convert types, it is limited to convert as provided function, and ignore any value that cannot be converted with the function.

:::

:::caution Current limitations

The `OptionalType` instance provide a single object to collection or array conversion, but it doesn't convert collections or arrays into single objects.

:::

### Custom conversions

So you can implement your own conversion function with different types of methods.

```java
OptionalType type = ...;

// Simple convert to MyObject
MyObject myObj = type.by(value -> {
	// Conversion
});
```

Return default value if:

* The current value is `null`
* Conversion throws exception
* Conversion return `null`

```java
MyObject def = ...;
MyObject myObj = type.by(value -> {
	// Conversion
}, def);
```

Provide class to check if the current value is instance of required object and avoid conversion function.

```java
MyObject def = ...;
MyObject myObj = type.by(MyObject.class, value -> {
	// Conversion
}, def);
```

## Iterator

The `OptionalType` instance extends `IterableType` that allow to iterate hover value type:

* Array: Iterate hover array values
* Collection: Iterate hover collection values
* Single object: Iterate 1 time with the value.

```java
OptionalType type = ...;

for (Object o : type) {
	// Array|Collection values or simply the single object
}

type.forEach(o -> {
	// Array|Collection values or simply the single object
});
```