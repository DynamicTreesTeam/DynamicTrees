package com.ferreusveritas.dynamictrees.models.experimental;

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
public class ModelResourceLocationWithState2 extends ModelResourceLocation {
	
	private IBlockState state;
	
	public ModelResourceLocationWithState2(ResourceLocation location, IBlockState state) {
		super(location, null);
		this.state = state;
	}
	
	public IBlockState getBlockState() {
		return state;
	}
	
	@Override
	public boolean equals(Object other) {
		return super.equals(other) && (other instanceof ModelResourceLocationWithState2 ? ((ModelResourceLocationWithState2)other).getBlockState().equals(getBlockState()) : false );
	}
	
	@Override
	public int hashCode() {
		return super.hashCode() ^ getBlockState().hashCode();
	}
}
