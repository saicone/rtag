package com.saicone.rtag.registry;

import com.mojang.serialization.DynamicOps;
import com.saicone.rtag.Rtag;
import com.saicone.rtag.data.ComponentType;
import com.saicone.rtag.util.MC;
import com.saicone.rtag.util.ProblemReporter;
import com.saicone.rtag.util.reflect.Lookup;
import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Modifier;

/**
 * Class to create I/O objects that require a certain level of registry.
 *
 * @author Rubenicos
 */
@ApiStatus.Experimental
public class IOValue {

    // lock
    static {
        Lookup.SERVER.require(MC.version().isNewerThanOrEquals(MC.V_1_21_6));
    }

    // import
    private static final Lookup.AClass<?> HolderLookup$Provider = Lookup.SERVER.importClass("net.minecraft.core.HolderLookup$Provider");
    private static final Lookup.AClass<?> CompoundTag = Lookup.SERVER.importClass("net.minecraft.nbt.CompoundTag");
    private static final Lookup.AClass<?> MC_ProblemReporter = Lookup.SERVER.importClass("net.minecraft.util.ProblemReporter");
    private static final Lookup.AClass<?> TagValueInput = Lookup.SERVER.importClass("net.minecraft.world.level.storage.TagValueInput");
    private static final Lookup.AClass<?> TagValueOutput = Lookup.SERVER.importClass("net.minecraft.world.level.storage.TagValueOutput");
    private static final Lookup.AClass<?> ValueInput = Lookup.SERVER.importClass("net.minecraft.world.level.storage.ValueInput");
    private static final Lookup.AClass<?> ValueOutput = Lookup.SERVER.importClass("net.minecraft.world.level.storage.ValueOutput");

    // declare
    private static final MethodHandle TagValueOutput$new = TagValueOutput.constructor(MC_ProblemReporter, DynamicOps.class, CompoundTag).handle();
    private static final MethodHandle TagValueInput_create = TagValueInput.method(Modifier.STATIC, ValueInput, "create", MC_ProblemReporter, HolderLookup$Provider, CompoundTag).handle();
    private static final MethodHandle TagValueOutput_createWithContext = TagValueOutput.method(Modifier.STATIC, TagValueOutput, "createWithContext", MC_ProblemReporter, HolderLookup$Provider).handle();
    private static final MethodHandle TagValueOutput_buildResult = TagValueOutput.method(CompoundTag, "buildResult").handle();

    /**
     * Create a tag value input using provided objects.<br>
     * This object is used to keep tract of Minecraft serialization process.
     *
     * @param compound the compound that will be inserted.
     * @return         a newly generated tag value input.
     */
    public static Object createInput(Object compound) {
        return createInput(ProblemReporter.DISCARDING, Rtag.getMinecraftRegistry(), compound);
    }

    /**
     * Create a tag value input using provided objects.<br>
     * This object is used to keep track of Minecraft serialization process.
     *
     * @param reporter the reporter to append errors.
     * @param provider the access provider.
     * @param compound the compound that will be inserted.
     * @return         a newly generated tag value input.
     */
    public static Object createInput(Object reporter, Object provider, Object compound) {
        try {
            return TagValueInput_create.invoke(reporter, provider, compound);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Create a tag value output-<br>
     * This object is used to keep track of Minecraft serialization process.
     *
     * @return a newly generated tag value output.
     */
    public static Object createOutput() {
        return createOutput(ProblemReporter.DISCARDING, Rtag.getMinecraftRegistry());
    }

    /**
     * Create a tag value output using provided objects.<br>
     * This object is used to keep track of Minecraft serialization process.
     *
     * @param reporter the reporter to append errors.
     * @param provider the access provider.
     * @return         a newly generated tag value output.
     */
    public static Object createOutput(Object reporter, Object provider) {
        try {
            return TagValueOutput_createWithContext.invoke(reporter, provider);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Create a tag value output using provided objects.<br>
     * This object is used to keep track of Minecraft serialization process.
     *
     * @param reporter the reporter to append errors.
     * @param provider the access provider.
     * @param compound the tag compound to insert information.
     * @return         a newly generated tag value output.
     */
    public static Object createOutputWrapping(Object reporter, Object provider, Object compound) {
        try {
            return TagValueOutput$new.invoke(reporter, ComponentType.createSerializationContext(ComponentType.NBT_OPS, provider), compound);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Get the result of a tag value output.
     *
     * @param output the tag value output.
     * @return       a tag compound.
     */
    public static Object result(Object output) {
        try {
            return TagValueOutput_buildResult.invoke(output);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
