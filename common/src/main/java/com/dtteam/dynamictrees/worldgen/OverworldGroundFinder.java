package com.dtteam.dynamictrees.worldgen;

import com.dtteam.dynamictrees.api.worldgen.GroundFinder;
import com.dtteam.dynamictrees.utility.CoordUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Harley O'Connor
 */
public final class OverworldGroundFinder implements GroundFinder {

    public List<BlockPos> findGround(LevelAccessor level, BlockPos start, @Nullable Heightmap.Types heightmap) {
		//We start of by getting the surface ground
		LinkedList<BlockPos> surfaceGround = new LinkedList<>(SURFACE.findGround(level, start, heightmap));
		BlockPos surfaceBlock = surfaceGround.getFirst();
		//Then we do a very sparse check to find underground biomes
		final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(start.getX(), 0, start.getZ());
		boolean caveBiomeFound = false;
		while (CoordUtils.inRange(pos, level.getMinY(), surfaceBlock.getY())) {
			if (level.getBiome(pos).is(TagKey.create(Registries.BIOME, Identifier.parse("c:is_underground")))){
				caveBiomeFound = true;
				break;
			}
			pos.move(0,-10,0);
		}
		//If underground biomes are present, we want to include them
		if (caveBiomeFound){
			List<BlockPos> subterraneanGround = SUBTERRANEAN.findGround(level, start, heightmap);
			surfaceGround.addAll(subterraneanGround);
			return new LinkedList<>(surfaceGround);
		}
		return surfaceGround;
    }

}
