package com.ferreusveritas.dynamictrees.systems.featuregen;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

/**
 * Used to add mushrooms under a tree canopy.  Currently used by dark oaks
 * for roofed forests.
 * 
 * @author ferreusveritas
 */
public class FeatureGenHugeMushrooms implements IPostGenFeature {
	
	protected final FeatureGenHugeMushroom mushGen;
	
	/** Use this for a custom mushroom generator */
	public FeatureGenHugeMushrooms(Species species, FeatureGenHugeMushroom mushGen) {
		this.mushGen = mushGen;
	}
	
	/** Use this for the default mushroom generator */
	public FeatureGenHugeMushrooms(Species species) {
		this(species, new FeatureGenHugeMushroom() );
	}
	
	@Override
	public boolean postGeneration(World world, BlockPos rootPos, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, IBlockState initialDirtState) {
		boolean worldGen = safeBounds != SafeChunkBounds.ANY;
		
		if (endPoints.isEmpty() && !worldGen) return false;
	
		BlockPos lowest = Collections.min(endPoints, (a, b) -> a.getY() - b.getY());
		
		Random rand = world.rand;
		
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
						
						if(mushGen.setHeight(height).generate(world, mushPos.down(), biome, rand, radius, safeBounds)) {
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
