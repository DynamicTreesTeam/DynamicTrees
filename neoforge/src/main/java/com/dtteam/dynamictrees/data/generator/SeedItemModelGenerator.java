package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.data.Generator;
import com.dtteam.dynamictrees.item.Seed;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;

/**
 * @author Harley O'Connor
 */
public class SeedItemModelGenerator implements Generator<ItemModelGenerators, Species> {

    public static final DependencyKey<Seed> SEED_ITEM = new DependencyKey<>("seed");

    @Override
    public void generate(ItemModelGenerators generators, Species input, Dependencies dependencies) {
        generators.generateFlatItem(dependencies.get(SEED_ITEM), ModelTemplates.FLAT_ITEM);
//        if (prov instanceof DTItemModelProvider provider){
//            final Seed seed = dependencies.get(SEED);
//            provider.withExistingParent(String.valueOf(BuiltInRegistries.ITEM.getKey(seed)), seed.getSpecies().getSeedParentModelLocation())
//                    .texture("layer0", seed.getSpecies().getTexturePath(Species.SEED).orElse(provider.item(BuiltInRegistries.ITEM.getKey(seed))));
//        }
    }

    @Override
    public Dependencies gatherDependencies(Species input) {
        return new Dependencies()
                .append(SEED_ITEM, input.getSeed());
    }

}
