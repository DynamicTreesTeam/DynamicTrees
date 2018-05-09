package com.ferreusveritas.dynamictrees.util;

import java.util.Random;

import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class CoordUtils {

	public enum Surround implements IStringSerializable {
		N ("n" , EnumFacing.NORTH),
		NW("nw", EnumFacing.NORTH, EnumFacing.WEST),
		W ("w" , EnumFacing.WEST),
		SW("sw", EnumFacing.SOUTH, EnumFacing.WEST),
		S ("s" , EnumFacing.SOUTH),
		SE("se", EnumFacing.SOUTH, EnumFacing.EAST),
		E ("e" , EnumFacing.EAST),		
		NE("ne", EnumFacing.NORTH, EnumFacing.EAST);

		
		final private String name;
		final private Vec3i offset;
		
		private Surround(String name, EnumFacing ... dirs) {
			this.name = name;
			BlockPos pos = BlockPos.ORIGIN;
			for(EnumFacing d : dirs) {
				pos = pos.add(d.getDirectionVec());
			}
			this.offset = pos;
		}
		
		public String getName() {
			return name;
		}
		
		public Vec3i getOffset() {
			return offset;
		}
		
		public Surround getOpposite() {
			return values()[(ordinal() + 4) & 7];
		}
	}
	
	public static boolean isSurroundedByLoadedChunks(World world, BlockPos pos) {
		for(Surround surr: CoordUtils.Surround.values()) {
			Vec3i dir = surr.getOffset();
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
	public static BlockPos getRayTraceFruitPos(World world, Species species, BlockPos treePos, BlockPos branchPos, SafeChunkBounds safeBounds) {

		RayTraceResult result = branchRayTrace(world, species, treePos, branchPos, 45, 60, 4 + world.rand.nextInt(3), safeBounds);

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
	
	
	public static RayTraceResult branchRayTrace(World world, Species species, BlockPos treePos, BlockPos branchPos, float spreadHor, float spreadVer, float distance, SafeChunkBounds safeBounds) {
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
		
		if(!safeBounds.inBounds(vantagePos, false) || world.isAirBlock(vantagePos)) {//The observing block must be in free space
			RayTraceResult result = rayTraceBlocks(world, vantageVec, branchVec, false, true, false, safeBounds);
			//Beyond here should be safe since the only blocks that can possibly be hit are in loaded chunks
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
	

	/**
	 * I had to import Minecraft's block ray trace algorithm to make it worldgen blocksafe.
	 * I honestly don't know much about what's going on in here because I haven't studied it.
	 * 
	 * If an attempt is made to read a block in an unloaded chunk it will simply return AIR or
	 * the properties of AIR where applicable.
	 * 
	 * @param world
	 * @param vantage
	 * @param lookingAt
	 * @param stopOnLiquid
	 * @param ignoreBlockWithoutBoundingBox
	 * @param returnLastUncollidableBlock
	 * @return
	 */
    public static RayTraceResult rayTraceBlocks(World world, Vec3d vantage, Vec3d lookingAt, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock, SafeChunkBounds safeBounds) {

    	if (!Double.isNaN(vantage.x) && !Double.isNaN(vantage.y) && !Double.isNaN(vantage.z)) {
            if (!Double.isNaN(lookingAt.x) && !Double.isNaN(lookingAt.y) && !Double.isNaN(lookingAt.z)) {
                int vantX = MathHelper.floor(lookingAt.x);
                int vantY = MathHelper.floor(lookingAt.y);
                int vantZ = MathHelper.floor(lookingAt.z);
                int lookX = MathHelper.floor(vantage.x);
                int lookY = MathHelper.floor(vantage.y);
                int lookZ = MathHelper.floor(vantage.z);
                BlockPos lookPos = new BlockPos(lookX, lookY, lookZ);
                IBlockState lookState = safeBounds.inBounds(lookPos, false) ? world.getBlockState(lookPos) : Blocks.AIR.getDefaultState();
                Block block = lookState.getBlock();

                AxisAlignedBB colBB1 = safeBounds.inBounds(lookPos, false) ? lookState.getCollisionBoundingBox(world, lookPos) : Block.NULL_AABB;
                
                if ((!ignoreBlockWithoutBoundingBox || colBB1 != Block.NULL_AABB) && block.canCollideCheck(lookState, stopOnLiquid)) {
                    RayTraceResult raytraceresult = lookState.collisionRayTrace(world, lookPos, vantage, lookingAt);

                    if (raytraceresult != null) {
                        return raytraceresult;
                    }
                }

                RayTraceResult raytraceresult2 = null;
                int ropeLen = 200;

                while (ropeLen-- >= 0) {
                    if (Double.isNaN(vantage.x) || Double.isNaN(vantage.y) || Double.isNaN(vantage.z)) {
                        return null;
                    }

                    if (lookX == vantX && lookY == vantY && lookZ == vantZ) {
                        return returnLastUncollidableBlock ? raytraceresult2 : null;
                    }

                    boolean flagX = true;
                    boolean flagY = true;
                    boolean flagZ = true;
                    double modX = 999.0D;
                    double modY = 999.0D;
                    double modZ = 999.0D;

                    if (vantX > lookX) {
                        modX = (double)lookX + 1.0D;
                    }
                    else if (vantX < lookX) {
                        modX = (double)lookX + 0.0D;
                    }
                    else {
                        flagX = false;
                    }

                    if (vantY > lookY) {
                        modY = (double)lookY + 1.0D;
                    }
                    else if (vantY < lookY) {
                        modY = (double)lookY + 0.0D;
                    }
                    else {
                        flagY = false;
                    }

                    if (vantZ > lookZ) {
                        modZ = (double)lookZ + 1.0D;
                    }
                    else if (vantZ < lookZ) {
                        modZ = (double)lookZ + 0.0D;
                    }
                    else {
                        flagZ = false;
                    }

                    double unkX = 999.0D;
                    double unkY = 999.0D;
                    double unkZ = 999.0D;
                    double deltaX = lookingAt.x - vantage.x;
                    double deltaY = lookingAt.y - vantage.y;
                    double deltaZ = lookingAt.z - vantage.z;

                    if (flagX) {
                        unkX = (modX - vantage.x) / deltaX;
                    }
                    if (flagY) {
                        unkY = (modY - vantage.y) / deltaY;
                    }
                    if (flagZ) {
                        unkZ = (modZ - vantage.z) / deltaZ;
                    }
                    if (unkX == -0.0D) {
                        unkX = -1.0E-4D;
                    }
                    if (unkY == -0.0D) {
                        unkY = -1.0E-4D;
                    }
                    if (unkZ == -0.0D) {
                        unkZ = -1.0E-4D;
                    }

                    EnumFacing enumfacing;

                    if (unkX < unkY && unkX < unkZ) {
                        enumfacing = vantX > lookX ? EnumFacing.WEST : EnumFacing.EAST;
                        vantage = new Vec3d(modX, vantage.y + deltaY * unkX, vantage.z + deltaZ * unkX);
                    }
                    else if (unkY < unkZ) {
                        enumfacing = vantY > lookY ? EnumFacing.DOWN : EnumFacing.UP;
                        vantage = new Vec3d(vantage.x + deltaX * unkY, modY, vantage.z + deltaZ * unkY);
                    }
                    else {
                        enumfacing = vantZ > lookZ ? EnumFacing.NORTH : EnumFacing.SOUTH;
                        vantage = new Vec3d(vantage.x + deltaX * unkZ, vantage.y + deltaY * unkZ, modZ);
                    }

                    lookX = MathHelper.floor(vantage.x) - (enumfacing == EnumFacing.EAST ? 1 : 0);
                    lookY = MathHelper.floor(vantage.y) - (enumfacing == EnumFacing.UP ? 1 : 0);
                    lookZ = MathHelper.floor(vantage.z) - (enumfacing == EnumFacing.SOUTH ? 1 : 0);
                    lookPos = new BlockPos(lookX, lookY, lookZ);
                    IBlockState iblockstate1 = safeBounds.inBounds(lookPos, false) ? world.getBlockState(lookPos) : Blocks.AIR.getDefaultState();
                    Block block1 = iblockstate1.getBlock();

                    AxisAlignedBB colBB2 = safeBounds.inBounds(lookPos, false) ? iblockstate1.getCollisionBoundingBox(world, lookPos) : Block.NULL_AABB;
                    
                    if (!ignoreBlockWithoutBoundingBox || iblockstate1.getMaterial() == Material.PORTAL || colBB2 != Block.NULL_AABB) {
                        if (block1.canCollideCheck(iblockstate1, stopOnLiquid)) {
                            RayTraceResult raytraceresult1 = iblockstate1.collisionRayTrace(world, lookPos, vantage, lookingAt);
                            if (raytraceresult1 != null) {
                                return raytraceresult1;
                            }
                        }
                        else {
                            raytraceresult2 = new RayTraceResult(RayTraceResult.Type.MISS, vantage, enumfacing, lookPos);
                        }
                    }
                }

                return returnLastUncollidableBlock ? raytraceresult2 : null;
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }

	
	public static BlockPos findGround(World world, BlockPos startPos) {
		MutableBlockPos pos = new MutableBlockPos(startPos);
		
		//Rise up until we are no longer in a solid block
		while(world.getBlockState(pos).isFullCube()) {
			pos.setPos(pos.getX(), pos.getY() + 1, pos.getZ());
		}
		//Dive down until we are again
		while(!world.getBlockState(pos).isFullCube() && pos.getY() > 50) {
			pos.setPos(pos.getX(), pos.getY() - 1, pos.getZ());
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
