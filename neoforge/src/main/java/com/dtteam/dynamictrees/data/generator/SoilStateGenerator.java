package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.block.soil.SoilBlock;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.data.Generator;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * @author Harley O'Connor
 */
public class SoilStateGenerator implements Generator<BlockModelGenerators, SoilProperties> {

    public static final DependencyKey<SoilBlock> SOIL = new DependencyKey<>("soil");
    public static final DependencyKey<Block> PRIMITIVE_SOIL = new DependencyKey<>("primitive_soil");

    @Override
    public void generate(BlockModelGenerators generators, SoilProperties input, Dependencies dependencies) {
        Identifier soilModel = input.getModelPath(SoilProperties.SOIL_BLOCK)
                .orElse(ModelLocationUtils.getModelLocation(dependencies.getOptional(PRIMITIVE_SOIL).orElse(Blocks.AIR)));
        Identifier rootsModel = input.getRootsOverlayModelLocation();

        SoilBlock soilBLock = dependencies.get(SOIL);

        generators.blockStateOutput.accept(
                MultiPartGenerator.multiPart(soilBLock)
                        .with(BlockModelGenerators.variant(new Variant(soilModel)))
                        .with(BlockModelGenerators.variant(new Variant(rootsModel)))
        );
    }

    @Override
    public boolean verifyInput(SoilProperties input) {
        return input.shouldGenerateBlock(); // Don't create states for substitutes as they use another soil's block.
    }

    @Override
    public Dependencies gatherDependencies(SoilProperties input) {
        return new Dependencies()
                .append(SOIL, input.getBlock())
                .append(PRIMITIVE_SOIL, input.getPrimitiveSoilBlockOptional());
    }

}
