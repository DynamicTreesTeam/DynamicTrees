package com.ferreusveritas.dynamictrees.systems.nodemappers;

import java.util.ArrayList;
import java.util.List;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import com.ferreusveritas.dynamictrees.util.CompatHelper;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
* Destroys all branches on a tree and the surrounding leaves.
* @author ferreusveritas
*/
public class NodeDestroyer implements INodeInspector {

	Species species;//Destroy any node that's made of the same kind of wood
	private List<BlockPos> endPoints;//We always need to track endpoints during destruction

	public NodeDestroyer(Species species) {
		this.endPoints = new ArrayList<BlockPos>(32);
		this.species = species;
	}

	public List<BlockPos> getEnds() {
		return endPoints;
	}
	
	@Override
	public boolean run(IBlockState blockState, World world, BlockPos pos, EnumFacing fromDir) {
		BlockBranch branch = TreeHelper.getBranch(blockState);

		if(branch != null && species.getFamily() == branch.getFamily()) {
			if(branch.getRadius(blockState, world, pos) == species.getPrimaryThickness()) {
				endPoints.add(pos);
			}
			world.setBlockToAir(pos);//Destroy the branch
		}

		return true;
	}

	@Override
	public boolean returnRun(IBlockState blockState, World world, BlockPos pos, EnumFacing fromDir) {
		return false;
	}

	protected void killSurroundingLeaves(World world, BlockPos twigPos) {
		if (!world.isRemote && !world.restoringBlockSnapshots) { // do not drop items while restoring blockstates, prevents item dupe
			ArrayList<ItemStack> dropList = new ArrayList<ItemStack>();
			TreeFamily tree = species.getFamily();
			for(BlockPos leavesPos : BlockPos.getAllInBox(twigPos.add(-3, -3, -3), twigPos.add(3, 3, 3))) {
				if(tree.isCompatibleGenericLeaves(world.getBlockState(leavesPos), world, leavesPos)) {
					world.setBlockToAir(leavesPos);
					dropList.clear();
					species.getTreeHarvestDrops(world, leavesPos, dropList, world.rand);
					for(ItemStack stack : dropList) {
						EntityItem itemEntity = new EntityItem(world, leavesPos.getX() + 0.5, leavesPos.getY() + 0.5, leavesPos.getZ() + 0.5, stack);
						CompatHelper.spawnEntity(world, itemEntity);
					}
				}
			}
		}
	}

}
