---
sidebar_position: 6
title: Chat Component
description: How to convert strings to chat components and viceversa
---

With ChatComponent class you can convert (json) strings into chat components and viceversa.

## Example

```java
// To component
Object component = ChatComponent.fromJson("{\"bold\":true,\"italic\":false,\"color\":\"dark_purple\",\"text\":\"Colored text!\"}");
Object sameComponent = ChatComponent.fromString("§5§lColored text!");

// From component
String json = ChatComponent.toJson(component);
String string = ChatComponent.toString(component);

// Cross-compatibility
String json = ChatComponent.toJson("§5§lColored text!");
String string = ChatComponent.toString("{\"bold\":true,\"italic\":false,\"color\":\"dark_purple\",\"text\":\"Colored text!\"}");
```