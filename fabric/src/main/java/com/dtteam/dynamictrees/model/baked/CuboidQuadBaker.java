package com.dtteam.dynamictrees.model.baked;

import com.mojang.math.Quadrant;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
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
import java.util.function.Predicate;

/**
 * Bakes vanilla cuboid faces into 26.2 {@link BakedQuad}s for FRAPI emission.
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

    public static void emit(QuadEmitter emitter, List<BakedQuad> quads, Direction face, Predicate<Direction> cullTest) {
        if (face != null && cullTest.test(face)) {
            return;
        }
        for (BakedQuad quad : quads) {
            if (face != null && quad.direction() != face) {
                continue;
            }
            emitter.fromBakedQuad(quad);
            emitter.cullFace(quad.direction());
            emitter.emit();
        }
    }

    public static List<BakedQuad> newList() {
        return new ArrayList<>(6);
    }
}
