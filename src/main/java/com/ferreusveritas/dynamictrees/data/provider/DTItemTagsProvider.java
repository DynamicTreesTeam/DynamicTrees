package com.ferreusveritas.dynamictrees.data.provider;

import com.ferreusveritas.dynamictrees.data.DTBlockTags;
import com.ferreusveritas.dynamictrees.data.DTItemTags;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

/**
 * @author Harley O'Connor
 */
public final class DTItemTagsProvider extends ItemTagsProvider {

    public DTItemTagsProvider(DataGenerator dataGenerator, String modId, BlockTagsProvider blockTagsProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(dataGenerator, blockTagsProvider, modId, existingFileHelper);
    }

    @Override
    protected void addTags() {
        this.copy(DTBlockTags.BRANCHES_THAT_BURN, DTItemTags.BRANCHES_THAT_BURN);
        this.copy(DTBlockTags.FUNGUS_BRANCHES, DTItemTags.FUNGUS_BRANCHES);

        Species.REGISTRY.getAllFor(this.modId).forEach(species -> {
            // Some species return the common seed, so only return if the species has its own seed.
            if (!species.hasSeed())
                return;

            // Create seed item tag.
            species.getSeed().ifPresent(seed ->
                    species.defaultSeedTags().forEach(tag ->
                            tag(tag).add(seed)));
        });
    }

    @Override
    public String getName() {
        return modId + " DT Block Tags";
    }
}
