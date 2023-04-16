package com.ferreusveritas.dynamictrees.api.worldgen;

import com.ferreusveritas.dynamictrees.worldgen.OverworldGroundFinder;
import com.ferreusveritas.dynamictrees.worldgen.SubterraneanGroundFinder;
import com.google.common.collect.Maps;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementations will find a suitable area to generate a tree on the ground.
 */
@FunctionalInterface
public interface GroundFinder {

    GroundFinder OVERWORLD = new OverworldGroundFinder();
    GroundFinder SUBTERRANEAN = new SubterraneanGroundFinder();

    /**
     * If this is not set manually, the ground finder returned will be {@link #SUBTERRANEAN} if {@link DimensionType#hasCeiling()}
     * returns {@code true} or {@link #OVERWORLD} if {@code false}.
     */
    Map<ResourceKey<Level>, GroundFinder> GROUND_FINDERS = new HashMap<>();

    /**
     * Finds the {@link BlockPos} of the first ground block for the y-column of the start {@link BlockPos} given.
     *
     * @param start the {@link BlockPos} to start from
     * @return the {@link BlockPos} of the first ground block
     */
    List<BlockPos> findGround(LevelAccessor level, BlockPos start, Heightmap.Types heightmap);

    static void registerGroundFinder(ResourceKey<Level> dimension, GroundFinder groundFinder) {
        GROUND_FINDERS.put(dimension, groundFinder);
    }

    static GroundFinder getGroundFinder(Level level) {
        return GROUND_FINDERS.computeIfAbsent(level.dimension(), k ->
                level.dimensionType().hasCeiling() ? SUBTERRANEAN : OVERWORLD
        );
    }

}
