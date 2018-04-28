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
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CompatHelper;
import com.ferreusveritas.dynamictrees.util.MathHelper;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase.Operation;

import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeHills;
import net.minecraft.world.biome.BiomePlains;
import net.minecraftforge.common.BiomeDictionary.Type;

public class DefaultBiomeDataBasePopulator {

	private Species oak;
	private Species birch;
	private Species spruce;
	private Species acacia;
	private Species jungle;
	private Species darkoak;
	private Species oakswamp;
	private Species apple;
	private Species cactus;
	
	private StaticSpeciesSelector staticOakDecision;
	private StaticSpeciesSelector staticSpruceDecision;
	private StaticSpeciesSelector staticBirchDecision;
	private StaticSpeciesSelector staticDarkOakDecision;
	
	public void init () {
		oak = TreeRegistry.findSpeciesSloppy("oak");
		birch = TreeRegistry.findSpeciesSloppy("birch");
		spruce = TreeRegistry.findSpeciesSloppy("spruce");
		acacia = TreeRegistry.findSpeciesSloppy("acacia");
		jungle = TreeRegistry.findSpeciesSloppy("jungle");
		darkoak = TreeRegistry.findSpeciesSloppy("darkoak");
		oakswamp = TreeRegistry.findSpeciesSloppy("oakswamp");
		apple = TreeRegistry.findSpeciesSloppy("apple");
		
		cactus = TreeRegistry.findSpeciesSloppy("cactus");
		
		staticOakDecision = new StaticSpeciesSelector(new SpeciesSelection(oak));
		staticSpruceDecision = new StaticSpeciesSelector(new SpeciesSelection(spruce));
		staticBirchDecision = new StaticSpeciesSelector(new SpeciesSelection(birch));
		staticDarkOakDecision = new StaticSpeciesSelector(new SpeciesSelection(darkoak));
		
		BiomeDataBase dbase = TreeGenerator.getTreeGenerator().biomeDataBase;
		
		Biome.REGISTRY.forEach(biome -> {
				dbase.setChanceSelector(biome, computeChance(biome), Operation.REPLACE);
				dbase.setDensitySelector(biome, computeDensity(biome), Operation.REPLACE);
				dbase.setSpeciesSelector(biome, computeSpecies(biome), Operation.REPLACE);
				
				//Cancel all tree decoration for Vanilla Minecraft Biomes
				if(biome.getRegistryName().getResourceDomain().equals("minecraft")) {
					dbase.setCancelVanillaTreeGen(biome, true);
				}
				
			}
		);
	}
	
	public IDensitySelector computeDensity(Biome biome) {
		if(CompatHelper.biomeHasType(biome, Type.SPOOKY)) { //Roofed Forest
			return (rnd, nd) -> { return 0.4f + (nd / 3.0f); };
		}
		if(CompatHelper.biomeHasType(biome, Type.SANDY)) { //Desert
			return (rnd, nd) -> { return ( nd * 0.6) + 0.4; };
		}
		final double treeDensity = MathHelper.clamp((CompatHelper.getBiomeTreesPerChunk(biome)) / 10.0f, 0.0f, 1.0f);//Gives 0.0 to 1.0
		return (rnd, nd) -> { return nd * treeDensity; };
	}
	
