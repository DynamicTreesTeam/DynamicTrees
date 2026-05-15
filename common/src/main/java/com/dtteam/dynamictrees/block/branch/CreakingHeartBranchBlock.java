package com.dtteam.dynamictrees.block.branch;

import com.dtteam.dynamictrees.api.network.MapSignal;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.family.AltBranchFamily;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CreakingHeartBranchBlock extends BasicBranchBlock {

    public CreakingHeartBranchBlock(Identifier name, Properties properties) {
        super(name, properties);
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
