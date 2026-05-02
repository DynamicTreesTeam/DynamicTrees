package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.block.pod.Pod;
import com.dtteam.dynamictrees.data.BiGenerator;
import com.dtteam.dynamictrees.data.builder.PottedSaplingLoaderBuilder;
import com.dtteam.dynamictrees.registry.DTRegistries;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.resources.Identifier;

/**
 * @author Harley O'Connor
 */
public class DTExtraModelGenerator implements BiGenerator<BlockModelGenerators, ItemModelGenerators, String> {

    @Override
    public void generate(BlockModelGenerators blockModels, ItemModelGenerators itemModels, String input, Dependencies deps) {
        itemModels.generateFlatItem(DTRegistries.DENDRO_POTION.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(DTRegistries.DIRT_BUCKET.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(DTRegistries.STAFF.get(), ModelTemplates.FLAT_ITEM);

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(
                        DTRegistries.TRUNK_SHELL.get(),
                        BlockModelGenerators.variant(new Variant(DynamicTrees.location("block/empty")))
                )
        );
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(
                        DTRegistries.POTTED_SAPLING.get(),
                        MultiVariant.of(new PottedSaplingLoaderBuilder(Identifier.withDefaultNamespace("block/flower_pot")))
                )
        );

        blockModels.createTrivialCube(Pod.REGISTRY.get(DynamicTrees.location("cocoa")).getBlock());
    }

    @Override
    public Dependencies gatherDependencies(String input) {
        return new Dependencies();
    }

}
