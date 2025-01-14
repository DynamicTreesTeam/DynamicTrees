//package com.dtteam.dynamictrees.data.generator;
//
//import com.dtteam.dynamictrees.block.rooty.RootyBlock;
//import com.dtteam.dynamictrees.block.rooty.SoilProperties;
//import com.dtteam.dynamictrees.data.Generator;
//import com.dtteam.dynamictrees.data.provider.DTBlockStateProvider;
//import net.minecraft.world.level.block.Block;
//import net.minecraftforge.registries.ForgeRegistries;
//
//import java.util.Objects;
//
///**
// * @author Harley O'Connor
// */
//public class SoilStateGenerator implements Generator<DTBlockStateProvider, SoilProperties> {
//
//    public static final DependencyKey<RootyBlock> SOIL = new DependencyKey<>("soil");
//    public static final DependencyKey<Block> PRIMITIVE_SOIL = new DependencyKey<>("primitive_soil");
//
//    @Override
//    public void generate(DTBlockStateProvider provider, SoilProperties input, Dependencies dependencies) {
//        provider.getMultipartBuilder(dependencies.get(SOIL))
//                .part().modelFile(provider.models().getExistingFile(
//                        input.getModelPath(SoilProperties.SOIL_BLOCK).orElse(provider.block(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(dependencies.get(PRIMITIVE_SOIL)))))
//                )).addModel().end()
//                .part().modelFile(provider.models().getExistingFile(input.getRootsOverlayModelLocation())).addModel().end();
//    }
//
//    @Override
//    public boolean verifyInput(SoilProperties input) {
//        return !input.hasSubstitute(); // Don't create states for substitutes as they use another soil's block.
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
