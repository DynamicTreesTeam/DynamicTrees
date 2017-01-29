package com.ferreusveritas.growingtrees.inspectors;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class NodeCoder implements INodeInspector {

	ArrayList<Integer> instructions;
	
	public NodeCoder(){
		instructions = new ArrayList<Integer>();
	}
	
	@Override
	public boolean run(World world, Block block, int x, int y, int z, ForgeDirection fromDir) {
		return false;
	}

}
