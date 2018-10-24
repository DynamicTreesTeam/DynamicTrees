package com.ferreusveritas.dynamictrees.worldgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.api.worldgen.IPoissonDiscProvider;
import com.ferreusveritas.dynamictrees.api.worldgen.IRadiusCoordinator;
import com.ferreusveritas.dynamictrees.util.PoissonDisc;
import com.ferreusveritas.dynamictrees.util.RandomXOR;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.Vec2i;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

/**
 * Manages and creates all the Poisson discs in a single dimension.
 * 
 * @author ferreusveritas
 */
public class PoissonDiscProviderWorld implements IPoissonDiscProvider {
	
	private IRadiusCoordinator radiusCoordinator;
	private HashMap<ChunkPos, PoissonDiscChunkSet> chunkCircles;
	private final RandomXOR random = new RandomXOR();
	
	public PoissonDiscProviderWorld(IRadiusCoordinator radCoord) {
		chunkCircles = new HashMap<ChunkPos, PoissonDiscChunkSet>();
		radiusCoordinator = radCoord;
	}
	
	@Override
	public synchronized List<PoissonDisc> getPoissonDiscs(World world, int chunkX, int chunkY, int chunkZ) {
		random.setXOR(new BlockPos(chunkX, chunkY, chunkZ));
		PoissonDiscChunkSet cSet = getChunkCircleSet(chunkX, chunkZ);
		if(cSet.generated) {
			return getChunkCircles(chunkX, chunkZ);
		} else {
			return generateCircles(world, random, chunkX, chunkZ);
		}
	}
	
	/**
	 * Use the circle and it's free arc angle to find the radius of the neighbor circle
	 * 
	 * @param world
	 * @param cA
	 * @return radius of the circle
	 */
	private int getRadiusAtCircleTangent(World world, PoissonDisc cA) {
		float angle = (float)cA.getFreeAngle();

		double x = cA.x + (MathHelper.sin(angle) * cA.radius * 1.5);
		double z = cA.z + (MathHelper.cos(angle) * cA.radius * 1.5);

		return radiusCoordinator.getRadiusAtCoords(world, x, z);
	}
	
