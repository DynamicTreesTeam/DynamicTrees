package com.ferreusveritas.dynamictrees.model.baked;

import com.ferreusveritas.dynamictrees.block.branch.SurfaceRootBlock;
import com.ferreusveritas.dynamictrees.client.ModelUtils;
import com.ferreusveritas.dynamictrees.model.data.RootModelConnections;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.RootConnections;
import com.google.common.collect.Maps;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@OnlyIn(Dist.CLIENT)
public class RootBlockBakedModel extends BranchBlockBakedModel {

    private TextureAtlasSprite barkTexture;

    private final BakedModel[][] sleeves = new BakedModel[4][7];
    private final BakedModel[][] cores = new BakedModel[2][8]; //8 Cores for 2 axis(X, Z) with the bark texture on all 6 sides rotated appropriately.
    private final BakedModel[][] verts = new BakedModel[4][8];

    public RootBlockBakedModel(ResourceLocation modelResLoc, ResourceLocation barkResLoc) {
        super(modelResLoc, barkResLoc, null);
    }

    @Override
    public void setupModels() {
        this.barkTexture = ModelUtils.getTexture(this.barkResLoc);

        for (int r = 0; r < 8; r++) {
            int radius = r + 1;
            if (radius < 8) {
                for (Direction dir : CoordUtils.HORIZONTALS) {
                    int horIndex = dir.get2DDataValue();
                    sleeves[horIndex][r] = bakeSleeve(radius, dir);
                    verts[horIndex][r] = bakeVert(radius, dir);
                }
            }
            cores[0][r] = bakeCore(radius, Direction.Axis.Z, this.barkTexture); //NORTH<->SOUTH
            cores[1][r] = bakeCore(radius, Direction.Axis.X, this.barkTexture); //WEST<->EAST
        }
    }

    public int getRadialHeight(int radius) {
        return radius * 2;
    }

    public BakedModel bakeSleeve(int radius, Direction dir) {
        int radialHeight = getRadialHeight(radius);

        //Work in double units(*2)
        int dradius = radius * 2;
        int halfSize = (16 - dradius) / 2;
        int halfSizeX = dir.getStepX() != 0 ? halfSize : dradius;
        int halfSizeZ = dir.getStepZ() != 0 ? halfSize : dradius;
        int move = 16 - halfSize;
        int centerX = 16 + (dir.getStepX() * move);
        int centerZ = 16 + (dir.getStepZ() * move);

        Vector3f posFrom = new Vector3f((centerX - halfSizeX) / 2, 0, (centerZ - halfSizeZ) / 2);
        Vector3f posTo = new Vector3f((centerX + halfSizeX) / 2, radialHeight, (centerZ + halfSizeZ) / 2);

        boolean sleeveNegative = dir.getAxisDirection() == Direction.AxisDirection.NEGATIVE;
        if (dir.getAxis() == Direction.Axis.Z) {// North/South
            sleeveNegative = !sleeveNegative;
        }

        Map<Direction, BlockElementFace> mapFacesIn = Maps.newEnumMap(Direction.class);

        for (Direction face : Direction.values()) {
            if (dir.getOpposite() != face) { //Discard side of sleeve that faces core
                BlockFaceUV uvface = null;
                if (face.getAxis().isHorizontal()) {
                    boolean facePositive = face.getAxisDirection() == Direction.AxisDirection.POSITIVE;
                    uvface = new BlockFaceUV(new float[]{facePositive ? 16 - radialHeight : 0, (sleeveNegative ? 16 - halfSize : 0), facePositive ? 16 : radialHeight, (sleeveNegative ? 16 : halfSize)}, ModelUtils.getFaceAngle(dir.getAxis(), face));
                } else {
                    uvface = new BlockFaceUV(new float[]{8 - radius, sleeveNegative ? 16 - halfSize : 0, 8 + radius, sleeveNegative ? 16 : halfSize}, ModelUtils.getFaceAngle(dir.getAxis(), face));
                }
                if (uvface != null) {
                    mapFacesIn.put(face, new BlockElementFace(null, -1, null, uvface));
                }
            }
        }

        BlockElement part = new BlockElement(posFrom, posTo, mapFacesIn, null, true);
        SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(blockModel.customData, ItemOverrides.EMPTY).particle(this.barkTexture);

        for (Map.Entry<Direction, BlockElementFace> e : part.faces.entrySet()) {
            Direction face = e.getKey();
            builder.addCulledFace(face, ModelUtils.makeBakedQuad(part, e.getValue(), this.barkTexture, face, BlockModelRotation.X0_Y0, this.modelResLoc));
        }

        return builder.build();
    }

    private BakedModel bakeVert(int radius, Direction dir) {
        int radialHeight = getRadialHeight(radius);
        SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(blockModel.customData, ItemOverrides.EMPTY).particle(this.barkTexture);

        AABB partBoundary = new AABB(8 - radius, radialHeight, 8 - radius, 8 + radius, 16 + radialHeight, 8 + radius)
                .move(dir.getStepX() * 7, 0, dir.getStepZ() * 7);

        for (int i = 0; i < 2; i++) {
            AABB pieceBoundary = partBoundary.intersect(new AABB(0, 0, 0, 16, 16, 16).move(0, 16 * i, 0));

            for (Direction face : Direction.values()) {
                Map<Direction, BlockElementFace> mapFacesIn = Maps.newEnumMap(Direction.class);

                BlockFaceUV uvface = new BlockFaceUV(ModelUtils.modUV(ModelUtils.getUVs(pieceBoundary, face)), ModelUtils.getFaceAngle(Direction.Axis.Y, face));
                mapFacesIn.put(face, new BlockElementFace(null, -1, null, uvface));

                Vector3f[] limits = ModelUtils.AABBLimits(pieceBoundary);

                BlockElement part = new BlockElement(limits[0], limits[1], mapFacesIn, null, true);
                builder.addCulledFace(face, ModelUtils.makeBakedQuad(part, part.faces.get(face), this.barkTexture, face, BlockModelRotation.X0_Y0, this.modelResLoc));
            }
        }

        return builder.build();
    }

