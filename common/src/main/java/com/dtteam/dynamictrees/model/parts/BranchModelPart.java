package com.dtteam.dynamictrees.model.parts;

import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.model.BranchMultiPartHolder;
import com.dtteam.dynamictrees.model.BranchMultiPartHolder.PartMap;
import com.dtteam.dynamictrees.model.ModelHelper;
import com.dtteam.dynamictrees.utility.CoordUtils;
import com.google.common.collect.Maps;
import com.mojang.math.Quadrant;
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
import java.util.function.Function;

public record BranchModelPart(QuadCollection quads, boolean useAmbientOcclusion, Material.Baked particleMaterial) implements BlockStateModelPart {

    private static final int MIN_RADIUS_FOR_ROOTS_CROSS = 4;
    final static float Z_FIGHTING_OFFSET = 0.001f;

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

        public PartMap<BranchModelPart> bakeAllSides(ModelBaker baker, int radius, Direction.Axis axis) {

            PartMap<QuadCollection.Builder> builders = createBuilders();

            CuboidModelElement part = generateCorePart(radius, axis, false);
            addUnculledFaces(baker, builders::get, part, material, false);

            return buildParts(builders, material);
        }

        protected CuboidModelElement generateCorePart(int radius, Direction.Axis axis, boolean flipNormals){
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

        protected @NotNull PartMap<BranchModelPart> buildParts(PartMap<QuadCollection.Builder> builders, Material.Baked material) {
            PartMap<BranchModelPart> parts = new PartMap<>();
            for (BranchMultiPartHolder.NullableDirection dir : BranchMultiPartHolder.NullableDirection.values())
                parts.put(dir, new BranchModelPart(builders.get(dir).build(), true, material));
            return parts;
        }

        protected @NotNull PartMap<QuadCollection.Builder> createBuilders() {
            PartMap<QuadCollection.Builder> builders = new PartMap<>();
            for (BranchMultiPartHolder.NullableDirection dir : BranchMultiPartHolder.NullableDirection.values())
                builders.put(dir, new QuadCollection.Builder());
            return builders;
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

        public PartMap<BranchModelPart> bakeAllSides(ModelBaker baker, int radius) {

            PartMap<BranchModelPart> parts = new PartMap<>();
            for (Direction dir : Direction.values()){
                CuboidModelElement part = generateSleevePart(radius, dir, false);
                QuadCollection.Builder builder = new QuadCollection.Builder();

                addUnculledFaces(baker, builder, part, material, false);

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
            return bakeAllSides(baker, BranchBlock.MAX_RADIUS+1).get(Direction.NORTH);
        }

        public PartMap<BranchModelPart> bakeAllSides(ModelBaker baker, int radius) {
            return bakeSides(baker, radius, EnumSet.allOf(Direction.class));
        }

        public PartMap<BranchModelPart> bakeSides(ModelBaker baker, int radius, EnumSet<Direction> sides) {
            AABB wholeVolume = new AABB(8 - radius, 0, 8 - radius, 8 + radius, 16, 8 + radius);

            ArrayList<Vec3i> offsets = new ArrayList<>();

            for (CoordUtils.Surround dir : CoordUtils.Surround.values()) {
                offsets.add(dir.getOffset()); // 8 surrounding component pieces
            }
            offsets.add(new Vec3i(0, 0, 0));//Center

            PartMap<BranchModelPart> parts = new PartMap<>();

            for (Direction face : sides) {
                QuadCollection.Builder builder = new QuadCollection.Builder();

                List<CuboidModelElement> cuboidParts = generateTrunkParts(face, offsets, wholeVolume);

                for (CuboidModelElement part : cuboidParts){
                    builder.addUnculledFace(ModelHelper.makeBakedQuad(baker, part, part.faces().get(face), material, face));
                }

                parts.put(face, new BranchModelPart(builder.build(), true, material));
            }

            return parts;
        }

        private @NotNull List<CuboidModelElement> generateTrunkParts(Direction face, ArrayList<Vec3i> offsets, AABB wholeVolume) {
            final Vec3i dirVector = face.getUnitVec3i();
            List<CuboidModelElement> cuboidParts = new LinkedList<>();
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

                    cuboidParts.add(new CuboidModelElement(limits[0], limits[1], mapFacesIn));
                }
            }
            return cuboidParts;
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

        private final boolean cross;
        public UnbakedRootCore(Material.Baked material, boolean cross) {
            super(material);
            this.cross = cross;
        }

        @Override
        public PartMap<BranchModelPart> bakeAllSides(ModelBaker baker, int radius, Direction.Axis axis) {

            PartMap<QuadCollection.Builder> builders = createBuilders();

            CuboidModelElement partOut = generateCorePart(radius, axis, false);
            CuboidModelElement partIn = generateCorePart(radius, axis, true);
            addUnculledFaces(baker, builders::get, partOut, material, false);
            addUnculledFaces(baker, builders::get, partIn, material, true);

            if (cross && radius >= MIN_RADIUS_FOR_ROOTS_CROSS){
                for (Direction.Axis planeAxis : Direction.Axis.values()){
                    if (planeAxis == axis) continue;
                    CuboidModelElement insideCross = generateCoreAxisPlane(radius, planeAxis, axis);
                    addUnculledFaces(baker, builders.get(null), insideCross, material, false);
                }
            }

            return buildParts(builders, material);
        }

        public CuboidModelElement generateCoreAxisPlane(int radius, Direction.Axis planeAxis, Direction.Axis coreAxis){
            Direction[] axisDirections = directionsOfAxis(planeAxis);

            Map<Direction, CuboidFace> mapFacesIn = Maps.newEnumMap(Direction.class);

            Vector3f posFrom = new Vector3f(8 - radius, 8 - radius, 8 - radius);
            Vector3f posTo = new Vector3f(8 + radius, 8 + radius, 8 + radius);

            final float center = 8 + Z_FIGHTING_OFFSET;

            if (planeAxis == Direction.Axis.X){
                posFrom = new Vector3f(center, posFrom.y(), posFrom.z());
                posTo = new Vector3f(center, posTo.y(), posTo.z());
            } else if (planeAxis == Direction.Axis.Y){
                posFrom = new Vector3f(posFrom.x(), center, posFrom.z());
                posTo = new Vector3f(posTo.x(), center, posTo.z());
            } else if (planeAxis == Direction.Axis.Z){
                posFrom = new Vector3f(posFrom.x(), posFrom.y(), center);
                posTo = new Vector3f(posTo.x(), posTo.y(), center);
            }

            for (Direction face : axisDirections) {
                CuboidFace.UVs uvface = new CuboidFace.UVs(8 - radius, 8 - radius, 8 + radius, 8 + radius);
                mapFacesIn.put(face, new CuboidFace(null, -1, "", uvface, ModelHelper.getFaceQuadrant(coreAxis, face.getOpposite())));
            }

            return new CuboidModelElement(posFrom, posTo, mapFacesIn);
        }
    }

    public static class UnbakedRootSleeveEnds extends UnbakedSleeve {

        public UnbakedRootSleeveEnds(Material.Baked material) {
            super(material);
        }

        public PartMap<BranchModelPart> bakeAllSides(ModelBaker baker, int radius) {

            PartMap<BranchModelPart> parts = new PartMap<>();
            for (Direction dir : Direction.values()){
                QuadCollection.Builder builder = new QuadCollection.Builder();

                CuboidModelElement endFaces = generateSleeveFace(radius, dir);
                addCulledFaces(baker, _->builder, endFaces, material);

                parts.put(dir, new BranchModelPart(builder.build(), true, material));
            }

            return parts;
        }

        public CuboidModelElement generateSleeveFace(int radius, Direction dir) {
            int dradius = radius * 2;
            int halfSize = (16 - dradius) / 2;
            float halfSizeX = dir.getStepX() != 0 ? halfSize + Z_FIGHTING_OFFSET : dradius;
            float halfSizeY = dir.getStepY() != 0 ? halfSize + Z_FIGHTING_OFFSET : dradius;
            float halfSizeZ = dir.getStepZ() != 0 ? halfSize + Z_FIGHTING_OFFSET : dradius;
            int move = 16 - halfSize;
            int centerX = 16 + (dir.getStepX() * move);
            int centerY = 16 + (dir.getStepY() * move);
            int centerZ = 16 + (dir.getStepZ() * move);

            Vector3f posFrom = new Vector3f((centerX - halfSizeX) / 2f, (centerY - halfSizeY) / 2f, (centerZ - halfSizeZ) / 2f);
            Vector3f posTo = new Vector3f((centerX + halfSizeX) / 2f, (centerY + halfSizeY) / 2f, (centerZ + halfSizeZ) / 2f);

            Map<Direction, CuboidFace> mapFacesIn = Maps.newEnumMap(Direction.class);
            CuboidFace.UVs uvface = new CuboidFace.UVs(8 - radius, 8 - radius, 8 + radius, 8 + radius);
            mapFacesIn.put(dir, new CuboidFace(dir, -1, "", uvface, Quadrant.R0));

            return new CuboidModelElement(posFrom, posTo, mapFacesIn);
        }

    }

    public static class UnbakedRootSleeve extends UnbakedSleeve {

        public UnbakedRootSleeve(Material.Baked material) {
            super(material);
        }

        @Override
        public PartMap<BranchModelPart> bakeAllSides(ModelBaker baker, int radius) {

            PartMap<BranchModelPart> parts = new PartMap<>();
            for (Direction dir : Direction.values()){
                QuadCollection.Builder builder = new QuadCollection.Builder();

                CuboidModelElement partOut = generateSleevePart(radius, dir, false);
                CuboidModelElement partIn = generateSleevePart(radius, dir, true);
                addUnculledFaces(baker, builder, partOut, material, false);
                addUnculledFaces(baker, builder, partIn, material, true);

                if (radius >= MIN_RADIUS_FOR_ROOTS_CROSS){
                    for (Direction.Axis axis : Direction.Axis.values()){
                        if (axis == dir.getAxis()) continue;
                        CuboidModelElement cross = generateSleeveAxisPlane(radius, axis, dir);
                        addUnculledFaces(baker, builder, cross, material, false);
                    }
                }

                parts.put(dir, new BranchModelPart(builder.build(), true, material));
            }

            return parts;
        }

        public CuboidModelElement generateSleeveAxisPlane(int radius, Direction.Axis axis, Direction dir){
            Direction[] axisDirections = directionsOfAxis(axis);

            int diameter = radius * 2;
            int halfSize = (16 - diameter) / 2;
            int halfSizeX = dir.getStepX() != 0 ? halfSize : diameter;
            int halfSizeY = dir.getStepY() != 0 ? halfSize : diameter;
            int halfSizeZ = dir.getStepZ() != 0 ? halfSize : diameter;
            int move = 16 - halfSize;
            int centerX = 16 + (dir.getStepX() * move);
            int centerY = 16 + (dir.getStepY() * move);
            int centerZ = 16 + (dir.getStepZ() * move);

            Map<Direction, CuboidFace> mapFacesIn = Maps.newEnumMap(Direction.class);

            Vector3f posFrom = new Vector3f((centerX - halfSizeX) / 2f, (centerY - halfSizeY) / 2f, (centerZ - halfSizeZ) / 2f);
            Vector3f posTo = new Vector3f((centerX + halfSizeX) / 2f, (centerY + halfSizeY) / 2f, (centerZ + halfSizeZ) / 2f);

            if (axis == Direction.Axis.X){
                posFrom = new Vector3f(8, posFrom.y(), posFrom.z());
                posTo = new Vector3f(8, posTo.y(), posTo.z());
            } else if (axis == Direction.Axis.Y){
                posFrom = new Vector3f(posFrom.x(), 8, posFrom.z());
                posTo = new Vector3f(posTo.x(), 8, posTo.z());
            } else if (axis == Direction.Axis.Z){
                posFrom = new Vector3f(posFrom.x(), posFrom.y(), 8);
                posTo = new Vector3f(posTo.x(), posTo.y(), 8);
            }

            for (Direction face : axisDirections){
                boolean negative = dir.getAxisDirection() == Direction.AxisDirection.NEGATIVE;
                if (dir.getAxis() == Direction.Axis.Z) negative = !negative;

                CuboidFace.UVs uvface = new CuboidFace.UVs(8 - radius, negative ? 16 - halfSize : 0, 8 + radius, negative ? 16 : halfSize);

                mapFacesIn.put(face, new CuboidFace(null, -1, "", uvface, ModelHelper.getFaceQuadrant(dir.getAxis(), face.getOpposite())));
            }

            return new CuboidModelElement(posFrom, posTo, mapFacesIn);
        }

    }

    public static class UnbakedHeartCore extends UnbakedCore{

        protected Material.Baked materialEnd;
        public UnbakedHeartCore(Material.Baked material, Material.Baked materialEnd) {
            super(material);
            this.materialEnd = materialEnd;
        }

        public PartMap<BranchModelPart> bakeAllSides(ModelBaker baker, int radius, Direction.Axis axis) {

            PartMap<QuadCollection.Builder> builders = createBuilders();

            CuboidModelElement part = generateCorePart(radius, axis, false);
            addUnculledFaces(baker, builders::get, part, material, materialEnd, axis);

            return buildParts(builders, material, materialEnd, axis);
        }

        private static void addUnculledFaces(ModelBaker baker, Function<Direction, QuadCollection.Builder> builders, CuboidModelElement part, Material.Baked material, Material.Baked materialEnds, Direction.Axis axis) {
            for (Map.Entry<Direction, CuboidFace> e : part.faces().entrySet()) {
                Direction face = e.getKey();
                Material.Baked mat = face.getAxis() == axis ? materialEnds : material;
                builders.apply(face).addUnculledFace(ModelHelper.makeBakedQuad(baker, part, e.getValue(), mat, face));
            }
        }

        protected static @NotNull PartMap<BranchModelPart> buildParts(PartMap<QuadCollection.Builder> builders, Material.Baked material, Material.Baked materialEnds, Direction.Axis axis) {
            PartMap<BranchModelPart> parts = new PartMap<>();
            for (BranchMultiPartHolder.NullableDirection dir : BranchMultiPartHolder.NullableDirection.values()){
                Material.Baked mat = dir.getDirection() != null && dir.getDirection().getAxis() == axis ? materialEnds : material;
                parts.put(dir, new BranchModelPart(builders.get(dir).build(), true, mat));
            }
            return parts;
        }
    }

    public static class UnbakedMossCore extends UnbakedCore {

        public UnbakedMossCore(Material.Baked material) {
            super(material);
        }

        public PartMap<BranchModelPart> bakeAllSides(ModelBaker baker, int radius, Direction.Axis axis) {

            QuadCollection.Builder builder = new QuadCollection.Builder();

            CuboidModelElement part = generateCorePart(radius, axis, false);
            addUnculledFaces(baker, builder, part, material, false);

            PartMap<BranchModelPart> parts = new PartMap<>();
            parts.put(Direction.UP, new BranchModelPart(builder.build(), true, material));
            return parts;
        }

        @Override
        protected CuboidModelElement generateCorePart(int radius, Direction.Axis axis, boolean flipNormals){
            Vector3f posFrom = new Vector3f(8 - radius, 8 + radius + Z_FIGHTING_OFFSET, 8 - radius);
            Vector3f posTo = new Vector3f(8 + radius, 8 + radius + 1, 8 + radius);

            Map<Direction, CuboidFace> mapFacesIn = Maps.newEnumMap(Direction.class);

            for (Direction face : Direction.values()) {
                CuboidFace.UVs uv;
                if (face.getAxis() == Direction.Axis.Y){
                    uv = new CuboidFace.UVs(8 - radius, 8-radius, 8 + radius, 8+radius);
                } else {
                    uv = new CuboidFace.UVs(8 - radius, 0, 8 + radius, 1);
                }
                mapFacesIn.put(face, new CuboidFace(face, -1, material.toString(), uv, Quadrant.R0));
            }

            return new CuboidModelElement(posFrom, posTo, mapFacesIn);
        }

    }

    public static class UnbakedMossSleeve extends UnbakedSleeve{

        public UnbakedMossSleeve(Material.Baked material) {
            super(material);
        }

        @Override
        public PartMap<BranchModelPart> bakeAllSides(ModelBaker baker, int radius) {

            PartMap<BranchModelPart> parts = new PartMap<>();
            for (Direction dir : Direction.values()){
                if (dir.getAxis() == Direction.Axis.Y) continue;
                CuboidModelElement part = generateSleevePart(radius, dir, false);
                QuadCollection.Builder builder = new QuadCollection.Builder();

                addUnculledFaces(baker, builder, part, material, false);

                parts.put(dir, new BranchModelPart(builder.build(), true, material));
            }

            return parts;
        }

        @Override
        public CuboidModelElement generateSleevePart(int radius, Direction dir, boolean flipNormals){
            //Work in double units(*2)
            int diameter = radius * 2;
            int halfSize = (16 - diameter) / 2;
            int halfSizeX = dir.getStepX() != 0 ? halfSize : diameter;
            int halfSizeZ = dir.getStepZ() != 0 ? halfSize : diameter;
            int move = 16 - halfSize;
            int centerX = 16 + (dir.getStepX() * move);
            int centerY = 16;
            int centerZ = 16 + (dir.getStepZ() * move);

            Vector3f posFrom = new Vector3f((centerX - halfSizeX) / 2f, (centerY + diameter) / 2f  + Z_FIGHTING_OFFSET, (centerZ - halfSizeZ) / 2f);
            Vector3f posTo = new Vector3f((centerX + halfSizeX) / 2f, (centerY + diameter) / 2f + 1, (centerZ + halfSizeZ) / 2f);

            boolean negative = dir.getAxisDirection() == Direction.AxisDirection.NEGATIVE;
            if (dir.getAxis() == Direction.Axis.Z) {//North/South
                negative = !negative;
            }

            Map<Direction, CuboidFace> mapFacesIn = Maps.newEnumMap(Direction.class);

            for (Direction face : Direction.values()) {
                CuboidFace.UVs uv;
                Quadrant q;
                if (face.getAxis() == Direction.Axis.Y){
                    uv = new CuboidFace.UVs(8 - radius, negative ? 16 - halfSize : 0, 8 + radius, negative ? 16 : halfSize);
                    q = ModelHelper.getFaceQuadrant(dir.getAxis(), face);
                } else {
                    uv = new CuboidFace.UVs(negative ? 16 - halfSize : 0, 0, negative ? 16 : halfSize,1);
                    q = Quadrant.R0;
                }
                mapFacesIn.put(face, new CuboidFace(face, -1, material.toString(), uv, q));
            }

            return new CuboidModelElement(posFrom, posTo, mapFacesIn);
        }

    }

    private static Direction[] directionsOfAxis(Direction.Axis axis){
        return Arrays.stream(Direction.values()).filter(d -> d.getAxis() == axis).toList().toArray(Direction[]::new);
    }

    private static void addUnculledFaces(ModelBaker baker, QuadCollection.Builder builder, CuboidModelElement part, Material.Baked material, boolean mirrorFace) {
        addUnculledFaces(baker, _->builder, part, material, mirrorFace);
    }

    private static void addUnculledFaces(ModelBaker baker, Function<Direction, QuadCollection.Builder> builders, CuboidModelElement part, Material.Baked material, boolean mirrorFace) {
        for (Map.Entry<Direction, CuboidFace> e : part.faces().entrySet()) {
            Direction face = e.getKey();
            builders.apply(face).addUnculledFace(ModelHelper.makeBakedQuad(baker, part, e.getValue(), material, mirrorFace ? face.getOpposite() : face));
        }
    }

    private static void addCulledFaces(ModelBaker baker, Function<Direction, QuadCollection.Builder> builders, CuboidModelElement part, Material.Baked material) {
        for (Map.Entry<Direction, CuboidFace> e : part.faces().entrySet()) {
            Direction face = e.getKey();
            builders.apply(face).addCulledFace(face, ModelHelper.makeBakedQuad(baker, part, e.getValue(), material, face));
        }
    }

}