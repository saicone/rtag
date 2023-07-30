---
sidebar_position: 6
title: Componente de chat
description: Como convertir strings en componentes de chat y viceversa
---

Con el class ChatComponent puedes convertir strings (en formato json) en componentes de chat y viceversa.

## Example

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