package com.dtteam.dynamictrees.data.provider;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.data.tags.DTItemTags;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * @author Harley O'Connor
 */
public class DTItemTagsProvider extends ItemTagsProvider {
    public DTItemTagsProvider(PackOutput output, String modId, CompletableFuture<HolderLookup.Provider> lookupProvider,
                              CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper fileHelper) {
        super(output, lookupProvider, blockTags, modId, fileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        if (this.modId.equals(DynamicTrees.MOD_ID)) {
            this.addDTOnlyTags();
        }
        this.addDTTags();
    }

    private void addDTOnlyTags() {
        this.tag(DTItemTags.BRANCHES)
                .addTag(DTItemTags.BRANCHES_THAT_BURN)
                .addTag(DTItemTags.FUNGUS_BRANCHES);

        this.tag(DTItemTags.SEEDS)
                .addTag(DTItemTags.FUNGUS_CAPS);

        this.tag(ItemTags.SAPLINGS)
                .addTag(DTItemTags.SEEDS);
    }

    protected void addDTTags() {
        Family.REGISTRY.dataGenerationStream(this.modId).forEach(family ->
                family.addGeneratedItemTags(this::tag));

        Species.REGISTRY.dataGenerationStream(this.modId).forEach(species ->
                species.addGeneratedItemTags(this::tag));
    }

    @Override
    public String getName() {
        return modId + " DT Item Tags";
    }

}
