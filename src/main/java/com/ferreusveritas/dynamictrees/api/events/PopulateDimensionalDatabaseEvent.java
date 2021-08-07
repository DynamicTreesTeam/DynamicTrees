package com.ferreusveritas.dynamictrees.api.events;

import com.ferreusveritas.dynamictrees.worldgen.BiomeDatabase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import java.util.Map;

/**
 * An {@link Event} for populating dimensional databases programmatically. This is posted after all default populators
 * and dimensional populators from Json have been registered.
 *
 * <p>Fired on the {@link MinecraftForge#EVENT_BUS}.</p>
 *
 * @author Harley O'Connor
 */
public final class PopulateDimensionalDatabaseEvent extends Event {

    private final Map<ResourceLocation, BiomeDatabase> dimensionalMap;
    private final BiomeDatabase defaultDatabase;

    public PopulateDimensionalDatabaseEvent(final Map<ResourceLocation, BiomeDatabase> dimensionalMap, final BiomeDatabase defaultDatabase) {
        this.dimensionalMap = dimensionalMap;
        this.defaultDatabase = defaultDatabase;
    }

    public BiomeDatabase getDimensionDatabase(final ResourceLocation dimensionRegistryName) {
        return dimensionalMap.computeIfAbsent(dimensionRegistryName, k -> BiomeDatabase.copyOf(this.defaultDatabase));
    }

}
