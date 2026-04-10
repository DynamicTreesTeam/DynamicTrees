package com.dtteam.dynamictrees.model.baked;

import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.model.ModelHelper;
import com.dtteam.dynamictrees.model.modeldata.ModelConnections;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.NamedRenderTypeManager;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.IModelBuilder;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;


public class BasicBranchBlockBakedModel implements IDynamicBakedModel {

    protected final BlockModel blockModel;

    protected final TextureAtlasSprite barkTexture;
    protected final TextureAtlasSprite ringsTexture;


    protected final BakedModel[][] sleeves = new BakedModel[6][7];
    protected final BakedModel[][] cores = new BakedModel[3][8]; // 8 Cores for 3 axis with the bark texture all all 6 sides rotated appropriately.
    protected final BakedModel[] rings = new BakedModel[8]; // 8 Cores with the ring textures on all 6 sides.

    public BasicBranchBlockBakedModel(IGeometryBakingContext customData, Identifier barkTextureLocation, Identifier ringsTextureLocation, Function<Material, TextureAtlasSprite> spriteGetter) {
        this.blockModel = new BlockModel(null, new ArrayList<>(), new HashMap<>(), false, BlockModel.GuiLight.FRONT, ItemTransforms.NO_TRANSFORMS, new ArrayList<>());
        if (customData.getRenderTypeHint() != null){
            this.blockModel.customData.setRenderTypeHint(customData.getRenderTypeHint());
        }
        this.barkTexture = spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, barkTextureLocation));
        this.ringsTexture = spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, ringsTextureLocation));
        initModels();
    }

    private void initModels() {
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

            rings[i] = bakeCore(radius, Axis.Y, ringsTexture);
        }
    }

    public BlockElement generateSleevePart(int radius, Direction dir, boolean flipNormals){
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
        if (flipNormals){
            Vector3f aux = posFrom;
            posFrom = posTo;
            posTo = aux;
            dir = dir.getOpposite();
        }

        boolean negative = dir.getAxisDirection() == AxisDirection.NEGATIVE;
        if (dir.getAxis() == Axis.Z) {//North/South
            negative = !negative;
        }

        Map<Direction, BlockElementFace> mapFacesIn = Maps.newEnumMap(Direction.class);

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
                    mapFacesIn.put(face, new BlockElementFace(null, -1, null, uvface));
                }
            }
        }

        return new BlockElement(posFrom, posTo, mapFacesIn, null, true);
    }
    public BakedModel bakeSleeve(int radius, Direction dir, TextureAtlasSprite bark) {

        BlockElement part = generateSleevePart(radius, dir, false);
        IModelBuilder<?> builder = ModelHelper.getModelBuilder(this.blockModel.customData, bark);

        for (Map.Entry<Direction, BlockElementFace> e : part.faces.entrySet()) {
            Direction face = e.getKey();
            builder.addCulledFace(face, ModelHelper.makeBakedQuad(part, e.getValue(), bark, face, BlockModelRotation.X0_Y0));
        }

        return builder.build();
    }

    protected BlockElement generateCorePart (int radius, Axis axis, boolean flipNormals){
        Vector3f posFrom = new Vector3f(8 - radius, 8 - radius, 8 - radius);
        Vector3f posTo = new Vector3f(8 + radius, 8 + radius, 8 + radius);
        if (flipNormals){
            Vector3f aux = posFrom;
            posFrom = posTo;
            posTo = aux;
        }

        Map<Direction, BlockElementFace> mapFacesIn = Maps.newEnumMap(Direction.class);

        for (Direction face : Direction.values()) {
            BlockFaceUV uvface = new BlockFaceUV(new float[]{8 - radius, 8 - radius, 8 + radius, 8 + radius}, getFaceAngle(axis, face));
            mapFacesIn.put(face, new BlockElementFace(null, -1, null, uvface));
        }

        return new BlockElement(posFrom, posTo, mapFacesIn, null, true);
    }
    public BakedModel bakeCore(int radius, Axis axis, TextureAtlasSprite icon) {

        BlockElement part = generateCorePart(radius, axis, false);
        IModelBuilder<?> builder = ModelHelper.getModelBuilder(this.blockModel.customData, icon);

        for (Map.Entry<Direction, BlockElementFace> e : part.faces.entrySet()) {
            Direction face = e.getKey();
            builder.addCulledFace(face, ModelHelper.makeBakedQuad(part, e.getValue(), icon, face, BlockModelRotation.X0_Y0));
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
            return switch (face) {
                case UP -> 0;
                case WEST -> 270;
                case DOWN -> 180;
                default -> 90;
            };
        } else { //EAST/WEST
            return (face == Direction.NORTH) ? 270 : 90;
        }
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
        if (state == null || side != null) return Collections.emptyList();

        final int coreRadius = getRadius(state);
        if (coreRadius > 8) return Collections.emptyList();


        int[] connections = new int[]{0, 0, 0, 0, 0, 0};
        Direction forceRingDir = null;
        final AtomicInteger twigRadius = new AtomicInteger(1);

        ModelConnections connectionsData = extraData.get(ModelConnections.CONNECTIONS_PROPERTY);
        if (connectionsData != null) {
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

        final List<BakedQuad> quadsList = new ArrayList<>(24);

        if (numConnections == 0 && forceRingDir != null) {
            quadsList.addAll(rings[coreRadius - 1].getQuads(state, forceRingDir, rand, extraData, renderType));
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
                        quadsList.addAll(cores[coreDir][coreRadius - 1].getQuads(state, face, rand, extraData, renderType));
                    } else {
                        quadsList.addAll(rings[coreRadius - 1].getQuads(state, face, rand, extraData, renderType));
                    }
                }
                // Get quads for sleeves models.
                if (coreRadius != 8) { // Special case for r!=8... If it's a solid block, so it has no sleeves.
                    for (Direction connDir : Direction.values()) {
                        final int idx = connDir.get3DDataValue();
                        final int connRadius = connections[idx];
                        // If the connection side matches the quadpull side then cull the sleeve face.  Don't cull radius-1 connections for leaves (which are partly transparent).
                        if (connRadius > 0 && (connRadius <= twigRadius.get() || face != connDir)) {
                            quadsList.addAll(sleeves[idx][connRadius - 1].getQuads(state, face, rand, extraData, renderType));
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
    @NotNull
    @Override
    public ModelData getModelData(@NotNull BlockAndTintGetter world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData tileData) {
        ModelConnections modelConnections;
        if (state.getBlock() instanceof BranchBlock branchBlock) {
            modelConnections = new ModelConnections(branchBlock.getConnectionData(world, pos, state)).setFamily(branchBlock.getFamily());
        } else {
            modelConnections = new ModelConnections();
        }

        return modelConnections.toModelData(tileData);
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
        return ((BranchBlock) blockState.getBlock()).getRadius(blockState);
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

    @NotNull
    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        return ChunkRenderTypeSet.of(getRenderType());
    }

    public RenderType getRenderType (){
        Identifier renderTypeHint = blockModel.customData.getRenderTypeHint();
        if (renderTypeHint == null)
            return RenderType.solid();
        return NamedRenderTypeManager.get(renderTypeHint).block();
    }
}