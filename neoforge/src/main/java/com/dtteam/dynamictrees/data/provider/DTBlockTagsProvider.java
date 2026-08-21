package com.dtteam.dynamictrees.data.provider;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.data.tags.DTBlockTags;
import com.dtteam.dynamictrees.registry.DTRegistries;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

/**
 * @author Harley O'Connor
 */
public class DTBlockTagsProvider extends BlockTagsProvider {
    public DTBlockTagsProvider(PackOutput output, String modid, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, modid);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.addDTTags();
        if (this.modId.equals(DynamicTrees.MOD_ID)) {
            this.addDTOnlyTags();
        }
    }

    private void addDTOnlyTags() {
        this.tag(DTBlockTags.BRANCHES_THAT_BURN);
        this.tag(DTBlockTags.FUNGUS_BRANCHES);
        this.tag(DTBlockTags.STRIPPED_BRANCHES_THAT_BURN);
        this.tag(DTBlockTags.STRIPPED_FUNGUS_BRANCHES);
        this.tag(DTBlockTags.FUNGUS_CAPS);
        this.tag(DTBlockTags.LEAVES);
        this.tag(DTBlockTags.SAPLINGS);
        this.tag(DTBlockTags.WART_BLOCKS);
        this.tag(DTBlockTags.ROOTS);
        this.tag(DTBlockTags.AERIAL_ROOTS_ROOTY_SOIL);
        this.tag(DTBlockTags.FOLIAGE);

        this.tag(DTBlockTags.BRANCHES)
                .addTag(DTBlockTags.BRANCHES_THAT_BURN)
                .addTag(DTBlockTags.FUNGUS_BRANCHES);

        this.tag(DTBlockTags.FOLIAGE)
                .add(key(Blocks.SHORT_GRASS))
                .add(key(Blocks.TALL_GRASS))
                .add(key(Blocks.FERN))
                .add(key(Blocks.LILY_PAD))
                .add(key(Blocks.PINK_PETALS))
                .add(key(Blocks.BROWN_MUSHROOM))
                .add(key(Blocks.RED_MUSHROOM))
                .add(key(Blocks.MOSS_CARPET))
                .addTag(BlockTags.FLOWERS)
                .addTag(BlockTags.REPLACEABLE_BY_TREES);

        this.tag(DTBlockTags.STRIPPED_BRANCHES)
                .addTag(DTBlockTags.STRIPPED_BRANCHES_THAT_BURN)
                .addTag(DTBlockTags.STRIPPED_FUNGUS_BRANCHES);

        this.tag(BlockTags.ENDERMAN_HOLDABLE)
                .addTag(DTBlockTags.FUNGUS_CAPS);

        this.tag(BlockTags.FLOWER_POTS)
                .add(key(DTRegistries.POTTED_SAPLING.get()));

        Species.REGISTRY.get(DynamicTrees.WARPED).getSapling().ifPresent(sapling ->
                this.tag(BlockTags.HOGLIN_REPELLENTS).add(key(sapling)));

        this.tag(BlockTags.LEAVES)
                .addTag(DTBlockTags.LEAVES);

        this.tag(BlockTags.LOGS)
                .addTag(DTBlockTags.BRANCHES);

        this.tag(BlockItemTags.LOGS_THAT_BURN.block())
                .addTag(DTBlockTags.BRANCHES_THAT_BURN)
                .addTag(DTBlockTags.STRIPPED_BRANCHES_THAT_BURN);

        this.tag(BlockItemTags.SAPLINGS.block())
                .addTag(DTBlockTags.SAPLINGS);

        this.tag(BlockTags.WART_BLOCKS)
                .addTag(DTBlockTags.WART_BLOCKS);

        this.tag(BlockTags.MINEABLE_WITH_AXE)
                .addTag(DTBlockTags.ROOTS)
                .addTag(DTBlockTags.AERIAL_ROOTS_ROOTY_SOIL);
    }

    protected void addDTTags() {
        LeavesProperties.REGISTRY.dataGenerationStream(this.modId).forEach(leavesProperties ->
                leavesProperties.addGeneratedBlockTags(tag -> NeoForgeTagAppender.blocks(this.tag(tag))));

        Family.REGISTRY.dataGenerationStream(this.modId).forEach(family ->
                family.addGeneratedBlockTags(tag -> NeoForgeTagAppender.blocks(this.tag(tag))));

        Species.REGISTRY.dataGenerationStream(this.modId).forEach(species ->
                species.addGeneratedBlockTags(tag -> NeoForgeTagAppender.blocks(this.tag(tag))));

        SoilProperties.REGISTRY.dataGenerationStream(this.modId).forEach(soilProperties ->
                soilProperties.addGeneratedBlockTags(tag -> NeoForgeTagAppender.blocks(this.tag(tag))));
    }

    private static ResourceKey<Block> key(Block block) {
        return ResourceKey.create(Registries.BLOCK, BuiltInRegistries.BLOCK.getKey(block));
    }

    @Override
    public String getName() {
        return this.modId + " DT Block Tags";
    }
}
