---
sidebar_position: 3
title: Mobs custom
description: Guía básica para crear mobs custom con Rtag
---

Vamos a hablar de ejemplos reales con entidades y NBT custom.

## Simple data

Hacer entidades simples con data NBT.

### Efectos custom

Aquí un ejemplo para darle un efecto de brillo y levitación a entidades utilizando Rtag.

```java
Entity entity = ...;

RtagEntity.edit(entity, tag -> {
	// Hacer que brille
	tag.set(true, "Glowing");

	// En case de que la entidad sea un mob o un jugador
	// puede recibir efectos de poción
	Map<String, Object> effect = Map.of(
		"Ambient", false,
		"Amplifier", false,
		"Duration", 200, // 10 segundos = 200 ticks
		"Id", (byte) 25,
		"ShowIcon", false,
		"ShowParticles", true,
		);
	tag.add(effect, "ActiveEffects");
});
```

### Atributos

Vamos a hacer un zombie custom que puede recojer el loot y hacer más daño que los zombies de Minecraft vanilla.

```java
Zombie zombie = ...;

RtagEntity.edit(zombie, tag -> {
	// Hacer que pueda recojer el loot
	tag.set(true, "CanPickUpLoot");
	// Hacer que el zombie infrinja 3.5 corazones de daño
	tag.setAttributeBase("generic.attackDamage", 3.5d);
});
```