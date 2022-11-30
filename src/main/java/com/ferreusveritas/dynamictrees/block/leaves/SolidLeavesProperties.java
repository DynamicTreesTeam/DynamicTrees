package com.ferreusveritas.dynamictrees.block.leaves;

import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockBehaviour;

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
        this.requiresShears = false;
    }

    @Override
    protected DynamicLeavesBlock createDynamicLeaves(BlockBehaviour.Properties properties) {
        return new SolidDynamicLeavesBlock(this, properties);
    }

}
