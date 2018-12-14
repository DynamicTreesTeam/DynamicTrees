package com.ferreusveritas.dynamictrees.models.experimental;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.ferreusveritas.dynamictrees.client.QuadManipulator;
import com.ferreusveritas.dynamictrees.models.bakedmodels.BakedModelBlockBranchBasic;
import com.ferreusveritas.dynamictrees.models.bakedmodels.BakedModelBlockBranchThick;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;

public class ModelLoaderWrapped implements ICustomModelLoader {
	
	protected IResourceManager resourceManager;
	
	public static Map<String, Function<ModelResourceLocationWrapped, IModel>> modelCreatorMap = new HashMap<>();
	
	static {
		modelCreatorMap.put( "branch", r -> loadModelBranch(r, false) );
		modelCreatorMap.put( "thickbranch", r -> loadModelBranch(r, true) );
		modelCreatorMap.put( "leaves", r -> loadModelLeaves(r) );
	}
	
	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
		this.resourceManager = resourceManager;
	}
	
	@Override
	public boolean accepts(ResourceLocation modelLocation) {
		
		if(modelLocation instanceof ModelResourceLocationWrapped) {
			return modelCreatorMap.containsKey(modelLocation.getResourcePath());
		}
		
		return false;
	}
	
	@Override
	public IModel loadModel(ResourceLocation modelLocation) throws Exception {
		return accepts(modelLocation) ? modelCreatorMap.get(modelLocation.getResourcePath()).apply((ModelResourceLocationWrapped) modelLocation) : null;
	}
	
	public static IModel loadModelBranch(ModelResourceLocationWrapped location, boolean thick) {
		IBlockState blockState = location.getBlockState();
		
		return new ModelBlockWrapped(location) {
			@Override
			public IBakedModel createBakedModel(Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
				IModel model = QuadManipulator.getModelForState(blockState);
				ResourceLocation ringsRes = QuadManipulator.getModelTexture(model, bakedTextureGetter, blockState, EnumFacing.UP);
				ResourceLocation barkRes = QuadManipulator.getModelTexture(model, bakedTextureGetter, blockState, EnumFacing.SOUTH);
				return thick ? 
					new BakedModelBlockBranchThick(barkRes, ringsRes, ringsRes, bakedTextureGetter) : //TODO: Work out thick ring texture
					new BakedModelBlockBranchBasic(barkRes, ringsRes, bakedTextureGetter);
			}
		};
	}
	
	public static IModel loadModelLeaves(ModelResourceLocationWrapped location) {
		return new ModelBlockWrapped(location);
	}
	
}
