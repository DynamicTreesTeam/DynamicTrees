package com.ferreusveritas.dynamictrees.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch.BlockItemStack;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.google.common.collect.AbstractIterator;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.property.IExtendedBlockState;

public class BranchDestructionData {
	public final Species species; // The species of the tree that was harvested
	public final int[] destroyedBranchesRadiusPosition; // Encoded branch radius and relative positions
	public final int[] destroyedBranchesConnections; // Encoded branch shapes
	public final int[] destroyedBranchesBlockIndex; // Encoded valid branch block index for family
	public final int[] destroyedLeaves; // Encoded leaves relative positions
	public final int[] destroyedLeavesBlockIndex; // Encoded valid leaves block index for species
	public final List<BlockItemStack> leavesDrops; // A list of itemstacks and their spawn positions.  Not used on the client.
	public final int[] endPoints; // Encoded endpoint relative positions
	public final float woodVolume; // A summation of all of the wood voxels that was harvested
	public final EnumFacing cutDir; // The face that was connected to the remaining body of the tree or the rooty block
	public final EnumFacing toolDir; // The face that was pounded on when breaking the block at cutPos
	public final BlockPos cutPos; // The absolute(world) position of the block that was cut
	public final int trunkHeight;
	
	public static final BlockBounds bounds = new BlockBounds(new BlockPos(-64, -64, -64), new BlockPos(64, 64, 64));		
	
	public BranchDestructionData() {
		this.species = Species.NULLSPECIES;
		this.destroyedBranchesConnections = new int[0];
		this.destroyedBranchesRadiusPosition = new int[0];
		this.destroyedBranchesBlockIndex = new int[0];
		this.destroyedLeaves = new int[0];
		this.destroyedLeavesBlockIndex = new int[0];
		this.leavesDrops = new ArrayList<>(0);
		this.endPoints = new int[0];
		this.woodVolume = 0;
		this.cutDir = EnumFacing.DOWN;
		this.toolDir = EnumFacing.DOWN;
		this.cutPos = BlockPos.ORIGIN;
		this.trunkHeight = 0;
	}
	
	public BranchDestructionData(Species species, Map<BlockPos, IExtendedBlockState> branches, Map<BlockPos, IBlockState> leaves, List<BlockItemStack> leavesDrops, List<BlockPos> ends, float volume, BlockPos cutPos, EnumFacing cutDir, EnumFacing toolDir, int trunkHeight) {
		this.species = species;
		int[][] encodedBranchData = convertBranchesToIntArrays(branches);
		this.destroyedBranchesRadiusPosition = encodedBranchData[0];
		this.destroyedBranchesConnections = encodedBranchData[1];
		this.destroyedBranchesBlockIndex = encodedBranchData[2];
		int[][] encodedLeavesData = convertLeavesToIntArray(leaves);
		this.destroyedLeaves = encodedLeavesData[0];
		this.destroyedLeavesBlockIndex = encodedLeavesData[1];
		this.leavesDrops = leavesDrops;
		this.endPoints = convertEndPointsToIntArray(ends);
		this.woodVolume = volume;
		this.cutPos = cutPos;
		this.cutDir = cutDir;
		this.toolDir = toolDir;
		this.trunkHeight = trunkHeight; 
	}
	
	public BranchDestructionData(NBTTagCompound nbt) {
		this.species = TreeRegistry.findSpecies(new ResourceLocation(nbt.getString("species")));
		this.destroyedBranchesRadiusPosition = nbt.getIntArray("branchpos");
		this.destroyedBranchesConnections = nbt.getIntArray("branchcon");
		this.destroyedBranchesBlockIndex = nbt.getIntArray("branchblock");
		this.destroyedLeaves = nbt.getIntArray("leaves");
		this.destroyedLeavesBlockIndex = nbt.getIntArray("leavesblock");
		this.leavesDrops = new ArrayList<>();
		this.endPoints = nbt.getIntArray("ends");
		this.woodVolume = nbt.getFloat("volume");
		this.cutPos = new BlockPos(nbt.getInteger("cutx"), nbt.getInteger("cuty"), nbt.getInteger("cutz") );
		this.cutDir = EnumFacing.values()[MathHelper.clamp(nbt.getInteger("cutdir"), 0, EnumFacing.values().length - 1)];
		this.toolDir = EnumFacing.values()[MathHelper.clamp(nbt.getInteger("tooldir"), 0, EnumFacing.values().length - 1)];
		this.trunkHeight = nbt.getInteger("trunkheight");
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		tag.setString("species", species.toString());
		tag.setIntArray("branchpos", destroyedBranchesRadiusPosition);
		tag.setIntArray("branchcon", destroyedBranchesConnections);
		tag.setIntArray("branchblock", destroyedBranchesBlockIndex);
		tag.setIntArray("leaves", destroyedLeaves);
		tag.setIntArray("leavesblock", destroyedLeavesBlockIndex);
		tag.setIntArray("ends", endPoints);
		tag.setFloat("volume", woodVolume);
		tag.setInteger("cutx", cutPos.getX());
		tag.setInteger("cuty", cutPos.getY());
		tag.setInteger("cutz", cutPos.getZ());
		tag.setInteger("cutdir", cutDir.getIndex());
		tag.setInteger("tooldir", toolDir.getIndex());
		tag.setInteger("trunkheight", trunkHeight);
		return tag;
	}
	
