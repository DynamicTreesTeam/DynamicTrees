package com.ferreusveritas.dynamictrees.cells;

import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;

import net.minecraft.util.math.BlockPos;

/**
 * A voxelmap of a leaf cluster for a species.  Values represent hydration value.
 * This leaf cluster map is "stamped" on to each branch end during worldgen.  Should be
 * representative of what the species actually produces with cell automata.
 */
public class LeafClusters {

	public static final SimpleVoxmap NULL_MAP = new SimpleVoxmap(1, 1, 1, new byte[] { 0 });
	
	public static final SimpleVoxmap DECIDUOUS = new SimpleVoxmap(5, 4, 5, new byte[] {
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
	
	
	public static final SimpleVoxmap CONIFER = new SimpleVoxmap(5, 2, 5, new byte[] {
			
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
	
	
	public static final SimpleVoxmap ACACIA = new SimpleVoxmap(7, 2, 7, new byte[] {
			
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
	
	
	public static final SimpleVoxmap DARK_OAK = new SimpleVoxmap(7, 5, 7, new byte[] {
			
			//Layer 0(Bottom)
			0, 0, 0, 0, 0, 0, 0,
			0, 0, 1, 1, 1, 0, 0,
			0, 1, 0, 0, 0, 1, 0,
			0, 1, 0, 0, 0, 1, 0,
			0, 1, 0, 0, 0, 1, 0,
			0, 0, 1, 1, 1, 0, 0,
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
	
	
	public static final SimpleVoxmap BARE = new SimpleVoxmap(1, 1, 1, new byte[] { 0x20 });
	
	public static final SimpleVoxmap PALM = new SimpleVoxmap(3, 3, 3, new byte[] {
			
			//Layer 0(Bottom)
			0, 0, 0,
			0, 0, 0,
			0, 0, 0,
			
			//Layer 1(Middle)
			0, 0, 0,
			0, 4, 0,
			0, 0, 0,
			
			//Layer 2 (Top)
			1, 2, 1,
			2, 3, 2,
			1, 2, 1
			
	}).setCenter(new BlockPos(3, 0, 3));
	
	public static final SimpleVoxmap BUSH = new SimpleVoxmap(5, 2, 5, new byte[] {
			0, 1, 1, 1, 0,
			1, 2, 3, 2, 1,
			1, 3, 0, 3, 1,
			1, 2, 3, 2, 1,
			0, 1, 1, 1, 0,
			
			0, 0, 0, 0, 0,
			0, 1, 1, 1, 0,
			0, 1, 1, 1, 0,
			0, 1, 1, 1, 0,
			0, 0, 0, 0, 0,
	}).setCenter(new BlockPos(2, 0, 2));

	public static final SimpleVoxmap NETHER_FUNGUS = new SimpleVoxmap(3, 6, 3, new byte[] {

			//Layer 0(Droop Bottom)
			1, 0, 1,
			0, 0, 0,
			1, 0, 1,

			//Layer 1(Droop Middle)
			2, 0, 2,
			0, 0, 0,
			2, 0, 2,

			//Layer 2(Droop Top)
			3, 0, 3,
			0, 0, 0,
			3, 0, 3,

			//Layer 3(Cap Bottom)
			5, 6, 5,
			6, 7, 6,
			5, 6, 5,

			//Layer 4(Cap Middle)
			6, 7, 6,
			7, 0, 7,
			6, 7, 6,

			//Layer 5 (Cap Top)
			5, 6, 5,
			6, 7, 6,
			5, 6, 5

	}).setCenter(new BlockPos(1, 4, 1));
}
