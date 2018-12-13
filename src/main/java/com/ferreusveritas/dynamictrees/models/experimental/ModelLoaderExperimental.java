package com.ferreusveritas.dynamictrees.models.experimental;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.ferreusveritas.dynamictrees.models.bakedmodels.BakedModelBlockBranchBasic;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumUsage;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;

public class ModelLoaderExperimental implements ICustomModelLoader {
	
	protected IResourceManager resourceManager;
	
	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
		this.resourceManager = resourceManager;
	}
	
	@Override
	public boolean accepts(ResourceLocation modelLocation) {
		return modelLocation instanceof ModelResourceLocationWithState2;
	}
	
	@Override
	public IModel loadModel(ResourceLocation modelLocation) throws Exception {
		
		if(modelLocation instanceof ModelResourceLocationWithState2) {
			ModelResourceLocationWithState2 modelWithBlockState = (ModelResourceLocationWithState2) modelLocation;
			IBlockState state = modelWithBlockState.getBlockState();
			
			//return new ModelBlockExperimental(state);
			
			return new IModel() {
				
				private IBakedModel wrappedModel;
				
				public IBakedModel getWrappedModel(Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
					if(wrappedModel == null) {
						Block block = Block.REGISTRY.getObject(new ResourceLocation("thaumcraft", "log_silverwood"));
						//Block block = Block.REGISTRY.getObject(new ResourceLocation("exampletrees", "ironlog"));
						IBlockState state = block.getDefaultState();
						
						IModel model = getModelForState(state);
						
						ResourceLocation barkRes = getModelTexture(model, bakedTextureGetter, state, EnumFacing.UP);
						ResourceLocation ringsRes = getModelTexture(model, bakedTextureGetter, state, EnumFacing.SOUTH);
						
						wrappedModel = new BakedModelBlockBranchBasic(barkRes, ringsRes, bakedTextureGetter);
						//wrappedModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state);
					}
					return wrappedModel;
				}
				
				@Override
				public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
					
					//All requests are forwarded to the wrapped model
					return new IBakedModel() {
						
						@Override
						public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
							return getWrappedModel(bakedTextureGetter).getQuads(state, side, rand);
						}
						
						@Override
						public boolean isGui3d() {
							return getWrappedModel(bakedTextureGetter).isGui3d();
						}
						
						@Override
						public boolean isBuiltInRenderer() {
							return getWrappedModel(bakedTextureGetter).isBuiltInRenderer();
						}
						
						@Override
						public boolean isAmbientOcclusion() {
							return getWrappedModel(bakedTextureGetter).isAmbientOcclusion();
						}
						
						@Override
						public TextureAtlasSprite getParticleTexture() {
							return getWrappedModel(bakedTextureGetter).getParticleTexture();
						}
						
						@Override
						public ItemOverrideList getOverrides() {
							return getWrappedModel(bakedTextureGetter).getOverrides();
						}
					};
					
				}
				
			};
			
			
		}
		
		return null;
	}
	
	public static IModel getModelForState(IBlockState state) {		
		IModel model = null;
		
		try {
			model = ModelLoaderRegistry.getModel(getModelLocation(state));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return model;
	}
	
	public static ModelResourceLocation getModelLocation(IBlockState state) {
		return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getBlockStateMapper().getVariants(state.getBlock()).get(state);
	}
	
	public static ResourceLocation getModelTexture(IModel model, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter, IBlockState state, EnumFacing dir) {
		
		float uvs[] = getSpriteUVFromBlockState(state, dir);
		
		List<TextureAtlasSprite> sprites = new ArrayList<>();
		
		float closest = Float.POSITIVE_INFINITY;
		ResourceLocation closestTex = new ResourceLocation("missingno");
		if(model != null) {
			for(ResourceLocation tex : model.getTextures()) {
				TextureAtlasSprite tas = bakedTextureGetter.apply(tex);
				float u = tas.getInterpolatedU(8);
				float v = tas.getInterpolatedU(8);
				sprites.add(tas);
				float du = u - uvs[0];
				float dv = v - uvs[1];
				float distSq = du * du + dv * dv;
				if(distSq < closest) {
					closest = distSq;
					closestTex = tex;
				}
			}
		}
		
		return closestTex;
	}
	
	public static float[] getSpriteUVFromBlockState(IBlockState state, EnumFacing side) {
		IBakedModel bakedModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state);
		List<BakedQuad> quads = bakedModel.getQuads(state, EnumFacing.UP, 0);
		BakedQuad quad = quads.get(0);
		
		float u = 0.0f;
		float v = 0.0f;
		
		int[] vertexData = quad.getVertexData();
		int numVertices = 0;
		for(int i = 0; i < vertexData.length; i += quad.getFormat().getIntegerSize()) {
			int pos = 0;
			for(VertexFormatElement vfe: quad.getFormat().getElements()) {
				if(vfe.getUsage() == EnumUsage.UV) {
					u += Float.intBitsToFloat(vertexData[i + pos + 0]);
					v += Float.intBitsToFloat(vertexData[i + pos + 1]);
				}
				pos += vfe.getSize() / 4;//Size is always in bytes but we are dealing with an array of int32s
			}
			numVertices++;
		}
		
		return new float[] { u / numVertices, v / numVertices };
	}
	
}
