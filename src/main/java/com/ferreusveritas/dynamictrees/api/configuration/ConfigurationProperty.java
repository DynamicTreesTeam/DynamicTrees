package com.ferreusveritas.dynamictrees.api.configuration;

import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialiser;
import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialisers;
import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;

/**
 * Class for custom configuration properties that can be deserialised from a {@link JsonObject} using {@link
 * #deserialise(JsonObject)}. Stores a property's identifier and class type, handling getting properties of type {@link
 * T} using {@link JsonDeserialiser}s.
 *
 * @param <T> The type of the property being held.
 * @author Harley O'Connor
 */
public class ConfigurationProperty<T> {

    public static final ConfigurationProperty<Object> NULL =
            new ConfigurationProperty<>("null", Object.class);

    private final String key;
    private final Class<T> type;

    protected ConfigurationProperty(String key, Class<T> type) {
        this.key = key;
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public Class<T> getType() {
        return type;
    }

    /**
     * Gets a {@link Result} for the property's value from the given {@link JsonObject}, or null if it
     * was not found.
     *
     * @param jsonObject The {@link JsonObject} to fetch from.
     * @return The an {@link Result} for the property value, or null if it wasn't found.
     */
    public Optional<Result<T, JsonElement>> deserialise(JsonObject jsonObject) {
        final JsonElement jsonElement = jsonObject.get(this.key);

        if (jsonElement == null) {
            return Optional.empty();
        }

        final JsonDeserialiser<T> getter = JsonDeserialisers.getOrThrow(this.type, "Tried to get class " +
                "\"" + this.type.getName() + "\" for gen feature property \"" + this.key + "\", but " +
                "deserialiser was not registered.");
        return Optional.ofNullable(getter.deserialise(jsonElement));
    }

    /**
     * Creates a new {@link ConfigurationProperty} from the identifier and class given.
     *
     * @param identifier The identifier for the property.
     * @param type       The {@link Class} of the value the property will store.
     * @param <T>        The value the property will store.
     * @return The new {@link ConfigurationProperty} object.
     */
    public static <T> ConfigurationProperty<T> property(String identifier, @Nonnull Class<T> type) {
        Objects.requireNonNull(type);
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConfigurationProperty<?> that = (ConfigurationProperty<?>) o;
        return Objects.equals(key, that.key) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, type);
    }

    @Override
    public String toString() {
        return "ConfigurationProperty{" +
                "identifier='" + key + '\'' +
                ", type=" + type +
                '}';
    }

}
