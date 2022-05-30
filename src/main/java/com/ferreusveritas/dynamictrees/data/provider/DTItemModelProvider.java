package com.ferreusveritas.dynamictrees.data.provider;

import com.ferreusveritas.dynamictrees.api.registry.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.List;

/**
 * @author Harley O'Connor
 */
public class DTItemModelProvider extends ItemModelProvider implements DTDataProvider {

    private final List<Registry<?>> registries;

    public DTItemModelProvider(DataGenerator generator, String modId, ExistingFileHelper existingFileHelper, List<Registry<?>> registries) {
        super(generator, modId, existingFileHelper);
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
