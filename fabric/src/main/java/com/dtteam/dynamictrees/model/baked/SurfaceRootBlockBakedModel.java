package com.dtteam.dynamictrees.model.baked;

import com.dtteam.dynamictrees.api.network.RootConnections;
import com.dtteam.dynamictrees.block.branch.SurfaceRootBlock;
import com.dtteam.dynamictrees.utility.CoordUtils;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Predicate;

public class SurfaceRootBlockBakedModel extends DynamicTreesBlockStateModel {

    private final ModelBaker baker;
    private final Material.Baked bark;
    private final List<BakedQuad>[][] sleeves = new List[4][7];
    private final List<BakedQuad>[][] cores = new List[2][8];
    private final List<BakedQuad>[][] verts = new List[4][8];

    public SurfaceRootBlockBakedModel(TextureAtlasSprite barkTexture) {
        this(null, new Material.Baked(barkTexture, false));
    }

    public SurfaceRootBlockBakedModel(ModelBaker baker, Material.Baked bark) {
        super(bark);
        this.baker = baker;
        this.bark = bark;
        if (baker != null) {
            initModels();
        }
    }

    private void initModels() {
        for (int r = 0; r < 8; r++) {
            int radius = r + 1;
            if (radius < 8) {
                for (Direction dir : CoordUtils.HORIZONTALS) {
                    int horIndex = dir.get2DDataValue();
                    sleeves[horIndex][r] = bakeSleeve(radius, dir);
                    verts[horIndex][r] = bakeVert(radius, dir);
                }
            }
            cores[0][r] = bakeCore(radius, Direction.Axis.Z);
            cores[1][r] = bakeCore(radius, Direction.Axis.X);
        }
    }

    private int getRadialHeight(int radius) {
        return radius * 2;
    }

    private List<BakedQuad> bakeSleeve(int radius, Direction dir) {
        int radialHeight = getRadialHeight(radius);
        int dradius = radius * 2;
        int halfSize = (16 - dradius) / 2;
        int halfSizeX = dir.getStepX() != 0 ? halfSize : dradius;
        int halfSizeZ = dir.getStepZ() != 0 ? halfSize : dradius;
        int move = 16 - halfSize;
        int centerX = 16 + (dir.getStepX() * move);
        int centerZ = 16 + (dir.getStepZ() * move);

        Vector3f from = new Vector3f((centerX - halfSizeX) / 2f, 0, (centerZ - halfSizeZ) / 2f);
        Vector3f to = new Vector3f((centerX + halfSizeX) / 2f, radialHeight, (centerZ + halfSizeZ) / 2f);

        boolean sleeveNegative = dir.getAxisDirection() == Direction.AxisDirection.NEGATIVE;
        if (dir.getAxis() == Direction.Axis.Z) {
            sleeveNegative = !sleeveNegative;
        }

        List<BakedQuad> quads = CuboidQuadBaker.newList();
        for (Direction face : Direction.values()) {
            if (dir.getOpposite() == face) {
                continue;
            }
            float minU;
            float minV;
            float maxU;
            float maxV;
            if (face.getAxis().isHorizontal()) {
                boolean facePositive = face.getAxisDirection() == Direction.AxisDirection.POSITIVE;
                minU = facePositive ? 16 - radialHeight : 0;
                minV = sleeveNegative ? 16 - halfSize : 0;
                maxU = facePositive ? 16 : radialHeight;
                maxV = sleeveNegative ? 16 : halfSize;
            } else {
                minU = 8 - radius;
                minV = sleeveNegative ? 16 - halfSize : 0;
                maxU = 8 + radius;
                maxV = sleeveNegative ? 16 : halfSize;
            }
            quads.add(CuboidQuadBaker.bake(baker, from, to, face, minU, minV, maxU, maxV, CuboidQuadBaker.faceAngle(dir.getAxis(), face), bark));
        }
        return quads;
    }

