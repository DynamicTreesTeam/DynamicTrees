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

public record BranchBlockStateModelPartCore(QuadCollection quads, boolean useAmbientOcclusion, Material.Baked particleMaterial) implements BlockStateModelPart {

    @Override
    public List<BakedQuad> getQuads(@Nullable Direction direction) {
        return this.quads.getQuads(direction);
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return this.quads.materialFlags();
    }

    public BranchBlockStateModelPartCore faceOnly(@Nullable Direction direction, boolean cull){
        QuadCollection.Builder builder = new QuadCollection.Builder();
        for (BakedQuad quad : getQuads(direction)){
            if (direction != null && cull){
                builder.addCulledFace(direction, quad);
            } else {
                builder.addUnculledFace(quad);
            }
        }
        return new BranchBlockStateModelPartCore(builder.build(), useAmbientOcclusion, particleMaterial);
    }

    public record Unbaked(Material.Baked material, boolean flipNormals) implements BlockStateModelPart.Unbaked {

        @Override
        public void resolveDependencies(Resolver resolver) {}

        @Override
        public BranchBlockStateModelPartCore bake(ModelBaker baker) {
            return bake(baker, 8, Direction.Axis.Y);
        }

        public BranchBlockStateModelPartCore bake(ModelBaker baker, int radius, Direction.Axis axis) {

            CuboidModelElement part = generateCorePart(radius, axis);
            QuadCollection.Builder builder = new QuadCollection.Builder();

            for (Map.Entry<Direction, CuboidFace> e : part.faces().entrySet()) {
                Direction face = e.getKey();
                    builder.addCulledFace(face, ModelHelper.makeBakedQuad(baker, part, e.getValue(), material, face));
            }

            return new BranchBlockStateModelPartCore(builder.build(), true, material);
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

}