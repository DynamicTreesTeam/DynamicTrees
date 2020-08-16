package com.ferreusveritas.dynamictrees.api.worldgen;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeManager.BiomeEntry;

public interface IGroundFinder {
	BlockPos findGround(BiomeEntry biomeEntry, World world, BlockPos start);
}
