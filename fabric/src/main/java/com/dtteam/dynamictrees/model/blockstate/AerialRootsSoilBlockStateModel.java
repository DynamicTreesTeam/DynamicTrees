package com.dtteam.dynamictrees.model.blockstate;

import com.dtteam.dynamictrees.model.BlockStateModelWithRadius;
import com.dtteam.dynamictrees.model.FabricDynamicBlockStateModel;
import com.dtteam.dynamictrees.model.parts.AerialRootSoilModelPart;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.family.Family;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;

/**
 * Fabric port of the NeoForge {@code AerialRootsSoilBlockStateModel}; deserialized from
 * blockstate JSONs with type {@code dynamictrees:aerial_roots_soil}.
 */
public record AerialRootsSoilBlockStateModel(
        AerialRootSoilModelPart[] soilParts
) implements FabricDynamicBlockStateModel, BlockStateModelWithRadius {

    private record RadiusGeometryKey(AerialRootsSoilBlockStateModel model, int radius) {}

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return new RadiusGeometryKey(this, TreeHelper.getRadius(state));
    }

    @Override
    public void collectParts(BlockState state, List<BlockStateModelPart> parts, int radius) {
        if (radius == 0 || radius > 8) return;
        parts.add(soilParts[radius - 1]);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        collectParts(state, parts, TreeHelper.getRadius(state));
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
        public BlockStateModel bake(ModelBaker baker) {
            AerialRootSoilModelPart[] soilParts = new AerialRootSoilModelPart[8];

            Material.Baked endMat = bakeMaterial(baker, end);
            Material.Baked overlayMat = bakeMaterial(baker, overlay);
            Material.Baked overlayEndMat = bakeMaterial(baker, overlay_end);
            Material.Baked sideMat = bakeMaterial(baker, side);

            AerialRootSoilModelPart.UnbakedPart unbakedPart = new AerialRootSoilModelPart.UnbakedPart(endMat, overlayMat, overlayEndMat, sideMat);

            for (int i = 0; i < 8; i++) {
                soilParts[i] = unbakedPart.bake(baker, i + 1);
            }

            return new AerialRootsSoilBlockStateModel(soilParts);
        }

        private Material.Baked bakeMaterial(ModelBaker baker, Identifier texture) {
            return baker.materials().get(new Material(texture, false), texture::toDebugFileName);
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {}
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return soilParts[0].materialFlags();
    }

    @Override
    public Material.Baked particleMaterial() {
        return soilParts[0].particleMaterial();
    }
}
