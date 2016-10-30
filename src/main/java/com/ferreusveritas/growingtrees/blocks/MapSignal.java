package com.ferreusveritas.growingtrees.blocks;

import java.util.ArrayList;

import com.ferreusveritas.growingtrees.inspectors.INodeInspector;
import com.ferreusveritas.growingtrees.special.IBottomListener;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class MapSignal {

	protected ArrayList<INodeInspector> nodeInspectors;
	
	int rootX;
	int rootY;
	int rootZ;
	int depth;
	
	ForgeDirection localRootDir;
	
	boolean overflow;
	boolean found;
	
	public MapSignal(){
		localRootDir = ForgeDirection.UNKNOWN;
		nodeInspectors = new ArrayList<INodeInspector>();
	}
	
	public MapSignal(INodeInspector ... nis){
		this();
		
		for(INodeInspector ni: nis){
			nodeInspectors.add(ni);
		}
	}
	
	public boolean run(World world, Block block, int x, int y, int z, ForgeDirection fromDir){
		for(INodeInspector inspector: nodeInspectors){
			inspector.run(world, block, x, y, z, fromDir);
		}
		return false;
	}
	
	public ArrayList<INodeInspector> getInspectors(){
		return nodeInspectors;
	}
}
