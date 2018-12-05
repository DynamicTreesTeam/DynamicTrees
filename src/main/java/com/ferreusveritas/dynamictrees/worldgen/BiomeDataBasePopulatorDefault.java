package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.EnumChance;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.IChanceSelector;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.IDensitySelector;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.ISpeciesSelector;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.RandomSpeciesSelector;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.SpeciesSelection;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.StaticSpeciesSelector;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDataBasePopulator;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase.Operation;

import net.minecraft.init.Biomes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeHills;
import net.minecraft.world.biome.BiomePlains;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class BiomeDataBasePopulatorDefault implements IBiomeDataBasePopulator {

	private static Species oak;
	private static Species birch;
	private static Species spruce;
	private static Species acacia;
	private static Species jungle;
	private static Species darkoak;
	private static Species oakswamp;
	private static Species apple;
	private static Species megaspruce;
	private static Species cactus;
	private static Species mushroomred;
	private static Species mushroombrn;
	
	private static StaticSpeciesSelector staticOakDecision;
	private static StaticSpeciesSelector staticSpruceDecision;
	private static StaticSpeciesSelector staticBirchDecision;
	private static RandomSpeciesSelector randomRoofedForestDecision;
	
	private static void createStaticAliases() {
		oak = TreeRegistry.findSpeciesSloppy("oak");
		birch = TreeRegistry.findSpeciesSloppy("birch");
		spruce = TreeRegistry.findSpeciesSloppy("spruce");
		acacia = TreeRegistry.findSpeciesSloppy("acacia");
		jungle = TreeRegistry.findSpeciesSloppy("jungle");
		darkoak = TreeRegistry.findSpeciesSloppy("darkoak");
		oakswamp = TreeRegistry.findSpeciesSloppy("oakswamp");
		apple = TreeRegistry.findSpeciesSloppy("apple");
		megaspruce = TreeRegistry.findSpeciesSloppy("megaspruce");
		
		cactus = TreeRegistry.findSpeciesSloppy("cactus");
		
		mushroomred = TreeRegistry.findSpeciesSloppy("mushroomred");
		mushroombrn = TreeRegistry.findSpeciesSloppy("mushroombrn");
		
		staticOakDecision = new StaticSpeciesSelector(new SpeciesSelection(oak));
		staticSpruceDecision = new StaticSpeciesSelector(new SpeciesSelection(spruce));
		staticBirchDecision = new StaticSpeciesSelector(new SpeciesSelection(birch));
		randomRoofedForestDecision = new RandomSpeciesSelector().add(darkoak, 20);//.add(mushroombrn, 1).add(mushroomred, 1);
	}
	
	@Override
	public void populate (BiomeDataBase dbase) {

		if(oak == null) {
			createStaticAliases();
		}
		
		Biome.REGISTRY.forEach(biome -> {
				dbase.setChanceSelector(biome, computeChance(biome), Operation.REPLACE);
				dbase.setDensitySelector(biome, computeDensity(biome), Operation.REPLACE);
				dbase.setSpeciesSelector(biome, computeSpecies(biome), Operation.REPLACE);
				
				//Cancel all tree decoration for Vanilla Minecraft Biomes
				if(biome.getRegistryName().getResourceDomain().equals("minecraft")) {
					dbase.setCancelVanillaTreeGen(biome, true);
				}
				
				//Identify the "forestness" of the biome.  Affects forest spread rate for biome.
				dbase.setForestness(biome, identifyForestness(biome));
			}
		);
		
		//Enable poisson disc multipass of roofed forests to ensure maximum density even with large trees
		//by filling in gaps in the generation with smaller trees 
		dbase.setMultipass(Biomes.ROOFED_FOREST, pass -> {
			switch(pass) {
				case 0: return 0;//Zero means to run as normal
				case 1: return 5;//Return only radius 5 on pass 1
				case 2: return 3;//Return only radius 3 on pass 2
				default: return -1;//A negative number means to terminate
			}
		});
		
		dbase.setIsSubterranean(Biomes.HELL, true);
	}
	
	public IDensitySelector computeDensity(Biome biome) {
		if(BiomeDictionary.hasType(biome, Type.SPOOKY)) { //Roofed Forest
			return (rnd, nd) -> 0.0f + (nd / 3.0f);
		}
		if(BiomeDictionary.hasType(biome, Type.SANDY)) { //Desert
			return (rnd, nd) -> ( nd * 0.6) + 0.4;
		}
		if(BiomeDictionary.hasType(biome, Type.MUSHROOM)) { //Mushroom Island
			return (rnd, nd) -> (nd * 0.25) + 0.25;
		}
		final double treeDensity = MathHelper.clamp(biome.decorator.treesPerChunk / 10.0f, 0.0f, 1.0f);//Gives 0.0 to 1.0
		return (rnd, nd) -> nd * treeDensity;
	}
	
	public IChanceSelector computeChance(Biome biome) {
		if(BiomeDictionary.hasType(biome, Type.CONIFEROUS)) {
			return (rnd, spc, rad) -> rad > 6 && rnd.nextFloat() < 0.5f ? EnumChance.CANCEL : EnumChance.OK;
		}
		if(BiomeDictionary.hasType(biome, Type.FOREST)) {//Never miss a chance to spawn a tree in a forest.
			return (rnd, spc, rad) -> EnumChance.OK;
		}
		if(biome == Biomes.MUTATED_ROOFED_FOREST) {//Although this is a forest it's not registered as one for some reason
			return (rnd, spc, rad) -> EnumChance.OK;
		}
		if(BiomeDictionary.hasType(biome, Type.SWAMP)) {//Swamps need more tree opportunities since it's so watery
			return (rnd, spc, rad) -> rnd.nextFloat() < 0.75f ? EnumChance.OK : EnumChance.CANCEL;
		} 
		if(BiomeDictionary.hasType(biome, Type.SANDY)) {//Deserts (for cacti)
			return (rnd, spc, rad) -> rnd.nextFloat() < 0.075f ? EnumChance.OK : EnumChance.CANCEL;
		}
		if(BiomeDictionary.hasType(biome, Type.MUSHROOM)) {
			return (rnd, spc, rad) -> rnd.nextFloat() < 0.66 ? EnumChance.OK : EnumChance.CANCEL;
		}
		if(biome.decorator.treesPerChunk < 0) {//Deserts, Mesas, Beaches(-999) Mushroom Island(-100)
			return (rnd, spc, rad) -> EnumChance.CANCEL;
		}
		if (biome == Biomes.RIVER) {
			return (rnd, spc, rad) -> EnumChance.CANCEL;
		}
		else {//Let the radius determine the chance
			return (rnd, spc, rad) -> rnd.nextFloat() < (rad > 3 ? 2.0f / rad : 1.0f) ? EnumChance.OK : EnumChance.CANCEL; 
			//Start dropping tree spawn opportunities when the radius gets bigger than 3
			//Note: the last call should never be UNHANDLED for the DefaultBiomeDensityProvider since it is the last in the chain
		}
	}
	
	public ISpeciesSelector computeSpecies(Biome biome) {
		if(biome instanceof BiomeHills) {//All biomes of type BiomeHills generate spruce 2/3 of the time and oak 1/3 of the time.
			return new RandomSpeciesSelector().add(spruce, 2).add(oak, 1);
		}
		if(biome instanceof BiomePlains) {
			return ModConfigs.enableAppleTrees ? new RandomSpeciesSelector().add(oak, 24).add(apple, 1) : staticOakDecision;
		}
		if(BiomeDictionary.hasType(biome, Type.FOREST)) {
			if(Species.isOneOfBiomes(biome, Biomes.REDWOOD_TAIGA, Biomes.REDWOOD_TAIGA_HILLS)) {
				return new StaticSpeciesSelector(megaspruce);
			}
			if(biome == Biomes.MUTATED_REDWOOD_TAIGA || biome == Biomes.MUTATED_REDWOOD_TAIGA_HILLS) {//BiomeDictionary does not accurately give these the CONIFEROUS type.
				return staticSpruceDecision;
			}
			if (BiomeDictionary.hasType(biome, Type.CONIFEROUS)) {
				return staticSpruceDecision;
			}
			if (BiomeDictionary.hasType(biome, Type.SPOOKY)) {
				return randomRoofedForestDecision;
			}
			if (Species.isOneOfBiomes(biome, Biomes.BIRCH_FOREST, Biomes.BIRCH_FOREST_HILLS)) {
				return staticBirchDecision;
			}
			//At this point we are mostly sure that we are dealing with a plain "BiomeForest" which generates a Birch Tree 1/5 of the time.
			return new RandomSpeciesSelector().add(oak, 4).add(birch, 1);
		}
		if(biome == Biomes.MUTATED_ROOFED_FOREST) {//For some reason this isn't registered as either FOREST or SPOOKY
			return randomRoofedForestDecision;
		}
		if(biome == Biomes.MESA_ROCK) {
			return staticOakDecision;
		}
		if(BiomeDictionary.hasType(biome, Type.MUSHROOM)) {
			return new RandomSpeciesSelector().add(mushroomred, 4).add(mushroombrn, 3);
		}
		if(BiomeDictionary.hasType(biome, Type.JUNGLE)) {
			return new StaticSpeciesSelector(new SpeciesSelection(jungle));
		}
		if(BiomeDictionary.hasType(biome, Type.SAVANNA)) {
			return new StaticSpeciesSelector(new SpeciesSelection(acacia));
		}
		if(BiomeDictionary.hasType(biome, Type.SWAMP)) {
			return new StaticSpeciesSelector(new SpeciesSelection(oakswamp));
		}
		if(BiomeDictionary.hasType(biome, Type.SANDY)) {
			return new StaticSpeciesSelector(new SpeciesSelection(cactus));
		}
		if(BiomeDictionary.hasType(biome, Type.WASTELAND)) {
			return new StaticSpeciesSelector(new SpeciesSelection());//Not handled, no tree
		}
		
		return BiomeDictionary.hasType(biome, Type.COLD) ? staticSpruceDecision : staticOakDecision;//Just default to spruce for cold biomes and oak for everything else
	}
	
}
