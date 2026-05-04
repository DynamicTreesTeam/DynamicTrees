package com.dtteam.dynamictrees.model.blockstate;

import com.dtteam.dynamictrees.registry.PottedSaplingBlockEntityNF;
import com.dtteam.dynamictrees.tree.family.Family;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

import java.util.List;
import java.util.Optional;

public record AerialRootsSoilBlockStateModel(
        Material.Baked particleMaterial
) implements DynamicBlockStateModel {

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return 0;
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return level.getModelData(pos).get(PottedSaplingBlockEntityNF.SPECIES);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {

    }

    public record Unbaked(Identifier end, Identifier overlay, Identifier overlay_end, Identifier side, Optional<Family> family) implements CustomUnbakedBlockStateModel {

        public static final String END_TEXTURE = "end";
        public static final String OVERLAY_TEXTURE = "overlay";
        public static final String OVERLAY_END_TEXTURE = "overlay_end";
        public static final String SIDE_TEXTURE = "side";
        public static final String TEXTURES = "textures";
        public static final String FAMILY = "family";

        private record RootsSoilTextures(Identifier end, Identifier overlay, Identifier overlay_end, Identifier side) {
            public static final MapCodec<RootsSoilTextures> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    Identifier.CODEC.fieldOf(END_TEXTURE).forGetter(RootsSoilTextures::end),
                    Identifier.CODEC.fieldOf(OVERLAY_TEXTURE).forGetter(RootsSoilTextures::overlay),
                    Identifier.CODEC.fieldOf(OVERLAY_END_TEXTURE).forGetter(RootsSoilTextures::overlay_end),
                    Identifier.CODEC.fieldOf(SIDE_TEXTURE).forGetter(RootsSoilTextures::side)
            ).apply(i, RootsSoilTextures::new));
        }

        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                RootsSoilTextures.CODEC.codec().fieldOf(TEXTURES).forGetter(m ->
                        new RootsSoilTextures(m.end(), m.overlay(), m.overlay_end(), m.side())),
                Family.CODEC.optionalFieldOf(FAMILY).forGetter(Unbaked::family)
        ).apply(i, (textures, family) ->
                new Unbaked(textures.end(), textures.overlay(), textures.overlay_end(), textures.side(), family)));

        @Override
        public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
            return CODEC;
        }

        @Override
        public BlockStateModel bake(ModelBaker modelBaker) {

            return new AerialRootsSoilBlockStateModel(null);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {

        }
    }
}