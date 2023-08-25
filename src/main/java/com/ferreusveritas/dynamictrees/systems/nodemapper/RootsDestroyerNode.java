package com.ferreusveritas.dynamictrees.systems.nodemapper;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.block.branch.BasicRootsBlock;
import com.ferreusveritas.dynamictrees.tree.family.MangroveFamily;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import javax.annotation.Nullable;

public class RootsDestroyerNode extends FindEndsNode {

    final MangroveFamily family;
    public RootsDestroyerNode(MangroveFamily family) {
        super();
        this.family = family;
    }

    @Override
    public boolean run(BlockState state, LevelAccessor level, BlockPos pos, @Nullable Direction fromDir) {
        BranchBlock branch = TreeHelper.getBranch(state);

        if (branch != null) {
            boolean waterlogged = state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED);
            BasicRootsBlock.Layer layer = state.hasProperty(BasicRootsBlock.LAYER) ? state.getValue(BasicRootsBlock.LAYER) : BasicRootsBlock.Layer.EXPOSED;
            Block primitive = (layer == BasicRootsBlock.Layer.COVERED && layer.getPrimitive(family).isPresent()) ? layer.getPrimitive(family).get() : Blocks.AIR;

            level.setBlock(pos, waterlogged ? Blocks.WATER.defaultBlockState() : primitive.defaultBlockState(), 3);//Destroy the branch and notify the client
        }

        return super.run(state, level, pos, fromDir);
    }

    @Override
    public boolean returnRun(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {
        return super.returnRun(state, level, pos, fromDir);
    }
}
