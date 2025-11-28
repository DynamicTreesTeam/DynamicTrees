package com.dtteam.dynamictrees.model.baked;

import com.dtteam.dynamictrees.model.ModelHelper;
import com.dtteam.dynamictrees.model.modeldata.ModelConnections;
import com.google.common.collect.Maps;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.IModelBuilder;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class BasicRootsBlockBakedModel extends BasicBranchBlockBakedModel {

    private static final int MIN_RADIUS_FOR_CROSS = 4;

    private final BakedModel[][] sleeveFaces = new BakedModel[6][8];

    public BasicRootsBlockBakedModel(IGeometryBakingContext customData, ResourceLocation barkTextureLocation, ResourceLocation ringsTextureLocation, Function<Material, TextureAtlasSprite> spriteGetter) {
        super(customData, barkTextureLocation, ringsTextureLocation, spriteGetter);
        initModels();
    }

    private void initModels() {
        if (!isTransparent()){
            for (int i = 0; i < 8; i++) {
                int radius = i + 1;
                for (Direction dir : Direction.values()) {
                    sleeveFaces[dir.get3DDataValue()][i] = bakeSleeveFace(radius, dir, ringsTexture);
                }
            }
        }
    }

    private boolean isTransparent(){
        return getRenderType() != RenderType.solid();
    }

    @Override
    public BakedModel bakeSleeve(int radius, Direction dir, TextureAtlasSprite bark) {
        BlockElement part = generateSleevePart(radius, dir, false);
        IModelBuilder<?> builder = ModelHelper.getModelBuilder(this.blockModel.customData, bark);

        for (Map.Entry<Direction, BlockElementFace> e : part.faces.entrySet()) {
            Direction face = e.getKey();
            builder.addCulledFace(face, ModelHelper.makeBakedQuad(part, e.getValue(), bark, face, BlockModelRotation.X0_Y0));
        }

        if (isTransparent()){
            BlockElement insidePart = generateSleevePart(radius, dir, true);

            for (Map.Entry<Direction, BlockElementFace> e : insidePart.faces.entrySet()) {
                Direction face = e.getKey();
                builder.addCulledFace(face, ModelHelper.makeBakedQuad(insidePart, e.getValue(), bark, face.getOpposite(), BlockModelRotation.X0_Y0));
            }

            if (radius >= MIN_RADIUS_FOR_CROSS){
                for (Axis axis : Axis.values()){
                    if (axis == dir.getAxis()) continue;
                    BlockElement insideCross = generateSleeveAxisPlane(radius, axis, dir);
                    for (Map.Entry<Direction, BlockElementFace> e : insideCross.faces.entrySet()) {
                        Direction face = e.getKey();
                        builder.addCulledFace(face, ModelHelper.makeBakedQuad(insideCross, e.getValue(), bark, face, BlockModelRotation.X0_Y0));
                    }
                }
            }
        }

        return builder.build();
    }

    public BlockElement generateSleeveAxisPlane(int radius, Axis axis, Direction dir){
        Direction[] axisDirections = directionsOfAxis(axis);

        int dradius = radius * 2;
        int halfSize = (16 - dradius) / 2;
        int halfSizeX = dir.getStepX() != 0 ? halfSize : dradius;
        int halfSizeY = dir.getStepY() != 0 ? halfSize : dradius;
        int halfSizeZ = dir.getStepZ() != 0 ? halfSize : dradius;
        int move = 16 - halfSize;
        int centerX = 16 + (dir.getStepX() * move);
        int centerY = 16 + (dir.getStepY() * move);
        int centerZ = 16 + (dir.getStepZ() * move);

        Map<Direction, BlockElementFace> mapFacesIn = Maps.newEnumMap(Direction.class);

        Vector3f posFrom = new Vector3f((centerX - halfSizeX) / 2f, (centerY - halfSizeY) / 2f, (centerZ - halfSizeZ) / 2f);
        Vector3f posTo = new Vector3f((centerX + halfSizeX) / 2f, (centerY + halfSizeY) / 2f, (centerZ + halfSizeZ) / 2f);

        if (axis == Axis.X){
            posFrom = new Vector3f(8, posFrom.y(), posFrom.z());
            posTo = new Vector3f(8, posTo.y(), posTo.z());
        } else if (axis == Axis.Y){
            posFrom = new Vector3f(posFrom.x(), 8, posFrom.z());
            posTo = new Vector3f(posTo.x(), 8, posTo.z());
        } else if (axis == Axis.Z){
            posFrom = new Vector3f(posFrom.x(), posFrom.y(), 8);
            posTo = new Vector3f(posTo.x(), posTo.y(), 8);
        }

        for (Direction face : axisDirections){

            boolean negative = dir.getAxisDirection() == Direction.AxisDirection.NEGATIVE;
            if (dir.getAxis() == Axis.Z) {
                negative = !negative;
            }

            BlockFaceUV uvface = new BlockFaceUV(
                    new float[]{8 - radius, negative ? 16 - halfSize : 0, 8 + radius, negative ? 16 : halfSize},
                    getFaceAngle(dir.getAxis(), face.getOpposite()));

            mapFacesIn.put(face, new BlockElementFace(null, -1, "", uvface));
        }

        return new BlockElement(posFrom, posTo, mapFacesIn, null, true);
    }

    @Override
    public BakedModel bakeCore(int radius, Axis coreAxis, TextureAtlasSprite icon) {

        IModelBuilder<?> builder = ModelHelper.getModelBuilder(this.blockModel.customData, icon);

        BlockElement part = generateCorePart(radius, coreAxis, false);
        for (Map.Entry<Direction, BlockElementFace> e : part.faces.entrySet()) {
            Direction face = e.getKey();
            builder.addCulledFace(face, ModelHelper.makeBakedQuad(part, e.getValue(), icon, face, BlockModelRotation.X0_Y0));
        }

        if (isTransparent()){
            BlockElement insidePart = generateCorePart(radius, coreAxis, true);
            for (Map.Entry<Direction, BlockElementFace> e : insidePart.faces.entrySet()) {
                Direction face = e.getKey();
                builder.addCulledFace(face, ModelHelper.makeBakedQuad(insidePart, e.getValue(), icon, face.getOpposite(), BlockModelRotation.X0_Y0));
            }

            if (radius >= MIN_RADIUS_FOR_CROSS && icon != ringsTexture){
                for (Axis planeAxis : Axis.values()){
                    if (planeAxis == coreAxis) continue;
                    BlockElement insideCross = generateCoreAxisPlane(radius, planeAxis, coreAxis);
                    for (Map.Entry<Direction, BlockElementFace> e : insideCross.faces.entrySet()) {
                        Direction face = e.getKey();
                        //this one is unculled cause the inside cross is always visible
                        builder.addUnculledFace(ModelHelper.makeBakedQuad(insideCross, e.getValue(), icon, face, BlockModelRotation.X0_Y0));
                    }
                }
            }
        }

        return builder.build();

    }

    public BlockElement generateCoreAxisPlane(int radius, Axis planeAxis, Axis coreAxis){
        Direction[] axisDirections = directionsOfAxis(planeAxis);

        Map<Direction, BlockElementFace> mapFacesIn = Maps.newEnumMap(Direction.class);

        Vector3f posFrom = new Vector3f(8 - radius, 8 - radius, 8 - radius);
        Vector3f posTo = new Vector3f(8 + radius, 8 + radius, 8 + radius);

        final float zFightingOffset = 0.001f;
        final float center = 8 + zFightingOffset;

        if (planeAxis == Axis.X){
            posFrom = new Vector3f(center, posFrom.y(), posFrom.z());
            posTo = new Vector3f(center, posTo.y(), posTo.z());
        } else if (planeAxis == Axis.Y){
            posFrom = new Vector3f(posFrom.x(), center, posFrom.z());
            posTo = new Vector3f(posTo.x(), center, posTo.z());
        } else if (planeAxis == Axis.Z){
            posFrom = new Vector3f(posFrom.x(), posFrom.y(), center);
            posTo = new Vector3f(posTo.x(), posTo.y(), center);
        }

        for (Direction face : axisDirections) {
            BlockFaceUV uvface = new BlockFaceUV(new float[]{8 - radius, 8 - radius, 8 + radius, 8 + radius}, getFaceAngle(coreAxis, face.getOpposite()));
            mapFacesIn.put(face, new BlockElementFace(null, -1, "", uvface));
        }

        return new BlockElement(posFrom, posTo, mapFacesIn, null, true);
    }

    public BakedModel bakeSleeveFace(int radius, Direction dir, TextureAtlasSprite rings) {
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

        Map<Direction, BlockElementFace> mapFacesIn = Maps.newEnumMap(Direction.class);
        BlockFaceUV uvface = new BlockFaceUV(new float[]{8 - radius, 8 - radius, 8 + radius, 8 + radius}, 0);
        mapFacesIn.put(dir, new BlockElementFace(dir, -1, "", uvface));

        BlockElement part = new BlockElement(posFrom, posTo, mapFacesIn, null, true);
        IModelBuilder<?> builder = ModelHelper.getModelBuilder(this.blockModel.customData, rings);

        for (Map.Entry<Direction, BlockElementFace> e : part.faces.entrySet()) {
            Direction face = e.getKey();
            builder.addCulledFace(face, ModelHelper.makeBakedQuad(part, e.getValue(), rings, face, BlockModelRotation.X0_Y0));
        }

        return builder.build();
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
        if (state == null) return Collections.emptyList();

        final int coreRadius = getRadius(state);
        if (coreRadius > 8) return Collections.emptyList();

        int[] connections = new int[]{0, 0, 0, 0, 0, 0};
        ModelConnections connectionsData = extraData.get(ModelConnections.CONNECTIONS_PROPERTY);
        if (connectionsData != null) connections = connectionsData.getAllRadii();

        if (side == null) {
            List<BakedQuad> quadsList = super.getQuads(state, null, rand, extraData, renderType);

            //The core inside cross is stored in the null side
            final Direction sourceDir = getSourceDir(coreRadius, connections);
            final int coreDir = resolveCoreDir(sourceDir);

            quadsList.addAll(cores[coreDir][coreRadius - 1].getQuads(state, null, rand, extraData, renderType));

            return quadsList;
        }

        //From here on is to add the ends to solid roots
        if (getRenderType() != RenderType.solid()) return Collections.emptyList();

        final List<BakedQuad> quadsList = new ArrayList<>(24);

        final int idx = side.get3DDataValue();
        final int connRadius = connections[idx];
        if (connRadius > 0) {
            quadsList.addAll(sleeveFaces[idx][connRadius - 1].getQuads(state, side, rand, extraData, renderType));
        }

        return quadsList;
    }

    private Direction[] directionsOfAxis(Axis axis){
        return Arrays.stream(Direction.values()).filter(d -> d.getAxis() == axis).toList().toArray(Direction[]::new);
    }

}