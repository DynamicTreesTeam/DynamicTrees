package com.ferreusveritas.dynamictrees.models.bakedmodels;

import com.ferreusveritas.dynamictrees.blocks.branches.BasicBranchBlock;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.client.ModelUtils;
import com.ferreusveritas.dynamictrees.models.modeldata.ModelConnections;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@OnlyIn(Dist.CLIENT)
public class BasicBranchBlockBakedModel extends BranchBlockBakedModel {

    protected TextureAtlasSprite barkTexture;
    protected TextureAtlasSprite ringsTexture;

    // 74 Baked models per tree family to achieve this. I guess it's not my problem.  Wasn't my idea anyway.
    private final IBakedModel[][] sleeves = new IBakedModel[6][7];
    private final IBakedModel[][] cores = new IBakedModel[3][8]; // 8 Cores for 3 axis with the bark texture all all 6 sides rotated appropriately.
    private final IBakedModel[] rings = new IBakedModel[8]; // 8 Cores with the ring textures on all 6 sides.

    public BasicBranchBlockBakedModel(ResourceLocation modelResLoc, ResourceLocation barkResLoc, ResourceLocation ringsResLoc) {
        super(modelResLoc, barkResLoc, ringsResLoc);
    }

    @Override
    public void setupModels() {
        this.barkTexture = ModelUtils.getTexture(this.barkResLoc);
        this.ringsTexture = ModelUtils.getTexture(this.ringsResLoc);

        for (int i = 0; i < 8; i++) {
            int radius = i + 1;
            if (radius < 8) {
                for (Direction dir : Direction.values()) {
                    sleeves[dir.get3DDataValue()][i] = bakeSleeve(radius, dir, barkTexture);
                }
            }
            cores[0][i] = bakeCore(radius, Axis.Y, barkTexture); //DOWN<->UP
            cores[1][i] = bakeCore(radius, Axis.Z, barkTexture); //NORTH<->SOUTH
            cores[2][i] = bakeCore(radius, Axis.X, barkTexture); //WEST<->EAST

            rings[i] = bakeCore(radius, Axis.Y, this.ringsTexture);
        }
    }

    public IBakedModel bakeSleeve(int radius, Direction dir, TextureAtlasSprite bark) {
        //Work in double units(*2)
        int dradius = radius * 2;
        int halfSize = (16 - dradius) / 2;
        int halfSizeX = dir.getStepX() != 0 ? halfSize : dradius;
        int halfSizeY = dir.getStepY() != 0 ? halfSize : dradius;
        int halfSizeZ = dir.getStepZ() != 0 ? halfSize : dradius;
        int move = 16 - halfSize;
        int centerX = 16 + (dir.getStepX() * move);
        int centerY = 16 + (dir.getStepY() * move);
        int centerZ = 16 + (dir.getStepZ() * move);

        Vector3f posFrom = new Vector3f((centerX - halfSizeX) / 2f, (centerY - halfSizeY) / 2f, (centerZ - halfSizeZ) / 2f);
        Vector3f posTo = new Vector3f((centerX + halfSizeX) / 2f, (centerY + halfSizeY) / 2f, (centerZ + halfSizeZ) / 2f);

        boolean negative = dir.getAxisDirection() == AxisDirection.NEGATIVE;
        if (dir.getAxis() == Axis.Z) {//North/South
            negative = !negative;
        }

        Map<Direction, BlockPartFace> mapFacesIn = Maps.newEnumMap(Direction.class);

        for (Direction face : Direction.values()) {
            if (dir.getOpposite() != face) { //Discard side of sleeve that faces core
                BlockFaceUV uvface = null;
                if (dir == face) {//Side of sleeve that faces away from core
                    if (radius == 1) { //We're only interested in end faces for radius == 1
                        uvface = new BlockFaceUV(new float[]{8 - radius, 8 - radius, 8 + radius, 8 + radius}, 0);
                    }
                } else { //UV for Bark texture
                    uvface = new BlockFaceUV(new float[]{8 - radius, negative ? 16 - halfSize : 0, 8 + radius, negative ? 16 : halfSize}, getFaceAngle(dir.getAxis(), face));
                }
                if (uvface != null) {
                    mapFacesIn.put(face, new BlockPartFace(null, -1, null, uvface));
                }
            }
        }

        BlockPart part = new BlockPart(posFrom, posTo, mapFacesIn, null, true);
        SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(this.blockModel.customData, ItemOverrideList.EMPTY).particle(bark);

        for (Map.Entry<Direction, BlockPartFace> e : part.faces.entrySet()) {
            Direction face = e.getKey();
            builder.addCulledFace(face, ModelUtils.makeBakedQuad(part, e.getValue(), bark, face, ModelRotation.X0_Y0, this.modelResLoc));
        }

        return builder.build();
    }

