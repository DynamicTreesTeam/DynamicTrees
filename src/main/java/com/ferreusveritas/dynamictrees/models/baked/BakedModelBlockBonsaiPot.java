package com.ferreusveritas.dynamictrees.models.baked;

import com.ferreusveritas.dynamictrees.block.entity.PottedSaplingBlockEntity;
import com.ferreusveritas.dynamictrees.client.QuadManipulator;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class BakedModelBlockBonsaiPot implements IDynamicBakedModel {

    protected BakedModel basePotModel;
    protected Map<Species, List<BakedQuad>> cachedSaplingQuads = new HashMap<>();

    public BakedModelBlockBonsaiPot(BakedModel basePotModel) {
        this.basePotModel = basePotModel;
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
        List<BakedQuad> quads = new ArrayList<>();

        if (side != null || state == null || !extraData.has(PottedSaplingBlockEntity.SPECIES) || !extraData.has(PottedSaplingBlockEntity.POT_MIMIC)) {
            return quads;
        }

        final Species species = extraData.get(PottedSaplingBlockEntity.SPECIES);
        final BlockState potState = extraData.get(PottedSaplingBlockEntity.POT_MIMIC);

        if (species == null || potState == null || !species.isValid() || !species.getSapling().isPresent()) {
            return quads;
        }

        final BlockState saplingState = species.getSapling().get().defaultBlockState();

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel potModel = dispatcher.getBlockModel(potState);
        BakedModel saplingModel = dispatcher.getBlockModel(saplingState);

        quads.addAll(potModel.getQuads(potState, side, rand, extraData, renderType));
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