package com.ferreusveritas.dynamictrees.models.bakedmodels;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;

import java.util.List;

//public abstract class BakedModelWrapped implements IBakedModel {
//
//	private IBakedModel wrappedModel;
//
//	//All requests are forwarded to the wrapped model
//	public IBakedModel getWrappedModel() {
//		if(wrappedModel == null) {
//			wrappedModel = createModel();
//		}
//		return wrappedModel;
//	}
//
//	public abstract IBakedModel createModel();
//
//	@Override
//	public List<BakedQuad> getQuads(BlockState state, Direction side, long rand) {
//		return getWrappedModel().getQuads(state, side, rand);
//	}
//
//	@Override
//	public boolean isGui3d() {
//		return getWrappedModel().isGui3d();
//	}
//
//	@Override
//	public boolean isBuiltInRenderer() {
//		return getWrappedModel().isBuiltInRenderer();
//	}
//
//	@Override
//	public boolean isAmbientOcclusion() {
//		return getWrappedModel().isAmbientOcclusion();
//	}
//
//	@Override
//	public TextureAtlasSprite getParticleTexture() {
//		return getWrappedModel().getParticleTexture();
//	}
//
//	@Override
//	public ItemOverrideList getOverrides() {
//		return getWrappedModel().getOverrides();
//	}
//
//}
