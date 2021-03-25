package com.ferreusveritas.dynamictrees.util.json;

import com.google.gson.JsonElement;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Implementation of {@link IJsonObjectGetter} that attempts to get a {@link ForgeRegistryEntry}
 * object from a {@link JsonElement}.
 *
 * @author Harley O'Connor
 */
public final class ForgeRegistryEntryGetter<T extends ForgeRegistryEntry<T>> implements IJsonObjectGetter<T> {

    private final IForgeRegistry<T> registry;
    private final String registryDisplayName;

    public ForgeRegistryEntryGetter(final IForgeRegistry<T> registry, final String registryDisplayName) {
        this.registry = registry;
        this.registryDisplayName = registryDisplayName;
    }

    @Override
    public ObjectFetchResult<T> get(JsonElement jsonElement) {
        return JsonObjectGetters.RESOURCE_LOCATION_GETTER.get(jsonElement).map(this.registry::getValue,
                "Could not find " + this.registryDisplayName + " for registry name '{previous_value}'.");
    }

}
