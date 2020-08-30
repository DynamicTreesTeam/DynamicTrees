package com.ferreusveritas.dynamictrees.api.worldgen;

import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IGroundFinder {
	BlockPos findGround(BiomeDataBase.BiomeEntry biomeEntry, World world, BlockPos start);
}
