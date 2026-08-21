package com.dtteam.dynamictrees.block.leaves;

import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/**
 * An extension of {@link LeavesProperties} which provides {@link SolidDynamicLeavesBlock} for a solid version of {@link
 * DynamicLeavesBlock}.
 *
 * @author Harley O'Connor
 */
public class SolidLeavesProperties extends LeavesProperties {

    public static final TypedRegistry.EntryType<LeavesProperties> TYPE = TypedRegistry.newType(SolidLeavesProperties::new);

    public SolidLeavesProperties(Identifier registryName) {
        super(registryName);
        this.requiresShears = false;
    }

    public BlockBehaviour.Properties getDefaultBlockProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .ignitedByLava()
                .strength(0.2F)
                .randomTicks()
                .sound(SoundType.GRASS)
                .forceSolidOn();
    }

    protected DynamicLeavesBlock createDynamicLeaves(BlockBehaviour.Properties properties) {
        return new SolidDynamicLeavesBlock(this, properties);
    }

}
