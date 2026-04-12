//package com.dtteam.dynamictrees.data.generator;
//
//import com.dtteam.dynamictrees.block.soil.SoilBlock;
//import com.dtteam.dynamictrees.block.soil.SoilProperties;
//import com.dtteam.dynamictrees.data.DTDataProvider;
//import com.dtteam.dynamictrees.data.Generator;
//import com.dtteam.dynamictrees.data.provider.DTBlockStateProvider;
//import net.minecraft.core.registries.BuiltInRegistries;
//import net.minecraft.world.level.block.Block;
//
//import java.util.Objects;
//
///**
// * @author Harley O'Connor
// */
//public class SoilStateGenerator implements Generator<DTDataProvider.BlockState, SoilProperties> {
//
//    public static final DependencyKey<SoilBlock> SOIL = new DependencyKey<>("soil");
//    public static final DependencyKey<Block> PRIMITIVE_SOIL = new DependencyKey<>("primitive_soil");
//
//    @Override
//    public void generate(DTDataProvider.BlockState prov, SoilProperties input, Dependencies dependencies) {
//        if (prov instanceof DTBlockStateProvider provider){
//            provider.getMultipartBuilder(dependencies.get(SOIL))
//                    .part().modelFile(provider.models().getExistingFile(
//                            input.getModelPath(SoilProperties.SOIL_BLOCK).orElse(provider.block(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(dependencies.get(PRIMITIVE_SOIL)))))
//                    )).addModel().end()
//                    .part().modelFile(provider.models().getExistingFile(input.getRootsOverlayModelLocation())).addModel().end();
//        }
//    }
//
//    @Override
//    public boolean verifyInput(SoilProperties input) {
//        return input.shouldGenerateBlock(); // Don't create states for substitutes as they use another soil's block.
//    }
//
//    @Override
//    public Dependencies gatherDependencies(SoilProperties input) {
//        return new Dependencies()
//                .append(SOIL, input.getBlock())
//                .append(PRIMITIVE_SOIL, input.getPrimitiveSoilBlockOptional());
//    }
//
//}
