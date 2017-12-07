package com.ferreusveritas.dynamictrees.trees;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.network.GrowSignal;
import com.ferreusveritas.dynamictrees.api.treedata.IBiomeSuitabilityDecider;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.inspectors.NodeFruit;
import com.ferreusveritas.dynamictrees.inspectors.NodeFruitCocoa;
import com.ferreusveritas.dynamictrees.util.MathHelper;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import com.ferreusveritas.dynamictrees.worldgen.TreeCodeStore;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class Species implements ISpecies {

	/** Simple name of the tree e.g. "oak" */
	private String name;
	/** ModID of mod registering this tree */
	private String modId;

	public final DynamicTree treeFamily;
	
	/** How quickly the branch thickens on it's own without branch merges [default = 0.3] */
	float tapering = 0.3f;
	/** The probability that the direction decider will choose up out of the other possible direction weights [default = 2] */
	int upProbability = 2;
	/** Number of blocks high we have to be before a branch is allowed to form [default = 3](Just high enough to walk under)*/
	int lowestBranchHeight = 3;
	/** Number of times a grow signal retries before failing. Affects growing speed [default = 0] */
	int retries = 0;
	/** Ideal signal energy. Greatest possible height that branches can reach from the root node [default = 16] */
	float signalEnergy = 16.0f;
	
	/** Ideal growth rate [default = 1.0]*/
	float growthRate = 1.0f;
	/** Ideal soil longevity [default = 8]*/
	int soilLongevity = 8;//TODO: Make a 0.0 to 1.0 float and recode
	
	/** A map of environmental biome factors that change a tree's suitability */
	public Map <Type, Float> envFactors = new HashMap<Type, Float>();//Environmental factors
	
	/** A list of JoCodes for world generation. Initialized in addJoCodes()*/
	protected TreeCodeStore joCodeStore;
	
	public Species(String name, DynamicTree treeFamily) {
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
	
	/** Gets the fruiting node analyzer for this tree.  See {@link NodeFruitCocoa} for an example.
	*  
	* @param world The World
	* @param x X-Axis of block
	* @param y Y-Axis of block
	* @param z Z-Axis of block
	*/
	@Override
	public NodeFruit getNodeFruit(World world, BlockPos pos) {
		return null;//Return null to disable fruiting. Most species do.
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
	// WORLDGEN
	//////////////////////////////

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
	public void postGeneration(World world, BlockPos pos, Biome biome, int radius, List<BlockPos> endPoints) {}
	
	/**
	 * Worldgen can produce thin sickly trees from the underinflation caused by not living it's full life.
	 * This factor is an attempt to compensate for the problem.
	 * 
	 * @return
	 */
	@Override
	public float getWorldGenTaperingFactor() {
		return 1.5f;
	}
}
