package com.dtteam.dynamictrees.model.baked;

import com.dtteam.dynamictrees.block.branch.BranchBlock;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class BasicBranchBlockBakedModel extends DynamicTreesBlockStateModel {

    protected final ModelBaker baker;
    protected final Material.Baked bark;
    protected final Material.Baked rings;

    protected final List<BakedQuad>[][] sleeves = new List[6][7];
    protected final List<BakedQuad>[][] cores = new List[3][8];
    protected final List<BakedQuad>[] ringsQuads = new List[8];

    public BasicBranchBlockBakedModel(TextureAtlasSprite barkTexture, TextureAtlasSprite ringsTexture) {
        this(null, new Material.Baked(barkTexture, false), new Material.Baked(ringsTexture, false));
    }

    public BasicBranchBlockBakedModel(ModelBaker baker, Material.Baked bark, Material.Baked rings) {
        super(bark);
        this.baker = baker;
        this.bark = bark;
        this.rings = rings;
        if (baker != null) {
            initModels();
        }
    }

    protected void initModels() {
        for (int i = 0; i < 8; i++) {
            int radius = i + 1;
            if (radius < 8) {
                for (Direction dir : Direction.values()) {
                    sleeves[dir.get3DDataValue()][i] = bakeSleeve(radius, dir, bark);
                }
            }
            cores[0][i] = bakeCore(radius, Direction.Axis.Y, bark);
            cores[1][i] = bakeCore(radius, Direction.Axis.Z, bark);
            cores[2][i] = bakeCore(radius, Direction.Axis.X, bark);
            ringsQuads[i] = bakeCore(radius, Direction.Axis.Y, rings);
        }
    }

    protected List<BakedQuad> bakeSleeve(int radius, Direction dir, Material.Baked barkMaterial) {
        int dradius = radius * 2;
        int halfSize = (16 - dradius) / 2;
        int halfSizeX = dir.getStepX() != 0 ? halfSize : dradius;
        int halfSizeY = dir.getStepY() != 0 ? halfSize : dradius;
        int halfSizeZ = dir.getStepZ() != 0 ? halfSize : dradius;
        int move = 16 - halfSize;
        int centerX = 16 + (dir.getStepX() * move);
        int centerY = 16 + (dir.getStepY() * move);
        int centerZ = 16 + (dir.getStepZ() * move);

        Vector3f from = new Vector3f((centerX - halfSizeX) / 2f, (centerY - halfSizeY) / 2f, (centerZ - halfSizeZ) / 2f);
        Vector3f to = new Vector3f((centerX + halfSizeX) / 2f, (centerY + halfSizeY) / 2f, (centerZ + halfSizeZ) / 2f);

        boolean negative = dir.getAxisDirection() == Direction.AxisDirection.NEGATIVE;
        if (dir.getAxis() == Direction.Axis.Z) {
            negative = !negative;
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
            int rotation;
            if (dir == face) {
                if (radius != 1) {
                    continue;
                }
                minU = 8 - radius;
                minV = 8 - radius;
                maxU = 8 + radius;
                maxV = 8 + radius;
                rotation = 0;
            } else {
                minU = 8 - radius;
                minV = negative ? 16 - halfSize : 0;
                maxU = 8 + radius;
                maxV = negative ? 16 : halfSize;
                rotation = CuboidQuadBaker.faceAngle(dir.getAxis(), face);
            }
            quads.add(CuboidQuadBaker.bake(baker, from, to, face, minU, minV, maxU, maxV, rotation, barkMaterial));
        }
        return quads;
    }

    protected List<BakedQuad> bakeCore(int radius, Direction.Axis axis, Material.Baked icon) {
        Vector3f from = new Vector3f(8 - radius, 8 - radius, 8 - radius);
        Vector3f to = new Vector3f(8 + radius, 8 + radius, 8 + radius);
        List<BakedQuad> quads = CuboidQuadBaker.newList();
        for (Direction face : Direction.values()) {
            int rotation = CuboidQuadBaker.faceAngle(axis, face);
            quads.add(CuboidQuadBaker.bake(baker, from, to, face, 8 - radius, 8 - radius, 8 + radius, 8 + radius, rotation, icon));
        }
        return quads;
    }

    @Override
    public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        if (!(state.getBlock() instanceof BranchBlock branch)) {
            return null;
        }
        int[] radii = branch.getConnectionData(level, pos, state).getAllRadii();
        return new BranchGeometryKey(getRadius(state), radii[0], radii[1], radii[2], radii[3], radii[4], radii[5]);
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state,
                          RandomSource random, Predicate<Direction> cullTest) {
        int[] connections = new int[]{0, 0, 0, 0, 0, 0};
        if (state.getBlock() instanceof BranchBlock branchBlock) {
            connections = branchBlock.getConnectionData(level, pos, state).getAllRadii();
        }
        for (BakedQuad quad : collectQuads(state, connections, null)) {
            if (cullTest.test(quad.direction())) {
                continue;
            }
            emitter.fromBakedQuad(quad);
            emitter.cullFace(quad.direction());
            emitter.emit();
        }
    }

    public List<BakedQuad> collectQuads(BlockState state, int[] connections, @Nullable Direction forceRingDir) {
        List<BakedQuad> out = new ArrayList<>();
        if (baker == null || !(state.getBlock() instanceof BranchBlock branchBlock)) {
            return out;
        }
        final int coreRadius = getRadius(state);
        if (coreRadius > BranchBlock.MAX_RADIUS || coreRadius < 1) {
            return out;
        }
        int twigRadius = Math.max(1, branchBlock.getFamily().getPrimaryThickness());
        int numConnections = 0;
        for (int radius : connections) {
            if (radius != 0) {
                numConnections++;
            }
        }
        if (numConnections == 0 && forceRingDir != null) {
            addFace(out, ringsQuads[coreRadius - 1], forceRingDir);
            return out;
        }

        Direction sourceDir = getSourceDir(coreRadius, connections);
        int coreDir = resolveCoreDir(sourceDir);
        Direction coreRingDir = (numConnections == 1 && sourceDir != null) ? sourceDir.getOpposite() : null;

        for (Direction face : Direction.values()) {
            if (coreRadius != connections[face.get3DDataValue()]) {
                List<BakedQuad> coreQuads = (coreRingDir != null && coreRingDir == face)
                        ? ringsQuads[coreRadius - 1]
                        : cores[coreDir][coreRadius - 1];
                addFace(out, coreQuads, face);
            }
            if (coreRadius != 8) {
                for (Direction connDir : Direction.values()) {
                    int idx = connDir.get3DDataValue();
                    int connRadius = connections[idx];
                    if (connRadius > 0 && connRadius < 8 && (connRadius <= twigRadius || face != connDir)) {
                        addFace(out, sleeves[idx][connRadius - 1], face);
                    }
                }
            }
        }
        return out;
    }

    protected static void addFace(List<BakedQuad> out, List<BakedQuad> src, Direction face) {
        if (src == null) {
            return;
        }
        for (BakedQuad quad : src) {
            if (quad.direction() == face) {
                out.add(quad);
            }
        }
    }

    protected Direction getSourceDir(int coreRadius, int[] connections) {
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
            return null;
        }
        return sourceDir;
    }

    protected int resolveCoreDir(@Nullable Direction dir) {
        if (dir == null) {
            return 0;
        }
        return dir.get3DDataValue() >> 1;
    }

    protected int getRadius(BlockState blockState) {
        return ((BranchBlock) blockState.getBlock()).getRadius(blockState);
    }

    public record BranchGeometryKey(int radius, int down, int up, int north, int south, int west, int east) {}
}
