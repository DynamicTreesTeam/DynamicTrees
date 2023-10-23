package com.ferreusveritas.dynamictrees.systems.nodemapper;

import com.ferreusveritas.dynamictrees.api.network.NodeInspector;
import com.ferreusveritas.dynamictrees.block.branch.BasicRootsBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class RootIntegrityNode implements NodeInspector {

    private final List<BlockPos> endPoints;

    public RootIntegrityNode() { //Array is provided for you
        this(new ArrayList<>(32));
    }

    public RootIntegrityNode(List<BlockPos> ends) { //Or use your own
        this.endPoints = ends;
    }

    @Override
    public boolean run(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {
        return false;
    }

    @Override
    public boolean returnRun(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {
        System.out.println(pos);
        if (state.getBlock() instanceof BasicRootsBlock rootsBlock && rootsBlock.isFullBlock(state)){
            endPoints.add(pos);
            return true;
        }
        return false;
    }

    public List<BlockPos> getStable() {
        return endPoints;
    }

}
