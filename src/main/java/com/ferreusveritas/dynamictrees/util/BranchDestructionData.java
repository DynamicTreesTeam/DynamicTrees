package com.ferreusveritas.dynamictrees.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ferreusveritas.dynamictrees.blocks.BlockBranchBasic;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch.BlockItemStack;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.property.IExtendedBlockState;
import scala.actors.threadpool.Arrays;

public class BranchDestructionData {
	public final Species species;
	public final long[] destroyedBranches;//Encoded branch shapes
	public final int[] destroyedLeaves;//Encoded leaves positions
	public final List<BlockItemStack> leavesDrops;
	public final List<BlockPos> endPoints;
	public final int woodVolume;
	public final EnumFacing cutDir;
	public final BlockPos cutPos;
	
	public BranchDestructionData(Species species, Map<BlockPos, IExtendedBlockState> branches, Map<BlockPos, IBlockState> leaves, List<BlockItemStack> leavesDrops, List<BlockPos> ends, int volume, BlockPos cutPos, EnumFacing cutDir) {
		this.species = species;
		this.destroyedBranches = convertBranchesToLongArray(branches);
		this.destroyedLeaves = convertLeavesToIntArray(leaves);
		this.leavesDrops = leavesDrops;
		this.endPoints = ends;
		this.woodVolume = volume;
		this.cutPos = cutPos;
		this.cutDir = cutDir;
	}
	
	///////////////////////////////////////////////////////////
	// Branches
	///////////////////////////////////////////////////////////
	
	private long[] convertBranchesToLongArray(Map<BlockPos, IExtendedBlockState> branchList) {
		long data[] = new long[branchList.size()];
		int index = 0;
		
		//Ensure the origin block is at the first index
		IExtendedBlockState origExState = branchList.get(BlockPos.ORIGIN);
		if(origExState != null) {
			data[index++] = encodeBranches(BlockPos.ORIGIN, (BlockBranchBasic) origExState.getBlock(), origExState);
			branchList.remove(BlockPos.ORIGIN);
		}

		//Encode the remaining blocks
		BlockBounds bounds = new BlockBounds(new BlockPos(-64, -64, -64), new BlockPos(64, 64, 64));		
		for(Entry<BlockPos, IExtendedBlockState> set : branchList.entrySet()) {
			BlockPos relPos = set.getKey();
			IExtendedBlockState exState = set.getValue();
			Block block = exState.getBlock();

			if(block instanceof BlockBranchBasic && bounds.inBounds(relPos)) { //Place comfortable limits on the system
				data[index++] = encodeBranches(relPos, (BlockBranchBasic) block, exState);
			}
		}
		
		return Arrays.copyOf(data, index);//Shrink down the array
	}
	
	private long encodeBranches(BlockPos relPos, BlockBranchBasic block, IExtendedBlockState exState) {
		
		//Encode radius and relative block position into the upper 32 bits of long
		int upper = 
				((((BlockBranchBasic)block).getRadius(exState) & 0x1F) << 24) | //Radius 0 - 31
				(((relPos.getX() + 64) & 0xFF) << 16) | 
				(((relPos.getY() + 64) & 0xFF) << 8) |
				(((relPos.getZ() + 64) & 0xFF) << 0) ;
		
		int lower = 0;
		
		//Encode connections into the lower 32 bits of long
		for(EnumFacing face : EnumFacing.values()) {
			int rad = (int) exState.getValue(BlockBranchBasic.CONNECTIONS[face.getIndex()]);
			lower |= (rad & 0x1F) << (face.getIndex() * 5);//5 bits per face * 6 faces = 30bits
		}

		long encoded = (((long)upper) << 32) | lower;
		
		return encoded;
	}
	
	public int getNumBranches() {
		return destroyedBranches.length;
	}
	
	public BlockPos getBranchRelPos(int index) {
		return decodeBranchRelPos(destroyedBranches[index]);
	}
	
