package com.saicone.rtag;

import com.saicone.rtag.entity.EntityBridge;
import com.saicone.rtag.entity.EntityTag;
import org.bukkit.entity.Entity;

/**
 * RtagEntity class to edit any {@link Entity} NBT tags.
 *
 * @author Rubenicos
 */
public class RtagEntity extends RtagEditor<Entity> {

    private static Object asMinecraft(Entity entity) {
        try {
            return EntityBridge.asMinecraft(entity);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    private static Object getTag(Object entity) {
        try {
            return EntityTag.saveTag(entity);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Constructs an RtagEntity with Entity to edit.
     *
     * @param entity Entity to edit.
     */
    public RtagEntity(Entity entity) {
        this(Rtag.INSTANCE, entity);
    }

    /**
     * Constructs an RtagEntity with specified Rtag parent
     * and Entity to edit.
     *
     * @param rtag Rtag parent.
     * @param entity Entity to edit.
     */
    public RtagEntity(Rtag rtag, Entity entity) {
        this(rtag, asMinecraft(entity));
    }

    /**
     * Constructs an RtagEntity with specified Rtag parent
     * and NMS Entity to edit.
     *
     * @param rtag   Rtag parent.
     * @param entity NMS entity to edit.
     */
    public RtagEntity(Rtag rtag, Object entity) {
        this(rtag, entity, getTag(entity));
    }

    /**
     * Constructs an RtagEntity with specified Rtag parent
     * and NMS Entity to edit.
     *
     * @param rtag   Rtag parent.
     * @param entity NMS entity to edit.
     * @param tag    Entity tag to edit.
     */
    public RtagEntity(Rtag rtag, Object entity, Object tag) {
        super(rtag, entity, tag);
    }

    /**
     * Load changes into entity instance.
     */
    public void load() {
        try {
            EntityTag.loadTag(getObject(), getTag());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
