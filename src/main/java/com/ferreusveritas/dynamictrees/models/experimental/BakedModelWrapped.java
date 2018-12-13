package com.ferreusveritas.dynamictrees.models.experimental;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

public abstract class BakedModelWrapped implements IBakedModel {
	
	private IBakedModel wrappedModel;
	
	//All requests are forwarded to the wrapped model
	public IBakedModel getWrappedModel() {
		if(wrappedModel == null) {
			wrappedModel = createModel();
		}
		return wrappedModel;
	}
	
	public abstract IBakedModel createModel();
	
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
	
}
