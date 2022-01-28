package com.saicone.rtag;

import com.saicone.rtag.entity.EntityBridge;
import com.saicone.rtag.entity.EntityTag;
import org.bukkit.entity.Entity;

/**
 * RtagItem class to edit any {@link Entity} NBT tags.
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

    private final Entity entity;

    /**
     * Constructs an RtagItem with specified Rtag parent
     * and Entity to edit.
     *
     * @param rtag Rtag parent.
     * @param entity Entity to edit.
     */
    public RtagEntity(Rtag rtag, Entity entity) {
        this(rtag, entity, asMinecraft(entity));
    }

    /**
     * Constructs an RtagItem with specified Rtag parent
     * and NMS Entity to edit.
     *
     * @param rtag   Rtag parent.
     * @param entity Original entity.
     * @param object NMS entity to edit.
     */
    public RtagEntity(Rtag rtag, Entity entity, Object object) {
        this(rtag, entity, object, getTag(object));
    }

    /**
     * Constructs an RtagItem with specified Rtag parent
     * and NMS Entity to edit.
     *
     * @param rtag   Rtag parent.
     * @param entity Original entity.
     * @param object NMS entity to edit.
     * @param tag    Entity tag to edit.
     */
    public RtagEntity(Rtag rtag, Entity entity, Object object, Object tag) {
        super(rtag, object, tag);
        this.entity = entity;
    }

    /**
     * Load changes into entity instance.
     *
     * @return The original entity.
     */
    public Entity load() {
        try {
            EntityTag.loadTag(getObject(), getTag());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return entity;
    }
}
