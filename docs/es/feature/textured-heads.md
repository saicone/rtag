---
sidebar_position: 5
title: Cabezas con textura
description: Como obtener cabezas con textura en Rtag
---

Con el class `SkullTexture` puedes obtener cabezas con textura desde el formato [base64](https://en.wikipedia.org/wiki/Base64), url, ID de textura, nombre o UUID del jugador.

## Ejemplo

Obtener la siguiente textura como un `ItemStack` de una cabeza usando todos los tipos de métodos.

![](http://textures.minecraft.net/texture/fedfa2e0fdea0c0423804cdb5b62ad05a6e914c046c4a3b7e355bf81269125fd)

```mdx-code-block
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
<TabItem value="base64" label="Base64" default>

```java
ItemStack head = SkullTexture.getTexturedHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmVkZmEyZTBmZGVhMGMwNDIzODA0Y2RiNWI2MmFkMDVhNmU5MTRjMDQ2YzRhM2I3ZTM1NWJmODEyNjkxMjVmZCJ9fQ==");
```

</TabItem>
<TabItem value="url" label="URL">

```java
ItemStack head = SkullTexture.getTexturedHead("http://textures.minecraft.net/texture/fedfa2e0fdea0c0423804cdb5b62ad05a6e914c046c4a3b7e355bf81269125fd");
```

</TabItem>
<TabItem value="texture" label="ID de textura">

```java
ItemStack head = SkullTexture.getTexturedHead("fedfa2e0fdea0c0423804cdb5b62ad05a6e914c046c4a3b7e355bf81269125fd");
```

</TabItem>
<TabItem value="name" label="Nombre">

```java
ItemStack head = SkullTexture.getTexturedHead("Rubenicos");
```

</TabItem>
<TabItem value="uuid" label="UUID">

```java
ItemStack head = SkullTexture.getTexturedHead("7ca003dc-175f-4f1f-b490-5651045311ad");
```

</TabItem>
</Tabs>