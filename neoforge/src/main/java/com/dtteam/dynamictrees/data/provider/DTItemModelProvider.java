package com.dtteam.dynamictrees.data.provider;

import com.dtteam.dynamictrees.api.registry.Registry;
import com.dtteam.dynamictrees.data.DTDataProvider;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.data.PackOutput;

import java.util.List;

/**
 * @author Harley O'Connor
 */
public class DTItemModelProvider extends ModelProvider implements DTDataProvider.ItemModel {

    private final List<Registry<?>> registries;

    public DTItemModelProvider(PackOutput output, String modId, List<Registry<?>> registries) {
        super(output, modId);
        this.registries = registries;
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        this.registries.forEach(registry ->
                registry.dataGenerationStream(this.modId).forEach(entry ->
                        entry.generateItemModelData(this)
                )
        );
    }

}
