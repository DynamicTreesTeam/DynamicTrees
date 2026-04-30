package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.block.leaves.DynamicLeavesBlock;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.data.Generator;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.world.level.block.Block;

/**
 * @author Harley O'Connor
 */
public class PalmLeavesStateGenerator implements Generator<BlockModelGenerators, LeavesProperties> {

    public static final DependencyKey<DynamicLeavesBlock> LEAVES = new DependencyKey<>("leaves");
    public static final DependencyKey<Block> PRIMITIVE_LEAVES = new DependencyKey<>("primitive_leaves");

    @Override
    public void generate(BlockModelGenerators generators, LeavesProperties input, Dependencies dependencies) {
        generators.createTrivialCube(dependencies.get(LEAVES));
//        if (prov instanceof DTBlockStateProvider provider){
//            Identifier defaultFrondsTexture = provider.block(IdentifierUtils.suffix(input.getRegistryName(), "_frond"));
//            Identifier defaultCoreTexture = provider.block(IdentifierUtils.suffix(input.getRegistryName(), "_base"));
//            PalmLeavesProperties palmInput = (PalmLeavesProperties) input;
//
//            final PalmLeavesLoaderBuilder frondBuilder = provider.models().getBuilder(palmInput.getFrondsModelName())
//                    .customLoader((b,e)->PalmLeavesLoaderBuilder.fronds(palmInput.getFrondLoader(), b,e));
//            palmInput.addFrondTextures(frondBuilder::texture, defaultFrondsTexture);
//
//            final BlockModelBuilder coreTopBuilder = provider.models().getBuilder(palmInput.getCoreTopModelName())
//                    .parent(provider.models().getExistingFile(palmInput.getCoreTopSmartModelLocation()));
//            palmInput.addFrondTextures(coreTopBuilder::texture, defaultFrondsTexture);
//
//            final BlockModelBuilder coreBottomBuilder = provider.models().getBuilder(palmInput.getCoreBottomModelName())
//                    .parent(provider.models().getExistingFile(palmInput.getCoreBottomSmartModelLocation()));
//            palmInput.addCoreTextures(coreBottomBuilder::texture, defaultCoreTexture);
//
//            final ModelFile blockModel = provider.models().getExistingFile(
//                    palmInput.getModelPath(LeavesProperties.LEAVES).orElse(
//                            provider.block(BuiltInRegistries.BLOCK.getKey(dependencies.get(PRIMITIVE_LEAVES)))
//                    )
//            );
//
//            final IntegerProperty distance = PalmLeavesProperties.DynamicPalmLeavesBlock.DISTANCE;
//            final IntegerProperty direction = PalmLeavesProperties.DynamicPalmLeavesBlock.DIRECTION;
//            provider.getMultipartBuilder(dependencies.get(LEAVES))
//                    .part().modelFile(frondBuilder.end())
//                    .addModel().condition(distance, 1,2)
//                    .end()
//
//                    .part().modelFile(coreTopBuilder)
//                    .addModel().condition(distance, 3)
//                    .end()
//
//                    .part().modelFile(coreBottomBuilder)
//                    .addModel().condition(distance, 4)
//                    .end()
//
//                    .part().modelFile(blockModel)
//                    .addModel().useOr()
//                    .nestedGroup()
//                    .condition(direction, 0)
//                    .condition(distance, 1,2)
//                    .end()
//                    .nestedGroup()
//                    .condition(distance, 5,6,7)
//                    .end()
//                    .end();
//
//        }
    }

    @Override
    public Dependencies gatherDependencies(LeavesProperties input) {
        return new Dependencies()
                .append(LEAVES, input.getDynamicLeavesBlock())
                .append(PRIMITIVE_LEAVES, input.getPrimitiveLeavesBlock());
    }

}
