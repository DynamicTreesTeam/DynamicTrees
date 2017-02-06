package com.ferreusveritas.growingtrees.inspectors;

import com.ferreusveritas.growingtrees.TreeHelper;
import com.ferreusveritas.growingtrees.blocks.BlockBranch;
import com.ferreusveritas.growingtrees.blocks.ITreePart;
import com.ferreusveritas.growingtrees.util.SimpleVoxmap;
import com.ferreusveritas.growingtrees.util.Vec3d;

import net.minecraft.block.Block;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class NodeInflator implements INodeInspector {

	private float radius;
	private Vec3d last;
	
	SimpleVoxmap leafMap;
	
	public NodeInflator(SimpleVoxmap leafMap) {
		this.leafMap = leafMap;
		last = new Vec3d(0, -1, 0);
	}
	
	@Override
	public boolean run(World world, Block block, int x, int y, int z, ForgeDirection fromDir) {
		BlockBranch branch = TreeHelper.getBranch(block);
		
		if(branch != null){
			radius = 1.0f;
		}

		return false;
	}

	@Override
	public boolean returnRun(World world, Block block, int x, int y, int z, ForgeDirection fromDir) {
		//Calculate Branch Thickness based on neighboring branches

		BlockBranch branch = TreeHelper.getBranch(block);
		
		if(branch != null){
			float areaAccum = radius * radius;//Start by accumulating the branch we just came from
			boolean isTwig = true;
			
			Vec3d d = new Vec3d();//delta coordinates
			
			for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS) {
				if(!dir.equals(fromDir)){//Don't count where the signal originated from
					
					d.set(x, y, z).add(dir);

					if(d.equals(last)){//or the branch we just came back from
						isTwig = false;//on the return journey if the block we just came from is a branch we are obviously not the endpoint(twig)
						continue;
					}

					ITreePart treepart = TreeHelper.getTreePart(world, d.x, d.y, d.z);
					if(branch.isSameWood(treepart)){
						int branchRadius = treepart.getRadius(world, d.x, d.y, d.z);
						areaAccum += branchRadius * branchRadius;
					}
				}
			}

			if(isTwig){
				//Handle leaves here
				leafMap.setVoxel(x, y, z, (byte) 16);
				SimpleVoxmap leafCluster = branch.getTree().getLeafCluster();
				leafMap.BlitMax(x, y, z, leafCluster);
			} else {
				//The new branch should be the square root of all of the sums of the areas of the branches coming into it.
				radius = (float)Math.sqrt(areaAccum) + branch.getTree().getTapering();

				//Make sure that non-twig branches are at least radius 2
				float secondaryThickness = branch.getTree().secondaryThickness;
				if(radius < secondaryThickness){
					radius = secondaryThickness;
				}

				branch.setRadius(world, x, y, z, (int)Math.floor(radius));
				leafMap.setVoxel(x, y, z, (byte) 32);
			}

			last.set(x, y, z);
		}

		return false;
	}
	
}
