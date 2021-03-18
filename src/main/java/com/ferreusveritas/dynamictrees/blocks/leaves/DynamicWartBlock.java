package com.ferreusveritas.dynamictrees.blocks.leaves;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class DynamicWartBlock extends DynamicLeavesBlock {

    public DynamicWartBlock (final LeavesProperties leavesProperties, final Properties properties) {
        super(leavesProperties, properties);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return getShape(state, worldIn, pos, context);
    }

    @Override
    public boolean addLandingEffects(BlockState state1, ServerWorld worldserver, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
        return false;
    }

    @Override
    public void onFallenUpon(World world, BlockPos pos, Entity entity, float fallDistance) {}

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) { }

    @Override
    protected boolean shouldDropForPlayer(PlayerEntity player) {
        return true;
    }

}
