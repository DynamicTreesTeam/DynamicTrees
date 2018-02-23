package com.ferreusveritas.dynamictrees.util;

import java.util.Random;

import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
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
	
	public static boolean isSurroundedByLoadedChunks(World world, BlockPos pos) {
		for(Vec3i dir: CoordUtils.surround) {
			if(world.getChunkProvider().getLoadedChunk((pos.getX() >> 4) + dir.getX(), (pos.getZ() >> 4) + dir.getZ()) == null ){
				return false;
			}
		}
		
		return true;
	}
	
	public static EnumFacing getRandomDir(Random rand) {
		return EnumFacing.getFront(2 + rand.nextInt(4));//Return NSWE
	}
	
	/**
	 * Find a suitable position for seed drops or fruit placement using ray tracing.
	 * 
	 * @param world The world
	 * @param treePos The block position of the {@link TreeFamily} trunk base.
	 * @param branchPos The {@link BlockPos} of a {@link BlockBranch} selected as a fruit target
	 * @return The {@link BlockPos} of a suitable location.  The block is always air if successful otherwise it is {@link BlockPos.ORIGIN}
	 */
	public static BlockPos getRayTraceFruitPos(World world, Species species, BlockPos treePos, BlockPos branchPos) {

		RayTraceResult result = branchRayTrace(world, species, treePos, branchPos, 45, 60, 4 + world.rand.nextInt(3));

		if(result != null) {
			BlockPos hitPos = result.getBlockPos();
			if(hitPos != BlockPos.ORIGIN) {
				do { //Run straight down until we hit a block that's non compatible leaves.
					hitPos = hitPos.down();
				} while(species.getFamily().isCompatibleGenericLeaves(world.getBlockState(hitPos), world, hitPos));
				if(world.isAirBlock(hitPos)) {//If that block is air then we have a winner.
					return hitPos;
				}
			}
		}

		return BlockPos.ORIGIN;
	}
	
	
	public static RayTraceResult branchRayTrace(World world, Species species, BlockPos treePos, BlockPos branchPos, float spreadHor, float spreadVer, float distance) {
		treePos = new BlockPos(treePos.getX(), branchPos.getY(), treePos.getZ());//Make the tree pos level with the branch pos

		Vec3d vOut = new Vec3d(branchPos.getX() - treePos.getX(), 0, branchPos.getZ() - treePos.getZ());
		
		if(vOut.equals(Vec3d.ZERO)) {
			vOut = new Vec3d(1, 0, 0);
			spreadHor = 180;
		}
		
		float deltaYaw = (world.rand.nextFloat() * spreadHor * 2) - spreadHor;
		float deltaPitch = (world.rand.nextFloat() * -spreadVer);// must be greater than -90 degrees(and less than 90) for the tangent function.
		vOut = vOut.normalize(). //Normalize to unit vector
				addVector(0, Math.tan(Math.toRadians(deltaPitch)), 0). //Pitch the angle downward by 0 to spreadVer degrees
				normalize(). //Re-normalize to unit vector
				rotateYaw((float) Math.toRadians(deltaYaw)). //Vary the yaw by +/- spreadHor
				scale(distance); //Vary the view distance
		
		Vec3d branchVec = new Vec3d(branchPos).addVector(0.5, 0.5, 0.5);//Get the vector of the middle of the branch block
		Vec3d vantageVec = branchVec.add(vOut);//Make a vantage point to look at the branch
		BlockPos vantagePos = new BlockPos(vantageVec);//Convert Vector to BlockPos for testing

		if(world.isAirBlock(vantagePos)) {//The observing block must be in free space
			RayTraceResult result = world.rayTraceBlocks(vantageVec, branchVec, false, true, false);

			if(result != null) {
				BlockPos hitPos = result.getBlockPos();
				if(result.typeOfHit == RayTraceResult.Type.BLOCK && hitPos != BlockPos.ORIGIN) {//We found a block
					if(species.getFamily().isCompatibleGenericLeaves(world.getBlockState(hitPos), world, hitPos)) {//Test if it's the right kind of leaves for the species
						return result;
					}
				}
			}
		}
		
		return null;
	}
	
	public static BlockPos findGround(World world, BlockPos pos) {
		//Rise up until we are no longer in a solid block
		while(world.getBlockState(pos).isFullCube()) {
			pos = pos.up();
		}
		//Dive down until we are again
		while(!world.getBlockState(pos).isFullCube() && pos.getY() > 50) {
			pos = pos.down();
		}
		return pos;
	}

	//Some ready made not terrible prime hash factors
	private static final int coordHashMap[][] = {
			{4111, 271, 3067},
			{7933711, 6144389, 9538033},
			{9973, 8287, 9721},
			{7211, 5437, 9613}
	};
	
	public static int coordHashCode(BlockPos pos, int a, int b, int c) {
		int hash = (pos.getX() * a ^ pos.getY() * b ^ pos.getZ() * c) >> 1;
		return hash & 0xFFFF;
	}
	
	public static int coordHashCode(BlockPos pos, int readyMade) {
		int factors[] = coordHashMap[readyMade & 3];
		return coordHashCode(pos, factors[0], factors[1], factors[2]);
	}
	
}
