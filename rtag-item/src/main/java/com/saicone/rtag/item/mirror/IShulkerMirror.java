package com.saicone.rtag.item.mirror;

import com.saicone.rtag.item.ItemTagStream;
import com.saicone.rtag.util.ServerInstance;
import org.jetbrains.annotations.ApiStatus;

/**
 * @deprecated Use {@link IContainerMirror} instead
 */
@ApiStatus.ScheduledForRemoval(inVersion = "1.6.0")
@Deprecated
public class IShulkerMirror extends IContainerMirror {

    /**
     * @deprecated Use {@link IContainerMirror#IContainerMirror(ItemTagStream, boolean)} instead
     *
     * @param stream ItemTagStream instance.
     */
    @Deprecated
    public IShulkerMirror(ItemTagStream stream) {
        super(stream, ServerInstance.VERSION >= 20.04f);
    }

    /**
     * Deprecated.
     *
     * @deprecated Use {@link IContainerMirror} instead
     *
     * @param tag  ItemStack tag.
     * @param from Version specified in compound.
     * @param to   Version to convert.
     */
    @Deprecated
    public void processTag(Object tag, float from, float to) {
        processComponents(tag, from, to);
    }
}
