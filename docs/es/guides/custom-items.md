---
sidebar_position: 1
title: Items custom
description: Guía básica para crear items custom con Rtag
---

Vamos a hablar de ejemplos reales con items y NBT custom.

## Data simple

Hacer items simples con data NBT.

### Vouchers

Guardar la información de un voucher dentro del tag de un item, envés de compararlo con una larga cantidad de items.

```java
private Map<String, List<String>> vouchers = new HashMap();

public ItemStack setVoucher(ItemStack item, String voucherId) {
	return RtagItem.edit(item, tag -> {
		tag.set(voucherId, "voucher");
	});
}

public String getVoucherId(ItemStack item) {
	return new RtagItem(item).get("voucher");
}

public boolean handleVoucher(Player player, ItemStack item) {
	final String id = getVoucherId(item);
	if (id == null || !vouchers.containsKey(id)) {
		return false;
	}
	for (String cmd : vouchers.get(id)) {
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName));
	}
	return true;
}
```

### Conteo de número

Contar algún número dentro del tag del item.

```java
public ItemStack addCount(ItemStack item, int amount) {
	return RtagItem.edit(item, tag -> {
		int i = tag.getOptional("myplugin", "count").or(0);
		i = i + amount;
		tag.set(i, "myplugin", "count");
	});
}

public int getCount(ItemStack item) {
	return new RtagItem(item).getOptional("myplugin", "count").or(0);
}
```

### Dueño del item

Guardar información sobre el dueño del item en un tag custom.

```java
public ItemStack setOwner(ItemStack item, OfflinePlayer player) {
	return RtagItem.edit(item, tag -> {
		tag.set(player.getName(), "owner", "name");
		tag.set(player.getUniqueId(), "owner", "uuid");
	});
}

public String getOwnerName(ItemStack item) {
	return new RtagItem(item).get("owner", "name");
}

public UUID getOwnerUuid(ItemStack item) {
	return new RtagItem(item).getOptional("owner", "uuid").asUuid();
}
```

## Interacciones

Modificar las interacciones con el item utilizando data en NBT además de manejo de eventos.

### Guardar item al morir

Ahora hagamos un tag custom como `keepItem`, que al ponerlo en `true` se guarde el item al morir.

```java
public ItemStack setKeepItem(ItemStack item, boolean value) {
	return RtagItem.edit(item, tag -> {
		tag.set(value, "keepItem");
	});
}

public boolean keepItem(ItemStack item) {
	return new RtagItem(item).getOptional("keepItem").or(false);
}
```

Y manejar los eventos `PlayerDeathEvent` y `PlayerRespawnEvent`.

```java
private final Map<String, List<ItemStack>> savedItems = new HashMap<>();

@EventHandler
public void onDeath(PlayerDeathEvent e) {
	if (e.getKeepInventory()) {
		return;
	}

	List<ItemStack> matches = new ArrayList<>();
	e.getDrops().forEach(item -> {
	    if (keepItem(item)) {
	        matches.add(item);
	    }
	});

	if (matches.isEmpty()) {
		return;
	}

	String name = e.getEntity().getName();
	if (savedItems.containsKey(name)) {
	    savedItems.get(name).addAll(matches);
	} else {
	    savedItems.put(name, matches);
	}

	e.getDrops().removeAll(matches);
}

@EventHandler
public void onRespawn(PlayerRespawnEvent e) {
	String name = e.getPlayer().getName();
	if (savedItems.containsKey(name)) {
		e.getPlayer().getInventory().addItem(savedItems.get(name).toArray(new ItemStack[0]));
		savedItems.remove(name);
	}
}
```