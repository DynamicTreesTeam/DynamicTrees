package com.ferreusveritas.dynamictrees.data.provider;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.data.DTBlockTags;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.block.Blocks;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

/**
 * @author Harley O'Connor
 */
public class DTBlockTagsProvider extends BlockTagsProvider {

    public DTBlockTagsProvider(DataGenerator dataGenerator, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(dataGenerator, modId, existingFileHelper);
    }

    @Override
    protected void addTags() {
        if (this.modId.equals(DynamicTrees.MOD_ID)) {
            this.addDTOnlyTags();
        }
        this.addDTTags();
    }

    private void addDTOnlyTags() {
        this.tag(DTBlockTags.BRANCHES)
                .addTag(DTBlockTags.BRANCHES_THAT_BURN)
                .addTag(DTBlockTags.FUNGUS_BRANCHES);

        this.tag(DTBlockTags.FOLIAGE)
                .add(Blocks.GRASS)
                .add(Blocks.TALL_GRASS)
                .add(Blocks.FERN);

        this.tag(DTBlockTags.STRIPPED_BRANCHES)
                .addTag(DTBlockTags.STRIPPED_BRANCHES_THAT_BURN)
                .addTag(DTBlockTags.STRIPPED_FUNGUS_BRANCHES);

        this.tag(BlockTags.ENDERMAN_HOLDABLE)
                .addTag(DTBlockTags.FUNGUS_CAPS);

        this.tag(BlockTags.FLOWER_POTS)
                .add(DTRegistries.POTTED_SAPLING.getBlock());

        Species.REGISTRY.get(DTTrees.WARPED).getSapling().ifPresent(sapling ->
                this.tag(BlockTags.HOGLIN_REPELLENTS).add(sapling));

        this.tag(BlockTags.LEAVES)
                .addTag(DTBlockTags.LEAVES);

        this.tag(BlockTags.LOGS)
                .addTag(DTBlockTags.BRANCHES);

        this.tag(BlockTags.LOGS_THAT_BURN)
                .addTag(DTBlockTags.BRANCHES_THAT_BURN)
                .addTag(DTBlockTags.STRIPPED_BRANCHES_THAT_BURN);

        this.tag(BlockTags.SAPLINGS)
                .addTag(DTBlockTags.SAPLINGS);

        this.tag(BlockTags.WART_BLOCKS)
                .addTag(DTBlockTags.WART_BLOCKS);
    }

    protected void addDTTags() {
        LeavesProperties.REGISTRY.dataGenerationStream(this.modId).forEach(leavesProperties -> {
            // Create dynamic leaves block tag.
            leavesProperties.getDynamicLeavesBlock().ifPresent(leaves ->
                    leavesProperties.defaultLeavesTags().forEach(tag ->
                            this.tag(tag).add(leaves))
            );
        });

        Family.REGISTRY.dataGenerationStream(this.modId).forEach(family -> {
            // Create branch tag if a branch exists.
            family.getBranch().ifPresent(branch ->
                    family.defaultBranchTags().forEach(tag ->
                            this.tag(tag).add(branch))
            );

            // Create stripped branch tag if the family has a stripped branch.
            family.getStrippedBranch().ifPresent(strippedBranch ->
                    family.defaultStrippedBranchTags().forEach(tag ->
                            this.tag(tag).add(strippedBranch))
            );
        });

        Species.REGISTRY.dataGenerationStream(this.modId).forEach(species -> {
            // Create dynamic sapling block tags.
            species.getSapling().ifPresent(sapling ->
                    species.defaultSaplingTags().forEach(tag ->
                            this.tag(tag).add(sapling)));
        });
    }

    @Override
    public String getName() {
        return modId + " DT Block Tags";
    }
}
