package com.saicone.rtag;

import com.saicone.rtag.entity.EntityObject;
import org.bukkit.entity.Entity;

/**
 * RtagEntity class to edit any {@link Entity} NBT tags.
 *
 * @author Rubenicos
 */
public class RtagEntity extends RtagEditor<Entity> {

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
        this(rtag, EntityObject.getHandle(entity));
    }

    /**
     * Constructs an RtagEntity with specified Rtag parent
     * and NMS Entity to edit.
     *
     * @param rtag   Rtag parent.
     * @param entity NMS entity to edit.
     */
    public RtagEntity(Rtag rtag, Object entity) {
        this(rtag, entity, EntityObject.save(entity));
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
            EntityObject.load(getObject(), getTag());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