    public IBakedModel bakeCore(int radius, Axis axis, TextureAtlasSprite icon) {

        Vector3f posFrom = new Vector3f(8 - radius, 8 - radius, 8 - radius);
        Vector3f posTo = new Vector3f(8 + radius, 8 + radius, 8 + radius);

        Map<Direction, BlockPartFace> mapFacesIn = Maps.newEnumMap(Direction.class);

        for (Direction face : Direction.values()) {
            BlockFaceUV uvface = new BlockFaceUV(new float[]{8 - radius, 8 - radius, 8 + radius, 8 + radius}, getFaceAngle(axis, face));
            mapFacesIn.put(face, new BlockPartFace(null, -1, null, uvface));
        }

        BlockPart part = new BlockPart(posFrom, posTo, mapFacesIn, null, true);
        SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(this.blockModel.customData, ItemOverrideList.EMPTY).particle(icon);

        for (Map.Entry<Direction, BlockPartFace> e : part.faces.entrySet()) {
            Direction face = e.getKey();
            builder.addCulledFace(face, ModelUtils.makeBakedQuad(part, e.getValue(), icon, face, ModelRotation.X0_Y0, this.modelResLoc));
        }

        return builder.build();
    }

    /**
     * A Hack to determine the UV face angle for a block column on a certain axis
     *
     * @param axis
     * @param face
     * @return
     */
    public int getFaceAngle(Axis axis, Direction face) {
        if (axis == Axis.Y) { //UP / DOWN
            return 0;
        } else if (axis == Axis.Z) {//NORTH / SOUTH
            switch (face) {
                case UP:
                    return 0;
                case WEST:
                    return 270;
                case DOWN:
                    return 180;
                default:
                    return 90;
            }
        } else { //EAST/WEST
            return (face == Direction.NORTH) ? 270 : 90;
        }
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        if (state == null || side != null) {
            return Collections.emptyList();
        }

        final List<BakedQuad> quadsList = new ArrayList<>(24);

        final int coreRadius = getRadius(state);

        if (coreRadius > 8) {
            return Collections.emptyList();
        }

        int[] connections = new int[]{0, 0, 0, 0, 0, 0};
        Direction forceRingDir = null;
        final AtomicInteger twigRadius = new AtomicInteger(1);

        if (extraData instanceof ModelConnections) {
            final ModelConnections connectionsData = (ModelConnections) extraData;
            connections = connectionsData.getAllRadii();
            forceRingDir = connectionsData.getRingOnly();

            connectionsData.getFamily().ifValid(family ->
                    twigRadius.set(family.getPrimaryThickness()));
        }

        // Count number of connections.
        int numConnections = 0;
        for (int i : connections) {
            numConnections += (i != 0) ? 1 : 0;
        }

        if (numConnections == 0 && forceRingDir != null) {
            quadsList.addAll(rings[coreRadius - 1].getQuads(state, forceRingDir, rand, extraData));
        } else {
            // The source direction is the biggest connection from one of the 6 directions.
            final Direction sourceDir = getSourceDir(coreRadius, connections);
            final int coreDir = resolveCoreDir(sourceDir);

            // This is for drawing the rings on a terminating branch.
            final Direction coreRingDir = (numConnections == 1 && sourceDir != null) ? sourceDir.getOpposite() : null;

            for (Direction face : Direction.values()) {
                // Get quads for core model.
                if (coreRadius != connections[face.get3DDataValue()]) {
                    if ((coreRingDir == null || coreRingDir != face)) {
                        quadsList.addAll(cores[coreDir][coreRadius - 1].getQuads(state, face, rand, extraData));
                    } else {
                        quadsList.addAll(rings[coreRadius - 1].getQuads(state, face, rand, extraData));
                    }
                }
                // Get quads for sleeves models.
                if (coreRadius != 8) { // Special case for r!=8.. If it's a solid block so it has no sleeves.
                    for (Direction connDir : Direction.values()) {
                        final int idx = connDir.get3DDataValue();
                        final int connRadius = connections[idx];
                        // If the connection side matches the quadpull side then cull the sleeve face.  Don't cull radius 1 connections for leaves (which are partly transparent).
                        if (connRadius > 0 && (connRadius == twigRadius.get() || face != connDir)) {
                            quadsList.addAll(sleeves[idx][connRadius - 1].getQuads(state, face, rand, extraData));
                        }
                    }
                }

            }
        }

        return quadsList;
    }


    /**
     * Checks all neighboring tree parts to determine the connection radius for each side of this branch block.
     */
    @Nonnull
    @Override
    public IModelData getModelData(@Nonnull IBlockDisplayReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData) {
        final Block block = state.getBlock();

        if (!(block instanceof BranchBlock)) {
            return new ModelConnections();
        }

        return new ModelConnections(((BranchBlock) block).getConnectionData(world, pos, state))
                .setFamily(((BranchBlock) block).getFamily());
    }

    /**
     * Locates the side with the largest neighbor radius that's equal to or greater than this branch block
     *
     * @param coreRadius
     * @param connections an array of 6 integers, one for the radius of each connecting side. DUNSWE.
     * @return
     */
    @Nullable
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
    protected int resolveCoreDir(@Nullable Direction dir) {
        if (dir == null) {
            return 0;
        }
        return dir.get3DDataValue() >> 1;
    }

    protected int getRadius(BlockState blockState) {
        // This way works with branches that don't have the RADIUS property, like cactus
        return ((BasicBranchBlock) blockState.getBlock()).getRadius(blockState);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return barkTexture;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Nonnull
    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }

    @Override
    public boolean doesHandlePerspectives() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

}
