package com.ferreusveritas.dynamictrees.data.provider;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.block.rooty.AerialRootsSoilProperties;
import com.ferreusveritas.dynamictrees.block.rooty.SoilProperties;
import com.ferreusveritas.dynamictrees.data.DTBlockTags;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.ferreusveritas.dynamictrees.tree.family.MangroveFamily;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeBlockTagsProvider;

import javax.annotation.Nullable;
import java.util.Optional;

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
                .add(Blocks.LILY_PAD)
                .addTag(BlockTags.FLOWERS)
                .addTag(BlockTags.REPLACEABLE_PLANTS);

        this.tag(DTBlockTags.STRIPPED_BRANCHES)
                .addTag(DTBlockTags.STRIPPED_BRANCHES_THAT_BURN)
                .addTag(DTBlockTags.STRIPPED_FUNGUS_BRANCHES);

        this.tag(BlockTags.ENDERMAN_HOLDABLE)
                .addTag(DTBlockTags.FUNGUS_CAPS);

        this.tag(BlockTags.FLOWER_POTS)
                .add(DTRegistries.POTTED_SAPLING.get());

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

        this.tag(BlockTags.MINEABLE_WITH_AXE)
                .addTag(DTBlockTags.ROOTS)
                .addTag(DTBlockTags.AERIAL_ROOTS_ROOTY_SOIL);
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
            // Create branch tag and harvest tag if a branch exists.
            family.getBranch().ifPresent(branch -> {
                this.tierTag(family.getDefaultBranchHarvestTier()).ifPresent(tagBuilder -> tagBuilder.add(branch));
                family.defaultBranchTags().forEach(tag ->
                        this.tag(tag).add(branch));
            });

            // Create stripped branch tag and harvest tag if the family has a stripped branch.
            family.getStrippedBranch().ifPresent(strippedBranch -> {
                this.tierTag(family.getDefaultStrippedBranchHarvestTier()).ifPresent(tagBuilder -> tagBuilder.add(strippedBranch));
                family.defaultStrippedBranchTags().forEach(tag ->
                        this.tag(tag).add(strippedBranch));
            });

            //Create roots tag and root harvest tag if the family is mangrove-like.
            if (family instanceof MangroveFamily mangroveFamily){
                mangroveFamily.getRoots().ifPresent(roots -> {
                    this.tierTag(mangroveFamily.getDefaultRootsHarvestTier()).ifPresent(tagBuilder -> tagBuilder.add(roots));
                    mangroveFamily.defaultRootsTags().forEach(tag ->
                            this.tag(tag).add(roots));
                });
            }

        });

        Species.REGISTRY.dataGenerationStream(this.modId).forEach(species -> {
            // Create dynamic sapling block tags.
            species.getSapling().ifPresent(sapling ->
                    species.defaultSaplingTags().forEach(tag ->
                            this.tag(tag).add(sapling)));
        });

        SoilProperties.REGISTRY.dataGenerationStream(this.modId).forEach(soilProperties -> {
            // add rooty blocks to the rooty soil tag.
            soilProperties.getBlock().ifPresent(rootyBlock ->
                    tag(DTBlockTags.ROOTY_SOIL).add(rootyBlock));
            // add mangrove-like rooty blocks to the axe mineable tags.
            if (soilProperties instanceof AerialRootsSoilProperties){
                soilProperties.getBlock().ifPresent(rootyBlock ->
                        tag(DTBlockTags.AERIAL_ROOTS_ROOTY_SOIL).add(rootyBlock)
                );
            }
        });
    }

    protected Optional<TagsProvider.TagAppender<Block>> tierTag(@Nullable Tier tier) {
        if (tier == null)
            return Optional.empty();

        TagKey<Block> tag = tier.getTag();

        return tag == null ? Optional.empty() : Optional.of(this.tag(tag));
    }

    @Override
    public String getName() {
        return modId + " DT Block Tags";
    }
}
