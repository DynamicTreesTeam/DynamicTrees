package com.dtteam.dynamictrees.model.baked;

import com.dtteam.dynamictrees.block.leaves.PalmLeavesProperties;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Predicate;

public final class PalmLeavesBakedModel extends DynamicTreesBlockStateModel {

    private final List<List<BakedQuad>> fronds;

    public PalmLeavesBakedModel(Material.Baked particle, List<List<BakedQuad>> frondQuads) {
        super(particle);
        this.fronds = frondQuads;
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return state.getValue(PalmLeavesProperties.DynamicPalmLeavesBlock.DIRECTION);
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state,
                          RandomSource random, Predicate<Direction> cullTest) {
        int direction = state.getValue(PalmLeavesProperties.DynamicPalmLeavesBlock.DIRECTION);
        if (direction <= 0 || direction > fronds.size()) {
            return;
        }
        for (BakedQuad quad : fronds.get(direction - 1)) {
            emitter.fromBakedQuad(quad);
            emitter.emit();
        }
    }
}
