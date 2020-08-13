package com.ferreusveritas.dynamictrees.models.bakedmodels;

import com.ferreusveritas.dynamictrees.blocks.MimicProperty;
import com.ferreusveritas.dynamictrees.blocks.MimicProperty.IMimic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraftforge.client.MinecraftForgeClient;

import java.util.ArrayList;
import java.util.List;

//@OnlyIn(Dist.CLIENT)
//public class BakedModelBlockRooty implements IBakedModel {
//
//	protected IBakedModel rootsModel;
//
//	public BakedModelBlockRooty(IBakedModel rootsModel) {
//		this.rootsModel = rootsModel;
//	}
//
//	@Override
//	public List<BakedQuad> getQuads(BlockState state, Direction side, long rand) {
//		List<BakedQuad> quads = new ArrayList<>(16);
//
//		if (state != null && state.getBlock() instanceof IMimic && state instanceof BlockState) {
//			BlockState extendedState = ((BlockState) state);
//			BlockState mimicState = extendedState.getValue(MimicProperty.MIMIC);
//
//			Minecraft mc = Minecraft.getInstance();
//			BlockRendererDispatcher blockRendererDispatcher = mc.getBlockRendererDispatcher();
//			BlockModelShapes blockModelShapes = blockRendererDispatcher.getBlockModelShapes();
//			IBakedModel mimicModel = blockModelShapes.getModelForState(mimicState);
//
//			quads.addAll(mimicModel.getQuads(mimicState, side, rand));
//
//			if(MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.CUTOUT_MIPPED) {
//				quads.addAll(rootsModel.getQuads(state, side, rand));
//			}
//		}
//
//		return quads;
//	}
//
//	@Override
//	public boolean isAmbientOcclusion() {
//		return true;
//	}
//
//	@Override
//	public boolean isGui3d() {
//		return true;
//	}
//
//	@Override
//	public boolean isBuiltInRenderer() {
//		return true;
//	}
//
//	@Override
//	public TextureAtlasSprite getParticleTexture() {
//		return rootsModel.getParticleTexture();
//	}
//
//	@Override
//	public ItemOverrideList getOverrides() {
//		return null;
//	}
//
//}
