package com.ferreusveritas.dynamictrees.systems;

import com.ferreusveritas.dynamictrees.util.function.TetraFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Allows branches connect to non-tree blocks
 *
 * @author Max Hyper
 */
public class BranchConnectables {

    private static final Map<Block, TetraFunction<BlockState, BlockGetter, BlockPos, Direction, Integer>> connectablesMap = new HashMap<>();

    //Direction can be null
    public static void makeBlockConnectable(Block block, TetraFunction<BlockState, BlockGetter, BlockPos, Direction, Integer> radiusFunction) {
        connectablesMap.putIfAbsent(block, radiusFunction);
    }

    public static boolean isBlockConnectable(Block block) {
        return connectablesMap.containsKey(block);
    }

    public static int getConnectionRadiusForBlock(BlockState state, BlockGetter world, BlockPos pos, @Nullable Direction side) {
        final Block block = state.getBlock();
        return isBlockConnectable(block) ? connectablesMap.get(block).apply(state, world, pos, side) : 0;
    }

}
