package com.ferreusveritas.dynamictrees.api.worldgen;

import com.ferreusveritas.dynamictrees.worldgen.BiomeDatabase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;

public interface IGroundFinder {
	BlockPos findGround(BiomeDatabase.Entry entry, ISeedReader world, BlockPos start);
}
