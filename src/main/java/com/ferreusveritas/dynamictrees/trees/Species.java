package com.ferreusveritas.dynamictrees.trees;

import java.util.HashMap;
import java.util.Map;

import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.treedata.IBiomeSuitabilityDecider;
import com.ferreusveritas.dynamictrees.util.MathHelper;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import com.ferreusveritas.dynamictrees.worldgen.TreeCodeStore;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class Species implements ISpecies {

	/** Simple name of the species e.g. "oak" */
	String name;

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
	/** The stick that is returned when a whole log can't be dropped */
	
	/** Ideal growth rate [default = 1.0]*/
	float growthRate = 1.0f;
	/** Ideal soil longevity [default = 8]*/
	int soilLongevity = 8;//TODO: Make a 0.0 to 1.0 float and recode
	
	/** A map of environmental biome factors that change a tree's suitability */
	public Map <Type, Float> envFactors = new HashMap<Type, Float>();//Environmental factors
	
	/** A list of JoCodes for world generation. Initialized in addJoCodes()*/
	protected TreeCodeStore joCodeStore;

	
	public Species(DynamicTree treeFamily) {
		this.treeFamily = treeFamily;
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
	
	public void setSoilLongevity(int longevity) {
		soilLongevity = longevity;
	}
	
	@Override
	public int getSoilLongevity(World world, BlockPos rootPos) {
		return (int)(biomeSuitability(world, rootPos) * soilLongevity);
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
		joCodeStore.addCodesFromFile("assets/" + getModID() + "/trees/"+ getName() + ".txt");
	}
}
