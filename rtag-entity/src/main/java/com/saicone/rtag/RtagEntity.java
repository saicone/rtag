package com.saicone.rtag;

import com.saicone.rtag.entity.EntityObject;
import org.bukkit.entity.Entity;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * RtagEntity class to edit any {@link Entity} NBT tags.
 *
 * @author Rubenicos
 */
public class RtagEntity extends RtagEditor<Entity> {

    /**
     * Create an RtagEntity using Entity.
     *
     * @param entity Entity to load changes.
     * @return       new RtagEntity instance.
     */
    public static RtagEntity of(Entity entity) {
        return new RtagEntity(entity);
    }

    /**
     * Create an RtagEntity using Entity and specified Rtag parent.
     *
     * @param rtag   Rtag parent.
     * @param entity Entity to load changes.
     * @return       new RtagEntity instance.
     */
    public static RtagEntity of(Rtag rtag, Entity entity) {
        return new RtagEntity(rtag, entity);
    }

    /**
     * Constructs an RtagEntity with Entity to edit.
     *
     * @param entity Entity to load changes.
     */
    public RtagEntity(Entity entity) {
        super(Rtag.INSTANCE, entity);
    }

    /**
     * Constructs an RtagEntity with specified Rtag parent
     * and Entity to edit.
     *
     * @param rtag   Rtag parent.
     * @param entity Entity to load changes.
     */
    public RtagEntity(Rtag rtag, Entity entity) {
        super(rtag, entity);
    }

    /**
     * Constructs an RtagEntity with specified Rtag parent
     * and NMS Entity to edit.
     *
     * @param rtag     Rtag parent.
     * @param entity   Entity to load changes.
     * @param mcEntity Minecraft server entity to edit.
     */
    public RtagEntity(Rtag rtag, Entity entity, Object mcEntity) {
        super(rtag, entity, mcEntity);
    }

    /**
     * Constructs an RtagEntity with specified Rtag parent
     * and NMS Entity to edit.
     *
     * @param rtag     Rtag parent.
     * @param entity   Entity to load changes.
     * @param mcEntity Minecraft server entity to edit.
     * @param tag      Entity tag to edit.
     */
    public RtagEntity(Rtag rtag, Entity entity, Object mcEntity, Object tag) {
        super(rtag, entity, mcEntity, tag);
    }

    /**
     * Get current entity instance.
     *
     * @return A Bukkit Entity.
     */
    public Entity getEntity() {
        return getTypeObject();
    }

    @Override
    public Object getLiteralObject(Entity entity) {
        return EntityObject.getHandle(entity);
    }

    @Override
    public Object getTag(Object entity) {
        return EntityObject.save(entity);
    }

    /**
     * Load changes into entity instance.
     */
    public void load() {
        try {
            EntityObject.load(getLiteralObject(), getTag());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Edit the current RtagEntity instance and return itself.
     *
     * @param consumer Function to apply.
     * @return         The current RtagEntity instance.
     */
    public RtagEntity edit(Consumer<RtagEntity> consumer) {
        consumer.accept(this);
        return this;
    }

    /**
     * Edit the provided entity using a RtagEntity instance by consumer.
     *
     * @param entity   Entity to edit.
     * @param consumer Consumer to accept.
     * @return         The provided item.
     * @param <T>      Entity type.
     */
    public static <T extends Entity> T edit(T entity, Consumer<RtagEntity> consumer) {
        return edit(Rtag.INSTANCE, entity, consumer);
    }

    /**
     * Edit a RtagEntity instance using the provided entity by function that return any type.<br>
     * Take in count that you should use {@link RtagEditor#load()} if you want to load the changes.
     *
     * @param entity   Entity to edit.
     * @param function Function to apply.
     * @return         The object provided by the function.
     * @param <T>      Entity type.
     * @param <R>      The required return type.
     */
    public static <T extends Entity, R> R edit(T entity, Function<RtagEntity, R> function) {
        return edit(Rtag.INSTANCE, entity, function);
    }

    /**
     * Edit the provided entity using a RtagEntity instance by consumer with defined Rtag parent.
     *
     * @param rtag     Rtag parent.
     * @param entity   Entity to edit.
     * @param consumer Consumer to accept.
     * @return         The provided item.
     * @param <T>      Entity type.
     */
    public static <T extends Entity> T edit(Rtag rtag, T entity, Consumer<RtagEntity> consumer) {
        new RtagEntity(rtag, entity).edit(consumer).load();
        return entity;
    }

    /**
     * Edit a RtagEntity instance using the provided entity by function that return any type with defined Rtag parent.<br>
     * Take in count that you should use {@link RtagEditor#load()} if you want to load the changes.
     *
     * @param rtag     Rtag parent.
     * @param entity   Entity to edit.
     * @param function Function to apply.
     * @return         The object provided by the function.
     * @param <T>      Entity type.
     * @param <R>      The required return type.
     */
    public static <T extends Entity, R> R edit(Rtag rtag, T entity, Function<RtagEntity, R> function) {
        return function.apply(new RtagEntity(rtag, entity));
    }
}
