package com.dtteam.dynamictrees.model.branch;

import com.mojang.blaze3d.platform.Transparency;
import com.mojang.math.Quadrant;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.quad.MutableQuad;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record BranchBlockStateModelPartSleeve(QuadCollection quads, boolean useAmbientOcclusion, Material.Baked particleMaterial) implements BlockStateModelPart {

    @Override
    public List<BakedQuad> getQuads(@Nullable Direction direction) {
        return this.quads.getQuads(direction);
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return this.quads.materialFlags();
    }

    public record Unbaked(Material.Baked material) implements BlockStateModelPart.Unbaked {

        @Override
        public void resolveDependencies(Resolver resolver) {}

        @Override
        public BranchBlockStateModelPartSleeve bake(ModelBaker baker) {
            return bake(baker, 0, Direction.UP);
        }

        public BranchBlockStateModelPartSleeve bake(ModelBaker baker, int radius, Direction direction) {

            return buildPart(baker, material, material, material, -0.5f, 0.5f);
        }

        //Test part builder
        private BranchBlockStateModelPartSleeve buildPart(ModelBaker baker, Material.Baked topSprite, Material.Baked bottomSprite, Material.Baked sideSprite, float yMin, float yMax) {

            QuadCollection.Builder builder = new QuadCollection.Builder();

            for (Direction dir : Direction.values()) {
                Material.Baked sprite = switch (dir) {
                    case UP -> topSprite;
                    case DOWN -> bottomSprite;
                    default -> sideSprite;
                };

                MutableQuad quad = new MutableQuad();
                quad.setSprite(sprite, Transparency.NONE);
                quad.setCubeFace(dir, 0.25f, yMin, 0.25f, 0.75f, yMax, 0.75f);
                quad.bakeUvsFromPosition();
                quad.recalculateWinding();

                if ((dir == Direction.UP && yMax == 1f) || (dir == Direction.DOWN && yMin == 0f)) {
                    builder.addCulledFace(dir, quad.toBakedQuad());
                } else if (dir.getAxis().isHorizontal()) {
                    builder.addCulledFace(dir, quad.toBakedQuad());
                } else {
                    builder.addUnculledFace(quad.toBakedQuad());
                }
            }

            return new BranchBlockStateModelPartSleeve(builder.build(), true, sideSprite);
        }

        private static @NotNull BakedQuad testFace(ModelBaker baker, Material.Baked particle) {
            ModelState noState = new ModelState() {@Override public Transformation transformation() {return ModelState.super.transformation();}};
            CuboidFace face = new CuboidFace(null, 0, "bark", new CuboidFace.UVs(0,0,16,16), Quadrant.R0);
            BakedQuad quad = FaceBakery.bakeQuad(baker, new Vector3f(0,0,0), new Vector3f(4,4,4), face, particle, Direction.UP, noState, null, true, 0);
            return quad;
        }
    }
}