package com.saicone.rtag.util.reflect;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.saicone.rtag.util.MC;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.UnaryOperator;

@ApiStatus.Internal
public class Remapper implements UnaryOperator<Reference> {

    public static final Remapper MOJANG_TO_SPIGOT = mojangToSpigot(MC.version());

    @NotNull
    public static Remapper mojangToSpigot(@NotNull MC version) {
        final Remapper remapper = new Remapper();
        if (version.isNewerThanOrEquals(MC.first())) {
            remapper.add(new Mapping("net.minecraft.core.DefaultedRegistry", "net.minecraft.core.RegistryBlocks") {{

            }});
            remapper.add(new Mapping("net.minecraft.core.BlockPos", "net.minecraft.core.BlockPosition") {{

            }});
            remapper.add(new Mapping("net.minecraft.nbt.ByteTag", "net.minecraft.nbt.NBTTagByte") {{

            }});
            remapper.add(new Mapping("net.minecraft.nbt.ByteArrayTag", "net.minecraft.nbt.NBTTagByteArray") {{

            }});
            remapper.add(new Mapping("net.minecraft.nbt.CompoundTag", "net.minecraft.nbt.NBTTagCompound") {{
                field(Map.class, "tags").to("map");
                method("net.minecraft.nbt.Tag", "copy").to("clone");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.DoubleTag", "net.minecraft.nbt.NBTTagDouble") {{

            }});
            remapper.add(new Mapping("net.minecraft.nbt.EndTag", "net.minecraft.nbt.NBTTagEnd") {{

            }});
            remapper.add(new Mapping("net.minecraft.nbt.FloatTag", "net.minecraft.nbt.NBTTagFloat") {{

            }});
            remapper.add(new Mapping("net.minecraft.nbt.IntTag", "net.minecraft.nbt.NBTTagInt") {{

            }});
            remapper.add(new Mapping("net.minecraft.nbt.IntArrayTag", "net.minecraft.nbt.NBTTagIntArray") {{

            }});
            remapper.add(new Mapping("net.minecraft.nbt.ListTag", "net.minecraft.nbt.NBTTagList") {{
                method("net.minecraft.nbt.Tag", "copy").to("clone");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.LongTag", "net.minecraft.nbt.NBTTagLong") {{

            }});
            remapper.add(new Mapping("net.minecraft.nbt.NbtAccounter", "net.minecraft.nbt.NBTReadLimiter") {{
                field("net.minecraft.nbt.NbtAccounter", "UNLIMITED").to("a");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.NbtIo", "net.minecraft.nbt.NBTCompressedStreamTools") {{
                method("net.minecraft.nbt.Tag", "readUnnamedTag", DataInput.class, int.class, "net.minecraft.nbt.NbtAccounter").to("a");
                method(void.class, "writeUnnamedTag", "net.minecraft.nbt.Tag", DataOutput.class).to("a");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.ShortTag", "net.minecraft.nbt.NBTTagShort") {{

            }});
            remapper.add(new Mapping("net.minecraft.nbt.StringTag", "net.minecraft.nbt.NBTTagString") {{

            }});
            remapper.add(new Mapping("net.minecraft.nbt.Tag", "net.minecraft.nbt.NBTBase") {{
                method(byte.class, "getId").to("getTypeId");
                method("net.minecraft.nbt.Tag", "copy").to("clone");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.TagParser", "net.minecraft.nbt.MojangsonParser") {{
                method("net.minecraft.nbt.CompoundTag", "parseTag", String.class).to("parse");
            }});
            remapper.add(new Mapping("net.minecraft.network.chat.Component", "net.minecraft.network.chat.IChatBaseComponent"));
            remapper.add(new Mapping("net.minecraft.network.chat.Component$Serializer", "net.minecraft.network.chat.IChatBaseComponent$ChatSerializer") {{
                method("net.minecraft.network.chat.Component", "fromJson", String.class).to("a");
                method(String.class, "toJson", "net.minecraft.network.chat.Component").to("a");
            }});
            remapper.add(new Mapping("net.minecraft.network.chat.MutableComponent", "net.minecraft.network.chat.IChatMutableComponent"));
            remapper.add(new Mapping("net.minecraft.world.item.ItemStack") {{
                method("net.minecraft.world.item.ItemStack", "of", "net.minecraft.nbt.CompoundTag").to("createStack");
                method("net.minecraft.world.item.ItemStack", "copy").to("cloneItemStack");
                method(void.class, "load", "net.minecraft.nbt.CompoundTag").to("c");
            }});
            remapper.add(new Mapping("net.minecraft.server.level.ServerLevel", "net.minecraft.server.level.WorldServer") {{

            }});
            remapper.add(new Mapping("net.minecraft.world.entity.Entity") {{
                method(String.class, "getEncodeId").to("ag");
                method(boolean.class, "saveAsPassenger", "net.minecraft.nbt.CompoundTag").to("c");
                method(void.class, "saveWithoutId", "net.minecraft.nbt.CompoundTag").to("e");
                method(void.class, "load", "net.minecraft.nbt.CompoundTag").to("f");
            }});
            remapper.add(new Mapping("net.minecraft.world.level.Level", "net.minecraft.world.level.World") {{
                method("net.minecraft.world.level.block.state.BlockState", "getBlockState", "net.minecraft.core.BlockPos").to("getType");
                method("net.minecraft.world.level.block.entity.BlockEntity", "getBlockEntity", "net.minecraft.core.BlockPos").to("getTileEntity");
            }});
            remapper.add(new Mapping("net.minecraft.world.level.block.entity.BlockEntity", "net.minecraft.world.level.block.entity.TileEntity") {{
                method("net.minecraft.world.level.Level", "getLevel").to("getWorld");
                method("net.minecraft.core.BlockPos", "getBlockPos").to("getPosition");
                method(void.class, "saveWithoutMetadata", "net.minecraft.nbt.CompoundTag").to("b");
                method(void.class, "load", "net.minecraft.nbt.CompoundTag").to("a");
            }});
            remapper.add(new Mapping("net.minecraft.world.level.block.state.BlockState", "net.minecraft.world.level.block.state.IBlockData") {{

            }});
        }
        if (version.isNewerThanOrEquals(MC.V_1_9)) {
            remapper.add(new Mapping("net.minecraft.world.entity.Entity") {{
                method(String.class, "getEncodeId").to("as");
                method("net.minecraft.nbt.CompoundTag", "saveWithoutId", "net.minecraft.nbt.CompoundTag").to("e");
            }});
            remapper.add(new Mapping("net.minecraft.world.level.block.entity.BlockEntity", "net.minecraft.world.level.block.entity.TileEntity") {{
                method("net.minecraft.nbt.CompoundTag", "saveWithoutMetadata", "net.minecraft.nbt.CompoundTag").to("save");
            }});
        }
        if (version.isNewerThanOrEquals(MC.V_1_10)) {
            remapper.add(new Mapping("net.minecraft.nbt.CompoundTag") {{
                method("net.minecraft.nbt.CompoundTag", "copy").to("g");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.ListTag") {{
                method("net.minecraft.nbt.ListTag", "copy").to("d");
            }});
            remapper.add(new Mapping("net.minecraft.world.entity.Entity") {{
                method(String.class, "getEncodeId").to("at");
            }});
        }
        if (version.isNewerThanOrEquals(MC.V_1_11)) {
            remapper.add(new Mapping("net.minecraft.world.item.ItemStack") {{
                method(void.class, "load", "net.minecraft.nbt.CompoundTag").to("load");
            }});
        }
        if (version.isNewerThanOrEquals(MC.V_1_12)) {
            remapper.add(new Mapping("net.minecraft.nbt.LongArrayTag", "net.minecraft.nbt.NBTTagLongArray") {{
                field(long[].class, "data").to("b");
            }});
            remapper.add(new Mapping("net.minecraft.world.entity.Entity") {{
                method(String.class, "getEncodeId").to("getSaveID");
                method("net.minecraft.nbt.CompoundTag", "saveWithoutId", "net.minecraft.nbt.CompoundTag").to("save");
            }});
            remapper.add(new Mapping("net.minecraft.world.level.block.entity.BlockEntity", "net.minecraft.world.level.block.entity.TileEntity") {{
                method(void.class, "load", "net.minecraft.nbt.CompoundTag").to("load");
            }});
        }
        if (version.isNewerThanOrEquals(MC.V_1_13)) {
            remapper.add(new Mapping("net.minecraft.nbt.NbtOps", "net.minecraft.nbt.DynamicOpsNBT") {{
                field("net.minecraft.nbt.NbtOps", "INSTANCE").to("a");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.CompoundTag") {{
                method("net.minecraft.nbt.CompoundTag", "copy").to("clone");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.LongArrayTag") {{
                field(long[].class, "data").to("f");
            }});
            remapper.add(new Mapping("net.minecraft.world.item.ItemStack") {{
                method("net.minecraft.world.item.ItemStack", "of", "net.minecraft.nbt.CompoundTag").to("a");
            }});
        }
        if (version.isNewerThanOrEquals(MC.V_1_13_2)) {
            remapper.add(new Mapping("net.minecraft.core.Registry", "net.minecraft.core.IRegistry") {{

            }});
        }
        if (version.isNewerThanOrEquals(MC.V_1_14)) {
            remapper.add(new Mapping("net.minecraft.nbt.ListTag") {{
                method("net.minecraft.nbt.ListTag", "copy").to("clone");
            }});
        }
        if (version.isNewerThanOrEquals(MC.V_1_15)) {
            remapper.add(new Mapping("net.minecraft.nbt.ByteTag") {{
                method("net.minecraft.nbt.ByteTag", "valueOf", byte.class).to("a");
                method("net.minecraft.nbt.ByteTag", "valueOf", boolean.class).to("a");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.DoubleTag") {{
                method("net.minecraft.nbt.DoubleTag", "valueOf", double.class).to("a");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.EndTag") {{
                field("net.minecraft.nbt.EndTag", "INSTANCE").to("b");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.FloatTag") {{
                method("net.minecraft.nbt.FloatTag", "valueOf", float.class).to("a");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.IntTag") {{
                method("net.minecraft.nbt.IntTag", "valueOf", int.class).to("a");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.LongTag") {{
                method("net.minecraft.nbt.LongTag", "valueOf", long.class).to("a");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.LongArrayTag") {{
                field(long[].class, "data").to("b");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.ShortTag") {{
                method("net.minecraft.nbt.ShortTag", "valueOf", short.class).to("a");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.StringTag") {{
                method("net.minecraft.nbt.StringTag", "valueOf", String.class).to("a");
            }});
        }
        if (version.isNewerThanOrEquals(MC.V_1_16)) {
            remapper.add(new Mapping("net.minecraft.core.RegistryAccess", "net.minecraft.core.IRegistryCustom") {{

            }});
            remapper.add(new Mapping("net.minecraft.network.chat.Component$Serializer") {{
                method("net.minecraft.network.chat.MutableComponent", "fromJson", String.class).to("a");
            }});
            remapper.add(new Mapping("net.minecraft.world.entity.Entity") {{
                method(boolean.class, "saveAsPassenger", "net.minecraft.nbt.CompoundTag").to("a_");
                method(void.class, "load", "net.minecraft.nbt.CompoundTag").to("load");
            }});
            remapper.add(new Mapping("net.minecraft.world.item.ItemStack") {{
                field(Codec.class, "CODEC").to("a");
            }});
            remapper.add(new Mapping("net.minecraft.world.level.LevelReader", "net.minecraft.world.level.IWorldReader") {{

            }});
        }
        if (version.isNewerThanOrEquals(MC.V_1_17)) {
            remapper.add(new Mapping("net.minecraft.nbt.ByteTag") {{
                field(byte.class, "data").to("x");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.ByteArrayTag") {{
                field(byte[].class, "data").to("c");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.CompoundTag") {{
                field(Map.class, "tags").to("x");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.DoubleTag") {{
                field(double.class, "data").to("w");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.FloatTag") {{
                field(float.class, "data").to("w");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.IntTag") {{
                field(int.class, "data").to("c");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.IntArrayTag") {{
                field(int[].class, "data").to("c");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.ListTag") {{
                field(byte.class, "type").to("w");
                field(List.class, "list").to("c");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.LongTag") {{
                field(long.class, "data").to("c");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.LongArrayTag") {{
                field(long[].class, "data").to("c");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.ShortTag") {{
                field(short.class, "data").to("c");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.StringTag") {{
                field(String.class, "data").to("A");
            }});
            remapper.add(new Mapping("net.minecraft.world.entity.Entity") {{
                method(boolean.class, "saveAsPassenger", "net.minecraft.nbt.CompoundTag").to("d");
            }});
        }
        if (version.isNewerThanOrEquals(MC.V_1_18)) {
            remapper.add(new Mapping("net.minecraft.nbt.CompoundTag") {{
                method("net.minecraft.nbt.CompoundTag", "copy").to("g");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.ListTag") {{
                method("net.minecraft.nbt.ListTag", "copy").to("d");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.Tag") {{
                method(byte.class, "getId").to("a");
                method("net.minecraft.nbt.Tag", "copy").to("c");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.TagParser") {{
                method("net.minecraft.nbt.CompoundTag", "parseTag", String.class).to("a");
            }});
            remapper.add(new Mapping("net.minecraft.world.entity.Entity") {{
                method(String.class, "getEncodeId").to("bk");
                method("net.minecraft.nbt.CompoundTag", "saveWithoutId", "net.minecraft.nbt.CompoundTag").to("f");
                method(void.class, "load", "net.minecraft.nbt.CompoundTag").to("g");
            }});
            remapper.add(new Mapping("net.minecraft.world.item.ItemStack") {{
                method(boolean.class, "isEmpty").to("b");
                method("net.minecraft.world.item.ItemStack", "copy").to("m");
                method("net.minecraft.nbt.CompoundTag", "save", "net.minecraft.nbt.CompoundTag").to("b");
                method("net.minecraft.nbt.CompoundTag", "getTag").to("s");
                method(void.class, "setTag", "net.minecraft.nbt.CompoundTag").to("c");
                method(int.class, "getCount").to("I");
                method(void.class, "setCount", int.class).to("e");
            }});
            remapper.add(new Mapping("net.minecraft.world.level.Level") {{
                method("net.minecraft.world.level.block.state.BlockState", "getBlockState", "net.minecraft.core.BlockPos").to("a_");
                method("net.minecraft.world.level.block.entity.BlockEntity", "getBlockEntity", "net.minecraft.core.BlockPos").to("c_");
            }});
            remapper.add(new Mapping("net.minecraft.world.level.block.entity.BlockEntity") {{
                method("net.minecraft.world.level.Level", "getLevel").to("k");
                method("net.minecraft.core.BlockPos", "getBlockPos").to("p");
                method("net.minecraft.nbt.CompoundTag", "saveWithoutMetadata").to("m");
                method(void.class, "load", "net.minecraft.nbt.CompoundTag").to("a");
            }});
        }
        if (version.isNewerThanOrEquals(MC.V_1_18_2)) {
            remapper.add(new Mapping("net.minecraft.world.item.ItemStack") {{
                method("net.minecraft.world.item.ItemStack", "copy").to("n");
                method("net.minecraft.nbt.CompoundTag", "getTag").to("t");
                method(int.class, "getCount").to("J");
            }});
        }
        if (version.isNewerThanOrEquals(MC.V_1_19)) {
            remapper.add(new Mapping("net.minecraft.world.item.ItemStack") {{
                method("net.minecraft.world.item.ItemStack", "copy").to("o");
                method("net.minecraft.nbt.CompoundTag", "getTag").to("u");
                method(int.class, "getCount").to("K");
            }});
            remapper.add(new Mapping("net.minecraft.world.entity.Entity") {{
                method(String.class, "getEncodeId").to("bn");
            }});
        }
        if (version.isNewerThanOrEquals(MC.V_1_19_3)) {
            remapper.add(new Mapping("net.minecraft.core.DefaultedRegistry") {{
                method(Object.class, "get", "net.minecraft.resources.ResourceLocation").to("a");
            }});
            remapper.add(new Mapping("net.minecraft.core.HolderLookup$Provider", "net.minecraft.core.HolderLookup$b") {{

            }});
            remapper.add(new Mapping("net.minecraft.core.registries.BuiltInRegistries") {{
                field("net.minecraft.core.DefaultedRegistry", "ITEM").to("i");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.Tag") {{
                method(byte.class, "getId").to("b");
                method("net.minecraft.nbt.Tag", "copy").to("d");
            }});
            remapper.add(new Mapping("net.minecraft.resources.ResourceLocation", "net.minecraft.resources.MinecraftKey") {{
                method(String.class, "getPath").to("a");
            }});
            remapper.add(new Mapping("net.minecraft.world.entity.Entity") {{
                method(String.class, "getEncodeId").to("bq");
            }});
            remapper.add(new Mapping("net.minecraft.world.item.ItemStack") {{
                method(void.class, "setCount", int.class).to("f");
            }});
        }
        if (version.isNewerThanOrEquals(MC.V_1_19_4)) {
            remapper.add(new Mapping("net.minecraft.nbt.CompoundTag") {{
                method("net.minecraft.nbt.CompoundTag", "copy").to("h");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.ListTag") {{
                method("net.minecraft.nbt.ListTag", "copy").to("e");
            }});
            remapper.add(new Mapping("net.minecraft.world.entity.Entity") {{
                method(String.class, "getEncodeId").to("bp");
            }});
            remapper.add(new Mapping("net.minecraft.world.level.block.entity.BlockEntity") {{
                method("net.minecraft.nbt.CompoundTag", "saveWithoutMetadata").to("o");
            }});
        }
        if (version.isNewerThanOrEquals(MC.V_1_20)) {
            remapper.add(new Mapping("net.minecraft.world.entity.Entity") {{
                method(String.class, "getEncodeId").to("br");
            }});
            remapper.add(new Mapping("net.minecraft.world.item.ItemStack") {{
                method("net.minecraft.world.item.ItemStack", "copy").to("p");
                method("net.minecraft.nbt.CompoundTag", "getTag").to("v");
                method(int.class, "getCount").to("L");
            }});
        }
        if (version.isNewerThanOrEquals(MC.V_1_20_2)) {
            remapper.add(new Mapping("net.minecraft.core.registries.BuiltInRegistries") {{
                field("net.minecraft.core.DefaultedRegistry", "ITEM").to("i");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.NbtAccounter") {{
                method("net.minecraft.nbt.NbtAccounter", "unlimitedHeap").to("a");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.NbtIo") {{
                method("net.minecraft.nbt.Tag", "readUnnamedTag", DataInput.class, "net.minecraft.nbt.NbtAccounter").to("c");
                method(void.class, "writeUnnamedTag", "net.minecraft.nbt.Tag", DataOutput.class).to("b");
            }});
            remapper.add(new Mapping("net.minecraft.world.entity.Entity") {{
                method(String.class, "getEncodeId").to("bu");
            }});
        }
        if (version.isNewerThanOrEquals(MC.V_1_20_3)) {
            remapper.add(new Mapping("net.minecraft.world.entity.Entity") {{
                method(String.class, "getEncodeId").to("bw");
            }});
        }
        if (version.isNewerThanOrEquals(MC.V_1_20_4)) {
            remapper.add(new Mapping("net.minecraft.core.registries.BuiltInRegistries") {{
                field("net.minecraft.core.DefaultedRegistry", "ITEM").to("h");
            }});
            remapper.add(new Mapping("net.minecraft.world.level.block.entity.BlockEntity") {{
                method("net.minecraft.world.level.Level", "getLevel").to("i");
                method("net.minecraft.core.BlockPos", "getBlockPos").to("aB_");
            }});
        }
        if (version.isNewerThanOrEquals(MC.V_1_20_5)) {
            remapper.add(new Mapping("net.minecraft.core.MappedRegistry", "net.minecraft.core.RegistryMaterials") {{
                field(Map.class, "byLocation").to("f");
            }});
            remapper.add(new Mapping("net.minecraft.core.Holder") {{
                method(Object.class, "value").to("a");
            }});
            remapper.add(new Mapping("net.minecraft.core.HolderLookup$Provider", "net.minecraft.core.HolderLookup$a") {{

            }});
            remapper.add(new Mapping("net.minecraft.core.component.DataComponentHolder") {{
                method("net.minecraft.core.component.DataComponentMap", "getComponents").to("a");
                method(Object.class, "get", "net.minecraft.core.component.DataComponentType").to("a");
                method(boolean.class, "has", "net.minecraft.core.component.DataComponentType").to("b");
            }});
            remapper.add(new Mapping("net.minecraft.core.component.DataComponentMap") {{
                field("net.minecraft.core.component.DataComponentMap", "EMPTY").to("a");
                method(Object.class, "get", "net.minecraft.core.component.DataComponentType").to("a");
                method(Set.class, "keySet").to("b");
                method("net.minecraft.core.component.DataComponentMap$Builder", "builder").to("a");
            }});
            remapper.add(new Mapping("net.minecraft.core.component.DataComponentMap$Builder", "net.minecraft.core.component.DataComponentMap$a") {{
                field(Reference2ObjectMap.class, "map").to("a");
                method("net.minecraft.core.component.DataComponentMap", "build").to("a");
            }});
            remapper.add(new Mapping("net.minecraft.core.component.DataComponentMap$Builder$SimpleMap", "net.minecraft.core.component.DataComponentMap$a$a") {{
                field(Reference2ObjectMap.class, "map").to("c");
            }});
            remapper.add(new Mapping("net.minecraft.core.component.DataComponentPatch") {{
                field("net.minecraft.core.component.DataComponentPatch", "EMPTY").to("a");
                field(Reference2ObjectMap.class, "map").to("d");
                method("net.minecraft.core.component.DataComponentPatch$Builder", "builder").to("a");
            }});
            remapper.add(new Mapping("net.minecraft.core.component.DataComponentPatch$Builder", "net.minecraft.core.component.DataComponentPatch$a") {{
                field(Reference2ObjectMap.class, "map").to("a");
                method("net.minecraft.core.component.DataComponentPatch", "build").to("a");
            }});
            remapper.add(new Mapping("net.minecraft.core.component.DataComponentType") {{
                field(Codec.class, "codec").to("b");
            }});
            remapper.add(new Mapping("net.minecraft.core.component.PatchedDataComponentMap") {{
                field(Reference2ObjectMap.class, "patch").to("d");
                method(Object.class, "set", "net.minecraft.core.component.DataComponentType", Object.class).to("b");
                method(Object.class, "remove", "net.minecraft.core.component.DataComponentType").to("d");
            }});
            remapper.add(new Mapping("net.minecraft.core.registries.BuiltInRegistries") {{
                field("net.minecraft.core.Registry", "DATA_COMPONENT_TYPE").to("as");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.CompoundTag") {{
                method("net.minecraft.nbt.CompoundTag", "copy").to("i");
            }});
            remapper.add(new Mapping("net.minecraft.network.chat.ComponentSerialization") {{
                field(Codec.class, "CODEC").to("a");
            }});
            remapper.add(new Mapping("net.minecraft.resources.RegistryOps") {{
                method("net.minecraft.resources.RegistryOps", "create", DynamicOps.class, "net.minecraft.core.HolderLookup$Provider").to("a");
            }});
            remapper.add(new Mapping("net.minecraft.world.entity.Entity") {{
                method(String.class, "getEncodeId").to("bC");
                method("net.minecraft.core.RegistryAccess", "registryAccess").to("dR");
            }});
            remapper.add(new Mapping("net.minecraft.world.item.ItemStack") {{
                field(Codec.class, "CODEC").to("b");
                field("net.minecraft.world.item.Item", "item").to("q");
                method(boolean.class, "isEmpty").to("e");
                method("net.minecraft.world.item.ItemStack", "copy").to("s");
                method(void.class, "setCount", int.class).to("e");
                method(void.class, "applyComponentsAndValidate", "net.minecraft.core.component.DataComponentPatch").to("a");
            }});
            remapper.add(new Mapping("net.minecraft.world.item.component.CustomData") {{
                field("net.minecraft.nbt.CompoundTag", "tag").to("e");
            }});
            remapper.add(new Mapping("net.minecraft.world.item.component.ResolvableProfile") {{
                field(GameProfile.class, "gameProfile").to("f");
            }});
            remapper.add(new Mapping("net.minecraft.world.level.LevelReader") {{
                method("net.minecraft.core.RegistryAccess", "registryAccess").to("H_");
            }});
            remapper.add(new Mapping("net.minecraft.world.level.block.entity.BlockEntity") {{
                method("net.minecraft.world.level.Level", "getLevel").to("i");
                method("net.minecraft.core.BlockPos", "getBlockPos").to("ay_");
                method("net.minecraft.nbt.CompoundTag", "saveWithoutMetadata", "net.minecraft.core.HolderLookup$Provider").to("d");
                method(void.class, "loadWithComponents", "net.minecraft.nbt.CompoundTag", "net.minecraft.core.HolderLookup$Provider").to("c");
            }});
        }
        if (version.isNewerThanOrEquals(MC.V_1_20_6)) {
            remapper.add(new Mapping("net.minecraft.world.item.component.CustomData") {{
                field("net.minecraft.nbt.CompoundTag", "tag").to("f");
            }});
        }
        if (version.isNewerThanOrEquals(MC.V_1_21)) {
            remapper.add(new Mapping("net.minecraft.core.registries.BuiltInRegistries") {{
                field("net.minecraft.core.DefaultedRegistry", "ITEM").to("g");
                field("net.minecraft.core.Registry", "DATA_COMPONENT_TYPE").to("aq");
            }});
            remapper.add(new Mapping("net.minecraft.resources.ResourceLocation") {{
                method("net.minecraft.resources.ResourceLocation", "parse", String.class).to("a");
            }});
            remapper.add(new Mapping("net.minecraft.world.entity.Entity") {{
                method(String.class, "getEncodeId").to("bD");
                method("net.minecraft.core.RegistryAccess", "registryAccess").to("dQ");
            }});
            remapper.add(new Mapping("net.minecraft.world.item.ItemStack") {{
                method(int.class, "getCount").to("H");
                method(void.class, "setCount", int.class).to("e");
            }});
            remapper.add(new Mapping("net.minecraft.world.level.block.entity.BlockEntity") {{
                method("net.minecraft.core.BlockPos", "getBlockPos").to("aD_");
            }});
        }
        if (version.isNewerThanOrEquals(MC.V_1_21_2)) {
            remapper.add(new Mapping("net.minecraft.core.DefaultedRegistry") {{
                method(Object.class, "getValue", "net.minecraft.resources.ResourceLocation").to("a");
            }});
            remapper.add(new Mapping("net.minecraft.core.MappedRegistry") {{
                field(Map.class, "byLocation").to("e");
            }});
            remapper.add(new Mapping("net.minecraft.core.registries.BuiltInRegistries") {{
                field("net.minecraft.core.Registry", "DATA_COMPONENT_TYPE").to("ao");
            }});
            remapper.add(new Mapping("net.minecraft.world.entity.Entity") {{
                method(String.class, "getEncodeId").to("bK");
                method("net.minecraft.core.RegistryAccess", "registryAccess").to("dY");
            }});
            remapper.add(new Mapping("net.minecraft.world.item.ItemStack") {{
                field(Codec.class, "CODEC").to("a");
                field("net.minecraft.world.item.Item", "item").to("o");
                method(boolean.class, "isEmpty").to("f");
                method("net.minecraft.world.item.ItemStack", "copy").to("v");
                method(int.class, "getCount").to("L");
            }});
            remapper.add(new Mapping("net.minecraft.world.level.LevelReader") {{
                method("net.minecraft.core.RegistryAccess", "registryAccess").to("K_");
            }});
            remapper.add(new Mapping("net.minecraft.world.level.block.entity.BlockEntity") {{
                method("net.minecraft.core.BlockPos", "getBlockPos").to("aB_");
            }});
        }
        if (version.isNewerThanOrEquals(MC.V_1_21_4)) {
            remapper.add(new Mapping("net.minecraft.core.component.PatchedDataComponentMap") {{
                method(Object.class, "remove", "net.minecraft.core.component.DataComponentType").to("e");
            }});
            remapper.add(new Mapping("net.minecraft.world.entity.Entity") {{
                method("net.minecraft.core.RegistryAccess", "registryAccess").to("dX");
            }});
            remapper.add(new Mapping("net.minecraft.world.item.ItemStack") {{
                field("net.minecraft.world.item.Item", "item").to("p");
                method(int.class, "getCount").to("M");
            }});
            remapper.add(new Mapping("net.minecraft.world.item.component.CustomData") {{
                field("net.minecraft.nbt.CompoundTag", "tag").to("g");
            }});
            remapper.add(new Mapping("net.minecraft.world.level.block.entity.BlockEntity") {{
                method("net.minecraft.core.BlockPos", "getBlockPos").to("aA_");
            }});
        }
        if (version.isNewerThanOrEquals(MC.V_1_21_5)) {
            remapper.add(new Mapping("net.minecraft.core.component.DataComponentGetter") {{
                method(Object.class, "get", "net.minecraft.core.component.DataComponentType").to("a");
            }});
            remapper.add(new Mapping("net.minecraft.core.component.DataComponentHolder") {{
                method(boolean.class, "has", "net.minecraft.core.component.DataComponentType").to("c");
            }});
            remapper.add(new Mapping("net.minecraft.core.registries.BuiltInRegistries") {{
                field("net.minecraft.core.Registry", "DATA_COMPONENT_TYPE").to("am");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.ByteTag") {{
                field(byte.class, "value").to("v");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.CompoundTag") {{
                method("net.minecraft.nbt.CompoundTag", "copy").to("l");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.DoubleTag") {{
                field(double.class, "value").to("c");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.FloatTag") {{
                field(float.class, "value").to("c");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.IntTag") {{
                field(int.class, "value").to("b");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.ListTag") {{
                field(List.class, "list").to("v");
                method("net.minecraft.nbt.ListTag", "copy").to("g");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.LongTag") {{
                field(long.class, "value").to("b");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.ShortTag") {{
                field(short.class, "value").to("b");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.StringTag") {{
                field(String.class, "value").to("b");
            }});
            remapper.add(new Mapping("net.minecraft.nbt.TagParser") {{
                method("net.minecraft.nbt.CompoundTag", "parseCompoundFully", String.class).to("a");
            }});
            remapper.add(new Mapping("net.minecraft.world.entity.Entity") {{
                method(String.class, "getEncodeId").to("bI");
                method(void.class, "load", "net.minecraft.nbt.CompoundTag").to("i");
            }});
            remapper.add(new Mapping("net.minecraft.world.item.ItemStack") {{
                field(Codec.class, "CODEC").to("b");
                field("net.minecraft.world.item.Item", "item").to("s");
            }});
            remapper.add(new Mapping("net.minecraft.world.level.LevelReader") {{
                method("net.minecraft.core.RegistryAccess", "registryAccess").to("J_");
            }});
            remapper.add(new Mapping("net.minecraft.world.level.block.entity.BlockEntity") {{
                method("net.minecraft.core.BlockPos", "getBlockPos").to("ax_");
            }});
        }
        if (version.isNewerThanOrEquals(MC.V_1_21_6)) {
            remapper.add(new Mapping("net.minecraft.util.ProblemReporter") {{
                field("net.minecraft.util.ProblemReporter", "DISCARDING").to("a");
            }});
            remapper.add(new Mapping("net.minecraft.world.entity.Entity") {{
                method(String.class, "getEncodeId").to("bN");
                method(void.class, "load", "net.minecraft.world.level.storage.ValueInput").to("e");
                method("net.minecraft.core.RegistryAccess", "registryAccess").to("eb");
            }});
            remapper.add(new Mapping("net.minecraft.world.level.storage.TagValueInput") {{
                method("net.minecraft.world.level.storage.ValueInput", "create", "net.minecraft.util.ProblemReporter", "net.minecraft.core.HolderLookup$Provider", "net.minecraft.nbt.CompoundTag").to("a");
            }});
            remapper.add(new Mapping("net.minecraft.world.level.storage.TagValueOutput") {{
                method("net.minecraft.world.level.storage.TagValueOutput", "createWithContext", "net.minecraft.util.ProblemReporter", "net.minecraft.core.HolderLookup$Provider").to("a");
                method("net.minecraft.nbt.CompoundTag", "buildResult").to("b");
            }});
            remapper.add(new Mapping("net.minecraft.world.level.LevelReader") {{
                method("net.minecraft.core.RegistryAccess", "registryAccess").to("K_");
            }});
            remapper.add(new Mapping("net.minecraft.world.level.block.entity.BlockEntity") {{
                method("net.minecraft.core.BlockPos", "getBlockPos").to("aA_");
                method(void.class, "saveWithoutMetadata", "net.minecraft.world.level.storage.ValueOutput").to("e");
                method(void.class, "loadWithComponents", "net.minecraft.world.level.storage.ValueInput").to("b");
            }});
        }
        if (version.isNewerThanOrEquals(MC.V_1_21_9)) {
            remapper.add(new Mapping("net.minecraft.core.registries.BuiltInRegistries") {{
                field("net.minecraft.core.DefaultedRegistry", "ITEM").to("h");
                field("net.minecraft.core.Registry", "DATA_COMPONENT_TYPE").to("an");
            }});
            remapper.add(new Mapping("net.minecraft.world.entity.Entity") {{
                method(String.class, "getEncodeId").to("bW");
                method(void.class, "load", "net.minecraft.world.level.storage.ValueInput").to("d");
                method("net.minecraft.core.RegistryAccess", "registryAccess").to("ej");
            }});
            remapper.add(new Mapping("net.minecraft.world.item.component.CustomData") {{
                field("net.minecraft.nbt.CompoundTag", "tag").to("e");
            }});
            remapper.add(new Mapping("net.minecraft.world.item.component.ResolvableProfile") {{
                method("net.minecraft.world.item.component.ResolvableProfile", "createResolved", GameProfile.class).to("a");
                method("net.minecraft.world.item.component.ResolvableProfile", "createUnresolved", String.class).to("a");
                method("net.minecraft.world.item.component.ResolvableProfile", "createUnresolved", UUID.class).to("a");
                method(GameProfile.class, "partialProfile").to("b");
            }});
            remapper.add(new Mapping("net.minecraft.world.level.LevelReader") {{
                method("net.minecraft.core.RegistryAccess", "registryAccess").to("L_");
            }});
            remapper.add(new Mapping("net.minecraft.world.level.block.entity.BlockEntity") {{
                method("net.minecraft.world.level.Level", "getLevel").to("j");
                method("net.minecraft.core.BlockPos", "getBlockPos").to("aD_");
            }});
        }
        if (version.isNewerThanOrEquals(MC.V_1_21_11)) {
            remapper.add(new Mapping("net.minecraft.core.DefaultedRegistry") {{
                method(Object.class, "getValue", "net.minecraft.resources.Identifier").to("a");
            }});
            remapper.add(new Mapping("net.minecraft.core.registries.BuiltInRegistries") {{
                field("net.minecraft.core.Registry", "DATA_COMPONENT_TYPE").to("am");
            }});
            remapper.add(new Mapping("net.minecraft.resources.Identifier", "net.minecraft.resources.MinecraftKey") {{
                method(String.class, "getPath").to("a");
                method("net.minecraft.resources.Identifier", "parse", String.class).to("a");
            }});
            remapper.add(new Mapping("net.minecraft.world.entity.Entity") {{
                method(String.class, "getEncodeId").to("ca");
                method("net.minecraft.core.RegistryAccess", "registryAccess").to("eo");
            }});
            remapper.add(new Mapping("net.minecraft.world.item.ItemStack") {{
                method(int.class, "getCount").to("N");
            }});
            remapper.add(new Mapping("net.minecraft.world.level.LevelReader") {{
                method("net.minecraft.core.RegistryAccess", "registryAccess").to("J_");
            }});
        }
        return remapper;
    }

