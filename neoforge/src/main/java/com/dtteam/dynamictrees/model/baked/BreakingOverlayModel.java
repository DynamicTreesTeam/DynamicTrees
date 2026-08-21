package com.dtteam.dynamictrees.model.baked;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Delegates world meshing to the inner DT model while exposing a static part list for vanilla
 * destroy-stage overlay rendering, which only calls {@link #collectParts(RandomSource, List)}.
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

    public DynamicTreesBlockStateModel inner() {
        return inner;
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
        if (breakingPart != null) {
            output.add(breakingPart);
        }
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        inner.collectParts(level, pos, state, random, parts);
    }

    @Override
    public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return inner.createGeometryKey(level, pos, state, random);
    }
}
