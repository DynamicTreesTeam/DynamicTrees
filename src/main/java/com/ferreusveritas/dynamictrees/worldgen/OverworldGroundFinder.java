package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.api.worldgen.GroundFinder;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;

import java.util.Collections;
import java.util.List;

/**
 * @author Harley O'Connor
 */
public final class OverworldGroundFinder implements GroundFinder {

    @Override
    public List<BlockPos> findGround(WorldGenLevel world, BlockPos start) {
        return Collections.singletonList(CoordUtils.findWorldSurface(world, start, true));
    }

}
