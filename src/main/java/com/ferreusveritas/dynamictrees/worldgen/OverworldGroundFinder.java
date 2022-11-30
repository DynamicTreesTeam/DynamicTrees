package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.api.worldgen.GroundFinder;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

import java.util.Collections;
import java.util.List;

/**
 * @author Harley O'Connor
 */
public final class OverworldGroundFinder implements GroundFinder {

    @Override
    public List<BlockPos> findGround(LevelAccessor level, BlockPos start) {
        return Collections.singletonList(CoordUtils.findWorldSurface(level, start, true));
    }

}
