package com.saicone.rtag;

import com.saicone.rtag.entity.EntityObject;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;
import com.saicone.rtag.util.OptionalType;
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
    public Entity load() {
        try {
            EntityObject.load(getLiteralObject(), getTag());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return getTypeObject();
    }

    /**
     * Change the base value of given attribute name.<br>
     * Take in count that some attributes doesn't exist in old Minecraft versions.
     *
     * @param name  Minecraft attribute name.
     * @param value Double value.
     * @return      true if the base value was changed.
     */
    public boolean setAttributeBase(String name, double value) {
        return setAttributeValue(name, "Base", value);
    }

    /**
     * Change the attribute value of given key.<br>
     * Take in count that some attributes doesn't exist in old Minecraft versions.
     *
     * @param name  Minecraft attribute name.
     * @param key   Key that represent the value.
     * @param value Value to set.
     * @return      true if the value was changed.
     */
    public boolean setAttributeValue(String name, String key, Object value) {
        final Object attribute = getAttribute(name);
        if (attribute != null) {
            TagCompound.set(attribute, key, getRtag().newTag(value));
            return true;
        }
        return false;
    }

    /**
     * Change entity health amount.<br>
     * Take in count some entities has a fixed health value.
     *
     * @param health Health value.
     * @return       true if the health was changed.
     */
    public boolean setHealth(float health) {
        return set(health, "Health");
    }

    /**
     * Get the NBTTagCompound of the given attribute name.
     *
     * @param name Minecraft attribute name.
     * @return     the raw full attribute if was found, null otherwise.
     */
    public Object getAttribute(String name) {
        if (name == null) {
            return null;
        }
        final Object attributes = TagCompound.get(getTag(), "Attributes");
        if (attributes != null) {
            for (Object attribute : TagList.getValue(attributes)) {
                if (name.equals(TagBase.getValue(TagCompound.get(attribute, "Name")))) {
                    return attribute;
                }
            }
        }
        return null;
    }

    /**
     * Get the double base value of the given attribute name.
     *
     * @param name Minecraft attribute name.
     * @return     the base value of the attribute if was found, 0 otherwise.
     */
    public double getAttributeBase(String name) {
        final Object attribute = getAttribute(name);
        if (attribute != null) {
            return OptionalType.of(TagBase.getValue(TagCompound.get(attribute, "Base"))).or(0);
        }
        return 0;
    }

    /**
     * Get the current entity health.
     *
     * @return the health amount.
     */
    public float getHealth() {
        return getOptional("Health").asFloat(0F);
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
