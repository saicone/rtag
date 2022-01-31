# Rtag
[![CodeFactor](https://www.codefactor.io/repository/github/saicone/rtag/badge?style=flat-square)](https://www.codefactor.io/repository/github/saicone/rtag)
![Code size](https://img.shields.io/github/languages/code-size/saicone/rtag?style=flat-square)
![Lines of code](https://img.shields.io/tokei/lines/github/saicone/rtag?style=flat-square)

The "readable tag" library, an easy way to handle NBTTagCompounds.

Rtag convert NBT tags to known objects and viceversa for better readability. It also provide (de)serializer interface to store and get custom objects.

```java
RtagItem itemTag = new RtagItem(item);

// Set values
itemTag.set("CustomValue", "deep", "path");
itemTag.set(40, "somekey");
itemTag.set("Item name!", "display", "name");
itemTag.set(new ArrayList(), "list", "path");
// Add value to list
itemTag.add((short) 3, "list", "path");

// Get values
String value = itemTag.get("deep", "path");
int intValue = itemTag.get("somekey");
String name = itemTag.get("display", "name");
List<Short> list = itemTag.get("list", "path");
// List value at index 0
short listValue = itemTag.get("list", "path", 0);

// Remove values
itemTag.remove("deep", "path");
String newValue = itemTag.get("deep", "path"); // return null

// Save item with changes
ItemStack newItem = itemTag.load();
```
