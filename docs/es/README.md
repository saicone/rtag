---
sidebar_position: 1
title: Rtag
description: La librería de "leer NBTTagCompound" en una forma fácil.
---

Bienvenid@ a la wiki de Rtag, aquí encontrarás información sobre como utilizar Rtag además de sus posibilidades dentro de la "customización" de un servidor de Minecraft.

```java
// Usando un item
RtagItem tag = new RtagItem(item);
// Usando una entidad
RtagEntity tag = new RtagEntity(entity);
// Usando un bloque
RtagBlock tag = new RtagBlock(block);


// --- Establecer valores
// Establecer el valor "Custom Text" en la ruta "display.Name"
tag.set("Texto custom", "display", "Name");
// O establecer una integral en la ruta "someKey"
tag.set(40, "someKey");
// Incluyendo compatibilidad con cualquier tipo de objeto como MyObject
MyObject myobject = new MyObject();
tag.set(myobject, "any", "path");

// También puedes añadir listas
tag.set(new ArrayList(), "list", "path");
// Y añadir valores dentro de las listas
tag.add((short) 3, "list", "path");
// O reemplazar un valor de una lista existente
tag.set((short) 5, "list", "path", 0); // posición 0

// --- Obtener valores
// Valor desde la ruta "display" -> "Name"
String name = tag.get("display", "Name");
// Obtener un valor de manera segura desde la ruta "someKey", -1 por defecto
int intValue = tag.getOptional("someKey").or(-1);
int sameValue = tag.getOptional("someKey").asInt(-1); // Este método trata de convertir cualquier tipo a una integral
// Obtención de valor explícita para objetos custom
MyObject sameobject = tag.getOptional("any", "path").as(MyObject.class);

// Obtener listas
List<Short> list = tag.get("list", "path");
// Obtener un valor desde su posición en una lista
short listValue = tag.get("list", "path", 0); // posición 0

// Obtener todo el tag como un Map con objetos
Map<String, Object> map = tag.get();

// --- Cargar los cambios realizados en el objeto (item, entidad, bloque)
// Cargar los cambios en el objeto original
tag.load();
// El RtagItem tiene la opción de cargar una copia del item original pero con los cambios
ItemStack itemCopy = tag.loadCopy();

// --- Actualizar el tag actual en caso de que el objeto haya sido editado
tag.update();
```

## Sobre Rtag

Rtag es una librería cuya idea fue planteada en 2020, pero su desarrollo empezó hasta 2022 debido a limitaciones comerciales con respecto al apego hacia sistemas viejos por parte de la mayoría de desarrolladores.

Esta librería fue creada con el fin de resolver la necesidad de desarrollar un servidor de Minecraft con gran variedad de cosas y al mismo tiempo mantener muchos jugadores sin que el rendimiento se vea afectado, usando así un sistema moderno y amigable para el usuario.

Sin la necesidad de conocer a fondo los NBT, con Rtag es posible manejarlos mediante métodos simples como get() y set().

## Obtener Rtag

