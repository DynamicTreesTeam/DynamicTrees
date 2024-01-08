package com.ferreusveritas.dynamictrees.models.baked;

import com.ferreusveritas.dynamictrees.client.ModelUtils;
import com.ferreusveritas.dynamictrees.models.modeldata.ModelConnections;
import com.google.common.collect.Maps;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.IModelBuilder;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class BasicRootsBlockBakedModel extends BasicBranchBlockBakedModel {

    private final BakedModel[][] sleeveFaces = new BakedModel[6][7];

    public BasicRootsBlockBakedModel(IGeometryBakingContext customData, ResourceLocation modelLocation, ResourceLocation barkTextureLocation, ResourceLocation ringsTextureLocation,
                                     Function<Material, TextureAtlasSprite> spriteGetter) {
        super(customData, modelLocation, barkTextureLocation, ringsTextureLocation, spriteGetter);
        initModels();
    }

    private void initModels() {
        if (getRenderType() == RenderType.solid()){
            for (int i = 0; i < 8; i++) {
                int radius = i + 1;
                if (radius < 8) {
                    for (Direction dir : Direction.values()) {
                        sleeveFaces[dir.get3DDataValue()][i] = bakeSleeveFace(radius, dir, ringsTexture);
                    }
                }
            }
        }
    }

    @Override
    public BakedModel bakeSleeve(int radius, Direction dir, TextureAtlasSprite bark) {
        boolean isTransparent = getRenderType() != RenderType.solid();
        BlockElement part = generateSleevePart(radius, dir, false);
        BlockElement reversePart = isTransparent ? generateSleevePart(radius, dir, true) : null;
        IModelBuilder<?> builder = ModelUtils.getModelBuilder(this.blockModel.customData, bark);

        for (Map.Entry<Direction, BlockElementFace> e : part.faces.entrySet()) {
            Direction face = e.getKey();
            builder.addCulledFace(face, ModelUtils.makeBakedQuad(part, e.getValue(), bark, face, BlockModelRotation.X0_Y0, this.modelLocation));
            if (isTransparent) builder.addCulledFace(face, ModelUtils.makeBakedQuad(reversePart, e.getValue(), bark, face.getOpposite(), BlockModelRotation.X0_Y0, this.modelLocation));
        }

        return builder.build();
    }
    @Override
    public BakedModel bakeCore(int radius, Axis axis, TextureAtlasSprite icon) {
        boolean isTransparent = getRenderType() != RenderType.solid();
        BlockElement part = generateCorePart(radius, axis, false);
        BlockElement reversePart = isTransparent ? generateCorePart(radius, axis, true) : null;
        IModelBuilder<?> builder = ModelUtils.getModelBuilder(this.blockModel.customData, icon);

        for (Map.Entry<Direction, BlockElementFace> e : part.faces.entrySet()) {
            Direction face = e.getKey();
            builder.addCulledFace(face, ModelUtils.makeBakedQuad(part, e.getValue(), icon, face, BlockModelRotation.X0_Y0, this.modelLocation));
            if (isTransparent) builder.addCulledFace(face, ModelUtils.makeBakedQuad(reversePart, e.getValue(), icon, face.getOpposite(), BlockModelRotation.X0_Y0, this.modelLocation));
        }

        return builder.build();

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
        mapFacesIn.put(dir, new BlockElementFace(dir, -1, null, uvface));

        BlockElement part = new BlockElement(posFrom, posTo, mapFacesIn, null, true);
        IModelBuilder<?> builder = ModelUtils.getModelBuilder(this.blockModel.customData, rings);

        for (Map.Entry<Direction, BlockElementFace> e : part.faces.entrySet()) {
            Direction face = e.getKey();
            builder.addCulledFace(face, ModelUtils.makeBakedQuad(part, e.getValue(), rings, face, BlockModelRotation.X0_Y0, this.modelLocation));
        }

        return builder.build();
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand, @Nonnull ModelData extraData, @Nullable RenderType renderType) {
        if (side == null) {
            return super.getQuads(state, null, rand, extraData, renderType);
        }
        if (state == null || getRenderType() != RenderType.solid()) {
            return Collections.emptyList();
        }

        final List<BakedQuad> quadsList = new ArrayList<>(24);
        final int coreRadius = getRadius(state);
        if (coreRadius > 8) {
            return Collections.emptyList();
        }

        int[] connections = new int[]{0, 0, 0, 0, 0, 0};
        ModelConnections connectionsData = extraData.get(ModelConnections.CONNECTIONS_PROPERTY);
        if (connectionsData != null) {
            connections = connectionsData.getAllRadii();
        }

        if (coreRadius != 8) {
            final int idx = side.get3DDataValue();
            final int connRadius = connections[idx];
            if (connRadius > 0) {
                quadsList.addAll(sleeveFaces[idx][connRadius - 1].getQuads(state, side, rand, extraData, renderType));
            }
        }

        return quadsList;
    }
}