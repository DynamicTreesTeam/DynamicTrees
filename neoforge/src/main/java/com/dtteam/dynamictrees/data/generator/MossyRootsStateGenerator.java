package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.block.branch.BasicRootsBlock;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.data.builder.BasicLoaderBuilder;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.family.MossyAerialRootsFamily;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.ConditionBuilder;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Optional;

/**
 * @author Max Hyper
 */
public class MossyRootsStateGenerator extends RootsStateGenerator {

    public static final DependencyKey<Block> MOSS_CARPET = new DependencyKey<>("moss_carpet");

    @Override
    protected void acceptOutput(BlockModelGenerators generators, BasicLoaderBuilder exposedBuilder, BasicLoaderBuilder filledBuilder, Identifier primitiveCoveredPath, BranchBlock branch, Family input, Dependencies dependencies) {
        Identifier testModel = ModelLocationUtils.getModelLocation(dependencies.getOptional(MOSS_CARPET).orElse(Blocks.AIR));

        generators.blockStateOutput.accept(
                MultiPartGenerator.multiPart(branch)
                        .with(
                                new ConditionBuilder().term(BasicRootsBlock.LAYER, BasicRootsBlock.Layer.EXPOSED),
                                MultiVariant.of(exposedBuilder)
                        ).with(
                                new ConditionBuilder().term(BasicRootsBlock.LAYER, BasicRootsBlock.Layer.FILLED),
                                MultiVariant.of(filledBuilder)
                        ).with(
                                new ConditionBuilder().term(BasicRootsBlock.LAYER, BasicRootsBlock.Layer.COVERED),
                                BlockModelGenerators.variant(new Variant(primitiveCoveredPath))
                        ).with(
                                new ConditionBuilder().term(BasicRootsBlock.LAYER, BasicRootsBlock.Layer.EXPOSED, BasicRootsBlock.Layer.FILLED),
                                BlockModelGenerators.variant(new Variant(testModel))
                        )
        );
    }

    @Override
    public Dependencies gatherDependencies(Family input) {
        MossyAerialRootsFamily mangroveInput = (MossyAerialRootsFamily) input;
        return super.gatherDependencies(input)
                .append(ROOT, mangroveInput.getMossyRoots())
                .append(MOSS_CARPET, Optional.of(mangroveInput.getMossCarpetBlock()));
    }

}
