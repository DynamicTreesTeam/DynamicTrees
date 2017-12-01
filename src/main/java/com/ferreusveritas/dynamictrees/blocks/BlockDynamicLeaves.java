package com.ferreusveritas.dynamictrees.blocks;

import java.util.ArrayList;
import java.util.Random;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.IAgeable;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.backport.BlockAccessDec;
import com.ferreusveritas.dynamictrees.api.backport.BlockAndMeta;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.EnumHand;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.api.cells.Cells;
import com.ferreusveritas.dynamictrees.api.cells.ICell;
import com.ferreusveritas.dynamictrees.api.network.GrowSignal;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.util.IRegisterable;
import com.ferreusveritas.dynamictrees.util.MathHelper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockDynamicLeaves extends BlockLeaves implements ITreePart, IAgeable, IRegisterable {
	
	private String[] species = {"X", "X", "X", "X"};
	private DynamicTree trees[] = new DynamicTree[4];
	protected String registryName;
	
	public BlockDynamicLeaves() {
		field_150121_P = true;//True for alpha transparent leaves
	}
	
	@Override
	public void setRegistryName(String regName) {
		registryName = regName;
	}
	
	@Override
	public String getRegistryName() {
		return registryName;
	}
	
	@Override
	public void setUnlocalizedNameReg(String unlocalName) {
		setBlockName(unlocalName);
	}
	
	public void setTree(int sub, DynamicTree tree) {
		trees[sub & 3] = tree;
		species[sub & 3] = tree.getName();
	}
	
	@Override
	public DynamicTree getTree(BlockAccessDec blockAccess, BlockPos pos) {
		return getTree(getSubBlockNum(blockAccess, pos));
	}
	
	public DynamicTree getTree(IBlockState blockState) {
		return getTreeFromMetadata(blockState.getMeta());
	}
	
	public DynamicTree getTreeFromMetadata(int metadata) {
		return getTree(getSubBlockNumFromMetadata(metadata));
	}
	
	public DynamicTree getTree(int treeNum) {
		return trees[treeNum & 3];
	}

	//Pull the subblock number portion from the metadata 
	public static final int getSubBlockNumFromMetadata(int meta){
		return (meta >> 2) & 3;
	}

	//Pull the subblock from the world
	public static int getSubBlockNum(IBlockAccess blockAccess, BlockPos pos) {
		return getSubBlockNumFromMetadata(new BlockAccessDec(blockAccess).getBlockMetadata(pos));
	}
	
	//Borrow flammability from the vanilla minecraft leaves
	@Override
	public int getFlammability(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
		return getTree(new BlockAccessDec(world), new BlockPos(x, y, z)).getPrimitiveLeaves().getBlock().getFlammability(world, x, y, z, face);
	}
	
	//Borrow fire spread rate from the vanilla minecraft leaves
	@Override
	public int getFireSpreadSpeed(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
		return getTree(new BlockAccessDec(world), new BlockPos(x, y, z)).getPrimitiveLeaves().getBlock().getFireSpreadSpeed(world, x, y, z, face);
	}

	@Override
	public void updateTick(net.minecraft.world.World world, int x, int y, int z, Random rand) {
		//if(random.nextInt() % 4 == 0) {
			BlockPos pos = new BlockPos(x, y, z);
			age(new World(world), pos, new World(world).getBlockState(pos), rand, false);
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
	public int onBlockPlaced(net.minecraft.world.World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata) {
		
		ForgeDirection dir = ForgeDirection.getOrientation(side).getOpposite();
		
		BlockPos deltaPos = new BlockPos(x, y, z).offset(dir);

		DynamicTree tree = TreeHelper.getSafeTreePart(world, deltaPos).getTree(new World(world), deltaPos);

		if(tree != null && tree.getDynamicLeaves() == this) {//Attempt to match the proper growing leaves for the tree being clicked on
			return tree.getDynamicLeavesSub() << 2;//Return matched metadata
		}

		return 0;
	}

	@Override
	public void breakBlock(net.minecraft.world.World world, int x, int y, int z, Block block, int metadata){}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(net.minecraft.world.World world, int x, int y, int z) {
		return AxisAlignedBB.getBoundingBox(x + 0.25, y, z + 0.25, x + 0.75, y + 0.50, z + 0.75);
	}

	@Override
	public void onFallenUpon(net.minecraft.world.World world, int x, int y, int z, Entity entity, float fallDistance) {
		onFallenUpon(new World(world), new BlockPos(x, y, z), entity, fallDistance);
	}

	public void onFallenUpon(World world, BlockPos pos, Entity entity, float fallDistance) {

		if(entity instanceof EntityLivingBase) { //We are only interested in Living things crashing through the canopy.
			entity.fallDistance--;

			AxisAlignedBB aabb = entity.boundingBox;
			
			int minX = MathHelper.floor(aabb.minX + 0.001D);
			int minZ = MathHelper.floor(aabb.minZ + 0.001D);
			int maxX = MathHelper.floor(aabb.maxX - 0.001D);
			int maxZ = MathHelper.floor(aabb.maxZ - 0.001D);

			boolean crushing = true;
			boolean hasLeaves = true;

			float volume = MathHelper.clamp(stepSound.getVolume() / 16.0f * fallDistance, 0, 3.0f);
			world.getWorld().playSoundAtEntity(entity, this.stepSound.getBreakSound(), volume, this.stepSound.getPitch());

			for(int iy = 0; (entity.fallDistance > 3.0f) && crushing && ((pos.getY() - iy) > 0); iy++) {
				if(hasLeaves) {//This layer has leaves that can help break our fall
					entity.fallDistance *= 0.66f;//For each layer we are crushing break the momentum
					hasLeaves = false;
				}
				for(int ix = minX; ix <= maxX; ix++) {
					for(int iz = minZ; iz <= maxZ; iz++) {
						BlockPos iPos = new BlockPos(ix, pos.getY() - iy, iz);
						if(TreeHelper.isLeaves(world, iPos)) {
							hasLeaves = true;//This layer has leaves
							crushBlock(world, iPos, entity);
						} else
						if (!world.isAirBlock(iPos)) {
							crushing = false;//We hit something solid thus no longer crushing leaves layers
						}
					}
				}
			}
		}
	}

	public void crushBlock(World world, BlockPos pos, Entity entity) {

		if(world.isRemote()) {
			Random random = world.rand;
			ITreePart treePart = TreeHelper.getTreePart(world, pos);
			if(treePart instanceof BlockDynamicLeaves) {
				DynamicTree tree = treePart.getTree(world, pos);
				if(tree != null) {
					int metadata = world.getBlockMetadata(pos);
					for(int dz = 0; dz < 8; dz++) {
						for(int dy = 0; dy < 8; dy++) {
							for(int dx = 0; dx < 8; dx++) {
								if(random.nextInt(8) == 0) {
									double fx = pos.getX() + dx / 8.0;
									double fy = pos.getY() + dy / 8.0;
									double fz = pos.getZ() + dz / 8.0;
									DynamicTrees.proxy.addDustParticle(world.getWorld(), fx, fy, fz, 0, random.nextFloat() * entity.motionY, 0, dx, dy, dz, this, metadata);
								}
							}
						}
					}
				}
			}
		}

		world.setBlockToAir(pos);
	}

	@Override
	public void onEntityCollidedWithBlock(net.minecraft.world.World world, int x, int y, int z, Entity entity) {
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
	public void beginLeavesDecay(net.minecraft.world.World world, int x, int y, int z) {}

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
		Block block = world.getBlock(pos);
		
		if(block instanceof BlockDynamicLeaves) {
			return false;
		}

		BlockPos belowPos = pos.down();
		Block belowBlock = world.getBlock(belowPos);

		//Prevent leaves from growing on the ground or above liquids
		if(belowBlock.isOpaqueCube() || belowBlock instanceof BlockLiquid) {
			return false;
		}

		//Help to grow into double tall grass and ferns in a more natural way
		if(block == Blocks.double_plant){
			int meta = world.getBlockMetadata(pos);
			if((meta & 8) != 0) {//Top block of double plant 
				meta = world.getBlockMetadata(belowPos);
				if(meta == 2 || meta == 3) {//tall grass or fern
					world.setBlockToAir(pos);
					world.setBlockState(belowPos, new BlockAndMeta(Blocks.tallgrass, meta - 1));
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
			int sub = tree.getDynamicLeavesSub();
			world.getWorld().setBlock(pos.getX(), pos.getY(), pos.getZ(), this, ((sub << 2) & 12) | ((hydro - 1) & 3), 2);//Removed Notify Neighbors Flag for performance
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
			if(isBottom(world, pos, world.getBlock(pos.down()))) {//Only act on the bottom block of the Growable stack
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
		if(world.getLightFor(EnumSkyBlock.Sky, pos) >= (TreeHelper.isLeaves(world, pos) ? tree.getLightRequirement() - 2 : tree.getLightRequirement())) {
			return true;
		}

		return false;
	}

	/** Used to find if the leaf block is at the bottom of the stack */
	public static boolean isBottom(World world, BlockPos pos) {
		Block belowBlock = world.getBlock(pos.down());
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
	public int getHydrationLevelFromNeighbors(BlockAccessDec world, BlockPos pos, DynamicTree tree) {

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
			return getHydrationLevelFromMetadata(blockState.getMeta());
		}
		return 0;
	}

	public int getHydrationLevelFromMetadata(int meta) {
		return (meta & 3) + 1;
	}

	public int getHydrationLevel(BlockAccessDec blockAccess, BlockPos pos) {
		return getHydrationLevelFromMetadata(blockAccess.getBlockMetadata(pos));
	}

	@Override
	public ICell getHydrationCell(BlockAccessDec blockAccess, BlockPos pos, IBlockState blockState, EnumFacing dir, DynamicTree leavesTree) {
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
		world.getWorld().notifyBlocksOfNeighborChange(pos.getX(), pos.getY(), pos.getZ(), Blocks.air);
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
			world.getWorld().setBlockMetadataWithNotify(pos.getX(), pos.getY(), pos.getZ(), (currentBlockState.getMeta() & 12) | ((hydro - 1) & 3), 4);
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
	* Will place a leaves block if the position is air.
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
			ITreePart treepart = TreeHelper.getSafeTreePart(world, pos);
			return treepart == this && tree.getDynamicLeavesSub() == getSubBlockNum(world, pos);//Check if this is the same type of leaves
		}
	}

	public GrowSignal branchOut(World world, BlockPos pos, GrowSignal signal) {

		DynamicTree tree = signal.getTree();

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
			world.setBlockState(pos, new BlockAndMeta(signal.branchBlock, 0), 2);
			signal.radius = signal.getTree().getSecondaryThickness();//For the benefit of the parent branch
		}

		signal.success = hasLeaves;

		return signal;
	}

	@Override
	public int probabilityForBlock(BlockAccessDec blockAccess, BlockPos pos, BlockBranch from) {
		return from.getTree().isCompatibleDynamicLeaves(blockAccess, pos) ? 2: 0;
	}

	//////////////////////////////
	// DROPS
	//////////////////////////////
	
	@Override
	public ArrayList<ItemStack> getDrops(net.minecraft.world.World _world, int x, int y, int z, int metadata, int fortune) {
		World world = new World(_world);
		DynamicTree tree = getTreeFromMetadata(metadata);
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();

		if(tree == null) {
			return ret;
		}

		int chance = this.func_150123_b(metadata);

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
		tree.getDrops(world, new BlockPos(x, y, z), chance, ret);

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
	public Item getItemDropped(int meta, Random random, int fortune) {
		return null;
	}

	@Override
	public int quantityDropped(Random random) {
		return 0;
	}

	@Override
	public int damageDropped(int metadata) {
		return 0;
	}

	//When the leaves are sheared just return vanilla leaves for usability
	@Override
	public ArrayList<ItemStack> onSheared(ItemStack item, IBlockAccess world, int x, int y, int z, int fortune) {
		int sub = getSubBlockNum(world, new BlockPos(x, y, z));
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
		ret.add(getTree(sub).getPrimitiveLeaves().toItemStack());
		return ret;
	}

	//////////////////////////////
	// RENDERING FUNCTIONS
	//////////////////////////////

	@Override
	public int getRadiusForConnection(BlockAccessDec blockAccess, BlockPos pos, BlockBranch from, int fromRadius) {
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

	//Gets the icon from the primitive block(Retains compatibility with Resource Packs)
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return getTreeFromMetadata(metadata).getPrimitiveLeaves().getIcon(side);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
	}

	//Returns the color this block should be rendered. Used by leaves.
	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderColor(int metadata) {
		return getTreeFromMetadata(metadata).foliageColorMultiplier(null, 0, 0, 0);
	}

	//A hack to retain vanilla minecraft leaves block colors in their biomes
	@Override
	@SideOnly(Side.CLIENT)
	public int colorMultiplier(IBlockAccess access, int x, int y, int z) {
		return getTree(new BlockAccessDec(access), new BlockPos(x, y, z)).foliageColorMultiplier(access, x, y, z);
	}

	@Override
	public boolean isFoliage(IBlockAccess world, int x, int y, int z) {
		return true;
	}

	@Override
	public int getRadius(BlockAccessDec blockAccess, BlockPos pos) {
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
	public int branchSupport(BlockAccessDec blockAccess, BlockBranch branch, BlockPos pos, EnumFacing dir, int radius) {
		//Leaves are only support for "twigs"
		return radius == 1 && branch.getTree() == getTree(blockAccess, pos) ? 0x01 : 0;
	}

	@Override
	public boolean applyItemSubstance(World world, BlockPos pos, EntityPlayer player, EnumHand hand, ItemStack itemStack) {
		return false;//Nothing is applied to leaves
	}

	@Override
	public int getMobilityFlag() {
		return 2;
	}

	//Included for compatibility.  Doesn't really seem to be needed in the way I use it.
	@Override
	public String[] func_150125_e() {
		return species;
	}

}
