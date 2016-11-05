package com.ferreusveritas.growingtrees.trees;

import java.util.ArrayList;
import java.util.Random;

import com.ferreusveritas.growingtrees.ConfigHandler;
import com.ferreusveritas.growingtrees.GrowingTrees;
import com.ferreusveritas.growingtrees.TreeHelper;
import com.ferreusveritas.growingtrees.blocks.BlockAndMeta;
import com.ferreusveritas.growingtrees.blocks.BlockBranch;
import com.ferreusveritas.growingtrees.blocks.BlockGrowingLeaves;
import com.ferreusveritas.growingtrees.blocks.BlockRootyDirt;
import com.ferreusveritas.growingtrees.blocks.GrowSignal;
import com.ferreusveritas.growingtrees.items.Seed;
import com.ferreusveritas.growingtrees.special.BottomListenerDropItems;
import com.ferreusveritas.growingtrees.special.IBottomListener;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * All data related to a tree species
 * @author ferreusveritas
 */
public class GrowingTree {

	/** Simple name of the tree e.g. "oak" */
	protected String name;
	
	//Branches
	/** The growing branch used by this tree */
	BlockBranch growingBranch;
	/** The primitive(vanilla) log to base the texture, drops, and other behavior from */
	BlockAndMeta primitiveLog;
	/** How quickly the branch thickens on it's own without branch merges */
	public float tapering;
	/** The probability that the direction decider will choose up out of the other possible direction weights */
	public int upProbability;
	/** Thickness of the branch connected to a twig(radius == 1) */
	public float secondaryThickness;
	/** Number of blocks high we have to be before a branch is allowed to form */
	public int lowestBranchHeight;
	/** Number of times a grow signal retries before failing. Affects growing speed */
	public int retries;
	/** Ideal signal energy. Greatest possible height that branches can reach from the root node */
	public float signalEnergy;
	
	//Dirt
	/** Ideal growth rate */
	public float growthRate;
	/** Ideal soil longevity */
	public int soilLongevity;
	
	//Leaves
	/** The growing leaves used by this tree */
	BlockGrowingLeaves growingLeaves;
	/** A growing leaves block needs a subblock number to specify which subblock we are working with **/
	int leavesSubBlock;
	/** Maximum amount of leaves in a stack before the bottom-most leaf block dies **/
	public int smotherLeavesMax;
	/** A list of special effects reserved for leaves on the bottom of a stack **/
	private ArrayList<IBottomListener> bottomSpecials = new ArrayList<IBottomListener>(4);
	/** The default hydration level of a newly created leaf block **/
	public byte defaultHydration;
	/** Automata input data for hydration solver */
	public short hydroSolution[];
	/** Automata input data for cell solver */
	public short cellSolution[];
	/** The primitive(vanilla) leaves are used for many purposes including rendering, drops, and some other basic behavior. */
	BlockAndMeta primitiveLeaves;

	ItemStack stick;
	
	//Seed
	/** The seed used to reproduce this tree.  Drops from the tree and can plant itself */
	Seed seed;

	/** Only growing trees mod should use this */
	public GrowingTree(String name, int seq){
		this(GrowingTrees.MODID, name, seq);
	}
	