	///////////////////////////////////////////////////////////
	// Branches
	///////////////////////////////////////////////////////////
	
	private int[][] convertBranchesToIntArrays(Map<BlockPos, IExtendedBlockState> branchList) {
		int[] relPosData = new int[branchList.size()];
		int[] connectionData = new int[branchList.size()];
		int[] blockIndexData = new int[branchList.size()];
		int index = 0;
		
		//Ensure the origin block is at the first index
		IExtendedBlockState origExState = branchList.get(BlockPos.ORIGIN);
		if(origExState != null) {
			relPosData[index] = encodeBranchesRadiusPos(BlockPos.ORIGIN, (BlockBranch) origExState.getBlock(), origExState);
			connectionData[index] = encodeBranchesConnections(origExState);
			blockIndexData[index++] = encodeBranchBlocks((BlockBranch) origExState.getBlock());
			branchList.remove(BlockPos.ORIGIN);
		}
		
		//Encode the remaining blocks
		for(Entry<BlockPos, IExtendedBlockState> set : branchList.entrySet()) {
			BlockPos relPos = set.getKey();
			IExtendedBlockState exState = set.getValue();
			Block block = exState.getBlock();
			
			if(block instanceof BlockBranch && bounds.inBounds(relPos)) { //Place comfortable limits on the system
				relPosData[index] = encodeBranchesRadiusPos(relPos, (BlockBranch) block, exState);
				connectionData[index] = encodeBranchesConnections(exState);
				blockIndexData[index++] = encodeBranchBlocks((BlockBranch) block);
			}
		}
		
		//Shrink down the arrays
		relPosData = Arrays.copyOf(relPosData, index);
		connectionData = Arrays.copyOf(connectionData, index);
		blockIndexData = Arrays.copyOf(blockIndexData, index);

		return new int[][] { relPosData, connectionData, blockIndexData };
	}
	
	private int encodeBranchesRadiusPos(BlockPos relPos, BlockBranch branchBlock, IBlockState state) {
		return	((branchBlock.getRadius(state) & 0x1F) << 24) | //Radius 0 - 31
				encodeRelBlockPos(relPos);
	}
	
	private int encodeBranchesConnections(IExtendedBlockState exState) {
		int result = 0;
		for(EnumFacing face : EnumFacing.values()) {
			int rad = (int) exState.getValue(BlockBranch.CONNECTIONS[face.getIndex()]);
			result |= (rad & 0x1F) << (face.getIndex() * 5);//5 bits per face * 6 faces = 30bits
		}
		return result;
	}

	private int encodeBranchBlocks (BlockBranch branch) {
		return branch.getFamily().getBranchBlockIndex(branch);
	}

	public int getNumBranches() {
		return destroyedBranchesRadiusPosition.length;
	}
	
	public BlockPos getBranchRelPos(int index) {
		return decodeRelPos(destroyedBranchesRadiusPosition[index]);
	}
	
	public int getBranchRadius(int index) {
		return decodeBranchRadius(destroyedBranchesRadiusPosition[index]);
	}
	
	private int decodeBranchRadius(int encoded) {
		return (encoded >> 24) & 0x1F; 
	}
	
	public IExtendedBlockState getBranchBlockState(int index) {
		return decodeBranchBlockState(destroyedBranchesRadiusPosition[index], destroyedBranchesConnections[index], destroyedBranchesBlockIndex[index]);
	}
	
	private IExtendedBlockState decodeBranchBlockState(int encodedRadPos, int encodedConnections, int encodedBlockIndex) {
		BlockBranch branch = species.getFamily().getValidBranchBlock(encodedBlockIndex);
		if(branch != null) {
			int radius = decodeBranchRadius(encodedRadPos);
			IBlockState state = branch.getStateForRadius(radius);
			if(state instanceof IExtendedBlockState) {
				IExtendedBlockState exState = (IExtendedBlockState) state;
				for(EnumFacing face : EnumFacing.values()) {
					int rad = (encodedConnections >> (face.getIndex() * 5) & 0x1F);
					exState = exState.withProperty(BlockBranch.CONNECTIONS[face.getIndex()], MathHelper.clamp(rad, 0, 8));
				}
				return exState;
			}
		}
		
		return null;
	}
	
	///////////////////////////////////////////////////////////
	// Leaves
	///////////////////////////////////////////////////////////
	
