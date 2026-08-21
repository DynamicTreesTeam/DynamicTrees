package com.dtteam.dynamictrees.model.baked;

import com.dtteam.dynamictrees.block.leaves.PalmLeavesProperties;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public final class PalmLeavesBakedModel extends DynamicTreesBlockStateModel {

    private final BlockStateModelPart[] fronds;

    public PalmLeavesBakedModel(Material.Baked particle, List<List<BakedQuad>> frondQuads) {
        super(particle);
        this.fronds = new BlockStateModelPart[frondQuads.size()];
        for (int i = 0; i < frondQuads.size(); i++) {
            this.fronds[i] = partFromUnculled(frondQuads.get(i), particle);
        }
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        int direction = state.getValue(PalmLeavesProperties.DynamicPalmLeavesBlock.DIRECTION);
        if (direction <= 0 || direction > fronds.length) {
            return;
        }
        parts.add(fronds[direction - 1]);
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return state.getValue(PalmLeavesProperties.DynamicPalmLeavesBlock.DIRECTION);
    }

    private static BlockStateModelPart partFromUnculled(List<BakedQuad> quads, Material.Baked particle) {
        QuadCollection.Builder builder = new QuadCollection.Builder();
        for (BakedQuad quad : quads) {
            builder.addUnculledFace(quad);
        }
        return new SimpleModelWrapper(builder.build(), false, particle);
    }
}
