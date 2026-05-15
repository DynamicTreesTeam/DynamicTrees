package com.dtteam.dynamictrees.block.branch;

import com.dtteam.dynamictrees.registry.DTRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.CreakingHeartBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

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

}
