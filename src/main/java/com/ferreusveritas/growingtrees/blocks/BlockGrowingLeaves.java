package com.ferreusveritas.growingtrees.blocks;

import java.util.ArrayList;
import java.util.Random;

import com.ferreusveritas.growingtrees.TreeHelper;
import com.ferreusveritas.growingtrees.items.Seed;
import com.ferreusveritas.growingtrees.special.IBottomListener;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
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

    private static final String[] species = new String[4];
	private Seed seed[] = new Seed[4];//The seed used to create this tree and the seed that is dropped from it.
	private BlockAndMeta primitiveLeaves[] = new BlockAndMeta[4];//Whatever leaf block is set here will determine how it looks
	private int smotherLeavesMax[] = new int[4];//Maximum amount of leaves in a stack before the bottom-most leaf block dies
	private ArrayList<IBottomListener>[] bottomSpecials = (ArrayList<IBottomListener>[])new ArrayList[4]; //A list of special effects reserved for leaves on the bottom of a stack
	private byte defaultHydration[] = new byte[4];//The default hydration level of a newly created leaf block
	
	protected short hydroSolution[][] = new short[4][];
	protected short cellSolution[][] = new short[4][];
	
	public BlockGrowingLeaves() {
		field_150121_P = true;//True for alpha transparent leaves
		
		for(int sub = 0; sub < 4; sub++){
			setPrimitiveLeaves(sub, Blocks.leaves, 0);//Set to plain Oak leaves by default
			setSmother(sub, 4);//Decent default smother value
			defaultHydration[sub] = 4;//Maximum possible hydration value
			setSolvers(sub, TreeHelper.cellSolverDeciduous, TreeHelper.hydroSolverDeciduous);//Default solvers for deciduous trees
		}
	}
	
	public BlockGrowingLeaves setSpeciesName(int sub, String name){
		species[sub & 3] = name;
		return this;
	}
	
	public BlockGrowingLeaves setSmother(int sub, int smother){
		smotherLeavesMax[sub & 3] = smother;
		return this;
	}
	
	public BlockGrowingLeaves setDefaultHydration(int sub, int hydro){
		defaultHydration[sub & 3] = (byte) hydro;
		return this;
	}
	
	//Set "primitive" leaves.  These leaves are used for many purposes including rendering, drops, and some other basic behavior.
	public BlockGrowingLeaves setPrimitiveLeaves(int sub, Block block, int meta){
		primitiveLeaves[sub & 3] = new BlockAndMeta(block, meta);
		return this;
	}
	
	public BlockAndMeta getPrimitiveLeaves(int sub){
		return primitiveLeaves[sub & 3];
	}
	
	//Borrow flammability from the vanilla minecraft leaves
	@Override
    public int getFlammability(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
		int sub = getSubBlockNum(world, x, y, z);
		return getPrimitiveLeaves(sub).getBlock().getFlammability(world, x, y, z, face);
    }
	
	//Borrow fire spread rate from the vanilla minecraft leaves
	@Override
	public int getFireSpreadSpeed(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
		int sub = getSubBlockNum(world, x, y, z);
		return getPrimitiveLeaves(sub).getBlock().getFireSpreadSpeed(world, x, y, z, face);
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
		int preHydro = getHydrationLevelFromMetadata(metadata);
		
		//Check hydration level.  Dry leaves are dead leaves.
		int hydro = getHydrationLevelFromNeighbors(world, x, y, z, sub);
		if(hydro == 0 || !hasAdequateLight(world, x, y, z)){
			world.setBlockToAir(x, y, z);//No water, no light .. no leaves
		} else { 
			//Encode new hydration level in metadata for this leaf
			if(preHydro != hydro){//A little performance gain
				setHydrationLevel(world, x, y, z, hydro);
			}
		}

		for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS){//Go on all 6 sides of this block
			growLeaves(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, sub);//Attempt to grow new leaves
		}

		//Do special things if the leaf block is/was on the bottom
		if(isBottom(world, x, y, z)){
			bottomSpecial(world, x, y, z, sub, random);
		}
	}
	
	/*
	@Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float px, float py, float pz){

		ItemStack equippedItem = player.getCurrentEquippedItem();
		
		if(equippedItem == null){//Bare hand
			if(world.isRemote){
				int metadata = world.getBlockMetadata(x, y, z);
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Leaves Metadata: " + Integer.toBinaryString(metadata)));
			}
		}

		return false;
    }
    */

	@Override
    public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata) {
		
		ForgeDirection dir = ForgeDirection.getOrientation(side).getOpposite();
		
		int dx = x + dir.offsetX;
		int dy = y + dir.offsetY;
		int dz = z + dir.offsetZ;
	
		if(TreeHelper.getSafeTreePart(world, dx, dy, dz).getGrowingLeaves(world, dx, dy, dz) == this){//Attempt to match the proper growing leaves for the tree being clicked on
			return TreeHelper.getSafeTreePart(world, dx, dy, dz).getGrowingLeavesSub(world, dx, dy, dz) << 2;
		}
		
		return 0;
    }
	
    @Override
    public void breakBlock(World p_149749_1_, int p_149749_2_, int p_149749_3_, int p_149749_4_, Block p_149749_5_, int p_149749_6_){}
    
    @Override
    public void beginLeavesDecay(World world, int x, int y, int z){}
    
	//Set the block at the provided coords to a leaf block if local light, space and hydration requirements are met
	public void growLeaves(World world, int x, int y, int z, int sub){
		if(world.isAirBlock(x, y, z) && hasAdequateLight(world, x, y, z)){
			int hydro = getHydrationLevelFromNeighbors(world, x, y, z, sub);
			setBlockToLeaves(world, x, y, z, sub, hydro);
		}
	}
	
	//Set the block at the provided coords to a leaf block if local light and space requirements are met 
	public boolean growLeaves(World world, int x, int y, int z, int sub, int hydro){
		hydro = hydro == 0 ? defaultHydration[hydro] : hydro;
		if(world.isAirBlock(x, y, z) && hasAdequateLight(world, x, y, z)){
			return setBlockToLeaves(world, x, y, z, sub, hydro);
		}
		return false;
	}
	
	//Set the block at the provided coords to a leaf block and also set it's hydration value.
	//If hydration value is 0 then it sets the block to air
	public boolean setBlockToLeaves(World world, int x, int y, int z, int sub, int hydro){
		hydro = MathHelper.clamp_int(hydro, 0, 4);
		if(hydro != 0){
			world.setBlock(x, y, z, this, ((sub << 2) & 12) | ((hydro - 1) & 3), 3);
			return true;
		} else {
			world.setBlockToAir(x, y, z);
			return false;
		}
	}
	
	//Check to make sure the leaves have enough light to exist
	public boolean hasAdequateLight(World world, int x, int y, int z){
		
		Block belowBlock = world.getBlock(x, y - 1, z);
		
		//Prevent leaves from growing on the ground
		if(belowBlock.isOpaqueCube()){
			return false;
		}
		
		//If clear sky is above the block then we needn't go any further
		if(world.canBlockSeeTheSky(x, y, z)){
			return true;
		}
		
		int sub = getSubBlockNum(world, x, y, z);
		int smother = smotherLeavesMax[sub];
		
		//Check to make sure there isn't too many leaves above this block.  Encourages forest canopy development.
		if(smother != 0){
			if(isBottom(world, x, y, z, belowBlock)){//Only act on the bottom block of the Growable stack
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

	//Used to find if the leaf block is at the bottom of the stack
	public static boolean isBottom(World world, int x, int y, int z){
		Block belowBlock = world.getBlock(x, y - 1, z);
		return isBottom(world, x, y, z, belowBlock);
	}
	
	//Used to find if the leaf block is at the bottom of the stack
	public static boolean isBottom(World world, int x, int y, int z, Block belowBlock){
		if(TreeHelper.isTreePart(belowBlock)){
			ITreePart belowTreepart = (ITreePart) belowBlock;
			return belowTreepart.getRadius(world, x, y - 1, z) > 1;//False for leaves, twigs, and dirt.  True for stocky branches
		}
		return true;//Non-Tree parts below indicate the bottom of stack
	}
	
	//Gathers hydration levels from neighbors before pushing the values into the solver
	public int getHydrationLevelFromNeighbors(IBlockAccess world, int x, int y, int z, int sub){

		int nv[] = new int[16];//neighbor hydration values
		
		for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS){
			int dx = x + dir.offsetX;
			int dy = y + dir.offsetY;
			int dz = z + dir.offsetZ;
			nv[TreeHelper.getSafeTreePart(world, dx, dy, dz).getHydrationLevel(world, dx, dy, dz, dir, this, sub)]++;
		}

		return solveCell(nv, cellSolution[sub]);//Find center cell's value from neighbors  
    }

	public BlockGrowingLeaves setSolvers(int sub, short[] cellSolution, short[] hydroSolution){
		setCellSolver(sub, cellSolution);
		setHydroSolver(sub, hydroSolution);
		return this;
	}
	
	public BlockGrowingLeaves setCellSolver(int sub, short[] solution){
		cellSolution[sub] = solution;
		return this;
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
	public int solveCell(int[] nv, short[] solution){
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

	public BlockGrowingLeaves setHydroSolver(int sub, short[] solution){
		hydroSolution[sub] = solution;
		return this;
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
	public int getHydrationLevel(IBlockAccess blockAccess, int x, int y, int z, ForgeDirection dir, BlockGrowingLeaves fromBlock, int fromSub) {

		int metadata = blockAccess.getBlockMetadata(x, y, z);
		int hydro = getHydrationLevelFromMetadata(metadata);

		if(dir != null){
			int sub = getSubBlockNumFromMetadata(metadata);
			if(fromBlock != this || sub != fromSub){//Only allow hydration requests from the same type of leaves
				return 0;
			}
			short[] solution = hydroSolution[sub];
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
	
	//Variable hydration levels are only appropriate for leaf blocks
	public static void setHydrationLevel(World world, int x, int y, int z, int hydro){
		hydro = MathHelper.clamp_int(hydro, 0, 4);
		
		if(hydro == 0){
			world.setBlockToAir(x,  y,  z);
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

	public boolean needLeaves(World world, int x, int y, int z, int sub, int hydro){
		if(world.isAirBlock(x, y, z)){
			return this.growLeaves(world, x, y, z, sub, hydro);
		} else {
			ITreePart treepart = TreeHelper.getSafeTreePart(world, x, y, z);
			return treepart == this && sub == getSubBlockNum(world, x, y, z);//Check if this is the same type of leaves
		}
	}
	
	public GrowSignal branchOut(World world, int x, int y, int z, GrowSignal signal){
		
		int sub = signal.branchBlock.getGrowingLeavesSub();

		//Check to be sure the placement for a branch is valid by testing to see if it would first support a leaves block
		if(!needLeaves(world, x, y, z, sub, defaultHydration[sub])){
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
			if(needLeaves(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, sub, defaultHydration[sub])){
				hasLeaves = true;
			}
		}
		
		if(hasLeaves){
			//Finally set the leaves block to a branch
			world.setBlock(x, y, z, signal.branchBlock, 0, 2);
			signal.radius = signal.branchBlock.secondaryThickness;//For the benefit of the parent branch
		}
		
		signal.success = hasLeaves;
		
		return signal;
	}

	
	@Override
	public int probabilityForBlock(IBlockAccess blockAccess, int x, int y, int z, BlockBranch from) {
		return from.isCompatibleGrowingLeaves(blockAccess, this, x, y, z) ? 2: 0;
	}

	//////////////////////////////
	//Bottom Special
	//////////////////////////////
	
	/**
	 * Run special effects for bottom blocks 
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @param sub
	 * @param random
	 */
	public void bottomSpecial(World world, int x, int y, int z, int sub, Random random){
		if(bottomSpecials[sub & 3] != null){
			for(IBottomListener special: bottomSpecials[sub]){
				float chance = special.chance();
				if(chance != 0.0f && random.nextFloat() <= chance){
					special.run(world, this, x, y, z, sub & 3, random);//Make it so!
				}
			}
		}
	}

	/**
	 * Provides an interface for other mods to add special effects like fruit, spawns or whatever 
	 * @param sub
	 * @param specials
	 * @return
	 */
	public BlockGrowingLeaves registerBottomSpecials(int sub, IBottomListener ... specials){
		for(IBottomListener special: specials){
			if(bottomSpecials[sub & 3] == null){
				bottomSpecials[sub & 3] = new ArrayList<IBottomListener>();
			}
			bottomSpecials[sub & 3].add(special);
		}
		return this;
	}
	
	//////////////////////////////
	//Drops Functions
	//////////////////////////////
	
	public BlockGrowingLeaves setSeed(Seed seed, int sub){
		this.seed[sub & 3] = seed;
		return this;
	}
	
	public Seed getSeed(int sub){
		return this.seed[sub & 3];
	}
	
	//Drop a seed when the player destroys the block
    @Override
    public Item getItemDropped(int meta, Random random, int fortune) {
    	int sub = (meta >> 2) & 3;
        return getSeed(sub);
    }

    //1 in 64 chance to drop a seed on destruction
    @Override
    public int quantityDropped(Random random) {
        return random.nextInt(64) == 0 ? 1 : 0;
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
        ret.add(getPrimitiveLeaves(sub).toItemStack());
        return ret;
    }

	//////////////////////////////
	//Rendering Functions
	//////////////////////////////
	
	@Override
	public int getRadiusForConnection(IBlockAccess blockAccess, int x, int y, int z, BlockBranch from, int fromRadius) {
		return fromRadius == 1 && from.isCompatibleGrowingLeaves(blockAccess, this, x, y, z) ? 1 : 0;
	}

	//Gets the icon from the primitive block(Retains compatibility with Resource Packs)
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		int sub = getSubBlockNumFromMetadata(metadata);
		return getPrimitiveLeaves(sub).getIcon(side);
	}
	
    @Override
	@SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister) {
    }
	
    //Returns the color this block should be rendered. Used by leaves.
    @Override
	@SideOnly(Side.CLIENT)
    public int getRenderColor(int metadata) {
		int sub = getSubBlockNumFromMetadata(metadata);
        return getPrimitiveLeaves(sub).getBlock().getRenderColor(getPrimitiveLeaves(sub).getMeta());
    }
	
    //A hack to retain vanilla minecraft leaves block colors in their biomes
    @Override
	@SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess access, int x, int y, int z) {

    	//ugly hack for rendering saplings
    	BlockBranch branch = TreeHelper.getBranch(access, x, y, z);
    	int sub = branch != null ? branch.getGrowingLeavesSub() : getSubBlockNum(access, x, y, z);//Hacky for sapling renderer
    	
    	if(getPrimitiveLeaves(sub).matches(Blocks.leaves)){
        	return	
        		(getPrimitiveLeaves(sub).getMeta() & 3) == 1 ? ColorizerFoliage.getFoliageColorPine() : 
        		(getPrimitiveLeaves(sub).getMeta() & 3) == 2 ? ColorizerFoliage.getFoliageColorBirch() : 
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
		return radius == 1 && this == branch.getGrowingLeaves() && BlockGrowingLeaves.getSubBlockNum(blockAccess, x, y, z) == branch.getGrowingLeavesSub()? 0x01 : 0;
	}

	@Override
	public BlockGrowingLeaves getGrowingLeaves(IBlockAccess blockAccess, int x, int y, int z) {
		return this;
	}

	@Override
	public int getGrowingLeavesSub(IBlockAccess blockAccess, int x, int y, int z) {
		return getSubBlockNum(blockAccess, x, y, z);
	}

	@Override
	public boolean applySubstance(World world, int x, int y, int z, ItemStack itemStack) {
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
