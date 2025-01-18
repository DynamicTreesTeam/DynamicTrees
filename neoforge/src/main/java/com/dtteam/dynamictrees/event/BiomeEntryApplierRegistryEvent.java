package com.dtteam.dynamictrees.event;

import com.dtteam.dynamictrees.deserialization.JsonPropertyAppliers;
import com.google.gson.JsonElement;

public final class BiomeEntryApplierRegistryEvent<O> extends ApplierRegistryEvent<O, JsonElement> {
    public BiomeEntryApplierRegistryEvent(JsonPropertyAppliers<O> appliers, String identifier) {
        super(appliers, identifier);
    }
}