---
sidebar_position: 2
title: Custom Objects
description: How to save and get custom objects in Rtag
---

With Rtag you can set custom objects into NBT tags and get has the required type, depends on your needs there are different options to (de)serialize objects.

## Gson serializer

By using the Gson library inside Bukkit code, it's possible to convert objets following the next process:

**Serializer (set)**: Custom Object -> Json String -> Map -> NBTTagCompound

**Deserializer (get)**: NBTTagCompound -> Map -> Json String -> Custom Object

```java
// Create your custom object
MyObject myObj = ...;

// --- Using Rtag instance
Rtag rtag = ...;
Object compount = ...;
// Set to "my -> object -> path"
rtag.set(compound, myObj, "my", "object", "path");
// Get from "my <- object <- path"
MyObject sameObj = rtag.getOptional(compount, "my", "object", "path").as(MyObject.class);


// --- Using RtagEditor instance
RtagEditor tag = ...;
// Set to "my -> object -> path"
tag.set(myObj, "my", "object", "path");
// Get from "my <- object <- path"
MyObject sameObj = tag.getOptional("my", "object", "path").as(MyObject.class);
```

## Rtag registry

Rtag by default only has support for normal Java objects (String, Integer, List... etc), if you want to set and get custom objects you can register a (de)serializer into Rtag instance.

**RtagSerializer**: Instance to convert a custom object into Map.

**RtagDeserializer**: Instance to convert a Map into a custom object.

:::info

This conversion put an additional key into your saved tag to detect it using the provided ID.

:::

### Example

Suppose you have a custom object named `CustomData` to save additional data in your items:

```java
package my.plugin;

public class CustomData {

    private final String type;
    private int level;
    private boolean broken;

    public CustomData(String type, int level, boolean broken) {
        this.type = type;
        this.level = level;
        this.broken = broken;
    }

    public String getType() {
        return type;
    }

    public int getLevel() {
    	return level;
    }

    public boolean isBroken() {
    	return broken;
    }

    public void setLevel(int level) {
    	this.level = level;
    }

    public void setBroken(boolean broken) {
    	this.broken = broken;
    }

    public boolean equals(Object object) {
    	if (object instanceof CustomData) {
    		CustomData data = (CustomData) object;
    		return data.getType().equals(type) && data.getLevel == level && data.isBroken == broken;
    	}
    	return false;
    }
}
```

So you need to create a (de)serializer for it:

```java
package my.plugin;

import java.util.HashMap;
import java.util.Map;

public class CustomDataConversion implements RtagSerializer<CustomData>, RtagDeserializer<CustomData> {
    
    // ID used when the object will be converted into Map.
    // CustomData -> Map
    @Override
    public String getInID() {
        return "myplugin:CustomData";
    }
    
    // ID used when an Map will be converted into the object.
    // Map -> CustomData
    @Override
    public String getOutID() {
        return "myplugin:CustomData";
    }

    // Convert the CustomData into Map
    @Override
    public Map<String, Object> serialize(CustomData data) {
        Map<String, Object> map = new HashMap();
        map.put("type", data.getType());
        map.put("level", data.getLevel());
        // Boolean value must be saved as byte
        map.put("broken", data.isBroken() ? (byte) 1 : (byte) 0);
        return map;
    }
    
    // Convert the Map into CustomData
    @Override
    public CustomData deserialize(Map<String, Object> map) {
        String type = (String) map.get("type");
        Integer level = (Integer) map.get("level");
        Byte broken = (Byte) map.get("broken");

        if (type == null || level == null || broken == null) {
        	return null;
        } else {
        	return new CustomData(type, level, broken == (byte) 1);
        }
    }
}
```

:::tip

It's suggested to use an ID with the format `<plugin>:<object>` to avoid incompatibility issues with other plugins 

:::

Then you need to register the (de)serializer into used Rtag instance:

```java
Rtag rtag = ...;
CustomDataConversion serializer = new CustomDataConversion();

rtag.putSerializer(CustomData.class, serializer);
rtag.putDeserializer(serializer);
```

Now when you use the Rtag instance with registered `CustomDataConversion`, you can set and get the CustomData with simple methods.

In this example will be used an RtagItem with Rtag that have the `CustomDataConversion`:

```java
private final Rtag rtag = initRtag();

private Rtag initRtag() {
    Rtag rtag = new Rtag();
    CustomDataConversion serializer = new CustomDataConversion();
    rtag.putSerializer(CustomData.class, serializer);
    rtag.putDeserializer(serializer);
    return rtag;
}

public void example(ItemStack item) {
    RtagItem tag = new RtagItem(rtag, item);
    
    // -- Save custom data into ItemStack
    
    // Data for the item
    CustomData data = new CustomData("EPIC", 30, false);
    // Save at path "custom -> data"
    tag.set(data, "custom", "data");
    
    // The changes will be loaded into original item
    tag.load();
    
    
    // -- Get custom data from ItemStack
    
    // Get data from "custom" -> "data" without explicit conversion
    CustomData itemData = tag.get("custom", "data");
    // Check if it equals
    System.out.println(data.equals(itemData));
}
```