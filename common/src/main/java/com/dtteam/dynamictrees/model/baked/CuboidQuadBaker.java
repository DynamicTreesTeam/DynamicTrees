package com.dtteam.dynamictrees.model.baked;

import com.mojang.math.Quadrant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidRotation;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;

/**
 * Bakes vanilla cuboid faces into 26.2 {@link BakedQuad}s.
 */
public final class CuboidQuadBaker {

    private static final ModelState IDENTITY = new ModelState() {};
    private static final CuboidRotation NO_ROTATION = new CuboidRotation(new Vector3f(8f, 8f, 8f), Matrix4f::new, false);

    private CuboidQuadBaker() {}

    public static Quadrant quadrant(int degrees) {
        return switch (Math.floorMod(degrees, 360)) {
            case 90 -> Quadrant.R90;
            case 180 -> Quadrant.R180;
            case 270 -> Quadrant.R270;
            default -> Quadrant.R0;
        };
    }

    public static int faceAngle(Direction.Axis axis, Direction face) {
        if (axis == Direction.Axis.Y) {
            return 0;
        } else if (axis == Direction.Axis.Z) {
            return switch (face) {
                case UP -> 0;
                case WEST -> 270;
                case DOWN -> 180;
                default -> 90;
            };
        } else {
            return (face == Direction.NORTH) ? 270 : 90;
        }
    }

    public static BakedQuad bake(ModelBaker baker, Vector3fc from, Vector3fc to, Direction face,
                                 float minU, float minV, float maxU, float maxV, int rotation, Material.Baked material) {
        CuboidFace cuboidFace = new CuboidFace(
                face,
                CuboidFace.NO_TINT,
                "",
                new CuboidFace.UVs(minU, minV, maxU, maxV),
                quadrant(rotation)
        );
        return FaceBakery.bakeQuad(baker, from, to, cuboidFace, material, face, IDENTITY, NO_ROTATION, true, 0);
    }


    public static List<BakedQuad> newList() {
        return new ArrayList<>(6);
    }

    /**
     * Bakes an unculled quad from four 0-1 block-space vertices and 0-1 UVs.
     */
    public static BakedQuad bakeUnculled(ModelBaker baker, Material.Baked material,
                                         Vector3f p0, float u0, float v0,
                                         Vector3f p1, float u1, float v1,
                                         Vector3f p2, float u2, float v2,
                                         Vector3f p3, float u3, float v3) {
        Vector3f e1 = new Vector3f(p1).sub(p0);
        Vector3f e2 = new Vector3f(p2).sub(p0);
        Vector3f normal = e1.cross(e2, new Vector3f());
        Direction facing = Direction.getApproximateNearest(normal.x, normal.y, normal.z);
        BakedQuad template = bake(baker, new Vector3f(0f, 0f, 0f), new Vector3f(16f, 16f, 16f),
                facing, 0f, 0f, 16f, 16f, 0, material);
        return new BakedQuad(
                baker.interner().vector(p0),
                baker.interner().vector(p1),
                baker.interner().vector(p2),
                baker.interner().vector(p3),
                packUv(baker, material, u0, v0),
                packUv(baker, material, u1, v1),
                packUv(baker, material, u2, v2),
                packUv(baker, material, u3, v3),
                facing,
                template.materialInfo()
        );
    }

    private static long packUv(ModelBaker baker, Material.Baked material, float u, float v) {
        BakedQuad sample = bake(baker, new Vector3f(0f, 0f, 0f), new Vector3f(1f, 1f, 1f),
                Direction.SOUTH, u * 16f, v * 16f, u * 16f + 0.01f, v * 16f + 0.01f, 0, material);
        return sample.packedUV0();
    }
}
