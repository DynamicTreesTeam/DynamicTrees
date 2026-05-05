package com.dtteam.dynamictrees.model.parts;

import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.model.ModelHelper;
import com.dtteam.dynamictrees.utility.CoordUtils;
import com.google.common.collect.Maps;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public record BranchModelPart(QuadCollection quads, boolean useAmbientOcclusion, Material.Baked particleMaterial) implements BlockStateModelPart {

    @Override
    public List<BakedQuad> getQuads(@Nullable Direction direction) {
        return this.quads.getQuads(direction);
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return this.quads.materialFlags();
    }

    public BranchModelPart faceOnly(@Nullable Direction direction, boolean cull){
        QuadCollection.Builder builder = new QuadCollection.Builder();
        for (BakedQuad quad : getQuads(direction)){
            if (direction != null && cull){
                builder.addCulledFace(direction, quad);
            } else {
                builder.addUnculledFace(quad);
            }
        }
        return new BranchModelPart(builder.build(), useAmbientOcclusion, particleMaterial);
    }

    public record UnbakedCore(Material.Baked material, boolean flipNormals) implements BlockStateModelPart.Unbaked {

        @Override
        public void resolveDependencies(Resolver resolver) {}

        @Override
        public BranchModelPart bake(ModelBaker baker) {
            return bake(baker, 8, Direction.Axis.Y);
        }

        public BranchModelPart bake(ModelBaker baker, int radius, Direction.Axis axis) {

            CuboidModelElement part = generateCorePart(radius, axis);
            QuadCollection.Builder builder = new QuadCollection.Builder();

            for (Map.Entry<Direction, CuboidFace> e : part.faces().entrySet()) {
                Direction face = e.getKey();
                    builder.addCulledFace(face, ModelHelper.makeBakedQuad(baker, part, e.getValue(), material, face));
            }

            return new BranchModelPart(builder.build(), true, material);
        }

        private CuboidModelElement generateCorePart(int radius, Direction.Axis axis){
            Vector3f posFrom = new Vector3f(8 - radius, 8 - radius, 8 - radius);
            Vector3f posTo = new Vector3f(8 + radius, 8 + radius, 8 + radius);
            if (flipNormals){
                Vector3f aux = posFrom;
                posFrom = posTo;
                posTo = aux;
            }

            Map<Direction, CuboidFace> mapFacesIn = Maps.newEnumMap(Direction.class);

            for (Direction face : Direction.values()) {
                CuboidFace.UVs uv = new CuboidFace.UVs(8 - radius, 8 - radius, 8 + radius, 8 + radius);
                mapFacesIn.put(face, new CuboidFace(face, -1, material.toString(), uv, ModelHelper.getFaceQuadrant(axis, face)));
            }

            return new CuboidModelElement(posFrom, posTo, mapFacesIn);
        }

    }

    public record UnbakedSleeve(Material.Baked material) implements BlockStateModelPart.Unbaked {

        @Override
        public void resolveDependencies(Resolver resolver) {}

        @Override
        public BranchModelPart bake(ModelBaker baker) {
            return bake(baker, 0, Direction.UP);
        }

        public BranchModelPart bake(ModelBaker baker, int radius, Direction direction) {
            CuboidModelElement part = generateSleevePart(radius, direction, false);
            QuadCollection.Builder builder = new QuadCollection.Builder();

            for (Map.Entry<Direction, CuboidFace> e : part.faces().entrySet()) {
                Direction face = e.getKey();
                builder.addCulledFace(face, ModelHelper.makeBakedQuad(baker, part, e.getValue(), material, face));
            }

            return new BranchModelPart(builder.build(), true, material);
        }

        public CuboidModelElement generateSleevePart(int radius, Direction dir, boolean flipNormals){
            //Work in double units(*2)
            int diameter = radius * 2;
            int halfSize = (16 - diameter) / 2;
            int halfSizeX = dir.getStepX() != 0 ? halfSize : diameter;
            int halfSizeY = dir.getStepY() != 0 ? halfSize : diameter;
            int halfSizeZ = dir.getStepZ() != 0 ? halfSize : diameter;
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

            boolean negative = dir.getAxisDirection() == Direction.AxisDirection.NEGATIVE;
            if (dir.getAxis() == Direction.Axis.Z) {//North/South
                negative = !negative;
            }

            Map<Direction, CuboidFace> mapFacesIn = Maps.newEnumMap(Direction.class);

            for (Direction face : Direction.values()) {
                if (dir.getOpposite() != face) { //Discard side of sleeve that faces core
                    CuboidFace.UVs uvface = null;
                    if (dir == face) {//Side of sleeve that faces away from core
                        if (radius == 1) { //We're only interested in end faces for radius == 1
                            uvface = new CuboidFace.UVs(8 - radius, 8 - radius, 8 + radius, 8 + radius);
                        }
                    } else { //UV for Bark texture
                        uvface = new CuboidFace.UVs(8 - radius, negative ? 16 - halfSize : 0, 8 + radius, negative ? 16 : halfSize);
                    }
                    if (uvface != null) {
                        mapFacesIn.put(face, new CuboidFace(face, -1, material.toString(), uvface, ModelHelper.getFaceQuadrant(dir.getAxis(), face)));
                    }
                }
            }

            return new CuboidModelElement(posFrom, posTo, mapFacesIn);
        }
    }

    public record UnbakedThickTrunk(Material.Baked material, boolean isRings) implements BlockStateModelPart.Unbaked {

        @Override
        public void resolveDependencies(Resolver resolver) {}

        @Override
        public BranchModelPart bake(ModelBaker baker) {
            return bake(baker, BranchBlock.MAX_RADIUS+1, EnumSet.noneOf(Direction.class));
        }

        public BranchModelPart bake(ModelBaker baker, int radius, EnumSet<Direction> faces) {

            QuadCollection.Builder builder = new QuadCollection.Builder();
            AABB wholeVolume = new AABB(8 - radius, 0, 8 - radius, 8 + radius, 16, 8 + radius);

            ArrayList<Vec3i> offsets = new ArrayList<>();

            for (CoordUtils.Surround dir : CoordUtils.Surround.values()) {
                offsets.add(dir.getOffset()); // 8 surrounding component pieces
            }
            offsets.add(new Vec3i(0, 0, 0));//Center

            for (Direction face : faces) {
                final Vec3i dirVector = face.getUnitVec3i();

                for (Vec3i offset : offsets) {
                    if (face.getAxis() == Direction.Axis.Y || new Vec3(dirVector.getX(), dirVector.getY(), dirVector.getZ()).add(new Vec3(offset.getX(), offset.getY(), offset.getZ())).lengthSqr() > 2.25) { //This means that the dir and face share a common direction
                        Vec3 scaledOffset = new Vec3(offset.getX() * 16, offset.getY() * 16, offset.getZ() * 16);//Scale the dimensions to match standard minecraft texels
                        AABB partBoundary = new AABB(0, 0, 0, 16, 16, 16).move(scaledOffset).intersect(wholeVolume);

                        Vector3f[] limits = ModelHelper.AABBLimits(partBoundary);

                        Map<Direction, CuboidFace> mapFacesIn = Maps.newEnumMap(Direction.class);

                        int wholeVolumeWidth = 48;
                        float[] uvCoords = isRings ?
                                getUvs(face, partBoundary, wholeVolumeWidth) :
                                ModelHelper.modUV(ModelHelper.getUVs(partBoundary, face));;

                        CuboidFace.UVs uvFace = new CuboidFace.UVs(uvCoords[0], uvCoords[1], uvCoords[2], uvCoords[3]);
                        mapFacesIn.put(face, new CuboidFace(null, -1, material.toString(), uvFace, ModelHelper.getFaceQuadrant(Direction.Axis.Y, face)));

                        CuboidModelElement part = new CuboidModelElement(limits[0], limits[1], mapFacesIn);
                        builder.addCulledFace(face, ModelHelper.makeBakedQuad(baker, part, part.faces().get(face), material, face));
                    }

                }
            }

            return new BranchModelPart(builder.build(), true, material);
        }

        private static float @NotNull [] getUvs(Direction face, AABB partBoundary, int wholeVolumeWidth) {
            float textureOffsetX = -16f;
            float textureOffsetZ = -16f;

            float minX = ((float) ((partBoundary.minX - textureOffsetX) / wholeVolumeWidth)) * 16f;
            float maxX = ((float) ((partBoundary.maxX - textureOffsetX) / wholeVolumeWidth)) * 16f;
            float minZ = ((float) ((partBoundary.minZ - textureOffsetZ) / wholeVolumeWidth)) * 16f;
            float maxZ = ((float) ((partBoundary.maxZ - textureOffsetZ) / wholeVolumeWidth)) * 16f;

            if (face == Direction.DOWN) {
                minZ = ((float) ((partBoundary.maxZ - textureOffsetZ) / wholeVolumeWidth)) * 16f;
                maxZ = ((float) ((partBoundary.minZ - textureOffsetZ) / wholeVolumeWidth)) * 16f;
            }

            return new float[]{minX, minZ, maxX, maxZ};
        }

    }

}