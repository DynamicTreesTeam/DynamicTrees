//package com.dtteam.dynamictrees.data.generator;
//
//import com.dtteam.dynamictrees.data.Generator;
//import com.dtteam.dynamictrees.data.provider.DTItemModelProvider;
//import com.dtteam.dynamictrees.item.Seed;
//import com.dtteam.dynamictrees.tree.species.Species;
//import net.minecraftforge.registries.ForgeRegistries;
//
///**
// * @author Harley O'Connor
// */
//public class SeedItemModelGenerator implements Generator<DTItemModelProvider, Species> {
//
//    public static final DependencyKey<Seed> SEED = new DependencyKey<>("seed");
//
//    @Override
//    public void generate(DTItemModelProvider provider, Species input, Dependencies dependencies) {
//        final Seed seed = dependencies.get(SEED);
//        provider.withExistingParent(String.valueOf(ForgeRegistries.ITEMS.getKey(seed)), seed.getSpecies().getSeedParentModelLocation())
//                .texture("layer0", seed.getSpecies().getTexturePath(Species.SEED).orElse(provider.item(ForgeRegistries.ITEMS.getKey(seed))));
//    }
//
//    @Override
//    public Dependencies gatherDependencies(Species input) {
//        return new Dependencies()
//                .append(SEED, input.getSeed());
//    }
//
//}
