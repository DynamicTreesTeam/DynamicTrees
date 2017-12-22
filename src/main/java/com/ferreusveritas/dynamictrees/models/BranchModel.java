package com.ferreusveritas.dynamictrees.models;

import java.util.Collection;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;

public class BranchModel implements IModel {

	public ResourceLocation barkTexture;
	public ResourceLocation ringsTexture;

	
	public BranchModel(ModelBlock modelBlock) {
		barkTexture = new ResourceLocation(modelBlock.resolveTextureName("bark"));
		ringsTexture = new ResourceLocation(modelBlock.resolveTextureName("rings"));
	}
	
	// return all other resources used by this model
	@Override
	public Collection<ResourceLocation> getDependencies() {
		return ImmutableList.copyOf(new ResourceLocation[]{});
	}

	// return all the textures used by this model
	@Override
	public Collection<ResourceLocation> getTextures() {
		return ImmutableList.copyOf(new ResourceLocation[]{barkTexture, ringsTexture});
	}

	// Bake the subcomponents into a CompositeModel
	@Override
	public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		try {
			return new CompositeModel(barkTexture, ringsTexture, bakedTextureGetter);
		} catch (Exception exception) {
			System.err.println("BranchModel.bake() failed due to exception:" + exception);
			return ModelLoaderRegistry.getMissingModel().bake(state, format, bakedTextureGetter);
		}
	}
	
}	