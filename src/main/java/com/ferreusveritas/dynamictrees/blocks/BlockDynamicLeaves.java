package com.ferreusveritas.dynamictrees.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.api.IAgeable;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.cells.CellNull;
import com.ferreusveritas.dynamictrees.api.cells.ICell;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockDoublePlant.EnumBlockHalf;
import net.minecraft.block.BlockDoublePlant.EnumPlantType;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockPlanks.EnumType;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockDynamicLeaves extends BlockLeaves implements ITreePart, IAgeable {
	
	public static boolean passableLeavesModLoaded = false;
	
	public static final PropertyInteger HYDRO = PropertyInteger.create("hydro", 1, 4);
	public static final PropertyInteger TREE = PropertyInteger.create("tree", 0, 3);
	
	public ILeavesProperties properties[] = new ILeavesProperties[] { LeavesProperties.NULLPROPERTIES, LeavesProperties.NULLPROPERTIES, LeavesProperties.NULLPROPERTIES, LeavesProperties.NULLPROPERTIES };
	
	public BlockDynamicLeaves() {
		this.setDefaultState(this.blockState.getBaseState().withProperty(HYDRO, 4).withProperty(TREE, 0));
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {HYDRO, TREE, DECAYABLE});
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(TREE, (meta >> 2) & 3).withProperty(HYDRO, (meta & 3) + 1);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return (state.getValue(HYDRO) - 1) | (state.getValue(TREE) << 2); 
	}
	
	public void setProperties(int tree, ILeavesProperties properties) {
		this.properties[tree & 3] = properties;
	}
	
	public ILeavesProperties getProperties(IBlockState blockState) {
		return properties[blockState.getValue(TREE) & 3];
	}
	
	@Override
	public TreeFamily getFamily(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos) {
		return getProperties(blockState).getTree();
	}
	
	// Get Leaves-specific flammability
	@Override
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return getProperties(world.getBlockState(pos)).getFlammability();
	}
	
	// Get Leaves-specific fire spread speed
	@Override
	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return getProperties(world.getBlockState(pos)).getFireSpreadSpeed();
	}
	
	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		//if(random.nextInt() % 4 == 0) {
			age(worldIn, pos, state, rand, SafeChunkBounds.ANY);
		//}
	}
	
	
	@Override
	public int age(World world, BlockPos pos, IBlockState state, Random rand, SafeChunkBounds safeBounds) {
		ILeavesProperties leavesProperties = getProperties(state);
		int oldHydro = state.getValue(BlockDynamicLeaves.HYDRO);
		boolean worldGen = safeBounds != SafeChunkBounds.ANY;
		
		//Check hydration level.  Dry leaves are dead leaves.
		int newHydro = getHydrationLevelFromNeighbors(world, pos, leavesProperties);
		if(newHydro == 0 || (!worldGen && !hasAdequateLight(state, world, leavesProperties, pos))) { //Light doesn't work right during worldgen so we'll just disable it during worldgen for now.
			world.setBlockToAir(pos);//No water, no light .. no leaves
			return -1;//Leaves were destroyed
		} else { 
			if(oldHydro != newHydro) {//Only update if the hydro has changed. A little performance gain
				//We do not use the 0x02 flag(update client) for performance reasons.  The clients do not need to know the hydration level of the leaves blocks as it
				//does not affect appearance or behavior.  For the same reason we use the 0x04 flag to prevent the block from being re-rendered.
				world.setBlockState(pos, leavesProperties.getDynamicLeavesState(newHydro), leavesProperties.appearanceChangesWithHydro() ? 2 : 4);
			}
		}
		
		//We should do this even if the hydro is only 1.  Since there could be adjacent branch blocks that could use a leaves block
		for(EnumFacing dir: EnumFacing.VALUES) {//Go on all 6 sides of this block
			if(newHydro > 1 || rand.nextInt(4) == 0 ) {//we'll give it a 1 in 4 chance to grow leaves if hydro is low to help performance
				BlockPos offpos = pos.offset(dir);
				if(safeBounds.inBounds(offpos, true) && isLocationSuitableForNewLeaves(world, leavesProperties, offpos)) {//Attempt to grow new leaves
					int hydro = getHydrationLevelFromNeighbors(world, offpos, leavesProperties);
					if(hydro > 0) {
						world.setBlockState(offpos, leavesProperties.getDynamicLeavesState(hydro), 2);//Removed Notify Neighbors Flag for performance
					}
				}
			}
		}
		
		return newHydro;//Leaves were not destroyed
	}
	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return getProperties(state).getPrimitiveLeavesItemStack();
	}
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {}
	
	@Override
	public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos) {
		return getProperties(state).getPrimitiveLeaves().getMapColor(world, pos);
	}
	
	/**
	 * We will disable landing effects because we crush the blocks on landing and create our own particles in crushBlock()
	 */
	@Override
	public boolean addLandingEffects(IBlockState state, WorldServer worldObj, BlockPos blockPosition, IBlockState iblockstate, EntityLivingBase entity, int numberOfParticles) {
		return true;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return FULL_BLOCK_AABB;
	}
	
	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean unknown) {
		if(entityIn instanceof EntityFallingTree) {
			return;
		}
		if(passableLeavesModLoaded || ModConfigs.vanillaLeavesCollision) {
			super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, unknown);
		} 
		else if (!ModConfigs.isLeavesPassable) {
			AxisAlignedBB aabb = new AxisAlignedBB(0.125, 0, 0.125, 0.875, 0.50, 0.875);
			addCollisionBoxToList(pos, entityBox, collidingBoxes, aabb);
		}
	}
	
	@Override
	public void onFallenUpon(World world, BlockPos pos, Entity entity, float fallDistance) {
		
		if(ModConfigs.canopyCrash && entity instanceof EntityLivingBase) { //We are only interested in Living things crashing through the canopy.
			entity.fallDistance--;
			
			AxisAlignedBB aabb = entity.getEntityBoundingBox();
			
			int minX = MathHelper.floor(aabb.minX + 0.001D);
			int minZ = MathHelper.floor(aabb.minZ + 0.001D);
			int maxX = MathHelper.floor(aabb.maxX - 0.001D);
			int maxZ = MathHelper.floor(aabb.maxZ - 0.001D);
			
			boolean crushing = true;
			boolean hasLeaves = true;
			
			SoundType stepSound = this.getSoundType();
			float volume = MathHelper.clamp(stepSound.getVolume() / 16.0f * fallDistance, 0, 3.0f);
			world.playSound(entity.posX, entity.posY, entity.posZ, stepSound.getBreakSound(), SoundCategory.BLOCKS, volume, stepSound.getPitch(), false);
			
			for(int iy = 0; (entity.fallDistance > 3.0f) && crushing && ((pos.getY() - iy) > 0); iy++) {
				if(hasLeaves) {//This layer has leaves that can help break our fall
					entity.fallDistance *= 0.66f;//For each layer we are crushing break the momentum
					hasLeaves = false;
				}
				for(int ix = minX; ix <= maxX; ix++) {
					for(int iz = minZ; iz <= maxZ; iz++) {
						BlockPos iPos = new BlockPos(ix, pos.getY() - iy, iz);
						IBlockState state = world.getBlockState(iPos);
						if(TreeHelper.isLeaves(state)) {
							hasLeaves = true;//This layer has leaves
							DynamicTrees.proxy.crushLeavesBlock(world, iPos, state, entity);
							world.setBlockToAir(iPos);
						} else
						if (!world.isAirBlock(iPos)) {
							crushing = false;//We hit something solid thus no longer crushing leaves layers
						}
					}
				}
			}
		}
	}
	
	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
		if(passableLeavesModLoaded || ModConfigs.vanillaLeavesCollision) {
			super.onEntityCollidedWithBlock(world, pos, state, entity);
		} 
		else {
			if (entity.motionY < 0.0D && entity.fallDistance < 2.0f) {
				entity.fallDistance = 0.0f;
				entity.motionY *= 0.5D;//Slowly sink into the block
			} else
				if (entity.motionY > 0 && entity.motionY < 0.25D) {
					entity.motionY += 0.025;//Allow a little climbing
				}
			
			entity.setSprinting(false);//One cannot sprint upon tree tops
			entity.motionX *= 0.25D;//Make travel slow and laborious
			entity.motionZ *= 0.25D;
		}
	}
	
	@Override
	public void beginLeavesDecay(IBlockState state, World world, BlockPos pos) {}
	
	/**
	 * Checks to see if the location at pos is suitable for new leaves
	 * and if so set new leaves at pos with hydro value
	 * 
	 * @param world The world
	 * @param leavesProp Properties of the leaves we are working with
	 * @param pos The position of interest
	 * @param hydro The hydration value for the resulting cell
	 * @return
	 */
	public boolean growLeavesIfLocationIsSuitable(World world, ILeavesProperties leavesProp, BlockPos pos, int hydro) {
		hydro = hydro == 0 ? leavesProp.getCellKit().getDefaultHydration() : hydro;
		if(isLocationSuitableForNewLeaves(world, leavesProp, pos)) {
			world.setBlockState(pos, leavesProp.getDynamicLeavesState(hydro), 2 | (leavesProp.appearanceChangesWithHydro() ? 1 : 0));//Removed Notify Neighbors Flag for performance
			return true;
		}
		return false;
	}
	
	//Test if the block at this location is capable of being grown into
	public boolean isLocationSuitableForNewLeaves(World world, ILeavesProperties leavesProperties, BlockPos pos) {
		IBlockState blockState = world.getBlockState(pos);
		Block block = blockState.getBlock();
		
		if(block instanceof BlockDynamicLeaves) {
			return false;
		}
				
		IBlockState belowBlockState = world.getBlockState(pos.down());
		
		//Prevent leaves from growing on the ground or above liquids
		if((belowBlockState.isFullCube() && (!(belowBlockState.getBlock() instanceof BlockLeaves)) ) || belowBlockState.getBlock() instanceof BlockLiquid) {
			return false;
		}
		
		//Help to grow into double tall grass and ferns in a more natural way
		if(block == Blocks.DOUBLE_PLANT){
			IBlockState bs = world.getBlockState(pos);
			EnumBlockHalf half = bs.getValue(BlockDoublePlant.HALF);
			if(half == EnumBlockHalf.UPPER) {//Top block of double plant
				if(belowBlockState.getBlock() == Blocks.DOUBLE_PLANT) {
					EnumPlantType type = belowBlockState.getValue(BlockDoublePlant.VARIANT);
					if(type == EnumPlantType.GRASS || type == EnumPlantType.FERN) {//tall grass or fern
						world.setBlockToAir(pos);
						world.setBlockState(pos.down(), Blocks.TALLGRASS.getDefaultState()
								.withProperty(BlockTallGrass.TYPE, type == EnumPlantType.GRASS ? BlockTallGrass.EnumType.GRASS : BlockTallGrass.EnumType.FERN), 3);
					}
				}
			}
		}
		
		return world.isAirBlock(pos) && hasAdequateLight(blockState, world, leavesProperties, pos);
	}
	
	
	
	/** Check to make sure the leaves have enough light to exist */
	public boolean hasAdequateLight(IBlockState blockState, World world, ILeavesProperties leavesProperties, BlockPos pos) {
		
		//If clear sky is above the block then we needn't go any further
		if(world.canBlockSeeSky(pos)) {
			return true;
		}
		
		int smother = leavesProperties.getSmotherLeavesMax();
		
		//Check to make sure there isn't too many leaves above this block.  Encourages forest canopy development.
		if(smother != 0){
			if(isBottom(world, pos)) {//Only act on the bottom block of the Growable stack
				//Prevent leaves from growing where they would be "smothered" from too much above foliage
				int smotherLeaves = 0;
				for(int i = 0; i < smother; i++) {
					smotherLeaves += TreeHelper.isTreePart(world, pos.up(i + 1)) ? 1 : 0;
				}
				if(smotherLeaves >= smother) {
					return false;
				}
			}
		}
		
		//Ensure the leaves don't grow in dark locations..  This creates a realistic canopy effect in forests and other nice stuff.
		//If there's already leaves here then don't kill them if it's a little dark
		//If it's empty space then don't create leaves unless it's sufficiently bright
		//The range allows for adaptation to the hysteretic effect that could cause blocks to rapidly appear and disappear 
		if(world.getLightFor(EnumSkyBlock.SKY, pos) >= (TreeHelper.isLeaves(blockState) ? leavesProperties.getLightRequirement() - 2 : leavesProperties.getLightRequirement())) {
			return true;
		}
		
		return false;
	}
	
	/** Used to find if the leaf block is at the bottom of the stack */
	public static boolean isBottom(World world, BlockPos pos) {
		IBlockState belowBlockState = world.getBlockState(pos.down());
		ITreePart belowTreepart = TreeHelper.getTreePart(belowBlockState);
		if(belowTreepart != TreeHelper.nullTreePart) {
			return belowTreepart.getRadius(belowBlockState) > 1;//False for leaves, twigs, and dirt.  True for stocky branches
		}
		return true;//Non-Tree parts below indicate the bottom of stack
	}
	
	/** Gathers hydration levels from neighbors before pushing the values into the solver */
	public int getHydrationLevelFromNeighbors(IBlockAccess world, BlockPos pos, ILeavesProperties leavesProp) {
		
		ICell cells[] = new ICell[6];
				
		for(EnumFacing dir: EnumFacing.VALUES) {
			BlockPos deltaPos = pos.offset(dir);
			IBlockState state = world.getBlockState(deltaPos);
			ITreePart part = TreeHelper.getTreePart(state);
			cells[dir.ordinal()] = part.getHydrationCell(world, deltaPos, state, dir, leavesProp);
		}
		
		return leavesProp.getCellKit().getCellSolver().solve(cells);//Find center cell's value from neighbors		
	}
	
	@Override
	public ICell getHydrationCell(IBlockAccess blockAccess, BlockPos pos, IBlockState blockState, EnumFacing dir, ILeavesProperties leavesProperties) {
		return dir != null ? leavesProperties.getCellKit().getCellForLeaves(blockState.getValue(BlockDynamicLeaves.HYDRO)) : CellNull.NULLCELL;
	}
	
	@Override
	public GrowSignal growSignal(World world, BlockPos pos, GrowSignal signal) {
		if(signal.step()) {//This is always placed at the beginning of every growSignal function
			branchOut(world, pos, signal);//When a growth signal hits a leaf block it attempts to become a tree branch
		}
		return signal;
	}
	
	/**
	* Will place a leaves block if the position is air and it's possible to create one there.
	* Otherwise it will check to see if the block is already there.
	* 
	* @param world
	* @param x
	* @param y
	* @param z
	* @param leavesProperties
	* @return True if the leaves are now at the coordinates.
	*/
	public boolean needLeaves(World world, BlockPos pos, ILeavesProperties leavesProperties) {
		if(world.isAirBlock(pos)){//Place Leaves if Air
			return this.growLeavesIfLocationIsSuitable(world, leavesProperties, pos, leavesProperties.getCellKit().getDefaultHydration());
		} else {//Otherwise check if there's already this type of leaves there.
			IBlockState blockState = world.getBlockState(pos);
			ITreePart treepart = TreeHelper.getTreePart(blockState);
			return treepart == this && leavesProperties == getProperties(blockState);//Check if this is the same type of leaves
		}
	}
	
	public GrowSignal branchOut(World world, BlockPos pos, GrowSignal signal) {
		
		ILeavesProperties leavesProperties = signal.getSpecies().getLeavesProperties();
		
		//Check to be sure the placement for a branch is valid by testing to see if it would first support a leaves block
		if(!needLeaves(world, pos, leavesProperties)){
			signal.success = false;
			return signal;
		}
		
		//Check to see if there's neighboring branches and abort if there's any found.
		EnumFacing originDir = signal.dir.getOpposite();
		
		for(EnumFacing dir: EnumFacing.VALUES) {
			if(!dir.equals(originDir)) {
				if(TreeHelper.isBranch(world.getBlockState(pos.offset(dir)))) {
					signal.success = false;
					return signal;
				}
			}
		}
		
		boolean hasLeaves = false;
		
		for(EnumFacing dir: EnumFacing.VALUES) {
			if(needLeaves(world, pos.offset(dir), leavesProperties)) {
				hasLeaves = true;
				break;
			}
		}
		
		if(hasLeaves) {
			//Finally set the leaves block to a branch
			TreeFamily family = signal.getSpecies().getFamily();
			family.getDynamicBranch().setRadius(world, pos, (int) family.getPrimaryThickness(), null);
			signal.radius = family.getSecondaryThickness();//For the benefit of the parent branch
		}
		
		signal.success = hasLeaves;
		
		return signal;
	}
	
	@Override
	public int probabilityForBlock(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, BlockBranch from) {
		return from.getFamily().isCompatibleDynamicLeaves(blockState, blockAccess, pos) ? 2: 0;
	}
	
	//////////////////////////////
	// DROPS
	//////////////////////////////
	
	@Override
	public List<ItemStack> getDrops(IBlockAccess access, BlockPos pos, IBlockState state, int fortune) {
		
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
		getExactSpecies(access, pos, getProperties(state)).getLeavesDrops(access, pos, ret, fortune);
		
		return ret;
	}
	
	/**
	 * Warning! Resource intensive algorithm.  Use only for interaction such as breaking blocks.
	 * 
	 * @param access
	 * @param pos
	 * @param leavesProperties
	 * @return
	 */
	Species getExactSpecies(IBlockAccess access, BlockPos pos, ILeavesProperties leavesProperties) {
		
		if(access instanceof World) {
			World world = (World) access;
			ArrayList<BlockPos> branchList = new ArrayList<BlockPos>();
			
			//Find all of the branches that are nearby
			for(BlockPos dPos: leavesProperties.getCellKit().getLeafCluster().getAllNonZero()) {
				dPos = pos.add(BlockPos.ORIGIN.subtract(dPos));//Becomes immutable at this point
				IBlockState state = access.getBlockState(dPos);
				if(TreeHelper.isBranch(state)) {
					BlockBranch branch = TreeHelper.getBranch(state);
					if(branch.getFamily() == leavesProperties.getTree() && branch.getRadius(state) == 1) {
						branchList.add(dPos);
					}
				}
			}
			
			if(!branchList.isEmpty()) {
				//Find the closest one
				BlockPos closest = branchList.get(0);
				double minDist = 999;
				
				for(BlockPos dPos : branchList) {
					double d = pos.distanceSq(dPos);
					if(d < minDist) {
						minDist = d;
						closest = dPos;
					}
				}
				
				return TreeHelper.getExactSpecies(world.getBlockState(closest), world, closest);
			}
		}
		
		return Species.NULLSPECIES;
	}
	
	@Override
	protected boolean canSilkHarvest() {
		return false;
	}
	
	//Some mods are using the following 3 member functions to find what items to drop, I'm disabling this behavior here.  I'm looking at you FastLeafDecay mod. ;)
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return null;
	}
	
	@Override
	public int quantityDropped(Random random) {
		return 0;
	}
	
	@Override
	public int damageDropped(IBlockState state) {
		return 0;
	}
	
	//When the leaves are sheared just return vanilla leaves for usability
	@Override
	public ArrayList<ItemStack> onSheared(ItemStack item, IBlockAccess blockAccess, BlockPos pos, int fortune) {
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
		ILeavesProperties properties = getProperties(blockAccess.getBlockState(pos));
		ItemStack stack = properties.getPrimitiveLeavesItemStack().copy();
		stack.setCount(1);
		ret.add(stack);
		return ret;
	}
	
	//////////////////////////////
	// RENDERING FUNCTIONS
	//////////////////////////////
	
	@Override
	public int getRadiusForConnection(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, BlockBranch from, EnumFacing side, int fromRadius) {
		return getProperties(blockState).getRadiusForConnection(blockState, blockAccess, pos, from, side, fromRadius);
	}
	
	@Override
	public boolean isPassable(IBlockAccess access, BlockPos pos) {
		return passableLeavesModLoaded ? super.isPassable(access, pos) : ModConfigs.isLeavesPassable;
	}
	
	@Override
	public boolean isFoliage(IBlockAccess world, BlockPos pos) {
		return true;
	}
	
	@Override
	public int getRadius(IBlockState blockState) {
		return 0;
	}
	
	/**
	 * Generally Leaves blocks should not be analyzed
	 */
	@Override
	public boolean shouldAnalyse() {
		return false;
	}
	
	@Override
	public MapSignal analyse(IBlockState blockState, World world, BlockPos pos, EnumFacing fromDir, MapSignal signal) {
		return signal;//Shouldn't need to run analysis on leaf blocks
	}
	
	@Override
	public int branchSupport(IBlockState blockState, IBlockAccess blockAccess, BlockBranch branch, BlockPos pos, EnumFacing dir, int radius) {
		//Leaves are only support for "twigs"
		return radius == 1 && branch.getFamily() == getFamily(blockState, blockAccess, pos) ? BlockBranch.setSupport(0, 1) : 0;
	}
	
	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return EnumPushReaction.DESTROY;
	}
	
	@Override
	public EnumType getWoodType(int meta) {
		return BlockPlanks.EnumType.OAK;//Shouldn't matter since it's only used to name things in ItemLeaves
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return Blocks.LEAVES.isOpaqueCube(state);
	}
	
	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;//This prevents fences and walls from attempting to connect to leaves.
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return Blocks.LEAVES.getBlockLayer();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		setGraphicsLevel(!Blocks.LEAVES.isOpaqueCube(blockState));
		return super.shouldSideBeRendered(blockState, blockAccess, pos, side); 
	}
	
	@Override
	public final TreePartType getTreePartType() {
		return TreePartType.LEAVES;
	}
	
}
