package com.dtteam.dynamictrees.model.blockstate;

import com.dtteam.dynamictrees.api.network.Connections;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.ThickBranchBlock;
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
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;

import java.util.List;

public record ThickBranchBlockStateModel(
        BranchBlockStateModel fallback,
        BranchMultiPartHolder trunkBark,
        BranchMultiPartHolder trunkRings
) implements DynamicBlockStateModel, BlockStateModelWithConnectionData {

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return fallback.materialFlags();
    }

    @Override
    public Material.Baked particleMaterial() {
        return fallback.particleMaterial();
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return ModelHelper.getModelConnections(level, pos, state);
    }

    @Override
    public void collectParts(BlockState state, List<BlockStateModelPart> parts, Connections connectionsData) {
        int coreRadius = TreeHelper.getRadius(state);
        if (coreRadius <= BranchBlock.MAX_RADIUS) {
            fallback.collectParts(state, parts, connectionsData);
            return;
        }
        coreRadius = Mth.clamp(coreRadius, BranchBlock.MAX_RADIUS + 1, ThickBranchBlock.MAX_RADIUS_THICK);

        final int[] connections = connectionsData.getAllRadii();
        final Direction forceRingDir = ((ModelConnections) connectionsData).getRingOnly();
        final int twigRadius = ((ModelConnections) connectionsData).getFamily().getPrimaryThickness();

        int numConnections = BranchBlockStateModel.countConnections(connections);

        if (numConnections == 0 && forceRingDir != null) return;

        if (forceRingDir != null) {
            connections[forceRingDir.get3DDataValue()] = 0;
            parts.add(trunkRings.getPart(forceRingDir, coreRadius - 9));
        }

        boolean branchesAround = areBranchesAround(connections);

        for (Direction face : Direction.values()) {
            gatherTrunkParts(parts, face, connections, twigRadius, branchesAround, coreRadius);
        }

    }

    private static boolean areBranchesAround(int[] connections) {
        return connections[2] + connections[3] + connections[4] + connections[5] != 0;
    }

    private void gatherTrunkParts(List<BlockStateModelPart> parts, Direction face, int[] connections, int twigRadius, boolean branchesAround, int coreRadius) {
        if (face == Direction.UP || face == Direction.DOWN) {
            if (connections[face.get3DDataValue()] < twigRadius && !branchesAround) {
                parts.add(this.trunkRings.getPart(face, coreRadius - 9));
            } else if (connections[face.get3DDataValue()] < coreRadius) {
                parts.add(this.trunkBark.getPart(face, coreRadius - 9));
            }
        } else {
            parts.add(trunkBark.getPart(face, coreRadius - 9));
        }
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        ModelConnections connectionsData = ModelHelper.getModelConnections(level, pos, state);
        collectParts(state, parts, connectionsData);
    }


}