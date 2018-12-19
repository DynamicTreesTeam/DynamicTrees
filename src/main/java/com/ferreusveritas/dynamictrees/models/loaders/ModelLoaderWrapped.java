package com.ferreusveritas.dynamictrees.models.loaders;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockBranchThick;
import com.ferreusveritas.dynamictrees.client.QuadManipulator;
import com.ferreusveritas.dynamictrees.event.TextureGenerationHandler;
import com.ferreusveritas.dynamictrees.models.ModelResourceLocationWrapped;
import com.ferreusveritas.dynamictrees.models.bakedmodels.BakedModelBlockBranchBasic;
import com.ferreusveritas.dynamictrees.models.bakedmodels.BakedModelBlockBranchThick;
import com.ferreusveritas.dynamictrees.models.blockmodels.ModelBlockWrapped;

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
		modelCreatorMap.put( "branch", r -> loadModelBranch(r) );
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
	
	public static IModel loadModelBranch(ModelResourceLocationWrapped location) {
		
		IBlockState blockState = location.getBlockState();
		BlockBranch branch = (BlockBranch) blockState.getBlock();
		boolean thick = branch instanceof BlockBranchThick;
		ResourceLocation thickRingRes = thick ? injectThickRingTextures(location) : null;
		
		return new ModelBlockWrapped(location) {
			@Override
			public IBakedModel createBakedModel(Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
				BlockBranch branch = (BlockBranch) blockState.getBlock();
				IBlockState primLog = branch.getFamily().getPrimitiveLog();
				
				IModel model = QuadManipulator.getModelForState(primLog);
				ResourceLocation ringsRes = QuadManipulator.getModelTexture(model, bakedTextureGetter, primLog, EnumFacing.UP);
				ResourceLocation barkRes = QuadManipulator.getModelTexture(model, bakedTextureGetter, primLog, EnumFacing.SOUTH);
				return thick ? 
					new BakedModelBlockBranchThick(barkRes, ringsRes, thickRingRes != null ? thickRingRes : ringsRes, bakedTextureGetter) :
					new BakedModelBlockBranchBasic(barkRes, ringsRes, bakedTextureGetter);
			}
		};
	}
	
	private static ResourceLocation injectThickRingTextures(ModelResourceLocationWrapped location) {
		IBlockState blockState = location.getBlockState();
		BlockBranch branch = (BlockBranch) blockState.getBlock();
		
		ResourceLocation familyName = branch.getFamily().getName();
		
		IBlockState primLog = branch.getFamily().getPrimitiveLog();
		IModel model = QuadManipulator.getModelForState(primLog);
		
		Iterator<ResourceLocation> iter = model.getTextures().iterator();
		ResourceLocation candidate1 = iter.hasNext() ? iter.next() : null;
		ResourceLocation candidate2 = iter.hasNext() ? iter.next() : null;
		
		if(candidate1 != null) {
			if(candidate2 != null) {
				if(candidate1.getResourceDomain().equals(candidate2.getResourceDomain())) { //The candidates must be from the same domain
					ResourceLocation thickRingRes = new ResourceLocation(candidate1.getResourceDomain(), "blocks/log_" +  familyName.getResourcePath() + "_top_thick");
					return TextureGenerationHandler.addDualTextureLocations(candidate1, candidate2, thickRingRes);
				}
			}
			return TextureGenerationHandler.addRingTextureLocation(candidate1);
		}
		
		return null;
	}
	
}
