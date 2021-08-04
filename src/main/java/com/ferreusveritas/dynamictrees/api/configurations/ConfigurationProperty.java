package com.ferreusveritas.dynamictrees.api.configurations;

import com.ferreusveritas.dynamictrees.util.json.IJsonObjectGetter;
import com.ferreusveritas.dynamictrees.util.json.JsonObjectGetters;
import com.ferreusveritas.dynamictrees.util.json.ObjectFetchResult;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

import javax.annotation.Nullable;

/**
 * Class for custom configuration properties that can be deserialised from a {@link JsonObject} using {@link
 * #deserialise(JsonObject)}. Stores a property's identifier and class type, handling getting properties of type {@link
 * T} using {@link JsonObjectGetters}s.
 *
 * @param <T> The type of the property being held.
 * @author Harley O'Connor
 */
public class ConfigurationProperty<T> {

    protected final String identifier;
    protected final Class<T> type;

    protected ConfigurationProperty(String identifier, Class<T> type) {
        this.identifier = identifier;
        this.type = type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Class<T> getType() {
        return type;
    }

    /**
     * Gets an {@link ObjectFetchResult} for the property's value from the given {@link JsonObject}, or null if it was
     * not found.
     *
     * @param jsonObject The {@link JsonObject} to fetch from.
     * @return The an {@link ObjectFetchResult} for the property value, or null if it wasn't found.
     */
    @Nullable
    public ObjectFetchResult<T> deserialise(JsonObject jsonObject) {
        final JsonElement jsonElement = jsonObject.get(this.identifier);

        if (jsonElement == null) {
            return null;
        }

        final IJsonObjectGetter<T> getter = JsonObjectGetters.getObjectGetter(this.type);

        if (!getter.isValid()) {
            return ObjectFetchResult.failure("Tried to get class '" + (this.type == null ? "null" : this.type.getSimpleName()) +
                    "' for gen feature property '" + this.identifier + "', but object getter was not registered.");
        }

        return getter.get(jsonElement);
    }

    /**
     * Creates a new {@link ConfigurationProperty} from the identifier and class given.
     *
     * @param identifier The identifier for the property.
     * @param type       The {@link Class} of the value the property will store.
     * @param <T>        The value the property will store.
     * @return The new {@link ConfigurationProperty} object.
     */
    public static <T> ConfigurationProperty<T> property(String identifier, Class<T> type) {
        return new ConfigurationProperty<>(identifier, type);
    }

    public static ConfigurationProperty<String> string(String identifier) {
        return property(identifier, String.class);
    }

    public static ConfigurationProperty<Boolean> bool(String identifier) {
        return property(identifier, Boolean.class);
    }

    public static ConfigurationProperty<Integer> integer(String identifier) {
        return property(identifier, Integer.class);
    }

    public static ConfigurationProperty<Long> longProperty(String identifier) {
        return property(identifier, Long.class);
    }

    public static ConfigurationProperty<Float> doubleProperty(String identifier) {
        return property(identifier, Float.class);
    }

    public static ConfigurationProperty<Float> floatProperty(String identifier) {
        return property(identifier, Float.class);
    }

    public static ConfigurationProperty<Block> block(String identifier) {
        return property(identifier, Block.class);
    }

    public static ConfigurationProperty<Item> item(String identifier) {
        return property(identifier, Item.class);
    }


    @Override
    public String toString() {
        return "ConfigurationProperty{" +
                "identifier='" + identifier + '\'' +
                ", type=" + type +
                '}';
    }

}
