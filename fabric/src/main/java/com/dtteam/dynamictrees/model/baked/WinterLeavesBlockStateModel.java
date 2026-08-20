package com.dtteam.dynamictrees.model.baked;

import com.dtteam.dynamictrees.systems.season.SeasonHelper;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Predicate;

/**
 * Swaps perishable dynamic leaves for the winter overlay when a season provider reports winter.
 */
public class WinterLeavesBlockStateModel implements BlockStateModel, FabricBlockStateModel {

    private final BlockStateModel summer;
    private final BlockStateModelPart winter;

    public WinterLeavesBlockStateModel(BlockStateModel summer, BlockStateModelPart winter) {
        this.summer = summer;
        this.winter = winter;
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return isWinter(pos);
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
        summer.collectParts(random, output);
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state,
                          RandomSource random, Predicate<Direction> cullTest) {
        if (isWinter(pos)) {
            emitPart(emitter, winter, cullTest);
            return;
        }
        if (summer instanceof FabricBlockStateModel fabricSummer) {
            fabricSummer.emitQuads(emitter, level, pos, state, random, cullTest);
            return;
        }
        List<BlockStateModelPart> parts = new java.util.ArrayList<>();
        summer.collectParts(random, parts);
        for (BlockStateModelPart part : parts) {
            emitPart(emitter, part, cullTest);
        }
    }

    private static void emitPart(QuadEmitter emitter, BlockStateModelPart part, Predicate<Direction> cullTest) {
        Iterable<BakedQuad> quads = part instanceof SimpleModelWrapper wrapper
                ? wrapper.quads().getAll()
                : allFaces(part);
        for (BakedQuad quad : quads) {
            if (cullTest.test(quad.direction())) {
                continue;
            }
            emitter.fromBakedQuad(quad);
            emitter.emit();
        }
    }

    private static List<BakedQuad> allFaces(BlockStateModelPart part) {
        List<BakedQuad> quads = new java.util.ArrayList<>();
        for (Direction face : Direction.values()) {
            quads.addAll(part.getQuads(face));
        }
        return quads;
    }

    static boolean isWinter(BlockPos pos) {
        Level clientLevel = Minecraft.getInstance().level;
        if (clientLevel == null) {
            return false;
        }
        Float season = SeasonHelper.getSeasonValue(clientLevel, pos);
        return season != null && SeasonHelper.isSeasonBetween(season, SeasonHelper.WINTER_START, SeasonHelper.SPRING_START);
    }

    @Override
    public Material.Baked particleMaterial() {
        return summer.particleMaterial();
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        if (isWinter(pos)) {
            return winter.particleMaterial();
        }
        return summer instanceof FabricBlockStateModel fabricSummer
                ? fabricSummer.particleMaterial(level, pos, state)
                : summer.particleMaterial();
    }

    @Override
    public int materialFlags() {
        return summer.materialFlags();
    }
}
