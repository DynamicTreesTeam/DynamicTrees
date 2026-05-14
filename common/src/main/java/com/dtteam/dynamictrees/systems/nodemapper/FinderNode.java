package com.dtteam.dynamictrees.systems.nodemapper;

import com.dtteam.dynamictrees.api.network.NodeInspector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;
import java.util.function.Predicate;

public class FinderNode implements NodeInspector {

    private final Set<BlockPos> nodeSet;
    private final Predicate<BlockState> condition;

    public FinderNode(Set<BlockPos> nodeSet, Predicate<BlockState> condition) {
        this.nodeSet = nodeSet;
        this.condition = condition;
    }

    @Override
    public boolean run(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {
        if (condition.test(state)){
            nodeSet.add(pos);
        }
        return false;
    }

    @Override
    public boolean returnRun(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {
        return false;
    }

    public int count() {
        return nodeSet.size();
    }

    public boolean found() {
        return count() > 0;
    }

}