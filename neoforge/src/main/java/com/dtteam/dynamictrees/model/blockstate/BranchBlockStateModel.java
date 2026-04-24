package com.dtteam.dynamictrees.model.blockstate;

import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.model.ModelConnections;
import com.dtteam.dynamictrees.model.parts.BranchBlockStateModelPartCore;
import com.dtteam.dynamictrees.model.parts.BranchBlockStateModelPartSleeve;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public record BranchBlockStateModel(
        BranchBlockStateModelPartCore[][] cores,
        BranchBlockStateModelPartSleeve[][] sleeves,
        BranchBlockStateModelPartCore[] rings,
        Material.Baked particleMaterial
) implements DynamicBlockStateModel {

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return this.cores[0][0].materialFlags();
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return getModelConnections(level, pos, state);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        final int coreRadius = getRadius(state);
        if (coreRadius > 8) return;

        int[] connections = new int[]{0, 0, 0, 0, 0, 0};
        Direction forceRingDir = null;
        final AtomicInteger twigRadius = new AtomicInteger(1);

        ModelConnections connectionsData = getModelConnections(level, pos, state);
        if (connectionsData != null) {
            connections = connectionsData.getAllRadii();
            forceRingDir = connectionsData.getRingOnly();

            connectionsData.getFamily().ifValid(family ->
                    twigRadius.set(family.getPrimaryThickness()));
        }

        // Count number of connections.
        int numConnections = 0;
        for (int i : connections) {
            numConnections += (i != 0) ? 1 : 0;
        }

        if (numConnections == 0 && forceRingDir != null) {
            parts.add(rings[coreRadius - 1].faceOnly(forceRingDir, false));
        } else {
            // The source direction is the biggest connection from one of the 6 directions.
            final Direction sourceDir = getSourceDir(coreRadius, connections);
            final int coreDir = resolveCoreDir(sourceDir);

            // This is for drawing the rings on a terminating branch.
            final Direction coreRingDir = (numConnections == 1 && sourceDir != null) ? sourceDir.getOpposite() : null;

            for (Direction face : Direction.values()) {
                // Get quads for core model.
                if (coreRadius != connections[face.get3DDataValue()]) {
                    if ((coreRingDir == null || coreRingDir != face)) {
                        parts.add(cores[coreDir][coreRadius - 1].faceOnly(face, coreRadius == 8));
                    } else {
                        parts.add(rings[coreRadius - 1].faceOnly(face, false));
                    }
                }
                // Get quads for sleeves models.
                if (coreRadius != 8) { // Special case for r!=8... If it's a solid block, so it has no sleeves.
                    for (Direction connDir : Direction.values()) {
                        final int idx = connDir.get3DDataValue();
                        final int connRadius = connections[idx];
                        // If the connection side matches the quadpull side then cull the sleeve face.  Don't cull radius-1 connections for leaves (which are partly transparent).
                        if (connRadius > 0 && (connRadius <= twigRadius.get() || face != connDir)) {
                            parts.add(sleeves[idx][connRadius - 1].faceOnly(face, false));
                        }
                    }
                }

            }
        }
    }

    public ModelConnections getModelConnections(@NotNull BlockAndTintGetter world, @NotNull BlockPos pos, @NotNull BlockState state) {
        ModelConnections modelConnections;
        if (state.getBlock() instanceof BranchBlock branchBlock) {
            modelConnections = new ModelConnections(branchBlock.getConnectionData(world, pos, state)).setFamily(branchBlock.getFamily());
        } else {
            modelConnections = new ModelConnections();
        }

        return modelConnections;
    }

    /**
     * Locates the side with the largest neighbor radius that's equal to or greater than this branch block
     *
     * @param coreRadius the radius of the branch block
     * @param connections an array of 6 integers, one for the radius of each connecting side. DUNSWE.
     */
    @Nullable
    private Direction getSourceDir(int coreRadius, int[] connections) {
        int largestConnection = 0;
        Direction sourceDir = null;

        for (Direction dir : Direction.values()) {
            int connRadius = connections[dir.get3DDataValue()];
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

    /**
     * Converts direction DUNSWE to 3 axis numbers for Y,Z,X
     */
    private int resolveCoreDir(@Nullable Direction dir) {
        if (dir == null) {
            return 0;
        }
        return dir.get3DDataValue() >> 1;
    }

    private int getRadius(BlockState blockState) {
        // This way works with branches that don't have the RADIUS property, like cactus
        return ((BranchBlock) blockState.getBlock()).getRadius(blockState);
    }

    public record Unbaked(Identifier barkTexture, Identifier ringsTexture) implements CustomUnbakedBlockStateModel {

        public static final String BARK_TEXTURE = "bark_texture";
        public static final String RINGS_TEXTURE = "rings_texture";

        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Identifier.CODEC.fieldOf(BARK_TEXTURE).forGetter(Unbaked::barkTexture),
                Identifier.CODEC.fieldOf(RINGS_TEXTURE).forGetter(Unbaked::ringsTexture)
        ).apply(i, Unbaked::new));

        @Override
        public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {}

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            BranchBlockStateModelPartSleeve[][] sleeves = new BranchBlockStateModelPartSleeve[6][7];
            BranchBlockStateModelPartCore[][] cores = new BranchBlockStateModelPartCore[3][8]; // 8 Cores for 3 axis with the bark texture all all 6 sides rotated appropriately.
            BranchBlockStateModelPartCore[] rings = new BranchBlockStateModelPartCore[8]; // 8 Cores with the ring textures on all 6 sides.

            Material.Baked barkMat = baker.materials().get(new Material(barkTexture, false), barkTexture::toDebugFileName);
            Material.Baked ringsMat = baker.materials().get(new Material(ringsTexture, false), ringsTexture::toDebugFileName);

            BranchBlockStateModelPartCore.Unbaked unbakedCores = new BranchBlockStateModelPartCore.Unbaked(barkMat, false);
            BranchBlockStateModelPartSleeve.Unbaked unbakedSleeves = new BranchBlockStateModelPartSleeve.Unbaked(barkMat);
            BranchBlockStateModelPartCore.Unbaked unbakedRings = new BranchBlockStateModelPartCore.Unbaked(ringsMat, false);

            for (int i = 0; i < 8; i++) {
                int radius = i + 1;
                if (radius < 8) {
                    for (Direction dir : Direction.values()) {
                        sleeves[dir.get3DDataValue()][i] = unbakedSleeves.bake(baker, radius, dir);
                    }
                }
                cores[0][i] = unbakedCores.bake(baker, radius, Direction.Axis.Y); //DOWN<->UP
                cores[1][i] = unbakedCores.bake(baker, radius, Direction.Axis.Z); //NORTH<->SOUTH
                cores[2][i] = unbakedCores.bake(baker, radius, Direction.Axis.X); //WEST<->EAST

                rings[i] = unbakedRings.bake(baker, radius, Direction.Axis.Y);
            }

            return new BranchBlockStateModel(cores, sleeves, rings, barkMat);
        }
    }
}