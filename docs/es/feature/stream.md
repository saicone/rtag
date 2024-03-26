---
sidebar_position: 4
title: Tag Stream
description: Convierte NBT en otros tipos de datos y viceversa
---

## Información

Rtag tiene la opción de guardar los NBTTagCompound en diferentes formas:

* Archivos
* [Base64](https://en.wikipedia.org/wiki/Base64)
* Bytes
* Maps (`Map<String, Object>`)
* String (En formato SNBT, también compatible con Json)
* Map legible (Solo para items)

Incluyendo compatibilidad con objetos serializables.

:::info Formatos de serialización compatibles

Cualquier serialización que haya convertido un objeto en un array de bytes (probablemente guardado en Base64) es compatible si está hecha con los siguientes métodos:

1. Usando un `BukkitObjectInputStream` para guardar los objetos como el tipo requerido o como `byte[]`.
2. Usando el class `NBTCompressedStreamTools` para guardar los objetos como `NBTTagCompound`, `NBTTagList` o `NBTTagByteArray` en bytes.
3. Objetos NBT guardados con el formato GZIP.
4. Objetos NBT guardados dentro de otro objeto NBT (como una lista de nbt o un array de bytes nbt).

:::

## TagCompound Data

El class TagCompound incluido en Rtag contiene una forma fácil para convertir y obtener cualquier NBTTagCompound desde Archivo, Base64, Bytes, Map y String.

```mdx-code-block
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
<TabItem value="file" label="Archivo" default>

```java
// NBTTagCompound desde cualquer parte
Object compound = ...;

// Convertirlo en un archivo
File file TStream.COMPOUND.toFile(compound, new File("archivo.nbt"));

// Obtener el compound desde un archivo
Object tagCompound = TStream.COMPOUND.fromFile(file);
```

</TabItem>
<TabItem value="base64" label="Base64">

```java
// NBTTagCompound desde cualquer parte
Object compound = ...;

// Convertirlo en Base64
String base64 = TStream.COMPOUND.toBase64(compound);

// Obtener el compound desde Base64
Object tagCompound = TStream.COMPOUND.fromBase64(base64)[0]; // Devuelve un array
```

</TabItem>
<TabItem value="bytes" label="Bytes">

```java
// NBTTagCompound desde cualquer parte
Object compound = ...;

// Convertirlo en un array de bytes
byte[] bytes = TStream.COMPOUND.toBytes(compound);

// Obtener el compound desde el array de bytes
Object tagCompound = TStream.COMPOUND.fromBytes(bytes);
```

</TabItem>
<TabItem value="map" label="Map">

```java
// NBTTagCompound desde cualquer parte
Object compound = ...;

// Convertirlo en un Map
Map<String, Object> map = TStream.COMPOUND.toMap(compound);

// Obtener el compound desde el Map
Object tagCompound = TStream.COMPOUND.fromMap(map);
```

</TabItem>
<TabItem value="string" label="String">

```java
// NBTTagCompound desde cualquer parte
Object compound = ...;

// Convertirlo en un SNBT
String snbt = TStream.COMPOUND.toString(compound);

// Obtener el compound desde el SNBT
Object tagCompound = TStream.COMPOUND.fromString(snbt);
```

</TabItem>
</Tabs>

## ItemTagStream

Rtag incluye compatibilidad para convertir los ItemStack en archivos, Base64, Bytes, Map, String y Map legible, esto es realmente útil si quieres **guardar items en bases de datos**.

:::info

El formato de "map legible" convierte el nombre y lore del item en Strings colorizados, envés del formato de [componente de chat](../../feature/chat-component/) introducido para el NBT de los items en Minecraft 1.13, es bastante útil si quieres guardar items en archivos y hacerlos editables por el usuario sin necesidad de entender componentes de chat.

:::

<Tabs>
<TabItem value="file" label="Archivo" default>

```java
ItemStack item = ...;

// Convertirlo en un archivo
File file = ItemTagStream.INSTANCE.toFile(item, new File("archivo.nbt"));

// Obtener el item desde un archivo
ItemStack sameItem = ItemTagStream.INSTANCE.fromFile(file);
```

</TabItem>
<TabItem value="base64" label="Base64">

```java
ItemStack item = ...;

// Convertirlo en Base64
String base64 = ItemTagStream.INSTANCE.toBase64(item);

// Obtener el item desde Base64
ItemStack sameItem = ItemTagStream.INSTANCE.fromBase64(base64)[0]; // Devuelve un array


List<ItemStack> items = // Lista de items;

// Convertirlo en Base64
String base64 = ItemTagStream.INSTANCE.toBase64(items);

// Obtener la lista desde Base64
List<ItemStack> sameItems = ItemTagStream.INSTANCE.listFromBase64(base64);
```

</TabItem>
<TabItem value="bytes" label="Bytes">

```java
ItemStack item = ...;

// Convertirlo en un array de bytes
byte[] bytes = ItemTagStream.INSTANCE.toBytes(item);

// Obtener el item desde el array de bytes
ItemStack sameItem = ItemTagStream.INSTANCE.fromBytes(bytes);
```

</TabItem>
<TabItem value="map" label="Map">

```java
ItemStack item = ...;

// Convertirlo en un map
Map<String, Object> map = ItemTagStream.INSTANCE.toMap(item);

// Obtener el item desde el map
ItemStack sameItem = ItemTagStream.INSTANCE.fromMap(map);
```

</TabItem>
<TabItem value="string" label="String">

```java
ItemStack item = ...;

// Convertirlo en un snbt
String snbt = ItemTagStream.INSTANCE.toString(item);

// Obtener el item desde el snbt
ItemStack sameItem = ItemTagStream.INSTANCE.fromString(snbt);
```

</TabItem>
<TabItem value="readable" label="Legible">

```java
ItemStack item = ...;

// Convertirlo en un map legible
Map<String, Object> map = ItemTagStream.INSTANCE.toReadableMap(item);

// Obtener el item desde el map legible
ItemStack sameItem = ItemTagStream.INSTANCE.fromReadableMap(map);
```

</TabItem>
</Tabs>

ItemTagStream incluye **compatibilidad con múltiples versiones**, puedes convertir cualquier item para luego obtenerlo en cualquier versión. Materiales, encantamientos, pociones... etc, todo será convertido! esto es algo que ni siquiera Bukkit tiene ya que es excluyente con las versiones viejas.

También detecta items serializados por Bukkit o Paper que agregan la versión de los datos mediante el tag `DataVersion` or `v` y de esta manera aplicar la conversión.

:::info

En la instancia por defecto de ItemTagStream, su conversión de ítems entre versiones es **únicamente compatible con Bukkit**.

Si tu servidor tiene Forge se sugiere crear tu propia instancia compatible con Forge.

:::