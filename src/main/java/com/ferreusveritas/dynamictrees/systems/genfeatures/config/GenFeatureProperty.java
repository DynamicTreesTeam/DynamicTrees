package com.ferreusveritas.dynamictrees.systems.genfeatures.config;

import com.ferreusveritas.dynamictrees.util.JsonHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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
     * Gets the property from the given {@link JsonObject}, or null if it was not found.
     * Note that the base implementation of this method only handles {@link JsonPrimitive} objects,
     * for handling other types of value a sub-class will be needed with an implementation of this.
     *
     * @param jsonObject The {@link JsonObject} to fetch from.
     * @return The {@link GenFeaturePropertyValue}.
     */
    @Nullable
    public GenFeaturePropertyValue<T> getFromJsonObject (JsonObject jsonObject) {
        JsonElement jsonElement = jsonObject.get(this.identifier);

        if (jsonElement == null || !jsonElement.isJsonPrimitive())
            return null;

        final T value = JsonHelper.getFromPrimitive(jsonElement.getAsJsonPrimitive(), this.type);

        if (value == null)
            return null;

        return new GenFeaturePropertyValue<>(value);
    }

    /**
     * Creates a new {@link GenFeatureProperty} from the identifier and class given.
     *
     * @deprecated For custom class types, sub-classes should be used with an implementation of
     * <tt>getFromJsonObject</tt> so that the value can be obtained from Json. This method will
     * be made private soon.
     * @param identifier The identifier for the property.
     * @param type The {@link Class} of the value the property will store.
     * @param <T> The value the property will store.
     * @return The new {@link GenFeatureProperty} object.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public static <T> GenFeatureProperty<T> createProperty(String identifier, Class<?> type) {
        return new GenFeatureProperty<>(identifier, (Class<T>) type);
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
        return createProperty(identifier, Double.class);
    }

    public static GenFeatureProperty<Float> createFloatProperty(String identifier) {
        return createProperty(identifier, Float.class);
    }

}
