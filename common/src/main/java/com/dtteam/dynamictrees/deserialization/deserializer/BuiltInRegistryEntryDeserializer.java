package com.dtteam.dynamictrees.deserialization.deserializer;

import com.dtteam.dynamictrees.deserialization.JsonDeserializers;
import com.dtteam.dynamictrees.deserialization.result.Result;
import com.google.gson.JsonElement;
import net.minecraft.core.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * Implementation of {@link JsonDeserializer} that attempts to
 * get a built-in registry entry object from a {@link JsonElement}.
 *
 * @author Harley O'Connor
 */
public final class BuiltInRegistryEntryDeserializer<T> implements JsonDeserializer<T> {

    private final Registry<T> registry;
    private final String registryDisplayName;

    @Nullable
    private final T nullValue;
    private final Predicate<T> validator;

    public BuiltInRegistryEntryDeserializer(final Registry<T> registry, final String registryDisplayName) {
        this(registry, registryDisplayName, null);
    }

    public BuiltInRegistryEntryDeserializer(final Registry<T> registry, final String registryDisplayName, @Nullable final T nullValue) {
        this.registry = registry;
        this.registryDisplayName = registryDisplayName;
        this.nullValue = nullValue;
        this.validator = value -> value != nullValue;
    }

    public Result<T, JsonElement> deserialize(JsonElement jsonElement) {
        final AtomicBoolean intentionallyNull = new AtomicBoolean();
        return JsonDeserializers.RESOURCE_LOCATION.deserialize(jsonElement).map(registryName -> {
                    // If registry name is the null value's registry name then it was intentionally the null value, so don't warn.
                    if (this.nullValue != null && Objects.equals(registryName, this.registry.getKey(this.nullValue))) {
                        intentionallyNull.set(true);
                        return this.nullValue;
                    }

                    return this.registry.getValue(registryName);
                }, value -> intentionallyNull.get() || this.validator.test(value),
                "Could not find " + this.registryDisplayName + " for registry name '{}'.");
    }

}