	/** Constructor suitable for derivative mods */
	public GrowingTree(String modid, String name, int seq){
		this.name = name;
		
		//Some generic defaults that will be inherited by all tree branches
		//Branches
		tapering = 0.3f;
		upProbability = 2;
		secondaryThickness = 2.0f;//This should probably always be 2
		lowestBranchHeight = 3;//High enough to walk under
		retries = 0;
		signalEnergy = 16.0f;

		//Dirt
		growthRate = 1.0f;
		soilLongevity = 8;
		
		//Leaves
		defaultHydration = 4;
		smotherLeavesMax = 4;
		//Default solvers for deciduous trees
		cellSolution = TreeHelper.cellSolverDeciduous;
		hydroSolution = TreeHelper.hydroSolverDeciduous;
		
		setPrimitiveLeaves(Blocks.leaves, 0);//Set to plain Oak leaves by default
		setPrimitiveLog(Blocks.log, 0);//Set to plain Oak log by default

		setGrowingLeaves(TreeHelper.getLeavesBlockForSequence(modid, name, seq), seq & 3);
		setGrowingBranch(new BlockBranch());
		growingBranch.setBlockName(modid + "_" + name + "branch");
		setSeed((Seed) new Seed().setTextureName(modid + ":" + name + "seed").setCreativeTab(GrowingTrees.growingTreesTab).setUnlocalizedName(modid + "_" + name + "seed"));
		
		setStick(new ItemStack(Items.stick));
		
		registerBottomSpecials(new BottomListenerDropItems(new ItemStack(getSeed()), ConfigHandler.seedDropRate, true));
	}

	
	public boolean applySubstance(World world, int x, int y, int z, BlockRootyDirt dirt, ItemStack itemStack){
		
		//Bonemeal fertilizes the soil
		if( itemStack.getItem() == Items.dye && itemStack.getItemDamage() == 15) {
			return dirt.substanceFertilize(world, x, y, z, 1);
		}
		
		if( itemStack.getItem() == Items.potionitem){
			switch(itemStack.getItemDamage()){
			case 8268://Harming
			case 8236://Harming II
				return dirt.substanceDeplete(world, x, y, z, 15);
			case 8196://Poison
			case 8260://Poison(long)
			case 8228://Poison II
				return dirt.substanceDisease(world, x, y, z);
			case 8194://Swiftness
			case 8258://Swiftness(long)
			case 8226://Swiftness II
				return dirt.substanceInstantGrowth(world, x, y, z);
			case 8201://Strength
			case 8265://Strength(long)
			case 8233://Strength II
				return dirt.substanceFertilize(world, x, y, z, 15);
			case 8193://Regeneration
			case 8257://Regeneration(long)
			case 8225://Regeneration II
				return dirt.substanceFreeze(world, x, y, z);
			default:
				return false;
			}
		}
		
		return false;
	}
	
	//////////////////////////////
	// REGISTRATION
	//////////////////////////////
	
	public GrowingTree register(){
		GameRegistry.registerBlock(growingBranch, name + "branch");
		GameRegistry.registerItem(seed, name + "seed");
		return this;
	}
	
	public GrowingTree registerRecipes(ItemStack primitiveSapling){
		GameRegistry.addShapelessRecipe(new ItemStack(seed), new Object[]{ primitiveSapling, Items.bowl});
		GameRegistry.addShapelessRecipe(primitiveSapling, new Object[]{ seed, Blocks.dirt });
		return this;
	}

	//////////////////////////////
	// TREE PROPERTIES
	//////////////////////////////

	public String getName(){
		return name;
	}
	
	public GrowingTree setGrowingLeaves(BlockGrowingLeaves gLeaves, int sub){
		growingLeaves = gLeaves;
		leavesSubBlock = sub;
		growingLeaves.setTree(leavesSubBlock, this);
		return this;
	}

	public BlockGrowingLeaves getGrowingLeaves(){
		return growingLeaves;
	}
	
	public int getGrowingLeavesSub(){
		return leavesSubBlock;
	}
	
	public GrowingTree setGrowingBranch(BlockBranch gBranch){
		growingBranch = gBranch;
		growingBranch.setTree(this);
		return this;
	}
	
	public BlockBranch getGrowingBranch(){
		return growingBranch;
	}
	
	public GrowingTree setSeed(Seed newSeed){
		seed = newSeed;
		seed.setTree(this);
		return this;
	}
	
	public Seed getSeed(){
		return seed;
	}
	
	public GrowingTree setStick(ItemStack itemStack){
		stick = itemStack;
		return this;
	}
	
	public ItemStack getStick(){
		return stick;
	}
	
	public GrowingTree setPrimitiveLeaves(Block primLeaves, int meta){
		primitiveLeaves = new BlockAndMeta(primLeaves, meta);
		return this;
	}
	
	public BlockAndMeta getPrimitiveLeaves(){
		return primitiveLeaves;
	}
	
	public GrowingTree setPrimitiveLog(Block primLog, int meta){
		primitiveLog = new BlockAndMeta(primLog, meta);
		return this;
	}
	
	public BlockAndMeta getPrimitiveLog(){
		return primitiveLog;
	}
	
	public float getEnergy(World world, int x, int y, int z){
		return signalEnergy;
	}

	public float getGrowthRate(World world, int x, int y, int z){
		return growthRate;
	}
	
	/** Probability reinforcer for up direction which is arguably the direction most trees generally grow in.*/
	public int getUpProbability(){
		return upProbability;
	}

	/** Probability reinforcer for current travel direction */
	public int getReinfTravel(){
		return 1;
	}
	
	public int getLowestBranchHeight(World world, int x, int y, int z){
		return lowestBranchHeight;
	}
	
