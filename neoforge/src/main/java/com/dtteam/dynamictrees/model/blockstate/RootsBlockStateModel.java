package com.dtteam.dynamictrees.model.blockstate;

import com.dtteam.dynamictrees.api.network.Connections;
import com.dtteam.dynamictrees.model.BlockStateModelWithConnectionData;
import com.dtteam.dynamictrees.model.ModelConnections;
import com.dtteam.dynamictrees.model.ModelHelper;
import com.mojang.serialization.Codec;
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

public record RootsBlockStateModel(
        BranchBlockStateModel exposed
) implements DynamicBlockStateModel, BlockStateModelWithConnectionData {

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return this.exposed.materialFlags();
    }

    @Override
    public Material.Baked particleMaterial() {
        return exposed.particleMaterial();
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return ModelHelper.getRootConnections(level, pos, state);
    }

    @Override
    public void collectParts(BlockState state, List<BlockStateModelPart> parts, Connections connectionsData) {
        exposed.collectParts(state, parts, connectionsData);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        ModelConnections connectionsData = ModelHelper.getModelConnections(level, pos, state);
        collectParts(state, parts, connectionsData);

    }

    public record Unbaked(Identifier exposedSide, Identifier exposedTop, boolean opaque) implements CustomUnbakedBlockStateModel {

        public static final String EXPOSED_SIDE = "side";
        public static final String EXPOSED_TOP = "top";
        public static final String OPAQUE = "opaque";
        public static final String TEXTURES = "textures";

        private record RootsTextures(Identifier exposedSide, Identifier exposedTop) {
            public static final MapCodec<RootsTextures> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    Identifier.CODEC.fieldOf(EXPOSED_SIDE).forGetter(RootsTextures::exposedSide),
                    Identifier.CODEC.fieldOf(EXPOSED_TOP).forGetter(RootsTextures::exposedTop)
            ).apply(i, RootsTextures::new));
        }

        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                RootsTextures.CODEC.codec().fieldOf(TEXTURES).forGetter(m -> new RootsTextures(m.exposedSide, m.exposedTop)),
                Codec.BOOL.optionalFieldOf(OPAQUE).forGetter(o -> Optional.of(o.opaque))
        ).apply(i, (textures, opaque) -> new Unbaked(textures.exposedSide, textures.exposedTop, opaque.orElse(false))));

        @Override
        public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(Resolver resolver) {}

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            BranchBlockStateModel exposed_outside = UnbakedBranchModel.bakeRegular(baker, exposedSide, exposedTop);

            return new RootsBlockStateModel(exposed_outside);
        }

//        public BranchBlockStateModel bakeRoots(ModelBaker baker, Identifier sideTexture, Identifier topTexture) {
//            BranchModelPart[][] sleeves = new BranchModelPart[6][7];
//            BranchModelPart[][] cores = new BranchModelPart[3][8]; // 8 Cores for 3 axis with the exposed_side texture all all 6 sides rotated appropriately.
//            BranchModelPart[] rings = new BranchModelPart[8]; // 8 Cores with the ring textures on all 6 sides.
//
//            Material.Baked barkMat = baker.materials().get(new Material(sideTexture, false), sideTexture::toDebugFileName);
//            Material.Baked ringsMat = baker.materials().get(new Material(topTexture, false), topTexture::toDebugFileName);
//
//            BranchModelPart.UnbakedCore unbakedCores = new BranchModelPart.UnbakedCore(barkMat, false);
//            BranchModelPart.UnbakedSleeve unbakedSleeves = new BranchModelPart.UnbakedSleeve(barkMat);
//            BranchModelPart.UnbakedCore unbakedRings = new BranchModelPart.UnbakedCore(ringsMat, false);
//
//            for (int i = 0; i < 8; i++) {
//                int radius = i + 1;
//                if (radius < 8) {
//                    for (Direction dir : Direction.values()) {
//                        sleeves[dir.get3DDataValue()][i] = unbakedSleeves.bake(baker, radius, dir);
//                    }
//                }
//                cores[0][i] = unbakedCores.bake(baker, radius, Direction.Axis.Y); //DOWN<->UP
//                cores[1][i] = unbakedCores.bake(baker, radius, Direction.Axis.Z); //NORTH<->SOUTH
//                cores[2][i] = unbakedCores.bake(baker, radius, Direction.Axis.X); //WEST<->EAST
//
//                rings[i] = unbakedRings.bake(baker, radius, Direction.Axis.Y);
//            }
//
//            return new BranchBlockStateModel(cores, sleeves, rings, barkMat);
//        }

    }

}