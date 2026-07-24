package com.dtteam.dynamictrees.model.baked;

import com.dtteam.dynamictrees.api.network.Connections;
import com.dtteam.dynamictrees.api.network.RootConnections;
import com.dtteam.dynamictrees.block.branch.SurfaceRootBlock;
import com.dtteam.dynamictrees.model.BlockStateModelWithConnectionData;
import com.dtteam.dynamictrees.model.FabricDynamicBlockStateModel;
import com.dtteam.dynamictrees.model.ModelHelper;
import com.dtteam.dynamictrees.model.parts.SurfaceRootModelPart;
import com.dtteam.dynamictrees.utility.CoordUtils;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Dynamic surface root model for Fabric. Mirrors the NeoForge {@code SurfaceRootBlockStateModel}.
 */
public class SurfaceRootBlockBakedModel implements FabricDynamicBlockStateModel, BlockStateModelWithConnectionData {

    protected final SurfaceRootModelPart[][] cores;
    protected final SurfaceRootModelPart[][] sleeves;
    protected final SurfaceRootModelPart[][] verts;
    protected final Material.Baked particleMaterial;

    public SurfaceRootBlockBakedModel(SurfaceRootModelPart[][] cores, SurfaceRootModelPart[][] sleeves,
                                      SurfaceRootModelPart[][] verts, Material.Baked particleMaterial) {
        this.cores = cores;
        this.sleeves = sleeves;
        this.verts = verts;
        this.particleMaterial = particleMaterial;
    }

    /**
     * Bakes a surface root model for the given bark texture. Mirrors the NeoForge
     * {@code SurfaceRootBlockStateModel.Unbaked#bake}.
     */
    public static SurfaceRootBlockBakedModel bake(ModelBaker baker, Identifier barkTexture) {
        SurfaceRootModelPart[][] sleeves = new SurfaceRootModelPart[4][7];
        SurfaceRootModelPart[][] cores = new SurfaceRootModelPart[2][8]; //8 Cores for 2 axis(X, Z) with the bark texture on all 6 sides rotated appropriately.
        SurfaceRootModelPart[][] verts = new SurfaceRootModelPart[4][8];

        Material.Baked barkMat = baker.materials().get(new Material(barkTexture, false), barkTexture::toDebugFileName);

        SurfaceRootModelPart.UnbakedCore unbakedCores = new SurfaceRootModelPart.UnbakedCore(barkMat);
        SurfaceRootModelPart.UnbakedSleeve unbakedSleeves = new SurfaceRootModelPart.UnbakedSleeve(barkMat);
        SurfaceRootModelPart.UnbakedVert unbakedVerts = new SurfaceRootModelPart.UnbakedVert(barkMat);

        for (int r = 0; r < 8; r++) {
            int radius = r + 1;
            if (radius < 8) {
                for (Direction dir : CoordUtils.HORIZONTALS) {
                    int horIndex = dir.get2DDataValue();
                    sleeves[horIndex][r] = unbakedSleeves.bake(baker, radius, dir);
                    verts[horIndex][r] = unbakedVerts.bake(baker, radius, dir);
                }
            }
            cores[0][r] = unbakedCores.bake(baker, radius, Direction.Axis.Z); //NORTH<->SOUTH
            cores[1][r] = unbakedCores.bake(baker, radius, Direction.Axis.X); //WEST<->EAST
        }

        return new SurfaceRootBlockBakedModel(cores, sleeves, verts, barkMat);
    }

    ///////////////////////////////////////////
    // FABRIC DYNAMIC GEOMETRY
    ///////////////////////////////////////////

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        collectParts(state, parts, ModelHelper.getRootConnections(level, pos, state));
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return ModelHelper.getRootConnections(level, pos, state);
    }

    ///////////////////////////////////////////
    // PART COLLECTION
    ///////////////////////////////////////////

    @Override
    public void collectParts(BlockState state, List<BlockStateModelPart> parts, Connections connectionsData) {
        int coreRadius = 0;
        if (state.getBlock() instanceof SurfaceRootBlock root) {
            coreRadius = root.getRadius(state);
        }
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

        //The source direction is the biggest connection from one of the horizontal directions
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
                    if (isGrounded && sleeves[idx][connRadius - 1] != null) {
                        parts.add(sleeves[idx][connRadius - 1]);
                    }
                    if (connectionLevels[idx] == RootConnections.ConnectionLevel.HIGH && verts[idx][connRadius - 1] != null) {
                        parts.add(verts[idx][connRadius - 1]);
                    }
                }
            }
        }
    }

    /**
     * Converts direction DUNSWE to 2 axis numbers for Z,X
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
            sourceDir = null; //Has no source node
        }
        return sourceDir;
    }

    ///////////////////////////////////////////
    // VANILLA MODEL METHODS
    ///////////////////////////////////////////

    @Override
    public Material.Baked particleMaterial() {
        return particleMaterial;
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return this.cores[0][0].materialFlags();
    }
}
