package com.saicone.rtag.registry;

import com.mojang.serialization.DynamicOps;
import com.saicone.rtag.Rtag;
import com.saicone.rtag.data.ComponentType;
import com.saicone.rtag.util.EasyLookup;
import com.saicone.rtag.util.ProblemReporter;
import com.saicone.rtag.util.ServerInstance;
import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodHandle;

/**
 * Class to create I/O objects that require a certain level of registry.
 *
 * @author Rubenicos
 */
@ApiStatus.Experimental
public class IOValue {

    // Import reflected classes
    static {
        if (ServerInstance.VERSION >= 21.05f) { // 1.21.6
            try {
                EasyLookup.addNMSClass("util.ProblemReporter");
                EasyLookup.addNMSClass("world.level.storage.ValueInput");
                EasyLookup.addNMSClass("world.level.storage.ValueOutput");
                EasyLookup.addNMSClass("world.level.storage.TagValueInput");
                EasyLookup.addNMSClass("world.level.storage.TagValueOutput");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private static final MethodHandle newTagValueOutput;
    private static final MethodHandle createTagValueInput;
    private static final MethodHandle createTagValueOutput;
    private static final MethodHandle buildResult;

    static {
        MethodHandle new$TagValueOutput = null;
        MethodHandle method$TagValueInput = null;
        MethodHandle method$TagValueOutput = null;
        MethodHandle method$buildResult = null;
        if (ServerInstance.VERSION >= 21.05f) {
            try {
                // Old method names
                String TagValueInput$create = "a";
                String TagValueOutput$createWithContext = "a";
                String TagValueOutput$buildResult = "b";

                // New method names
                if (ServerInstance.Type.MOJANG_MAPPED) {
                    TagValueInput$create = "create";
                    TagValueOutput$createWithContext = "createWithContext";
                    TagValueOutput$buildResult = "buildResult";
                }

                method$TagValueInput = EasyLookup.staticMethod("TagValueInput", TagValueInput$create, "ValueInput", "ProblemReporter", "HolderLookup.Provider", "NBTTagCompound");

                new$TagValueOutput = EasyLookup.constructor("TagValueOutput", "ProblemReporter", DynamicOps.class, "NBTTagCompound");
                method$TagValueOutput = EasyLookup.staticMethod("TagValueOutput", TagValueOutput$createWithContext, "TagValueOutput", "ProblemReporter", "HolderLookup.Provider");
                method$buildResult = EasyLookup.method("TagValueOutput", TagValueOutput$buildResult, "NBTTagCompound");
            } catch (NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        newTagValueOutput = new$TagValueOutput;
        createTagValueInput = method$TagValueInput;
        createTagValueOutput = method$TagValueOutput;
        buildResult = method$buildResult;
    }

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
            return createTagValueInput.invoke(reporter, provider, compound);
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
            return createTagValueOutput.invoke(reporter, provider);
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
    public static Object createOutput(Object reporter, Object provider, Object compound) {
        try {
            return newTagValueOutput.invoke(reporter, ComponentType.createSerializationContext(ComponentType.NBT_OPS, provider), compound);
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
            return buildResult.invoke(output);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
