package com.ferreusveritas.dynamictrees.worldgen;

import java.util.HashMap;
import java.util.Random;

import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeSpeciesSelector;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CompatHelper;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeHills;
import net.minecraft.world.biome.BiomePlains;
import net.minecraftforge.common.BiomeDictionary.Type;

public class DefaultBiomeTreeSelector implements IBiomeSpeciesSelector {

	private Species oak;
	private Species birch;
	private Species spruce;
	private Species acacia;
	private Species jungle;
	private Species darkoak;
	private Species oakswamp;
	private Species apple;
	private Species cactus;
	
	private StaticDecision staticOakDecision;
	private StaticDecision staticSpruceDecision;
	private StaticDecision staticBirchDecision;
	private StaticDecision staticDarkOakDecision;
	
	HashMap<Integer, DecisionProvider> fastTreeLookup = new HashMap<Integer, DecisionProvider>();
	
	public DefaultBiomeTreeSelector() {		
	}
	
	@Override
	public void init() {
		oak = TreeRegistry.findSpeciesSloppy("oak");
		birch = TreeRegistry.findSpeciesSloppy("birch");
		spruce = TreeRegistry.findSpeciesSloppy("spruce");
		acacia = TreeRegistry.findSpeciesSloppy("acacia");
		jungle = TreeRegistry.findSpeciesSloppy("jungle");
		darkoak = TreeRegistry.findSpeciesSloppy("darkoak");
		oakswamp = TreeRegistry.findSpeciesSloppy("oakswamp");
		apple = TreeRegistry.findSpeciesSloppy("apple");
		
		cactus = TreeRegistry.findSpeciesSloppy("cactus");
		
		staticOakDecision = new StaticDecision(new Decision(oak));
		staticSpruceDecision = new StaticDecision(new Decision(spruce));
		staticBirchDecision = new StaticDecision(new Decision(birch));
		staticDarkOakDecision = new StaticDecision(new Decision(darkoak));
	}
	
	@Override
	public ResourceLocation getName() {
		return new ResourceLocation(ModConstants.MODID, "default");
	}
	
	@Override
	public int getPriority() {
		return 0;
	}
	
	@Override
	public Decision getSpecies(World world, Biome biome, BlockPos pos, IBlockState dirt, Random random) {

		int biomeId = Biome.getIdForBiome(biome);
		DecisionProvider select = fastTreeLookup.get(biomeId);//Speedily look up the selector for the biome id
				
		if(select == null) {
			if(biome instanceof BiomeHills) {//All biomes of type BiomeHills generate spruce 2/3 of the time and oak 1/3 of the time.
				select = new RandomDecision(world.rand).addSpecies(spruce, 2).addSpecies(oak, 1);
			}
			else if(biome instanceof BiomePlains) {
				if(ModConfigs.enableAppleTrees) {
					select = new RandomDecision(world.rand).addSpecies(oak, 24).addSpecies(apple, 1);
				} else {
					select = staticOakDecision;
				}
			}
			else if(CompatHelper.biomeHasType(biome, Type.FOREST)) {
				if(biome == Biomes.MUTATED_REDWOOD_TAIGA || biome == Biomes.MUTATED_REDWOOD_TAIGA_HILLS) {//BiomeDictionary does not accurately give these the CONIFEROUS type.
					select = staticSpruceDecision;
				} else if (CompatHelper.biomeHasType(biome, Type.CONIFEROUS)) {
					select = staticSpruceDecision;
				} else if (CompatHelper.biomeHasType(biome, Type.SPOOKY)) {
					select = staticDarkOakDecision;
				} else if (Species.isOneOfBiomes(biome, Biomes.BIRCH_FOREST, Biomes.BIRCH_FOREST_HILLS)) {
					select = staticBirchDecision;
				} else {//At this point we are mostly sure that we are dealing with a plain "BiomeForest" which generates a Birch Tree 1/5 of the time.
					select = new RandomDecision(world.rand).addSpecies(oak, 4).addSpecies(birch, 1);
				}
			} else if(biome == Biomes.MUTATED_ROOFED_FOREST) {//For some reason this isn't registered as either FOREST or SPOOKY
				select = staticDarkOakDecision;
			}
			else if(biome == Biomes.MESA_ROCK) {
				select = staticOakDecision;
			}
			else if(CompatHelper.biomeHasType(biome, Type.JUNGLE)) {
				select = new StaticDecision(new Decision(jungle));
			}
			else if(CompatHelper.biomeHasType(biome, Type.SAVANNA)) {
				select = new StaticDecision(new Decision(acacia));
			}
			else if(CompatHelper.biomeHasType(biome, Type.SWAMP)) {
				select = new StaticDecision(new Decision(oakswamp));
			}
			else if(CompatHelper.biomeHasType(biome, Type.SANDY)) {
				select = new StaticDecision(new Decision(cactus));
			}
			else if(CompatHelper.biomeHasType(biome, Type.WASTELAND)) {
				select = new StaticDecision(new Decision());//Not handled, no tree
			}
			else {
				select = staticOakDecision;//Just default to oak for everything else
			}

			fastTreeLookup.put(biomeId, select);//Cache decision for future use
		}

		return select.getDecision();
	}

}
