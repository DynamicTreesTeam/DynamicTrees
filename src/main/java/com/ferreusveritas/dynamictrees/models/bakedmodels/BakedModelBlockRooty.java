package com.ferreusveritas.dynamictrees.models.bakedmodels;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class BakedModelBlockRooty implements IDynamicBakedModel {

	protected IBakedModel rootsModel;

	public BakedModelBlockRooty(IBakedModel rootsModel) {
		this.rootsModel = rootsModel;
	}

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
        return null;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return null;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return null;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        List<BakedQuad> quads = new ArrayList<>(16);

//        if (state != null && state.getBlock() instanceof IMimic) {
//            BlockState extendedState = (state);
//            BlockState mimicState = extendedState.get(MimicProperty.MIMIC);
//
//            Minecraft mc = Minecraft.getInstance();
//            BlockRendererDispatcher blockRendererDispatcher = mc.getBlockRendererDispatcher();
//            BlockModelShapes blockModelShapes = blockRendererDispatcher.getBlockModelShapes();
//            IBakedModel mimicModel = blockModelShapes.getModelForState(mimicState);
//
//            quads.addAll(mimicModel.getQuads(mimicState, side, rand));
//
//            if(MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.CUTOUT_MIPPED) {
//                quads.addAll(rootsModel.getQuads(state, side, rand));
//            }
//        }

        return quads;
    }

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
}