    private final Map<Reference, Reference> mappings = new HashMap<>();

    public void add(@NotNull Mapping mapping) {
        if (mapping.getTo() != null) {
            mappings.put(Reference.clazz(mapping.getFrom()), Reference.clazz(mapping.getTo()));
        }
        mappings.putAll(mapping.getEntries());
    }

    @Override
    public Reference apply(Reference reference) {
        return mappings.get(reference);
    }

    public static class Mapping {

        private final Object from;
        private final Object to;
        private final Map<Reference, Reference> entries = new HashMap<>();

        public Mapping(@NotNull Object from) {
            this(from, null);
        }

        public Mapping(@NotNull Object from, @Nullable Object to) {
            this.from = from;
            this.to = to;
        }

        @NotNull
        public Object getFrom() {
            return from;
        }

        @Nullable
        public Object getTo() {
            return to;
        }

        @NotNull
        public Map<Reference, Reference> getEntries() {
            return entries;
        }

        @NotNull
        public ReferenceBuilder constructor(@NotNull Object... parameters) {
            return new ReferenceBuilder(from, null, null, (Object[]) parameters) {
                @Override
                protected void build(@NotNull Reference reference) {
                    entries.put(Reference.constructor(from, parameters), reference);
                }
            };
        }

        @NotNull
        public ReferenceBuilder method(@NotNull Object type, @NotNull String name, @NotNull Object... parameters) {
            return new ReferenceBuilder(from, type, name, (Object[]) parameters) {
                @Override
                protected void build(@NotNull Reference reference) {
                    entries.put(Reference.method(from, type, name, parameters), reference);
                }
            };
        }

