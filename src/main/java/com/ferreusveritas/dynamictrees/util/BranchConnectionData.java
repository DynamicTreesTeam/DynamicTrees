package com.ferreusveritas.dynamictrees.util;

import net.minecraft.block.BlockState;

public class BranchConnectionData {
	
	private BlockState blockState;
	private Connections connections;
	
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