	private int[][] convertLeavesToIntArray(Map<BlockPos, IBlockState> leavesList) {
		int[] posData = new int[leavesList.size()];
		int[] blockIndexData = new int[leavesList.size()];
		int index = 0;
		
		//Encode the remaining blocks
		for(Entry<BlockPos, IBlockState> set : leavesList.entrySet()) {
			BlockPos relPos = set.getKey();
			IBlockState state = set.getValue();
			Block block = state.getBlock();
			
			if(block instanceof BlockDynamicLeaves && bounds.inBounds(relPos)) { // Place comfortable limits on the system
				posData[index] = encodeLeaves(relPos, (BlockDynamicLeaves) block, state);
				blockIndexData[index++] = encodeLeavesBlocks(state, this.species);
			}
		}
		
		posData = Arrays.copyOf(posData, index); // Shrink down the array
		blockIndexData = Arrays.copyOf(blockIndexData, index);

		return new int[][] {posData, blockIndexData};
	}

	private int encodeLeaves(BlockPos relPos, BlockDynamicLeaves block, IBlockState state) {
		return	(state.getValue(BlockDynamicLeaves.HYDRO) << 24) | encodeRelBlockPos(relPos);
	}

	private int encodeLeavesBlocks (IBlockState state, Species species){
		return species.getLeavesBlockIndex(state);
	}

	public int getNumLeaves() {
		return destroyedLeaves.length;
	}
	
	public BlockPos getLeavesRelPos(int index) {
		return decodeLeavesRelPos(destroyedLeaves[index]);
	}
	
	private BlockPos decodeLeavesRelPos(int encoded) {
		return decodeRelPos(encoded);
	}
	
	public int getLeavesHydro(int index) {
		return decodeLeavesHydro(destroyedLeaves[index]);
	}
	
	private int decodeLeavesHydro(int encoded) {
		return (encoded >> 24) & 0x0F; 
	}

	public ILeavesProperties getLeavesProperties (int index) {
		return this.species.getValidLeavesProperties(this.destroyedLeavesBlockIndex[index]);
	}
	
	public IBlockState getLeavesBlockState (int index) {
		return this.species.getValidLeavesBlock(this.destroyedLeavesBlockIndex[index]);
	}
	
	///////////////////////////////////////////////////////////
	// End Points
	///////////////////////////////////////////////////////////
	
	private int[] convertEndPointsToIntArray(List<BlockPos> endPoints) {
		int[] data = new int[endPoints.size()];
		int index = 0;
		
		for(BlockPos relPos : endPoints) {
			if(bounds.inBounds(relPos)) { //Place comfortable limits on the system
				data[index++] = encodeRelBlockPos(relPos);
			}
		}
		
		return Arrays.copyOf(data, index);//Shrink down the array
	}
	
	public int getNumEndpoints() {
		return endPoints.length;
	}
	
	public BlockPos getEndPointRelPos(int index) {
		return decodeRelPos(endPoints[index]);
	}

	
	///////////////////////////////////////////////////////////
	// Position Iteration
	///////////////////////////////////////////////////////////
	
	public static enum PosType {
		BRANCHES,
		LEAVES,
		ENDPOINTS
	}
	
	/**
	 * Get absolute positions of a position type
	 * 
	 * @param posType
	 * @return
	 */
	public Iterable<BlockPos> getPositions(PosType posType) {
		return getPositions(posType, true);
	}

	/**
	 * Get relative or absolute positions of a position type
	 * 
	 * @param posType
	 * @param absolute
	 * @return
	 */
	public Iterable<BlockPos> getPositions(PosType posType, boolean absolute) {
		
		final Function<Integer, BlockPos> getter;
		final int limit;
		
		switch(posType) {
			default:
			case BRANCHES: 
				getter = absolute ? i -> getBranchRelPos(i).add(cutPos) : i -> getBranchRelPos(i);
				limit = getNumBranches();
				break;
			case ENDPOINTS:
				getter = absolute ? i -> getEndPointRelPos(i).add(cutPos) : i -> getEndPointRelPos(i);
				limit = getNumEndpoints();
				break;
			case LEAVES:
				getter = absolute ? i -> getLeavesRelPos(i).add(cutPos) : i -> getLeavesRelPos(i);
				limit = getNumLeaves();
				break;
		}
				
		return new Iterable<BlockPos>() {
			@Override
			public Iterator<BlockPos> iterator() {
				return new AbstractIterator<BlockPos>() {
					private int index = 0;
					@Override
					protected BlockPos computeNext() {
						return index < limit ? getter.apply(index++) : this.endOfData();
					}
				};
			}
		};
	}
	
	
	///////////////////////////////////////////////////////////
	// Generic
	///////////////////////////////////////////////////////////	
	
	private int encodeRelBlockPos(BlockPos relPos) {
		return	(((relPos.getX() + 64) & 0xFF) << 16) | 
				(((relPos.getY() + 64) & 0xFF) << 8) |
				(((relPos.getZ() + 64) & 0xFF) << 0) ;
	}
	
	private BlockPos decodeRelPos(int encoded) {
		return new BlockPos(
			(((encoded >> 16) & 0xFF) - 64),
			(((encoded >> 8) & 0xFF) - 64),
			(((encoded >> 0) & 0xFF) - 64)
		);
	}
	
}
