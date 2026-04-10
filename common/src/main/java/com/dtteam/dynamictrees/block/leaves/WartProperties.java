package com.dtteam.dynamictrees.block.leaves;

import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.data.DTLootTableBuilder;
import com.dtteam.dynamictrees.data.tags.DTBlockTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Collections;
import java.util.List;

/**
 * @author Harley O'Connor
 */
public class WartProperties extends SolidLeavesProperties {

    public static final TypedRegistry.EntryType<LeavesProperties> TYPE = TypedRegistry.newType(WartProperties::new);

    public WartProperties(final Identifier registryName) {
        super(registryName);
    }

    @Override
    protected String getBlockRegistryNameSuffix() {
        return "_wart";
    }

    @Override
    public BlockBehaviour.Properties getDefaultBlockProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_RED)
                .strength(1.0F)
                .sound(SoundType.WART_BLOCK)
                .randomTicks();
    }

    @Override
    public List<TagKey<Block>> defaultLeavesTags() {
        return Collections.singletonList(DTBlockTags.WART_BLOCKS);
    }

    @Override
    public LootTable.Builder createBlockDrops(HolderLookup.Provider registries) {
        return DTLootTableBuilder.createWartBlockDrops(primitiveLeaves.getBlock(), registries);
    }

    @Override
    public LootTable.Builder createDrops(HolderLookup.Provider registries) {
        return DTLootTableBuilder.createWartDrops(primitiveLeaves.getBlock(), registries);
    }

}
