package com.ferreusveritas.dynamictrees.data.provider;

import com.ferreusveritas.dynamictrees.api.registry.Registry;
import com.google.common.collect.ImmutableList;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Collection;
import java.util.List;

/**
 * @author Harley O'Connor
 */
public class DTBlockStateProvider extends BlockStateProvider implements DTDataProvider {

    private final String modId;
    private final List<Registry<?>> registries;

    public DTBlockStateProvider(PackOutput output, String modId, ExistingFileHelper existingFileHelper,
                                Collection<Registry<?>> registries) {
        super(output, modId, existingFileHelper);
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
