package com.ferreusveritas.dynamictrees.api.events;

import com.ferreusveritas.dynamictrees.worldgen.JsonBiomeDatabasePopulator;
import com.ferreusveritas.dynamictrees.worldgen.json.IJsonBiomeApplier;
import com.ferreusveritas.dynamictrees.worldgen.json.IJsonBiomeSelector;

public class BiomeDatabaseJsonCapabilityRegistryEvent extends JsonCapabilityRegistryEvent {

    public void register(String name, IJsonBiomeSelector selector) {
        JsonBiomeDatabasePopulator.addJsonBiomeSelector(name, selector);
    }

    public void register(String name, IJsonBiomeApplier applier) {
        JsonBiomeDatabasePopulator.addJsonBiomeApplier(name, applier);
    }

}
