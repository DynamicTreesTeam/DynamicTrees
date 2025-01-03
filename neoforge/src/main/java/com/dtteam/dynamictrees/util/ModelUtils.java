package com.dtteam.dynamictrees.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.IModelBuilder;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import org.joml.Vector3f;

public class ModelUtils {

    public static final ModelProperty<RootConnections> ROOT_CONNECTIONS_PROPERTY = new ModelProperty<>();

    public static float[] getUVs(AABB box, Direction face) {
        return switch (face) {
            default -> new float[]{(float) box.minX, 16f - (float) box.minZ, (float) box.maxX, 16f - (float) box.maxZ};
            case UP -> new float[]{(float) box.minX, (float) box.minZ, (float) box.maxX, (float) box.maxZ};
            case NORTH -> new float[]{16f - (float) box.maxX, (float) box.minY, 16f - (float) box.minX, (float) box.maxY};
            case SOUTH -> new float[]{(float) box.minX, (float) box.minY, (float) box.maxX, (float) box.maxY};
            case WEST -> new float[]{(float) box.minZ, (float) box.minY, (float) box.maxZ, (float) box.maxY};
            case EAST -> new float[]{16f - (float) box.maxZ, (float) box.minY, 16f - (float) box.minZ, (float) box.maxY};
        };
    }

    /**
     * A Hack to determine the UV face angle for a block column on a certain axis
     *
     * @param axis
     * @param face
     * @return
     */
    public static int getFaceAngle(Axis axis, Direction face) {
        if (axis == Axis.Y) { //UP / DOWN
            return 0;
        } else if (axis == Axis.Z) {//NORTH / SOUTH
            return switch (face) {
                case UP -> 0;
                case WEST, NORTH -> 270;
                case DOWN -> 180;
                default -> 90;
            };
        } else { //EAST/WEST
            return (face == Direction.NORTH) ? 270 : 90;
        }
    }

    public static float[] modUV(float[] uvs) {
        uvs[0] = (int) uvs[0] & 0xf;
        uvs[1] = (int) uvs[1] & 0xf;
        uvs[2] = (((int) uvs[2] - 1) & 0xf) + 1;
        uvs[3] = (((int) uvs[3] - 1) & 0xf) + 1;
        return uvs;
    }

    public static Vector3f[] AABBLimits(AABB aabb) {
        return new Vector3f[]{
                new Vector3f((float) aabb.minX, (float) aabb.minY, (float) aabb.minZ),
                new Vector3f((float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ),
        };
    }

    public static BakedQuad makeBakedQuad(BlockElement blockPart, BlockElementFace partFace, TextureAtlasSprite atlasSprite, Direction dir, BlockModelRotation modelRotation) {
        return new FaceBakery().bakeQuad(blockPart.from, blockPart.to, partFace, atlasSprite, dir, modelRotation, blockPart.rotation, true);
    }

    public static IModelBuilder<?> getModelBuilder(IGeometryBakingContext context, TextureAtlasSprite particle) {
        ResourceLocation renderTypeHint = context.getRenderTypeHint();
        RenderTypeGroup renderTypes = renderTypeHint != null ? context.getRenderType(renderTypeHint) : RenderTypeGroup.EMPTY;

        return IModelBuilder.of(context.useAmbientOcclusion(), context.useBlockLight(), context.isGui3d(),
                context.getTransforms(), ItemOverrides.EMPTY, particle, renderTypes);
    }

    @SuppressWarnings("deprecation")
    public static TextureAtlasSprite getTexture(ResourceLocation resLoc) {
        return getTexture(resLoc, TextureAtlas.LOCATION_BLOCKS);
    }

    public static TextureAtlasSprite getTexture(ResourceLocation resLoc, ResourceLocation atlasResLoc) {
        return Minecraft.getInstance().getTextureAtlas(atlasResLoc).apply(resLoc);
    }

}