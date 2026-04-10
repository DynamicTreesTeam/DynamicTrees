package com.dtteam.dynamictrees.model.baked;

import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.ThickBranchBlock;
import com.dtteam.dynamictrees.utility.CoordUtils;
import com.dtteam.dynamictrees.utility.CoordUtils.Surround;
import com.google.common.collect.Maps;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class ThickBranchBlockBakedModel extends BasicBranchBlockBakedModel {

    private final TextureAtlasSprite thickRingsTexture;
    private final List<BakedQuad>[] trunksBarkQuads = new List[16];
    private final List<BakedQuad>[] trunksTopBarkQuads = new List[16];
    private final List<BakedQuad>[] trunksTopRingsQuads = new List[16];
    private final List<BakedQuad>[] trunksBotRingsQuads = new List[16];

    public ThickBranchBlockBakedModel(TextureAtlasSprite barkTexture, TextureAtlasSprite ringsTexture, TextureAtlasSprite thickRingsTexture) {
        super(barkTexture, ringsTexture);
        this.thickRingsTexture = thickRingsTexture;
        initThickModels();
    }

    private void initThickModels() {
        for (int i = 0; i < ThickBranchBlock.MAX_RADIUS_THICK - ThickBranchBlock.MAX_RADIUS; i++) {
            int radius = i + ThickBranchBlock.MAX_RADIUS + 1;
            trunksBarkQuads[i] = bakeTrunkBark(radius, this.barkTexture, true);
            trunksTopBarkQuads[i] = bakeTrunkBark(radius, this.barkTexture, false);
            trunksTopRingsQuads[i] = bakeTrunkRings(radius, thickRingsTexture, Direction.UP);
            trunksBotRingsQuads[i] = bakeTrunkRings(radius, thickRingsTexture, Direction.DOWN);
        }
    }

    public List<BakedQuad> bakeTrunkBark(int radius, TextureAtlasSprite bark, boolean side) {
        List<BakedQuad> quads = new ArrayList<>();
        FaceBakery faceBakery = new FaceBakery();
        AABB wholeVolume = new AABB(8 - radius, 0, 8 - radius, 8 + radius, 16, 8 + radius);

        final Direction[] run = side ? CoordUtils.HORIZONTALS : new Direction[]{Direction.UP, Direction.DOWN};
        ArrayList<Vec3i> offsets = new ArrayList<>();

        for (Surround dir : Surround.values()) {
            offsets.add(dir.getOffset());
        }
        offsets.add(new Vec3i(0, 0, 0));

        for (Direction face : run) {
            final Vec3i dirVector = face.getNormal();

            for (Vec3i offset : offsets) {
                if (face.getAxis() == Axis.Y || new Vec3(dirVector.getX(), dirVector.getY(), dirVector.getZ()).add(new Vec3(offset.getX(), offset.getY(), offset.getZ())).lengthSqr() > 2.25) {
                    Vec3 scaledOffset = new Vec3(offset.getX() * 16, offset.getY() * 16, offset.getZ() * 16);
                    AABB partBoundary = new AABB(0, 0, 0, 16, 16, 16).move(scaledOffset).intersect(wholeVolume);

                    Vector3f[] limits = aabbLimits(partBoundary);

                    Map<Direction, BlockElementFace> mapFacesIn = Maps.newEnumMap(Direction.class);

                    BlockFaceUV uvface = new BlockFaceUV(modUV(getUVs(partBoundary, face)), getFaceAngle(Axis.Y, face));
                    mapFacesIn.put(face, new BlockElementFace(null, -1, null, uvface));

                    BlockElement part = new BlockElement(limits[0], limits[1], mapFacesIn, null, true);
                    quads.add(faceBakery.bakeQuad(part.from, part.to, part.faces.get(face), bark, face, BlockModelRotation.X0_Y0, part.rotation, true));
                }
            }
        }

        return quads;
    }

    public List<BakedQuad> bakeTrunkRings(int radius, TextureAtlasSprite ring, Direction face) {
        List<BakedQuad> quads = new ArrayList<>();
        FaceBakery faceBakery = new FaceBakery();
        AABB wholeVolume = new AABB(8 - radius, 0, 8 - radius, 8 + radius, 16, 8 + radius);
        int wholeVolumeWidth = 48;

        ArrayList<Vec3i> offsets = new ArrayList<>();

        for (Surround dir : Surround.values()) {
            offsets.add(dir.getOffset());
        }
        offsets.add(new Vec3i(0, 0, 0));

        for (Vec3i offset : offsets) {
            Vec3 scaledOffset = new Vec3(offset.getX() * 16, offset.getY() * 16, offset.getZ() * 16);
            AABB partBoundary = new AABB(0, 0, 0, 16, 16, 16).move(scaledOffset).intersect(wholeVolume);

            Vector3f posFrom = new Vector3f((float) partBoundary.minX, (float) partBoundary.minY, (float) partBoundary.minZ);
            Vector3f posTo = new Vector3f((float) partBoundary.maxX, (float) partBoundary.maxY, (float) partBoundary.maxZ);

            Map<Direction, BlockElementFace> mapFacesIn = Maps.newEnumMap(Direction.class);
            float[] uvs = getRingsUvs(face, partBoundary, wholeVolumeWidth);

            BlockFaceUV uvFace = new BlockFaceUV(uvs, getFaceAngle(Axis.Y, face));
            mapFacesIn.put(face, new BlockElementFace(null, -1, null, uvFace));

            BlockElement part = new BlockElement(posFrom, posTo, mapFacesIn, null, true);
            quads.add(faceBakery.bakeQuad(part.from, part.to, part.faces.get(face), ring, face, BlockModelRotation.X0_Y0, part.rotation, true));
        }

        return quads;
    }

    private static float[] getRingsUvs(Direction face, AABB partBoundary, int wholeVolumeWidth) {
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

    public static float[] getUVs(AABB box, Direction face) {
        return switch (face) {
            case UP -> new float[]{(float) box.minX, (float) box.minZ, (float) box.maxX, (float) box.maxZ};
            case NORTH -> new float[]{16f - (float) box.maxX, (float) box.minY, 16f - (float) box.minX, (float) box.maxY};
            case SOUTH -> new float[]{(float) box.minX, (float) box.minY, (float) box.maxX, (float) box.maxY};
            case WEST -> new float[]{(float) box.minZ, (float) box.minY, (float) box.maxZ, (float) box.maxY};
            case EAST -> new float[]{16f - (float) box.maxZ, (float) box.minY, 16f - (float) box.minZ, (float) box.maxY};
            default -> new float[]{(float) box.minX, 16f - (float) box.minZ, (float) box.maxX, 16f - (float) box.maxZ};
        };
    }

    public static float[] modUV(float[] uvs) {
        uvs[0] = (int) uvs[0] & 0xf;
        uvs[1] = (int) uvs[1] & 0xf;
        uvs[2] = (((int) uvs[2] - 1) & 0xf) + 1;
        uvs[3] = (((int) uvs[3] - 1) & 0xf) + 1;
        return uvs;
    }

    public static Vector3f[] aabbLimits(AABB aabb) {
        return new Vector3f[]{
                new Vector3f((float) aabb.minX, (float) aabb.minY, (float) aabb.minZ),
                new Vector3f((float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ),
        };
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        if (state == null) return;

        int coreRadius = getRadius(state);

        if (coreRadius <= BranchBlock.MAX_RADIUS) {
            super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
            return;
        }

        coreRadius = Mth.clamp(coreRadius, 9, 24);

        int[] connections = new int[]{0, 0, 0, 0, 0, 0};
        int twigRadius = 1;

        if (state.getBlock() instanceof BranchBlock branchBlock) {
            connections = branchBlock.getConnectionData(blockView, pos, state).getAllRadii();
            twigRadius = branchBlock.getFamily().getPrimaryThickness();
        }

        var emitter = context.getEmitter();

        boolean branchesAround = connections[2] + connections[3] + connections[4] + connections[5] != 0;

        int radiusIndex = coreRadius - 9;
        if (radiusIndex < 0 || radiusIndex >= trunksBarkQuads.length) return;

        var renderer = RendererAccess.INSTANCE.getRenderer();
        MaterialFinder finder = renderer.materialFinder();
        finder.disableAo(0, true);
        finder.disableDiffuse(0, true);
        RenderMaterial material = finder.find();

        for (Direction face : Direction.values()) {
            List<BakedQuad> barkQuads = trunksBarkQuads[radiusIndex];
            if (barkQuads != null) {
                for (BakedQuad quad : barkQuads) {
                    if (quad.getDirection() == face) {
                        emitter.fromVanilla(quad, material, face);
                        emitter.emit();
                    }
                }
            }

            if (face == Direction.UP || face == Direction.DOWN) {
                if (connections[face.get3DDataValue()] < twigRadius && !branchesAround) {
                    List<BakedQuad> ringQuads = trunksTopRingsQuads[radiusIndex];
                    if (ringQuads != null) {
                        for (BakedQuad quad : ringQuads) {
                            if (quad.getDirection() == face) {
                                emitter.fromVanilla(quad, material, face);
                                emitter.emit();
                            }
                        }
                    }
                } else if (connections[face.get3DDataValue()] < coreRadius) {
                    List<BakedQuad> topBarkQuads = trunksTopBarkQuads[radiusIndex];
                    if (topBarkQuads != null) {
                        for (BakedQuad quad : topBarkQuads) {
                            if (quad.getDirection() == face) {
                                emitter.fromVanilla(quad, material, face);
                                emitter.emit();
                            }
                        }
                    }
                }
            }
        }
    }
}
