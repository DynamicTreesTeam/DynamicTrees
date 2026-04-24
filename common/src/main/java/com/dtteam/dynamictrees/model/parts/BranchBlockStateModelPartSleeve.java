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
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

public record BranchBlockStateModelPartSleeve(QuadCollection quads, boolean useAmbientOcclusion, Material.Baked particleMaterial) implements BlockStateModelPart {

    @Override
    public List<BakedQuad> getQuads(@Nullable Direction direction) {
        return this.quads.getQuads(direction);
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return this.quads.materialFlags();
    }

    public BranchBlockStateModelPartSleeve faceOnly(@Nullable Direction direction, boolean cull){
        QuadCollection.Builder builder = new QuadCollection.Builder();
        for (BakedQuad quad : getQuads(direction)){
            if (direction != null && cull){
                builder.addCulledFace(direction, quad);
            } else {
                builder.addUnculledFace(quad);
            }
        }
        return new BranchBlockStateModelPartSleeve(builder.build(), useAmbientOcclusion, particleMaterial);
    }

    public record Unbaked(Material.Baked material) implements BlockStateModelPart.Unbaked {

        @Override
        public void resolveDependencies(Resolver resolver) {}

        @Override
        public BranchBlockStateModelPartSleeve bake(ModelBaker baker) {
            return bake(baker, 0, Direction.UP);
        }

        public BranchBlockStateModelPartSleeve bake(ModelBaker baker, int radius, Direction direction) {
            CuboidModelElement part = generateSleevePart(radius, direction, false);
            QuadCollection.Builder builder = new QuadCollection.Builder();

            for (Map.Entry<Direction, CuboidFace> e : part.faces().entrySet()) {
                Direction face = e.getKey();
                builder.addCulledFace(face, ModelHelper.makeBakedQuad(baker, part, e.getValue(), material, face));
            }

            return new BranchBlockStateModelPartSleeve(builder.build(), true, material);
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
}