	public int getSoilLongevity(World world, int x, int y, int z){
		return (int)(biomeSuitability(world, x, y, z) * soilLongevity);
	}

	public float getTapering(){
		return tapering;
	}

	/** Used by seed to determine the proper dirt block to create for planting. */
	public BlockRootyDirt getRootyDirtBlock(){
		return GrowingTrees.blockRootyDirt;
	}
	
	//////////////////////////////
	// LEAVES HANDLING
	//////////////////////////////
	
	public boolean isCompatibleGrowingLeaves(IBlockAccess blockAccess, int x, int y, int z){
		return isCompatibleGrowingLeaves(blockAccess, blockAccess.getBlock(x, y, z), x, y, z);
	}

	public boolean isCompatibleGrowingLeaves(IBlockAccess blockAccess, Block block, int x, int y, int z){
		return isCompatibleGrowingLeaves(block, BlockGrowingLeaves.getSubBlockNum(blockAccess, x, y, z));
	}
	
	public boolean isCompatibleGrowingLeaves(Block leaves, int sub){
		return leaves == getGrowingLeaves() && sub == getGrowingLeavesSub();
	}
	
	public boolean isCompatibleVanillaLeaves(IBlockAccess blockAccess, int x, int y, int z){
		return getPrimitiveLeaves().matches(blockAccess, x, y, z, 3);
	}
		
	public boolean isCompatibleGenericLeaves(IBlockAccess blockAccess, int x, int y, int z){
		return isCompatibleGrowingLeaves(blockAccess, x, y, z) || isCompatibleVanillaLeaves(blockAccess, x, y, z);
	}
	
	//////////////////////////////
	// BIOME HANDLING
	//////////////////////////////
	
	//Biome suitability 0.0f for completely unsuited.. 1.0f for perfectly suited
	public float biomeSuitability(World world, int x, int y, int z){
        /*
        BIOME CLIMATE DATA FOR REFERENCE: 
        
        BIOME				NAME				TEMP	PRECIP
        ocean				Ocean				0.5		0.5
        plains				Plains				0.8		0.4
        desert				Desert				2.0		0.0
        extremeHills		Extreme Hills		0.2		0.3
        forest				Forest				0.7		0.8
        taiga				Taiga				0.25	0.8
        swampland			Swampland			0.8		0.9
        river				River				0.5		0.5
        hell				Hell				2.0		0.0
        Sky					Sky					0.5		0.5
        frozenOcean			FrozenOcean			0.0		0.5
        frozenRiver			FrozenRiver			0.0		0.5
        icePlains			Ice Plains			0.0		0.5
        iceMountains		Ice Mountains		0.0		0.5
        mushroomIsland		MushroomIsland		0.9		1.0
        mushroomIslandShore	MushroomIslandShore	0.9		1.0
        beach				Beach				0.8		0.4
        desertHills			DesertHills			2.0		0.0
        forestHills			ForestHills			0.7		0.8
        taigaHills			TaigaHills			0.25	0.8
        extremeHillsEdge	Extreme Hills Edge	0.2		0.3
        jungle				Jungle				0.95	0.9
        jungleHills			JungleHills			0.95	0.9
        jungleEdge			JungleEdge			0.95	0.8
        deepOcean			Deep Ocean			0.5		0.5
        stoneBeach			Stone Beach			0.2		0.3
        coldBeach			Cold Beach			0.05	0.3
        birchForest			Birch Forest		0.6		0.6
        birchForestHills	Birch Forest Hills	0.6		0.6
        roofedForest		Roofed Forest		0.7		0.8
        coldTaiga			Cold Taiga			-0.5	0.4
        coldTaigaHills		Cold Taiga Hills	-0.5	0.4
        megaTaiga			Mega Taiga			0.3		0.8
        megaTaigaHills		Mega Taiga Hills	0.3		0.8
        extremeHillsPlus	Extreme Hills+		0.2		0.3
        savanna				Savanna				1.2		0.0
        savannaPlateau		Savanna Plateau		1.0		0.0
        mesa				Mesa				2.0		0.0
        mesaPlateau_F		Mesa Plateau F		2.0		0.0
        mesaPlateau			Mesa Plateau		2.0		0.0
        */
		
        return 1.0f;
	}
	
	public static boolean isOneOfBiomes(BiomeGenBase biomeToCheck, BiomeGenBase ... biomes){
		for(BiomeGenBase biome: biomes){
			if(biomeToCheck.biomeID == biome.biomeID){
				return true;
			}
		}
		return false;
	}

