package com.ferreusveritas.dynamictrees.systems.genfeatures.config;

import com.ferreusveritas.dynamictrees.util.json.IJsonObjectGetter;
import com.ferreusveritas.dynamictrees.util.json.JsonObjectGetters;
import com.ferreusveritas.dynamictrees.util.json.ObjectFetchResult;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.block.Block;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nullable;

/**
 * Base Property class for {@link ConfiguredGenFeature} objects. Stores a property's identifier
 * and class type. The base implementation should only be used by {@link JsonPrimitive} types, and
 * in the future will be limited to this only (sub-classes will need to be created for other objects).
 *
 * @author Harley O'Connor
 */
public class GenFeatureProperty<T> {

    protected final String identifier;
    protected final Class<T> type;

    protected GenFeatureProperty(String identifier, Class<T> type) {
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
     * Gets an {@link ObjectFetchResult} for the property's value from the given {@link JsonObject},
     * or null if it was not found.
     *
     * @param jsonObject The {@link JsonObject} to fetch from.
     * @return The an {@link ObjectFetchResult} for the property value, or null if it wasn't found.
     */
    @Nullable
    public ObjectFetchResult<T> getValueFromJsonObject(JsonObject jsonObject) {
        final JsonElement jsonElement = jsonObject.get(this.identifier);

        final IJsonObjectGetter<T> getter = JsonObjectGetters.getObjectGetter(this.type);

        if (jsonElement == null)
            return null;

        if (!getter.isValid()) {
            LogManager.getLogger().warn("Tried to get class {} for gen feature property {}, but object getter was not registered.", this.type == null ? "null" : this.type.getSimpleName(), this.identifier);
            return null;
        }

        return getter.get(jsonElement);
    }

    /**
     * Creates a new {@link GenFeatureProperty} from the identifier and class given.
     *
     * @param identifier The identifier for the property.
     * @param type The {@link Class} of the value the property will store.
     * @param <T> The value the property will store.
     * @return The new {@link GenFeatureProperty} object.
     */
    public static <T> GenFeatureProperty<T> createProperty(String identifier, Class<T> type) {
        return new GenFeatureProperty<>(identifier, type);
    }

    public static GenFeatureProperty<String> createStringProperty(String identifier) {
        return createProperty(identifier, String.class);
    }

    public static GenFeatureProperty<Boolean> createBooleanProperty(String identifier) {
        return createProperty(identifier, Boolean.class);
    }

    public static GenFeatureProperty<Integer> createIntegerProperty(String identifier) {
        return createProperty(identifier, Integer.class);
    }

    public static GenFeatureProperty<Long> createLongProperty(String identifier) {
        return createProperty(identifier, Long.class);
    }

    public static GenFeatureProperty<Float> createDoubleProperty(String identifier) {
        return createProperty(identifier, Float.class);
    }

    public static GenFeatureProperty<Float> createFloatProperty(String identifier) {
        return createProperty(identifier, Float.class);
    }

    public static GenFeatureProperty<Block> createBlockProperty(String identifier) {
        return createProperty(identifier, Block.class);
    }

    @Override
    public String toString() {
        return "GenFeatureProperty{" +
                "identifier='" + identifier + '\'' +
                ", type=" + type +
                '}';
    }

}
