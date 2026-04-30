package com.dtteam.dynamictrees.model;

import com.dtteam.dynamictrees.api.network.RootConnections;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.SurfaceRootBlock;
import com.mojang.math.Quadrant;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.cuboid.CuboidRotation;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class ModelHelper {

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
        ModelState noState = new NoModelState();
        return FaceBakery.bakeQuad(baker, blockPart.from(), blockPart.to(), partFace, material, dir, noState, noRotation, true, 0);
    }

    private static class NoModelState implements ModelState{ }

    public static ModelConnections getModelConnections(@NotNull BlockAndTintGetter world, @NotNull BlockPos pos, @NotNull BlockState state) {
        ModelConnections modelConnections;
        if (state.getBlock() instanceof BranchBlock branchBlock) {
            modelConnections = new ModelConnections(branchBlock.getConnectionData(world, pos, state)).setFamily(branchBlock.getFamily());
        } else {
            modelConnections = new ModelConnections();
        }

        return modelConnections;
    }

    public static RootConnections getRootConnections(@NotNull BlockAndTintGetter world, @NotNull BlockPos pos, @NotNull BlockState state) {
        return state.getBlock() instanceof SurfaceRootBlock surfaceRootBlock
                ? new RootConnections(surfaceRootBlock.getConnectionData(world, pos))
                : new RootConnections();
    }

}