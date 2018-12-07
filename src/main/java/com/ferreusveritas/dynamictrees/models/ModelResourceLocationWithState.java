package com.ferreusveritas.dynamictrees.models;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;

/**
 * This class allows the smuggling of an IBlockState in with
 * the {@link ModelResourceLocation}.  This also makes the {@link ICustomModelLoader}
 * able to easily identify it as a load candidate. 
 * 
 * @author ferreusveritas
 * 
 */
public class ModelResourceLocationWithState extends ModelResourceLocation {
	
	private IBlockState state;
	
	public ModelResourceLocationWithState(ResourceLocation location, IBlockState state) {
		super(location, null);
		this.state = state;
	}
	
	public IBlockState getBlockState() {
		return state;
	}
	
}
