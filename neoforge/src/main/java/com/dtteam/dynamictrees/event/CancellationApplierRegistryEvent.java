package com.dtteam.dynamictrees.event;

import com.dtteam.dynamictrees.deserialization.JsonPropertyAppliers;
import com.google.gson.JsonElement;

public final class CancellationApplierRegistryEvent<O> extends ApplierRegistryEvent<O, JsonElement> {
    public CancellationApplierRegistryEvent(JsonPropertyAppliers<O> appliers, String identifier) {
        super(appliers, identifier);
    }
}