	private BlockPos decodeBranchRelPos(long encoded) {
		return new BlockPos(
				(((encoded >> 48) & 0xFF) - 64),
				(((encoded >> 40) & 0xFF) - 64),
				(((encoded >> 32) & 0xFF) - 64)
			);
	}
	
	public int getBranchRadius(int index) {
		return decodeBranchRadius(destroyedBranches[index]);
	}
	
	private int decodeBranchRadius(long encoded) {
		return (int) ((encoded >> 56) & 0x1F); 
	}

	public IExtendedBlockState getBranchBlockState(int index) {
		return decodeBranchBlockState(destroyedBranches[index]);
	}
	
	private IExtendedBlockState decodeBranchBlockState(long encoded) {
		BlockBranch branch = (BlockBranchBasic)species.getFamily().getDynamicBranch();
		if(branch instanceof BlockBranchBasic) {
			IBlockState state = ((BlockBranchBasic)branch).getStateForRadius(decodeBranchRadius(encoded));
			if(state instanceof IExtendedBlockState) {
				IExtendedBlockState exState = (IExtendedBlockState) ((BlockBranchBasic)species.getFamily().getDynamicBranch()).getStateForRadius(decodeBranchRadius(encoded));
				for(EnumFacing face : EnumFacing.values()) {
					int rad = (int) (encoded >> (face.getIndex() * 5) & 0x1F);
					exState = exState.withProperty(BlockBranchBasic.CONNECTIONS[face.getIndex()], MathHelper.clamp(rad, 0, 8));
				}
				return exState;
			}
		}
		
		return null;
	}
	
	///////////////////////////////////////////////////////////
	// Leaves
	///////////////////////////////////////////////////////////

	private int[] convertLeavesToIntArray(Map<BlockPos, IBlockState> leavesList) {
		int data[] = new int[leavesList.size()];
		int index = 0;

		if(leavesList.isEmpty()) {
			data = new int[16];
			data[index++] = encodeLeaves(new BlockPos(0, 4, 0), (BlockDynamicLeaves) species.getLeavesProperties().getDynamicLeavesState().getBlock(), species.getLeavesProperties().getDynamicLeavesState());
			return Arrays.copyOf(data, index);//Shrink down the array
		}
		
		//Encode the remaining blocks
		BlockBounds bounds = new BlockBounds(new BlockPos(-64, -64, -64), new BlockPos(64, 64, 64));		
		for(Entry<BlockPos, IBlockState> set : leavesList.entrySet()) {
			BlockPos relPos = set.getKey();
			IBlockState state = set.getValue();
			Block block = state.getBlock();

			if(block instanceof BlockDynamicLeaves && bounds.inBounds(relPos)) { //Place comfortable limits on the system
				data[index++] = encodeLeaves(relPos, (BlockDynamicLeaves) block, state);
			}
		}
		
		return Arrays.copyOf(data, index);//Shrink down the array
	}

	private int encodeLeaves(BlockPos relPos, BlockDynamicLeaves block, IBlockState state) {
		return	(state.getValue(BlockDynamicLeaves.HYDRO) << 24) |
				(((relPos.getX() + 64) & 0xFF) << 16) | 
				(((relPos.getY() + 64) & 0xFF) << 8) |
				(((relPos.getZ() + 64) & 0xFF) << 0) ;
	}
	
	public int getNumLeaves() {
		return destroyedLeaves.length;
	}
	
	public BlockPos getLeavesRelPos(int index) {
		return decodeBranchRelPos(destroyedLeaves[index]);
	}
	
	private BlockPos decodeBranchRelPos(int encoded) {
		return new BlockPos(
			(((encoded >> 16) & 0xFF) - 64),
			(((encoded >> 8) & 0xFF) - 64),
			(((encoded >> 0) & 0xFF) - 64)
		);
	}
	
	public int getLeavesHydro(int index) {
		return decodeLeavesHydro(destroyedLeaves[index]);
	}
	
	private int decodeLeavesHydro(int encoded) {
		return (encoded >> 24) & 0x0F; 
	}
	
}
