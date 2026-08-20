package com.dtteam.dynamictrees.model.baked;

import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.ThickBranchBlock;
import com.dtteam.dynamictrees.utility.CoordUtils;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ThickBranchBlockBakedModel extends BasicBranchBlockBakedModel {

    private final Material.Baked thickRings;
    private final List<BakedQuad>[] trunksBark = new List[16];
    private final List<BakedQuad>[] trunksTopBark = new List[16];
    private final List<BakedQuad>[] trunksTopRings = new List[16];
    private final List<BakedQuad>[] trunksBotRings = new List[16];

    public ThickBranchBlockBakedModel(TextureAtlasSprite barkTexture, TextureAtlasSprite ringsTexture, TextureAtlasSprite thickRingsTexture) {
        this(null, new Material.Baked(barkTexture, false), new Material.Baked(ringsTexture, false), new Material.Baked(thickRingsTexture, false));
    }

    public ThickBranchBlockBakedModel(ModelBaker baker, Material.Baked bark, Material.Baked rings, Material.Baked thickRings) {
        super(baker, bark, rings);
        this.thickRings = thickRings;
        if (baker != null) {
            initThickModels();
        }
    }

    private void initThickModels() {
        for (int i = 0; i < ThickBranchBlock.MAX_RADIUS_THICK - BranchBlock.MAX_RADIUS; i++) {
            int radius = i + BranchBlock.MAX_RADIUS + 1;
            trunksBark[i] = bakeTrunk(radius, bark, true);
            trunksTopBark[i] = bakeTrunk(radius, bark, false);
            trunksTopRings[i] = bakeTrunkRings(radius, thickRings, Direction.UP);
            trunksBotRings[i] = bakeTrunkRings(radius, thickRings, Direction.DOWN);
        }
    }

    private List<Vec3i> trunkOffsets() {
        List<Vec3i> offsets = new ArrayList<>();
        for (CoordUtils.Surround dir : CoordUtils.Surround.values()) {
            offsets.add(dir.getOffset());
        }
        offsets.add(new Vec3i(0, 0, 0));
        return offsets;
    }

    private List<BakedQuad> bakeTrunk(int radius, Material.Baked material, boolean side) {
        AABB wholeVolume = new AABB(8 - radius, 0, 8 - radius, 8 + radius, 16, 8 + radius);
        List<BakedQuad> quads = CuboidQuadBaker.newList();
        Direction[] faces = side ? CoordUtils.HORIZONTALS : new Direction[]{Direction.UP, Direction.DOWN};
        for (Direction face : faces) {
            Vec3i dirVector = face.getUnitVec3i();
            for (Vec3i offset : trunkOffsets()) {
                if (face.getAxis() != Direction.Axis.Y
                        && new Vec3(dirVector.getX(), dirVector.getY(), dirVector.getZ())
                        .add(offset.getX(), offset.getY(), offset.getZ()).lengthSqr() <= 2.25) {
                    continue;
                }
                Vec3 scaledOffset = new Vec3(offset.getX() * 16, offset.getY() * 16, offset.getZ() * 16);
                AABB partBoundary = new AABB(0, 0, 0, 16, 16, 16).move(scaledOffset).intersect(wholeVolume);
                if (partBoundary.getXsize() <= 0 || partBoundary.getYsize() <= 0 || partBoundary.getZsize() <= 0) {
                    continue;
                }
                Vector3f from = new Vector3f((float) partBoundary.minX, (float) partBoundary.minY, (float) partBoundary.minZ);
                Vector3f to = new Vector3f((float) partBoundary.maxX, (float) partBoundary.maxY, (float) partBoundary.maxZ);
                float[] uvs = faceUvs(face, partBoundary);
                quads.add(CuboidQuadBaker.bake(baker, from, to, face, uvs[0], uvs[1], uvs[2], uvs[3],
                        CuboidQuadBaker.faceAngle(Direction.Axis.Y, face), material));
            }
        }
        return quads;
    }

    private List<BakedQuad> bakeTrunkRings(int radius, Material.Baked material, Direction face) {
        AABB wholeVolume = new AABB(8 - radius, 0, 8 - radius, 8 + radius, 16, 8 + radius);
        List<BakedQuad> quads = CuboidQuadBaker.newList();
        int wholeVolumeWidth = 48;
        for (Vec3i offset : trunkOffsets()) {
            Vec3 scaledOffset = new Vec3(offset.getX() * 16, offset.getY() * 16, offset.getZ() * 16);
            AABB partBoundary = new AABB(0, 0, 0, 16, 16, 16).move(scaledOffset).intersect(wholeVolume);
            if (partBoundary.getXsize() <= 0 || partBoundary.getYsize() <= 0 || partBoundary.getZsize() <= 0) {
                continue;
            }
            Vector3f from = new Vector3f((float) partBoundary.minX, (float) partBoundary.minY, (float) partBoundary.minZ);
            Vector3f to = new Vector3f((float) partBoundary.maxX, (float) partBoundary.maxY, (float) partBoundary.maxZ);
            float[] uvs = ringUvs(face, partBoundary, wholeVolumeWidth);
            quads.add(CuboidQuadBaker.bake(baker, from, to, face, uvs[0], uvs[1], uvs[2], uvs[3], 0, material));
        }
        return quads;
    }

    private static float[] faceUvs(Direction face, AABB partBoundary) {
        return switch (face.getAxis()) {
            case Y -> new float[]{(float) partBoundary.minX, (float) partBoundary.minZ, (float) partBoundary.maxX, (float) partBoundary.maxZ};
            case Z -> new float[]{(float) partBoundary.minX, (float) partBoundary.minY, (float) partBoundary.maxX, (float) partBoundary.maxY};
            case X -> new float[]{(float) partBoundary.minZ, (float) partBoundary.minY, (float) partBoundary.maxZ, (float) partBoundary.maxY};
        };
    }

    private static float[] ringUvs(Direction face, AABB partBoundary, int wholeVolumeWidth) {
        float textureOffsetX = -16f;
        float textureOffsetZ = -16f;
        float minX = ((float) ((partBoundary.minX - textureOffsetX) / wholeVolumeWidth)) * 16f;
        float maxX = ((float) ((partBoundary.maxX - textureOffsetX) / wholeVolumeWidth)) * 16f;
        float minZ = ((float) ((partBoundary.minZ - textureOffsetZ) / wholeVolumeWidth)) * 16f;
        float maxZ = ((float) ((partBoundary.maxZ - textureOffsetZ) / wholeVolumeWidth)) * 16f;
        if (face == Direction.DOWN) {
            minZ = ((float) ((partBoundary.maxZ - textureOffsetZ) / wholeVolumeWidth)) * 16f;
            maxZ = ((float) ((partBoundary.minZ - textureOffsetZ) / wholeVolumeWidth)) * 16f;
        }
        return new float[]{minX, minZ, maxX, maxZ};
    }

    @Override
    public List<BakedQuad> collectQuads(BlockState state, int[] connections, Direction forceRingDir) {
        int coreRadius = getRadius(state);
        if (coreRadius <= BranchBlock.MAX_RADIUS) {
            return super.collectQuads(state, connections, forceRingDir);
        }
        if (baker == null) {
            return List.of();
        }
        coreRadius = Mth.clamp(coreRadius, 9, ThickBranchBlock.MAX_RADIUS_THICK);
        int index = coreRadius - 9;
        int numConnections = 0;
        for (int radius : connections) {
            if (radius != 0) {
                numConnections++;
            }
        }
        if (numConnections == 0 && forceRingDir != null) {
            return List.of();
        }
        int[] conn = connections.clone();
        int twigRadius = 1;
        if (state.getBlock() instanceof BranchBlock branchBlock) {
            twigRadius = Math.max(1, branchBlock.getFamily().getPrimaryThickness());
        }
        List<BakedQuad> out = new ArrayList<>();
        if (forceRingDir != null) {
            conn[forceRingDir.get3DDataValue()] = 0;
            addFace(out, trunksBotRings[index], forceRingDir);
        }
        boolean branchesAround = conn[2] + conn[3] + conn[4] + conn[5] != 0;
        for (Direction face : Direction.values()) {
            addFace(out, trunksBark[index], face);
            if (face == Direction.UP || face == Direction.DOWN) {
                if (conn[face.get3DDataValue()] < twigRadius && !branchesAround) {
                    addFace(out, trunksTopRings[index], face);
                } else if (conn[face.get3DDataValue()] < coreRadius) {
                    addFace(out, trunksTopBark[index], face);
                }
            }
        }
        return out;
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
}
