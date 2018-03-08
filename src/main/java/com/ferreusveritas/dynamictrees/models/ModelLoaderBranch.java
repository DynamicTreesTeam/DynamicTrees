package com.ferreusveritas.dynamictrees.models;

import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;

public class ModelLoaderBranch extends ModelLoaderGeneric {
	
	public ModelLoaderBranch() {
		super("dynamictree", new ResourceLocation("dynamictrees", "block/smartmodel/branch"));
	}
	
	@Override
	protected IModel loadModel(ResourceLocation resourceLocation, ModelBlock baseModelBlock) {
		return new BranchModel(baseModelBlock);
	}
	
}