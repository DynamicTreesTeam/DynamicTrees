package com.ferreusveritas.dynamictrees.block;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.systems.pod.Pod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class OffsetablePodBlock extends PodBlock{

    public OffsetablePodBlock(Properties properties, Pod pod) {
        super(properties, pod);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        if (pod != null)
            builder.add(pod.getOffsetProperty());
        super.createBlockStateDefinition(builder);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) return null;
        int rad = TreeHelper.getRadius(context.getLevel(),context.getClickedPos());
        if (pod.isValidRadius(rad))
            return state.setValue(pod.getOffsetProperty(), rad);
        return null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        Direction direction = state.getValue(FACING);
        int currentOffset = state.getValue(pod.getOffsetProperty());
        int newOffset = TreeHelper.getRadius(level, pos.offset(direction.getNormal()));
        if (currentOffset != newOffset && pod.isValidRadius(newOffset)){
            level.setBlock(pos, state.setValue(pod.getOffsetProperty(), newOffset), 2);
        }
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction dir = state.getValue(FACING);
        VoxelShape shape = super.getShape(state, level, pos, context);
        float mult = (1f/16) * (8 - state.getValue(pod.getOffsetProperty()));
        return shape.move(dir.getStepX()*mult, dir.getStepY()*mult, dir.getStepZ()*mult);
    }
}
