package com.dtteam.dynamictrees.model.blockstate;

import com.dtteam.dynamictrees.api.network.Connections;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.ThickBranchBlock;
import com.dtteam.dynamictrees.model.BlockStateModelWithConnectionData;
import com.dtteam.dynamictrees.model.ModelConnections;
import com.dtteam.dynamictrees.model.ModelHelper;
import com.dtteam.dynamictrees.model.parts.BranchModelPart;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.family.Family;
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
        BranchModelPart[] trunksSideBark,
        BranchModelPart[] trunksTopBark,
        BranchModelPart[] trunksTopRings,
        BranchModelPart[] trunksBotRings
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

        int[] connections = new int[]{0, 0, 0, 0, 0, 0};
        Direction forceRingDir = null;
        int twigRadius = 1;

        if (connectionsData instanceof ModelConnections branchConnections) {
            connections = branchConnections.getAllRadii();
            forceRingDir = branchConnections.getRingOnly();
            Family family = branchConnections.getFamily();
            if (family.isValid()) {
                twigRadius = family.getPrimaryThickness();
            }
        }

        //Count number of connections
        int numConnections = 0;
        for (int i : connections) {
            numConnections += (i != 0) ? 1 : 0;
        }
        if (numConnections == 0 && forceRingDir != null) return;

        if (forceRingDir != null) {
            connections[forceRingDir.get3DDataValue()] = 0;
            parts.add(this.trunksBotRings[coreRadius - 9].faceOnly(forceRingDir, false));
        }

        boolean branchesAround = connections[2] + connections[3] + connections[4] + connections[5] != 0;

        parts.add(this.trunksSideBark[coreRadius - 9]);
        for (Direction face : Direction.values()) {
            if (face == Direction.UP || face == Direction.DOWN) {
                if (connections[face.get3DDataValue()] < twigRadius && !branchesAround) {
                    parts.add(this.trunksTopRings[coreRadius - 9].faceOnly(face, false));
                } else if (connections[face.get3DDataValue()] < coreRadius) {
                    parts.add(this.trunksTopBark[coreRadius - 9].faceOnly(face, false));
                }
            }
        }

    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        ModelConnections connectionsData = ModelHelper.getModelConnections(level, pos, state);
        collectParts(state, parts, connectionsData);
    }


}