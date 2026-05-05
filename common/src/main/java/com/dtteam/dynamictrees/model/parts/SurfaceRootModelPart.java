package com.dtteam.dynamictrees.model.parts;

import com.dtteam.dynamictrees.model.ModelHelper;
import com.google.common.collect.Maps;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

public record SurfaceRootModelPart(QuadCollection quads, boolean useAmbientOcclusion, Material.Baked particleMaterial) implements BlockStateModelPart {

    @Override
    public List<BakedQuad> getQuads(@Nullable Direction direction) {
        return this.quads.getQuads(direction);
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return this.quads.materialFlags();
    }

    public record UnbakedCore(Material.Baked material) implements Unbaked {

        @Override
        public void resolveDependencies(Resolver resolver) {}

        @Override
        public SurfaceRootModelPart bake(ModelBaker baker) {
            return bake(baker, 8, Direction.Axis.Y);
        }

        public SurfaceRootModelPart bake(ModelBaker baker, int radius, Direction.Axis axis) {

            CuboidModelElement part = generateCorePart(radius, axis);
            QuadCollection.Builder builder = new QuadCollection.Builder();

            for (Map.Entry<Direction, CuboidFace> e : part.faces().entrySet()) {
                Direction face = e.getKey();
                    builder.addCulledFace(face, ModelHelper.makeBakedQuad(baker, part, e.getValue(), material, face));
            }

            return new SurfaceRootModelPart(builder.build(), true, material);
        }

        private CuboidModelElement generateCorePart(int radius, Direction.Axis axis){
            int diameter = radius * 2;
            Vector3f posFrom = new Vector3f(8 - radius, 0, 8 - radius);
            Vector3f posTo = new Vector3f(8 + radius, diameter, 8 + radius);

            Map<Direction, CuboidFace> mapFacesIn = Maps.newEnumMap(Direction.class);

            for (Direction face : Direction.values()) {
                CuboidFace.UVs uv;
                if (face.getAxis().isHorizontal()) {
                    boolean positive = face.getAxisDirection() == Direction.AxisDirection.POSITIVE;
                    uv = new CuboidFace.UVs(positive ? 16 - diameter : 0, 8 - radius, positive ? 16 : diameter, 8 + radius);
                } else {
                    uv = new CuboidFace.UVs(8 - radius, 8 - radius, 8 + radius, 8 + radius);
                }

                mapFacesIn.put(face, new CuboidFace(null, -1, material.toString(), uv, ModelHelper.getFaceQuadrant(axis, face)));
            }

            return new CuboidModelElement(posFrom, posTo, mapFacesIn);
        }

    }

    public record UnbakedSleeve(Material.Baked material) implements Unbaked {

        @Override
        public void resolveDependencies(Resolver resolver) {}

        @Override
        public SurfaceRootModelPart bake(ModelBaker baker) {
            return bake(baker, 0, Direction.UP);
        }

        public SurfaceRootModelPart bake(ModelBaker baker, int radius, Direction direction) {
            CuboidModelElement part = generateSleevePart(radius, direction, false);
            QuadCollection.Builder builder = new QuadCollection.Builder();

            for (Map.Entry<Direction, CuboidFace> e : part.faces().entrySet()) {
                Direction face = e.getKey();
                builder.addCulledFace(face, ModelHelper.makeBakedQuad(baker, part, e.getValue(), material, face));
            }

            return new SurfaceRootModelPart(builder.build(), true, material);
        }

        public CuboidModelElement generateSleevePart(int radius, Direction dir, boolean flipNormals){
            int diameter = radius * 2;

            //Work in double units(*2)
            int dradius = radius * 2;
            int halfSize = (16 - dradius) / 2;
            int halfSizeX = dir.getStepX() != 0 ? halfSize : dradius;
            int halfSizeZ = dir.getStepZ() != 0 ? halfSize : dradius;
            int move = 16 - halfSize;
            int centerX = 16 + (dir.getStepX() * move);
            int centerZ = 16 + (dir.getStepZ() * move);

            Vector3f posFrom = new Vector3f((float) ((centerX - halfSizeX) / 2), 0, (float) ((centerZ - halfSizeZ) / 2));
            Vector3f posTo = new Vector3f((float) ((centerX + halfSizeX) / 2), diameter, (float) ((centerZ + halfSizeZ) / 2));

            boolean negative = dir.getAxisDirection() == Direction.AxisDirection.NEGATIVE;
            if (dir.getAxis() == Direction.Axis.Z) {//North/South
                negative = !negative;
            }

            Map<Direction, CuboidFace> mapFacesIn = Maps.newEnumMap(Direction.class);

            for (Direction face : Direction.values()) {
                if (dir.getOpposite() != face) { //Discard side of sleeve that faces core
                    CuboidFace.UVs uvFace;
                    if (face.getAxis().isHorizontal()) {
                        boolean facePositive = face.getAxisDirection() == Direction.AxisDirection.POSITIVE;
                        uvFace = new CuboidFace.UVs(facePositive ? 16 - diameter : 0, (negative ? 16 - halfSize : 0), facePositive ? 16 : diameter, (negative ? 16 : halfSize));
                    } else {
                        uvFace = new CuboidFace.UVs(8 - radius, negative ? 16 - halfSize : 0, 8 + radius, negative ? 16 : halfSize);
                    }
                    mapFacesIn.put(face, new CuboidFace(face, -1, material.toString(), uvFace, ModelHelper.getFaceQuadrant(dir.getAxis(), face)));
                }
            }

            return new CuboidModelElement(posFrom, posTo, mapFacesIn);
        }
    }

    public record UnbakedVert(Material.Baked material) implements Unbaked {

        @Override
        public void resolveDependencies(Resolver resolver) {}

        @Override
        public SurfaceRootModelPart bake(ModelBaker baker) {
            return bake(baker, 0, Direction.UP);
        }

        public SurfaceRootModelPart bake(ModelBaker baker, int radius, Direction direction) {
            int radialHeight = radius * 2;
            QuadCollection.Builder builder = new QuadCollection.Builder();

            AABB partBoundary = new AABB(8 - radius, radialHeight, 8 - radius, 8 + radius, 16 + radialHeight, 8 + radius)
                    .move(direction.getStepX() * 7, 0, direction.getStepZ() * 7);

            for (int i = 0; i < 2; i++) {
                AABB pieceBoundary = partBoundary.intersect(new AABB(0, 0, 0, 16, 16, 16).move(0, 16 * i, 0));

                for (Direction face : Direction.values()) {
                    Map<Direction, CuboidFace> mapFacesIn = Maps.newEnumMap(Direction.class);

                    float[] uvCoords = ModelHelper.modUV(ModelHelper.getUVs(pieceBoundary, face));
                    CuboidFace.UVs uvface = new CuboidFace.UVs(uvCoords[0], uvCoords[1], uvCoords[2], uvCoords[3]);
                    mapFacesIn.put(face, new CuboidFace(face, -1, material.toString(), uvface, ModelHelper.getFaceQuadrant(Direction.Axis.Y, face)));

                    Vector3f[] limits = ModelHelper.AABBLimits(pieceBoundary);

                    CuboidModelElement part = new CuboidModelElement(limits[0], limits[1], mapFacesIn);
                    builder.addCulledFace(face, ModelHelper.makeBakedQuad(baker, part, part.faces().get(face), material, face));
                }
            }

            return new SurfaceRootModelPart(builder.build(), true, material);
        }

    }

}