package com.ferreusveritas.dynamictrees.blocks.leaves;

import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.data.DTBlockTags;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;

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
    public AbstractBlock.Properties getDefaultBlockProperties(Material material, MaterialColor materialColor) {
        return AbstractBlock.Properties.of(material, materialColor).strength(1.0F).sound(SoundType.WART_BLOCK).randomTicks();
    }

    @Override
    public List<ITag.INamedTag<Block>> defaultLeavesTags() {
        return Collections.singletonList(DTBlockTags.WART_BLOCKS);
    }

}
