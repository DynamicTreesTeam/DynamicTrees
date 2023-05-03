package com.ferreusveritas.dynamictrees.api.data;

import com.ferreusveritas.dynamictrees.data.provider.DTItemModelProvider;
import com.ferreusveritas.dynamictrees.item.Seed;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * @author Harley O'Connor
 */
public class SeedItemModelGenerator implements Generator<DTItemModelProvider, Species> {

    public static final DependencyKey<Seed> SEED = new DependencyKey<>("seed");

    @Override
    public void generate(DTItemModelProvider provider, Species input, Dependencies dependencies) {
        final Seed seed = dependencies.get(SEED);
        provider.withExistingParent(String.valueOf(ForgeRegistries.ITEMS.getKey(seed)), seed.getSpecies().getSeedParentLocation())
                .texture("layer0", provider.item(ForgeRegistries.ITEMS.getKey(seed)));
    }

    @Override
    public Dependencies gatherDependencies(Species input) {
        return new Dependencies()
                .append(SEED, input.getSeed());
    }

}
