package com.ferreusveritas.dynamictrees.systems.featuregen;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.ferreusveritas.dynamictrees.api.IGenFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.worldgen.MushroomGenerator;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class FeatureGenHugeMushrooms implements IGenFeature {

	private int radius = 2;
	
	public FeatureGenHugeMushrooms(Species species) { }
	
	public FeatureGenHugeMushrooms setRadius(int radius) {
		this.radius = radius;
		return this;
	}
	
	@Override
	public void gen(World world, BlockPos treePos, List<BlockPos> endPoints, SafeChunkBounds safeBounds) {
		BlockPos lowest = Collections.min(endPoints, (a, b) -> a.getY() - b.getY());
		
		Random rand = world.rand;
		MushroomGenerator mushGen = new MushroomGenerator();
		
		int success = 0;
		
		if(radius >= 5) {
			for(int tries = 0; tries < 4; tries++) {
				
				float angle = (float) (rand.nextFloat() * Math.PI * 2);
				int xOff = (int) (MathHelper.sin(angle) * (radius - 1));
				int zOff = (int) (MathHelper.cos(angle) * (radius - 1));
				
				BlockPos mushPos = treePos.add(xOff, 0, zOff);
				
				mushPos = CoordUtils.findGround(world, new BlockPos(mushPos)).up();
				
				if(safeBounds.inBounds(mushPos, true)) {
					int maxHeight = lowest.getY() - mushPos.getY();
					if(maxHeight >= 2) {
						int height = MathHelper.clamp(rand.nextInt(maxHeight) + 3, 3, maxHeight);
						
						if(mushGen.generate(world, rand, mushPos, height, safeBounds)) {
							if(++success >= 2) {
								return;
							}
						}
					}
				}
			}
		}

	}
	
}
