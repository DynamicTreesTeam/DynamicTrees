package com.dtteam.dynamictrees.block.branch;

import com.dtteam.dynamictrees.registry.DTRegistries;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.family.CreakingHeartFamily;
import com.dtteam.dynamictrees.tree.family.Family;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.CreakingHeartBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CreakingHeartBranchBlockEntity extends CreakingHeartBlockEntity {

    public CreakingHeartBranchBlockEntity(BlockPos worldPosition, BlockState blockState) {
        //We pass the creaking heart as a dummy to clear the check, but it's not saved.
        super(worldPosition, Blocks.CREAKING_HEART.defaultBlockState());
        modifyType();
        modifyBlockState(blockState);
    }

    protected void modifyType(){
        this.type = DTRegistries.CREAKING_HEART_BLOCK_ENTITY.get();
    }

    private void modifyBlockState(BlockState blockState) {
        validateBlockState(blockState);
        this.blockState = blockState;
    }

    private void validateBlockState(BlockState blockState) {
        if (!this.isValidBlockState(blockState)) {
            String name = this.getNameForReporting();
            throw new IllegalStateException("Invalid block entity " + name + " state at " + this.worldPosition + ", got " + blockState);
        }
    }

    @Nullable
    public CreakingHeartFamily getHeartFamily(Level level){
        CreakingHeartBranchBlock branch = getHeartBranch(level);
        if (branch == null) return null;
        if (branch.getFamily() instanceof CreakingHeartFamily heartFamily)
            return heartFamily;
        return null;
    }

    @Nullable
    public CreakingHeartBranchBlock getHeartBranch(Level level){
        if (TreeHelper.getBranch(level.getBlockState(getBlockPos())) instanceof CreakingHeartBranchBlock heartBranch)
            return heartBranch;
        return null;
    }

    @Nullable
    public BranchBlock getStandardBranch(Level level){
        Family family = getHeartFamily(level);
        if (family == null) return null;
        return family.getBranch().orElse(null);
    }

    /**
     * We do this to prevent minecraft from adding 2-3 instead of 1, doing 3 makes it spread too much.
     * Instead, we drop 2-3 items from the branch :)
     */
    private long lastTickSpread;

    @Override
    protected Optional<BlockPos> spreadResin(ServerLevel level) {
        if (lastTickSpread == ticksExisted) return Optional.empty();
        RandomSource random = level.getRandom();
        Mutable<BlockPos> placedResin = new MutableObject<>(null);
        BlockPos.breadthFirstTraversal(this.worldPosition, 2, 64, (pos, acceptor) -> {
            for(Direction dir : Util.shuffledCopy(Direction.values(), random)) {
                BlockPos neighbourPos = pos.relative(dir);
                if (TreeHelper.isBranch(level.getBlockState(neighbourPos))) {
                    acceptor.accept(neighbourPos);
                }
            }
        }, (pos) -> {
            BlockState branchState = level.getBlockState(pos);
            if (TreeHelper.getBranch(branchState) == getStandardBranch(level)) {
                CreakingHeartBranchBlock heart = getHeartBranch(level);
                if (heart != null) {
                    heart.addResinToBranch(branchState, level, pos);
                    placedResin.setValue(pos);
                    lastTickSpread = ticksExisted;
                    return BlockPos.TraversalNodeStatus.STOP;
                }
            }
            return BlockPos.TraversalNodeStatus.ACCEPT;
        });
        return Optional.ofNullable(placedResin.get());
    }
}
