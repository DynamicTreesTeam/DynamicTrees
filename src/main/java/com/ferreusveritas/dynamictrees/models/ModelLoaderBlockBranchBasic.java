package com.ferreusveritas.dynamictrees.models;

import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;

public class ModelLoaderBlockBranchBasic extends ModelLoaderGeneric {
	
	public ModelLoaderBlockBranchBasic() {
		super("dynamictree", new ResourceLocation("dynamictrees", "block/smartmodel/branch"));
	}
	
	@Override
	protected IModel loadModel(ResourceLocation resourceLocation, ModelBlock baseModelBlock) {
		return new ModelBlockBranchBasic(baseModelBlock);
	}
	
}