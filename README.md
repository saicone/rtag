# Rtag
[![CodeFactor](https://www.codefactor.io/repository/github/saicone/rtag/badge?style=flat-square)](https://www.codefactor.io/repository/github/saicone/rtag)
![Code size](https://img.shields.io/github/languages/code-size/saicone/rtag?style=flat-square)
![Lines of code](https://img.shields.io/tokei/lines/github/saicone/rtag?style=flat-square)

The "readable tag" library, an easy way to handle NBTTagCompounds.

Rtag convert NBT tags to known objects and viceversa for better readability.

```java
RtagItem itemTag = new RtagItem(item);

// Set values
itemTag.set("CustomValue", "deep", "path");
itemTag.set(40, "somekey");
itemTag.set("Item name!", "display", "name");
itemTag.set(new ArrayList(), "list", "path");
// Add value to list
itemTag.add((short) 3, "list", "path");

// Get values
String value = itemTag.get("deep", "path");
Integer intValue = itemTag.get("somekey");
String name = itemTag.get("display", "name");
List<Short> list = itemTag.get("list", "path");
// List value at index 0
Short listValue = itemTag.get("list", "path", 0);

// Remove values
itemTag.remove("deep", "path");
String newValue = itemTag.get("deep", "path"); // return null

// Get item copy with changes
ItemStack newItem = itemTag.loadCopy();
// Or load changes into original item
itemTag.load();
```

### Requirements
*  **At least Minecraft 1.8.8:** Rtag is made to be used in latest Minecraft versions, old versions support is only for commercial purposes.
*  Minimum Java 11

# Why Rtag
There are other libraries to edit NBT tags, why should Rtag be used over the others?

## Really fast
Rtag abuses of static final MethodHandles to convert the use of reflected methods as if they were direct calls, so it works to edit NBT tags in non-async operations without producing a bad performance impact on big servers.

## Easy to understand
You don't need to be an expert with NBT tags, just with simple methods you can set and get normal Java objects.
```java
Rtag rtag = new Rtag();
rtag.set(compound, "Normal string", "CustomTagPath");
String string = rtag.get(compound, "CustomTagPath");
```

## Store custom objects
You can register (de)serializers in Rtag instance to set and get custom objects with automatic conversion.
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
    public deserialize(Map<String, Object> compound) {
        // Convert compount into you custom object
    }
}
```

## TagStream instances

### ItemTagStream
With ItemTagStream instance you can convert items into Base64|File|Bytes and viceversa.

Including **cross-version support**! Save an item on any version and get on any version without compatibility problems. Materials, enchantments, potions... etc, all will be converted!
```java
ItemTagStream tag = ItemTagStream.INSTANCE;

String string = tag.toBase64(item);
ItemStack sameItem = tag.fromBase64(string)[0];
```

## Additional utilities

### Textured heads
With SkullTexture class you can get textures heads from base64, url or texture ID.
```java
// Example with texture ID
ItemStack head = SkullTexture.getTextureHead("6e2aaebaa1a9ead536edc79ddfade46cf50b4c40c83c102fb63d84d53c76d68f");
```

### Chat Component
With ChatComponent class you can convert (json) strings into chat components!
```java
Object component = ChatComponent.fromJson("{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"dark_purple\",\"text\":\"Colored text!\"}");

Object otherComponent = ChatComponent.fromString("§5§lColored text!");
```
