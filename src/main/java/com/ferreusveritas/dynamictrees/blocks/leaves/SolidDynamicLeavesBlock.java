package com.ferreusveritas.dynamictrees.blocks.leaves;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * An extension of {@link DynamicLeavesBlock} which makes the block solid. This means that it can be landed on like
 * normal and gives fall damage, is a full cube, and isn't made passable when the config option is enabled.
 */
public class SolidDynamicLeavesBlock extends DynamicLeavesBlock {

    public SolidDynamicLeavesBlock(final LeavesProperties leavesProperties, final Properties properties) {
        super(leavesProperties, properties);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public boolean addLandingEffects(BlockState state1, ServerLevel worldserver, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
        return false;
    }

    @Override
    public void fallOn(Level world, BlockState blockstate, BlockPos pos, Entity entity, float fallDistance) {
        entity.causeFallDamage(fallDistance, 1.0F, DamageSource.FALLING_BLOCK);
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
    }

}
