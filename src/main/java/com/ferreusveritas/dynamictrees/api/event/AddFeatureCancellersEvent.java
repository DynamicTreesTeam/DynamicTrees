package com.ferreusveritas.dynamictrees.api.event;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDatabase;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.lifecycle.IModBusEvent;

/**
 * An {@link Event} for adding {@link FeatureCanceller} objects to biomes via {@link
 * #registerFeatureCancellations(RegistryKey, FeatureCanceller...)} and namespaces to of features to cancel via {@link
 * #registerNamespaces(RegistryKey, String...)}.
 * <p>
 * Fired on the mod bus.
 *
 * @author Harley O'Connor
 * @deprecated feature cancellations will be solely defined in the biome database Json in the future
 */
@Deprecated
public final class AddFeatureCancellersEvent extends Event implements IModBusEvent {

    private final BiomeDatabase defaultDatabase;

    public AddFeatureCancellersEvent(BiomeDatabase defaultDatabase) {
        this.defaultDatabase = defaultDatabase;
    }

    private BiomePropertySelectors.FeatureCancellations getCancellations(final ResourceLocation biomeResLoc) {
        return this.defaultDatabase.getEntry(biomeResLoc).getFeatureCancellations();
    }

    public void registerFeatureCancellations(final ResourceKey<Biome> biome, final FeatureCanceller... featureCancellers) {
        final BiomePropertySelectors.FeatureCancellations featureCancellations = this.getCancellations(biome.location());

        for (final FeatureCanceller featureCanceller : featureCancellers) {
            featureCancellations.putCanceller(featureCanceller);
        }
    }

    public void registerNamespaces(final ResourceKey<Biome> biome, final String... namespaces) {
        final BiomePropertySelectors.FeatureCancellations featureCancellations = this.getCancellations(biome.location());

        for (final String namespace : namespaces) {
            featureCancellations.putNamespace(namespace);
        }
    }

}
