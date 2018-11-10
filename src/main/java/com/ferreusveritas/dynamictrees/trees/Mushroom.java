package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.IGenFeature;
import com.ferreusveritas.dynamictrees.systems.featuregen.FeatureGenHugeMushroom;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

public class Mushroom extends Species {
	
	protected static final IBlockState dirtState = Blocks.DIRT.getDefaultState();
	protected final boolean redcap;
	
	/** @param redcap True to select redcap mushroom.  Otherwise brown cap is selected */
	public Mushroom(boolean redcap) {
		this.redcap = redcap;
		setRegistryName(new ResourceLocation(ModConstants.MODID, "mushroom" + (redcap ? "red" : "brn")));
		setStandardSoils();
		addGenFeature(new FeatureGenHugeMushroom(redcap ? Blocks.BROWN_MUSHROOM_BLOCK : Blocks.RED_MUSHROOM_BLOCK), IGenFeature.FULLGEN);
	}
	
}