    public BakedModel bakeCore(int radius, Direction.Axis axis, TextureAtlasSprite icon) {
        int radialHeight = getRadialHeight(radius);

        Vector3f posFrom = new Vector3f(8 - radius, 0, 8 - radius);
        Vector3f posTo = new Vector3f(8 + radius, radialHeight, 8 + radius);

        Map<Direction, BlockElementFace> mapFacesIn = Maps.newEnumMap(Direction.class);

        for (Direction face : Direction.values()) {
            BlockFaceUV uvface;
            if (face.getAxis().isHorizontal()) {
                boolean positive = face.getAxisDirection() == Direction.AxisDirection.POSITIVE;
                uvface = new BlockFaceUV(new float[]{positive ? 16 - radialHeight : 0, 8 - radius, positive ? 16 : radialHeight, 8 + radius}, ModelUtils.getFaceAngle(axis, face));
            } else {
                uvface = new BlockFaceUV(new float[]{8 - radius, 8 - radius, 8 + radius, 8 + radius}, ModelUtils.getFaceAngle(axis, face));
            }

            mapFacesIn.put(face, new BlockElementFace(null, -1, null, uvface));
        }

        BlockElement part = new BlockElement(posFrom, posTo, mapFacesIn, null, true);
        SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(blockModel.customData, ItemOverrides.EMPTY).particle(icon);

        for (Map.Entry<Direction, BlockElementFace> e : part.faces.entrySet()) {
            Direction face = e.getKey();
            builder.addCulledFace(face, ModelUtils.makeBakedQuad(part, e.getValue(), icon, face, BlockModelRotation.X0_Y0, this.modelResLoc));
        }

        return builder.build();
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random random, @Nonnull IModelData modelData) {
        if (side != null || state == null) {
            return Collections.emptyList();
        }

        final List<BakedQuad> quads = new ArrayList<>(24);

        int coreRadius = this.getRadius(state);

        int[] connections = new int[]{0, 0, 0, 0};
        RootConnections.ConnectionLevel[] connectionLevels = RootConnections.PLACEHOLDER_CONNECTION_LEVELS.clone();
        if (modelData instanceof RootConnections) {
            RootConnections connectionData = (RootConnections) modelData;
            connections = connectionData.getAllRadii();
            connectionLevels = connectionData.getConnectionLevels();
        }

        for (int i = 0; i < connections.length; i++) {
            connections[i] = Mth.clamp(connections[i], 0, coreRadius);
        }

        //The source direction is the biggest connection from one of the 6 directions
        Direction sourceDir = this.getSourceDir(coreRadius, connections);
        if (sourceDir == null) {
            sourceDir = Direction.DOWN;
        }
        int coreDir = this.resolveCoreDir(sourceDir);

        boolean isGrounded = state.getValue(SurfaceRootBlock.GROUNDED) == Boolean.TRUE;

        for (Direction face : Direction.values()) {
            //Get quads for core model
            if (isGrounded) {
                quads.addAll(cores[coreDir][coreRadius - 1].getQuads(state, face, random, modelData));
            }

            //Get quads for sleeves models
            if (coreRadius != 8) { //Special case for r!=8.. If it's a solid block so it has no sleeves
                for (Direction connDir : CoordUtils.HORIZONTALS) {
                    int idx = connDir.get2DDataValue();
                    int connRadius = connections[idx];
                    //If the connection side matches the quadpull side then cull the sleeve face.  Don't cull radius 1 connections for leaves(which are partly transparent).
                    if (connRadius > 0) {//  && (connRadius == 1 || side != connDir)) {
                        if (isGrounded) {
                            quads.addAll(sleeves[idx][connRadius - 1].getQuads(state, face, random, modelData));
                        }
                        if (connectionLevels[idx] == RootConnections.ConnectionLevel.HIGH) {
                            quads.addAll(verts[idx][connRadius - 1].getQuads(state, face, random, modelData));
                        }
                    }
                }
            }
        }

        return quads;
    }

    @Nonnull
    @Override
    public IModelData getModelData(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData modelData) {
        Block block = state.getBlock();
        return block instanceof SurfaceRootBlock ? new RootModelConnections(((SurfaceRootBlock) block).getConnectionData(level, pos)) : new RootModelConnections();
    }

    /**
     * Locates the side with the largest neighbor radius that's equal to or greater than this branch block
     *
     * @param coreRadius
     * @param connections an array of 6 integers, one for the radius of each connecting side. DUNSWE.
     * @return
     */
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
            sourceDir = null;//Has no source node
        }
        return sourceDir;
    }

    /**
     * Converts direction DUNSWE to 3 axis numbers for Y,Z,X
     *
     * @param dir
     * @return
     */
    protected int resolveCoreDir(Direction dir) {
        return dir.getAxis() == Direction.Axis.X ? 1 : 0;
    }

    protected int getRadius(BlockState state) {
        // This way works with branches that don't have the RADIUS property, like cactus
        return ((SurfaceRootBlock) state.getBlock()).getRadius(state);
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
    public boolean isCustomRenderer() {
        return true;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.barkTexture;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

}
