---
sidebar_position: 4
title: Tag Stream
description: Convert NBT into different types of data and viceversa
---

## Information

Rtag has the option to save any NBTTagCompound into different ways:

* File
* [Base64](https://en.wikipedia.org/wiki/Base64)
* Bytes (`byte[]`)
* Maps (`Map<String, Object>`)
* String (SNBT format, also compatible with Json)
* Readable Map (Only for items)

Including compatibility with serializable objects.

:::info Supported serialization formats

Any byte array serialization (probably saved as Base64) is compatible if it's made by the following methods:

1. Using `BukkitObjectInputStream` to save objects as type object or `byte[]`.
2. Using `NBTCompressedStreamTools` to save objects as `NBTTagCompound`, `NBTTagList` or `NBTTagByteArray` inside bytes.
3. NBT objects saved with GZIP format.
4. NBT objects saved inside other NBT object (like nbt list or byte array).

:::

## TagCompound Data

The TagCompound class includes the "DATA" interface, an easy way to convert any NBTTagCompound into File, Base64, Bytes, Map and String.

```mdx-code-block
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
<TabItem value="file" label="File" default>

```java
// NBTTagCompound from anywhere
Object compound = ...;

// Convert into File
File file TStream.COMPOUND.toFile(compound, new File("file.nbt"));

// Get from file
Object tagCompound = TStream.COMPOUND.fromFile(file);
```

</TabItem>
<TabItem value="base64" label="Base64">

```java
// NBTTagCompound from anywhere
Object compound = ...;

// Convert into Base64
String base64 = TStream.COMPOUND.toBase64(compound);

// Get from Base64
Object tagCompound = TStream.COMPOUND.fromBase64(base64)[0]; // Return array
```

</TabItem>
<TabItem value="bytes" label="Bytes">

```java
// NBTTagCompound from anywhere
Object compound = ...;

// Convert into bytes
byte[] bytes = TStream.COMPOUND.toBytes(compound);

// Get from bytes
Object tagCompound = TStream.COMPOUND.fromBytes(bytes);
```

</TabItem>
<TabItem value="map" label="Map">

```java
// NBTTagCompound from anywhere
Object compound = ...;

// Convert into map
Map<String, Object> map = TStream.COMPOUND.toMap(compound);

// Get from map
Object tagCompound = TStream.COMPOUND.fromMap(map);
```

</TabItem>
<TabItem value="string" label="String">

```java
// NBTTagCompound from anywhere
Object compound = ...;

// Convert into snbt
String snbt = TStream.COMPOUND.toString(compound);

// Get from snbt
Object tagCompound = TStream.COMPOUND.fromString(snbt);
```

</TabItem>
</Tabs>

## ItemTagStream

Rtag includes an easy way to convert any ItemStack into File, Base64, Bytes, Map, String and Readable Map, useful to **save items in a database**.

:::info

The "readable map" format convert item name and lore into colored strings, instead of [chat component](../../feature/chat-component/) format introduced for items nbt on Minecraft 1.13, useful to save items in files and make them editable by the user without understanding chat components.

:::

<Tabs>
<TabItem value="file" label="File" default>

```java
ItemStack item = ...;

// Convert into File
File file = ItemTagStream.INSTANCE.toFile(item, new File("file.nbt"));

// Get from File
ItemStack sameItem = ItemTagStream.INSTANCE.fromFile(file);
```

</TabItem>
<TabItem value="base64" label="Base64">

```java
ItemStack item = ...;

// Convert into Base64
String base64 = ItemTagStream.INSTANCE.toBase64(item);

// Get from Base64
ItemStack sameItem = ItemTagStream.INSTANCE.fromBase64(base64)[0]; // Return array


List<ItemStack> items = // List of items;

// Convert into Base64
String base64 = ItemTagStream.INSTANCE.toBase64(items);

// Get from Base64
List<ItemStack> sameItems = ItemTagStream.INSTANCE.listFromBase64(base64);
```

</TabItem>
<TabItem value="bytes" label="Bytes">

```java
ItemStack item = ...;

// Convert into bytes
byte[] bytes = ItemTagStream.INSTANCE.toBytes(item);

// Get from bytes
ItemStack sameItem = ItemTagStream.INSTANCE.fromBytes(bytes);
```

</TabItem>
<TabItem value="map" label="Map">

```java
ItemStack item = ...;

// Convert into map
Map<String, Object> map = ItemTagStream.INSTANCE.toMap(item);

// Get from map
ItemStack sameItem = ItemTagStream.INSTANCE.fromMap(map);
```

</TabItem>
<TabItem value="string" label="String">

```java
ItemStack item = ...;

// Convert into snbt
String snbt = ItemTagStream.INSTANCE.toString(item);

// Get from snbt
ItemStack sameItem = ItemTagStream.INSTANCE.fromString(snbt);
```

</TabItem>
<TabItem value="readable" label="Readable">

```java
ItemStack item = ...;

// Convert into readable map
Map<String, Object> map = ItemTagStream.INSTANCE.toReadableMap(item);

// Get from readable map
ItemStack sameItem = ItemTagStream.INSTANCE.fromReadableMap(map);
```

</TabItem>
</Tabs>

Including **cross-version support**! Save an item on any version and get on any version without compatibility problems. Materials, enchantments, potions... etc, all will be converted!

It also detects items serialized by Bukkit or Paper that adds data version tag as `DataVersion` or `v` to apply the conversion.

:::caution Current limitations

The default ItemTagStream instance it's **only compatible** with Bukkit items, if your server uses Forge it is suggested to use your own instance of ItemTagStream with Forge compatibility.

:::