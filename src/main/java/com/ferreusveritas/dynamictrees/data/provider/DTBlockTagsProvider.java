package com.ferreusveritas.dynamictrees.data.provider;

import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

/**
 * @author Harley O'Connor
 */
public final class DTBlockTagsProvider extends BlockTagsProvider {

    public DTBlockTagsProvider(DataGenerator dataGenerator, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(dataGenerator, modId, existingFileHelper);
    }

    @Override
    protected void addTags() {
        LeavesProperties.REGISTRY.getAllFor(this.modId).forEach(leavesProperties -> {
            // Create dynamic leaves block tag.
            leavesProperties.getDynamicLeavesBlock().ifPresent(leaves ->
                    leavesProperties.defaultLeavesTags().forEach(tag ->
                            tag(tag).add(leaves))
            );
        });

        Family.REGISTRY.getAllFor(this.modId).forEach(family -> {
            // Create branch tag if a branch exists.
            family.getBranchOptional().ifPresent(branch ->
                    family.defaultBranchTags().forEach(tag ->
                            tag(tag).add(branch))
            );

            // Create stripped branch tag if the family has a stripped branch.
            family.getStrippedBranchOptional().ifPresent(strippedBranch ->
                    family.defaultStrippedBranchTags().forEach(tag ->
                            tag(tag).add(strippedBranch))
            );
        });

        Species.REGISTRY.getAllFor(this.modId).forEach(species -> {
            // Create dynamic sapling block tags.
            species.getSapling().ifPresent(sapling ->
                    species.defaultSaplingTags().forEach(tag ->
                            tag(tag).add(sapling)));
        });
    }

    @Override
    public String getName() {
        return modId + " DT Block Tags";
    }
}
