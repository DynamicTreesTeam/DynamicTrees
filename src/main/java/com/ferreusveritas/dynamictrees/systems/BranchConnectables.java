package com.ferreusveritas.dynamictrees.systems;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import java.util.HashMap;
import java.util.Map;

/**
 * Allows branches connect to non-tree blocks
 *
 * @author Max Hyper
 */
public class BranchConnectables {

    private static final Map<Block, RadiusForConnectionFunction> connectablesMap = new HashMap<>();

    public static void makeBlockConnectable (Block block, RadiusForConnectionFunction radiusFunction){
        connectablesMap.putIfAbsent(block, radiusFunction);
    }

    public static boolean isBlockConnectable (Block block){
        return connectablesMap.containsKey(block);
    }

    public static int getConnectionRadiusForBlock (BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        final Block block = state.getBlock();
        return isBlockConnectable(block) ? connectablesMap.get(block).apply(state, world, pos, side) : 0;
    }

    public interface RadiusForConnectionFunction {

        int apply (BlockState blockState, IBlockReader world, BlockPos pos, Direction side);

    }

}
