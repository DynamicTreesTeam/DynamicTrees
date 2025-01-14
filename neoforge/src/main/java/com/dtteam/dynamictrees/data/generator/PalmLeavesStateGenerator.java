//package com.dtteam.dynamictrees.data.generator;
//
//import com.dtteam.dynamictrees.block.leaves.DynamicLeavesBlock;
//import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
//import com.dtteam.dynamictrees.block.leaves.PalmLeavesProperties;
//import com.dtteam.dynamictrees.data.Generator;
//import com.dtteam.dynamictrees.data.provider.DTBlockStateProvider;
//import com.dtteam.dynamictrees.data.provider.PalmLeavesLoaderBuilder;
//import com.dtteam.dynamictrees.util.ResourceLocationUtils;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.level.block.Block;
//import net.minecraft.world.level.block.state.properties.IntegerProperty;
//import net.minecraftforge.client.model.generators.BlockModelBuilder;
//import net.minecraftforge.client.model.generators.ModelFile;
//import net.minecraftforge.registries.ForgeRegistries;
//
///**
// * @author Harley O'Connor
// */
//public class PalmLeavesStateGenerator implements Generator<DTBlockStateProvider, LeavesProperties> {
//
//    public static final DependencyKey<DynamicLeavesBlock> LEAVES = new DependencyKey<>("leaves");
//    public static final DependencyKey<Block> PRIMITIVE_LEAVES = new DependencyKey<>("primitive_leaves");
//
//    @Override
//    public void generate(DTBlockStateProvider provider, LeavesProperties input, Dependencies dependencies) {
//        ResourceLocation defaultFrondsTexture = provider.block(ResourceLocationUtils.suffix(input.getRegistryName(), "_frond"));
//        ResourceLocation defaultCoreTexture = provider.block(ResourceLocationUtils.suffix(input.getRegistryName(), "_base"));
//        PalmLeavesProperties palmInput = (PalmLeavesProperties) input;
//
//        final PalmLeavesLoaderBuilder frondBuilder = provider.models().getBuilder(palmInput.getFrondsModelName())
//                        .customLoader(palmInput.getFrondsLoaderConstructor());
//        palmInput.addFrondTextures(frondBuilder::texture, defaultFrondsTexture);
//
//        final BlockModelBuilder coreTopBuilder = provider.models().getBuilder(palmInput.getCoreTopModelName())
//                .parent(provider.models().getExistingFile(palmInput.getCoreTopSmartModelLocation()));
//        palmInput.addFrondTextures(coreTopBuilder::texture, defaultFrondsTexture);
//
//        final BlockModelBuilder coreBottomBuilder = provider.models().getBuilder(palmInput.getCoreBottomModelName())
//                .parent(provider.models().getExistingFile(palmInput.getCoreBottomSmartModelLocation()));
//        palmInput.addCoreTextures(coreBottomBuilder::texture, defaultCoreTexture);
//
//        final ModelFile blockModel = provider.models().getExistingFile(
//                palmInput.getModelPath(LeavesProperties.LEAVES).orElse(
//                        provider.block(ForgeRegistries.BLOCKS.getKey(dependencies.get(PRIMITIVE_LEAVES)))
//                )
//        );
//
//        final IntegerProperty distance = PalmLeavesProperties.DynamicPalmLeavesBlock.DISTANCE;
//        final IntegerProperty direction = PalmLeavesProperties.DynamicPalmLeavesBlock.DIRECTION;
//        provider.getMultipartBuilder(dependencies.get(LEAVES))
//                .part().modelFile(frondBuilder.end())
//                .addModel().condition(distance, 1,2)
//                .end()
//
//                .part().modelFile(coreTopBuilder)
//                .addModel().condition(distance, 3)
//                .end()
//
//                .part().modelFile(coreBottomBuilder)
//                .addModel().condition(distance, 4)
//                .end()
//
//                .part().modelFile(blockModel)
//                .addModel().useOr()
//                    .nestedGroup()
//                        .condition(direction, 0)
//                        .condition(distance, 1,2)
//                    .end()
//                    .nestedGroup()
//                        .condition(distance, 5,6,7)
//                    .end()
//                .end();
//
//    }
//
//    @Override
//    public Dependencies gatherDependencies(LeavesProperties input) {
//        return new Dependencies()
//                .append(LEAVES, input.getDynamicLeavesBlock())
//                .append(PRIMITIVE_LEAVES, input.getPrimitiveLeavesBlock());
//    }
//
//}
