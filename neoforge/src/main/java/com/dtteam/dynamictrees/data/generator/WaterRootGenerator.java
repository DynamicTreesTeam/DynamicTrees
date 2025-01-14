//package com.dtteam.dynamictrees.data.generator;
//
//import com.dtteam.dynamictrees.DynamicTrees;
//import com.dtteam.dynamictrees.block.rooty.SoilProperties;
//import com.dtteam.dynamictrees.data.provider.DTBlockStateProvider;
//
///**
// * @author Harley O'Connor
// */
//public final class WaterRootGenerator extends SoilStateGenerator {
//
//    @Override
//    public void generate(DTBlockStateProvider provider, SoilProperties input, Dependencies dependencies) {
//        // TODO: Smart model for water roots.
//        provider.simpleBlock(
//                dependencies.get(SOIL),
//                provider.models().getExistingFile(DynamicTrees.location("block/roots_water"))
//        );
//    }
//
//    @Override
//    public Dependencies gatherDependencies(SoilProperties input) {
//        return new Dependencies()
//                .append(SOIL, input.getBlock());
//    }
//
//}
