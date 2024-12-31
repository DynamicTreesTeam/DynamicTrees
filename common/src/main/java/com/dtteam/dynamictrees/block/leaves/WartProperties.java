package com.dtteam.dynamictrees.block.leaves;

import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.data.tags.DTBlockTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.Collections;
import java.util.List;

/**
 * @author Harley O'Connor
 */
public class WartProperties extends SolidLeavesProperties {

    public static final TypedRegistry.EntryType<LeavesProperties> TYPE = TypedRegistry.newType(WartProperties::new);

    public WartProperties(final ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected String getBlockRegistryNameSuffix() {
        return "_wart";
    }

    @Override
    public MapColor getDefaultMapColor() {
        return MapColor.GRASS;
    }

    @Override
    public BlockBehaviour.Properties getDefaultBlockProperties(MapColor mapColor) {
        return BlockBehaviour.Properties.of().mapColor(mapColor).strength(1.0F).sound(SoundType.WART_BLOCK).randomTicks();
    }

    @Override
    public List<TagKey<Block>> defaultLeavesTags() {
        return Collections.singletonList(DTBlockTags.WART_BLOCKS);
    }

//    @Override
//    public LootTable.Builder createBlockDrops() {
//        return DTLootTableProvider.BlockLoot.createWartBlockDrops(primitiveLeaves.getBlock());
//    }
//
//    @Override
//    public LootTable.Builder createDrops() {
//        return DTLootTableProvider.BlockLoot.createWartDrops(primitiveLeaves.getBlock());
//    }

}
