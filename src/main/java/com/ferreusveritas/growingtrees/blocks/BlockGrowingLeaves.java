package com.ferreusveritas.growingtrees.blocks;

import java.util.ArrayList;
import java.util.Random;

import com.ferreusveritas.growingtrees.TreeHelper;
import com.ferreusveritas.growingtrees.items.Seed;
import com.ferreusveritas.growingtrees.trees.GrowingTree;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockGrowingLeaves extends BlockLeaves implements ITreePart {
 
    private String[] species = {"X", "X", "X", "X"};
	private GrowingTree trees[] = new GrowingTree[4];
	
	public BlockGrowingLeaves() {
		field_150121_P = true;//True for alpha transparent leaves
	}
	
	public void setTree(int sub, GrowingTree tree){
		trees[sub & 3] = tree;
		species[sub & 3] = tree.getName();
	}
	
	public GrowingTree getTree(int sub){
		return trees[sub & 3];
	}

	@Override
	public GrowingTree getTree(IBlockAccess blockAccess, int x, int y, int z) {
		return getTree(getSubBlockNum(blockAccess, x, y, z));
	}
	
	//Borrow flammability from the vanilla minecraft leaves
	@Override
    public int getFlammability(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
		return getTree(getSubBlockNum(world, x, y, z)).getPrimitiveLeaves().getBlock().getFlammability(world, x, y, z, face);
    }
	
	//Borrow fire spread rate from the vanilla minecraft leaves
	@Override
	public int getFireSpreadSpeed(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
		return getTree(getSubBlockNum(world, x, y, z)).getPrimitiveLeaves().getBlock().getFireSpreadSpeed(world, x, y, z, face);
    }

	//Pull the subblock number portion from the metadata 
	public static final int getSubBlockNumFromMetadata(int meta){
		return (meta >> 2) & 3;
	}
	
	//Pull the subblock from the world
	public static int getSubBlockNum(IBlockAccess world, int x, int y, int z){
		return getSubBlockNumFromMetadata(world.getBlockMetadata(x, y, z));
	}
	
	@Override
	public void updateTick(World world, int x, int y, int z, Random random){
		updateLeaves(world, x, y, z, random);
	}

	public void updateLeaves(World world, int x, int y, int z, Random random){
		int metadata = world.getBlockMetadata(x, y, z);
		int sub = getSubBlockNumFromMetadata(metadata);
		GrowingTree tree = getTree(sub);
		int preHydro = getHydrationLevelFromMetadata(metadata);
		
		//Check hydration level.  Dry leaves are dead leaves.
		int hydro = getHydrationLevelFromNeighbors(world, x, y, z, tree);
		if(hydro == 0 || !hasAdequateLight(world, x, y, z)){
			removeLeaves(world, x, y, z);//No water, no light .. no leaves
		} else { 
			//Encode new hydration level in metadata for this leaf
			if(preHydro != hydro){//A little performance gain
				setHydrationLevel(world, x, y, z, hydro);
			}
		}

		for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS){//Go on all 6 sides of this block
			growLeaves(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, tree);//Attempt to grow new leaves
		}

		//Do special things if the leaf block is/was on the bottom
		if(isBottom(world, x, y, z)){
			getTree(sub).bottomSpecial(world, x, y, z, random);
		}
	}

	@Override
    public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata) {
		
		ForgeDirection dir = ForgeDirection.getOrientation(side).getOpposite();
		
		int dx = x + dir.offsetX;
		int dy = y + dir.offsetY;
		int dz = z + dir.offsetZ;
	
		GrowingTree tree = TreeHelper.getSafeTreePart(world, dx, dy, dz).getTree(world, dx, dy, dz);
		
		if(tree != null && tree.getGrowingLeaves() == this){//Attempt to match the proper growing leaves for the tree being clicked on
			return tree.getGrowingLeavesSub() << 2;//Return matched metadata
		}
		
		return 0;
    }
	
    @Override
    public void breakBlock(World p_149749_1_, int p_149749_2_, int p_149749_3_, int p_149749_4_, Block p_149749_5_, int p_149749_6_){}
    
    @Override
    public void beginLeavesDecay(World world, int x, int y, int z){}
    
	//Set the block at the provided coords to a leaf block if local light, space and hydration requirements are met
	public void growLeaves(World world, int x, int y, int z, GrowingTree tree){
		Block block = world.getBlock(x,  y,  z);
		if(isLocationSuitableForNewLeaves(world, x, y, z)){
			int hydro = getHydrationLevelFromNeighbors(world, x, y, z, tree);
			setBlockToLeaves(world, x, y, z, tree.getGrowingLeavesSub(), hydro);
		}
	}
	
	//Set the block at the provided coords to a leaf block if local light and space requirements are met 
	public boolean growLeaves(World world, int x, int y, int z, int sub, int hydro){
		hydro = hydro == 0 ? getTree(sub).defaultHydration : hydro;
		if(isLocationSuitableForNewLeaves(world, x, y, z)){
			return setBlockToLeaves(world, x, y, z, sub, hydro);
		}
		return false;
	}
	
	//Test if the block at this location is capable of being grown into
	public boolean isLocationSuitableForNewLeaves(World world, int x, int y, int z){
		Block block = world.getBlock(x,  y,  z);
		Block belowBlock = world.getBlock(x, y - 1, z);
		
		//Prevent leaves from growing on the ground or above liquids
		if(belowBlock.isOpaqueCube() || belowBlock instanceof BlockLiquid){
			return false;
		}
		
		//Help to grow into double tall grass and ferns in a more natural way
		if(block == Blocks.double_plant){
			int meta = world.getBlockMetadata(x, y, z);
			if((meta & 8) != 0){//Top block of double plant 
				meta = world.getBlockMetadata(x, y - 1, z);
				if(meta == 2 || meta == 3){//tall grass or fern
					world.setBlockToAir(x, y, z);
					world.setBlock(x, y - 1, z, Blocks.tallgrass, meta - 1, 3);
				}
			}
		}
		
		return block.isAir(world, x, y, z) && hasAdequateLight(world, x, y, z);
	}
	
	/** Set the block at the provided coords to a leaf block and also set it's hydration value.
	* If hydration value is 0 then it sets the block to air
	*/
	public boolean setBlockToLeaves(World world, int x, int y, int z, int sub, int hydro){
		hydro = MathHelper.clamp_int(hydro, 0, 4);
		if(hydro != 0){
			world.setBlock(x, y, z, this, ((sub << 2) & 12) | ((hydro - 1) & 3), 3);
			return true;
		} else {
			removeLeaves(world, x, y, z);
			return false;
		}
	}
	
	/** Check to make sure the leaves have enough light to exist */
	public boolean hasAdequateLight(World world, int x, int y, int z){
		
		//If clear sky is above the block then we needn't go any further
		if(world.canBlockSeeTheSky(x, y, z)){
			return true;
		}
		
		GrowingTree tree = getTree(getSubBlockNum(world, x, y, z));
		int smother = tree.smotherLeavesMax;
		
		//Check to make sure there isn't too many leaves above this block.  Encourages forest canopy development.
		if(smother != 0){
			if(isBottom(world, x, y, z, world.getBlock(x, y - 1, z))){//Only act on the bottom block of the Growable stack
				//Prevent leaves from growing where they would be "smothered" from too much above foliage
				int smotherLeaves = 0;
				for(int i = 0; i < smother; i++){
					smotherLeaves += TreeHelper.isTreePart(world, x, y + i + 1, z) ? 1 : 0;
				}
				if(smotherLeaves >= smother){
					return false;
				}
			}
		}
		
		//Ensure the leaves don't grow in dark locations..  This creates a realistic canopy effect in forests and other nice stuff.
		//If there's already leaves here then don't kill them if it's a little dark
		//If it's empty space then don't create leaves unless it's sufficiently bright
		if(world.getSavedLightValue(EnumSkyBlock.Sky, x, y, z) >= (TreeHelper.isLeaves(world, x, y, z) ? 11 : 13)){//TODO: Make ranges agile
			return true;
		}
		
		return false;
	}

	/** Used to find if the leaf block is at the bottom of the stack */
	public static boolean isBottom(World world, int x, int y, int z){
		Block belowBlock = world.getBlock(x, y - 1, z);
		return isBottom(world, x, y, z, belowBlock);
	}
	
	/** Used to find if the leaf block is at the bottom of the stack */
	public static boolean isBottom(World world, int x, int y, int z, Block belowBlock){
		if(TreeHelper.isTreePart(belowBlock)){
			ITreePart belowTreepart = (ITreePart) belowBlock;
			return belowTreepart.getRadius(world, x, y - 1, z) > 1;//False for leaves, twigs, and dirt.  True for stocky branches
		}
		return true;//Non-Tree parts below indicate the bottom of stack
	}
	
	/** Gathers hydration levels from neighbors before pushing the values into the solver */
	public int getHydrationLevelFromNeighbors(IBlockAccess world, int x, int y, int z, GrowingTree tree){

		int nv[] = new int[16];//neighbor hydration values
		
		for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS){
			int dx = x + dir.offsetX;
			int dy = y + dir.offsetY;
			int dz = z + dir.offsetZ;
			nv[TreeHelper.getSafeTreePart(world, dx, dy, dz).getHydrationLevel(world, dx, dy, dz, dir, tree)]++;
		}

		return solveCell(nv, tree.cellSolution);//Find center cell's value from neighbors  
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
	public static int solveCell(int[] nv, short[] solution){
		for(int d: solution){
			if(nv[(d >> 8) & 15] >= ((d >> 4) & 15)){
				return d & 15;
			}
		}
		return 0;
	}
	
	public int getHydrationLevelFromMetadata(int meta){
		return (meta & 3) + 1;
	}
	
	public int getHydrationLevel(IBlockAccess blockAccess, int x, int y, int z) {
		return getHydrationLevelFromMetadata(blockAccess.getBlockMetadata(x, y, z));
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
	public int getHydrationLevel(IBlockAccess blockAccess, int x, int y, int z, ForgeDirection dir, GrowingTree leavesTree) {

		int metadata = blockAccess.getBlockMetadata(x, y, z);
		int hydro = getHydrationLevelFromMetadata(metadata);

		if(dir != null){
			GrowingTree tree = getTree(getSubBlockNumFromMetadata(metadata));
			if(leavesTree != tree){//Only allow hydration requests from the same type of leaves
				return 0;
			}
			short[] solution = tree.hydroSolution;
			if(solution != null){
				int dirBits = dir == ForgeDirection.DOWN ? 0x100 : dir == ForgeDirection.UP ? 0x200 : 0x400;
				for(int d: solution){
					if((d & dirBits) != 0){
						int hydroCond = (d >> 4) & 15;
						hydroCond = hydroCond == 15 ? hydro : hydroCond;//15 is special and means the actual hydro value
						int result = d & 15;
						result = result == 15 ? hydro : result;
						if(hydro == hydroCond){
							int op = (d >> 12) & 15;
							switch(op){
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

	public static void removeLeaves(World world, int x, int y, int z){
		world.setBlockToAir(x,  y,  z);
		world.notifyBlocksOfNeighborChange(x, y, z, Blocks.air);
	}
	
	//Variable hydration levels are only appropriate for leaf blocks
	public static void setHydrationLevel(World world, int x, int y, int z, int hydro){
		hydro = MathHelper.clamp_int(hydro, 0, 4);
		
		if(hydro == 0){
			removeLeaves(world, x, y, z);
		} else {
			int currMeta = world.getBlockMetadata(x, y, z);
			world.setBlockMetadataWithNotify(x, y, z, (currMeta & 12) | ((hydro - 1) & 3), 6);
		}
	}
	
	@Override
	public GrowSignal growSignal(World world, int x, int y, int z, GrowSignal signal) {
		if(signal.step()){//This is always placed at the beginning of every growSignal function
			branchOut(world, x, y, z, signal);//When a growth signal hits a leaf block it attempts to become a tree branch
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
	public boolean needLeaves(World world, int x, int y, int z, GrowingTree tree){
		if(world.isAirBlock(x, y, z)){//Place Leaves if Air
			return this.growLeaves(world, x, y, z, tree.getGrowingLeavesSub(), tree.defaultHydration);
		} else {//Otherwise check if there's already this type of leaves there.
			ITreePart treepart = TreeHelper.getSafeTreePart(world, x, y, z);
			return treepart == this && tree.getGrowingLeavesSub() == getSubBlockNum(world, x, y, z);//Check if this is the same type of leaves
		}
	}
	
	public GrowSignal branchOut(World world, int x, int y, int z, GrowSignal signal){
		
		GrowingTree tree = signal.getTree();

		//Check to be sure the placement for a branch is valid by testing to see if it would first support a leaves block
		if(!needLeaves(world, x, y, z, tree)){
			signal.success = false;
			return signal;
		}
		
		//Check to see if there's neighboring branches and abort if there's any found.
		ForgeDirection originDir = signal.dir.getOpposite();

		for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS){
			if(!dir.equals(originDir)){
				if(TreeHelper.isBranch(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ)){
					signal.success = false;
					return signal;
				}
			}
		}

		boolean hasLeaves = false;
		
		for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS){
			if(needLeaves(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, tree)){
				hasLeaves = true;
			}
		}
		
		if(hasLeaves){
			//Finally set the leaves block to a branch
			world.setBlock(x, y, z, signal.branchBlock, 0, 2);
			signal.radius = signal.getTree().secondaryThickness;//For the benefit of the parent branch
		}
		
		signal.success = hasLeaves;
		
		return signal;
	}
	
	@Override
	public int probabilityForBlock(IBlockAccess blockAccess, int x, int y, int z, BlockBranch from) {
		return from.getTree().isCompatibleGrowingLeaves(blockAccess, x, y, z) ? 2: 0;
	}

	
	//////////////////////////////
	// DROPS FUNCTIONS
	//////////////////////////////
	
    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
        int chance = this.func_150123_b(metadata);

        //Hokey fortune stuff here.
        if (fortune > 0) {
            chance -= 2 << fortune;
            if (chance < 10) chance = 10;
        }

        //It's mostly for seeds.. mostly.
        //Ignores quantityDropped() for Vanilla consistency and fortune compatibility.
        if (world.rand.nextInt(chance) == 0){
        	ret.add(new ItemStack(getSeedDropped(metadata)));
        }

        //More fortune contrivances here.  Vanilla compatible returns.
        chance = 200; //1 in 200 chance of returning an "apple"
        if (fortune > 0) {
            chance -= 10 << fortune;
            if (chance < 40) chance = 40;
        }

        //Get species specific drops.. apples or cocoa for instance
        getTree(getSubBlockNumFromMetadata(metadata)).getDrops(world, x, y, z, chance, ret);

        return ret;
    }
	
    @Override
    protected boolean canSilkHarvest() {
        return false;
    }

	//Drop a seed when the player destroys the block
    public Seed getSeedDropped(int meta){
    	return getTree(getSubBlockNumFromMetadata(meta)).getSeed();
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
    	int sub = getSubBlockNum(world, x, y, z);
        ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
        ret.add(getTree(sub).getPrimitiveLeaves().toItemStack());
        return ret;
    }

	//////////////////////////////
	// RENDERING FUNCTIONS
	//////////////////////////////
	
	@Override
	public int getRadiusForConnection(IBlockAccess blockAccess, int x, int y, int z, BlockBranch from, int fromRadius) {
		return fromRadius == 1 && from.getTree().isCompatibleGrowingLeaves(blockAccess, x, y, z) ? 1 : 0;
	}

	//Gets the icon from the primitive block(Retains compatibility with Resource Packs)
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return getTree(getSubBlockNumFromMetadata(metadata)).getPrimitiveLeaves().getIcon(side);
	}
	
    @Override
	@SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister) {
    }
	
    //Returns the color this block should be rendered. Used by leaves.
    @Override
	@SideOnly(Side.CLIENT)
    public int getRenderColor(int metadata) {
		BlockAndMeta primLeaves = getTree(getSubBlockNumFromMetadata(metadata)).getPrimitiveLeaves();
        return primLeaves.getBlock().getRenderColor(primLeaves.getMeta());
    }
	
    //A hack to retain vanilla minecraft leaves block colors in their biomes
    @Override
	@SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess access, int x, int y, int z) {

    	//ugly hack for rendering saplings
    	BlockBranch branch = TreeHelper.getBranch(access, x, y, z);
    	int sub = branch != null ? branch.getTree().getGrowingLeavesSub() : getSubBlockNum(access, x, y, z);//Hacky for sapling renderer
    	
    	BlockAndMeta primLeaves = getTree(sub).getPrimitiveLeaves();
    	if(primLeaves.matches(Blocks.leaves)){
        	return	
        		(primLeaves.getMeta() & 3) == 1 ? ColorizerFoliage.getFoliageColorPine() : 
        		(primLeaves.getMeta() & 3) == 2 ? ColorizerFoliage.getFoliageColorBirch() : 
        		super.colorMultiplier(access, x, y, z);//Oak or Jungle
    	}
    	
    	return super.colorMultiplier(access, x, y, z);//Something else
    }

    
/*	TODO: Particle effects. Future leaves dropping from trees and wisps and stuff. Client side only
	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random random){
		if(isBottom(world, x, y, z)){
			EntityFX leaf = new EntityParticleLeaf(world, x + 0.5d, y - 0.5d, z + 0.5d, 0, -0.2, 0);
			Minecraft.getMinecraft().effectRenderer.addEffect(leaf);
		}
	}
*/
    
    @Override
	public boolean isFoliage(IBlockAccess world, int x, int y, int z) {
        return true;
    }
    
	@Override
	public int getRadius(IBlockAccess blockAccess, int x, int y, int z) {
		return 0;
	}

	@Override
	public MapSignal analyse(World world, int x, int y, int z, ForgeDirection fromDir, MapSignal signal) {
		return signal;//Shouldn't need to run analysis on leaf blocks
	}

	@Override
	public boolean isRootNode() {
		return false;
	}

	@Override
	public int branchSupport(IBlockAccess blockAccess, BlockBranch branch, int x, int y, int z, ForgeDirection dir,	int radius) {
		//Leaves are only support for "twigs"
		return radius == 1 && branch.getTree() == getTree(blockAccess, x, y, z) ? 0x01 : 0;
	}

	@Override
	public boolean applyItemSubstance(World world, int x, int y, int z, EntityPlayer player, ItemStack itemStack){
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
