package com.ferreusveritas.dynamictrees.api.worldgen;

import com.ferreusveritas.dynamictrees.worldgen.json.IJsonBiomeApplier;
import com.ferreusveritas.dynamictrees.worldgen.json.IJsonBiomeSelector;
import net.minecraftforge.eventbus.api.Event;

/**
 * @author Harley O'Connor
 */
public abstract class JsonCapabilityRegistryEvent extends Event {

    public abstract void register(String name, IJsonBiomeSelector selector);

    public abstract void register(String name, IJsonBiomeApplier applier);

}
