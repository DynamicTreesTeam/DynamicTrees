package com.dtteam.dynamictrees.model.baked;

import com.dtteam.dynamictrees.api.network.RootConnections;
import com.dtteam.dynamictrees.block.branch.SurfaceRootBlock;
import com.dtteam.dynamictrees.utility.CoordUtils;
import com.google.common.collect.Maps;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class SurfaceRootBlockBakedModel implements BakedModel, FabricBakedModel {

    protected final TextureAtlasSprite barkTexture;

    public final List<BakedQuad>[][] sleevesQuads = new List[4][7];
    public final List<BakedQuad>[][] coresQuads = new List[2][8];
    public final List<BakedQuad>[][] vertsQuads = new List[4][8];

    public SurfaceRootBlockBakedModel(TextureAtlasSprite barkTexture) {
        this.barkTexture = barkTexture;
        initModels();
    }

    private void initModels() {
        for (int r = 0; r < 8; r++) {
            int radius = r + 1;
            if (radius < 8) {
                for (Direction dir : CoordUtils.HORIZONTALS) {
                    int horIndex = dir.get2DDataValue();
                    sleevesQuads[horIndex][r] = bakeSleeve(radius, dir);
                    vertsQuads[horIndex][r] = bakeVert(radius, dir);
                }
            }
            coresQuads[0][r] = bakeCore(radius, Direction.Axis.Z);
            coresQuads[1][r] = bakeCore(radius, Direction.Axis.X);
        }
    }

    public int getRadialHeight(int radius) {
        return radius * 2;
    }

    public List<BakedQuad> bakeSleeve(int radius, Direction dir) {
        int radialHeight = getRadialHeight(radius);

        int dradius = radius * 2;
        int halfSize = (16 - dradius) / 2;
        int halfSizeX = dir.getStepX() != 0 ? halfSize : dradius;
        int halfSizeZ = dir.getStepZ() != 0 ? halfSize : dradius;
        int move = 16 - halfSize;
        int centerX = 16 + (dir.getStepX() * move);
        int centerZ = 16 + (dir.getStepZ() * move);

        Vector3f posFrom = new Vector3f((centerX - halfSizeX) / 2f, 0, (centerZ - halfSizeZ) / 2f);
        Vector3f posTo = new Vector3f((centerX + halfSizeX) / 2f, radialHeight, (centerZ + halfSizeZ) / 2f);

        boolean sleeveNegative = dir.getAxisDirection() == Direction.AxisDirection.NEGATIVE;
        if (dir.getAxis() == Direction.Axis.Z) {
            sleeveNegative = !sleeveNegative;
        }

        Map<Direction, BlockElementFace> mapFacesIn = Maps.newEnumMap(Direction.class);

        for (Direction face : Direction.values()) {
            if (dir.getOpposite() != face) {
                BlockFaceUV uvface;
                if (face.getAxis().isHorizontal()) {
                    boolean facePositive = face.getAxisDirection() == Direction.AxisDirection.POSITIVE;
                    uvface = new BlockFaceUV(new float[]{facePositive ? 16 - radialHeight : 0, (sleeveNegative ? 16 - halfSize : 0), facePositive ? 16 : radialHeight, (sleeveNegative ? 16 : halfSize)}, getFaceAngle(dir.getAxis(), face));
                } else {
                    uvface = new BlockFaceUV(new float[]{8 - radius, sleeveNegative ? 16 - halfSize : 0, 8 + radius, sleeveNegative ? 16 : halfSize}, getFaceAngle(dir.getAxis(), face));
                }
                mapFacesIn.put(face, new BlockElementFace(null, -1, null, uvface));
            }
        }

        BlockElement part = new BlockElement(posFrom, posTo, mapFacesIn, null, true);
        List<BakedQuad> quads = new ArrayList<>();
        FaceBakery faceBakery = new FaceBakery();

        for (Map.Entry<Direction, BlockElementFace> e : part.faces.entrySet()) {
            Direction face = e.getKey();
            quads.add(faceBakery.bakeQuad(part.from, part.to, e.getValue(), barkTexture, face, BlockModelRotation.X0_Y0, part.rotation, true));
        }

        return quads;
    }

    private List<BakedQuad> bakeVert(int radius, Direction dir) {
        int radialHeight = getRadialHeight(radius);
        List<BakedQuad> quads = new ArrayList<>();
        FaceBakery faceBakery = new FaceBakery();

        AABB partBoundary = new AABB(8 - radius, radialHeight, 8 - radius, 8 + radius, 16 + radialHeight, 8 + radius)
                .move(dir.getStepX() * 7, 0, dir.getStepZ() * 7);

        for (int i = 0; i < 2; i++) {
            AABB pieceBoundary = partBoundary.intersect(new AABB(0, 0, 0, 16, 16, 16).move(0, 16 * i, 0));

            for (Direction face : Direction.values()) {
                Map<Direction, BlockElementFace> mapFacesIn = Maps.newEnumMap(Direction.class);

                BlockFaceUV uvface = new BlockFaceUV(modUV(getUVs(pieceBoundary, face)), getFaceAngle(Direction.Axis.Y, face));
                mapFacesIn.put(face, new BlockElementFace(null, -1, null, uvface));

                Vector3f[] limits = AABBLimits(pieceBoundary);

                BlockElement part = new BlockElement(limits[0], limits[1], mapFacesIn, null, true);
                quads.add(faceBakery.bakeQuad(part.from, part.to, part.faces.get(face), barkTexture, face, BlockModelRotation.X0_Y0, part.rotation, true));
            }
        }

        return quads;
    }

    public List<BakedQuad> bakeCore(int radius, Direction.Axis axis) {
        int radialHeight = getRadialHeight(radius);

        Vector3f posFrom = new Vector3f(8 - radius, 0, 8 - radius);
        Vector3f posTo = new Vector3f(8 + radius, radialHeight, 8 + radius);

        Map<Direction, BlockElementFace> mapFacesIn = Maps.newEnumMap(Direction.class);

        for (Direction face : Direction.values()) {
            BlockFaceUV uvface;
            if (face.getAxis().isHorizontal()) {
                boolean positive = face.getAxisDirection() == Direction.AxisDirection.POSITIVE;
                uvface = new BlockFaceUV(new float[]{positive ? 16 - radialHeight : 0, 8 - radius, positive ? 16 : radialHeight, 8 + radius}, getFaceAngle(axis, face));
            } else {
                uvface = new BlockFaceUV(new float[]{8 - radius, 8 - radius, 8 + radius, 8 + radius}, getFaceAngle(axis, face));
            }

            mapFacesIn.put(face, new BlockElementFace(null, -1, null, uvface));
        }

        BlockElement part = new BlockElement(posFrom, posTo, mapFacesIn, null, true);
        List<BakedQuad> quads = new ArrayList<>();
        FaceBakery faceBakery = new FaceBakery();

        for (Map.Entry<Direction, BlockElementFace> e : part.faces.entrySet()) {
            Direction face = e.getKey();
            quads.add(faceBakery.bakeQuad(part.from, part.to, e.getValue(), barkTexture, face, BlockModelRotation.X0_Y0, part.rotation, true));
        }

        return quads;
    }

    public int getFaceAngle(Direction.Axis axis, Direction face) {
        if (axis == Direction.Axis.Y) {
            return 0;
        } else if (axis == Direction.Axis.Z) {
            return switch (face) {
                case UP -> 0;
                case WEST, NORTH -> 270;
                case DOWN -> 180;
                default -> 90;
            };
        } else {
            return (face == Direction.NORTH) ? 270 : 90;
        }
    }

    public float[] getUVs(AABB box, Direction face) {
        return switch (face) {
            case UP -> new float[]{(float) box.minX, (float) box.minZ, (float) box.maxX, (float) box.maxZ};
            case NORTH -> new float[]{16f - (float) box.maxX, (float) box.minY, 16f - (float) box.minX, (float) box.maxY};
            case SOUTH -> new float[]{(float) box.minX, (float) box.minY, (float) box.maxX, (float) box.maxY};
            case WEST -> new float[]{(float) box.minZ, (float) box.minY, (float) box.maxZ, (float) box.maxY};
            case EAST -> new float[]{16f - (float) box.maxZ, (float) box.minY, 16f - (float) box.minZ, (float) box.maxY};
            default -> new float[]{(float) box.minX, 16f - (float) box.minZ, (float) box.maxX, 16f - (float) box.maxZ};
        };
    }

    public float[] modUV(float[] uvs) {
        uvs[0] = (int) uvs[0] & 0xf;
        uvs[1] = (int) uvs[1] & 0xf;
        uvs[2] = (((int) uvs[2] - 1) & 0xf) + 1;
        uvs[3] = (((int) uvs[3] - 1) & 0xf) + 1;
        return uvs;
    }

    public Vector3f[] AABBLimits(AABB aabb) {
        return new Vector3f[]{
                new Vector3f((float) aabb.minX, (float) aabb.minY, (float) aabb.minZ),
                new Vector3f((float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ),
        };
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        if (state == null) return;

        int coreRadius = getRadius(state);
        if (coreRadius <= 0 || coreRadius > 8) return;

        int[] connections = new int[]{0, 0, 0, 0};
        RootConnections.ConnectionLevel[] connectionLevels = RootConnections.PLACEHOLDER_CONNECTION_LEVELS.clone();

        if (state.getBlock() instanceof SurfaceRootBlock surfaceRootBlock) {
            RootConnections connectionData = surfaceRootBlock.getConnectionData(blockView, pos);
            connections = connectionData.getAllRadii();
            connectionLevels = connectionData.getConnectionLevels();
        }

        for (int i = 0; i < connections.length; i++) {
            connections[i] = Mth.clamp(connections[i], 0, coreRadius);
        }

        Direction sourceDir = getSourceDir(coreRadius, connections);
        if (sourceDir == null) {
            sourceDir = Direction.DOWN;
        }
        int coreDir = resolveCoreDir(sourceDir);

        boolean isGrounded = state.getValue(SurfaceRootBlock.GROUNDED);

        QuadEmitter emitter = context.getEmitter();

        for (Direction face : Direction.values()) {
            if (isGrounded) {
                List<BakedQuad> coreQuads = coresQuads[coreDir][coreRadius - 1];
                if (coreQuads != null) {
                    for (BakedQuad quad : coreQuads) {
                        if (quad.getDirection() == face) {
                            var renderer = RendererAccess.INSTANCE.getRenderer();
                            MaterialFinder finder = renderer.materialFinder();

                            finder.disableAo(0, true);
                            finder.disableDiffuse(0, true);

                            RenderMaterial material = finder.find();

                            emitter.fromVanilla(quad, material, null);
                            emitter.emit();
                        }
                    }
                }
            }

            if (coreRadius != 8) {
                for (Direction connDir : CoordUtils.HORIZONTALS) {
                    int idx = connDir.get2DDataValue();
                    int connRadius = connections[idx];
                    if (connRadius > 0) {
                        if (isGrounded && sleevesQuads[idx][connRadius - 1] != null) {
                            for (BakedQuad quad : sleevesQuads[idx][connRadius - 1]) {
                                if (quad.getDirection() == face) {
                                    var renderer = RendererAccess.INSTANCE.getRenderer();
                                    MaterialFinder finder = renderer.materialFinder();

                                    finder.disableAo(0, true);
                                    finder.disableDiffuse(0, true);

                                    RenderMaterial material = finder.find();

                                    emitter.fromVanilla(quad, material, null);
                                    emitter.emit();
                                }
                            }
                        }
                        if (connectionLevels[idx] == RootConnections.ConnectionLevel.HIGH && vertsQuads[idx][connRadius - 1] != null) {
                            for (BakedQuad quad : vertsQuads[idx][connRadius - 1]) {
                                if (quad.getDirection() == face) {
                                    var renderer = RendererAccess.INSTANCE.getRenderer();
                                    MaterialFinder finder = renderer.materialFinder();

                                    finder.disableAo(0, true);
                                    finder.disableDiffuse(0, true);

                                    RenderMaterial material = finder.find();

                                    emitter.fromVanilla(quad, material, null);
                                    emitter.emit();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
    }

    protected Direction getSourceDir(int coreRadius, int[] connections) {
        int largestConnection = 0;
        Direction sourceDir = null;

        for (Direction dir : CoordUtils.HORIZONTALS) {
            int horIndex = dir.get2DDataValue();
            int connRadius = connections[horIndex];
            if (connRadius > largestConnection) {
                largestConnection = connRadius;
                sourceDir = dir;
            }
        }

        if (largestConnection < coreRadius) {
            sourceDir = null;
        }
        return sourceDir;
    }

    protected int resolveCoreDir(Direction dir) {
        return dir.getAxis() == Direction.Axis.X ? 1 : 0;
    }

    protected int getRadius(BlockState blockState) {
        return ((SurfaceRootBlock) blockState.getBlock()).getRadius(blockState);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
        return Collections.emptyList();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return barkTexture;
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
