package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.block.leaves.DynamicLeavesBlock;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.data.builder.WinterLeavesLoaderBuilder;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.resources.Identifier;

public class WinterLeavesStateGenerator extends LeavesStateGenerator {

    @Override
    public void generate(BlockModelGenerators generators, LeavesProperties input, Dependencies dependencies) {

        Identifier leavesModel = input.getModelPath(LeavesProperties.LEAVES)
                .orElse(ModelLocationUtils.getModelLocation(dependencies.get(PRIMITIVE_LEAVES)));

        DynamicLeavesBlock leavesBlock = dependencies.get(LEAVES);

        generators.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(
                        leavesBlock,
                        MultiVariant.of(new WinterLeavesLoaderBuilder(
                                leavesModel,
                                DynamicTrees.location("block/winter_leaves")
                        ))
                )
        );
    }

}
