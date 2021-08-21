package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.api.worldgen.GroundFinder;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;

import java.util.Collections;
import java.util.List;

/**
 * @author Harley O'Connor
 */
public final class OverworldGroundFinder implements GroundFinder {

    @Override
    public List<BlockPos> findGround(ISeedReader world, BlockPos start) {
        return Collections.singletonList(CoordUtils.findWorldSurface(world, start, true));
    }

}
