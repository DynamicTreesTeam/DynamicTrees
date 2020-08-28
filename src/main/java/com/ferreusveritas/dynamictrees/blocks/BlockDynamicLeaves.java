package com.ferreusveritas.dynamictrees.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

import com.ferreusveritas.dynamictrees.api.IAgeable;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.cells.CellNull;
import com.ferreusveritas.dynamictrees.api.cells.ICell;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.init.DTClient;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import com.ferreusveritas.dynamictrees.util.IRayTraceCollision;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.PushReaction;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockDynamicLeaves extends LeavesBlock implements ITreePart, IAgeable, IRayTraceCollision {

	public static boolean passableLeavesModLoaded = false;

	protected static Random backupRng = new Random();

//	public static final IntegerProperty DISTANCE = IntegerProperty.create("distance", 1, 4);
//	public static final BooleanProperty PERSISTENT = BooleanProperty.create("flowering");

	public ILeavesProperties properties = LeavesProperties.NULLPROPERTIES;

	public BlockDynamicLeaves() {
		super(Properties.create(Material.LEAVES).sound(SoundType.PLANT).tickRandomly());
		this.setDefaultState(this.stateContainer.getBaseState().with(DISTANCE, LeavesProperties.maxHydro).with(PERSISTENT, false));
	}

	public Block setDefaultNaming(String modid, String name) {
		setRegistryName(modid, name);
		return this;
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(DISTANCE, PERSISTENT);
	}

	public void setProperties(ILeavesProperties properties) {
		this.properties = properties;
	}

	public ILeavesProperties getProperties(BlockState blockState) {
		return properties;
	}

	@Override
	public TreeFamily getFamily(BlockState blockState, IBlockReader blockAccess, BlockPos pos) {
		return getProperties(blockState).getTree();
	}

	// Get Leaves-specific flammability
	@Override
	public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
		return getProperties(world.getBlockState(pos)).getFlammability();
	}

	// Get Leaves-specific fire spread speed
	@Override
	public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
		return getProperties(world.getBlockState(pos)).getFireSpreadSpeed();
	}

	@Override
	public void tick(BlockState state, World worldIn, BlockPos pos, Random rand) {
		super.tick(state, worldIn, pos, rand);

		if (rand == null) rand = backupRng;
		if(rand.nextInt(DTConfigs.treeGrowthFolding.get()) == 0) {
			double attempts = DTConfigs.treeGrowthFolding.get() * DTConfigs.treeGrowthMultiplier.get();

			if(attempts >= 1.0f || rand.nextFloat() < attempts) {
				doTick(worldIn, pos, state, rand);
			}

			int start = rand.nextInt(26);

			while(--attempts > 0) {
				if(attempts >= 1.0f || rand.nextFloat() < attempts) {
					int r = (start++ % 26) + 14;//14 - 39
					r = r > 26 ? r - 13 : r - 14;//0 - 26 but Skip 13
					BlockPos dPos = pos.add((r % 3) - 1, ((r / 3) % 3) - 1, ((r / 9) % 3) - 1);// (-1, -1, -1) to (1, 1, 1) skipping (0, 0, 0)
					BlockState dState = worldIn.getBlockState(dPos);
					if(dState.getBlock() instanceof BlockDynamicLeaves) {
						((BlockDynamicLeaves)dState.getBlock()).doTick(worldIn, dPos, dState, rand);
					}
				}
			}
		}
	}

	protected void doTick(World worldIn, BlockPos pos, BlockState state, Random rand) {
		if((pos.getX() != 0 && pos.getX() != 15 & pos.getZ() != 0 & pos.getZ() != 15) || worldIn.isAreaLoaded(pos, 1)) {
			if(!state.get(LeavesBlock.PERSISTENT) && getProperties(state).updateTick(worldIn, pos, state, rand)) {
				age(worldIn, pos, state, rand, SafeChunkBounds.ANY);
			}
		}
	}

	@Override
	public int age(World world, BlockPos pos, BlockState state, Random rand, SafeChunkBounds safeBounds) {

//		world.removeBlock(pos, false);

		ILeavesProperties leavesProperties = getProperties(state);
		int oldHydro = state.get(BlockDynamicLeaves.DISTANCE);
		boolean worldGen = safeBounds != SafeChunkBounds.ANY;

		//Check hydration level.  Dry leaves are dead leaves.
		int newHydro = getHydrationLevelFromNeighbors(world, pos, leavesProperties);

		if(newHydro == 0 || (!worldGen && !hasAdequateLight(state, world, leavesProperties, pos))) { //Light doesn't work right during worldgen so we'll just disable it during worldgen for now.
			world.removeBlock(pos, false);//No water, no light .. no leaves
			return -1;//Leaves were destroyed
		} else {
			if(oldHydro != newHydro) {//Only update if the hydro has changed. A little performance gain
				//We do not use the 0x02 flag(update client) for performance reasons.  The clients do not need to know the hydration level of the leaves blocks as it
				//does not affect appearance or behavior.  For the same reason we use the 0x04 flag to prevent the block from being re-rendered.
				world.setBlockState(pos, leavesProperties.getDynamicLeavesState(newHydro), 4);
			}
		}

		NewLeavesPropertiesHandler newLeavesHander = getNewLeavesPropertiesHandler(world, pos, state, newHydro, worldGen);

		//We should do this even if the hydro is only 1.  Since there could be adjacent branch blocks that could use a leaves block
		for(Direction dir: Direction.values()) {//Go on all 6 sides of this block
			if(newHydro > 1 || rand.nextInt(4) == 0 ) {//we'll give it a 1 in 4 chance to grow leaves if hydro is low to help performance
				BlockPos offpos = pos.offset(dir);
				if(safeBounds.inBounds(offpos, true) && isLocationSuitableForNewLeaves(world, leavesProperties, offpos)) {//Attempt to grow new leaves
					int hydro = getHydrationLevelFromNeighbors(world, offpos, leavesProperties);
					if(hydro > 0) {
						world.setBlockState(offpos, newLeavesHander.getLeaves(world, offpos, leavesProperties.getDynamicLeavesState(hydro)), 2);//Removed Notify Neighbors Flag for performance
					}
				}
			}
		}

		return newHydro;//Leaves were not destroyed
	}

	/**
	 * Provides a method to add custom leaves properties besides the normal
	 * hydro.  Currently used by flowering oak in the BoP add-on
	 *
	 * @param world The world
	 * @param pos Position of the new leaves blck
	 * @param state The original state of the leaves block before aging occured
	 * @param newHydro The new calculated hydration value of the leaves
	 * @param worldGen true if this is happening during worldgen
	 * @return A provider for adding more blockstate properties
	 */
	protected NewLeavesPropertiesHandler getNewLeavesPropertiesHandler(World world, BlockPos pos, BlockState state, int newHydro, boolean worldGen) {
		return (w, p, l) -> l; //By default just pass the blockState along
	}

	protected interface NewLeavesPropertiesHandler {
		BlockState getLeaves(World world, BlockPos pos, BlockState leavesStateWithHydro);
	}

	@Override
	public float getBlockHardness(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return getProperties(state).getPrimitiveLeaves().getBlockHardness(worldIn, pos);
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		return getProperties(state).getPrimitiveLeavesItemStack();
	}