	public static int isOneOfBiomes(BiomeGenBase biomeToCheck, int valueIfTrue, BiomeGenBase ... biomes){
		return isOneOfBiomes(biomeToCheck, biomes) ? valueIfTrue : 0;
	}
	
	/////////////////////////////
	
	//Handle rotting branches
	public boolean rot(World world, int x, int y, int z, int neighborCount, int radius, Random random){
		if(radius <= 1){
			for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS){
				if(getGrowingLeaves().growLeaves(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, getGrowingLeavesSub(), 0)){
					return false;
				}
			}
		} 
		world.setBlockToAir(x, y, z);
		return true;
	}
	
	///////////////////////////////////////////
	// GROWTH
	///////////////////////////////////////////
	
	public int getBranchHydrationLevel(IBlockAccess blockAccess, int x, int y, int z, ForgeDirection dir, BlockBranch branch, BlockGrowingLeaves fromBlock, int fromSub) {
		return branch.getRadius(blockAccess, x, y, z) == 1 && this == fromBlock.getTree(fromSub) ? 5 : 0;
	}

	public int[] customDirectionManipulation(World world, int x, int y, int z, int radius, GrowSignal signal, int probMap[]){
		return probMap;
	}
	
	//Selects a new direction to turn to.
	//This class uses a probability map to make the decision.  Can be overridden for different species.
	public ForgeDirection selectNewDirection(World world, int x, int y, int z, BlockBranch branch, GrowSignal signal) {
		ForgeDirection originDir = signal.dir.getOpposite();

		//prevent branches on the ground
		if(signal.numSteps + 1 <= getLowestBranchHeight(world, signal.originX, signal.originY, signal.originZ)){
			return ForgeDirection.UP;
		}
		
		int probMap[] = new int[6];//6 directions possible DUNSWE

		//Probability taking direction into account
		probMap[ForgeDirection.UP.ordinal()] = signal.dir != ForgeDirection.DOWN ? getUpProbability(): 0;//Favor up 
		probMap[signal.dir.ordinal()] += getReinfTravel(); //Favor current direction
		
		//Create probability map for direction change
		for(int i = 0; i < 6; i++){
			ForgeDirection dir = ForgeDirection.getOrientation(i);
			if(!dir.equals(originDir)){
				int dx = x + dir.offsetX;
				int dy = y + dir.offsetY;
				int dz = z + dir.offsetZ;
				
				//Check probability for surrounding blocks
				//Typically Air:1, Leaves:2, Branches: 2+r
				probMap[i] += TreeHelper.getSafeTreePart(world, dx, dy, dz).probabilityForBlock(world, dx, dy, dz, branch);
			}
		}
		
		//Do custom stuff for various species
		probMap = customDirectionManipulation(world, x, y, z, branch.getRadius(world, x, y, z), signal, probMap);
		
		//Select a direction from the probability map
		int choice = selectRandomFromDistribution(signal.rand, probMap);//Select a direction from the probability map
		return ForgeDirection.getOrientation(choice != -1 ? choice : 1);//Default to up if things are screwy
	}
	
	//Select a random direction weighted from the probability map 
	public static int selectRandomFromDistribution(Random random, int distMap[]){

		int distSize = 0;
		
		for(int i = 0; i < distMap.length; i++){
			distSize += distMap[i];
		}

		if(distSize <= 0){
			return -1;
		}
		
		int rnd = random.nextInt(distSize) + 1;
		
		for(int i = 0; i < 6; i++){
			if(rnd > distMap[i]){
				rnd -= distMap[i];
			} else {
				return i;
			}
		}

		return 0;
	}	

	//////////////////////////////
	// BOTTOM SPECIAL
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
	public void bottomSpecial(World world, int x, int y, int z, Random random){
		for(IBottomListener special: bottomSpecials){
			float chance = special.chance();
			if(chance != 0.0f && random.nextFloat() <= chance){
				special.run(world, this, x, y, z, random);//Make it so!
			}
		}
	}

	/**
	 * Provides an interface for other mods to add special effects like fruit, spawns or whatever 
	 * @param sub
	 * @param specials
	 * @return GrowingTree for chaining
	 */
	public GrowingTree registerBottomSpecials(IBottomListener ... specials){
		for(IBottomListener special: specials){
			bottomSpecials.add(special);
		}
		return this;
	}
	
}
