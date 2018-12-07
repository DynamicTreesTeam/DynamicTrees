package com.ferreusveritas.dynamictrees.models;

import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;

public class ModelLoaderTest implements ICustomModelLoader {

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
	}

	@Override
	public boolean accepts(ResourceLocation modelLocation) {
		
		if(modelLocation.getResourcePath().equals("xxxxxx")) {
			System.out.println("Found: " + modelLocation);
			return true;
		}
		
		return false;
	}

	@Override
	public IModel loadModel(ResourceLocation modelLocation) throws Exception {
		System.out.println("Testing Loading model: " + modelLocation);
		
		return new IModel() {
			
			@Override
			public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {

				System.out.println("Testing Baking model: " + modelLocation + " --> " + state);
				BlockStateMapper mapper = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getBlockStateMapper();
				System.out.println("Finding mapper did not result in crash");

				return null;
			}
		};
		
	}
	
}
