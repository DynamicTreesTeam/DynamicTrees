package com.dtteam.dynamictrees.model.blockstate;

import com.dtteam.dynamictrees.model.ModelHelper;
import com.dtteam.dynamictrees.registry.PottedSaplingBlockEntityNF;
import com.dtteam.dynamictrees.tree.species.Species;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record PottedSaplingBlockStateModel(
        BlockStateModelPart pot,
        Map<Species, SimpleModelWrapper> saplings,
        Material.Baked particleMaterial
) implements DynamicBlockStateModel {

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return this.pot.materialFlags();
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return level.getModelData(pos).get(PottedSaplingBlockEntityNF.SPECIES);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {

        Species species = level.getModelData(pos).get(PottedSaplingBlockEntityNF.SPECIES);
        if (species == null || !species.isValid() || species.getSapling().isEmpty()) return;

        SimpleModelWrapper sapling = saplings.get(species);

        parts.add(pot);
        parts.add(sapling);
    }

    public record Unbaked(Identifier modelLocation) implements CustomUnbakedBlockStateModel {

        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Identifier.CODEC.fieldOf("model").forGetter(Unbaked::modelLocation)
        ).apply(i, Unbaked::new));

        @Override
        public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
            return CODEC;
        }

        @Override
        public BlockStateModel bake(ModelBaker modelBaker) {
            ResolvedModel pot = modelBaker.getModel(modelLocation);
            TextureSlots potSlots = pot.getTopTextureSlots();
            Material.Baked material = pot.resolveParticleMaterial(potSlots, modelBaker);

            SimpleModelWrapper bakedPot = new SimpleModelWrapper(
                    pot.bakeTopGeometry(potSlots, modelBaker, ModelHelper.noState()),
                    pot.getTopAmbientOcclusion(),
                    material
            );

            Map<Species, SimpleModelWrapper> saplings = new HashMap<>();

            for (Species species : Species.REGISTRY){
                if (species.getSapling().isPresent()){
                    Identifier modelLocation = species.getSaplingModelLocation();
                    ResolvedModel resolved = modelBaker.getModel(modelLocation);

                    TextureSlots saplingSlots = resolved.getTopTextureSlots();
                    saplings.put(species, new SimpleModelWrapper(
                            resolved.bakeTopGeometry(saplingSlots, modelBaker, OFFSET_UP),
                            resolved.getTopAmbientOcclusion(),
                            resolved.resolveParticleMaterial(saplingSlots, modelBaker)));
                }
            }

            return new PottedSaplingBlockStateModel(bakedPot, saplings, material);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(this.modelLocation);
        }
    }

    public static final ModelState OFFSET_UP = new ModelState() {
        private static final Transformation TRANSFORM = new Transformation(
                new Vector3f(0f, 0.25f, 0f),
                null, null, null
        );

        @Override @NonNull
        public Transformation transformation() {
            return TRANSFORM;
        }
    };

}