    private List<BakedQuad> bakeVert(int radius, Direction dir) {
        int radialHeight = getRadialHeight(radius);
        Vector3f from = new Vector3f(
                8 - radius + dir.getStepX() * 7,
                radialHeight,
                8 - radius + dir.getStepZ() * 7
        );
        Vector3f to = new Vector3f(
                8 + radius + dir.getStepX() * 7,
                Math.min(16, 16 + radialHeight),
                8 + radius + dir.getStepZ() * 7
        );
        List<BakedQuad> quads = CuboidQuadBaker.newList();
        for (Direction face : Direction.values()) {
            quads.add(CuboidQuadBaker.bake(baker, from, to, face, 8 - radius, 8 - radius, 8 + radius, 8 + radius, CuboidQuadBaker.faceAngle(Direction.Axis.Y, face), bark));
        }
        return quads;
    }

    private List<BakedQuad> bakeCore(int radius, Direction.Axis axis) {
        int radialHeight = getRadialHeight(radius);
        Vector3f from = new Vector3f(8 - radius, 0, 8 - radius);
        Vector3f to = new Vector3f(8 + radius, radialHeight, 8 + radius);
        List<BakedQuad> quads = CuboidQuadBaker.newList();
        for (Direction face : Direction.values()) {
            float minU;
            float minV;
            float maxU;
            float maxV;
            if (face.getAxis().isHorizontal()) {
                boolean positive = face.getAxisDirection() == Direction.AxisDirection.POSITIVE;
                minU = positive ? 16 - radialHeight : 0;
                minV = 8 - radius;
                maxU = positive ? 16 : radialHeight;
                maxV = 8 + radius;
            } else {
                minU = 8 - radius;
                minV = 8 - radius;
                maxU = 8 + radius;
                maxV = 8 + radius;
            }
            quads.add(CuboidQuadBaker.bake(baker, from, to, face, minU, minV, maxU, maxV, CuboidQuadBaker.faceAngle(axis, face), bark));
        }
        return quads;
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state,
                          RandomSource random, Predicate<Direction> cullTest) {
        if (baker == null || state == null) {
            return;
        }
        int coreRadius = getRadius(state);
        RootConnections connectionData = state.getBlock() instanceof SurfaceRootBlock surfaceRoot
                ? surfaceRoot.getConnectionData(level, pos)
                : new RootConnections();
        int[] connections = connectionData.getAllRadii();
        RootConnections.ConnectionLevel[] connectionLevels = connectionData.getConnectionLevels();
        for (int i = 0; i < connections.length; i++) {
            connections[i] = Mth.clamp(connections[i], 0, coreRadius);
        }

        Direction sourceDir = getSourceDir(coreRadius, connections);
        if (sourceDir == null) {
            sourceDir = Direction.DOWN;
        }
        int coreDir = resolveCoreDir(sourceDir);
        boolean isGrounded = state.getValue(SurfaceRootBlock.GROUNDED);

        for (Direction face : Direction.values()) {
            if (isGrounded) {
                CuboidQuadBaker.emit(emitter, cores[coreDir][coreRadius - 1], face, cullTest);
            }
            if (coreRadius != 8) {
                for (Direction connDir : CoordUtils.HORIZONTALS) {
                    int idx = connDir.get2DDataValue();
                    int connRadius = connections[idx];
                    if (connRadius > 0 && connRadius < 8) {
                        if (isGrounded) {
                            CuboidQuadBaker.emit(emitter, sleeves[idx][connRadius - 1], face, cullTest);
                        }
                        if (connectionLevels[idx] == RootConnections.ConnectionLevel.HIGH) {
                            CuboidQuadBaker.emit(emitter, verts[idx][connRadius - 1], face, cullTest);
                        }
                    }
                }
            }
        }
    }

    public List<BakedQuad> collectBreakingQuads(BlockState state) {
        int coreRadius = getRadius(state);
        if (baker == null || coreRadius < 1 || coreRadius > 8) {
            return List.of();
        }
        List<BakedQuad> core = cores[0][coreRadius - 1];
        return core != null ? core : List.of();
    }

    protected Direction getSourceDir(int coreRadius, int[] connections) {
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
            return null;
        }
        return sourceDir;
    }

    protected int resolveCoreDir(Direction dir) {
        return dir.getAxis() == Direction.Axis.X ? 1 : 0;
    }

    protected int getRadius(BlockState blockState) {
        return ((SurfaceRootBlock) blockState.getBlock()).getRadius(blockState);
    }
}
