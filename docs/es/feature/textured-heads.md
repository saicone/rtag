---
sidebar_position: 5
title: Cabeza/Perfil de Jugador
description: Como obtener cabezas y perfiles con Rtag
---

Con el class `SkullTexture` puedes obtener cabezas con textura desde el formato [base64](https://en.wikipedia.org/wiki/Base64), url, ID de textura, nombre o UUID del jugador, y también perfiles de jugador.

## Cabeza

Obtener la siguiente textura como un `ItemStack` de una cabeza usando todos los tipos de métodos.

![](http://textures.minecraft.net/texture/fedfa2e0fdea0c0423804cdb5b62ad05a6e914c046c4a3b7e355bf81269125fd)

```mdx-code-block
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
<TabItem value="base64" label="Base64" default>

```java
String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmVkZmEyZTBmZGVhMGMwNDIzODA0Y2RiNWI2MmFkMDVhNmU5MTRjMDQ2YzRhM2I3ZTM1NWJmODEyNjkxMjVmZCJ9fQ==";
ItemStack head = SkullTexture.getTexturedHead(texture);
```

</TabItem>
<TabItem value="url" label="URL">

```java
String texture = "http://textures.minecraft.net/texture/fedfa2e0fdea0c0423804cdb5b62ad05a6e914c046c4a3b7e355bf81269125fd";
ItemStack head = SkullTexture.getTexturedHead(texture);
```

</TabItem>
<TabItem value="texture" label="ID de la textura">

```java
String texture = "fedfa2e0fdea0c0423804cdb5b62ad05a6e914c046c4a3b7e355bf81269125fd";
ItemStack head = SkullTexture.getTexturedHead(texture);
```

</TabItem>
<TabItem value="player" label="Jugador">

```java
Player player = Bukkit.getPlayer("Rubenicos");
ItemStack head = SkullTexture.mojang().item(player);
```

:::warning Los perfiles de jugador son dados por el servidor

Obtener la cabeza de un jugador usando el propio jugador puede causar problemas en los servidores no-premium.

:::

</TabItem>
<TabItem value="name" label="Nombre">

```java
String name = "Rubenicos";
// Usando la API de Mojang
ItemStack head = SkullTexture.mojang().item(name);
// Usando la API de PlayerDB
ItemStack head = SkullTexture.playerDB().item(name);
// Usando la API de CraftHead
ItemStack head = SkullTexture.craftHead().item(name);

// --- Obtener el item async, esto es bastante util para evitar lagear el servidor
// Usando la API de Mojang
CompletableFuture<ItemStack> head = SkullTexture.mojang().itemAsync(name);
// Usando la API de PlayerDB
CompletableFuture<ItemStack> head = SkullTexture.playerDB().itemAsync(name);
// Usando la API de CraftHead
CompletableFuture<ItemStack> head = SkullTexture.craftHead().itemAsync(name);

head.thenAccept(item -> {
    // usar el item
});
```

</TabItem>
<TabItem value="uuid" label="UUID">

```java
// --- Compatible con todo tipo de formas de declarar el UUID de un jugador
UUID uniqueId = UUID.fromString("7ca003dc-175f-4f1f-b490-5651045311ad");
String uniqueId = "7ca003dc-175f-4f1f-b490-5651045311ad";
String uniqueId = "7ca003dc175f4f1fb4905651045311ad";

// Usando la API de Mojang
ItemStack head = SkullTexture.mojang().item(uniqueId);
// Usando la API de PlayerDB
ItemStack head = SkullTexture.playerDB().item(uniqueId);
// Usando la API de CraftHead
ItemStack head = SkullTexture.craftHead().item(uniqueId);

// --- Obtener el item async, esto es bastante util para evitar lagear el servidor
// Usando la API de Mojang
CompletableFuture<ItemStack> head = SkullTexture.mojang().itemAsync(uniqueId);
// Usando la API de PlayerDB
CompletableFuture<ItemStack> head = SkullTexture.playerDB().itemAsync(uniqueId);
// Usando la API de CraftHead
CompletableFuture<ItemStack> head = SkullTexture.craftHead().itemAsync(uniqueId);

head.thenAccept(item -> {
    // usar el item
});
```

</TabItem>
</Tabs>

:::tip Rendimiento del servidor

Si quieres obtener la cabeza de un jugador usando el nombre o UUID del jugador, es recomendable usar los métodos async ya que probablemente se usará internet para obtener la textura.

Además, crea un [Executor](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Executor.html) si lo que quieres es obtener una cantidad exagerada
de cabezas con textura usando nombres y UUIDs, NO USES el scheduler de Bukkit porque podrías lagear otros plugins.

:::

## Perfil

Obtener un `SkullTexture.Profile` (es una encapsulación del GameProfile de Mojang) usando diferentes objetos de los cuales se puede extraer un perfil.

```java
// --- Compatible con diferentes formas de declarar un jugador
// El mismo jugador
Player player = Bukkit.getPlayer("Rubenicos");
// Nombre del jugador
String player = "Rubenicos";
// ID del jugador
UUID player = UUID.fromString("7ca003dc-175f-4f1f-b490-5651045311ad");
String player = "7ca003dc-175f-4f1f-b490-5651045311ad";
String player = "7ca003dc175f4f1fb4905651045311ad";

// Usando la API de Mojang
SkullTexture.Profile profile = SkullTexture.mojang().profileFrom(player);
// Usando la API de PlayerDB
SkullTexture.Profile profile = SkullTexture.playerDB().profileFrom(player);
// Usando la API de CraftHead
SkullTexture.Profile profile = SkullTexture.craftHead().profileFrom(player);
```

:::warning Obtener perfiles

La obtención de perfiles en la mayoría de veces requiere conexión a internet con largos tiempos de espera, NO USES estos
métodos para obtener muchos perfiles diferentes de manera continua en el hilo principal del servidor o usando el scheduler de Bukkit
(Es mejor que te hagas tu propio scheduler en este caso).

:::

:::info Reglas para el caché

Los valores de los perfiles son guardados en el caché por 3 horas desde su último acceso para reduir el consumo de datos.

Si no quieres que nada se guarde en el caché deberías hacer tu propia implementación de `SkullTexture` o crear un
`SkullTexture.Mojang`, `SkullTexture.PlayerDB` o `SkullTexture.CraftHead` usando `null` en el parámetro del caché.

:::
