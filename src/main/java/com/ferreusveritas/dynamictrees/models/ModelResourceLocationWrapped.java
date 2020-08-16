package com.ferreusveritas.dynamictrees.models;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;

/**
 * This class allows the smuggling of an BlockState in with
 * the {@link ModelResourceLocation}.  This also makes the {@link ICustomModelLoader}
 * able to easily identify it as a load candidate.
 *
 * @author ferreusveritas
 *
 */
public class ModelResourceLocationWrapped extends ModelResourceLocation {

	private BlockState state;

	public ModelResourceLocationWrapped(ResourceLocation location, BlockState state) {
		super(location, null);
		this.state = state;
	}

	public BlockState getBlockState() {
		return state;
	}

	@Override
	public boolean equals(Object other) {
		return super.equals(other) && (other instanceof ModelResourceLocationWrapped && ((ModelResourceLocationWrapped) other).getBlockState().equals(getBlockState()));
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ getBlockState().hashCode();
	}
}
