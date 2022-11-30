package com.ferreusveritas.dynamictrees.block.leaves;

import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.data.DTBlockTags;
import com.ferreusveritas.dynamictrees.data.provider.DTLootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.storage.loot.LootTable;

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
    public Material getDefaultMaterial() {
        return Material.GRASS;
    }

    @Override
    public BlockBehaviour.Properties getDefaultBlockProperties(Material material, MaterialColor materialColor) {
        return BlockBehaviour.Properties.of(material, materialColor).strength(1.0F).sound(SoundType.WART_BLOCK)./*harvestTool(ToolType.HOE).*/randomTicks();
    }

    @Override
    public List<TagKey<Block>> defaultLeavesTags() {
        return Collections.singletonList(DTBlockTags.WART_BLOCKS);
    }

    @Override
    public LootTable.Builder createBlockDrops() {
        return DTLootTableProvider.createWartBlockDrops(primitiveLeaves.getBlock());
    }

    @Override
    public LootTable.Builder createDrops() {
        return DTLootTableProvider.createWartDrops(primitiveLeaves.getBlock());
    }

}
