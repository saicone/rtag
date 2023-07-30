---
sidebar_position: 3
title: Functional Paths
description: How to handle paths with functions
---

:::info Before continue

This is an advanced feature, you can ignore this because is rarely used.

To understand this page you should see [Tag Objects guide](advanced/tags.md).

:::


The tree-like paths used by Rtag is quite simple, so there is a more complex way to handle edits with functions that accept `NBTTagCompound` and `NBTTagList` objects while the path is resolved.

## Usage

For example, suppose we have the following data in a NBT:

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

It's impossible for a tree-like path to get the `value` of id `EPIC`, because it's on a list.

In a normal case you should get the entire list and iterate hover to check what value has `EPIC` in `id`, which is quite expensive because Rtag will convert the entire list into normal objects including unused values in the operation.

To solve that you can use functional paths by using a function that iterate hover `NBTTagList` directly.

```java
// Prepare the function
ThrowableFunction<Object, Object> function = nbtList -> {
	for (Object nbtCompound : TagList.getValue(nbtList)) {
		Object id = TagCompound.get(nbtCompound, "id");
		if ("EPIC".equals(TagBase.getValue(id))) {
			return nbtCompound;
		}
	}
	return null;
};


// --- Using Rtag instance
Rtag rtag = ...;
Object compound = ...;
// Get the value path from object with EPIC id
int value = rtag.get(compound, "main", "list", function, "value");
// Or replace the value
rtag.set(compound, 45, "main", "list", function, "value");


// --- Using RtagEditor instance
RtagEditor tag = ...;
// Get the value path from object with EPIC id
int value = tag.get("main", "list", function, "value");
// Or replace the value
tag.set(45, "main", "list", function, "value");
```

If you want to get `value` from objects id `RARE` or `LEGENDARY` you can register a function provided by method.

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

And use like this:

```java
// --- Using Rtag instance
Rtag rtag = ...;
Object compound = ...;
// Get the value path from object with RARE id
int value = rtag.get(compound, "main", "list", getFunction("RARE"), "value");
// Or replace the value
rtag.set(compound, 35, "main", "list", getFunction("RARE"), "value");


// --- Using RtagEditor instance
RtagEditor tag = ...;
// Get the value path from object with LEGENDARY id
int value = tag.get("main", "list", getFunction("LEGENDARY"), "value");
// Or replace the value
tag.set(55, "main", "list", getFunction("LEGENDARY"), "value");
```