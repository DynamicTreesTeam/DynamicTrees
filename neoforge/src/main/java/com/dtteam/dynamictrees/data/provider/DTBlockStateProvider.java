package com.dtteam.dynamictrees.data.provider;

import com.dtteam.dynamictrees.api.registry.Registry;
import com.dtteam.dynamictrees.data.DTDataProvider;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.data.PackOutput;

import java.util.Collection;
import java.util.List;

/**
 * @author Harley O'Connor
 */
public class DTBlockStateProvider extends ModelProvider implements DTDataProvider.BlockState {

    private final List<Registry<?>> registries;

    public DTBlockStateProvider(PackOutput output, String modId, Collection<Registry<?>> registries) {
        super(output, modId);
        this.registries = ImmutableList.copyOf(registries);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        this.registries.forEach(registry ->
                registry.dataGenerationStream(this.modId).forEach(entry ->
                        entry.generateStateData(blockModels, this)
                )
        );
    }

}
