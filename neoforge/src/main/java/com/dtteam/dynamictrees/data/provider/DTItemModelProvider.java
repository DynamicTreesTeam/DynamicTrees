package com.dtteam.dynamictrees.data.provider;

import com.dtteam.dynamictrees.api.registry.Registry;
import com.dtteam.dynamictrees.data.DTDataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.List;

/**
 * @author Harley O'Connor
 */
public class DTItemModelProvider extends ItemModelProvider implements DTDataProvider.ItemModel {

    private final List<Registry<?>> registries;

    public DTItemModelProvider(PackOutput output, String modId, ExistingFileHelper fileHelper, List<Registry<?>> registries) {
        super(output, modId, fileHelper);
        this.registries = registries;
    }

    @Override
    protected void registerModels() {
        this.registries.forEach(registry ->
                registry.dataGenerationStream(this.modid).forEach(entry ->
                        entry.generateItemModelData(this)
                )
        );
    }

}
