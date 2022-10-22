package com.ferreusveritas.dynamictrees.model.baked;

import com.ferreusveritas.dynamictrees.client.QuadManipulator;
import com.ferreusveritas.dynamictrees.block.entity.PottedSaplingBlockEntity;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@OnlyIn(Dist.CLIENT)
public class BakedModelBlockBonsaiPot implements IDynamicBakedModel {

    protected BakedModel basePotModel;
    protected Map<Species, List<BakedQuad>> cachedSaplingQuads = new HashMap<>();

    public BakedModelBlockBonsaiPot(BakedModel basePotModel) {
        this.basePotModel = basePotModel;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        List<BakedQuad> quads = new ArrayList<>();

        if (side != null || state == null || !extraData.hasProperty(PottedSaplingBlockEntity.SPECIES) || !extraData.hasProperty(PottedSaplingBlockEntity.POT_MIMIC)) {
            return quads;
        }

        final Species species = extraData.getData(PottedSaplingBlockEntity.SPECIES);
        final BlockState potState = extraData.getData(PottedSaplingBlockEntity.POT_MIMIC);

        if (species == null || potState == null || !species.isValid() || !species.getSapling().isPresent()) {
            return quads;
        }

        final BlockState saplingState = species.getSapling().get().defaultBlockState();

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel potModel = dispatcher.getBlockModel(potState);
        BakedModel saplingModel = dispatcher.getBlockModel(saplingState);

        quads.addAll(potModel.getQuads(potState, side, rand, extraData));
        quads.addAll(cachedSaplingQuads.computeIfAbsent(species, s -> QuadManipulator.getQuads(saplingModel, saplingState, new Vec3(0, 0.25, 0), rand, extraData)));

        return quads;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.basePotModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.basePotModel.getParticleIcon();
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

}
