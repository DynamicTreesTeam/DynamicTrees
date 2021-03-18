package com.ferreusveritas.dynamictrees.blocks.leaves;

import com.ferreusveritas.dynamictrees.util.ResourceLocationUtils;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.ResourceLocation;

/**
 * @author Harley O'Connor
 */
public final class WartProperties extends LeavesProperties {

    public WartProperties(final ResourceLocation registryName) {
        super(registryName);
    }

    public static class Type extends LeavesProperties.Type {
        @Override
        public LeavesProperties construct(ResourceLocation registryName) {
            return new WartProperties(registryName);
        }
    }

    @Override
    protected ResourceLocation getDynamicLeavesRegName() {
        return ResourceLocationUtils.suffix(this.getRegistryName(), "_wart");
    }

    @Override
    protected DynamicLeavesBlock createDynamicLeaves(AbstractBlock.Properties properties) {
        return new DynamicWartBlock(this, properties);
    }

    @Override
    public AbstractBlock.Properties getDefaultBlockProperties() {
        // TODO: Dynamic material colour setting.
        return AbstractBlock.Properties.create(Material.ORGANIC, MaterialColor.WARPED_WART).hardnessAndResistance(1.0F).sound(SoundType.WART);
    }

}
