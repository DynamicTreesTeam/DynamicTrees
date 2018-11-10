package com.ferreusveritas.dynamictrees.systems.featuregen;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.worldgen.MushroomGenerator;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class FeatureGenHugeMushrooms implements IPostGenFeature {
	
	public FeatureGenHugeMushrooms(Species species) { }
	
	@Override
	public boolean postGeneration(World world, BlockPos rootPos, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, IBlockState initialDirtState) {
		if (endPoints.isEmpty()) return false;

		BlockPos lowest = Collections.min(endPoints, (a, b) -> a.getY() - b.getY());
		
		Random rand = world.rand;
		MushroomGenerator mushGen = new MushroomGenerator();
		
		int success = 0;
		
		if(radius >= 5) {
			for(int tries = 0; tries < 4; tries++) {
				
				float angle = (float) (rand.nextFloat() * Math.PI * 2);
				int xOff = (int) (MathHelper.sin(angle) * (radius - 1));
				int zOff = (int) (MathHelper.cos(angle) * (radius - 1));
				
				BlockPos mushPos = rootPos.add(xOff, 0, zOff);
				
				mushPos = CoordUtils.findGround(world, new BlockPos(mushPos)).up();
				
				if(safeBounds.inBounds(mushPos, true)) {
					int maxHeight = lowest.getY() - mushPos.getY();
					if(maxHeight >= 2) {
						int height = MathHelper.clamp(rand.nextInt(maxHeight) + 3, 3, maxHeight);
						
						if(mushGen.generate(world, rand, mushPos, height, safeBounds)) {
							if(++success >= 2) {
								return true;
							}
						}
					}
				}
			}
		}

		return false;
	}
	
}
