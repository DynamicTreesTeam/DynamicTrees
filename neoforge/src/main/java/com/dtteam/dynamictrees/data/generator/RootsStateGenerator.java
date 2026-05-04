package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.data.Generator;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.family.AerialRootsFamily;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.world.level.block.Block;

/**
 * @author Max Hyper
 */
public class RootsStateGenerator implements Generator<BlockModelGenerators, Family> {

    public static final DependencyKey<BranchBlock> ROOT = new DependencyKey<>("root");
    public static final DependencyKey<Block> PRIMITIVE_ROOT = new DependencyKey<>("primitive_root");
    public static final DependencyKey<Block> PRIMITIVE_FILLED_ROOT = new DependencyKey<>("filled_primitive_root");
    public static final DependencyKey<Block> PRIMITIVE_COVERED_ROOT = new DependencyKey<>("covered_primitive_root");

    @Override
    public void generate(BlockModelGenerators generators, Family input, Dependencies dependencies) {
        generators.createTrivialCube(dependencies.get(ROOT));
//        if (prov instanceof DTBlockStateProvider provider){
//            final BranchBlock root = dependencies.get(ROOT);
//            final BranchLoaderBuilder builderExposed = provider.models().getBuilder(
//                    Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(root)).getPath()
//            ).customLoader(BranchLoaderBuilder.branchBuilders.get(input.getRootsLoader()));
//            input.addRootTextures(builderExposed::texture, provider.block(BuiltInRegistries.BLOCK.getKey(dependencies.get(PRIMITIVE_ROOT))));
//
//            final BranchLoaderBuilder builderFilled = provider.models().getBuilder(
//                    Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(root)).getPath() + "_filled"
//            ).customLoader(BranchLoaderBuilder.branchBuilders.get(input.getRootsLoader()));
//            input.addRootTextures(builderFilled::texture, provider.block(BuiltInRegistries.BLOCK.getKey(dependencies.get(PRIMITIVE_FILLED_ROOT))));
//
//            provider.getVariantBuilder(root)
//                    .partialState().with(BasicRootsBlock.LAYER, BasicRootsBlock.Layer.EXPOSED)
//                    .modelForState().modelFile(builderExposed.end().renderType("cutout_mipped")).addModel()
//                    .partialState().with(BasicRootsBlock.LAYER, BasicRootsBlock.Layer.FILLED)
//                    .modelForState().modelFile(builderFilled.end()).addModel()
//                    .partialState().with(BasicRootsBlock.LAYER, BasicRootsBlock.Layer.COVERED)
//                    .modelForState().modelFile(provider.models().getExistingFile(input
//                            .getModelPath(Family.COVERED_ROOTS_BLOCK)
//                            .orElse(provider.blockTexture(dependencies.get(PRIMITIVE_COVERED_ROOT)))
//                    )).addModel();
//        }
    }

    @Override
    public Dependencies gatherDependencies(Family input) {
        AerialRootsFamily mangroveInput = (AerialRootsFamily) input;
        return new Dependencies()
                .append(ROOT, mangroveInput.getRoots())
                .append(PRIMITIVE_ROOT, mangroveInput.getPrimitiveRoots())
                .append(PRIMITIVE_FILLED_ROOT, mangroveInput.getPrimitiveFilledRoots())
                .append(PRIMITIVE_COVERED_ROOT, mangroveInput.getPrimitiveCoveredRoots());
    }

}
