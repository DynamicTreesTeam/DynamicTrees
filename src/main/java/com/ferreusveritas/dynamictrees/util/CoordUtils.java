package com.ferreusveritas.dynamictrees.util;

import java.util.Random;

import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;

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
			if(world.getChunkProvider().chunkExists((pos.getX() >> 4) + dir.getX(), (pos.getZ() >> 4) + dir.getZ())){
				return false;
			}
		}
		
		return true;
	}
	
	public static EnumFacing getRandomDir(Random rand) {
		return EnumFacing.getFront(2 + rand.nextInt(4));//Return NSWE
	}
}
