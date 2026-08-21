package com.dtteam.dynamictrees.model.baked;

import com.dtteam.dynamictrees.systems.season.SeasonHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Swaps perishable dynamic leaves for the winter overlay when a season provider reports winter.
 */
public class WinterLeavesBlockStateModel implements BlockStateModel {

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
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        if (isWinter(pos)) {
            parts.add(winter);
            return;
        }
        summer.collectParts(level, pos, state, random, parts);
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
        return isWinter(pos) ? winter.particleMaterial() : summer.particleMaterial(level, pos, state);
    }

    @Override
    public int materialFlags() {
        return summer.materialFlags();
    }
}
