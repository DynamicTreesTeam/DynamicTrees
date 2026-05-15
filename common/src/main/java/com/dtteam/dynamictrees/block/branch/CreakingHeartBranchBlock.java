package com.dtteam.dynamictrees.block.branch;

import com.dtteam.dynamictrees.api.network.MapSignal;
import com.dtteam.dynamictrees.block.CreakingHeartBranchState;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.family.AltBranchFamily;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;

public class CreakingHeartBranchBlock extends BasicBranchBlock {

    public static final EnumProperty<CreakingHeartBranchState> STATE = EnumProperty.create("creaking_heart_state", CreakingHeartBranchState.class);

    public CreakingHeartBranchBlock(Identifier name, Properties properties) {
        super(name, properties);
        registerDefaultState(defaultBlockState().setValue(STATE, CreakingHeartBranchState.DORMANT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STATE);
        super.createBlockStateDefinition(builder);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        ticks.scheduleTick(pos, this, 1);
        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockState newState = updateState(state, level, pos);
        if (newState != state) {
            level.setBlock(pos, newState, 3);
        }
    }

    public static BlockState updateState(BlockState state, Level level, BlockPos pos) {
        boolean shouldAwake = level.environmentAttributes().getValue(EnvironmentAttributes.CREAKING_ACTIVE, pos);
        return state.setValue(STATE, shouldAwake ? CreakingHeartBranchState.AWAKE : CreakingHeartBranchState.DORMANT);
    }

    @Override
    public BlockState getStateForRadius(int radius, BlockState previousState) {
        BlockState state = super.getStateForRadius(radius, previousState);
        if (previousState.hasProperty(CreakingHeartBranchBlock.STATE)){
            return state.setValue(CreakingHeartBranchBlock.STATE, previousState.getValue(CreakingHeartBranchBlock.STATE));
        }
        return state;
    }

    @Override
    public float getHardness(BlockState state, BlockGetter level, BlockPos pos) {
        return super.getHardness(state, level, pos);
    }

    @Override
    public Optional<Block> getPrimitiveLog() {
        if (getFamily() instanceof AltBranchFamily altLogFamily)
            return altLogFamily.getPrimitiveAltLog();
        return super.getPrimitiveLog();
    }

    @Override
    protected SoundType getSoundType(BlockState state) {
        return getPrimitiveLog().map(block -> block.defaultBlockState().getSoundType()).orElseGet(() -> super.getSoundType(state));
    }

    /**
     * We unfortunately cannot use {@link BranchBlock#analyse(BlockState, LevelAccessor, BlockPos, Direction, MapSignal)}
     * As it requires a {@link LevelAccessor} and we only have a {@link BlockGetter}.
     * BFS
     */
    @Nullable
    public static BlockPos findFromBranch(BlockState state, BlockGetter level, BlockPos pos, int stepsLeft, HashSet<BlockPos> explored, @Nullable Direction from){
        if (state.getBlock() instanceof CreakingHeartBranchBlock) return pos;
        if (stepsLeft <= 0) return null;
        explored.add(pos);
        for (Direction dir : Direction.values()){
            if (dir == from) continue;
            BlockPos sidePos = pos.offset(dir.getUnitVec3i());
            if (explored.contains(sidePos)) continue;
            BlockState sideState = level.getBlockState(sidePos);
            if (TreeHelper.isBranch(sideState)){
                BlockPos foundPos = findFromBranch(sideState, level, sidePos, stepsLeft-1, explored, dir.getOpposite());
                if (foundPos != null) return foundPos;
            }
        }
        return null;
    }
    @Nullable
    public static BlockPos findFromBranch(BlockState state, BlockGetter level, BlockPos pos, int stepsLeft){
        return findFromBranch(state, level, pos, stepsLeft, new HashSet<>(), null);
    }

}
