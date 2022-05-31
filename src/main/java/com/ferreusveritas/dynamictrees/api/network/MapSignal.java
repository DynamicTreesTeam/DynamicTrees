package com.ferreusveritas.dynamictrees.api.network;

import com.ferreusveritas.dynamictrees.systems.nodemappers.CollectorNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;

public class MapSignal {

    protected ArrayList<NodeInspector> nodeInspectors;

    public BlockPos root;
    public int depth;

    public boolean multiroot = false;
    public boolean destroyLoopedNodes = true;
    public boolean trackVisited = false;

    public Direction localRootDir;

    public boolean overflow;
    public boolean foundRoot;

    public MapSignal() {
        localRootDir = null;
        nodeInspectors = new ArrayList<>();
    }

    public MapSignal(NodeInspector... nis) {
        this();

        nodeInspectors.addAll(Arrays.asList(nis));
    }

    public boolean run(BlockState blockState, LevelAccessor world, BlockPos pos, @Nullable Direction fromDir) {
        for (NodeInspector inspector : nodeInspectors) {
            inspector.run(blockState, world, pos, fromDir);
        }
        return false;
    }

    public boolean returnRun(BlockState blockState, LevelAccessor world, BlockPos pos, Direction fromDir) {
        for (NodeInspector inspector : nodeInspectors) {
            inspector.returnRun(blockState, world, pos, fromDir);
        }
        return false;
    }

    public ArrayList<NodeInspector> getInspectors() {
        return nodeInspectors;
    }

    public boolean doTrackingVisited(BlockPos pos) {
        if (nodeInspectors.size() > 0) {
            final NodeInspector inspector = nodeInspectors.get(0);

            if (inspector instanceof CollectorNode) {
                return ((CollectorNode) inspector).contains(pos);
            }
        }
        return false;
    }

}
