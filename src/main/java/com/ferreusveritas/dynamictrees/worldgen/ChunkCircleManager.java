package com.ferreusveritas.dynamictrees.worldgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.util.Circle;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.Vec2i;
import com.ferreusveritas.dynamictrees.util.Vec3i;

import net.minecraft.util.MathHelper;

public class ChunkCircleManager {

	IRadiusCoordinator radiusCoordinator;
	HashMap<Vec2i, ChunkCircleSet> chunkCircles;

	public ChunkCircleManager(IRadiusCoordinator radCoord) {
		chunkCircles = new HashMap<Vec2i, ChunkCircleSet>();
		radiusCoordinator = radCoord;
	}

	public ArrayList<Circle> getCircles(World world, Random random, int chunkX, int chunkZ) {
		ChunkCircleSet cSet = getChunkCircleSet(chunkX, chunkZ);
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
	private int getRadiusAtCircleTangent(World world, Circle cA) {
		float angle = (float)cA.getFreeAngle();

		double x = cA.x + (MathHelper.sin(angle) * cA.radius * 1.5);
		double z = cA.z + (MathHelper.cos(angle) * cA.radius * 1.5);

		return radiusCoordinator.getRadiusAtCoords(world, x, z);
	}
	
	public ArrayList<Circle> generateCircles(World world, Random random, int chunkX, int chunkZ) {
		
		ArrayList<Circle> circles = new ArrayList<Circle>(64);//64 is above the typical range to expect for 9 chunks
		ArrayList<Circle> unsolvedCircles = new ArrayList<Circle>(64);
		
		//Collect already solved circles from surrounding chunks
		for(Vec3i dir: CoordUtils.surround) {
			getChunkCircles(circles, chunkX + dir.getX(), chunkZ + dir.getZ());
		}
		
		int chunkXStart = chunkX << 4;
		int chunkZStart = chunkZ << 4;

		for(Circle c: circles) {
			c.edgeMask(chunkXStart, chunkZStart);//Do edge masking
		}
		
		//Mask out circles against one another
		for(int i = 0; i < circles.size() - 1; i++) {
			for(int j = i + 1; j < circles.size(); j++) {
				CircleHelper.maskCircles(circles.get(i), circles.get(j));
			}
		}
		
		//Handle no existing circles by creating a single circle to build off of
		if(circles.size() == 0) {
			int x = chunkXStart + random.nextInt(16);
			int z = chunkZStart + random.nextInt(16);
			int radius = radiusCoordinator.getRadiusAtCoords(world, x, z);
			Circle rootCircle = new Circle(x, z, radius);
			rootCircle.real = true;
			circles.add(rootCircle);
		}
		
		//Gather the unsolved circles into a list
		CircleHelper.gatherUnsolved(unsolvedCircles, circles);
		
		int count = 0;
		
		//Keep solving all unsolved circles until there aren't any more to solve.
		while(!unsolvedCircles.isEmpty()) {
			Circle master = unsolvedCircles.get(0);//Any circle will do.  May as well be the first.
			
			int radius = getRadiusAtCircleTangent(world, master);
			
			Circle slave = CircleHelper.findSecondCircle(master, radius);//Create a second circle tangential to the master circle.
			Vec2i slavePos = new Vec2i(slave);//Cache slave position
			
			//Mask off the master so it won't happen again.
			master.arc |= 1 << master.getFreeBit();//Clear specific arc bit for good measure
			CircleHelper.maskCircles(master, slave, true);

			//Create a list of existing circles that are intersecting with this circle.  List is ordered by penetration depth.
			int i = 0;
			TreeMap<Integer, Circle> intersecting = new TreeMap<Integer, Circle>();
			for(Circle c: circles) {
				if(slave.doCirclesIntersect(c)) {
					int depth = 16 + (int)c.circlePenetration(slave);
					intersecting.put(depth << 8 | i++, c);
				}
			}
			
			//Run through all of the circles that were intersecting
			for(Entry<Integer, Circle> entry: intersecting.entrySet()) {
				Circle master1 = master;//Cache master value because we do swapping later
				Circle master2 = entry.getValue();
				
				//Determine handedness of 3rd circle interaction
				int cross = Vec2i.crossProduct(new Vec2i(slavePos).sub(master1),new Vec2i(master2).sub(master1));
				if(cross < 0){//Swap circles if the cross product is negative
					Circle temp = master2;
					master2 = master1;
					master1 = temp;
				}
				
				slave = CircleHelper.findThirdCircle(master1, master2, radius);//Attempt to triangulate a circle position that is touching tangentially to both master circles
				if(slave != null) {//Found a 3rd circle candidate
					for(int ci = 0; ci < circles.size(); ci++) {
						Circle c = circles.get(ci);
						if(slave.doCirclesIntersect(c)){//See if this new circle intersects with any of the existing circles. If it does then..
							if(c.real || (!c.real && !slave.isInCenterChunk(chunkXStart, chunkZStart)) ) {
								slave = null;//Discard the circle because it's intersecting with an existing real circle
								break;//We needn't continue since we've proven that the circle intersects with any circle
							} else {//The overlapping circle is not real.. but the slave circle is.
								CircleHelper.fastRemove(circles, ci--);//Delete the offending non-real circle. The order of the circles is unimportant
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
				CircleHelper.solveCircles(unsolvedCircles, circles);//run all of the unsolved circles again
				circles.add(slave);//add the new circle to the full list
			}
			
			CircleHelper.gatherUnsolved(unsolvedCircles, circles);//List up the remaining unsolved circles and try again
			
			//For debug purposes
			if(++count > 64 && !unsolvedCircles.isEmpty()) {//It shouldn't over take 64 iterations to solve all of the circles
				System.err.println("-----" + unsolvedCircles.size() + " unsolved circles-----");
				System.err.println("@ chunk x:" + chunkX + ", z:" + chunkZ);
				System.err.println("after " + count + " iterations" );
				for(Circle c: circles) {
					System.err.println((c.hasFreeAngles() ? "->" : "  ") +  c);
				}
				CircleDebug.outputCirclesToPng(circles, chunkX, chunkZ, "");
				break;//Something went terribly wrong and we shouldn't hang the system for it.
			}
			
		}
		
		//Add circles to circle set
		ChunkCircleSet cSet = getChunkCircleSet(chunkX, chunkZ);
		cSet.generated = true;
		
		for(Circle c: circles) {
			if(c.isInCenterChunk(chunkXStart, chunkZStart)) {
				cSet.addCircle(c);
			}
		}
		circles.clear();
		
		return cSet.getCircles(circles, chunkX, chunkZ);
	}
	
	private ChunkCircleSet getChunkCircleSet(int chunkX, int chunkZ) {
		Vec2i key = new Vec2i(chunkX, chunkZ);
		ChunkCircleSet cSet;
		
		if(chunkCircles.containsKey(key)) {
			cSet = chunkCircles.get(key);
		} else {
			cSet = new ChunkCircleSet();
			chunkCircles.put(key, cSet);
		}
		
		return cSet;
	}
	
	public byte[] getChunkCircleData(int chunkX, int chunkZ) {
		return getChunkCircleSet(chunkX, chunkZ).getCircleData();
	}
	
	public void setChunkCircleData(int chunkX, int chunkZ, byte[] circleData) {
		getChunkCircleSet(chunkX, chunkZ).setCircleData(circleData);
	}
	
	public void unloadChunkCircleData(int chunkX, int chunkZ) {
		chunkCircles.remove(new Vec2i(chunkX, chunkZ));
	}
	
	private ArrayList<Circle> getChunkCircles(int chunkX, int chunkZ) {
		return getChunkCircles(new ArrayList<Circle>(), chunkX, chunkZ);
	}
	
	private ArrayList<Circle> getChunkCircles(ArrayList<Circle> circles, int chunkX, int chunkZ) {
		ChunkCircleSet cSet = getChunkCircleSet(chunkX, chunkZ);
		cSet.getCircles(circles, chunkX, chunkZ);
		return circles;
	}
	
}
