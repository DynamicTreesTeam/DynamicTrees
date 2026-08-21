package com.dtteam.dynamictrees.model.baked;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class DynamicTreesBlockStateModel implements BlockStateModel {
    private final Material.Baked particle;

    public DynamicTreesBlockStateModel(TextureAtlasSprite particleSprite) {
        this.particle = new Material.Baked(particleSprite, false);
    }

    public DynamicTreesBlockStateModel(Material.Baked particle) {
        this.particle = particle;
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        collectParts(random, parts);
    }

    @Override
    public Material.Baked particleMaterial() {
        return particle;
    }

    @Override
    public int materialFlags() {
        return 0;
    }

    protected void addPart(List<BlockStateModelPart> parts, List<BakedQuad> quads) {
        if (quads == null || quads.isEmpty()) {
            return;
        }
        parts.add(partFromQuads(quads, particle));
    }

    public static BlockStateModelPart partFromQuads(List<BakedQuad> quads, Material.Baked particle) {
        QuadCollection.Builder builder = new QuadCollection.Builder();
        for (BakedQuad quad : quads) {
            builder.addCulledFace(quad.direction(), quad);
        }
        return new SimpleModelWrapper(builder.build(), true, particle);
    }
}
