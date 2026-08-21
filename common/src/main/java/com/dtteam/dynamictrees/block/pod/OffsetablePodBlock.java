package com.dtteam.dynamictrees.block.pod;

import com.dtteam.dynamictrees.tree.TreeHelper;
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
import org.jetbrains.annotations.Nullable;

public class OffsetablePodBlock extends PodBlock{

    public OffsetablePodBlock(Properties properties, Pod pod) {
        super(properties, pod);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        if (pod != null)
            builder.add(pod.getOffsetProperty());
        super.createBlockStateDefinition(builder);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) return null;
        int rad = TreeHelper.getRadius(context.getLevel(),context.getClickedPos());
        if (pod.isValidRadius(rad))
            return state.setValue(pod.getOffsetProperty(), rad);
        return null;
    }

    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, net.minecraft.world.level.redstone.Orientation orientation, boolean isMoving) {
        BlockPos neighborPos = pos;
        BlockPos fromPos = pos;
        Direction direction = state.getValue(FACING);
        int currentOffset = state.getValue(pod.getOffsetProperty());
        int newOffset = TreeHelper.getRadius(level, pos.offset(direction.getUnitVec3i()));
        if (currentOffset != newOffset && pod.isValidRadius(newOffset)){
            level.setBlock(pos, state.setValue(pod.getOffsetProperty(), newOffset), 2);
        }
        super.neighborChanged(state, level, pos, block, orientation, isMoving);
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction dir = state.getValue(FACING);
        VoxelShape shape = super.getShape(state, level, pos, context);
        float mult = (1f/16) * (8 - state.getValue(pod.getOffsetProperty()));
        return shape.move(dir.getStepX()*mult, dir.getStepY()*mult, dir.getStepZ()*mult);
    }
}
