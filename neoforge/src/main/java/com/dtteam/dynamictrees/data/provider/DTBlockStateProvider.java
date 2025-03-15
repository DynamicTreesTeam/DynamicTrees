package com.dtteam.dynamictrees.data.provider;

import com.dtteam.dynamictrees.api.registry.Registry;
import com.dtteam.dynamictrees.data.DTDataProvider;
import com.google.common.collect.ImmutableList;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.Collection;
import java.util.List;

/**
 * @author Harley O'Connor
 */
public class DTBlockStateProvider extends BlockStateProvider implements DTDataProvider.BlockState {

    private final String modId;
    private final List<Registry<?>> registries;

    public DTBlockStateProvider(PackOutput output, String modId, ExistingFileHelper fileHelper,
                                Collection<Registry<?>> registries) {
        super(output, modId, fileHelper);
        this.modId = modId;
        this.registries = ImmutableList.copyOf(registries);
    }

    @Override
    protected void registerStatesAndModels() {
        this.registries.forEach(registry ->
                registry.dataGenerationStream(this.modId).forEach(entry ->
                        entry.generateStateData(this)
                )
        );
    }

}
