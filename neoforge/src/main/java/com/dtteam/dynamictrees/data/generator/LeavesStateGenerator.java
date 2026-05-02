package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.block.leaves.DynamicLeavesBlock;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.data.Generator;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

/**
 * @author Harley O'Connor
 */
public class LeavesStateGenerator implements Generator<BlockModelGenerators, LeavesProperties> {

    public static final DependencyKey<DynamicLeavesBlock> LEAVES = new DependencyKey<>("leaves");
    public static final DependencyKey<Block> PRIMITIVE_LEAVES = new DependencyKey<>("primitive_leaves");

    @Override
    public void generate(BlockModelGenerators generators, LeavesProperties input, Dependencies dependencies) {

        Identifier leavesModel = input.getModelPath(LeavesProperties.LEAVES)
                .orElse(ModelLocationUtils.getModelLocation(dependencies.get(PRIMITIVE_LEAVES)));

        DynamicLeavesBlock leavesBlock = dependencies.get(LEAVES);

        generators.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(leavesBlock, BlockModelGenerators.variant(new Variant(leavesModel)))
        );
    }

    @Override
    public Dependencies gatherDependencies(LeavesProperties input) {
        return new Dependencies()
                .append(LEAVES, input.getDynamicLeavesBlock())
                .append(PRIMITIVE_LEAVES, input.getPrimitiveLeavesBlock());
    }

}
