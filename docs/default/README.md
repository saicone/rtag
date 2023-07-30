---
sidebar_position: 1
title: Rtag
description: The "readable tag" library, an easy way to handle NBTTagCompounds.
---

Welcome to Rtag wiki, here you find information about Rtag usage, including the usages with Minecraft server customization.

```java
// Using Item
RtagItem tag = new RtagItem(item);
// Using Entity
RtagEntity tag = new RtagEntity(entity);
// Using block
RtagBlock tag = new RtagBlock(block);


// --- Put values
// Set the value "Custom Text" at "display.Name" path
tag.set("Custom Text", "display", "Name");
// Or set an integer at "someKey" path
tag.set(40, "someKey");
// Including compatibility with any type of object like MyObject
MyObject myobject = new MyObject();
tag.set(myobject, "any", "path");

// So you can add lists
tag.set(new ArrayList(), "list", "path");
// And add values into list
tag.add((short) 3, "list", "path");
// Or replace the values of existing list
tag.set((short) 5, "list", "path", 0); // index 0

// --- Get values
// Value from path "display" -> "Name"
String name = tag.get("display", "Name");
// Safe value get from path "someKey", or -1 by default
int intValue = tag.getOptional("someKey").or(-1);
int sameValue = tag.getOptional("someKey").asInt(-1); // This method try to convert any type to int
// Explicit value get for custom objects
MyObject sameobject = tag.getOptional("any", "path").as(MyObject.class);

// Get lists
List<Short> list = tag.get("list", "path");
// Get list value from index
short listValue = tag.get("list", "path", 0); // index 0

// Get the entire object tag as Map of Java objects
Map<String, Object> map = tag.get();

// --- Load changes into object
// Load changes into original object
tag.load();
// RtagItem as the option to create an item copy with changes loaded
ItemStack itemCopy = tag.loadCopy();

// --- Update current tag if the original object was edited
tag.update();
```

## About Rtag

Rtag is a library whose idea was planned around 2020, but the development starts in 2022 due to commercial limitations about attachment to old systems by most developers.

This library was created to solve the need to develop a Minecraft server with a wide variety of custom things avoiding a bad performance impact on big servers, using an "easy to understand" way to handle NBT.

You don't need to be an expert with NBT tags, just with simple methods you can set and get normal Java objects.

## Get Rtag

