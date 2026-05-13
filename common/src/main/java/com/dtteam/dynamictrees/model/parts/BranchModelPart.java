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

import java.util.*;

public record BranchModelPart(QuadCollection quads, boolean useAmbientOcclusion, Material.Baked particleMaterial) implements BlockStateModelPart {

    @Override
    public List<BakedQuad> getQuads(@Nullable Direction direction) {
        return this.quads.getQuads(direction);
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return this.quads.materialFlags();
    }

    public static class UnbakedCore implements BlockStateModelPart.Unbaked{
        @Override
        public void resolveDependencies(Resolver resolver) {}
        @Override
        public BranchModelPart bake(ModelBaker baker) {
            return bakeAllSides(baker, 8, Direction.Axis.Y).get(Direction.NORTH);
        }

        protected Material.Baked material;
        public UnbakedCore(Material.Baked material) {
            this.material = material;
        }

        public EnumMap<Direction, BranchModelPart> bakeAllSides(ModelBaker baker, int radius, Direction.Axis axis) {

            CuboidModelElement part = generateCorePart(radius, axis);
            EnumMap<Direction, QuadCollection.Builder> builders = new EnumMap<>(Direction.class);
            for (Direction dir : Direction.values()){
                builders.put(dir, new QuadCollection.Builder());
            }

            for (Map.Entry<Direction, CuboidFace> e : part.faces().entrySet()) {
                Direction face = e.getKey();
                builders.get(face).addUnculledFace(ModelHelper.makeBakedQuad(baker, part, e.getValue(), material, face));
            }

            EnumMap<Direction, BranchModelPart> parts = new EnumMap<>(Direction.class);
            for (Direction dir : Direction.values()){
                parts.put(dir, new BranchModelPart(builders.get(dir).build(), true, material));
            }

            return parts;
        }

        private CuboidModelElement generateCorePart(int radius, Direction.Axis axis){
            Vector3f posFrom = new Vector3f(8 - radius, 8 - radius, 8 - radius);
            Vector3f posTo = new Vector3f(8 + radius, 8 + radius, 8 + radius);

            Map<Direction, CuboidFace> mapFacesIn = Maps.newEnumMap(Direction.class);

            for (Direction face : Direction.values()) {
                CuboidFace.UVs uv = new CuboidFace.UVs(8 - radius, 8 - radius, 8 + radius, 8 + radius);
                mapFacesIn.put(face, new CuboidFace(face, -1, material.toString(), uv, ModelHelper.getFaceQuadrant(axis, face)));
            }

            return new CuboidModelElement(posFrom, posTo, mapFacesIn);
        }

    }

    public static class UnbakedSleeve implements BlockStateModelPart.Unbaked{
        @Override
        public void resolveDependencies(Resolver resolver) {}
        @Override
        public BranchModelPart bake(ModelBaker baker) {
            return bakeAllSides(baker, 8).get(Direction.NORTH);
        }

        protected Material.Baked material;
        public UnbakedSleeve(Material.Baked material) {
            this.material = material;
        }

        public EnumMap<Direction, BranchModelPart> bakeAllSides(ModelBaker baker, int radius) {

            EnumMap<Direction, BranchModelPart> parts = new EnumMap<>(Direction.class);
            for (Direction dir : Direction.values()){
                CuboidModelElement part = generateSleevePart(radius, dir, false);
                QuadCollection.Builder builder = new QuadCollection.Builder();

                for (Map.Entry<Direction, CuboidFace> e : part.faces().entrySet()) {
                    Direction face = e.getKey();
                    builder.addUnculledFace(ModelHelper.makeBakedQuad(baker, part, e.getValue(), material, face));
                }

                parts.put(dir, new BranchModelPart(builder.build(), true, material));
            }

            return parts;
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

    public static class UnbakedThickTrunk implements BlockStateModelPart.Unbaked{
        @Override
        public void resolveDependencies(Resolver resolver) {}

        protected Material.Baked material;
        protected boolean isRings;
        public UnbakedThickTrunk(Material.Baked material, boolean isRings) {
            this.material = material;
            this.isRings = isRings;
        }

        @Override
        public BranchModelPart bake(ModelBaker baker) {
            return bakeAllSides(baker, BranchBlock.MAX_RADIUS+1).get(Direction.UP);
        }

        public EnumMap<Direction, BranchModelPart> bakeAllSides(ModelBaker baker, int radius) {
            return bakeSides(baker, radius, EnumSet.allOf(Direction.class));
        }

        public EnumMap<Direction, BranchModelPart> bakeSides(ModelBaker baker, int radius, EnumSet<Direction> sides) {
            AABB wholeVolume = new AABB(8 - radius, 0, 8 - radius, 8 + radius, 16, 8 + radius);

            ArrayList<Vec3i> offsets = new ArrayList<>();

            for (CoordUtils.Surround dir : CoordUtils.Surround.values()) {
                offsets.add(dir.getOffset()); // 8 surrounding component pieces
            }
            offsets.add(new Vec3i(0, 0, 0));//Center

            EnumMap<Direction, BranchModelPart> parts = new EnumMap<>(Direction.class);

            for (Direction face : sides) {
                QuadCollection.Builder builder = new QuadCollection.Builder();

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

                        builder.addUnculledFace(ModelHelper.makeBakedQuad(baker, part, part.faces().get(face), material, face));

                    }
                }

                parts.put(face, new BranchModelPart(builder.build(), true, material));
            }

            return parts;
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

    public static class UnbakedRootCore extends UnbakedCore {

        public UnbakedRootCore(Material.Baked material) {
            super(material);
        }
    }

    public static class UnbakedOpaqueRootSleeve extends UnbakedSleeve {

        public UnbakedOpaqueRootSleeve(Material.Baked material) {
            super(material);
        }

    }

    public static class UnbakedRootSleeve extends UnbakedSleeve {

        public UnbakedRootSleeve(Material.Baked material) {
            super(material);
        }

    }

}