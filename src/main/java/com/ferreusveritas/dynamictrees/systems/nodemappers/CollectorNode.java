package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import java.util.Set;

/**
 * @author ferreusveritas
 */
public class CollectorNode implements INodeInspector {

    private Set<BlockPos> nodeSet;

    public CollectorNode(Set<BlockPos> nodeSet) {
        this.nodeSet = nodeSet;
    }

    @Override
    public boolean run(BlockState blockState, IWorld world, BlockPos pos, Direction fromDir) {
        nodeSet.add(pos);
        return false;
    }

    @Override
    public boolean returnRun(BlockState blockState, IWorld world, BlockPos pos, Direction fromDir) {
        return false;
    }

    public boolean contains(BlockPos pos) {
        return nodeSet.contains(pos);
    }

}