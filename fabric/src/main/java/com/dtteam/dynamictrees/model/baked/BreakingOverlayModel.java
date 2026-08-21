package com.dtteam.dynamictrees.model.baked;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

/**
 * Delegates world meshing to an FRAPI model while exposing a static part list for vanilla
 * destroy-stage overlay rendering, which only calls {@link #collectParts}.
 */
public final class BreakingOverlayModel extends DynamicTreesBlockStateModel {
    private final DynamicTreesBlockStateModel inner;
    @Nullable
    private final BlockStateModelPart breakingPart;

    public BreakingOverlayModel(DynamicTreesBlockStateModel inner, List<BakedQuad> breakingQuads) {
        super(inner.particleMaterial());
        this.inner = inner;
        this.breakingPart = breakingQuads.isEmpty() ? null : partFromQuads(breakingQuads, inner.particleMaterial());
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
        if (breakingPart != null) {
            output.add(breakingPart);
        }
    }

    @Override
    public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return inner.createGeometryKey(level, pos, state, random);
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state,
                          RandomSource random, Predicate<Direction> cullTest) {
        inner.emitQuads(emitter, level, pos, state, random, cullTest);
    }

    private static BlockStateModelPart partFromQuads(List<BakedQuad> quads, net.minecraft.client.resources.model.sprite.Material.Baked particle) {
        QuadCollection.Builder builder = new QuadCollection.Builder();
        for (BakedQuad quad : quads) {
            builder.addCulledFace(quad.direction(), quad);
        }
        return new SimpleModelWrapper(builder.build(), true, particle);
    }
}