        @NotNull
        public ReferenceBuilder field(@NotNull Object type, @NotNull String name) {
            return new ReferenceBuilder(from, type, name, (Object[]) null) {
                @Override
                protected void build(@NotNull Reference reference) {
                    entries.put(Reference.field(from, type, name), reference);
                }
            };
        }
    }

    public abstract static class ReferenceBuilder {

        private final Object parent;
        private final Object type;
        private final String name;
        private final Object[] parameters;

        protected ReferenceBuilder(@NotNull Object parent, @Nullable Object type, @Nullable String name, @Nullable Object[] parameters) {
            this.parent = parent;
            this.type = type;
            this.name = name;
            this.parameters = parameters;
        }

        public void toType(@NotNull Object type) {
            build(new Reference(parent, type, name, parameters));
        }

        public void toConstructor(@NotNull Object... parameters) {
            build(new Reference(parent, type, name, parameters));
        }

        public void to(@NotNull String name) {
            build(new Reference(parent, type, name, parameters));
        }

        public void to(@NotNull Object type, @NotNull String name) {
            build(new Reference(parent, type, name, parameters));
        }

        public void to(@NotNull Object type, @NotNull String name, @NotNull Object... parameters) {
            build(new Reference(parent, type, name, parameters));
        }

        protected abstract void build(@NotNull Reference reference);
    }
}
