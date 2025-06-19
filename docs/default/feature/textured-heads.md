---
sidebar_position: 5
title: Player Head/Profile
description: How to get textured heads or profiles with Rtag
---

With the `SkullTexture` class you can get textured heads from [base64](https://en.wikipedia.org/wiki/Base64), url, texture ID, player name or UUID, and also get player profiles.

## Head

Get the following texture as `ItemStack` head using all the different methods.

![](http://textures.minecraft.net/texture/fedfa2e0fdea0c0423804cdb5b62ad05a6e914c046c4a3b7e355bf81269125fd)

```mdx-code-block
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
<TabItem value="base64" label="Base64" default>

```java
String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmVkZmEyZTBmZGVhMGMwNDIzODA0Y2RiNWI2MmFkMDVhNmU5MTRjMDQ2YzRhM2I3ZTM1NWJmODEyNjkxMjVmZCJ9fQ==";
ItemStack head = SkullTexture.mojang().item(texture);
```

</TabItem>
<TabItem value="url" label="URL">

```java
String texture = "http://textures.minecraft.net/texture/fedfa2e0fdea0c0423804cdb5b62ad05a6e914c046c4a3b7e355bf81269125fd";
ItemStack head = SkullTexture.mojang().item(texture);
```

</TabItem>
<TabItem value="texture" label="Texture ID">

```java
String texture = "fedfa2e0fdea0c0423804cdb5b62ad05a6e914c046c4a3b7e355bf81269125fd";
ItemStack head = SkullTexture.mojang().item(texture);
```

</TabItem>
<TabItem value="player" label="Player">

```java
Player player = Bukkit.getPlayer("Rubenicos");
ItemStack head = SkullTexture.mojang().item(player);
```

:::warning Player profiles are provided by the server

Obtaining a player head using the player itself may cause problems with offline-mode servers.

:::

</TabItem>
<TabItem value="name" label="Name">

```java
String name = "Rubenicos";
// Using Mojang API
ItemStack head = SkullTexture.mojang().item(name);
// Using PlayerDB API
ItemStack head = SkullTexture.playerDB().item(name);
// Using CraftHead API
ItemStack head = SkullTexture.craftHead().item(name);

// --- Also get the item asynchronously, this is useful to avoid the server to get stuck
// Using Mojang API
CompletableFuture<ItemStack> head = SkullTexture.mojang().itemAsync(name);
// Using PlayerDB API
CompletableFuture<ItemStack> head = SkullTexture.playerDB().itemAsync(name);
// Using CraftHead API
CompletableFuture<ItemStack> head = SkullTexture.craftHead().itemAsync(name);

head.thenAccept(item -> {
    // do something
});
```

</TabItem>
<TabItem value="uuid" label="UUID">

```java
// --- Compatible with multiple types of UUID declaration
UUID uniqueId = UUID.fromString("7ca003dc-175f-4f1f-b490-5651045311ad");
String uniqueId = "7ca003dc-175f-4f1f-b490-5651045311ad";
String uniqueId = "7ca003dc175f4f1fb4905651045311ad";

// Using Mojang API
ItemStack head = SkullTexture.mojang().item(uniqueId);
// Using PlayerDB API
ItemStack head = SkullTexture.playerDB().item(uniqueId);
// Using CraftHead API
ItemStack head = SkullTexture.craftHead().item(uniqueId);

// --- Also get the item asynchronously, this is useful to avoid the server to get stuck
// Using Mojang API
CompletableFuture<ItemStack> head = SkullTexture.mojang().itemAsync(uniqueId);
// Using PlayerDB API
CompletableFuture<ItemStack> head = SkullTexture.playerDB().itemAsync(uniqueId);
// Using CraftHead API
CompletableFuture<ItemStack> head = SkullTexture.craftHead().itemAsync(uniqueId);

head.thenAccept(item -> {
    // do something
});
```

</TabItem>
</Tabs>

:::tip Server performance

If you want to get some textured head using player name or UUID is suggested to use asynchronous methods since it will probably require internet connection.

And also provide a separated [Executor](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Executor.html) if you want to get an exaggerated
amount of textured heads using names and UUIDs, DO NOT use Bukkit scheduler due you will slow down others plugins performance.

:::

## Profile

Get a `SkullTexture.Profile` (an encapsulation of GameProfile from Mojang code) using different types of profile providers.

```java
// --- Compatible with multiple types of player declaration
// Player object
Player player = Bukkit.getPlayer("Rubenicos");
// Player name
String player = "Rubenicos";
// Player id
UUID player = UUID.fromString("7ca003dc-175f-4f1f-b490-5651045311ad");
String player = "7ca003dc-175f-4f1f-b490-5651045311ad";
String player = "7ca003dc175f4f1fb4905651045311ad";

// Using Mojang API
SkullTexture.Profile profile = SkullTexture.mojang().profileFrom(player);
// Using PlayerDB API
SkullTexture.Profile profile = SkullTexture.playerDB().profileFrom(player);
// Using CraftHead API
SkullTexture.Profile profile = SkullTexture.craftHead().profileFrom(player);
```

:::warning Profile fetching

Getting profiles in most cases require internet connection with long waiting times, DO NOT fetch different profiles
continuously on the main thread or using Bukkit scheduler (Make your own one in this case).

:::

:::info Cache rules

By default, profiles are cached with 3 hours expiration after last access to reduce network usage.

If you don't want to cache anything make your own implementation of `SkullTexture` or create
a new `SkullTexture.Mojang`, `SkullTexture.PlayerDB` or `SkullTexture.CraftHead` with `null` cache.

:::
