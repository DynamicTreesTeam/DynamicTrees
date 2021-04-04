package com.ferreusveritas.dynamictrees.blocks.leaves;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ToolType;

import net.minecraft.block.AbstractBlock.Properties;

public class DynamicFungusBlock extends DynamicLeavesBlock {

    public DynamicFungusBlock() {
        super(Properties.of(Material.WOOD).randomTicks().strength(0.2F).sound(SoundType.WOOD).harvestTool(ToolType.AXE));
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
    public void fallOn(World world, BlockPos pos, Entity entity, float fallDistance) {}

    @Override
    public void entityInside(BlockState state, World world, BlockPos pos, Entity entity) { }

}
