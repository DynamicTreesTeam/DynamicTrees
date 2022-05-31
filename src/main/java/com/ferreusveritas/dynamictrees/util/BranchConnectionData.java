package com.ferreusveritas.dynamictrees.util;

import net.minecraft.world.level.block.state.BlockState;

public class BranchConnectionData {

    private final BlockState blockState;
    private final Connections connections;

    public BranchConnectionData(BlockState blockState, Connections connections) {
        this.blockState = blockState;
        this.connections = connections;
    }

    public BlockState getBlockState() {
        return blockState;
    }

    public Connections getConnections() {
        return connections;
    }

}
