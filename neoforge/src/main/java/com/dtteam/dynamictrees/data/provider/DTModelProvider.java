package com.dtteam.dynamictrees.data.provider;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.registry.Registry;
import com.dtteam.dynamictrees.block.fruit.Fruit;
import com.dtteam.dynamictrees.block.pod.Pod;
import com.dtteam.dynamictrees.data.DTDataProvider;
import com.dtteam.dynamictrees.data.builder.PottedSaplingLoaderBuilder;
import com.dtteam.dynamictrees.registry.DTRegistries;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.List;

/**
 * @author Harley O'Connor
 */
public class DTModelProvider extends ModelProvider implements DTDataProvider {

    private final List<Registry<?>> registries;

    public DTModelProvider(PackOutput output, String modId, Collection<Registry<?>> registries) {
        super(output, modId);
        this.registries = ImmutableList.copyOf(registries);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        this.registries.forEach(registry ->
                registry.dataGenerationStream(this.modId).forEach(entry -> {
                            entry.generateStateData(blockModels);
                            entry.generateItemModelData(itemModels);
                        }
                ));

        //TEMP FOR TESTING
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
        blockModels.createTrivialCube(Fruit.REGISTRY.get(DynamicTrees.location("apple")).getBlock());
        blockModels.createTrivialCube(Pod.REGISTRY.get(DynamicTrees.location("cocoa")).getBlock());
    }

}
