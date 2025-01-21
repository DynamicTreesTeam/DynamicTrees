package com.dtteam.dynamictrees.worldgen;

import com.dtteam.dynamictrees.api.worldgen.GroundFinder;
import com.dtteam.dynamictrees.utility.helper.CoordUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * @author Harley O'Connor
 */
public final class SurfaceGroundFinder implements GroundFinder {

    @Override
    public List<BlockPos> findGround(LevelAccessor level, BlockPos start, @Nullable Heightmap.Types heightmap) {
    	if (heightmap == null) {
    		return Collections.singletonList(CoordUtils.findWorldSurface(level, start, true));
    	} else {
    		return Collections.singletonList(CoordUtils.findWorldSurface(level, start, heightmap));    		
    	}
    }

}
