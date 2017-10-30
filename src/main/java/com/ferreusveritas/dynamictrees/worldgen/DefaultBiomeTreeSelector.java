package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeTreeSelector;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class DefaultBiomeTreeSelector implements IBiomeTreeSelector {

	DynamicTree oak;
	DynamicTree birch;
	DynamicTree spruce;
	DynamicTree acacia;
	DynamicTree jungle;
	DynamicTree darkoak;
	
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
		return "default";
	}
	
	@Override
	public int getPriority() {
		return 0;
	}
	
	@Override
	public Decision getTree(World world, Biome biome, BlockPos pos, IBlockState dirt) {
		
		if(BiomeDictionary.isBiomeOfType(biome, Type.FOREST)) {
			
			if(BiomeDictionary.isBiomeOfType(biome, Type.CONIFEROUS)) {
				return new Decision(spruce);
			}
			
			if(BiomeDictionary.isBiomeOfType(biome, Type.SPOOKY)) {
				return new Decision(darkoak);
			}
			
			return new Decision(DynamicTree.isOneOfBiomes(biome, Biomes.BIRCH_FOREST, Biomes.BIRCH_FOREST_HILLS) ?  birch : oak);
		}
		
		if(BiomeDictionary.isBiomeOfType(biome, Type.JUNGLE)) {
			return new Decision(jungle);
		}
		
		if(BiomeDictionary.isBiomeOfType(biome, Type.SAVANNA)) {
			return new Decision(acacia);
		}
		
		if(BiomeDictionary.isBiomeOfType(biome, Type.SANDY)) {
			return new Decision();//Not handled, no tree
		}
		
		if(BiomeDictionary.isBiomeOfType(biome, Type.WASTELAND)) {
			return new Decision();//Not handled, no tree
		}
		
		return new Decision(oak);
	}

}
