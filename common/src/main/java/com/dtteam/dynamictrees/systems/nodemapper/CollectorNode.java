package com.dtteam.dynamictrees.systems.nodemapper;

import com.dtteam.dynamictrees.api.function.TriPredicate;
import com.dtteam.dynamictrees.api.network.NodeInspector;
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
    private final TriPredicate<BlockState, LevelAccessor, BlockPos> predicate;

    public CollectorNode(Set<BlockPos> nodeSet, TriPredicate<BlockState, LevelAccessor, BlockPos> predicate) {
        this.nodeSet = nodeSet;
        this.predicate = predicate;
    }

    public CollectorNode(Set<BlockPos> nodeSet) {
        this(nodeSet, (_,_,_)->true);
    }

    @Override
    public boolean run(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {
        if (predicate.test(state, level, pos)){
            nodeSet.add(pos);
        }
        return false;
    }

    @Override
    public boolean returnRun(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {
        return false;
    }

    public boolean contains(BlockPos pos) {
        return nodeSet.contains(pos);
    }

}