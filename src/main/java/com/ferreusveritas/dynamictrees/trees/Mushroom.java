package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.IGenFeature;
import com.ferreusveritas.dynamictrees.systems.featuregen.HugeMushroomGenFeature;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;

public class Mushroom extends Species {
	
	protected final boolean redcap;
	
	/** @param redcap True to select redcap mushroom.  Otherwise brown cap is selected */
	public Mushroom(boolean redcap) {
		this.redcap = redcap;
		setRegistryName(new ResourceLocation(DynamicTrees.MODID, (redcap ? "red" : "brown") + "_mushroom"));
		setStandardSoils();
		addGenFeature(new HugeMushroomGenFeature(redcap ? Blocks.RED_MUSHROOM_BLOCK : Blocks.BROWN_MUSHROOM_BLOCK), IGenFeature.FULLGEN);
	}
	
}
