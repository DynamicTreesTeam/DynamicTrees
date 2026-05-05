package com.dtteam.dynamictrees.model.parts;

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
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

public record AerialRootSoilModelPart(QuadCollection quads, boolean useAmbientOcclusion, Material.Baked particleMaterial) implements BlockStateModelPart {

    @Override
    public List<BakedQuad> getQuads(@Nullable Direction direction) {
        return this.quads.getQuads(direction);
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return this.quads.materialFlags();
    }

    public record UnbakedPart(
            Material.Baked end,
            Material.Baked overlay,
            Material.Baked overlay_end,
            Material.Baked side
    ) implements Unbaked {

        @Override
        public void resolveDependencies(Resolver resolver) {}

        @Override
        public AerialRootSoilModelPart bake(ModelBaker baker) {
            return bake(baker, 8);
        }

        public AerialRootSoilModelPart bake(ModelBaker baker, int radius) {

            Vector3f posFrom = new Vector3f(8 - radius, 0, 8 - radius);
            Vector3f posTo = new Vector3f(8 + radius, 16, 8 + radius);

            QuadCollection.Builder builder = new QuadCollection.Builder();

            Map<Direction, CuboidFace> sideFaces = cylinderSides(radius, side);
            Map<Direction, CuboidFace> upFace = cylinderEnds(radius, end, Direction.UP);
            Map<Direction, CuboidFace> downFace = cylinderEnds(radius, side, Direction.DOWN);
            Map<Direction, CuboidFace> overlayFaces = cylinderSides(radius, overlay);
            Map<Direction, CuboidFace> overlayEndFaces = cylinderEnds(radius, overlay_end, Direction.DOWN);

            CuboidModelElement sidePart = new CuboidModelElement(posFrom, posTo, sideFaces);
            CuboidModelElement endPart1 = new CuboidModelElement(posFrom, posTo, upFace);
            CuboidModelElement endPart2 = new CuboidModelElement(posFrom, posTo, downFace);
            CuboidModelElement overlayPart = new CuboidModelElement(posFrom, posTo, overlayFaces);
            CuboidModelElement overlayEndPart = new CuboidModelElement(posFrom, posTo, overlayEndFaces);

            addFacesToBuilder(baker, builder, sidePart, side, radius == 8);
            addFacesToBuilder(baker, builder, endPart1, end, true);
            addFacesToBuilder(baker, builder, endPart2, side, true);
            addFacesToBuilder(baker, builder, overlayPart, overlay, radius == 8);
            addFacesToBuilder(baker, builder, overlayEndPart, overlay_end, true);

            return new AerialRootSoilModelPart(builder.build(), true, side);
        }

        private void addFacesToBuilder(ModelBaker baker, QuadCollection.Builder builder, CuboidModelElement sidePart, Material.Baked material, boolean cull) {
            for (Map.Entry<Direction, CuboidFace> e : sidePart.faces().entrySet()) {
                Direction dir = e.getKey();
                CuboidFace face = e.getValue();
                if (cull){
                    builder.addCulledFace(dir, ModelHelper.makeBakedQuad(baker, sidePart, face, material, dir));
                } else {
                    builder.addUnculledFace(ModelHelper.makeBakedQuad(baker, sidePart, face, material, dir));
                }
            }
        }

        private static Map<Direction, CuboidFace> cylinderSides(int radius, Material.Baked side) {
            Map<Direction, CuboidFace> mapFacesIn = Maps.newEnumMap(Direction.class);
            for (Direction face : CoordUtils.HORIZONTALS) {
                CuboidFace.UVs uv = new CuboidFace.UVs(8 - radius, 0, 8 + radius, 16);
                mapFacesIn.put(face, new CuboidFace(face, -1, side.toString(), uv, Quadrant.R0));
            }
            return mapFacesIn;
        }

        private static Map<Direction, CuboidFace> cylinderEnds(int radius, Material.Baked end, Direction face) {
            Map<Direction, CuboidFace> mapFacesIn = Maps.newEnumMap(Direction.class);
            CuboidFace.UVs uv = new CuboidFace.UVs(8 - radius, 8 - radius, 8 + radius, 8 + radius);
            mapFacesIn.put(face, new CuboidFace(face, -1, end.toString(), uv, Quadrant.R0));
            return mapFacesIn;
        }


    }

}