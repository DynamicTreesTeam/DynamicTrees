package com.ferreusveritas.dynamictrees.trees;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.network.GrowSignal;
import com.ferreusveritas.dynamictrees.api.treedata.IBiomeSuitabilityDecider;
import com.ferreusveritas.dynamictrees.api.treedata.ISpecies;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSapling;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.util.CompatHelper;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.MathHelper;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import com.ferreusveritas.dynamictrees.worldgen.TreeCodeStore;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class Species implements ISpecies {

	/** Simple name of the tree e.g. "oak" */
	private final String name;
	/** ModID of mod registering this tree */
	private final String modId;

	protected  final DynamicTree treeFamily;
	
	/** How quickly the branch thickens on it's own without branch merges [default = 0.3] */
	protected float tapering = 0.3f;
	/** The probability that the direction decider will choose up out of the other possible direction weights [default = 2] */
	protected int upProbability = 2;
	/** Number of blocks high we have to be before a branch is allowed to form [default = 3](Just high enough to walk under)*/
	protected int lowestBranchHeight = 3;
	/** Number of times a grow signal retries before failing. Affects growing speed [default = 0] */
	protected int retries = 0;
	/** Ideal signal energy. Greatest possible height that branches can reach from the root node [default = 16] */
	protected float signalEnergy = 16.0f;
	/** Ideal growth rate [default = 1.0]*/
	protected float growthRate = 1.0f;
	/** Ideal soil longevity [default = 8]*/
	protected int soilLongevity = 8;//TODO: Make a 0.0 to 1.0 float and recode
	
	//Seeds
	/** The seed used to reproduce this tree.  Drops from the tree and can plant itself */
	protected Seed seed;
	/** The seed stack for the seed.  Hold damage value for seed items with multiple variants */
	protected ItemStack seedStack;
	/** A blockState that will turn itself into this tree */
	protected IBlockState saplingBlock;
	
	//WorldGen
	/** A map of environmental biome factors that change a tree's suitability */
	protected Map <Type, Float> envFactors = new HashMap<Type, Float>();//Environmental factors
	/** A list of JoCodes for world generation. Initialized in addJoCodes()*/
	protected TreeCodeStore joCodeStore;
	
	/** Hands Off! Only {@link DynamicTrees} mod should use this */
	public Species(String name, DynamicTree treeFamily) {
		this(ModConstants.MODID, name, treeFamily);
	}
	
	/**
	 * Constructor suitable for derivative mods
	 * 
	 * @param modid The MODID of the mod that is registering this species
	 * @param name The simple name of the species e.g. "oak"
	 * @param treeFamily The {@link DynamicTree} that this species belongs to.
	 */
	public Species(String modId, String name, DynamicTree treeFamily) {
		this.modId = modId;
		this.name = name;
		this.treeFamily = treeFamily;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String getModId() {
		return modId;
	}
	
	@Override
	public DynamicTree getTree() {
		return treeFamily;
	}
	
	protected void setBasicGrowingParameters(float tapering, float energy, int upProbability, int lowestBranchHeight, float growthRate) {
		this.tapering = tapering;
		this.signalEnergy = energy;
		this.upProbability = upProbability;
		this.lowestBranchHeight = lowestBranchHeight;
		this.growthRate = growthRate;
	}
	
	@Override
	public float getEnergy(World world, BlockPos rootPos) {
		return signalEnergy;
	}
	
	@Override
	public float getGrowthRate(World world, BlockPos rootPos) {
		return growthRate;
	}
	
	/** Probability reinforcer for up direction which is arguably the direction most trees generally grow in.*/
	@Override
	public int getUpProbability() {
		return upProbability;
	}
	
	/** Thickness of the branch connected to a twig(radius == 1).. This should probably always be 2 [default = 2] */
	@Override
	public float getSecondaryThickness() {
		return 2.0f;
	}
	
	/** Probability reinforcer for current travel direction */
	@Override
	public int getReinfTravel() {
		return 1;
	}
	
	@Override
	public int getLowestBranchHeight() {
		return lowestBranchHeight;
	}
	
	/**
	* @param world
	* @param pos 
	* @return The lowest number of blocks from the RootyDirtBlock that a branch can form.
	*/
	@Override
	public int getLowestBranchHeight(World world, BlockPos pos) {
		return getLowestBranchHeight();
	}
	
	public void setRetries(int retries) {
		this.retries = retries;
	}
	
	@Override
	public int getRetries() {
		return retries;
	}
	
	@Override
	public float getTapering() {
		return tapering;
	}
	
	
	///////////////////////////////////////////
	//SEEDS
	///////////////////////////////////////////
	
	@Override
	public ItemStack getSeedStack(int qty) {
		return CompatHelper.setStackCount(seedStack.copy(), qty);
	}
	
	@Override
	public Seed getSeed() {
		return seed;
	}
	
	/**
	 * Generate a seed. Developer is still required to register the item
	 * in the appropriate registries.
	 */
	public Seed generateSeed() {
		seed = new Seed(getName() + "seed");
		return setSeedStack(new ItemStack(seed));
	}
	
	public Seed setSeedStack(ItemStack newSeedStack) {
		if(newSeedStack.getItem() instanceof Seed) {
			seedStack = newSeedStack;
			seed = (Seed) seedStack.getItem();
			seed.setSpecies(this, seedStack);
			return seed;
		} else {
			System.err.println("setSeedStack must have an ItemStack with an Item that is an instance of a Seed");
		}
		return null;
	}
	
	///////////////////////////////////////////
	//SAPLING
	///////////////////////////////////////////
	
	/** 
	 * Sets the Dynamic Sapling for this tree type.  Also sets
	 * the tree type in the dynamic sapling.
	 * 
	 * @param sapling
	 * @return
	 */
	public ISpecies setDynamicSapling(IBlockState sapling) {
		saplingBlock = sapling;//Link the tree to the sapling
		
		//Link the sapling to the Species
		if(saplingBlock.getBlock() instanceof BlockDynamicSapling) {
			BlockDynamicSapling dynSap = (BlockDynamicSapling) saplingBlock.getBlock();
			dynSap.setSpecies(saplingBlock, this);
		}
		
		return this;
	}
	
	public IBlockState getDynamicSapling() {
		return saplingBlock;
	}
	
	@Override
	public boolean placeSaplingBlock(World world, BlockPos pos) {
		world.setBlockState(pos, getDynamicSapling());
		return true;
	}

	///////////////////////////////////////////
	//DIRT
	///////////////////////////////////////////
	
	@Override
	public BlockRootyDirt getRootyDirtBlock() {
		return ModBlocks.blockRootyDirt;
	}
	
	public void setSoilLongevity(int longevity) {
		soilLongevity = longevity;
	}
	
	@Override
	public int getSoilLongevity(World world, BlockPos rootPos) {
		return (int)(biomeSuitability(world, rootPos) * soilLongevity);
	}
	
	@Override
	public boolean isAcceptableSoil(IBlockAccess blockAccess, BlockPos pos, IBlockState soilBlockState) {
		Block soilBlock = soilBlockState.getBlock();
		return soilBlock == Blocks.DIRT || soilBlock == Blocks.GRASS || soilBlock == Blocks.MYCELIUM || soilBlock == ModBlocks.blockRootyDirt;
	}
	
	@Override
	public boolean isAcceptableSoilForWorldgen(IBlockAccess blockAccess, BlockPos pos, IBlockState soilBlockState) {
		return isAcceptableSoil(blockAccess, pos, soilBlockState);
	}
	
	//////////////////////////////
	// GROWTH
	//////////////////////////////
	
	@Override
	public EnumFacing selectNewDirection(World world, BlockPos pos, BlockBranch branch, GrowSignal signal) {
		EnumFacing originDir = signal.dir.getOpposite();
		
		//prevent branches on the ground
		if(signal.numSteps + 1 <= getLowestBranchHeight(world, signal.rootPos)) {
			return EnumFacing.UP;
		}
		
		int probMap[] = new int[6];//6 directions possible DUNSWE
		
		//Probability taking direction into account
		probMap[EnumFacing.UP.ordinal()] = signal.dir != EnumFacing.DOWN ? getUpProbability(): 0;//Favor up
		probMap[signal.dir.ordinal()] += getReinfTravel(); //Favor current direction
		
		//Create probability map for direction change
		for(EnumFacing dir: EnumFacing.VALUES) {
			if(!dir.equals(originDir)) {
				BlockPos deltaPos = pos.offset(dir);
				//Check probability for surrounding blocks
				//Typically Air:1, Leaves:2, Branches: 2+r
				probMap[dir.getIndex()] += TreeHelper.getSafeTreePart(world, deltaPos).probabilityForBlock(world, deltaPos, branch);
			}
		}
		
		//Do custom stuff or override probability map for various species
		probMap = customDirectionManipulation(world, pos, branch.getRadius(world, pos), signal, probMap);
		
		//Select a direction from the probability map
		int choice = MathHelper.selectRandomFromDistribution(signal.rand, probMap);//Select a direction from the probability map
		return newDirectionSelected(EnumFacing.getFront(choice != -1 ? choice : 1), signal);//Default to up if things are screwy
	}
	
	/** Species can override the probability map here **/
	protected int[] customDirectionManipulation(World world, BlockPos pos, int radius, GrowSignal signal, int probMap[]) {
		return probMap;
	}
	
	/** Species can override to take action once a new direction is selected **/
	protected EnumFacing newDirectionSelected(EnumFacing newDir, GrowSignal signal) {
		return newDir;
	}
	
	@Override
	public void postGrow(World world, BlockPos rootPos, BlockPos treePos, int soilLife) {
		
	}
	
	//////////////////////////////
	// BIOME HANDLING
	//////////////////////////////
	
	public Species envFactor(Type type, float factor) {
		envFactors.put(type, factor);
		return this;
	}

	/**
	*
	* @param world The World
	* @param pos
	* @return range from 0.0 - 1.0.  (0.0f for completely unsuited.. 1.0f for perfectly suited)
	*/
	@Override
	public float biomeSuitability(World world, BlockPos pos) {
		
		Biome biome = world.getBiome(pos);
		
		//An override to allow other mods to change the behavior of the suitability for a world location. Such as Terrafirmacraft.
		if(TreeRegistry.isBiomeSuitabilityOverrideEnabled()) {
			IBiomeSuitabilityDecider.Decision override = TreeRegistry.getBiomeSuitability(world, biome, this, pos);
			
			if(override.isHandled()) {
				return override.getSuitability();
			}
		}
		
		if(ModConfigs.ignoreBiomeGrowthRate || isBiomePerfect(biome)) {
			return 1.0f;
		}
		
		float s = defaultSuitability();
		
		for(Type t : BiomeDictionary.getTypes(biome)) {
			s *= envFactors.containsKey(t) ? envFactors.get(t) : 1.0f;
		}
		
		return MathHelper.clamp(s, 0.0f, 1.0f);
	}
	
	public boolean isBiomePerfect(Biome biome) {
		return false;
	}
	
	/** A value that determines what a tree's suitability is before climate manipulation occurs. */
	public static final float defaultSuitability() {
		return 0.85f;
	}
	
	/**
	* A convenience function to test if a biome is one of the many options passed.
	* 
	* @param biomeToCheck The biome we are matching
	* @param biomes Multiple biomes to match against
	* @return True if a match is found. False if not.
	*/
	public static boolean isOneOfBiomes(Biome biomeToCheck, Biome ... biomes) {
		for(Biome biome: biomes) {
			if(biomeToCheck == biome) {
				return true;
			}
		}
		return false;
	}

	//////////////////////////////
	// INTERACTIVE
	//////////////////////////////
	
	@Override
	public boolean onTreeActivated(World world, BlockPos rootPos, BlockPos hitPos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		return false;
	}
	
	//////////////////////////////
	// WORLDGEN
	//////////////////////////////

	@Override
	public boolean generate(World world, BlockPos pos, Biome biome, Random random, int radius) {
		EnumFacing facing = CoordUtils.getRandomDir(random);
		if(getJoCodeStore() != null) {
			JoCode code = getJoCodeStore().getRandomCode(radius, random);
			if(code != null) {
				code.generate(world, this, pos, biome, facing, radius);
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public TreeCodeStore getJoCodeStore() {
		return joCodeStore;
	}
	
	/**
	 * A {@link JoCode} defines the block model of the {@link DynamicTree}
	 */
	public void addJoCodes() {
		joCodeStore = new TreeCodeStore(this);
		joCodeStore.addCodesFromFile("assets/" + getModId() + "/trees/"+ getName() + ".txt");
	}
	
	@Override
	public void postGeneration(World world, BlockPos pos, Biome biome, int radius, List<BlockPos> endPoints, boolean worldGen) {}
	
	@Override
	public float getWorldGenTaperingFactor() {
		return 1.5f;
	}
	
}
