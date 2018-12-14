package com.ferreusveritas.dynamictrees.models.experimental;

import java.util.function.Function;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;

public class ModelBlockWrapped implements IModel {
	
	private final IBlockState blockState;
	
	public ModelBlockWrapped(ModelResourceLocationWrapped resourceLocation) {
		this.blockState = resourceLocation.getBlockState();
	}
	
	public IBakedModel createBakedModel(Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(blockState);
	}
	
	@Override
	public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		return new BakedModelWrapped() {
			@Override
			public IBakedModel createModel() {
				return createBakedModel(bakedTextureGetter);
			}
		};
	}
	
}
