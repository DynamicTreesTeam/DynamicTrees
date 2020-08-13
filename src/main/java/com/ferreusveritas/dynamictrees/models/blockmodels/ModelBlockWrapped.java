package com.ferreusveritas.dynamictrees.models.blockmodels;

import com.ferreusveritas.dynamictrees.models.ModelResourceLocationWrapped;
import com.ferreusveritas.dynamictrees.models.bakedmodels.BakedModelWrapped;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;

import java.util.function.Function;

public class ModelBlockWrapped implements IModel {
	
	private final BlockState blockState;
	
	public ModelBlockWrapped(ModelResourceLocationWrapped resourceLocation) {
		this.blockState = resourceLocation.getBlockState();
	}
	
	public IBakedModel createBakedModel(Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		return Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(blockState);
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
