package com.ferreusveritas.dynamictrees.api.events;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDatabase;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

/**
 * An {@link Event} for adding {@link FeatureCanceller} objects to biomes via
 * {@link #registerFeatureCancellations(RegistryKey, FeatureCanceller...)} and namespaces to
 * of features to cancel via {@link #registerNamespaces(RegistryKey, String...)}.
 *
 * <p>Fired on the {@link MinecraftForge#EVENT_BUS}.</p>
 *
 * @author Harley O'Connor
 */
public final class AddFeatureCancellersEvent extends Event {

    private final BiomeDatabase defaultDatabase;

    public AddFeatureCancellersEvent(BiomeDatabase defaultDatabase) {
        this.defaultDatabase = defaultDatabase;
    }

    private BiomePropertySelectors.FeatureCancellations getCancellations (final ResourceLocation biomeResLoc) {
        return this.defaultDatabase.getEntry(biomeResLoc).getFeatureCancellations();
    }

    public void registerFeatureCancellations (final RegistryKey<Biome> biome, final FeatureCanceller... featureCancellers) {
        final BiomePropertySelectors.FeatureCancellations featureCancellations = this.getCancellations(biome.getLocation());

        for (final FeatureCanceller featureCanceller : featureCancellers)
            featureCancellations.putCanceller(featureCanceller);
    }

    public void registerNamespaces (final RegistryKey<Biome> biome, final String... namespaces) {
        final BiomePropertySelectors.FeatureCancellations featureCancellations = this.getCancellations(biome.getLocation());

        for (final String namespace : namespaces)
            featureCancellations.putNamespace(namespace);
    }

}
