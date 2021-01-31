package com.ferreusveritas.dynamictrees.systems;

import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import java.util.HashMap;
import java.util.Map;

public class BranchConnectables {

    private static Map<Block, RadiusForConnectionFunction> connectablesMap = new HashMap<>();

    public static void makeBlockConnectable (Block block, RadiusForConnectionFunction radiusFunction){
        connectablesMap.putIfAbsent(block, radiusFunction);
    }
    public static boolean isBlockConnectable (Block block){
        return connectablesMap.containsKey(block);
    }
    public static int getConnectionRadiusForBlock (BlockState blockState, IBlockReader world, BlockPos pos, BranchBlock from, Direction side, int fromRadius){
        Block block = blockState.getBlock();
        if (connectablesMap.containsKey(block))
            return connectablesMap.get(block).apply(blockState, world, pos, from, side, fromRadius);
        return 0;
    }

    public interface RadiusForConnectionFunction {

        int apply (BlockState blockState, IBlockReader world, BlockPos pos, BranchBlock from, Direction side, int fromRadius);

    }

}
