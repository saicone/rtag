---
sidebar_position: 5
title: Textured Heads
description: How to get textured heads with Rtag
---

With the `SkullTexture` class you can get textured heads from [base64](https://en.wikipedia.org/wiki/Base64), url, texture ID, player name or UUID.

:::info

The texture values are cached with 3 hours expiration after last access.

If you get some textured head using player name or UUID the texture value will be obtained asynchronously if it's not cached, so "LOADING" texture value will be obtained in that case.

:::

## Example

Get the following texture as `ItemStack` head using all the different methods.

![](http://textures.minecraft.net/texture/fedfa2e0fdea0c0423804cdb5b62ad05a6e914c046c4a3b7e355bf81269125fd)

```mdx-code-block
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
<TabItem value="base64" label="Base64" default>

```java
String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmVkZmEyZTBmZGVhMGMwNDIzODA0Y2RiNWI2MmFkMDVhNmU5MTRjMDQ2YzRhM2I3ZTM1NWJmODEyNjkxMjVmZCJ9fQ==";
// Simple get, the texture value will be used
ItemStack head = SkullTexture.getTexturedHead(texture);
```

</TabItem>
<TabItem value="url" label="URL">

```java
String texture = "http://textures.minecraft.net/texture/fedfa2e0fdea0c0423804cdb5b62ad05a6e914c046c4a3b7e355bf81269125fd";
// Simple get, the texture value will be used
ItemStack head = SkullTexture.getTexturedHead(texture);
```

</TabItem>
<TabItem value="texture" label="Texture ID">

```java
String texture = "fedfa2e0fdea0c0423804cdb5b62ad05a6e914c046c4a3b7e355bf81269125fd";
// Simple get, the texture value will be used
ItemStack head = SkullTexture.getTexturedHead(texture);
```

</TabItem>
<TabItem value="name" label="Name">

```java
String texture = "Rubenicos";
// Simple get, the texture may be retrieved on async operation
ItemStack head = SkullTexture.getTexturedHead(texture);

// Consume retrieved head when it's ready to use
ItemStack head = SkullTexture.getTexturedHead(texture, item -> {
    // do something
});
```

</TabItem>
<TabItem value="uuid" label="UUID">

```java
String texture = "7ca003dc-175f-4f1f-b490-5651045311ad";
// Simple get, the texture may be retrieved on async operation
ItemStack head = SkullTexture.getTexturedHead(texture);

// Consume retrieved head when it's ready to use
ItemStack head = SkullTexture.getTexturedHead(texture, item -> {
    // do something
});
```

</TabItem>
</Tabs>