package com.ferreusveritas.dynamictrees.blocks.leaves;

import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.util.ResourceLocation;

/**
 * An extension of {@link LeavesProperties} which provides {@link SolidDynamicLeavesBlock} for a solid version of {@link
 * DynamicLeavesBlock}.
 *
 * @author Harley O'Connor
 */
public class SolidLeavesProperties extends LeavesProperties {

    public static final TypedRegistry.EntryType<LeavesProperties> TYPE = TypedRegistry.newType(SolidLeavesProperties::new);

    public SolidLeavesProperties(ResourceLocation registryName) {
        super(registryName);
        this.canBeSheared = false;
    }

    @Override
    protected DynamicLeavesBlock createDynamicLeaves(AbstractBlock.Properties properties) {
        return new SolidDynamicLeavesBlock(this, properties);
    }

}
