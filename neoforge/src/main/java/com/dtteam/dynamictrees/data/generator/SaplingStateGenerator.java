package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.block.sapling.DynamicSaplingBlock;
import com.dtteam.dynamictrees.data.Generator;
import com.dtteam.dynamictrees.data.GeneratorHelper;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public class SaplingStateGenerator implements Generator<BlockModelGenerators, Species> {

    public static final DependencyKey<DynamicSaplingBlock> SAPLING = new DependencyKey<>("sapling");
    public static final DependencyKey<Block> PRIMITIVE_LOG = new DependencyKey<>("primitive_log");
    public static final DependencyKey<Block> PRIMITIVE_LEAVES = new DependencyKey<>("primitive_leaves", true);

    @Override
    public void generate(BlockModelGenerators generators, Species input, Dependencies dependencies) {
        Block log = dependencies.get(PRIMITIVE_LOG);
        Identifier logTexture = ModelLocationUtils.getModelLocation(log);
        Identifier leavesTexture = ModelLocationUtils.getModelLocation(dependencies.getOptional(PRIMITIVE_LEAVES).orElse(log));

        final Map<String, Identifier> textures = new HashMap<>();
        input.addSaplingTextures(textures::put, leavesTexture, logTexture);

        DynamicSaplingBlock saplingBlock = dependencies.get(SAPLING);

        TextureSlot[] slots = GeneratorHelper.createSlots(textures);
        ModelTemplate saplingTemplate = ModelTemplates.create(input.getSaplingSmartModelLocation().toString(), slots);
        Identifier modelLocation = input.getRegistryName().withPrefix("block/saplings/");
        Identifier model = saplingTemplate.create(modelLocation, GeneratorHelper.createMapping(textures, slots), generators.modelOutput);

        generators.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(
                        saplingBlock,
                        BlockModelGenerators.variant(new Variant(model))
                )
        );
    }

    @Override
    public Dependencies gatherDependencies(Species input) {
        return new Dependencies()
                .append(SAPLING, input.getSapling())
                .append(PRIMITIVE_LOG, input.getFamily().getPrimitiveLog())
                .append(PRIMITIVE_LEAVES, input.getPrimitiveLeaves());
    }

}
