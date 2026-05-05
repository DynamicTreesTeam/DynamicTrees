package com.dtteam.dynamictrees.model.blockstate;

import com.dtteam.dynamictrees.api.network.Connections;
import com.dtteam.dynamictrees.model.BlockStateModelWithConnectionData;
import com.dtteam.dynamictrees.model.ModelConnections;
import com.dtteam.dynamictrees.model.ModelHelper;
import com.dtteam.dynamictrees.model.parts.SurfaceRootModelPart;
import com.dtteam.dynamictrees.utility.CoordUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

import java.util.List;

public record RootsBlockStateModel(
        SurfaceRootModelPart[][] cores,
        SurfaceRootModelPart[][] sleeves,
        SurfaceRootModelPart[][] verts,
        Material.Baked particleMaterial
) implements DynamicBlockStateModel, BlockStateModelWithConnectionData {

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return this.cores[0][0].materialFlags();
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return ModelHelper.getRootConnections(level, pos, state);
    }

    @Override
    public void collectParts(BlockState state, List<BlockStateModelPart> parts, Connections connectionsData) {

    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        ModelConnections connectionsData = ModelHelper.getModelConnections(level, pos, state);
        collectParts(state, parts, connectionsData);
    }

    public record Unbaked(Identifier barkTexture, Identifier ringsTexture) implements CustomUnbakedBlockStateModel {

        public static final String BARK_TEXTURE = "bark";
        public static final String RINGS_TEXTURE = "rings";
        public static final String TEXTURES = "textures";

        private record RootsTextures(Identifier bark, Identifier rings) {
            public static final MapCodec<RootsTextures> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    Identifier.CODEC.fieldOf(BARK_TEXTURE).forGetter(RootsTextures::bark),
                    Identifier.CODEC.fieldOf(RINGS_TEXTURE).forGetter(RootsTextures::rings)
            ).apply(i, RootsTextures::new));
        }

        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                RootsTextures.CODEC.codec().fieldOf(TEXTURES).forGetter(m -> new RootsTextures(m.barkTexture, m.ringsTexture))
        ).apply(i, textures -> new Unbaked(textures.bark, textures.rings)));

        @Override
        public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(Resolver resolver) {}

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            SurfaceRootModelPart[][] sleeves = new SurfaceRootModelPart[4][7];
            SurfaceRootModelPart[][] cores = new SurfaceRootModelPart[2][8]; //8 Cores for 2 axis(X, Z) with the bark texture on all 6 sides rotated appropriately.
            SurfaceRootModelPart[][] verts = new SurfaceRootModelPart[4][8];

            Material.Baked barkMat = baker.materials().get(new Material(barkTexture, false), barkTexture::toDebugFileName);

            SurfaceRootModelPart.UnbakedCore unbakedCores = new SurfaceRootModelPart.UnbakedCore(barkMat);
            SurfaceRootModelPart.UnbakedSleeve unbakedSleeves = new SurfaceRootModelPart.UnbakedSleeve(barkMat);
            SurfaceRootModelPart.UnbakedVert unbakedRings = new SurfaceRootModelPart.UnbakedVert(barkMat);

            for (int r = 0; r < 8; r++) {
                int radius = r + 1;
                if (radius < 8) {
                    for (Direction dir : CoordUtils.HORIZONTALS) {
                        int horIndex = dir.get2DDataValue();
                        sleeves[horIndex][r] = unbakedSleeves.bake(baker, radius, dir);
                        verts[horIndex][r] = unbakedRings.bake(baker, radius, dir);
                    }
                }
                cores[0][r] = unbakedCores.bake(baker, radius, Direction.Axis.Z); //NORTH<->SOUTH
                cores[1][r] = unbakedCores.bake(baker, radius, Direction.Axis.X); //WEST<->EAST
            }

            return new RootsBlockStateModel(cores, sleeves, verts, barkMat);
        }
    }

}