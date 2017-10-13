package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import net.minecraft.world.biome.Biome;

/**
 * Provides the tree used for a given biome
 * 
 * @author ferreusveritas
 *
 */
public interface IBiomeTreeSelector {

	public DynamicTree getTree(Biome biome);
	
}
