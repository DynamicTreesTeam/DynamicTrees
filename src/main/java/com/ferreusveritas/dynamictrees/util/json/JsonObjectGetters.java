package com.ferreusveritas.dynamictrees.util.json;

import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * @author Harley O'Connor
 */
public final class JsonObjectGetters {

    private static final Set<JsonObjectGetterHolder<?>> OBJECT_GETTERS = Sets.newHashSet();

    /**
     * Gets the {@link IJsonObjectGetter} for the given class type.
     *
     * @param objectClass The {@link Class} of the object to get.
     * @param <T> The type of the object.
     * @return The {@link IJsonObjectGetter} for the class.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> IJsonObjectGetter<T> getObjectGetter (final Class<T> objectClass) {
        return OBJECT_GETTERS.stream().filter(jsonObjectGetterHolder -> jsonObjectGetterHolder.objectClass.equals(objectClass))
                .findFirst().map(jsonObjectGetterHolder -> (IJsonObjectGetter<T>) jsonObjectGetterHolder.objectGetter).orElse(null);
    }

    /**
     * Registers an {@link IJsonObjectGetter} to the registry.
     *
     * @param objectClass The {@link Class} of the object that will be obtained.
     * @param objectGetter The {@link IJsonObjectGetter} to register.
     * @param <T> The type of the object getter.
     * @return The {@link IJsonObjectGetter} given.
     */
    public static <T> IJsonObjectGetter<T> register(final Class<T> objectClass, final IJsonObjectGetter<T> objectGetter) {
        OBJECT_GETTERS.add(new JsonObjectGetterHolder<>(objectClass, objectGetter));
        return objectGetter;
    }

    /**
     * Holds an {@link IJsonObjectGetter} for the relevant class.
     *
     * @param <T> The type of the object getter.
     */
    private static final class JsonObjectGetterHolder<T> {
        private final Class<T> objectClass;
        private final IJsonObjectGetter<T> objectGetter;

        public JsonObjectGetterHolder(final Class<T> objectClass, final IJsonObjectGetter<T> objectGetter) {
            this.objectClass = objectClass;
            this.objectGetter = objectGetter;
        }
    }

    public static final IJsonObjectGetter<String> STRING_GETTER = register(String.class, jsonElement -> {
        if (!jsonElement.isJsonPrimitive() || !jsonElement.getAsJsonPrimitive().isString())
            return ObjectFetchResult.failure("Json element was not a string.");

        return ObjectFetchResult.success(jsonElement.getAsString());
    });

    public static final IJsonObjectGetter<Boolean> BOOLEAN_GETTER = register(Boolean.class, jsonElement -> {
        if (!jsonElement.isJsonPrimitive() || !jsonElement.getAsJsonPrimitive().isBoolean())
            return ObjectFetchResult.failure("Json element was not a boolean.");

        return ObjectFetchResult.success(jsonElement.getAsBoolean());
    });

    public static final IJsonObjectGetter<Number> NUMBER_GETTER = register(Number.class, jsonElement -> {
        if (!jsonElement.isJsonPrimitive() || !jsonElement.getAsJsonPrimitive().isNumber())
            return ObjectFetchResult.failure("Json element was not a number.");

        return ObjectFetchResult.success(jsonElement.getAsNumber());
    });

    public static final IJsonObjectGetter<Integer> INTEGER_GETTER = register(Integer.class, jsonElement -> {
        final ObjectFetchResult<Number> numberFetch = NUMBER_GETTER.get(jsonElement);

        if (!numberFetch.wasSuccessful())
            return ObjectFetchResult.failureFromOther(numberFetch);

        return ObjectFetchResult.success(numberFetch.getValue().intValue());
    });

    public static final IJsonObjectGetter<Double> DOUBLE_GETTER = register(Double.class, jsonElement -> {
        final ObjectFetchResult<Number> numberFetch = NUMBER_GETTER.get(jsonElement);

        if (!numberFetch.wasSuccessful())
            return ObjectFetchResult.failureFromOther(numberFetch);

        return ObjectFetchResult.success(numberFetch.getValue().doubleValue());
    });

    public static final IJsonObjectGetter<Float> FLOAT_GETTER = register(Float.class, jsonElement -> {
        final ObjectFetchResult<Number> numberFetch = NUMBER_GETTER.get(jsonElement);

        if (!numberFetch.wasSuccessful())
            return ObjectFetchResult.failureFromOther(numberFetch);

        return ObjectFetchResult.success(numberFetch.getValue().floatValue());
    });

    public static final IJsonObjectGetter<JsonObject> JSON_OBJECT_GETTER = register(JsonObject.class, jsonElement -> {
        if (!jsonElement.isJsonObject())
            return ObjectFetchResult.failure("Json element was not a json object.");

        return ObjectFetchResult.success(jsonElement.getAsJsonObject());
    });

    public static final IJsonObjectGetter<ResourceLocation> RESOURCE_LOCATION_GETTER = register(ResourceLocation.class, jsonElement -> {
        final ObjectFetchResult<String> stringFetchResult = STRING_GETTER.get(jsonElement);

        if (!stringFetchResult.wasSuccessful())
            return ObjectFetchResult.failureFromOther(stringFetchResult);

        try {
            return ObjectFetchResult.success(new ResourceLocation(stringFetchResult.getValue()));
        } catch (ResourceLocationException e) {
            return ObjectFetchResult.failure("Json element was not a valid resource location: " + e.getMessage());
        }
    });

    public static final IJsonObjectGetter<GrowthLogicKit> GROWTH_LOGIC_KIT_GETTER = register(GrowthLogicKit.class, new ForgeRegistryEntryGetter<>(GrowthLogicKit.REGISTRY, "growth logic kit"));
    public static final IJsonObjectGetter<GenFeature> GEN_FEATURE_GETTER = register(GenFeature.class, new ForgeRegistryEntryGetter<>(GenFeature.REGISTRY, "gen feature"));
    public static final IJsonObjectGetter<TreeFamily> TREE_FAMILY_GETTER = register(TreeFamily.class, new ForgeRegistryEntryGetter<>(TreeFamily.REGISTRY, "tree family"));
    public static final IJsonObjectGetter<Species> SPECIES_GETTER = register(Species.class, new ForgeRegistryEntryGetter<>(Species.REGISTRY, "species"));

    // TODO: Make leaves properties a forge registry.
    public static final IJsonObjectGetter<ILeavesProperties> LEAVES_PROPERTIES_GETTER = register(ILeavesProperties.class, jsonElement -> {
        final ObjectFetchResult<String> stringFetchResult = STRING_GETTER.get(jsonElement);

        if (!stringFetchResult.wasSuccessful())
            return ObjectFetchResult.failureFromOther(stringFetchResult);

        final ILeavesProperties leavesProperties = DTRegistries.leaves.get(stringFetchResult.getValue());
        return ObjectFetchResult.successOrFailure(leavesProperties,
                "Json element referenced unregistered leaves properties '" + stringFetchResult.getValue() + "'.");
    });

}
