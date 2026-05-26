package com.dtteam.dynamictrees.model.blockstate;

import com.dtteam.dynamictrees.model.ModelHelper;
import com.dtteam.dynamictrees.systems.season.SeasonHelper;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

import java.util.List;

public record WinterLeavesBlockStateModel(
        BlockStateModelPart leaves,
        BlockStateModelPart winterLeaves
) implements DynamicBlockStateModel {

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return isWinter(pos);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        if (isWinter(pos)){
            parts.add(winterLeaves);
        } else {
            parts.add(leaves);
        }

    }

    private static boolean isWinter(BlockPos pos) {
        Level clientLevel = Minecraft.getInstance().level;
        return clientLevel != null && SeasonHelper.isSeasonBetween(SeasonHelper.getSeasonValue(clientLevel, pos), SeasonHelper.WINTER_START, SeasonHelper.SPRING_START);
    }

    public record Unbaked(Identifier modelLocation, Identifier winterModel) implements CustomUnbakedBlockStateModel {

        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Identifier.CODEC.fieldOf("model").forGetter(Unbaked::modelLocation),
                Identifier.CODEC.fieldOf("winter_model").forGetter(Unbaked::winterModel)
        ).apply(i, Unbaked::new));

        @Override
        public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
            return CODEC;
        }

        @Override
        public BlockStateModel bake(ModelBaker modelBaker) {
            ResolvedModel leaves = modelBaker.getModel(modelLocation);
            TextureSlots leavesTextures = leaves.getTopTextureSlots();
            Material.Baked leavesMaterial = leaves.resolveParticleMaterial(leavesTextures, modelBaker);

            SimpleModelWrapper bakedLeaves = new SimpleModelWrapper(
                    leaves.bakeTopGeometry(leavesTextures, modelBaker, ModelHelper.noState()),
                    leaves.getTopAmbientOcclusion(),
                    leavesMaterial
            );

            ResolvedModel winterLeaves = modelBaker.getModel(winterModel);
            TextureSlots winterTextures = winterLeaves.getTopTextureSlots();
            Material.Baked winterMaterial = winterLeaves.resolveParticleMaterial(winterTextures, modelBaker);

            SimpleModelWrapper bakedWinterLeaves = new SimpleModelWrapper(
                    winterLeaves.bakeTopGeometry(winterTextures, modelBaker, ModelHelper.noState()),
                    winterLeaves.getTopAmbientOcclusion(),
                    winterMaterial
            );

            return new WinterLeavesBlockStateModel(bakedLeaves, bakedWinterLeaves);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(this.modelLocation);
            resolver.markDependency(this.winterModel);
        }
    }

    @Override
    public Material.Baked particleMaterial() {
        return leaves.particleMaterial();
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return leaves.materialFlags();
    }


}