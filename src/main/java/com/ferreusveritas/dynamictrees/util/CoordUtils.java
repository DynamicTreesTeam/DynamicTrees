package com.ferreusveritas.dynamictrees.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class CoordUtils {

	public static final Vec3i[] surround = {
			new Vec3i( 0, 0,-1),//N
			new Vec3i( 0, 0, 1),//S
			new Vec3i(-1, 0, 0),//W
			new Vec3i( 1, 0, 0),//E
			new Vec3i(-1, 0,-1),//NW
			new Vec3i( 1, 0,-1),//NE
			new Vec3i(-1, 0, 1),//SW
			new Vec3i( 1, 0, 1) //SE
		};
	
	public static boolean isSurroundedByExistingChunks(World world, BlockPos pos) {
		for(Vec3i dir: CoordUtils.surround) {
			if(world.getChunkProvider().getLoadedChunk((pos.getX() >> 4) + dir.getX(), (pos.getZ() >> 4) + dir.getZ()) == null ){
				return false;
			}
		}
		
		return true;
	}
}
