package com.ferreusveritas.dynamictrees.models.experimental;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Charsets;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumUsage;
import net.minecraft.client.resources.IResource;
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
					//if(wrappedModel == null) {
						wrappedModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state);
						
						Block n = Block.REGISTRY.getObject(new ResourceLocation("thaumcraft", "log_silverwood"));
						//Block n = Block.REGISTRY.getObject(new ResourceLocation("exampletrees", "ironlog"));
						IBlockState state = n.getDefaultState();
						
						Map<IBlockState, ModelResourceLocation> map = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getBlockStateMapper().getVariants(state.getBlock());
						ModelResourceLocation loc = map.get(state);
						
						try {
							IModel model = ModelLoaderRegistry.getModel(loc);
							
							List<TextureAtlasSprite> sprites = new ArrayList<>();
							
							for(ResourceLocation tex : model.getTextures()) {
								System.out.println("XXX TEX: " + tex);
								sprites.add(bakedTextureGetter.apply(tex));
							}

							for( TextureAtlasSprite sprite : sprites) {
								System.out.println("XXX SPRITE: " + sprite);
								System.out.println("XXX SPRITE U: " + sprite.getMinU());
								System.out.println("XXX SPRITE V: " + sprite.getMinV());
							}

							
							Set<ResourceLocation> set = new HashSet<>();
							
							for(ResourceLocation res : model.getDependencies()) {
								set.add(res);
							}
							
							for(ResourceLocation res : set) {
								set.add(res);
								System.out.println("XXX RES: " + res);
								
								IBakedModel kk = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state);
								List<BakedQuad> jj = kk.getQuads(state, EnumFacing.UP, 0);

								for(BakedQuad inQuad: jj) {
									BakedQuad quadCopy = new BakedQuad(inQuad.getVertexData().clone(), inQuad.getTintIndex(), inQuad.getFace(), inQuad.getSprite(), inQuad.shouldApplyDiffuseLighting(), inQuad.getFormat());
									int[] vertexData = quadCopy.getVertexData();
									for(int i = 0; i < vertexData.length; i += inQuad.getFormat().getIntegerSize()) {
										int pos = 0;
										for(VertexFormatElement vfe: inQuad.getFormat().getElements()) {
											if(vfe.getUsage() == EnumUsage.UV) {
												float u = Float.intBitsToFloat(vertexData[i + pos + 0]);
												float v = Float.intBitsToFloat(vertexData[i + pos + 1]);
												System.out.println(u + ", " + v);
											}
											
											pos += vfe.getSize() / 4;//Size is always in bytes but we are dealing with an array of int32s
										}
									}
									

								}

							}
							
							
							
						}
						catch (Exception e) {
							e.printStackTrace();
						}
						
						
					//}
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
	
	protected ModelBlock getBaseModelBlock(ResourceLocation virtualLocation) {
		if (!accepts(virtualLocation)) {
			return null;
		}
		
		String path = virtualLocation.getResourcePath(); // Extract the path portion of the ResourceLocation
		ResourceLocation location = new ResourceLocation(virtualLocation.getResourceDomain(), path); // Recreate the resource location without the code
		
		ModelBlock modelBlock = null;
		Reader reader = null;
		IResource iresource = null;
		
		try {
			iresource = resourceManager.getResource(getModelLocation(location));
			reader = new InputStreamReader(iresource.getInputStream(), Charsets.UTF_8);
			modelBlock = ModelBlock.deserialize(reader);
			modelBlock.name = location.toString();
			
			ModelBlock rootParent = modelBlock;
			
			//Climb the hierarchy to discover the name of the root parent model
			while (rootParent.parent != null) {
				rootParent = rootParent.parent;
			}
			
			// If the name of the parent node is our model then we're good to go.
			if (rootParent.getParentLocation() != null && rootParent.getParentLocation().equals("resourceName")) {
				return modelBlock;
			}
			
			return null;
			
		} catch (IOException e) {
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(iresource);
		}
		
		return null;
	}
	
	protected ResourceLocation getModelLocation(ResourceLocation location) {
		return new ResourceLocation(location.getResourceDomain(), "models/" + location.getResourcePath() + ".json");
	}
	
}
