package com.ferreusveritas.dynamictrees.blocks;

import java.util.ArrayList;
import java.util.Random;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.IAgeable;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.network.GrowSignal;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.util.IRegisterable;

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
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
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
	
	public DynamicTree getTree(int sub) {
		return trees[sub & 3];
	}

	@Override
	public DynamicTree getTree(IBlockAccess blockAccess, BlockPos pos) {
		return getTree(getSubBlockNum(blockAccess, pos));
	}

	public DynamicTree getTree(IBlockState blockState) {
		return getTreeFromMetadata(blockState.getMeta());
	}

	public DynamicTree getTreeFromMetadata(int metadata) {
		return getTree(getSubBlockNumFromMetadata(metadata));
	}

	//Pull the subblock number portion from the metadata 
	public static final int getSubBlockNumFromMetadata(int meta){
		return (meta >> 2) & 3;
	}

	//Pull the subblock from the world
	public static int getSubBlockNum(IBlockAccess blockAccess, BlockPos pos) {
		return getSubBlockNumFromMetadata(pos.getMeta(blockAccess));
	}
	
	//Borrow flammability from the vanilla minecraft leaves
	@Override
	public int getFlammability(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
		return getTree(world, new BlockPos(x, y, z)).getPrimitiveLeaves().getBlock().getFlammability(world, x, y, z, face);
	}
	
	//Borrow fire spread rate from the vanilla minecraft leaves
	@Override
	public int getFireSpreadSpeed(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
		return getTree(world, new BlockPos(x, y, z)).getPrimitiveLeaves().getBlock().getFireSpreadSpeed(world, x, y, z, face);
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random rand) {
		//if(random.nextInt() % 4 == 0) {
			age(world, new BlockPos(x, y, z), rand, false);
		//}
	}

	@Override
	public void age(World world, BlockPos pos, Random rand, boolean fast) {
		int metadata = pos.getMeta(world);
		DynamicTree tree = getTreeFromMetadata(metadata);
		int preHydro = getHydrationLevelFromMetadata(metadata);

		//Check hydration level.  Dry leaves are dead leaves.
		int hydro = getHydrationLevelFromNeighbors(world, pos, tree);
		if(hydro == 0 || !hasAdequateLight(world, tree, pos)){
			removeLeaves(world, pos);//No water, no light .. no leaves
		} else { 
			//Encode new hydration level in metadata for this leaf
			if(preHydro != hydro) {//A little performance gain
				setHydrationLevel(world, pos, hydro, metadata);
			}
		}

		for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS) {//Go on all 6 sides of this block
			growLeaves(world, tree, pos.offset(dir));//Attempt to grow new leaves
		}

		//Do special things if the leaf block is/was on the bottom
		if(!fast && isBottom(world, pos)) {
			tree.bottomSpecial(world, pos, rand);
		}
	}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata) {
		
		ForgeDirection dir = ForgeDirection.getOrientation(side).getOpposite();
		
		BlockPos deltaPos = new BlockPos(x, y, z).offset(dir);

		DynamicTree tree = TreeHelper.getSafeTreePart(world, deltaPos).getTree(world, deltaPos);

		if(tree != null && tree.getGrowingLeaves() == this) {//Attempt to match the proper growing leaves for the tree being clicked on
			return tree.getGrowingLeavesSub() << 2;//Return matched metadata
		}

		return 0;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int metadata){}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return AxisAlignedBB.getBoundingBox(x + 0.25, y, z + 0.25, x + 0.75, y + 0.50, z + 0.75);
	}

	@Override
	public void onFallenUpon(World world, int x, int y, int z, Entity entity, float fallDistance) {
		onFallenUpon(world, new BlockPos(x, y, z), entity, fallDistance);
	}

	public void onFallenUpon(World world, BlockPos pos, Entity entity, float fallDistance) {

		if(entity instanceof EntityLivingBase) { //We are only interested in Living things crashing through the canopy.
			entity.fallDistance--;

			AxisAlignedBB aabb = entity.boundingBox;
			
			int minX = MathHelper.floor_double(aabb.minX + 0.001D);
			int minZ = MathHelper.floor_double(aabb.minZ + 0.001D);
			int maxX = MathHelper.floor_double(aabb.maxX - 0.001D);
			int maxZ = MathHelper.floor_double(aabb.maxZ - 0.001D);

			boolean crushing = true;
			boolean hasLeaves = true;

			float volume = MathHelper.clamp_float(stepSound.getVolume() / 16.0f * fallDistance, 0, 3.0f);
			world.playSoundAtEntity(entity, this.stepSound.getBreakSound(), volume, this.stepSound.getPitch());

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
						if (!iPos.isAirBlock(world)) {
							crushing = false;//We hit something solid thus no longer crushing leaves layers
						}
					}
				}
			}
		}
	}

	public void crushBlock(World world, BlockPos pos, Entity entity) {

		if(world.isRemote) {
			Random random = world.rand;
			ITreePart treePart = TreeHelper.getTreePart(world, pos);
			if(treePart instanceof BlockDynamicLeaves) {
				DynamicTree tree = treePart.getTree(world, pos);
				if(tree != null) {
					int metadata = pos.getMeta(world);
					for(int dz = 0; dz < 8; dz++) {
						for(int dy = 0; dy < 8; dy++) {
							for(int dx = 0; dx < 8; dx++) {
								if(random.nextInt(8) == 0) {
									double fx = pos.getX() + dx / 8.0;
									double fy = pos.getY() + dy / 8.0;
									double fz = pos.getZ() + dz / 8.0;
									DynamicTrees.proxy.addDustParticle(world, fx, fy, fz, 0, MathHelper.randomFloatClamp(random, 0, (float) entity.motionY), 0, dx, dy, dz, this, metadata);
								}
							}
						}
					}
				}
			}
		}

		pos.setBlockToAir(world);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
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
	public void beginLeavesDecay(World world, int x, int y, int z) {}

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
		Block block = pos.getBlock(world);
		
		if(block instanceof BlockDynamicLeaves) {
			return false;
		}

		BlockPos belowPos = pos.down();
		Block belowBlock = belowPos.getBlock(world);

		//Prevent leaves from growing on the ground or above liquids
		if(belowBlock.isOpaqueCube() || belowBlock instanceof BlockLiquid) {
			return false;
		}

		//Help to grow into double tall grass and ferns in a more natural way
		if(block == Blocks.double_plant){
			int meta = pos.getMeta(world);
			if((meta & 8) != 0) {//Top block of double plant 
				meta = belowPos.getMeta(world);
				if(meta == 2 || meta == 3) {//tall grass or fern
					world.setBlockToAir(pos.getX(), pos.getY(), pos.getZ());
					world.setBlock(belowPos.getX(), belowPos.getY(), belowPos.getZ(), Blocks.tallgrass, meta - 1, 3);
				}
			}
		}

		return pos.isAirBlock(world) && hasAdequateLight(world, tree, pos);
	}

	/** Set the block at the provided coords to a leaf block and also set it's hydration value.
	* If hydration value is 0 then it sets the block to air
	*/
	public boolean setBlockToLeaves(World world, DynamicTree tree, BlockPos pos, int hydro) {
		hydro = MathHelper.clamp_int(hydro, 0, 4);
		if(hydro != 0) {
			int sub = tree.getGrowingLeavesSub();
			world.setBlock(pos.getX(), pos.getY(), pos.getZ(), this, ((sub << 2) & 12) | ((hydro - 1) & 3), 2);//Removed Notify Neighbors Flag for performance
			return true;
		} else {
			removeLeaves(world, pos);
			return false;
		}
	}

	/** Check to make sure the leaves have enough light to exist */
	public boolean hasAdequateLight(World world, DynamicTree tree, BlockPos pos) {

		//If clear sky is above the block then we needn't go any further
		if(world.canBlockSeeTheSky(pos.getX(), pos.getY(), pos.getZ())) {
			return true;
		}

		int smother = tree.getSmotherLeavesMax();

		//Check to make sure there isn't too many leaves above this block.  Encourages forest canopy development.
		if(smother != 0){
			if(isBottom(world, pos, pos.down().getBlock(world))) {//Only act on the bottom block of the Growable stack
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
		if(world.getSavedLightValue(EnumSkyBlock.Sky, pos.getX(), pos.getY(), pos.getZ()) >= (TreeHelper.isLeaves(world, pos) ? tree.getLightRequirement() - 2 : tree.getLightRequirement())) {
			return true;
		}

		return false;
	}

	/** Used to find if the leaf block is at the bottom of the stack */
	public static boolean isBottom(World world, BlockPos pos) {
		Block belowBlock = pos.down().getBlock(world);
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

		int nv[] = new int[16];//neighbor hydration values

		for(EnumFacing dir: EnumFacing.VALUES) {
			BlockPos deltaPos = pos.offset(dir);
			int val = TreeHelper.getSafeTreePart(world, deltaPos).getHydrationLevel(world, deltaPos, dir, tree);
			nv[val]++;
		}
		
		return solveCell(nv, tree.getCellSolution());//Find center cell's value from neighbors
	}

	/**
	* Cellular automata function that determines the behavior of the center cell from it's neighbors.
	* Values here are the number of neighbors for each hydration level.  Must be 16 elements.
	* Override member function to create unique species behavior
	*	4 Hex digits.. 0xXHCR  
	*	X: Reserved
	*	H: Selected hydration value
	*	C: Minimum count of neighbor blocks with selected hydration H
	*	R: Resulting Hydration
	*
	* Example:
	*	exampleSolver = 0x0514, 0x0413, 0x0312, 0x0211
	*	0x0514.. (5 X 1 = 4)  If there's 1 or more neighbor blocks with hydration 5 then make this block hydration 4
	* 
	* @param nv Array of counts of neighbor hydration values
	* @param solution Array of solver elements to solve the cell automata
	* @return
	*/
	public static int solveCell(int[] nv, short[] solution) {
		for(int d: solution) {
			if(nv[(d >> 8) & 15] >= ((d >> 4) & 15)) {
				return d & 15;
			}
		}
		return 0;
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

	public int getHydrationLevel(IBlockAccess blockAccess, BlockPos pos) {
		return getHydrationLevelFromMetadata(pos.getMeta(blockAccess));
	}

	/**
	* 0xODHR:Operation DirectionMask Hydrovalue Result
	*
	* Operations:
	*	0: return Result only
	*	1: return sum of Hydro and Result
	*	2: return diff of Hydro and Result
	*	3: return product of Hydro and Result
	*	4: return dividend of Hydro and Result
	* 
	*Directions bits:
	*	0x0100:Down
	*	0x0200:Up
	*	0x0400:Horizontal
	*
	*Hydrovalue:
	*	Any number you want to conditionally compare.  15(F) means any Hydrovalue
	*
	*Result:
	*	The number to return
	*
	*Example:
	*	solverData = { 0x02F0, 0x0144, 0x0742, 0x0132, 0x0730 } 
	*
	*	INIT ->	if dir or solution is undefined then return hydro
	*	02F0 -> else if dir is up(2) and hydro is equal to hydro(F) then return 0 (Always returns zero when direction is up)
	*	0144 -> else if dir is down(1) and hydro is equal to 4 then return 4
	*	0742 -> else if dir is any(7) and hydro is equal to 4 then return 2
	*	0132 -> else if dir is down(1) and hydro is equal to 3 then return 2
	*	0730 -> else if dir is any(7) and hydro is equal to 3 then return 0
	*	else return hydro
	*/
	@Override
	public int getHydrationLevel(IBlockAccess blockAccess, BlockPos pos, EnumFacing dir, DynamicTree leavesTree) {

		IBlockState state = pos.getBlockState(blockAccess);
		int hydro = getHydrationLevel(state);
		
		if(dir != null) {
			DynamicTree tree = getTree(state);
			if(tree == null) {
				return 0;
			}
			if(leavesTree != tree) {//Only allow hydration requests from the same type of leaves
				return 0;
			}
			short[] solution = tree.getHydroSolution();
			if(solution != null) {
				int dirBits = dir == EnumFacing.DOWN ? 0x100 : dir == EnumFacing.UP ? 0x200 : 0x400;
				for(int d: solution) {
					if((d & dirBits) != 0) {
						int hydroCond = (d >> 4) & 15;
						hydroCond = hydroCond == 15 ? hydro : hydroCond;//15 is special and means the actual hydro value
						int result = d & 15;
						result = result == 15 ? hydro : result;
						if(hydro == hydroCond) {
							int op = (d >> 12) & 15;
							switch(op) {
							case 0: break;
							case 1: result = hydro + result; break;
							case 2: result = hydro - result; break;
							case 3: result = hydro * result; break;
							case 4: result = hydro / result; break;
							default: break;
							}
							return MathHelper.clamp_int(result, 0, 4);
						}
					}
				}
			}
		}

		return hydro;
	}

	public static void removeLeaves(World world, BlockPos pos) {
		world.setBlockToAir(pos.getX(), pos.getY(), pos.getZ());
		world.notifyBlocksOfNeighborChange(pos.getX(), pos.getY(), pos.getZ(), Blocks.air);
	}
	
	//Variable hydration levels are only appropriate for leaf blocks
	public static void setHydrationLevel(World world, BlockPos pos, int hydro, int currMeta) {
		hydro = MathHelper.clamp_int(hydro, 0, 4);
		
		if(hydro == 0) {
			removeLeaves(world, pos);
		} else {
			//We do not use the 0x02 flag(update client) for performance reasons.  The clients do not need to know the hydration level of the leaves blocks as it
			//does not affect appearance or behavior.  For the same reason we use the 0x04 flag to prevent the block from being re-rendered.
			world.setBlockMetadataWithNotify(pos.getX(), pos.getY(), pos.getZ(), (currMeta & 12) | ((hydro - 1) & 3), 4);
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
		if(world.isAirBlock(pos.getX(), pos.getY(), pos.getZ())){//Place Leaves if Air
			return this.growLeaves(world, tree, pos, tree.getDefaultHydration());
		} else {//Otherwise check if there's already this type of leaves there.
			ITreePart treepart = TreeHelper.getSafeTreePart(world, pos);
			return treepart == this && tree.getGrowingLeavesSub() == getSubBlockNum(world, pos);//Check if this is the same type of leaves
		}
	}

	public GrowSignal branchOut(World world, BlockPos pos, GrowSignal signal) {

		DynamicTree tree = signal.getTree();

		//Check to be sure the placement for a branch is valid by testing to see if it would first support a leaves block
		if(!needLeaves(world, pos, tree)){
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
			world.setBlock(pos.getX(), pos.getY(), pos.getZ(), signal.branchBlock, 0, 2);
			signal.radius = signal.getTree().getSecondaryThickness();//For the benefit of the parent branch
		}

		signal.success = hasLeaves;

		return signal;
	}

	@Override
	public int probabilityForBlock(IBlockAccess blockAccess, BlockPos pos, BlockBranch from) {
		return from.getTree().isCompatibleGrowingLeaves(blockAccess, pos) ? 2: 0;
	}

	//////////////////////////////
	// DROPS
	//////////////////////////////
	
	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
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
			ret.add(new ItemStack(tree.getSeed()));
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
	public int getRadiusForConnection(IBlockAccess blockAccess, BlockPos pos, BlockBranch from, int fromRadius) {
		return fromRadius == 1 && from.getTree().isCompatibleGrowingLeaves(blockAccess, pos) ? 1 : 0;
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
		return getTree(access, new BlockPos(x, y, z)).foliageColorMultiplier(access, x, y, z);
	}

	@Override
	public boolean isFoliage(IBlockAccess world, int x, int y, int z) {
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
	public boolean applyItemSubstance(World world, BlockPos pos, EntityPlayer player, ItemStack itemStack) {
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
