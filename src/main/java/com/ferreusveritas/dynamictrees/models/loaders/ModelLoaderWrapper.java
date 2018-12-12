package com.ferreusveritas.dynamictrees.models.loaders;

import java.util.List;
import java.util.function.Function;

import com.ferreusveritas.dynamictrees.models.ModelResourceLocationWithState;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;

public class ModelLoaderWrapper implements ICustomModelLoader {
	
	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) { }
	
	@Override
	public boolean accepts(ResourceLocation modelLocation) {
		return modelLocation instanceof ModelResourceLocationWithState;
	}
	
	@Override
	public IModel loadModel(ResourceLocation modelLocation) throws Exception {
		
		if(modelLocation instanceof ModelResourceLocationWithState) {
			ModelResourceLocationWithState modelWithBlockState = (ModelResourceLocationWithState) modelLocation;
			IBlockState state = modelWithBlockState.getBlockState();
			
			return new IModel() {
				
				private IBakedModel wrappedModel;
				
				public IBakedModel getWrappedModel() {
					if(wrappedModel == null) {
						wrappedModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state);
					}
					return wrappedModel;
				}
				
				@Override
				public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
					
					//All requests are forwarded to the wrapped model
					return new IBakedModel() {
						
						@Override
						public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
							return getWrappedModel().getQuads(state, side, rand);
						}
						
						@Override
						public boolean isGui3d() {
							return getWrappedModel().isGui3d();
						}
						
						@Override
						public boolean isBuiltInRenderer() {
							return getWrappedModel().isBuiltInRenderer();
						}
						
						@Override
						public boolean isAmbientOcclusion() {
							return getWrappedModel().isAmbientOcclusion();
						}
						
						@Override
						public TextureAtlasSprite getParticleTexture() {
							return getWrappedModel().getParticleTexture();
						}
						
						@Override
						public ItemOverrideList getOverrides() {
							return getWrappedModel().getOverrides();
						}
					};
					
				}
				
			};
		}
		
		return null;
	}
	
}
