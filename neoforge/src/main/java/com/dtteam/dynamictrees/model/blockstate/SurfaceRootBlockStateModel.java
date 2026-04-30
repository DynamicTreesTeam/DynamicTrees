package com.dtteam.dynamictrees.model.blockstate;

import com.dtteam.dynamictrees.api.network.Connections;
import com.dtteam.dynamictrees.api.network.RootConnections;
import com.dtteam.dynamictrees.block.branch.SurfaceRootBlock;
import com.dtteam.dynamictrees.model.BlockStateModelWithConnectionData;
import com.dtteam.dynamictrees.model.ModelHelper;
import com.dtteam.dynamictrees.model.parts.SurfaceRootBlockStateModelPart;
import com.dtteam.dynamictrees.utility.CoordUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record SurfaceRootBlockStateModel(
        SurfaceRootBlockStateModelPart[][] cores,
        SurfaceRootBlockStateModelPart[][] sleeves,
        SurfaceRootBlockStateModelPart[][] verts,
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
        int coreRadius = 0;
        if (state.getBlock() instanceof SurfaceRootBlock root)
            coreRadius = root.getRadius(state);
        if (coreRadius == 0) return;

        int[] connections = new int[]{0, 0, 0, 0};
        RootConnections.ConnectionLevel[] connectionLevels = RootConnections.PLACEHOLDER_CONNECTION_LEVELS.clone();

        if (connectionsData instanceof RootConnections rootConnections) {
            connections = rootConnections.getAllRadii();
            connectionLevels = rootConnections.getConnectionLevels();
        }

        for (int i = 0; i < connections.length; i++) {
            connections[i] = Mth.clamp(connections[i], 0, coreRadius);
        }

        //The source direction is the biggest connection from one of the 6 directions
        Direction sourceDir = get2DSourceDir(coreRadius, connections);
        if (sourceDir == null) {
            sourceDir = Direction.DOWN;
        }
        int coreDir = resolveCoreDir(sourceDir);

        boolean isGrounded = state.getValue(SurfaceRootBlock.GROUNDED);
        if (isGrounded) {
            parts.add(cores[coreDir][coreRadius - 1]);
        }

        //Get quads for sleeves models
        if (coreRadius != 8) { //Special case for r!=8.. If it's a solid block so it has no sleeves
            for (Direction connDir : CoordUtils.HORIZONTALS) {
                int idx = connDir.get2DDataValue();
                int connRadius = connections[idx];
                if (connRadius > 0) {
                    if (isGrounded) {
                        parts.add(sleeves[idx][connRadius - 1]);
                    }
                    if (connectionLevels[idx] == RootConnections.ConnectionLevel.HIGH) {
                        parts.add(verts[idx][connRadius - 1]);
                    }
                }
            }
        }
    }

    /**
     * Converts direction DUNSWE to 3 axis numbers for Y,Z,X
     *
     * @param dir
     * @return
     */
    protected int resolveCoreDir(Direction dir) {
        return dir.getAxis() == Direction.Axis.X ? 1 : 0;
    }

    @Nullable
    public static Direction get2DSourceDir(int coreRadius, int[] connections) {
        int largestConnection = 0;
        Direction sourceDir = null;

        for (Direction dir : CoordUtils.HORIZONTALS) {
            int connRadius = connections[dir.get2DDataValue()];
            if (connRadius > largestConnection) {
                largestConnection = connRadius;
                sourceDir = dir;
            }
        }

        if (largestConnection < coreRadius) {
            sourceDir = null;//Has no source node
        }
        return sourceDir;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        RootConnections connectionsData = ModelHelper.getRootConnections(level, pos, state);
        collectParts(state, parts, connectionsData);
    }

    public record Unbaked(Identifier barkTexture) implements CustomUnbakedBlockStateModel {

        public static final String BARK_TEXTURE = "bark_texture";

        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Identifier.CODEC.fieldOf(BARK_TEXTURE).forGetter(Unbaked::barkTexture)
        ).apply(i, Unbaked::new));

        @Override
        public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {}

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            SurfaceRootBlockStateModelPart[][] sleeves = new SurfaceRootBlockStateModelPart[4][7];
            SurfaceRootBlockStateModelPart[][] cores = new SurfaceRootBlockStateModelPart[2][8]; //8 Cores for 2 axis(X, Z) with the bark texture on all 6 sides rotated appropriately.
            SurfaceRootBlockStateModelPart[][] verts = new SurfaceRootBlockStateModelPart[4][8];

            Material.Baked barkMat = baker.materials().get(new Material(barkTexture, false), barkTexture::toDebugFileName);

            SurfaceRootBlockStateModelPart.UnbakedCore unbakedCores = new SurfaceRootBlockStateModelPart.UnbakedCore(barkMat);
            SurfaceRootBlockStateModelPart.UnbakedSleeve unbakedSleeves = new SurfaceRootBlockStateModelPart.UnbakedSleeve(barkMat);
            SurfaceRootBlockStateModelPart.UnbakedVert unbakedRings = new SurfaceRootBlockStateModelPart.UnbakedVert(barkMat);

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

            return new SurfaceRootBlockStateModel(cores, sleeves, verts, barkMat);
        }
    }

}