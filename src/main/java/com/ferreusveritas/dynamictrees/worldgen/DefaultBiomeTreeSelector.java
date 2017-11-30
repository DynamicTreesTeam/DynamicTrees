package com.ferreusveritas.dynamictrees.worldgen;

import java.util.HashMap;
import java.util.Random;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeTreeSelector;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class DefaultBiomeTreeSelector implements IBiomeTreeSelector {

	private DynamicTree oak;
	private DynamicTree birch;
	private DynamicTree spruce;
	private DynamicTree acacia;
	private DynamicTree jungle;
	private DynamicTree darkoak;
	
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
		oak = TreeRegistry.findTree("oak");
		birch = TreeRegistry.findTree("birch");
		spruce = TreeRegistry.findTree("spruce");
		acacia = TreeRegistry.findTree("acacia");
		jungle = TreeRegistry.findTree("jungle");
		darkoak = TreeRegistry.findTree("darkoak");
	}
	
	@Override
	public String getName() {
		return DynamicTrees.MODID + ":default";
	}
	
	@Override
	public int getPriority() {
		return 0;
	}
	
	@Override
	public Decision getTree(World world, BiomeGenBase biome, BlockPos pos, IBlockState dirt, Random random) {
		
		int biomeId = biome.biomeID;
		ITreeSelector select;
				
		if(fastTreeLookup.containsKey(biomeId)) {
			select = fastTreeLookup.get(biomeId);//Speedily look up the type of tree
		}
		else {
			if(BiomeDictionary.isBiomeOfType(biome, Type.FOREST)) {
				if (BiomeDictionary.isBiomeOfType(biome, Type.CONIFEROUS)) {
					select = new StaticDecision(new Decision(spruce));
				} else if (BiomeDictionary.isBiomeOfType(biome, Type.SPOOKY)) {
					select = new StaticDecision(new Decision(darkoak));
				} else if (DynamicTree.isOneOfBiomes(biome, BiomeGenBase.birchForest, BiomeGenBase.birchForestHills)) {
					select = new StaticDecision(new Decision(birch));
				} else {
					select = new StaticDecision(new Decision(oak));
				}
			} /*else if(biome == BiomeGenBase.MUTATED_ROOFED_FOREST) {//For some reason this isn't registered as either FOREST or SPOOKY
				select = new StaticDecision(new Decision(darkoak));
			}*/
			else if(BiomeDictionary.isBiomeOfType(biome, Type.JUNGLE)) {
				select = new StaticDecision(new Decision(jungle));
			}
			else if(BiomeDictionary.isBiomeOfType(biome, Type.SAVANNA)) {
				select = new StaticDecision(new Decision(acacia));
			}
			else if(BiomeDictionary.isBiomeOfType(biome, Type.SANDY)) {
				select = new StaticDecision(new Decision());//Not handled, no tree
			}
			else if(BiomeDictionary.isBiomeOfType(biome, Type.WASTELAND)) {
				select = new StaticDecision(new Decision());//Not handled, no tree
			} else {
				select = new StaticDecision(new Decision(oak));//Just default to oak for everything else
			}

			fastTreeLookup.put(biomeId, select);//Cache decision for future use
		}

		return select.getDecision();
	}

}
