package com.ferreusveritas.dynamictrees.models;

import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;

public class ModelLoaderBlockSurfaceRoot extends ModelLoaderGeneric {
	
	public ModelLoaderBlockSurfaceRoot() {
		super("dynamicroot", new ResourceLocation("dynamictrees", "block/smartmodel/branch"));
	}
	
	@Override
	protected IModel loadModel(ResourceLocation resourceLocation, ModelBlock baseModelBlock) {
		return new ModelBlockSurfaceRoot(baseModelBlock);
	}
	
}