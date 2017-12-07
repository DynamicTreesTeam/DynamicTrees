package com.ferreusveritas.dynamictrees.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.IAgeable;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.cells.Cells;
import com.ferreusveritas.dynamictrees.api.cells.ICell;
import com.ferreusveritas.dynamictrees.api.network.GrowSignal;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.util.MathHelper;

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
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class BlockDynamicLeaves extends BlockLeaves implements ITreePart, IAgeable {
	
	public static final PropertyInteger HYDRO = PropertyInteger.create("hydro", 1, 4);
	public static final PropertyInteger TREE = PropertyInteger.create("tree", 0, 3);
	
	private DynamicTree trees[] = new DynamicTree[4];
	
	public BlockDynamicLeaves() {
		this.setDefaultState(this.blockState.getBaseState().withProperty(HYDRO, 4).withProperty(TREE, 0));
		leavesFancy = true;//True for alpha transparent leaves
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {HYDRO, TREE});
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(TREE, (meta >> 2) & 3).withProperty(HYDRO, (meta & 3) + 1);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return (state.getValue(HYDRO) - 1) | (state.getValue(TREE) << 2); 
	}
	
	public void setTree(int treeNum, DynamicTree tree) {
		trees[treeNum & 3] = tree;
	}
	
	@Override
	public DynamicTree getTree(IBlockAccess blockAccess, BlockPos pos) {
		return getTree(blockAccess.getBlockState(pos));
	}
	
	public DynamicTree getTree(IBlockState blockState) {
		return getTree(blockState.getValue(TREE));
	}
	
	public DynamicTree getTree(int treeNum) {
		return trees[treeNum & 3];
	}

	//Borrow flammability from the vanilla minecraft leaves
	@Override
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return getTree(world, pos).getPrimitiveLeaves().getBlock().getFlammability(world, pos, face);
	}
	
	//Borrow fire spread rate from the vanilla minecraft leaves
	@Override
	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return getTree(world, pos).getPrimitiveLeaves().getBlock().getFireSpreadSpeed(world, pos, face);
	}

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		//if(random.nextInt() % 4 == 0) {
			age(worldIn, pos, state, rand, false);
		//}
	}

	@Override
	public boolean age(World world, BlockPos pos, IBlockState state, Random rand, boolean fast) {
		DynamicTree tree = getTree(state);
		int preHydro = getHydrationLevel(state);

		//Check hydration level.  Dry leaves are dead leaves.
		int hydro = getHydrationLevelFromNeighbors(world, pos, tree);
		if(hydro == 0 || !hasAdequateLight(world, tree, pos)){
			removeLeaves(world, pos);//No water, no light .. no leaves
			return true;//Leaves were destroyed
		} else { 
			//Encode new hydration level in metadata for this leaf
			if(preHydro != hydro) {//A little performance gain
				if(setHydrationLevel(world, pos, hydro, state)) {
					return true;//Leaves were destroyed
				}
			}
		}

		if(hydro > 1) {
			for(EnumFacing dir: EnumFacing.VALUES) {//Go on all 6 sides of this block
				growLeaves(world, tree, pos.offset(dir));//Attempt to grow new leaves
			}
		}

		//Do special things if the leaf block is/was on the bottom
		if(!fast && isBottom(world, pos)) {
			tree.bottomSpecial(world, pos, rand);
		}
		
		return false;//Leaves were not destroyed
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {

		EnumFacing dir = facing.getOpposite();
		
		BlockPos deltaPos = pos.offset(dir);

		DynamicTree tree = TreeHelper.getSafeTreePart(world, deltaPos).getTree(world, deltaPos);

		if(tree != null && tree.getDynamicLeaves() == this) {//Attempt to match the proper dynamic leaves for the tree being clicked on
			return getDefaultState().withProperty(TREE, tree.getDynamicLeavesSub());
		}

		return getDefaultState();
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {}
	
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
		AxisAlignedBB aabb = new AxisAlignedBB(0.125, 0, 0.125, 0.875, 0.50, 0.875);
        addCollisionBoxToList(pos, entityBox, collidingBoxes, aabb);
	}

	@Override
	public void onFallenUpon(World world, BlockPos pos, Entity entity, float fallDistance) {

		if(entity instanceof EntityLivingBase) { //We are only interested in Living things crashing through the canopy.
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

	@Override
	public void beginLeavesDecay(IBlockState state, World world, BlockPos pos) {}

	//Set the block at the provided coords to a leaf block if local light, space and hydration requirements are met
	public void growLeaves(World world, DynamicTree tree, BlockPos pos){
		if(isLocationSuitableForNewLeaves(world, tree, pos)){
			int hydro = getHydrationLevelFromNeighbors(world, pos, tree);
			setBlockToLeaves(world, tree, pos, hydro);
		}
	}

	//Set the block at the provided coords to a leaf block if local light and space requirements are met 
	public boolean growLeaves(World world, DynamicTree tree, BlockPos pos, int hydro) {
		hydro = hydro == 0 ? tree.getDefaultHydration() : hydro;
		if(isLocationSuitableForNewLeaves(world, tree, pos)) {
			return setBlockToLeaves(world, tree, pos, hydro);
		}
		return false;
	}

	//Test if the block at this location is capable of being grown into
	public boolean isLocationSuitableForNewLeaves(World world, DynamicTree tree, BlockPos pos) {
		IBlockState blockState = world.getBlockState(pos);
		Block block = blockState.getBlock();
		
		if(block instanceof BlockDynamicLeaves) {
			return false;
		}

		IBlockState belowBlockState = world.getBlockState(pos.down());

		//Prevent leaves from growing on the ground or above liquids
		if(belowBlockState.isOpaqueCube() || belowBlockState.getBlock() instanceof BlockLiquid) {
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

		return world.isAirBlock(pos) && hasAdequateLight(world, tree, pos);
	}

	/** Set the block at the provided coords to a leaf block and also set it's hydration value.
	* If hydration value is 0 then it sets the block to air
	*/
	public boolean setBlockToLeaves(World world, DynamicTree tree, BlockPos pos, int hydro) {
		hydro = MathHelper.clamp(hydro, 0, 4);
		if(hydro != 0) {
			world.setBlockState(pos, getDefaultState().withProperty(HYDRO, hydro).withProperty(TREE, tree.getDynamicLeavesSub()), 2);//Removed Notify Neighbors Flag for performance
			return true;
		} else {
			removeLeaves(world, pos);
			return false;
		}
	}

	/** Check to make sure the leaves have enough light to exist */
	public boolean hasAdequateLight(World world, DynamicTree tree, BlockPos pos) {

		//If clear sky is above the block then we needn't go any further
		if(world.canBlockSeeSky(pos)) {
			return true;
		}

		int smother = tree.getSmotherLeavesMax();

		//Check to make sure there isn't too many leaves above this block.  Encourages forest canopy development.
		if(smother != 0){
			if(isBottom(world, pos, world.getBlockState(pos.down()).getBlock())) {//Only act on the bottom block of the Growable stack
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
		if(world.getLightFor(EnumSkyBlock.SKY, pos) >= (TreeHelper.isLeaves(world, pos) ? tree.getLightRequirement() - 2 : tree.getLightRequirement())) {
			return true;
		}

		return false;
	}

	/** Used to find if the leaf block is at the bottom of the stack */
	public static boolean isBottom(World world, BlockPos pos) {
		Block belowBlock = world.getBlockState(pos.down()).getBlock();
		return isBottom(world, pos, belowBlock);
	}

	/** Used to find if the leaf block is at the bottom of the stack */
	public static boolean isBottom(World world, BlockPos pos, Block belowBlock) {
		if(TreeHelper.isTreePart(belowBlock)) {
			ITreePart belowTreepart = (ITreePart) belowBlock;
			return belowTreepart.getRadius(world, pos.down()) > 1;//False for leaves, twigs, and dirt.  True for stocky branches
		}
		return true;//Non-Tree parts below indicate the bottom of stack
	}
	
	/** Gathers hydration levels from neighbors before pushing the values into the solver */
	public int getHydrationLevelFromNeighbors(IBlockAccess world, BlockPos pos, DynamicTree tree) {

		ICell cells[] = new ICell[6];
		
		for(EnumFacing dir: EnumFacing.VALUES) {
			BlockPos deltaPos = pos.offset(dir);
			IBlockState state = world.getBlockState(deltaPos);
			cells[dir.ordinal()] = TreeHelper.getSafeTreePart(state).getHydrationCell(world, deltaPos, state, dir, tree);
		}
		
		return tree.getCellSolver().solve(cells);//Find center cell's value from neighbors		
	}
	
	public int getHydrationLevel(IBlockState blockState) {
		if(blockState.getBlock() instanceof BlockDynamicLeaves) {
			return blockState.getValue(HYDRO);
		}
		return 0;
	}

	public int getHydrationLevel(IBlockAccess blockAccess, BlockPos pos) {
		return getHydrationLevel(blockAccess.getBlockState(pos));
	}

	@Override
	public ICell getHydrationCell(IBlockAccess blockAccess, BlockPos pos, IBlockState blockState, EnumFacing dir, DynamicTree leavesTree) {
		int hydro = getHydrationLevel(blockState);
		DynamicTree tree = getTree(blockState);
		
		if(dir != null && tree != null) {
			return tree.getCellForLeaves(hydro);
		} else {
			return Cells.normalCells[hydro];
		}
	}

	public static void removeLeaves(World world, BlockPos pos) {
		world.setBlockToAir(pos);
		world.notifyNeighborsOfStateChange(pos, Blocks.AIR, false); //TODO: Does this affect performance and is it necessary?
	}
	
	//Variable hydration levels are only appropriate for leaf blocks
	public static boolean setHydrationLevel(World world, BlockPos pos, int hydro, IBlockState currentBlockState) {
		hydro = MathHelper.clamp(hydro, 0, 4);
		
		if(hydro == 0) {
			removeLeaves(world, pos);
			return true;
		} else {
			//We do not use the 0x02 flag(update client) for performance reasons.  The clients do not need to know the hydration level of the leaves blocks as it
			//does not affect appearance or behavior.  For the same reason we use the 0x04 flag to prevent the block from being re-rendered.
			world.setBlockState(pos, currentBlockState.withProperty(HYDRO, hydro), 4);
			return false;
		}
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
	* @param tree
	* @return True if the leaves are now at the coordinates.
	*/
	public boolean needLeaves(World world, BlockPos pos, DynamicTree tree) {
		if(world.isAirBlock(pos)){//Place Leaves if Air
			return this.growLeaves(world, tree, pos, tree.getDefaultHydration());
		} else {//Otherwise check if there's already this type of leaves there.
			IBlockState blockState = world.getBlockState(pos);
			ITreePart treepart = TreeHelper.getSafeTreePart(blockState);
			return treepart == this && tree == getTree(blockState);//Check if this is the same type of leaves
		}
	}

	public GrowSignal branchOut(World world, BlockPos pos, GrowSignal signal) {

		DynamicTree tree = signal.getSpecies().getTree();

		//Check to be sure the placement for a branch is valid by testing to see if it would first support a leaves block
		if(tree == null || !needLeaves(world, pos, tree)){
			signal.success = false;
			return signal;
		}

		//Check to see if there's neighboring branches and abort if there's any found.
		EnumFacing originDir = signal.dir.getOpposite();

		for(EnumFacing dir: EnumFacing.VALUES) {
			if(!dir.equals(originDir)) {
				if(TreeHelper.isBranch(world, pos.offset(dir))) {
					signal.success = false;
					return signal;
				}
			}
		}

		boolean hasLeaves = false;

		for(EnumFacing dir: EnumFacing.VALUES) {
			if(needLeaves(world, pos.offset(dir), tree)) {
				hasLeaves = true;
			}
		}

		if(hasLeaves) {
			//Finally set the leaves block to a branch
			world.setBlockState(pos, tree.getDynamicBranch().getDefaultState(), 2);
			signal.radius = signal.getSpecies().getSecondaryThickness();//For the benefit of the parent branch
		}

		signal.success = hasLeaves;

		return signal;
	}

	@Override
	public int probabilityForBlock(IBlockAccess blockAccess, BlockPos pos, BlockBranch from) {
		return from.getTree().isCompatibleDynamicLeaves(blockAccess, pos) ? 2: 0;
	}

	//////////////////////////////
	// DROPS
	//////////////////////////////
	
	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		DynamicTree tree = getTree(state);
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();

		if(tree == null) {
			return ret;
		}

		int chance = getSaplingDropChance(state);

		//Hokey fortune stuff here.
		if (fortune > 0) {
			chance -= 2 << fortune;
			if (chance < 10) { 
				chance = 10;
			}
		}

		//It's mostly for seeds.. mostly.
		//Ignores quantityDropped() for Vanilla consistency and fortune compatibility.
		Random rand = world instanceof World ? ((World)world).rand : new Random();
		if (rand.nextInt(chance) == 0) {
			ret.add(tree.getSeedStack());
		}

		//More fortune contrivances here.  Vanilla compatible returns.
		chance = 200; //1 in 200 chance of returning an "apple"
		if (fortune > 0) {
			chance -= 10 << fortune;
			if (chance < 40) {
				chance = 40;
			}
		}

		//Get species specific drops.. apples or cocoa for instance
		tree.getDrops(world, pos, chance, ret);

		return ret;
	}

	@Override
	protected boolean canSilkHarvest() {
		return false;
	}

	//1 in 64 chance to drop a seed on destruction.. This quantity is used when the tree is cut down and not for when the leaves are directly destroyed.
	public int quantitySeedDropped(Random random) {
		return random.nextInt(64) == 0 ? 1 : 0;
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
		DynamicTree tree = getTree(blockAccess, pos);
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
		ret.add(tree.getPrimitiveLeavesItemStack(1));
		return ret;
	}

	//////////////////////////////
	// RENDERING FUNCTIONS
	//////////////////////////////

	@Override
	public int getRadiusForConnection(IBlockAccess blockAccess, BlockPos pos, BlockBranch from, int fromRadius) {
		return fromRadius == 1 && from.getTree().isCompatibleDynamicLeaves(blockAccess, pos) ? 1 : 0;
	}


	/*	FUTURE: Particle effects. Future leaves dropping from trees and wisps and stuff. Client side only
	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random random){
		if(isBottom(world, x, y, z)){
			EntityFX leaf = new EntityParticleLeaf(world, x + 0.5d, y - 0.5d, z + 0.5d, 0, -0.2, 0);
			Minecraft.getMinecraft().effectRenderer.addEffect(leaf);
		}
	}
	*/

	@Override
	public boolean isFoliage(IBlockAccess world, BlockPos pos) {
		return true;
	}

	@Override
	public int getRadius(IBlockAccess blockAccess, BlockPos pos) {
		return 0;
	}

	@Override
	public MapSignal analyse(World world, BlockPos pos, EnumFacing fromDir, MapSignal signal) {
		return signal;//Shouldn't need to run analysis on leaf blocks
	}

	@Override
	public boolean isRootNode() {
		return false;
	}

	@Override
	public int branchSupport(IBlockAccess blockAccess, BlockBranch branch, BlockPos pos, EnumFacing dir, int radius) {
		//Leaves are only support for "twigs"
		return radius == 1 && branch.getTree() == getTree(blockAccess, pos) ? 0x01 : 0;
	}

	@Override
	public boolean applyItemSubstance(World world, BlockPos pos, EntityPlayer player, EnumHand hand, ItemStack itemStack) {
		return false;//Nothing is applied to leaves
	}

	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return EnumPushReaction.DESTROY;
	}

	@Override
	public EnumType getWoodType(int meta) {
		return BlockPlanks.EnumType.OAK;//Shouldn't matter since it's only used to name things in ItemLeaves
	}

}
