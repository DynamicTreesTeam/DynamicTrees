package com.ferreusveritas.dynamictrees.util.json;

import com.google.gson.JsonElement;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Implementation of {@link IJsonObjectGetter} that attempts to get a {@link ForgeRegistryEntry}
 * object from a {@link JsonElement}.
 *
 * @author Harley O'Connor
 */
public final class ForgeRegistryEntryGetter<T extends ForgeRegistryEntry<T>> implements IJsonObjectGetter<T> {

    private final IForgeRegistry<T> registry;
    private final String registryDisplayName;
    private final Predicate<T> validator;

    public ForgeRegistryEntryGetter(final IForgeRegistry<T> registry, final String registryDisplayName) {
        this(registry, registryDisplayName, Objects::nonNull);
    }

    public ForgeRegistryEntryGetter(final IForgeRegistry<T> registry, final String registryDisplayName, final Predicate<T> validator) {
        this.registry = registry;
        this.registryDisplayName = registryDisplayName;
        this.validator = validator;
    }

    @Override
    public ObjectFetchResult<T> get(JsonElement jsonElement) {
        return JsonObjectGetters.RESOURCE_LOCATION_GETTER.get(jsonElement).map(this.registry::getValue,
                this.validator, "Could not find " + this.registryDisplayName + " for registry name '{previous_value}'.");
    }

}
