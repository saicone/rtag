---
sidebar_position: 6
title: Componente de chat
description: Como utilizar el class de componentes de chat.
---

El class `ChatComponent` contiene multiples métodos utiles para manejar cosas relacionadas con el chat.

## Conversión

Con el class `ChatComponent` puedes convertir strings (en formato json) en componentes de chat y viceversa.

```java
// En component
Object component = ChatComponent.fromJson("{\"bold\":true,\"italic\":false,\"color\":\"dark_purple\",\"text\":\"Colored text!\"}");
Object sameComponent = ChatComponent.fromString("§5§lColored text!");

// Desde component
String json = ChatComponent.toJson(component);
String string = ChatComponent.toString(component);

// Compatibilidad cruzada
String json = ChatComponent.toJson("§5§lColored text!");
String string = ChatComponent.toString("{\"bold\":true,\"italic\":false,\"color\":\"dark_purple\",\"text\":\"Colored text!\"}");
```

## Formato mejorado para nbt

Dale un formato mejorado (pretty format) a cualquier objeto nbt en una o múltiples líneas separadas por `\n` usando una paleta de color:

* `NBT_PALETTE` - La paleta de color por defecto como el comando `/data` de Minecraft (`[ "§f", "§b", "§a", "§6", "§c" ]`).
* `NBT_PALETTE_HEX` - Paleta de color modificada para Bukkit 1.16 o superior utilizando un formato hexadecimal para colores como `§#RRGGBB`.
* `NBT_PALETTE_BUNGEE` - Paleta de color modificada para Bukkit 1.16 o superior utilizando el formato hexadecimal del chat de bungeecord para colores como `§x§R§R§G§G§B§B`.
* `NBT_PALETTE_MINIMESSAGE` - Paleta de color por defecto utilizando el [formato MiniMessage de Adventure](https://docs.advntr.dev/minimessage/index.html).

Ejemplo con la paleta de color por defecto:

![Pretty NBT](https://raw.githubusercontent.com/saicone/rtag/main/docs/images/pretty-nbt.png)

:::info

Para hacer tu propia paleta debes proporcionar un `String[]` de un tamaño de 5 con los colores en el orden de:

1. Color base.
2. Color de las key (llave) de los compound.
3. Strings.
4. Números.
5. Sufijo de los números.

:::

Ejemplo con una paleta de color personalizada:

![Pretty NBT with palette](https://raw.githubusercontent.com/saicone/rtag/main/docs/images/pretty-nbt-palette.png)

### Linea simple

Para aplicar el formato en una sola línea solamente se debe utilizar `null` en el segundo argumento.

```java
Object nbt = ...;

// Formato mejorato (pretty format) en un componente de chat, componente en json o un texto de color
Object component = ChatComponent.toPrettyComponent(nbt, null);
String json = ChatComponent.toPrettyJson(nbt, null);
String colored = ChatComponent.toPrettyString(nbt, null);

// Usando una paleta de color (ejemplo con texto de color)
String colored = ChatComponent.toPrettyString(nbt, null, ChatComponent.NBT_PALETTE_BUNGEE);
```

### Múltiples líneas

Para aplicar el formato en múltiples líneas se debe proporcionar un `String` para usarlo como sangría, por ejemplo `"  "` para una sangría de 2 espacios.

```java
Object nbt = ...;

// Formato mejorato (pretty format) en un componente de chat, componente en json o un texto de color
Object component = ChatComponent.toPrettyComponent(nbt, "  ");
String json = ChatComponent.toPrettyJson(nbt, "  ");
String colored = ChatComponent.toPrettyString(nbt, "  ");

// Usando una paleta de color (ejemplo con texto de color)
String colored = ChatComponent.toPrettyString(nbt, "  ", ChatComponent.NBT_PALETTE_BUNGEE);
```