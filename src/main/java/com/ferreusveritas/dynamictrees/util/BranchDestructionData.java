package com.ferreusveritas.dynamictrees.util;

import com.google.common.collect.AbstractIterator;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

public class BranchDestructionData {
//	public final Species species;//The species of the tree that was harvested
//	public final int[] destroyedBranchesRadiusPosition;//Encoded branch radius and relative positions
//	public final int[] destroyedBranchesConnections;//Encoded branch shapes
//	public final int[] destroyedLeaves;//Encoded leaves relative positions
//	public final List<BlockItemStack> leavesDrops;//A list of itemstacks and their spawn positions.  Not used on the client.
//	public final int[] endPoints;//Encoded endpoint relative positions
//	public final float woodVolume;//A summation of all of the wood voxels that was harvested
//	public final Direction cutDir;//The face that was connected to the remaining body of the tree or the rooty block
//	public final Direction toolDir;//The face that was pounded on when breaking the block at cutPos
//	public final BlockPos cutPos;//The absolute(world) position of the block that was cut
//	public final int trunkHeight;
//
//	public static final BlockBounds bounds = new BlockBounds(new BlockPos(-64, -64, -64), new BlockPos(64, 64, 64));
//
//	public BranchDestructionData() {
//		species = Species.NULLSPECIES;
//		destroyedBranchesConnections = new int[0];
//		destroyedBranchesRadiusPosition = new int[0];
//		destroyedLeaves = new int[0];
//		leavesDrops = new ArrayList<>(0);
//		endPoints = new int[0];
//		woodVolume = 0;
//		cutDir = Direction.DOWN;
//		toolDir = Direction.DOWN;
//		cutPos = BlockPos.ZERO;
//		trunkHeight = 0;
//	}
//
//	public BranchDestructionData(Species species, Map<BlockPos, IExtendedBlockState> branches, Map<BlockPos, BlockState> leaves, List<BlockItemStack> leavesDrops, List<BlockPos> ends, float volume, BlockPos cutPos, Direction cutDir, Direction toolDir, int trunkHeight) {
//		this.species = species;
//		int[][] encodedBranchData = convertBranchesToIntArrays(branches);
//		this.destroyedBranchesRadiusPosition = encodedBranchData[0];
//		this.destroyedBranchesConnections = encodedBranchData[1];
//		this.destroyedLeaves = convertLeavesToIntArray(leaves);
//		this.leavesDrops = leavesDrops;
//		this.endPoints = convertEndPointsToIntArray(ends);
//		this.woodVolume = volume;
//		this.cutPos = cutPos;
//		this.cutDir = cutDir;
//		this.toolDir = toolDir;
//		this.trunkHeight = trunkHeight;
//	}
//
//	public BranchDestructionData(NBTTagCompound nbt) {
//		this.species = TreeRegistry.findSpecies(new ResourceLocation(nbt.getString("species")));
//		this.destroyedBranchesRadiusPosition = nbt.getIntArray("branchpos");
//		this.destroyedBranchesConnections = nbt.getIntArray("branchcon");
//		this.destroyedLeaves = nbt.getIntArray("leaves");
//		this.leavesDrops = new ArrayList<BlockItemStack>();
//		this.endPoints = nbt.getIntArray("ends");
//		this.woodVolume = nbt.getFloat("volume");
//		this.cutPos = new BlockPos(nbt.getInteger("cutx"), nbt.getInteger("cuty"), nbt.getInteger("cutz") );
//		this.cutDir = Direction.values()[MathHelper.clamp(nbt.getInteger("cutdir"), 0, Direction.values().length - 1)];
//		this.toolDir = Direction.values()[MathHelper.clamp(nbt.getInteger("tooldir"), 0, Direction.values().length - 1)];
//		this.trunkHeight = nbt.getInteger("trunkheight");
//	}
//
//	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
//		tag.setString("species", species.toString());
//		tag.setIntArray("branchpos", destroyedBranchesRadiusPosition);
//		tag.setIntArray("branchcon", destroyedBranchesConnections);
//		tag.setIntArray("leaves", destroyedLeaves);
//		tag.setIntArray("ends", endPoints);
//		tag.setFloat("volume", woodVolume);
//		tag.setInteger("cutx", cutPos.getX());
//		tag.setInteger("cuty", cutPos.getY());
//		tag.setInteger("cutz", cutPos.getZ());
//		tag.setInteger("cutdir", cutDir.getIndex());
//		tag.setInteger("tooldir", toolDir.getIndex());
//		tag.setInteger("trunkheight", trunkHeight);
//		return tag;
//	}
//
//	///////////////////////////////////////////////////////////
//	// Branches
//	///////////////////////////////////////////////////////////
//
//	private int[][] convertBranchesToIntArrays(Map<BlockPos, IExtendedBlockState> branchList) {
//		int data1[] = new int[branchList.size()];
//		int data2[] = new int[branchList.size()];
//		int index = 0;
//
//		//Ensure the origin block is at the first index
//		IExtendedBlockState origExState = branchList.get(BlockPos.ZERO);
//		if(origExState != null) {
//			data1[index] = encodeBranchesRadiusPos(BlockPos.ZERO, (BlockBranch) origExState.getBlock(), origExState);
//			data2[index++] = encodeBranchesConnections(origExState);
//			branchList.remove(BlockPos.ZERO);
//		}
//
//		//Encode the remaining blocks
//		for(Entry<BlockPos, IExtendedBlockState> set : branchList.entrySet()) {
//			BlockPos relPos = set.getKey();
//			IExtendedBlockState exState = set.getValue();
//			Block block = exState.getBlock();
//
//			if(block instanceof BlockBranch && bounds.inBounds(relPos)) { //Place comfortable limits on the system
//				data1[index] = encodeBranchesRadiusPos(relPos, (BlockBranch) block, exState);
//				data2[index++] = encodeBranchesConnections(exState);
//			}
//		}
//
//		//Shrink down the arrays
//		data1 = Arrays.copyOf(data1, index);
//		data2 = Arrays.copyOf(data2, index);
//
//		return new int[][] { data1, data2 };
//	}
//
//	private int encodeBranchesRadiusPos(BlockPos relPos, BlockBranch branchBlock, BlockState state) {
//		return	((branchBlock.getRadius(state) & 0x1F) << 24) | //Radius 0 - 31
//				encodeRelBlockPos(relPos);
//	}
//
//	private int encodeBranchesConnections(IExtendedBlockState exState) {
//		int result = 0;
//		for(Direction face : Direction.values()) {
//			int rad = (int) exState.getValue(BlockBranch.CONNECTIONS[face.getIndex()]);
//			result |= (rad & 0x1F) << (face.getIndex() * 5);//5 bits per face * 6 faces = 30bits
//		}
//		return result;
//	}
//
//	public int getNumBranches() {
//		return destroyedBranchesRadiusPosition.length;
//	}
//
//	public BlockPos getBranchRelPos(int index) {
//		return decodeRelPos(destroyedBranchesRadiusPosition[index]);
//	}
//
//	public int getBranchRadius(int index) {
//		return decodeBranchRadius(destroyedBranchesRadiusPosition[index]);
//	}
//
//	private int decodeBranchRadius(int encoded) {
//		return (encoded >> 24) & 0x1F;
//	}
//
//	public IExtendedBlockState getBranchBlockState(int index) {
//		return decodeBranchBlockState(destroyedBranchesRadiusPosition[index], destroyedBranchesConnections[index]);
//	}
//
//	private IExtendedBlockState decodeBranchBlockState(int encodedRadPos, int encodedConnections) {
//		BlockBranch branch = (BlockBranch)species.getFamily().getDynamicBranch();
//		if(branch instanceof BlockBranch) {
//			int radius = decodeBranchRadius(encodedRadPos);
//			BlockState state = branch.getStateForRadius(radius);
//			if(state instanceof IExtendedBlockState) {
//				IExtendedBlockState exState = (IExtendedBlockState) state;
//				for(Direction face : Direction.values()) {
//					int rad = (int) (encodedConnections >> (face.getIndex() * 5) & 0x1F);
//					exState = exState.withProperty(BlockBranch.CONNECTIONS[face.getIndex()], MathHelper.clamp(rad, 0, 8));
//				}
//				return exState;
//			}
//		}
//
//		return null;
//	}
//
//	///////////////////////////////////////////////////////////
//	// Leaves
//	///////////////////////////////////////////////////////////
//
//	private int[] convertLeavesToIntArray(Map<BlockPos, BlockState> leavesList) {
//		int data[] = new int[leavesList.size()];
//		int index = 0;
//
//		//Encode the remaining blocks
//		for(Entry<BlockPos, BlockState> set : leavesList.entrySet()) {
//			BlockPos relPos = set.getKey();
//			BlockState state = set.getValue();
//			Block block = state.getBlock();
//
//			if(block instanceof BlockDynamicLeaves && bounds.inBounds(relPos)) { //Place comfortable limits on the system
//				data[index++] = encodeLeaves(relPos, (BlockDynamicLeaves) block, state);
//			}
//		}
//
//		return Arrays.copyOf(data, index);//Shrink down the array
//	}
//
//	private int encodeLeaves(BlockPos relPos, BlockDynamicLeaves block, BlockState state) {
//		return	(state.getValue(BlockDynamicLeaves.HYDRO) << 24) | encodeRelBlockPos(relPos);
//	}
//
//	public int getNumLeaves() {
//		return destroyedLeaves.length;
//	}
//
//	public BlockPos getLeavesRelPos(int index) {
//		return decodeLeavesRelPos(destroyedLeaves[index]);
//	}
//
//	private BlockPos decodeLeavesRelPos(int encoded) {
//		return decodeRelPos(encoded);
//	}
//
//	public int getLeavesHydro(int index) {
//		return decodeLeavesHydro(destroyedLeaves[index]);
//	}
//
//	private int decodeLeavesHydro(int encoded) {
//		return (encoded >> 24) & 0x0F;
//	}
//
//	///////////////////////////////////////////////////////////
//	// End Points
//	///////////////////////////////////////////////////////////
//
//	private int[] convertEndPointsToIntArray(List<BlockPos> endPoints) {
//		int data[] = new int[endPoints.size()];
//		int index = 0;
//
//		for(BlockPos relPos : endPoints) {
//			if(bounds.inBounds(relPos)) { //Place comfortable limits on the system
//				data[index++] = encodeRelBlockPos(relPos);
//			}
//		}
//
//		return Arrays.copyOf(data, index);//Shrink down the array
//	}
//
//	public int getNumEndpoints() {
//		return endPoints.length;
//	}
//
//	public BlockPos getEndPointRelPos(int index) {
//		return decodeRelPos(endPoints[index]);
//	}
//
//
//	///////////////////////////////////////////////////////////
//	// Position Iteration
//	///////////////////////////////////////////////////////////
//
//	public static enum PosType {
//		BRANCHES,
//		LEAVES,
//		ENDPOINTS
//	}
//
//	/**
//	 * Get absolute positions of a position type
//	 *
//	 * @param posType
//	 * @return
//	 */
//	public Iterable<BlockPos> getPositions(PosType posType) {
//		return getPositions(posType, true);
//	}
//
//	/**
//	 * Get relative or absolute positions of a position type
//	 *
//	 * @param posType
//	 * @param absolute
//	 * @return
//	 */
//	public Iterable<BlockPos> getPositions(PosType posType, boolean absolute) {
//
//		final Function<Integer, BlockPos> getter;
//		final int limit;
//
//		switch(posType) {
//			default:
//			case BRANCHES:
//				getter = absolute ? i -> getBranchRelPos(i).add(cutPos) : i -> getBranchRelPos(i);
//				limit = getNumBranches();
//				break;
//			case ENDPOINTS:
//				getter = absolute ? i -> getEndPointRelPos(i).add(cutPos) : i -> getEndPointRelPos(i);
//				limit = getNumEndpoints();
//				break;
//			case LEAVES:
//				getter = absolute ? i -> getLeavesRelPos(i).add(cutPos) : i -> getLeavesRelPos(i);
//				limit = getNumLeaves();
//				break;
//		}
//
//		return new Iterable<BlockPos>() {
//			@Override
//			public Iterator<BlockPos> iterator() {
//				return new AbstractIterator<BlockPos>() {
//					private int index = 0;
//					@Override
//					protected BlockPos computeNext() {
//						return index < limit ? getter.apply(index++) : this.endOfData();
//					}
//				};
//			}
//		};
//	}
//
//
//	///////////////////////////////////////////////////////////
//	// Generic
//	///////////////////////////////////////////////////////////
//
//	private int encodeRelBlockPos(BlockPos relPos) {
//		return	(((relPos.getX() + 64) & 0xFF) << 16) |
//				(((relPos.getY() + 64) & 0xFF) << 8) |
//				(((relPos.getZ() + 64) & 0xFF) << 0) ;
//	}
//
//	private BlockPos decodeRelPos(int encoded) {
//		return new BlockPos(
//			(((encoded >> 16) & 0xFF) - 64),
//			(((encoded >> 8) & 0xFF) - 64),
//			(((encoded >> 0) & 0xFF) - 64)
//		);
//	}
//
}
