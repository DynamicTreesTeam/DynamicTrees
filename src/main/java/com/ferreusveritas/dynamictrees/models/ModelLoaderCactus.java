package com.ferreusveritas.dynamictrees.models;

import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;

public class ModelLoaderCactus extends ModelLoaderBranch {
	
	public final String code = "#dynamiccactus";
	
	// return true if our Model Loader accepts this ModelResourceLocation
	@Override
	public boolean accepts(ResourceLocation resourceLocation) {
		return resourceLocation.getResourcePath().endsWith(code);//Hacky but fast
	}
	
	// When called for our BlockBranch's ModelResourceLocation, return our BranchModel.
	@Override
	public IModel loadModel(ResourceLocation resourceLocation) {
		ModelBlock modelBlock = getBranchBlockModel(resourceLocation);//We need this to model textures.
		return (modelBlock != null) ? new BranchModel(modelBlock) : ModelLoaderRegistry.getMissingModel();
	}

}
