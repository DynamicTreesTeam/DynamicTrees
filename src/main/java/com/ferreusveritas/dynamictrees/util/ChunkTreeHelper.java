package com.ferreusveritas.dynamictrees.util;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree.DestroyType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ChunkTreeHelper {
	
	public static void removeOrphanedBranchNodes(World world, ChunkPos cPos, int radius) {
		
		if(cPos == null) {
			return;
		}
		
		Chunk chunk = world.getChunkFromChunkCoords(cPos.x, cPos.z);
		BlockBounds bounds = new BlockBounds(cPos);
		
		bounds.shrink(EnumFacing.UP, 255 - (chunk.getTopFilledSegment() + 16));
		for(EnumFacing dir: EnumFacing.HORIZONTALS) {
			bounds.expand(dir, radius * 16);
		}
		
		Set<BlockPos> found = new HashSet<>();
		
		for(MutableBlockPos pos: bounds.iterate()) {
			if(found.contains(pos)) {
				continue;
			}
			IBlockState state = world.getBlockState(pos);
			Optional<BlockBranch> branchBlock = TreeHelper.getBranchOpt(state);
			if(branchBlock.isPresent()) {
				BlockPos rootPos = TreeHelper.findRootNode(world, pos);
				if(rootPos == BlockPos.ORIGIN) {// If the root position is the ORIGIN object it means that no root block was found
					// If the root node isn't found then all nodes are orphan.  Destroy the entire network.
					BranchDestructionData destroyData = branchBlock.get().destroyBranchFromNode(world, pos, EnumFacing.DOWN, true);
					EntityFallingTree.dropTree(world, destroyData, new ArrayList<ItemStack>(0), DestroyType.ROOT);
				} else {
					// There is at least one root block in the network
					IBlockState rootyState = world.getBlockState(rootPos);
					Optional<BlockRooty> rootyBlock = TreeHelper.getRootyOpt(rootyState);
					if(rootyBlock.isPresent()) { // Rooty block confirmed
						EnumFacing trunkDir = rootyBlock.get().getTrunkDirection(world, rootPos);
						BlockPos trunkPos = rootPos.offset(trunkDir);
						IBlockState trunkState = world.getBlockState(trunkPos);
						Optional<BlockBranch> trunk = TreeHelper.getBranchOpt(trunkState);
						
						if(trunk.isPresent()) { // There's a trunk coming out of the rooty block, that's kinda expected
							MapSignal signal = new MapSignal();
							signal.destroyLoopedNodes = false;
							trunk.get().analyse(trunkState, world, trunkPos, null, signal);
							
							if(signal.multiroot || signal.overflow) { // We found multiple root nodes.  This can't be resolved. Destroy the entire network
								BranchDestructionData destroyData = branchBlock.get().destroyBranchFromNode(world, pos, EnumFacing.DOWN, true);
								EntityFallingTree.dropTree(world, destroyData, new ArrayList<ItemStack>(0), DestroyType.ROOT);//Destroy the tree client side without fancy effects
								continue;
							}
						}
						
					}
				}
			}
		}
		
	}
	
	public static void removeAllBranchesFromChunk(World world, ChunkPos cPos, int radius) {
		
		if(cPos == null) {
			return;
		}
		
		Chunk chunk = world.getChunkFromChunkCoords(cPos.x, cPos.z);
		BlockBounds bounds = new BlockBounds(cPos);
		
		bounds.shrink(EnumFacing.UP, 255 - (chunk.getTopFilledSegment() + 16));
		for(EnumFacing dir: EnumFacing.HORIZONTALS) {
			bounds.expand(dir, radius * 16);
		}
		
		for(MutableBlockPos pos: bounds.iterate()) {
			IBlockState state = world.getBlockState(pos);
			Optional<BlockBranch> branchBlock = TreeHelper.getBranchOpt(state);
			if(branchBlock.isPresent()) {
				BranchDestructionData destroyData = branchBlock.get().destroyBranchFromNode(world, pos, EnumFacing.DOWN, true);
				EntityFallingTree.dropTree(world, destroyData, new ArrayList<ItemStack>(0), DestroyType.ROOT);// Destroy the tree client side without fancy effects
			}
		}
		
	}
	
}
