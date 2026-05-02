package com.dtteam.dynamictrees.data.provider;

import com.dtteam.dynamictrees.api.registry.Registry;
import com.dtteam.dynamictrees.data.DTDataProvider;
import com.dtteam.dynamictrees.data.GatherDataHelper;
import com.dtteam.dynamictrees.data.Generator;
import com.dtteam.dynamictrees.data.generator.DataGenerators;
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
                            entry.getBlockModelGenerators().forEach(id ->
                                    DataGenerators.runBlockModelGenerator(blockModels, entry, id)
                            );
                            entry.getItemModelGenerators().forEach(id ->
                                    DataGenerators.runItemModelGenerator(itemModels, entry, id)
                            );
                        }
                ));
        var generator = GatherDataHelper.getExtraModelGenerator(this.modId);
        if (generator != null) {
            generator.generate(blockModels, itemModels, "", new Generator.Dependencies());
        }
    }

}
