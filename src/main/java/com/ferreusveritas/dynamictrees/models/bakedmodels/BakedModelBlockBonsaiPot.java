package com.ferreusveritas.dynamictrees.models.bakedmodels;

import com.ferreusveritas.dynamictrees.client.QuadManipulator;
import com.ferreusveritas.dynamictrees.tileentity.BonsaiTileEntity;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@OnlyIn(Dist.CLIENT)
public class BakedModelBlockBonsaiPot implements IDynamicBakedModel {

    protected IBakedModel basePotModel;
    protected Map<Species, List<BakedQuad>> cachedSaplingQuads = new HashMap<>();

    public BakedModelBlockBonsaiPot(IBakedModel basePotModel) {
        this.basePotModel = basePotModel;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        List<BakedQuad> quads = new ArrayList<>();

        if (side != null || state == null || !extraData.hasProperty(BonsaiTileEntity.SPECIES) || !extraData.hasProperty(BonsaiTileEntity.POT_MIMIC)) {
            return quads;
        }

        final Species species = extraData.getData(BonsaiTileEntity.SPECIES);
        final BlockState potState = extraData.getData(BonsaiTileEntity.POT_MIMIC);

        if (species == null || potState == null || !species.isValid() || !species.getSapling().isPresent()) {
            return quads;
        }

        final BlockState saplingState = species.getSapling().get().getDefaultState();

        BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
        IBakedModel potModel = dispatcher.getModelForState(potState);
        IBakedModel saplingModel = dispatcher.getModelForState(saplingState);

        quads.addAll(potModel.getQuads(potState, side, rand, extraData));
        quads.addAll(cachedSaplingQuads.computeIfAbsent(species, s -> QuadManipulator.getQuads(saplingModel, saplingState, new Vector3d(0, 0.25, 0), rand, extraData)));

        return quads;
    }

    @Override
	public boolean isAmbientOcclusion() {
		return this.basePotModel.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return true;
	}

	@Override
	public boolean isBuiltInRenderer() {
		return true;
	}

    @Override
    public boolean isSideLit() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return this.basePotModel.getParticleTexture();
    }

    @Override
	public ItemOverrideList getOverrides() {
		return ItemOverrideList.EMPTY;
	}

}