	public List<PoissonDisc> generateCircles(World world, Random random, int chunkX, int chunkZ) {
		
		List<PoissonDisc> circles = new ArrayList<PoissonDisc>(64);//64 is above the typical range to expect for 9 chunks
		List<PoissonDisc> unsolvedCircles = new ArrayList<PoissonDisc>(64);
		
		//Collect already solved circles from surrounding chunks
		for(CoordUtils.Surround surr: CoordUtils.Surround.values()) {
			Vec3i dir = surr.getOffset();
			getChunkCircles(circles, chunkX + dir.getX(), chunkZ + dir.getZ());
		}
		
		int chunkXStart = chunkX << 4;
		int chunkZStart = chunkZ << 4;

		for(PoissonDisc c: circles) {
			c.edgeMask(chunkXStart, chunkZStart);//Do edge masking
		}
		
		//Mask out circles against one another
		for(int i = 0; i < circles.size() - 1; i++) {
			for(int j = i + 1; j < circles.size(); j++) {
				PoissonDiscHelper.maskDiscs(circles.get(i), circles.get(j));
			}
		}
		
		//Handle no existing circles by creating a single circle to build off of
		if(circles.size() == 0) {
			int x = chunkXStart + random.nextInt(16);
			int z = chunkZStart + random.nextInt(16);
			int radius = radiusCoordinator.getRadiusAtCoords(world, x, z);
			PoissonDisc rootCircle = new PoissonDisc(x, z, radius);
			rootCircle.real = true;
			circles.add(rootCircle);
		}
		
		//Gather the unsolved circles into a list
		PoissonDiscHelper.gatherUnsolved(unsolvedCircles, circles);
		
		int count = 0;
		
		//Keep solving all unsolved circles until there aren't any more to solve.
		while(!unsolvedCircles.isEmpty()) {
			PoissonDisc master = unsolvedCircles.get(0);//Any circle will do.  May as well be the first.
			
			int radius = getRadiusAtCircleTangent(world, master);
			
			PoissonDisc slave = PoissonDiscHelper.findSecondDisc(master, radius, true);//Create a second circle tangential to the master circle.
			Vec2i slavePos = new Vec2i(slave);//Cache slave position
			
			//Mask off the master so it won't happen again.
			master.arc |= 1 << master.getFreeBit();//Clear specific arc bit for good measure
			PoissonDiscHelper.maskDiscs(master, slave, true);

			//Create a list of existing circles that are intersecting with this circle.  List is ordered by penetration depth.
			int i = 0;
			TreeMap<Integer, PoissonDisc> intersecting = new TreeMap<Integer, PoissonDisc>();
			for(PoissonDisc c: circles) {
				if(slave.doCirclesIntersectPadding(c)) {
					int depth = 16 + (int)c.circlePenetration(slave);
					intersecting.put(depth << 8 | i++, c);
				}
			}
			
			//Run through all of the circles that were intersecting
			for(Entry<Integer, PoissonDisc> entry: intersecting.entrySet()) {
				PoissonDisc master1 = master;//Cache master value because we do swapping later
				PoissonDisc master2 = entry.getValue();
				
				//Determine handedness of 3rd circle interaction
				int cross = Vec2i.crossProduct(new Vec2i(slavePos).sub(master1),new Vec2i(master2).sub(master1));
				if(cross < 0){//Swap circles if the cross product is negative
					PoissonDisc temp = master2;
					master2 = master1;
					master1 = temp;
				}
				
				slave = PoissonDiscHelper.findThirdDisc(master1, master2, radius);//Attempt to triangulate a circle position that is touching tangentially to both master circles
				if(slave != null) {//Found a 3rd circle candidate
					for(int ci = 0; ci < circles.size(); ci++) {
						PoissonDisc c = circles.get(ci);
						if(slave.doCirclesIntersect(c)){//See if this new circle intersects with any of the existing circles. If it does then..
							if(c.real || (!c.real && !slave.isInCenterChunk(chunkXStart, chunkZStart)) ) {
								slave = null;//Discard the circle because it's intersecting with an existing real circle
								break;//We needn't continue since we've proven that the circle intersects with any circle
							} else {//The overlapping circle is not real.. but the slave circle is.
								PoissonDiscHelper.fastRemove(circles, ci--);//Delete the offending non-real circle. The order of the circles is unimportant
							} 
						}
					}
				}
				
				if(slave != null) {
					break;//We found a viable circle.. time to move on
				}
			}
			
			if(slave != null) {//The circle has passed all of the non-intersection tests.  Let's add it to the list of circles
				slave.edgeMask(chunkXStart, chunkZStart);//Set the proper mask for whatever chunk this circle resides.
				slave.real = slave.isInCenterChunk(chunkXStart, chunkZStart);//Only circles created in the center chunk are real
				unsolvedCircles.add(slave);//The new circle is necessarily unsolved and we need it in this list for the next step.
				PoissonDiscHelper.solveDiscs(unsolvedCircles, circles);//run all of the unsolved circles again
				circles.add(slave);//add the new circle to the full list
			}
			
			PoissonDiscHelper.gatherUnsolved(unsolvedCircles, circles);//List up the remaining unsolved circles and try again
			
			//For debug purposes
			if(++count > 64 && !unsolvedCircles.isEmpty()) {//It shouldn't over take 64 iterations to solve all of the circles
				System.err.println("-----" + unsolvedCircles.size() + " unsolved circles-----");
				System.err.println("@ chunk x:" + chunkX + ", z:" + chunkZ);
				System.err.println("after " + count + " iterations" );
				for(PoissonDisc c: circles) {
					System.err.println((c.hasFreeAngles() ? "->" : "  ") +  c);
				}
				if(ModConfigs.poissonDiscImageWrite) {
					PoissonDiscDebug.outputCirclesToPng(circles, chunkX, chunkZ, "");
				}
				break;//Something went terribly wrong and we shouldn't hang the system for it.
			}
			
		}
		
		//Add circles to circle set
		PoissonDiscChunkSet cSet = getChunkCircleSet(chunkX, chunkZ);
		cSet.generated = true;
		
		for(PoissonDisc c: circles) {
			if(c.isInCenterChunk(chunkXStart, chunkZStart)) {
				cSet.addCircle(c);
			}
		}
		circles.clear();
		
		return cSet.getCircles(circles, chunkX, chunkZ);
	}
	
	private PoissonDiscChunkSet getChunkCircleSet(int chunkX, int chunkZ) {
		ChunkPos key = new ChunkPos(chunkX, chunkZ);
		PoissonDiscChunkSet cSet;
		
		if(chunkCircles.containsKey(key)) {
			cSet = chunkCircles.get(key);
		} else {
			cSet = new PoissonDiscChunkSet();
			chunkCircles.put(key, cSet);
		}
		
		return cSet;
	}
	
	@Override
	public byte[] getChunkPoissonData(World world, int chunkX, int chunkY, int chunkZ) {
		return getChunkCircleSet(chunkX, chunkZ).getCircleData();
	}
	
	@Override
	public void setChunkPoissonData(World world, int chunkX, int chunkY, int chunkZ, byte[] circleData) {
		getChunkCircleSet(chunkX, chunkZ).setCircleData(circleData);
	}
	
	@Override
	public void unloadChunkPoissonData(World world, int chunkX, int chunkY, int chunkZ) {
		chunkCircles.remove(new ChunkPos(chunkX, chunkZ));
	}
	
	private List<PoissonDisc> getChunkCircles(int chunkX, int chunkZ) {
		return getChunkCircles(new ArrayList<PoissonDisc>(), chunkX, chunkZ);
	}
	
	private List<PoissonDisc> getChunkCircles(List<PoissonDisc> circles, int chunkX, int chunkZ) {
		PoissonDiscChunkSet cSet = getChunkCircleSet(chunkX, chunkZ);
		cSet.getCircles(circles, chunkX, chunkZ);
		return circles;
	}

	
}
