---
sidebar_position: 2
title: Objectos custom
description: Como guardar y obtener objetos custom con Rtag
---

Con Rtag puedes establecer objetos custom como NBT y obtenerlos según el tipo de objeto requerido, dependiendo en tus necesdiades existen diferentes opciones para (de)serializar objetos.

## Serializador Gson

Al usar la librería Gson dentro del código de Bukkit, es posible (de)serializar objetos mediante el siguiente proceso:

**Serializer (establecer)**: Objeto custom -> String en Json -> Map -> NBTTagCompound

**Deserializer (obtener)**: NBTTagCompound -> Map -> String en Json -> Objeto custom

```java
// Crear tu objeto custom
MyObject myObj = ...;

// --- Obtenerlo desde una instancia de Rtag
Rtag rtag = ...;
Object compount = ...;
// Establecer en "my -> object -> path"
rtag.set(compound, myObj, "my", "object", "path");
// Obtener desde "my <- object <- path"
MyObject sameObj = rtag.getOptional(compount, "my", "object", "path").as(MyObject.class);


// --- Obtenerlo desde una instancia de RtagEditor
RtagEditor tag = ...;
// Establecer en "my -> object -> path"
tag.set(myObj, "my", "object", "path");
// Obtener desde "my <- object <- path"
MyObject sameObj = tag.getOptional("my", "object", "path").as(MyObject.class);
```

## Registro en Rtag

Rtag por defecto solo tiene soporte con objetos normales de Java (String, Integer, List... etc), si quieres establecer y obtener objetos custom puedes registrar un (de)serializador en la instancia de Rtag.

**RtagSerializer**: Instancia para convertir el objeto custom en un Map.

**RtagDeserializer**: Instancia para converir el Map en un objeto custom.

:::info

Esta conversión establece un key adicional en el tag guardado para detectarlo usando el ID proporcionado.

:::

### Ejemplo

Supongamos que tienes un objeto custom llamado `CustomData` para guardar datos adicionales en tus items:

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

Ahora debes crear un class que funcione como serializador y deserializador:

```java
package my.plugin;

import java.util.HashMap;
import java.util.Map;

public class CustomDataSerializer implements RtagSerializer<CustomData>, RtagDeserializer<CustomData> {
    
    // ID usado para convertir el objeto custom en un Map
    // CustomData -> Map
    @Override
    public String getInID() {
        return "myplugin:CustomData";
    }
    
    // ID usado para convertir un Map en un objeto custom
    // Map -> CustomData
    @Override
    public String getOutID() {
        return "myplugin:CustomData";
    }

    // Convertir el objeto de CustomData en un Map
    @Override
    public Map<String, Object> serialize(CustomData data) {
        Map<String, Object> map = new HashMap();
        map.put("type", data.getType());
        map.put("level", data.getLevel());
        map.put("broken", data.isBroken());
        return map;
    }
    
    // Convertir el Map en un objeto de CustomData
    @Override
    public CustomData deserialize(Map<String, Object> map) {
        String type = map.get("type");
        Integer level = map.get("level");
        Boolean broken = map.get("broken");

        if (type == null || level == null || broken == null) {
        	return null;
        } else {
        	return new CustomData(type, level, broken);
        }
    }
}
```

:::tip

Se sugiere usar un ID con el formado de `<plugin>:<objeto>` para evitar incompatibilidad con otros plugins.

:::

Luego debes registrar el class en la instancia de Rtag que estás utilizando:

```java
Rtag rtag = ...;
CustomDataSerializer serializer = new CustomDataSerializer();

rtag.putSerializer(CustomData.class, serializer);
rtag.putDeserializer(serializer);
```

Ahora al utilizar la instancia de Rtag donde registraste el `CustomDataSerializer` podrás guardar y almacenar el objeto de CustomData.

En este ejemplo se usará un RtagItem con la instancia de Rtag que tiene registrado el `CustomDataSerializer`:

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
    
    // -- Guardar data custom en el ItemStack
    
    // Data para el item
    CustomData data = new CustomData("EPIC", 30, false);
    // Establecer en la ruta "custom -> data"
    tag.set(data, "custom", "data");
    
    // Los cambios serán cargados en el item original
    tag.load();
    
    
    // -- Obtener la data custom desde un ItemStack
    
    // Obtener la data desde "custom" -> "data" sin una conversión explícita
    CustomData itemData = tag.get("custom", "data");
    // Revisar si son iguales
    System.out.println(data.equals(itemData));
}
```