//	@Override
//	public MapColor getMapColor(BlockState state, World world, BlockPos pos) {
//		return getProperties(state).getPrimitiveLeaves().getMapColor(world, pos);
//	}

	/**
	 * We will disable landing effects because we crush the blocks on landing and create our own particles in crushBlock()
	 */
	@Override
	public boolean addLandingEffects(BlockState state1, ServerWorld worldserver, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
		return true;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return VoxelShapes.fullCube();
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		if (passableLeavesModLoaded || DTConfigs.isLeavesPassable.get() || isItemPassable(context)){
			return VoxelShapes.empty();
		} else {
			return VoxelShapes.create(new AxisAlignedBB(0.125, 0, 0.125, 0.875, 0.50, 0.875));
		}
	}

	public boolean isItemPassable (ISelectionContext context){
		return (context.getEntity() instanceof ItemEntity && ((ItemEntity)context.getEntity()).getItem().getItem() instanceof Seed);
	}

	@Override
	public void onFallenUpon(World world, BlockPos pos, Entity entity, float fallDistance) {

		if(DTConfigs.canopyCrash.get() && entity instanceof LivingEntity) { //We are only interested in Living things crashing through the canopy.
			entity.fallDistance--;

			AxisAlignedBB aabb = entity.getBoundingBox();

			int minX = MathHelper.floor(aabb.minX + 0.001D);
			int minZ = MathHelper.floor(aabb.minZ + 0.001D);
			int maxX = MathHelper.floor(aabb.maxX - 0.001D);
			int maxZ = MathHelper.floor(aabb.maxZ - 0.001D);

			boolean crushing = true;
			boolean hasLeaves = true;

			SoundType stepSound = this.getSoundType(world.getBlockState(pos), world, pos, entity);
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
						BlockState state = world.getBlockState(iPos);
						if(TreeHelper.isLeaves(state)) {
							hasLeaves = true;//This layer has leaves
							DTClient.crushLeavesBlock(world, iPos, state, entity);
							world.removeBlock(iPos, false);
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
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		if(entity instanceof ItemEntity || passableLeavesModLoaded || DTConfigs.vanillaLeavesCollision.get()) {
			super.onEntityCollision(state, world, pos, entity);
		}
		else {
			if (entity.getMotion().y < 0.0D && entity.fallDistance < 2.0f) {
				entity.fallDistance = 0.0f;
				entity.setMotion(entity.getMotion().x, entity.getMotion().y * 0.5D, entity.getMotion().z);//Slowly sink into the block
			} else
			if (entity.getMotion().y > 0 && entity.getMotion().y < 0.25D) {
				entity.setMotion(entity.getMotion().x, entity.getMotion().y + 0.025, entity.getMotion().z);//Allow a little climbing
			}

			entity.setSprinting(false);//One cannot sprint upon tree tops
			entity.setMotion(entity.getMotion().x * 0.25D, entity.getMotion().y, entity.getMotion().z  * 0.25D);//Make travel slow and laborious
		}
	}

	@Override
	public void beginLeaveDecay(BlockState state, IWorldReader world, BlockPos pos) { }

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
			world.setBlockState(pos, leavesProp.getDynamicLeavesState(hydro), 2);//Removed Notify Neighbors Flag for performance
			return true;
		}
		return false;
	}

	//Test if the block at this location is capable of being grown into
	public boolean isLocationSuitableForNewLeaves(World world, ILeavesProperties leavesProperties, BlockPos pos) {
		BlockState blockState = world.getBlockState(pos);
		Block block = blockState.getBlock();

		if(block instanceof BlockDynamicLeaves) {
			return false;
		}

		BlockState belowBlockState = world.getBlockState(pos.down());

		//Prevent leaves from growing on the ground or above liquids
		if((belowBlockState.isSolid() && (!(belowBlockState.getBlock() instanceof LeavesBlock)) ) || belowBlockState.getBlock() instanceof FlowingFluidBlock) {
			return false;
		}

		//Help to grow into double tall grass and ferns in a more natural way
//		if(block instanceof DoublePlantBlock){
//			BlockState bs = world.getBlockState(pos);
//			DoubleBlockHalf half = bs.get(DoublePlantBlock.HALF);
//			if(half == DoubleBlockHalf.UPPER) {//Top block of double plant
//				if(belowBlockState.getBlock() instanceof DoublePlantBlock) {
//					if(type == EnumPlantType.GRASS || type == EnumPlantType.FERN) {//tall grass or fern
//						world.removeBlock(pos, false);
//						Blocks.TALL_GRASS
//						world.setBlockState(pos.down(), Blocks.GRASS.getDefaultState()
//								.with(BlockTallGrass.TYPE, type == EnumPlantType.GRASS ? BlockTallGrass.EnumType.GRASS : BlockTallGrass.EnumType.FERN), 3);
//					}
//				}
//			}
//		}

		return (world.isAirBlock(pos) || world.getBlockState(pos).getMaterial().isReplaceable()) && hasAdequateLight(blockState, world, leavesProperties, pos);
	}



	/** Check to make sure the leaves have enough light to exist */
	public boolean hasAdequateLight(BlockState blockState, World world, ILeavesProperties leavesProperties, BlockPos pos) {

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
		if(world.getLightFor(LightType.SKY, pos) >= (TreeHelper.isLeaves(blockState) ? leavesProperties.getLightRequirement() - 2 : leavesProperties.getLightRequirement())) {
			return true;
		}

		return false;
	}

	/** Used to find if the leaf block is at the bottom of the stack */
	public static boolean isBottom(World world, BlockPos pos) {
		BlockState belowBlockState = world.getBlockState(pos.down());
		ITreePart belowTreepart = TreeHelper.getTreePart(belowBlockState);
		if(belowTreepart != TreeHelper.nullTreePart) {
			return belowTreepart.getRadius(belowBlockState) > 1;//False for leaves, twigs, and dirt.  True for stocky branches
		}
		return true;//Non-Tree parts below indicate the bottom of stack
	}

	/** Gathers hydration levels from neighbors before pushing the values into the solver */
	public int getHydrationLevelFromNeighbors(World access, BlockPos pos, ILeavesProperties leavesProp) {

		ICell[] cells = new ICell[6];

		for(Direction dir: Direction.values()) {
			BlockPos deltaPos = pos.offset(dir);
			BlockState state = access.getBlockState(deltaPos);
			ITreePart part = TreeHelper.getTreePart(state);
			cells[dir.ordinal()] = part.getHydrationCell(access, deltaPos, state, dir, leavesProp);
		}

		return leavesProp.getCellKit().getCellSolver().solve(cells);//Find center cell's value from neighbors
	}

	@Override
	public ICell getHydrationCell(IBlockReader blockAccess, BlockPos pos, BlockState blockState, Direction dir, ILeavesProperties leavesProperties) {
		return dir != null ? leavesProperties.getCellKit().getCellForLeaves(blockState.get(LeavesBlock.DISTANCE)) : CellNull.NULLCELL;
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
	* @param pos
	* @param leavesProperties
	* @return True if the leaves are now at the coordinates.
	*/
	public boolean needLeaves(World world, BlockPos pos, ILeavesProperties leavesProperties) {
		if(world.isAirBlock(pos)){//Place Leaves if Air
			return this.growLeavesIfLocationIsSuitable(world, leavesProperties, pos, leavesProperties.getCellKit().getDefaultHydration());
		} else {//Otherwise check if there's already this type of leaves there.
			BlockState blockState = world.getBlockState(pos);
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
		Direction originDir = signal.dir.getOpposite();

		for(Direction dir: Direction.values()) {
			if(!dir.equals(originDir)) {
				if(TreeHelper.isBranch(world.getBlockState(pos.offset(dir)))) {
					signal.success = false;
					return signal;
				}
			}
		}

		boolean hasLeaves = false;

		for(Direction dir: Direction.values()) {
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
	public int probabilityForBlock(BlockState blockState, IBlockReader blockAccess, BlockPos pos, BlockBranch from) {
		return from.getFamily().isCompatibleDynamicLeaves(blockState, blockAccess, pos) ? 2: 0;
	}

	//////////////////////////////
	// DROPS
	//////////////////////////////

	//When the leaves are sheared just return vanilla leaves for usability
	@Nonnull
	@Override
	public List<ItemStack> onSheared(@Nonnull ItemStack item, IWorld world, BlockPos pos, int fortune) {
		System.out.println("on sheared");
		ArrayList<ItemStack> ret = new ArrayList<>();
		ret.add(getProperties(world.getBlockState(pos)).getPrimitiveLeavesItemStack());
		return ret;
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		ArrayList<ItemStack> ret = new ArrayList<>();
		PlayerEntity player = builder.get(LootParameters.LAST_DAMAGE_PLAYER);
		int fortuneLevel = 0;
		if (player != null){
			ItemStack handStack = player.getHeldItemMainhand();
			fortuneLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, handStack);
		}
		return getExactSpecies(builder.getWorld(), builder.get(LootParameters.POSITION), getProperties(state)).getLeavesDrops(builder.getWorld(), builder.get(LootParameters.POSITION), ret, fortuneLevel);
	}

	/**
	 * Warning! Resource intensive algorithm.  Use only for interaction such as breaking blocks.
	 *
	 * @param access
	 * @param pos
	 * @param leavesProperties
	 * @return
	 */
	Species getExactSpecies(World access, BlockPos pos, ILeavesProperties leavesProperties) {

		if(access != null) {
			ArrayList<BlockPos> branchList = new ArrayList<>();

			//Find all of the branches that are nearby
			for(BlockPos dPos: leavesProperties.getCellKit().getLeafCluster().getAllNonZero()) {
				dPos = pos.add(BlockPos.ZERO.subtract(dPos));//Becomes immutable at this point
				BlockState state = access.getBlockState(dPos);
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

				return TreeHelper.getExactSpecies((access).getBlockState(closest), access, closest);
			}
		}

		return Species.NULLSPECIES;
	}

//	@Override
//	protected boolean canSilkHarvest() {
//		return false;
//	}
//
//	//Some mods are using the following 3 member functions to find what items to drop, I'm disabling this behavior here.  I'm looking at you FastLeafDecay mod. ;)
//	@Override
//	public Item getItemDropped(BlockState state, Random rand, int fortune) {
//		return null;
//	}
//
//	@Override
//	public int quantityDropped(Random random) {
//		return 0;
//	}
//
//	@Override
//	public int damageDropped(BlockState state) {
//		return 0;
//	}

	//////////////////////////////
	// RENDERING FUNCTIONS
	//////////////////////////////

	@Override
	public int getRadiusForConnection(BlockState blockState, IBlockReader world, BlockPos pos, BlockBranch from, Direction side, int fromRadius) {
		return getProperties(blockState).getRadiusForConnection(blockState, world, pos, from, side, fromRadius);
	}

	@Override
	public boolean isFoliage(BlockState state, IWorldReader world, BlockPos pos) {
		return true;
	}

	@Override
	public int getRadius(BlockState blockState) {
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
	public MapSignal analyse(BlockState blockState, World world, BlockPos pos, Direction fromDir, MapSignal signal) {
		return signal;//Shouldn't need to run analysis on leaf blocks
	}

	@Override
	public int branchSupport(BlockState blockState, IBlockReader blockAccess, BlockBranch branch, BlockPos pos, Direction dir, int radius) {
		//Leaves are only support for "twigs"
		return radius == 1 && branch.getFamily() == getFamily(blockState, blockAccess, pos) ? BlockBranch.setSupport(0, 1) : 0;
	}

	@Override
	public PushReaction getPushReaction(BlockState state) {
		return PushReaction.DESTROY;
	}

//	@Override
//	public EnumType getWoodType(int meta) {
//		return BlockPlanks.EnumType.OAK;//Shouldn't matter since it's only used to name things in ItemLeaves
//	}
//
//	@Override
//	public boolean isOpaqueCube(BlockState state) {
//		return Blocks.LEAVES.isOpaqueCube(state);
//	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public BlockRenderLayer getRenderLayer() {
		return Blocks.OAK_LEAVES.getRenderLayer();
	}

//	@Override
//	@OnlyIn(Dist.CLIENT)
//	public boolean shouldSideBeRendered(BlockState blockState, World blockAccess, BlockPos pos, Direction side) {
//		setGraphicsLevel(!Blocks.LEAVES.isOpaqueCube(blockState));
//		return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
//	}

	@Override
	public final TreePartType getTreePartType() {
		return TreePartType.LEAVES;
	}

	@Override
	public boolean isRayTraceCollidable() {
		return true;
	}

}
