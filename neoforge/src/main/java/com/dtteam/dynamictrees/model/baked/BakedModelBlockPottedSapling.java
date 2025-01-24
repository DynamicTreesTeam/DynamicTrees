package com.dtteam.dynamictrees.model.baked;

import com.dtteam.dynamictrees.registry.PottedSaplingBlockEntityNF;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.utility.QuadManipulator;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class BakedModelBlockPottedSapling implements IDynamicBakedModel {

    protected BakedModel basePotModel;
    protected Map<Species, List<BakedQuad>> cachedSaplingQuads = new ConcurrentHashMap<>();

    public BakedModelBlockPottedSapling(BakedModel basePotModel) {
        this.basePotModel = basePotModel;
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
        List<BakedQuad> quads = new ArrayList<>();

        if (state == null || !extraData.has(PottedSaplingBlockEntityNF.SPECIES) || !extraData.has(PottedSaplingBlockEntityNF.POT_MIMIC)) {
            return quads;
        }

        final BlockState potState = extraData.get(PottedSaplingBlockEntityNF.POT_MIMIC);

        if (potState == null) {
            return quads;
        }

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel potModel = dispatcher.getBlockModel(potState);
        quads.addAll(potModel.getQuads(potState, side, rand, extraData, renderType));

        if (side == null){
            final Species species = extraData.get(PottedSaplingBlockEntityNF.SPECIES);
            if (species == null || !species.isValid() || species.getSapling().isEmpty()) {
                return quads;
            }
            final BlockState saplingState = species.getSapling().get().defaultBlockState();
            BakedModel saplingModel = dispatcher.getBlockModel(saplingState);
            quads.addAll(cachedSaplingQuads.computeIfAbsent(species, s -> QuadManipulator.getQuads(saplingModel, saplingState, new Vec3(0, 0.25, 0), new Direction[]{null}, rand, extraData)));
        }

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
    public TextureAtlasSprite getParticleIcon(ModelData data) {
        return this.basePotModel.getParticleIcon(data);
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        return ChunkRenderTypeSet.of(RenderType.CUTOUT_MIPPED);
    }

}