package com.dtteam.dynamictrees.model;

import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;

public final class BakedQuadMover {

    private BakedQuadMover() {
    }

    public static List<BakedQuad> move(List<BakedQuad> quads, Vec3 offset) {
        if (offset.equals(Vec3.ZERO)) {
            return quads;
        }
        List<BakedQuad> out = new ArrayList<>(quads.size());
        for (BakedQuad quad : quads) {
            out.add(move(quad, offset));
        }
        return out;
    }

    public static BakedQuad move(BakedQuad quad, Vec3 offset) {
        float x = (float) offset.x;
        float y = (float) offset.y;
        float z = (float) offset.z;
        return new BakedQuad(
                add(quad.position0(), x, y, z),
                add(quad.position1(), x, y, z),
                add(quad.position2(), x, y, z),
                add(quad.position3(), x, y, z),
                quad.packedUV0(),
                quad.packedUV1(),
                quad.packedUV2(),
                quad.packedUV3(),
                quad.direction(),
                quad.materialInfo()
        );
    }

    private static Vector3fc add(Vector3fc pos, float x, float y, float z) {
        return new Vector3f(pos.x() + x, pos.y() + y, pos.z() + z);
    }
}
