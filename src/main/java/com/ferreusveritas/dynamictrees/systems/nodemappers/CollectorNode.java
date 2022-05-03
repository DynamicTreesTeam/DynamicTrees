package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.network.NodeInspector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

/**
 * @author ferreusveritas
 */
public class CollectorNode implements NodeInspector {

    private final Set<BlockPos> nodeSet;

    public CollectorNode(Set<BlockPos> nodeSet) {
        this.nodeSet = nodeSet;
    }

    @Override
    public boolean run(BlockState blockState, LevelAccessor world, BlockPos pos, Direction fromDir) {
        nodeSet.add(pos);
        return false;
    }

    @Override
    public boolean returnRun(BlockState blockState, LevelAccessor world, BlockPos pos, Direction fromDir) {
        return false;
    }

    public boolean contains(BlockPos pos) {
        return nodeSet.contains(pos);
    }

}