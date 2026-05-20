package com.dtteam.dynamictrees.model.blockstate;

import com.dtteam.dynamictrees.api.network.Connections;
import com.dtteam.dynamictrees.model.BlockStateModelWithConnectionData;
import com.dtteam.dynamictrees.model.BranchMultiPartHolder;
import com.dtteam.dynamictrees.model.ModelConnections;
import com.dtteam.dynamictrees.model.ModelHelper;
import com.dtteam.dynamictrees.tree.TreeHelper;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record BranchBlockStateModel(
        BranchMultiPartHolder cores,
        BranchMultiPartHolder sleeves,
        BranchMultiPartHolder rings,
        BranchMultiPartHolder sleeveRings
) implements DynamicBlockStateModel, BlockStateModelWithConnectionData {

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return ModelHelper.getModelConnections(level, pos, state);
    }

    @Override
    public void collectParts(BlockState state, List<BlockStateModelPart> parts, Connections connectionsData) {
        final int coreRadius = TreeHelper.getRadius(state);
        if (coreRadius > 8 || coreRadius == 0) return;
        if (!(connectionsData instanceof ModelConnections)) return;

        final int[] connections = connectionsData.getAllRadii();
        final Direction forceRingDir = ((ModelConnections) connectionsData).getRingOnly();
        final int twigRadius = ((ModelConnections) connectionsData).getFamily().getPrimaryThickness();
        final int numConnections = countConnections(connections);

        final Direction sourceDir = get3DSourceDir(coreRadius, connections);
        final Direction.Axis coreDir = sourceDir == null ? Direction.Axis.Y : sourceDir.getAxis();
        final Direction coreRingDir = (numConnections == 1 && sourceDir != null) ? sourceDir.getOpposite() : null;

        if (forceRings(numConnections, forceRingDir)) {
            addPart(parts,rings.getPart(forceRingDir, coreRadius));
        } else {
            for (Direction face : Direction.values()) {
                gatherCoreParts(parts, face, coreRadius, connections, coreRingDir, coreDir);
                gatherSleeveParts(parts, face, coreRadius, connections, twigRadius);
            }
        }
        //The null side is usually empty, but roots have the cross.
        addPart(parts, cores.getPart(coreDir, null, coreRadius - 1));
    }

    private void gatherSleeveParts(List<BlockStateModelPart> parts, Direction face, int coreRadius, int[] connections, int twigRadius) {
        // Get quads for sleeves models.
        for (Direction connDir : Direction.values()) {
            final int idx = connDir.get3DDataValue();
            final int connRadius = connections[idx];
            if (connRadius == 0) continue;
            // If the connection side matches the quadpull side then cull the sleeve face.
            // Don't cull radius-1 connections for leaves (which are partly transparent).
            if (coreRadius < 8 && connRadius <= twigRadius || face != connDir) {
                addPart(parts,sleeves.getPart(connDir, connRadius));
            }
            if (face == connDir && !sleeveRings.isEmpty()){
                addPart(parts,sleeveRings.getPart(connDir, connRadius));
            }
        }
    }

    private void gatherCoreParts(List<BlockStateModelPart> parts, Direction face, int coreRadius, int[] connections, Direction coreRingDir, Direction.Axis coreDir) {
        if (coreRadius == connections[face.get3DDataValue()]) return;

        if (coreRingDir != null && coreRingDir == face) {
            addPart(parts,rings.getPart(face, coreRadius));
        } else {
            addPart(parts,cores.getPart(coreDir, face, coreRadius));
        }
    }

    private static void addPart(List<BlockStateModelPart> parts, @Nullable BlockStateModelPart part){
        if (part == null) return;
        parts.add(part);
    }

    private static boolean forceRings(int numConnections, Direction forceRingDir) {
        return numConnections == 0 && forceRingDir != null;
    }

    public static int countConnections(int[] connections) {
        int numConnections = 0;
        for (int i : connections) {
            numConnections += (i != 0) ? 1 : 0;
        }
        return numConnections;
    }

    @Nullable
    public static Direction get3DSourceDir(int coreRadius, int[] connections) {
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

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        ModelConnections connectionsData = ModelHelper.getModelConnections(level, pos, state);
        collectParts(state, parts, connectionsData);
    }

    @Override
    public Material.Baked particleMaterial() {
        return cores.getFirstMaterial();
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return cores.materialFlags();
    }
}