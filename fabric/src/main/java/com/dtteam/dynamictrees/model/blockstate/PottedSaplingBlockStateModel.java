package com.dtteam.dynamictrees.model.blockstate;

import com.dtteam.dynamictrees.block.sapling.PottedSaplingBlockEntity;
import com.dtteam.dynamictrees.model.FabricDynamicBlockStateModel;
import com.dtteam.dynamictrees.model.ModelHelper;
import com.dtteam.dynamictrees.tree.species.Species;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fabric port of the NeoForge {@code PottedSaplingBlockStateModel}; deserialized from blockstate
 * JSONs with type {@code dynamictrees:potted_dynamic_sapling}. Retrieves the potted species from
 * the block entity (NeoForge uses model data instead).
 */
public record PottedSaplingBlockStateModel(
        BlockStateModelPart pot,
        Map<Species, SimpleModelWrapper> saplings,
        Material.Baked particleMaterial
) implements FabricDynamicBlockStateModel {

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return this.pot.materialFlags();
    }

    @Nullable
    private static Species getSpecies(BlockAndTintGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof PottedSaplingBlockEntity pottedSapling
                ? pottedSapling.getSpecies() : null;
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return getSpecies(level, pos);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        Species species = getSpecies(level, pos);
        if (species == null || !species.isValid() || species.getSapling().isEmpty()) return;

        SimpleModelWrapper sapling = saplings.get(species);
        if (sapling == null) return;

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

            for (Species species : Species.REGISTRY) {
                if (species.getSapling().isPresent()) {
                    Identifier saplingModelLocation = species.getSaplingModelLocation();
                    ResolvedModel resolved = modelBaker.getModel(saplingModelLocation);

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
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(this.modelLocation);
            // Also mark the sapling models so bake() can safely resolve them.
            for (Species species : Species.REGISTRY) {
                if (species.getSapling().isPresent()) {
                    resolver.markDependency(species.getSaplingModelLocation());
                }
            }
        }
    }

    public static final ModelState OFFSET_UP = new ModelState() {
        private static final Transformation TRANSFORM = new Transformation(
                new Vector3f(0f, 0.25f, 0f),
                null, null, null
        );

        @Override
        @NotNull
        public Transformation transformation() {
            return TRANSFORM;
        }
    };
}
