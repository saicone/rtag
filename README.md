<h1 align="center">Rtag</h1>

<h4 align="center">The "readable tag" library, an easy way to handle NBTTagCompounds.</h4>

<p align="center">
    <a href="https://www.codefactor.io/repository/github/saicone/rtag">
        <img src="https://www.codefactor.io/repository/github/saicone/rtag/badge?style=flat-square"/>
    </a>
    <a href="https://github.com/saicone/rtag">
        <img src="https://img.shields.io/github/languages/code-size/saicone/rtag?logo=github&logoColor=white&style=flat-square"/>
    </a>
    <a href="https://github.com/saicone/rtag">
        <img src="https://img.shields.io/tokei/lines/github/saicone/rtag?logo=github&logoColor=white&style=flat-square"/>
    </a>
    <a href="https://jitpack.io/#com.saicone/rtag">
        <img src="https://jitpack.io/v/com.saicone/rtag.svg?style=flat-square"/>
    </a>
    <a href="https://javadoc.saicone.com/rtag/">
        <img src="https://img.shields.io/badge/JavaDoc-Online-green?style=flat-square"/>
    </a>
    <a href="https://docs.saicone.com/rtag/">
        <img src="https://img.shields.io/badge/Saicone-Rtag%20Wiki-3b3bb0?logo=github&logoColor=white&style=flat-square"/>
    </a>
</p>

Rtag convert NBT tags to known objects and viceversa for better readability.

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

## Get Rtag

### Requirements
*  **At least Minecraft 1.8.8:** Rtag is made to be used in last Minecraft versions, old versions support is only for commercial purposes.
*  Minimum Java 11

### Project build
For Gradle Groovy project (build.gradle)
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

<details>
  <summary>For Gradle Kotlin project (build.gradle.kts)</summary>
  
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
</details>

<details>
  <summary>For Maven project (pom.xml)</summary>
  
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
</details>


# Why Rtag
There are other libraries to edit NBT tags, why should Rtag be used over the others?

## Really fast
Rtag abuses of static final MethodHandles to convert the use of reflected methods as if they were direct calls, so it works to edit NBT tags in non-async operations without producing a bad performance impact on big servers.

## Easy to understand

### Simple methods
You don't need to be an expert with NBT tags, just with simple methods you can set and get normal Java objects.
```java
Rtag rtag = new Rtag();
rtag.set(compound, "Normal string", "CustomTagPath");
String string = rtag.get(compound, "CustomTagPath");
```

### Compatibility methods
The main RtagEditor instances have methods to make tag editing easier.
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

### Functional methods
You can edit objects using functions inside RtagEditor instances and return any type of object.
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

## Store custom objects

### Using default method
By default, Rtag uses the Gson library inside Bukkit to (de)serialize custom objects, but you need to get them using explicit conversion.
```java
Rtag rtag = new Rtag();
MyObject myObject = new MyObject();

rtag.set(compound, myObject, "CustomTagPath");
MyObject sameObject = rtag.getOptional(compound, "CustomTagPath").as(MyObject.class);
```

### Using your own method
You can register (de)serializers in Rtag instance to set and get custom objects with automatic conversion.

This conversion put an additional key into your saved tag to detect it using the provided ID.
```java
public class MyObjectSerializer implements RtagSerializer<MyObject>, RtagDeserializer<MyObject> {
    
    public MyObjectSerializer(Rtag rtag) {
        rtag.putSerializer(MyObject.class, this);
        rtag.putDeserializer(this);
    }
    
    // MyObject -> Map
    @Override
    public String getInID() {
        // It's suggested to use a unique namespaced key
        return "myplugin:MyObject";
    }
    
    // Map -> MyObject
    @Override
    public String getOutID() {
        return "myplugin:MyObject";
    }

    @Override
    public Map<String, Object> serialize(MyObject object) {
        // Convert your custom object into map
    }
    
    @Override
    public MyObject deserialize(Map<String, Object> compound) {
        // Convert compound into you custom object
    }
}
```
Then you can get your custom object without explicit conversion.
```java
Rtag rtag = new Rtag();
new MyObjectSerializer(rtag);
MyObject myObject = new MyObject();

rtag.set(compound, myObject, "CustomTagPath");
MyObject sameObject = rtag.get(compound, "CustomTagPath");
```

## TagStream instances

### ItemTagStream
With ItemTagStream instance you can convert items into Base64|File|Bytes|Map|String and viceversa.

Including **cross-version support**! Save an item on any version and get on any version without compatibility problems. Materials, enchantments, potions... etc, all will be converted!
```java
ItemTagStream tag = ItemTagStream.INSTANCE;

String string = tag.toBase64(item);
ItemStack sameItem = tag.fromBase64(string)[0];
```

## Additional utilities

### Textured heads
With SkullTexture class you can get textured heads from base64, url, texture ID, player name or uuid.
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
With ChatComponent class you can convert (json) strings into chat components and viceversa.
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