![version](https://img.shields.io/github/v/tag/saicone/rtag?label=version%20actual&style=for-the-badge)

### Requisitos

*  Mínimo **Minecraft 1.8.8:** Rtag ofrece soporte a versiones bastante viejas por razones comerciales, pero en un principio está diseñado para usarse con mayor eficiencia en las últimas versiones de Minecraft.
*  Mínimo **Java 11**

### Dependencia

Para utilizar Rtag en tu proyecto se debe agregar como una dependencia y decirle a los usuarios de tu plugin/proyecto que descarguen Rtag como plugin desde su [página de Spigot](https://www.spigotmc.org/resources/rtag.100694/).

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
    // Otros módulos
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
    // Otros módulos
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
    <!-- Otros módulos -->
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

### Dependencia implementada

Rtag es completamente compatible para ser implementado dentro de tu proyecto, si lo usas de esta manera NO hace falta que los usuarios de tu plugin/proyecto descarguen el plugin.

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
    // Otros módulos
    implementation 'com.saicone.rtag:rtag-block:VERSION'
    implementation 'com.saicone.rtag:rtag-entity:VERSION'
    implementation 'com.saicone.rtag:rtag-item:VERSION'
}

jar.dependsOn (shadowJar)

shadowJar {
    // Mover al paquete de Rtag (NO IGNORES ESTO)
    relocate 'com.saicone.rtag', project.group + '.libs.rtag'
    // Excluir los classes que no utilices (opcional)
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
    // Otros módulos
    implementation("com.saicone.rtag:rtag-block:VERSION")
    implementation("com.saicone.rtag:rtag-entity:VERSION")
    implementation("com.saicone.rtag:rtag-item:VERSION")
}

tasks {
    jar {
        dependsOn(tasks.shadowJar)
    }

    shadowJar {
        // Mover al paquete de Rtag (NO IGNORES ESTO)
        relocate("com.saicone.rtag", "${project.group}.libs.rtag")
        // Excluir los classes que no utilices (opcional)
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
    <!-- Otros módulos -->
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
                <!-- Mover al paquete de Rtag (NO IGNORES ESTO) -->
                <relocation>
                    <pattern>com.saicone.rtag</pattern>
                    <shadedPattern>${project.groupId}.libs.rtag</shadedPattern>
                </relocation>
            </relocations>
            <!-- Excluir los classes que no utilices (opcional) -->
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

## Por qué Rtag

Existen otras librerias para editar NBT, ¿Por qué Rtag debería utilizarse sobre las demás?

### Velocidad como llamadas directas

Rtag abusa del uso del `static final MethodHandle` para convertir el uso de métodos reflejados como si fueran llamadas directas, así que funciona para editar NBT en operaciones no-async sin producir un mal impacto en el rendimiento de servidores grandes.

### Fácil de entender

No necesitas ser un experto en NBT, solo con métodos simples puedes establecer y obtener objetos normales de Java usando rutas al estilo de una estructura de datos de árbol.

```java
Rtag rtag = new Rtag();
rtag.set(compound, "Texto Normal", "Custom", "Tag", "Path");
String string = rtag.get(compound, "Custom", "Tag", "Path");
```

Las principales [instancias de RtagEditor](usage/editor.md) tienen métodos para hacer la edición de tags fácilmente.

```java
RtagItem tag = new RtagItem(item);
tag.setUnbreakable(true);
tag.setRepairCost(20);
int level = tag.getEnchantmentLevel("unbreaking"); // enum de Enchantment, nombre o id

RtagEntity tag = new RtagEntity(entity);
tag.setAttributeBase("generic.attackDamage", 0.5);

RtagBlock tag = new RtagBlock(block);
tag.setCustomName("§eNombre con color");
```

Además puedes editar objetos usando funciones dentro de las instancias de RtagEditor y devolver cualquier tipo de objeto.

```java
ItemStack item = ...;
// Editar objeto original
RtagItem.edit(item, tag -> {
    tag.set("Custom Text", "display", "name");
    tag.set(30, "someKey");
});
// Devolver una copia del original
ItemStack copy = RtagItem.edit(item, tag -> {
    tag.set(30, "someKey");
    return tag.loadCopy();
});
```

### Guardar objetos custom

Por defecto, Rtag utiliza la librería Gson dentro de Bukkit para (de)serializar los objetos custom, pero necesitas obtenerlos usando una conversión explícita.

Además, puede registrar tus propios (de)serializadores dentro de la instancia de Rtag que utilices para llevar a cabo una [conversión automática](feature/custom-objects.md).

```java
Rtag rtag = new Rtag();
MyObject myObject = new MyObject();

rtag.set(compound, myObject, "CustomTagPath");
MyObject sameObject = rtag.getOptional(compound, "CustomTagPath").as(MyObject.class);
```

### Instancias del TagStream

Con la [instancia ItemTagStream](feature/stream.md) puedes convertir cualquier item en los formatos Base64|Archivo|Bytes|Map|String y viceversa.
Incuyendo **compatibilidad entre versiones**! Puedes guardar cualquier item en cualquier versión y obtenerlo en cualquier versión sin problemas de compatibilidad. Materiales, encantamientos, pociones... etc, todo será convertido!

```java
ItemTagStream tag = ItemTagStream.INSTANCE;

String string = tag.toBase64(item);
ItemStack sameItem = tag.fromBase64(string)[0];
```

### Cabezas con textura

Con el [class SkullTexture](feature/skulls.md) puedes obtener cabezas con textura desde los formatos base64, url, id de la textura, nombre o uuid del

```java
// Base64
ItemStack head = SkullTexture.getTexturedHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmVkZmEyZTBmZGVhMGMwNDIzODA0Y2RiNWI2MmFkMDVhNmU5MTRjMDQ2YzRhM2I3ZTM1NWJmODEyNjkxMjVmZCJ9fQ==");
// URL
ItemStack head = SkullTexture.getTexturedHead("http://textures.minecraft.net/texture/fedfa2e0fdea0c0423804cdb5b62ad05a6e914c046c4a3b7e355bf81269125fd");
// ID de la textura
ItemStack head = SkullTexture.getTexturedHead("fedfa2e0fdea0c0423804cdb5b62ad05a6e914c046c4a3b7e355bf81269125fd");
// Nombre de jugador
ItemStack head = SkullTexture.getTexturedHead("Rubenicos");
// UUID de jugador
ItemStack head = SkullTexture.getTexturedHead("7ca003dc-175f-4f1f-b490-5651045311ad");
```

### Componentes de chat

Con el [class ChatComponent](feature/chat-component.md) puede convertir (json) strings en componentes de chat y viceversa

````java
// Convertir en componente
Object component = ChatComponent.fromJson("{\"bold\":true,\"italic\":false,\"color\":\"dark_purple\",\"text\":\"Colored text!\"}");
Object sameComponent = ChatComponent.fromString("§5§lColored text!");

// Convertir en texto
String json = ChatComponent.toJson(component);
String string = ChatComponent.toString(component);

// Compatibilidad entre texto normal y json
String json = ChatComponent.toJson("§5§lColored text!");
String string = ChatComponent.toString("{\"bold\":true,\"italic\":false,\"color\":\"dark_purple\",\"text\":\"Colored text!\"}");
```