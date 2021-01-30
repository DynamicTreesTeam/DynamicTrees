package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
* Destroys all branches on a tree and the surrounding leaves.
* @author ferreusveritas
*/
public class NodeDestroyer implements INodeInspector {
	
	Species species;//Destroy any node that's made of the same kind of wood
	private List<BlockPos> endPoints;//We always need to track endpoints during destruction
	private PlayerEntity player = null;
	
	public NodeDestroyer(Species species) {
		this.endPoints = new ArrayList<BlockPos>(32);
		this.species = species;
	}

	public NodeDestroyer setPlayer (PlayerEntity player){
		this.player = player;
		return this;
	}
	
	public List<BlockPos> getEnds() {
		return endPoints;
	}
	
	@Override
	public boolean run(BlockState blockState, IWorld world, BlockPos pos, Direction fromDir) {
		//TODO: MAKE DYNAMIC LIST FOR ADDONS
		if ((blockState.getBlock() == Blocks.BEE_NEST || blockState.getBlock() == Blocks.SHROOMLIGHT)){
			if (player != null && world instanceof World){
				TileEntity te = world.getTileEntity(pos);
				blockState.getBlock().removedByPlayer(blockState, (World) world, pos, player, true, world.getFluidState(pos));
				blockState.getBlock().harvestBlock((World) world, player, pos, blockState, te, player.getHeldItemMainhand());
			} else {
				world.removeBlock(pos, false);
			}

		}

		BranchBlock branch = TreeHelper.getBranch(blockState);
		
		if(branch != null && species.getFamily() == branch.getFamily()) {
			if(branch.getRadius(blockState) == species.getFamily().getPrimaryThickness()) {
				endPoints.add(pos);
			}
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);//Destroy the branch and notify the client
		}
		
		return true;
	}
	
	@Override
	public boolean returnRun(BlockState blockState, IWorld world, BlockPos pos, Direction fromDir) {
		return false;
	}
	
}
