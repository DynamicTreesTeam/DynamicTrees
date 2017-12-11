package com.ferreusveritas.dynamictrees.worldgen;

import java.util.HashMap;
import java.util.Random;

import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeSpeciesSelector;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class DefaultBiomeTreeSelector implements IBiomeSpeciesSelector {

	private Species oak;
	private Species birch;
	private Species spruce;
	private Species acacia;
	private Species jungle;
	private Species darkoak;
	private Species oakswamp;
	
	private interface ITreeSelector {
		Decision getDecision();
	}
	
	private class StaticDecision implements ITreeSelector {
		Decision decision;
		
		public StaticDecision(Decision decision) {
			this.decision = decision;
		}

		@Override
		public Decision getDecision() {
			return decision;
		}
	}
	
	HashMap<Integer, ITreeSelector> fastTreeLookup = new HashMap<Integer, ITreeSelector>();
	
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
	}
	
	@Override
	public String getName() {
		return ModConstants.MODID + ":default";
	}
	
	@Override
	public int getPriority() {
		return 0;
	}
	
	@Override
	public Decision getSpecies(World world, Biome biome, BlockPos pos, IBlockState dirt, Random random) {
		
		int biomeId = Biome.getIdForBiome(biome);
		ITreeSelector select;
				
		if(fastTreeLookup.containsKey(biomeId)) {
			select = fastTreeLookup.get(biomeId);//Speedily look up the type of tree
		}
		else {
			if(BiomeDictionary.hasType(biome, Type.FOREST)) {
				if (BiomeDictionary.hasType(biome, Type.CONIFEROUS)) {
					select = new StaticDecision(new Decision(spruce));
				} else if (BiomeDictionary.hasType(biome, Type.SPOOKY)) {
					select = new StaticDecision(new Decision(darkoak));
				} else if (Species.isOneOfBiomes(biome, Biomes.BIRCH_FOREST, Biomes.BIRCH_FOREST_HILLS)) {
					select = new StaticDecision(new Decision(birch));
				} else {
					select = new StaticDecision(new Decision(oak));
				}
			} else if(biome == Biomes.MUTATED_ROOFED_FOREST) {//For some reason this isn't registered as either FOREST or SPOOKY
				select = new StaticDecision(new Decision(darkoak));
			}
			else if(BiomeDictionary.hasType(biome, Type.JUNGLE)) {
				select = new StaticDecision(new Decision(jungle));
			}
			else if(BiomeDictionary.hasType(biome, Type.SAVANNA)) {
				select = new StaticDecision(new Decision(acacia));
			}
			else if(BiomeDictionary.hasType(biome, Type.SWAMP)) {
				select = new StaticDecision(new Decision(oakswamp));
			}
			else if(BiomeDictionary.hasType(biome, Type.SANDY)) {
				select = new StaticDecision(new Decision());//Not handled, no tree
			}
			else if(BiomeDictionary.hasType(biome, Type.WASTELAND)) {
				select = new StaticDecision(new Decision());//Not handled, no tree
			} else {
				select = new StaticDecision(new Decision(oak));//Just default to oak for everything else
			}

			fastTreeLookup.put(biomeId, select);//Cache decision for future use
		}

		return select.getDecision();
	}

}
