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

	// In case the entity its a mob or player it can receive potion effects
	// Note: This example only apply for Minecraft 1.20.2 or higher,
	//       so it's suggested to use Bukkit API to add potion effects
	//       (Or check MC wiki to see old tags and make your own method)
	Map<String, Object> effect = Map.of(
		"ambient", false,
		"amplifier", false,
		"duration", 200, // 10 seconds = 200 ticks
		"id", "minecraft:levitation",
		"show_icon", false,
		"show_particles", true,
		);
	tag.add(effect, "active_effects");
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