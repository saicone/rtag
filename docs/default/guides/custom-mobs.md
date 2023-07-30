---
sidebar_position: 3
title: Custom mobs
description: Guide to create basic custom mobs with Rtag
---

Let's talk about real examples with entities and custom NBT.

## Simple data

Make simple entities with NBT data.

### Custom effects

Here an example to give glowing effect and levitation to entities using Rtag.

```java
Entity entity = ...;

RtagEntity.edit(entity, tag -> {
	// Make it glow
	tag.set(true, "Glowing");

	// In case the entity its a mob or player
	// it can receive potion effects
	Map<String, Object> effect = Map.of(
		"Ambient", false,
		"Amplifier", false,
		"Duration", 200, // 10 seconds = 200 ticks
		"Id", (byte) 25,
		"ShowIcon", false,
		"ShowParticles", true,
		);
	tag.add(effect, "ActiveEffects");
});
```

### Attributes

Let's make a custom zombie that can pick up loot and deal more damage than zombies in Minecraft vanilla.

```java
Zombie zombie = ...;

RtagEntity.edit(zombie, tag -> {
	// Make pickup loot
	tag.set(true, "CanPickUpLoot");
	// Make zombie deal 3.5 hearts of damage
	tag.setAttributeBase("generic.attackDamage", 3.5d);
});
```