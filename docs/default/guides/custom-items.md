---
sidebar_position: 1
title: Custom items
description: Guide to create basic custom items with Rtag
---

Let's talk about real examples with item and custom NBT.

## Simple data

Make simple items with NBT data.

### Vouchers

Save voucher information inside item tag instead of comparing it with a large amount of items.

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

### Number count

Count some number inside item tag.

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

### Item owner

Save item owner as custom tag.

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

## Interactions

Modify item interactions using NBT data and event handling.

### Save item on death

Let's make a custom tag like `keepItem`, set to `true` to save that item on death.

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

And handle it with `PlayerDeathEvent` and `PlayerRespawnEvent`.

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