	public IChanceSelector computeChance(Biome biome) {
		if(CompatHelper.biomeHasType(biome, Type.CONIFEROUS)) {
			return (rnd, spc, rad) -> { return rad > 6 && rnd.nextFloat() < 0.5f ? EnumChance.CANCEL : EnumChance.OK; };
		}
		if(CompatHelper.biomeHasType(biome, Type.FOREST)) {//Never miss a chance to spawn a tree in a forest.
			return (rnd, spc, rad) -> { return EnumChance.OK; };
		}
		if(biome == Biomes.MUTATED_ROOFED_FOREST) {//Although this is a forest it's not registered as one for some reason
			return (rnd, spc, rad) -> { return EnumChance.OK; };
		}
		if(CompatHelper.biomeHasType(biome, Type.SWAMP)) {//Swamps need more tree opportunities since it's so watery
			return (rnd, spc, rad) -> { return rnd.nextFloat() < 0.75f ? EnumChance.OK : EnumChance.CANCEL; };
		} 
		if(CompatHelper.biomeHasType(biome, Type.SANDY)) {//Deserts (for cacti)
			return (rnd, spc, rad) -> { return rnd.nextFloat() < 0.75f ? EnumChance.OK : EnumChance.CANCEL; };
		}
		else if(CompatHelper.getBiomeTreesPerChunk(biome) < 0) {//Deserts, Mesas, Beaches(-999) Mushroom Island(-100)
			return (rnd, spc, rad) -> { return EnumChance.CANCEL; };
		}
		if (biome == Biomes.RIVER) {
			return (rnd, spc, rad) -> { return EnumChance.CANCEL; };
		}
		else {
			return (rnd, spc, rad) -> {//Let the radius determine the chance
				//Start dropping tree spawn opportunities when the radius gets bigger than 3
				return rnd.nextFloat() < (rad > 3 ? 2.0f / rad : 1.0f) ? EnumChance.OK : EnumChance.CANCEL;
				//Note: the last call should never be UNHANDLED for the DefaultBiomeDensityProvider since it is the last in the chain
			};
		}
	}
	
	public ISpeciesSelector computeSpecies(Biome biome) {
		if(biome instanceof BiomeHills) {//All biomes of type BiomeHills generate spruce 2/3 of the time and oak 1/3 of the time.
			return new RandomSpeciesSelector().add(spruce, 2).add(oak, 1);
		}
		if(biome instanceof BiomePlains) {
			if(ModConfigs.enableAppleTrees) {
				return new RandomSpeciesSelector().add(oak, 24).add(apple, 1);
			} else {
				return staticOakDecision;
			}
		}
		if(CompatHelper.biomeHasType(biome, Type.FOREST)) {
			if(biome == Biomes.MUTATED_REDWOOD_TAIGA || biome == Biomes.MUTATED_REDWOOD_TAIGA_HILLS) {//BiomeDictionary does not accurately give these the CONIFEROUS type.
				return staticSpruceDecision;
			}
			if (CompatHelper.biomeHasType(biome, Type.CONIFEROUS)) {
				return staticSpruceDecision;
			}
			if (CompatHelper.biomeHasType(biome, Type.SPOOKY)) {
				return staticDarkOakDecision;
			}
			if (Species.isOneOfBiomes(biome, Biomes.BIRCH_FOREST, Biomes.BIRCH_FOREST_HILLS)) {
				return staticBirchDecision;
			}
			//At this point we are mostly sure that we are dealing with a plain "BiomeForest" which generates a Birch Tree 1/5 of the time.
			return new RandomSpeciesSelector().add(oak, 4).add(birch, 1);
		}
		if(biome == Biomes.MUTATED_ROOFED_FOREST) {//For some reason this isn't registered as either FOREST or SPOOKY
			return staticDarkOakDecision;
		}
		if(biome == Biomes.MESA_ROCK) {
			return staticOakDecision;
		}
		if(CompatHelper.biomeHasType(biome, Type.JUNGLE)) {
			return new StaticSpeciesSelector(new SpeciesSelection(jungle));
		}
		if(CompatHelper.biomeHasType(biome, Type.SAVANNA)) {
			return new StaticSpeciesSelector(new SpeciesSelection(acacia));
		}
		if(CompatHelper.biomeHasType(biome, Type.SWAMP)) {
			return new StaticSpeciesSelector(new SpeciesSelection(oakswamp));
		}
		if(CompatHelper.biomeHasType(biome, Type.SANDY)) {
			return new StaticSpeciesSelector(new SpeciesSelection(cactus));
		}
		if(CompatHelper.biomeHasType(biome, Type.WASTELAND)) {
			return new StaticSpeciesSelector(new SpeciesSelection());//Not handled, no tree
		}
		
		return staticOakDecision;//Just default to oak for everything else
	}
	
}
