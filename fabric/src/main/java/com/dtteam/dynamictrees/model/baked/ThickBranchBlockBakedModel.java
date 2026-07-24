package com.dtteam.dynamictrees.model.baked;

import com.dtteam.dynamictrees.api.network.Connections;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.ThickBranchBlock;
import com.dtteam.dynamictrees.model.BranchMultiPartHolder;
import com.dtteam.dynamictrees.model.ModelConnections;
import com.dtteam.dynamictrees.model.parts.BranchModelPart;
import com.dtteam.dynamictrees.tree.TreeHelper;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;
import java.util.List;

/**
 * Dynamic thick branch (trunk) model for Fabric. Mirrors the NeoForge {@code ThickBranchBlockStateModel}.
 */
public class ThickBranchBlockBakedModel extends BasicBranchBlockBakedModel {

    private final BranchMultiPartHolder trunkBark;  // The trunk will always feature bark on its sides.
    private final BranchMultiPartHolder trunkRings; // The trunk will feature rings on its top and bottom.

    public ThickBranchBlockBakedModel(BasicBranchBlockBakedModel fallback,
                                      BranchMultiPartHolder trunkBark, BranchMultiPartHolder trunkRings) {
        super(fallback.cores, fallback.sleeves, fallback.rings, fallback.sleeveRings);
        this.trunkBark = trunkBark;
        this.trunkRings = trunkRings;
    }

    /**
     * Bakes a thick branch model out of the given unbaked trunk parts and a regular branch fallback.
     * Mirrors the NeoForge {@code UnbakedBranchModel#bakeThick}.
     */
    public static ThickBranchBlockBakedModel bakeThick(
            ModelBaker baker, BasicBranchBlockBakedModel fallback,
            BranchModelPart.UnbakedThickTrunk unbakedBark, BranchModelPart.UnbakedThickTrunk unbakedRings) {
        BranchMultiPartHolder trunksBark = new BranchMultiPartHolder();
        BranchMultiPartHolder trunksRings = new BranchMultiPartHolder();

        for (int radius = BranchBlock.MAX_RADIUS + 1; radius <= ThickBranchBlock.MAX_RADIUS_THICK; radius++) {
            trunksBark.putAllParts(radius, unbakedBark.bakeAllSides(baker, radius));
            trunksRings.putAllParts(radius, unbakedRings.bakeSides(baker, radius, EnumSet.of(Direction.UP, Direction.DOWN)));
        }

        return new ThickBranchBlockBakedModel(fallback, trunksBark, trunksRings);
    }

    @Override
    public void collectParts(BlockState state, List<BlockStateModelPart> parts, Connections connectionsData) {
        int coreRadius = TreeHelper.getRadius(state);
        if (coreRadius <= BranchBlock.MAX_RADIUS) {
            super.collectParts(state, parts, connectionsData);
            return;
        }
        coreRadius = Mth.clamp(coreRadius, BranchBlock.MAX_RADIUS + 1, ThickBranchBlock.MAX_RADIUS_THICK);
        if (!(connectionsData instanceof ModelConnections modelConnections)) return;

        final int[] connections = connectionsData.getAllRadii();
        final Direction forceRingDir = modelConnections.getRingOnly();
        final int twigRadius = modelConnections.getFamily().getPrimaryThickness();

        int numConnections = countConnections(connections);

        if (numConnections == 0 && forceRingDir != null) return;

        if (forceRingDir != null) {
            connections[forceRingDir.get3DDataValue()] = 0;
            addPart(parts, trunkRings.getPart(forceRingDir, coreRadius));
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
                addPart(parts, this.trunkRings.getPart(face, coreRadius));
            } else if (connections[face.get3DDataValue()] < coreRadius) {
                addPart(parts, this.trunkBark.getPart(face, coreRadius));
            }
        } else {
            addPart(parts, trunkBark.getPart(face, coreRadius));
        }
    }
}
