package com.ferreusveritas.dynamictrees.api.worldgen;

import com.ferreusveritas.dynamictrees.worldgen.OverworldGroundFinder;
import com.ferreusveritas.dynamictrees.worldgen.SubterraneanGroundFinder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;

import java.util.List;

/**
 * Implementations will find a suitable area to generate a tree on the ground.
 */
@FunctionalInterface
public interface GroundFinder {

    GroundFinder OVERWORLD = new OverworldGroundFinder();
    GroundFinder SUBTERRANEAN = new SubterraneanGroundFinder();

    /**
     * Finds the {@link BlockPos} of the first ground block for the y-column of the start {@link BlockPos} given.
     *
     * @param start the {@link BlockPos} to start from
     * @return the {@link BlockPos} of the first ground block
     */
    List<BlockPos> findGround(LevelAccessor level, BlockPos start);

}
