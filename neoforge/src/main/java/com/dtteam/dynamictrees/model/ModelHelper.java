package com.dtteam.dynamictrees.model;

import com.dtteam.dynamictrees.api.network.RootConnections;
import com.mojang.math.Quadrant;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.cuboid.CuboidRotation;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.model.data.ModelProperty;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class ModelHelper {

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
        return switch (getFaceQuadrant(axis, face).ordinal()) {
            case 0 -> 0;
            case 1 -> 90;
            case 2 -> 180;
            case 3 -> 270;
            default -> throw new MatchException(null, null);
        };
    }

    public static Quadrant getFaceQuadrant(Axis axis, Direction face) {
        if (axis == Axis.Y) { //UP / DOWN
            return Quadrant.R0;
        } else if (axis == Axis.Z) {//NORTH / SOUTH
            return switch (face) {
                case UP -> Quadrant.R0;
                case WEST, NORTH -> Quadrant.R270;
                case DOWN -> Quadrant.R180;
                default -> Quadrant.R90;
            };
        } else { //EAST/WEST
            return (face == Direction.NORTH) ? Quadrant.R270 : Quadrant.R90;
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

    public static BakedQuad makeBakedQuad(ModelBaker baker, CuboidModelElement blockPart, CuboidFace partFace, Material.Baked material, Direction dir) {
        CuboidRotation noRotation = new CuboidRotation(new Vector3f(0,0,0), Matrix4f::new, false);
        ModelState noState = new ModelState() { @Override public Transformation transformation() {return ModelState.super.transformation();} };
        return FaceBakery.bakeQuad(baker, blockPart.from(), blockPart.to(), partFace, material, dir, noState, noRotation, true, 0);
    }

//    public static IModelBuilder<?> getModelBuilder(IGeometryBakingContext context, TextureAtlasSprite particle) {
//        Identifier renderTypeHint = context.getRenderTypeHint();
//        RenderTypeGroup renderTypes = renderTypeHint != null ? context.getRenderType(renderTypeHint) : RenderTypeGroup.EMPTY;
//
//        return IModelBuilder.of(context.useAmbientOcclusion(), context.useBlockLight(), context.isGui3d(),
//                context.getTransforms(), ItemOverrides.EMPTY, particle, renderTypes);
//    }
//
//    @SuppressWarnings("deprecation")
//    public static TextureAtlasSprite getTexture(Identifier resLoc) {
//        return getTexture(resLoc, TextureAtlas.LOCATION_BLOCKS);
//    }
//
//    public static TextureAtlasSprite getTexture(Identifier resLoc, Identifier atlasResLoc) {
//        return Minecraft.getInstance().getTextureAtlas(atlasResLoc).apply(resLoc);
//    }

}