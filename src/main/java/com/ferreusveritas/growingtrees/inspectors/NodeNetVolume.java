package com.ferreusveritas.growingtrees.inspectors;

import com.ferreusveritas.growingtrees.TreeHelper;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class NodeNetVolume implements INodeInspector {

	private int volume;//number of voxels(1x1x1 pixels) of wood accumulated from network analysis
	
	@Override
	public boolean run(World world, Block block, int x, int y, int z, ForgeDirection fromDir) {
		int radius = TreeHelper.getSafeTreePart(block).getRadius(world, x, y, z);
		volume += radius * radius * 64;//Integrate volume of this tree part into the total volume calculation
		return true;
	}

	public int getVolume(){
		return volume;
	}
	
}
