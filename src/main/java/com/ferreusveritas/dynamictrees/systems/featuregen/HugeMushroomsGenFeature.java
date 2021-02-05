package com.ferreusveritas.dynamictrees.systems.featuregen;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Used to add mushrooms under a tree canopy.  Currently used by dark oaks
 * for roofed forests.
 * 
 * @author ferreusveritas
 */
public class HugeMushroomsGenFeature implements IPostGenFeature {
	
	protected final HugeMushroomGenFeature mushGen;
	protected int maxShrooms = 2;
	protected int maxAttempts = 4;
	
	/** Use this for a custom mushroom generator */
	public HugeMushroomsGenFeature(HugeMushroomGenFeature mushGen) {
		this.mushGen = mushGen;
	}
	
	/** Use this for the default mushroom generator */
	public HugeMushroomsGenFeature() {
		this(new HugeMushroomGenFeature());
	}
	
	public HugeMushroomsGenFeature setMaxShrooms(int max) {
		this.maxShrooms = max;
		return this;
	}
	
	public HugeMushroomsGenFeature setMaxAttempts(int max) {
		this.maxAttempts = max;
		return this;
	}
	
	@Override
	public boolean postGeneration(IWorld world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState, float seasonValue, float seasonFruitProductionFactor) {
		boolean worldGen = safeBounds != SafeChunkBounds.ANY;
		
		if (endPoints.isEmpty() || !worldGen) return false;
	
		BlockPos lowest = Collections.min(endPoints, (a, b) -> a.getY() - b.getY());
		
		Random rand = world.getRandom();
		
		int success = 0;
		
		if(radius >= 5) {
			for(int tries = 0; tries < maxAttempts; tries++) {
				
				float angle = (float) (rand.nextFloat() * Math.PI * 2);
				int xOff = (int) (MathHelper.sin(angle) * (radius - 1));
				int zOff = (int) (MathHelper.cos(angle) * (radius - 1));
				
				BlockPos mushPos = rootPos.add(xOff, 0, zOff);
				
				mushPos = CoordUtils.findGround(world, new BlockPos(mushPos)).up();
				
				if(safeBounds.inBounds(mushPos, true)) {
					int maxHeight = lowest.getY() - mushPos.getY();
					if(maxHeight >= 2) {
						int height = MathHelper.clamp(rand.nextInt(maxHeight) + 3, 3, maxHeight);
						
						if(mushGen.setHeight(height).generate(world, mushPos.down(), species, biome, rand, radius, safeBounds)) {
							if(++success >= maxShrooms) {
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
