package com.dtteam.dynamictrees.model;

import com.mojang.blaze3d.platform.Transparency;
import com.mojang.math.Quadrant;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
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

public record BranchBlockStateModelPart(QuadCollection quads, boolean useAmbientOcclusion, Material.Baked particleMaterial) implements BlockStateModelPart {

    @Override
    public List<BakedQuad> getQuads(@Nullable Direction direction) {
        return this.quads.getQuads(direction);
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return this.quads.materialFlags();
    }

    public record Unbaked(Identifier bark, Identifier rings) implements BlockStateModelPart.Unbaked {

        private static final String BARK_TEXTURE = "bark_texture";
        private static final String RINGS_TEXTURE = "rings_texture";

        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Identifier.CODEC.fieldOf(BARK_TEXTURE).forGetter(Unbaked::bark),
                Identifier.CODEC.fieldOf(RINGS_TEXTURE).forGetter(Unbaked::rings)
        ).apply(i, Unbaked::new));

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {}

        @Override
        public BlockStateModelPart bake(ModelBaker baker) {
            return bakeAll(baker)[0];
        }

        public BranchBlockStateModelPart[] bakeAll(ModelBaker baker) {
            Material.Baked barkMaterial = baker.materials().get(new Material(bark, false), ()->BARK_TEXTURE);
            Material.Baked ringsMaterial = baker.materials().get(new Material(rings, false), ()->RINGS_TEXTURE);

            return new BranchBlockStateModelPart[] {
                    buildPart(baker, ringsMaterial, ringsMaterial, barkMaterial, 0f, 1f),
                    buildPart(baker, ringsMaterial, ringsMaterial, barkMaterial, 0f, 0.5f)
            };
        }

        //Test part builder
        private BranchBlockStateModelPart buildPart(ModelBaker baker, Material.Baked topSprite, Material.Baked bottomSprite, Material.Baked sideSprite, float yMin, float yMax) {

            QuadCollection.Builder builder = new QuadCollection.Builder();

            for (Direction dir : Direction.values()) {
                Material.Baked sprite = switch (dir) {
                    case UP -> topSprite;
                    case DOWN -> bottomSprite;
                    default -> sideSprite;
                };

                MutableQuad quad = new MutableQuad();
                quad.setSprite(sprite, Transparency.NONE);
                quad.setCubeFace(dir, 0f, yMin, 0f, 1f, yMax, 1f);
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

            return new BranchBlockStateModelPart(builder.build(), true, sideSprite);
        }

        private static @NotNull BakedQuad testFace(ModelBaker baker, Material.Baked particle) {
            ModelState noState = new ModelState() {@Override public Transformation transformation() {return ModelState.super.transformation();}};
            CuboidFace face = new CuboidFace(null, 0, "bark", new CuboidFace.UVs(0,0,16,16), Quadrant.R0);
            BakedQuad quad = FaceBakery.bakeQuad(baker, new Vector3f(0,0,0), new Vector3f(4,4,4), face, particle, Direction.UP, noState, null, true, 0);
            return quad;
        }
    }
}