---
sidebar_position: 5
title: Cabezas con textura
description: Como obtener cabezas con textura en Rtag
---

Con el class `SkullTexture` puedes obtener cabezas con textura desde el formato [base64](https://en.wikipedia.org/wiki/Base64), url, ID de textura, nombre o UUID del jugador.

:::info

Los valores de las texturas son guardados en el caché por 3 horas desde su último acceso.

Si obtienes una cabeza con textura utilizando el nombre o UUID de jugador la textura será obtenida de manera async en caso de no estar en el caché, así que la textura de "CARGANDO" será devuelta en ese caso.

:::

## Ejemplo

Obtener la siguiente textura como un `ItemStack` de una cabeza usando todos los tipos de métodos.

![](http://textures.minecraft.net/texture/fedfa2e0fdea0c0423804cdb5b62ad05a6e914c046c4a3b7e355bf81269125fd)

```mdx-code-block
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
<TabItem value="base64" label="Base64" default>

```java
String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmVkZmEyZTBmZGVhMGMwNDIzODA0Y2RiNWI2MmFkMDVhNmU5MTRjMDQ2YzRhM2I3ZTM1NWJmODEyNjkxMjVmZCJ9fQ==";
// Obtener de manera simple, el valor de la textura será utilizado
ItemStack head = SkullTexture.getTexturedHead(texture);
```

</TabItem>
<TabItem value="url" label="URL">

```java
String texture = "http://textures.minecraft.net/texture/fedfa2e0fdea0c0423804cdb5b62ad05a6e914c046c4a3b7e355bf81269125fd";
// Obtener de manera simple, el valor de la textura será utilizado
ItemStack head = SkullTexture.getTexturedHead(texture);
```

</TabItem>
<TabItem value="texture" label="ID de la textura">

```java
String texture = "fedfa2e0fdea0c0423804cdb5b62ad05a6e914c046c4a3b7e355bf81269125fd";
// Obtener de manera simple, el valor de la textura será utilizado
ItemStack head = SkullTexture.getTexturedHead(texture);
```

</TabItem>
<TabItem value="name" label="Nombre">

```java
String texture = "Rubenicos";
// Obtener de manera simple, la textura puede ser obtenida de manera async
ItemStack head = SkullTexture.getTexturedHead(texture);

// Consumir la cabeza con textura luego ser obtenida cuando este lista para se utilizada
ItemStack head = SkullTexture.getTexturedHead(texture, item -> {
    // hacer cualquier cosa
});
```

</TabItem>
<TabItem value="uuid" label="UUID">

```java
String texture = "7ca003dc-175f-4f1f-b490-5651045311ad";
// Obtener de manera simple, la textura puede ser obtenida de manera async
ItemStack head = SkullTexture.getTexturedHead(texture);

// Consumir la cabeza con textura luego ser obtenida cuando este lista para se utilizada
ItemStack head = SkullTexture.getTexturedHead(texture, item -> {
    // hacer cualquier cosa
});
```

</TabItem>
</Tabs>