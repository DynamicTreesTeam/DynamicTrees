package com.ferreusveritas.dynamictrees.blocks.branches;

import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.cells.CellNull;
import com.ferreusveritas.dynamictrees.api.cells.ICell;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.blocks.leaves.DynamicLeavesBlock;
import com.ferreusveritas.dynamictrees.cells.MetadataCell;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

public class BasicBranchBlock extends BranchBlock {
	
	protected static final IntegerProperty RADIUS = IntegerProperty.create("radius", 1, RADMAX_NORMAL);
	
	protected BlockState[] branchStates;
	
	private int flammability = 5; // Mimic vanilla logs
	private int fireSpreadSpeed = 5; // Mimic vanilla logs
	
	// Trees are mostly made of wood
	public BasicBranchBlock(String name) {
		this(AbstractBlock.Properties.create(Material.WOOD), name);//Trees are made of wood. Brilliant.
	}
	
	// Useful for more unique subclasses
	public BasicBranchBlock(AbstractBlock.Properties properties, String name) {
		super(properties.sound(SoundType.WOOD).harvestTool(ToolType.AXE).harvestLevel(0), name); //aaaaand they also sound like wood.

		cacheBranchStates();
	}

	@Nullable
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(RADIUS, 1);
	}
	
	public void cacheBranchStates() {
		this.setDefaultState(stateContainer.getBaseState().with(RADIUS, 1));
		
		branchStates = new BlockState[RADMAX_NORMAL + 1];
		
		//Cache the branch blocks states for rapid lookup
		branchStates[0] = Blocks.AIR.getDefaultState();
		for(int radius = 1; radius <= RADMAX_NORMAL; radius++) {
			branchStates[radius] = getDefaultState().with(BasicBranchBlock.RADIUS, radius);
		}
	}
	
	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////
	
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(RADIUS);
	}
	
	///////////////////////////////////////////
	// TREE INFORMATION
	///////////////////////////////////////////
	
	@Override
	public int branchSupport(BlockState blockState, IBlockReader blockAccess, BranchBlock branch, BlockPos pos, Direction dir, int radius) {
		return isSameTree(branch) ? BasicBranchBlock.setSupport(1, 1) : 0;// Other branches of the same type are always valid support.
	}
	
	@Override
	public boolean canFall() {
		return true;
	}
	
	
	///////////////////////////////////////////
	// WORLD UPDATE
	///////////////////////////////////////////

	@Override
	public boolean checkForRot(IWorld world, BlockPos pos, Species species, int radius, Random rand, float chance, boolean rapid) {
		
		if( !rapid && (chance == 0.0f || rand.nextFloat() > chance) ) {
			return false;//Bail out if not in rapid mode and the rot chance fails
		}
		
		// Rooty dirt below the block counts as a branch in this instance
		// Rooty dirt below for saplings counts as 2 neighbors if the soil is not infertile
		int neigh = 0;// High Nybble is count of branches, Low Nybble is any reinforcing treepart(including branches)
		
		for (Direction dir : Direction.values()) {
			BlockPos deltaPos = pos.offset(dir);
			BlockState deltaBlockState = world.getBlockState(deltaPos);
			neigh += TreeHelper.getTreePart(deltaBlockState).branchSupport(deltaBlockState, world, this, deltaPos, dir, radius);
			if (getBranchSupport(neigh) >= 1 && getLeavesSupport(neigh) >= 2) {// Need two neighbors.. one of which must be another branch
				return false;// We've proven that this branch is reinforced so there is no need to continue
			}
		}
		
		boolean didRot = species.rot(world, pos, neigh & 0x0F, radius, rand, rapid);// Unreinforced branches are destroyed
		
		if(rapid && didRot) {// Speedily rot back dead branches if this block rotted
			for (Direction dir : Direction.values()) {// The logic here is that if this block rotted then
				BlockPos neighPos = pos.offset(dir);// the neighbors might be rotted too.
				BlockState neighState = world.getBlockState(neighPos);
				if(neighState.getBlock() == this) { // Only check blocks logs that are the same as this one
					checkForRot(world, neighPos, species, getRadius(neighState), rand, 1.0f, true);
				}
			}
		}
		
		return didRot;
	}
	
	
	///////////////////////////////////////////
	// PHYSICAL PROPERTIES
	///////////////////////////////////////////

	@Override
	public float getHardness (IBlockReader worldIn, BlockPos pos) {
		int radius = getRadius(worldIn.getBlockState(pos));
		float hardness = getFamily().getPrimitiveLog().getDefaultState().getBlockHardness(worldIn, pos) * (radius * radius) / 64.0f * 8.0f;
		hardness = (float) Math.min(hardness, DTConfigs.maxTreeHardness.get());//So many youtube let's plays start with "OMG, this is taking so long to break this tree!"
		return hardness;
	}

	@Override
	public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
		int radius = getRadius(world.getBlockState(pos));
		return (fireSpreadSpeed * radius) / 8 ;
	}
	
	@Override
	public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
		return flammability;
	}
	
	public BasicBranchBlock setFlammability(int flammability) {
		this.flammability = flammability;
		return this;
	}
	
	public BasicBranchBlock setFireSpreadSpeed(int fireSpreadSpeed) {
		this.fireSpreadSpeed = fireSpreadSpeed;
		return this;
	}

	///////////////////////////////////////////
	// GROWTH
	///////////////////////////////////////////
	
	@Override
	public ICell getHydrationCell(IBlockReader blockAccess, BlockPos pos, BlockState blockState, Direction dir, ILeavesProperties leavesProperties) {
		TreeFamily thisTree = getFamily();
		
		if(leavesProperties.getTree() == thisTree) {// The requesting leaves must match the tree for hydration to occur
			int radiusAndMeta = thisTree.getRadiusForCellKit(blockAccess, pos, blockState, dir, this);
			int radius = MetadataCell.getRadius(radiusAndMeta);
			int metadata = MetadataCell.getMeta(radiusAndMeta);
			return leavesProperties.getCellKit().getCellForBranch(radius, metadata);
		} else {
			return CellNull.NULLCELL;
		}
	}
	
	@Override
	public int getRadius(BlockState blockState) {
		return blockState.getBlock() == this ? blockState.get(RADIUS) : 0;
	}
	
	@Override
	public int setRadius(IWorld world, BlockPos pos, int radius, Direction originDir, int flags) {
		destroyMode = DynamicTrees.EnumDestroyMode.SETRADIUS;
		world.setBlockState(pos, getStateForRadius(radius), flags);
		destroyMode = DynamicTrees.EnumDestroyMode.SLOPPY;
		return radius;
	}
	
	@Override
	public BlockState getStateForRadius(int radius) {
		return branchStates[MathHelper.clamp(radius, 1, getMaxRadius())];
	}
	
	// Directionless probability grabber
	@Override
	public int probabilityForBlock(BlockState blockState, IBlockReader blockAccess, BlockPos pos, BranchBlock from) {
		return isSameTree(from) ? getRadius(blockState) + 2 : 0;
	}
	
	public GrowSignal growIntoAir(World world, BlockPos pos, GrowSignal signal, int fromRadius) {
		Species species = signal.getSpecies();
		
		DynamicLeavesBlock leaves = TreeHelper.getLeaves(species.getLeavesProperties().getDynamicLeavesState());
		if (leaves != null) {
			if (fromRadius == 1) {// If we came from a twig then just make some leaves
				signal.success = leaves.growLeavesIfLocationIsSuitable(world, species.getLeavesProperties(), pos, 0);
			} else {// Otherwise make a proper branch
				return leaves.branchOut(world, pos, signal);
			}
		}
		return signal;
	}
	
	@Override
	public GrowSignal growSignal(World world, BlockPos pos, GrowSignal signal) {
		
		if (signal.step()) {// This is always placed at the beginning of every growSignal function
			
			BlockState currBlockState = world.getBlockState(pos);
			Species species = signal.getSpecies();
			boolean inTrunk = signal.isInTrunk();
			
			Direction originDir = signal.dir.getOpposite();// Direction this signal originated from
			Direction targetDir = species.selectNewDirection(world, pos, this, signal);// This must be cached on the stack for proper recursion
			signal.doTurn(targetDir);
			
			{
				BlockPos deltaPos = pos.offset(targetDir);
				BlockState deltaState = world.getBlockState(deltaPos);
				
				// Pass grow signal to next block in path
				ITreePart treepart = TreeHelper.getTreePart(deltaState);
				if (treepart != TreeHelper.nullTreePart) {
					signal = treepart.growSignal(world, deltaPos, signal);// Recurse
				} else if (world.isAirBlock(deltaPos) || deltaState.getBlock() == DTRegistries.trunkShellBlock) {
					signal = growIntoAir(world, deltaPos, signal, getRadius(currBlockState));
				}
			}
			
			// Calculate Branch Thickness based on neighboring branches
			float areaAccum = signal.radius * signal.radius;// Start by accumulating the branch we just came from
			
			for (Direction dir : Direction.values()) {
				if (!dir.equals(originDir) && !dir.equals(targetDir)) {// Don't count where the signal originated from or the branch we just came back from
					BlockPos deltaPos = pos.offset(dir);
					
					// If it is decided to implement a special block(like a squirrel hole, tree
					// swing, rotting, burned or infested branch, etc) then this new block could be
					// derived from BlockBranch and this works perfectly. Should even work with
					// tileEntity blocks derived from BlockBranch.
					BlockState blockState = world.getBlockState(deltaPos);
					ITreePart treepart = TreeHelper.getTreePart(blockState);
					if (isSameTree(treepart)) {
						int branchRadius = treepart.getRadius(blockState);
						areaAccum += branchRadius * branchRadius;
					}
				}
			}
			
			//Only continue to set radii if the tree growth isn't choked out
			if(!signal.choked) {
				// Ensure that side branches are not thicker than the size of a block.  Also enforce species max thickness
				int maxRadius = inTrunk ? species.maxBranchRadius() : Math.min(species.maxBranchRadius(), RADMAX_NORMAL);
				
				// The new branch should be the square root of all of the sums of the areas of the branches coming into it.
				// But it shouldn't be smaller than it's current size(prevents the instant slimming effect when chopping off branches)
				signal.radius = MathHelper.clamp((float) Math.sqrt(areaAccum) + species.getTapering(), getRadius(currBlockState), maxRadius);// WOW!
				int targetRadius = (int) Math.floor(signal.radius);
				int setRad = setRadius(world, pos, targetRadius, originDir);
				if(setRad < targetRadius) { //We tried to set a radius but it didn't comply because something is in the way.
					signal.choked = true; //If something is in the way then it means that the tree growth is choked
				}
			}
		}
		
		return signal;
	}
	
	
	///////////////////////////////////////////
	// PHYSICAL BOUNDS
	///////////////////////////////////////////
	
	// This is only so effective because the center of the player must be inside the block that contains the tree trunk.
	// The result is that only thin branches and trunks can be climbed
	
	@Override
	public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity) {
		return DTConfigs.enableBranchClimbling.get() && getRadius(state) <= 3;
	}
	
	@Nonnull
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {

		int thisRadiusInt = getRadius(state);
		double radius = thisRadiusInt / 16.0;
		VoxelShape core = VoxelShapes.create(0.5 - radius, 0.5 - radius, 0.5 - radius, 0.5 + radius, 0.5 + radius, 0.5 + radius);
		
		for (Direction dir : Direction.values()) {
			int sideRadiusInt = Math.min(getSideConnectionRadius(worldIn, pos, thisRadiusInt, dir), thisRadiusInt);
			double sideRadius = sideRadiusInt / 16.0f;
			if (sideRadius > 0.0f) {
				double gap = 0.5f - sideRadius;
				AxisAlignedBB aabb = new AxisAlignedBB(0.5 - sideRadius, 0.5 - sideRadius, 0.5 - sideRadius, 0.5 + sideRadius, 0.5 + sideRadius, 0.5 + sideRadius);
				aabb = aabb.expand(dir.getXOffset() * gap, dir.getYOffset() * gap, dir.getZOffset() * gap);
				core = VoxelShapes.or(core, VoxelShapes.create(aabb));
			}
		}
		
		return core;
	}

	@Override
	public int getRadiusForConnection(BlockState blockState, IBlockReader world, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
		return getRadius(blockState);
	}
	
	protected int getSideConnectionRadius(IBlockReader blockAccess, BlockPos pos, int radius, Direction side) {
		BlockPos deltaPos = pos.offset(side);
		try {
			BlockState blockState = blockAccess.getBlockState(deltaPos);
			return TreeHelper.getTreePart(blockState).getRadiusForConnection(blockState, blockAccess, deltaPos, this, side, radius);
		} catch (Exception e) { // Temporary measure until we find a way to solve calling an out-of-bounds block here.
			System.out.println("X: " + deltaPos.getX() + " Y: " + deltaPos.getY() + " Z: " + deltaPos.getZ());
			return 0;
		}
	}
	
	
	///////////////////////////////////////////
	// NODE ANALYSIS
	///////////////////////////////////////////
	
	protected int getMaxSignalDepth() {
		return 32;
	}
	
	@Override
	public boolean shouldAnalyse() {
		return true;
	}
	
	/**
	 * This is a recursive algorithm used to explore the branch network.  It calls a run() function for the signal on the way out
	 * and a returnRun() on the way back.
	 *
	 * Okay so a little explanation here..
	 * I've been hit up by people who claim that recursion is a bad idea.  The reason why they think this is because java has to push values
	 * on the stack for each level of recursion and then pop them off as the levels complete.  Many times this can lead to performance issues.
	 * Fine, I understand that.  The reason why it doesn't matter here is because of the object oriented nature of how the tree parts
	 * function demand that a different analyze function be called for each object type.  Even if this were rewritten to be iterative the
	 * same number of stack pushes and pops would need to be performed to run the custom function for each node in the network anyway.  The
	 * depth of recursion for this algorithm is less than 32.  So there's no real risk of a stack overflow.
	 *
	 * The difference being that in an iterative design I would need to maintain a stack array holding all of the values and push and pop
	 * them manually or use a stack index.  This is messy and not something I would want to maintain for practically non-existent gains.
	 * Java does a pretty good job of managing the stack on its own.
	 */
	@Override
	public MapSignal analyse(BlockState blockState, IWorld world, BlockPos pos, Direction fromDir, MapSignal signal) {
		// Note: fromDir will be null in the origin node
		if (signal.depth++ < getMaxSignalDepth()) {// Prevents going too deep into large networks, or worse, being caught in a network loop
			signal.run(blockState, world, pos, fromDir);// Run the inspectors of choice
			for (Direction dir : Direction.values()) {// Spread signal in various directions
				if (dir != fromDir) {// don't count where the signal originated from
					BlockPos deltaPos = pos.offset(dir);
					
					BlockState deltaState = world.getBlockState(deltaPos);
					ITreePart treePart = TreeHelper.getTreePart(deltaState);
					
					if(treePart.shouldAnalyse()) {
						signal = treePart.analyse(deltaState, world, deltaPos, dir.getOpposite(), signal);
						
						// This should only be true for the originating block when the root node is found
						if (signal.found && signal.localRootDir == null && fromDir == null) {
							signal.localRootDir = dir;
						}
					}
				}
			}
			signal.returnRun(blockState, world, pos, fromDir);
		} else {
			BlockState state = world.getBlockState(pos);
			if(state.getBlock() instanceof BranchBlock) {
				BranchBlock branch = (BranchBlock) state.getBlock();
				branch.breakDeliberate(world, pos, DynamicTrees.EnumDestroyMode.OVERFLOW);// Destroy one of the offending nodes
			}
			signal.overflow = true;
		}
		signal.depth--;
		
		return signal;
	}

}
