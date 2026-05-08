package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.block.branch.BasicRootsBlock;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.data.Generator;
import com.dtteam.dynamictrees.data.builder.BasicLoaderBuilder;
import com.dtteam.dynamictrees.tree.family.AerialRootsFamily;
import com.dtteam.dynamictrees.tree.family.Family;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

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
        final BranchBlock branch = dependencies.get(ROOT);
        final Block primitiveExposed = dependencies.get(PRIMITIVE_ROOT);
        final Block primitiveFilled = dependencies.get(PRIMITIVE_FILLED_ROOT);
        final Block primitiveCovered = dependencies.get(PRIMITIVE_COVERED_ROOT);
        Identifier primitiveExposedPath = ModelLocationUtils.getModelLocation(primitiveExposed);
        Identifier primitiveFilledPath = ModelLocationUtils.getModelLocation(primitiveFilled);
        Identifier primitiveCoveredPath = ModelLocationUtils.getModelLocation(primitiveCovered);

        final Map<String, Identifier> exposedTextures = new HashMap<>();
        final Map<String, Identifier> filledTextures = new HashMap<>();
        input.addRootTextures(exposedTextures::put, primitiveExposedPath);
        input.addRootTextures(filledTextures::put, primitiveFilledPath);

        BasicLoaderBuilder exposedBuilder = BasicLoaderBuilder.loaderBuilders.get(input.getRootsLoader()).apply(exposedTextures, input);
        BasicLoaderBuilder filledBuilder = BasicLoaderBuilder.loaderBuilders.get(input.getRootsLoader().withSuffix("_opaque")).apply(filledTextures, input);

        var propertyDispatch = PropertyDispatch.initial(BasicRootsBlock.LAYER).generate(
                layer -> switch (layer){
                    case EXPOSED -> MultiVariant.of(exposedBuilder);
                    case FILLED -> MultiVariant.of(filledBuilder);
                    default -> BlockModelGenerators.variant(new Variant(primitiveCoveredPath));
                }
        );

        generators.blockStateOutput.accept(MultiVariantGenerator.dispatch(branch).with(propertyDispatch));
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
