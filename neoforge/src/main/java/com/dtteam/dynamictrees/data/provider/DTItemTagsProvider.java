package com.dtteam.dynamictrees.data.provider;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.data.tags.DTItemTags;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

/**
 * @author Harley O'Connor
 */
public class DTItemTagsProvider extends ItemTagsProvider {
    public DTItemTagsProvider(PackOutput output, String modId, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, modId);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.addDTTags();
        if (this.modId.equals(DynamicTrees.MOD_ID)) {
            this.addDTOnlyTags();
        }
    }

    private void addDTOnlyTags() {
        this.tag(DTItemTags.BRANCHES_THAT_BURN);
        this.tag(DTItemTags.FUNGUS_BRANCHES);
        this.tag(DTItemTags.FUNGUS_CAPS);
        this.tag(DTItemTags.SEEDS);

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
                family.addGeneratedItemTags(tag -> NeoForgeTagAppender.items(this.tag(tag))));

        Species.REGISTRY.dataGenerationStream(this.modId).forEach(species ->
                species.addGeneratedItemTags(tag -> NeoForgeTagAppender.items(this.tag(tag))));
    }

    @Override
    public String getName() {
        return this.modId + " DT Item Tags";
    }
}
