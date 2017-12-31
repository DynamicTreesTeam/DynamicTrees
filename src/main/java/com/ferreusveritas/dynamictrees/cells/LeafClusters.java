package com.ferreusveritas.dynamictrees.cells;

import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;

import net.minecraft.util.math.BlockPos;

/**
 * A voxelmap of a leaf cluster for a species.  Values represent hydration value.
 * This leaf cluster map is "stamped" on to each branch end during worldgen.  Should be
 * representative of what the species actually produces with cell automata.
 */
public class LeafClusters {

	public static final SimpleVoxmap deciduous = new SimpleVoxmap(5, 4, 5, new byte[] {
			//Layer 0 (Bottom)
			0, 0, 0, 0, 0,
			0, 1, 1, 1, 0,
			0, 1, 1, 1, 0,
			0, 1, 1, 1, 0,
			0, 0, 0, 0, 0,

			//Layer 1
			0, 1, 1, 1, 0,
			1, 3, 4, 3, 1,
			1, 4, 0, 4, 1,
			1, 3, 4, 3, 1,
			0, 1, 1, 1, 0,
			
			//Layer 2
			0, 1, 1, 1, 0,
			1, 2, 3, 2, 1,
			1, 3, 4, 3, 1,
			1, 2, 3, 2, 1,
			0, 1, 1, 1, 0,
			
			//Layer 3(Top)
			0, 0, 0, 0, 0,
			0, 1, 1, 1, 0,
			0, 1, 1, 1, 0,
			0, 1, 1, 1, 0,
			0, 0, 0, 0, 0,
			
	}).setCenter(new BlockPos(2, 1, 2));
	
	
	public static final SimpleVoxmap conifer = new SimpleVoxmap(5, 2, 5, new byte[] {
			
			//Layer 0(Bottom)
			0, 0, 1, 0, 0,
			0, 1, 2, 1, 0,
			1, 2, 0, 2, 1,
			0, 1, 2, 1, 0,
			0, 0, 1, 0, 0,
			
			//Layer 1 (Top)
			0, 0, 0, 0, 0,
			0, 0, 1, 0, 0,
			0, 1, 1, 1, 0,
			0, 0, 1, 0, 0,
			0, 0, 0, 0, 0
			
	}).setCenter(new BlockPos(2, 0, 2));
	
	
	public static final SimpleVoxmap acacia = new SimpleVoxmap(7, 2, 7, new byte[] {
			
			//Layer 0(Bottom)
			0, 0, 1, 1, 1, 0, 0,
			0, 1, 2, 2, 2, 1, 0,
			1, 2, 3, 4, 3, 2, 1,
			1, 2, 4, 0, 4, 2, 1,
			1, 2, 3, 4, 3, 2, 1,
			0, 1, 2, 2, 2, 1, 0,
			0, 0, 1, 1, 1, 0, 0,
			
			//Layer 1 (Top)
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 1, 1, 1, 0, 0,
			0, 1, 2, 2, 2, 1, 0,
			0, 1, 2, 2, 2, 1, 0,
			0, 1, 2, 2, 2, 1, 0,
			0, 0, 1, 1, 1, 0, 0,
			0, 0, 0, 0, 0, 0, 0
			
	}).setCenter(new BlockPos(3, 0, 3));
	
	
	public static final SimpleVoxmap darkoak = new SimpleVoxmap(7, 5, 7, new byte[] {
			
			//Layer 0(Bottom)
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 2, 2, 2, 0, 0,
			0, 2, 0, 0, 0, 2, 0,
			0, 2, 0, 0, 0, 2, 0,
			0, 2, 0, 0, 0, 2, 0,
			0, 0, 2, 2, 2, 0, 0,
			0, 0, 0, 0, 0, 0, 0,
			
			//Layer 1
			0, 0, 1, 1, 1, 0, 0,
			0, 1, 2, 2, 2, 1, 0,
			1, 2, 3, 4, 3, 2, 1,
			1, 2, 4, 0, 4, 2, 1,
			1, 2, 3, 4, 3, 2, 1,
			0, 1, 2, 2, 2, 1, 0,
			0, 0, 1, 1, 1, 0, 0,
			
			//Layer 2
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 1, 1, 1, 0, 0,
			0, 1, 2, 2, 2, 1, 0,
			0, 1, 2, 4, 2, 1, 0,
			0, 1, 2, 2, 2, 1, 0,
			0, 0, 1, 1, 1, 0, 0,
			0, 0, 0, 0, 0, 0, 0,
			
			//Layer 3
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 1, 1, 1, 0, 0,
			0, 0, 1, 2, 1, 0, 0,
			0, 0, 1, 1, 1, 0, 0,
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0,
			
			//Layer 4 (Top)
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 1, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0
			
		}).setCenter(new BlockPos(3, 1, 3));
	
}