![version](https://img.shields.io/github/v/tag/saicone/rtag?label=current%20version&style=for-the-badge)

### Requirements

*  At least **Minecraft 1.8.8:** Rtag is made to be used in latest Minecraft versions, old versions support is only for commercial purposes.
*  Minimum **Java 11**

### Dependency

To use Rtag in your project **without shading** you need to add first as a dependency and tell your users to download Rtag from their [Spigot page](https://www.spigotmc.org/resources/rtag.100694/).

```mdx-code-block
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs groupId="build-file">
<TabItem value="groovy" label="build.gradle" default>

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.saicone.rtag:rtag:VERSION'
    // Other modules
    compileOnly 'com.saicone.rtag:rtag-block:VERSION'
    compileOnly 'com.saicone.rtag:rtag-entity:VERSION'
    compileOnly 'com.saicone.rtag:rtag-item:VERSION'
}
```

</TabItem>
<TabItem value="kotlin" label="build.gradle.kts">

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.saicone.rtag:rtag:VERSION")
    // Other modules
    compileOnly("com.saicone.rtag:rtag-block:VERSION")
    compileOnly("com.saicone.rtag:rtag-entity:VERSION")
    compileOnly("com.saicone.rtag:rtag-item:VERSION")
}
```

</TabItem>
<TabItem value="maven" label="pom.xml">

```xml
<repositories>
    <repository>
        <id>Jitpack</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
  
<dependencies>
    <dependency>
        <groupId>com.saicone.rtag</groupId>
        <artifactId>rtag</artifactId>
        <version>VERSION</version>
        <scope>provided</scope>
    </dependency>
    <!-- Other modules -->
    <dependency>
        <groupId>com.saicone.rtag</groupId>
        <artifactId>rtag-block</artifactId>
        <version>VERSION</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.saicone.rtag</groupId>
        <artifactId>rtag-entity</artifactId>
        <version>VERSION</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.saicone.rtag</groupId>
        <artifactId>rtag-item</artifactId>
        <version>VERSION</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

</TabItem>
</Tabs>

### Shaded Dependency

Rtag it's completely shadeable, you can implement rtag directly in your plugin (no download requried by the user).

<Tabs groupId="build-file">
<TabItem value="groovy" label="build.gradle" default>

```groovy
plugins {
    id 'com.github.johnrengelman.shadow' version '8.1.1'
}

repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation  'com.saicone.rtag:rtag:VERSION'
    // Other modules
    implementation 'com.saicone.rtag:rtag-block:VERSION'
    implementation 'com.saicone.rtag:rtag-entity:VERSION'
    implementation 'com.saicone.rtag:rtag-item:VERSION'
}

jar.dependsOn (shadowJar)

shadowJar {
    // Relocate rtag (DO NOT IGNORE THIS)
    relocate 'com.saicone.rtag', project.group + '.libs.rtag'
    // Exclude unused classes (optional)
    minimize()
}
```

</TabItem>
<TabItem value="kotlin" label="build.gradle.kts">

```kotlin
plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.saicone.rtag:rtag:VERSION")
    // Other modules
    implementation("com.saicone.rtag:rtag-block:VERSION")
    implementation("com.saicone.rtag:rtag-entity:VERSION")
    implementation("com.saicone.rtag:rtag-item:VERSION")
}

tasks {
    jar {
        dependsOn(tasks.shadowJar)
    }

    shadowJar {
        // Relocate rtag (DO NOT IGNORE THIS)
        relocate("com.saicone.rtag", "${project.group}.libs.rtag")
        // Exclude unused classes (optional)
        minimize()
    }
}
```

</TabItem>
<TabItem value="maven" label="pom.xml">

```xml
<repositories>
    <repository>
        <id>Jitpack</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
  
<dependencies>
    <dependency>
        <groupId>com.saicone.rtag</groupId>
        <artifactId>rtag</artifactId>
        <version>VERSION</version>
        <scope>compile</scope>
    </dependency>
    <!-- Other modules -->
    <dependency>
        <groupId>com.saicone.rtag</groupId>
        <artifactId>rtag-block</artifactId>
        <version>VERSION</version>
        <scope>compile</scope>
    </dependency>
    <dependency>
        <groupId>com.saicone.rtag</groupId>
        <artifactId>rtag-entity</artifactId>
        <version>VERSION</version>
        <scope>compile</scope>
    </dependency>
    <dependency>
        <groupId>com.saicone.rtag</groupId>
        <artifactId>rtag-item</artifactId>
        <version>VERSION</version>
        <scope>compile</scope>
    </dependency>
</dependencies>

<build>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-shade-plugin</artifactId>
        <version>3.3.0</version>
        <configuration>
            <relocations>
                <!-- Relocate rtag (DO NOT IGNORE THIS) -->
                <relocation>
                    <pattern>com.saicone.rtag</pattern>
                    <shadedPattern>${project.groupId}.libs.rtag</shadedPattern>
                </relocation>
            </relocations>
            <!-- Exclude unused classes (optional) -->
            <minimizeJar>true</minimizeJar>
        </configuration>
        <executions>
            <execution>
                <phase>package</phase>
                <goals>
                    <goal>shade</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
</build>
```

</TabItem>
</Tabs>

## Why Rtag

There are other libraries to edit NBT tags, why should Rtag be used over the others?

### Speed like direct calls

Rtag abuses of `static final MethodHandle` to convert the use of reflected methods as if they were direct calls, so it works to edit NBT tags in non-async operations without producing a bad performance impact on big servers.

### Easy to understand

You don't need to be an expert with NBT tags, just with simple methods you can set and get normal Java objects using tree-like path.

```java
Rtag rtag = new Rtag();
rtag.set(compound, "Normal string", "Custom", "Tag", "Path");
String string = rtag.get(compound, "Custom", "Tag", "Path");
```

The main [RtagEditor instances](usage/editor/) have methods to make tag editing easier.

```java
RtagItem tag = new RtagItem(item);
tag.setUnbreakable(true);
tag.setRepairCost(20);
int level = tag.getEnchantmentLevel("unbreaking"); // Enchantment enum, name or id

RtagEntity tag = new RtagEntity(entity);
tag.setAttributeBase("generic.attackDamage", 0.5);

RtagBlock tag = new RtagBlock(block);
tag.setCustomName("§eColored name");
```

So you can edit objects using functions inside RtagEditor instances and return any type of object.

```java
ItemStack item = ...;
// Edit original
RtagItem.edit(item, tag -> {
    tag.set("Custom Text", "display", "name");
    tag.set(30, "someKey");
});
// Return a copy
ItemStack copy = RtagItem.edit(item, tag -> {
    tag.set(30, "someKey");
    return tag.loadCopy();
});
```

### Store custom objects

By default, Rtag uses the Gson library inside Bukkit to (de)serialize custom objects, but you need to get them using explicit conversion.

You can register (de)serializers in Rtag instance to [set and get custom objects with automatic conversion](feature/custom-objects/).

```java
Rtag rtag = new Rtag();
MyObject myObject = new MyObject();

rtag.set(compound, myObject, "CustomTagPath");
MyObject sameObject = rtag.getOptional(compound, "CustomTagPath").as(MyObject.class);
```

### TagStream instances

With [ItemTagStream instance](feature/stream/) you can convert items into Base64|File|Bytes|Map|String and viceversa.
Including **cross-version support**! Save an item on any version and get on any version without compatibility problems. Materials, enchantments, potions... etc, all will be converted!

```java
ItemTagStream tag = ItemTagStream.INSTANCE;

String string = tag.toBase64(item);
ItemStack sameItem = tag.fromBase64(string)[0];
```

### Textured heads

With [SkullTexture class](feature/skulls/) you can get textured heads from base64, url, texture ID, player name or uuid.

```java
// Base64
ItemStack head = SkullTexture.getTexturedHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmVkZmEyZTBmZGVhMGMwNDIzODA0Y2RiNWI2MmFkMDVhNmU5MTRjMDQ2YzRhM2I3ZTM1NWJmODEyNjkxMjVmZCJ9fQ==");
// URL
ItemStack head = SkullTexture.getTexturedHead("http://textures.minecraft.net/texture/fedfa2e0fdea0c0423804cdb5b62ad05a6e914c046c4a3b7e355bf81269125fd");
// Texture ID
ItemStack head = SkullTexture.getTexturedHead("fedfa2e0fdea0c0423804cdb5b62ad05a6e914c046c4a3b7e355bf81269125fd");
// Player name
ItemStack head = SkullTexture.getTexturedHead("Rubenicos");
// Player UUID
ItemStack head = SkullTexture.getTexturedHead("7ca003dc-175f-4f1f-b490-5651045311ad");
```

### Chat Component

With [ChatComponent class](feature/chat-component/) you can convert (json) strings into chat components and viceversa.

````java
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