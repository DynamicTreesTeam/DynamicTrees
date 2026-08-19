package com.dtteam.dynamictrees.model;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModelPart;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Fabric mirror of NeoForge's {@code DynamicBlockStateModel}: a {@link BlockStateModel} whose
 * geometry depends on level context. Implementors provide the level-aware
 * {@link #collectParts(BlockAndTintGetter, BlockPos, BlockState, RandomSource, List)}; quads are
 * emitted through the Fabric Renderer API's {@link FabricBlockStateModel#emitQuads} path.
 */
public interface FabricDynamicBlockStateModel extends BlockStateModel, FabricBlockStateModel {

    void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts);

    @Override
    default void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
        // Geometry is dynamic; without level context there is nothing meaningful to collect.
    }

    @Override
    default void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state,
                           RandomSource random, Predicate<@Nullable Direction> cullTest) {
        List<BlockStateModelPart> parts = new ArrayList<>();
        collectParts(level, pos, state, random, parts);
        for (BlockStateModelPart part : parts) {
            ((FabricBlockStateModelPart) part).emitQuads(emitter, cullTest);
        }
    }
}
