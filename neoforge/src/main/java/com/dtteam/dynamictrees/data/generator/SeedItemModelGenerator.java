//package com.dtteam.dynamictrees.data.generator;
//
//import com.dtteam.dynamictrees.data.DTDataProvider;
//import com.dtteam.dynamictrees.data.Generator;
//import com.dtteam.dynamictrees.data.provider.DTItemModelProvider;
//import com.dtteam.dynamictrees.item.Seed;
//import com.dtteam.dynamictrees.tree.species.Species;
//import net.minecraft.core.registries.BuiltInRegistries;
//
///**
// * @author Harley O'Connor
// */
//public class SeedItemModelGenerator implements Generator<DTDataProvider.ItemModel, Species> {
//
//    public static final DependencyKey<Seed> SEED = new DependencyKey<>("seed");
//
//    @Override
//    public void generate(DTDataProvider.ItemModel prov, Species input, Dependencies dependencies) {
//        if (prov instanceof DTItemModelProvider provider){
//            final Seed seed = dependencies.get(SEED);
//            provider.withExistingParent(String.valueOf(BuiltInRegistries.ITEM.getKey(seed)), seed.getSpecies().getSeedParentModelLocation())
//                    .texture("layer0", seed.getSpecies().getTexturePath(Species.SEED).orElse(provider.item(BuiltInRegistries.ITEM.getKey(seed))));
//        }
//    }
//
//    @Override
//    public Dependencies gatherDependencies(Species input) {
//        return new Dependencies()
//                .append(SEED, input.getSeed());
//    }
//
//}
