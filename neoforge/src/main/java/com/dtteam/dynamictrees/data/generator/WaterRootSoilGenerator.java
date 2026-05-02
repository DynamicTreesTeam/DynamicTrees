package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.block.soil.SoilBlock;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.resources.Identifier;

/**
 * @author Harley O'Connor
 */
public final class WaterRootSoilGenerator extends SoilStateGenerator {

    @Override
    public void generate(BlockModelGenerators generators, SoilProperties input, Dependencies dependencies) {
        Identifier rootsModel = DynamicTrees.location("block/roots_water");
        SoilBlock soilBlock = dependencies.get(SOIL);

        generators.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(soilBlock, BlockModelGenerators.variant(new Variant(rootsModel)))
        );
    }

    @Override
    public Dependencies gatherDependencies(SoilProperties input) {
        return new Dependencies()
                .append(SOIL, input.getBlock());
    }

}
