package com.dtteam.dynamictrees.model;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

import java.util.List;

public record BranchBlockStateModel(BranchBlockStateModelPart[] variants) implements DynamicBlockStateModel {

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return this.variants[0].particleMaterial();
    }

    @Override
    public Material.Baked particleMaterial() {
        return this.variants[0].particleMaterial();
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return this.variants[0].materialFlags();
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return level.getBlockState(pos.north()).getBlock();
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {

        //Test to see if this weird way of loading models works
        int index = level.getBlockState(pos.north()).is(Blocks.STONE) ? 1 : 0;

        parts.add(this.variants[index]);
    }

    public record Unbaked(BranchBlockStateModelPart.Unbaked part) implements CustomUnbakedBlockStateModel {

        public static final MapCodec<Unbaked> CODEC = BranchBlockStateModelPart.Unbaked.CODEC.xmap(Unbaked::new, Unbaked::part);

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            this.part.resolveDependencies(resolver);
        }

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            return new BranchBlockStateModel(this.part.bakeAll(baker));
        }

        @Override
        public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
            return CODEC;
        }
    }
}