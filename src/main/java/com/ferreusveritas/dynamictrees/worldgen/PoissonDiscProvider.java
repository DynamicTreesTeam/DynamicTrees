package com.ferreusveritas.dynamictrees.worldgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import com.ferreusveritas.dynamictrees.api.worldgen.IPoissonDebug;
import com.ferreusveritas.dynamictrees.api.worldgen.IPoissonDiscProvider;
import com.ferreusveritas.dynamictrees.api.worldgen.IRadiusCoordinator;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.PoissonDisc;
import com.ferreusveritas.dynamictrees.util.RandomXOR;
import com.ferreusveritas.dynamictrees.util.Vec2i;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;

/**
 * Manages and creates all the Poisson discs in a single dimension.
 * 
 * @author ferreusveritas
 */
public class PoissonDiscProvider implements IPoissonDiscProvider {
	
	private final IRadiusCoordinator radiusCoordinator;
	private final HashMap<ChunkPos, PoissonDiscChunkSet> chunkDiscs;
	private final RandomXOR random = new RandomXOR();
	private IPoissonDebug debug;
	
	public PoissonDiscProvider(IRadiusCoordinator radCoord) {
		chunkDiscs = new HashMap<ChunkPos, PoissonDiscChunkSet>();
		radiusCoordinator = radCoord;
	}
	
	public void setDebug(IPoissonDebug debug) {
		this.debug = debug;
	}
	
	@Override
	public List<PoissonDisc> getPoissonDiscs(int chunkX, int chunkY, int chunkZ) {
		random.setXOR(new BlockPos(chunkX, chunkY, chunkZ));
		PoissonDiscChunkSet cSet = getChunkDiscSet(chunkX, chunkZ);
		if(cSet.generated) {
			return getChunkPoissonDiscs(chunkX, chunkZ);
		} else {
			return generatePoissonDiscs(random, chunkX, chunkZ);
		}
	}
	
	/**
	 * Use the disc and it's free arc angle to find the radius of the neighbor disc
	 * 
	 * @param world
	 * @param cA
	 * @return radius of the disc
	 */
	private int getRadiusAtDiscTangent(PoissonDisc cA) {
		float angle = (float)cA.getFreeAngle();

		double x = cA.x + (MathHelper.sin(angle) * cA.radius * 1.5);
		double z = cA.z + (MathHelper.cos(angle) * cA.radius * 1.5);

		int radius = radiusCoordinator.getRadiusAtCoords(x, z);
		if(debug != null) { debug.getRadius(cA, radius); }
		return radius;
	}
	
	//A set of caches so we needn't create the lists from scratch for every chunk
	private List<PoissonDisc> discCache1 = new ArrayList<PoissonDisc>(64);//64 is above the typical range to expect for 9 chunks
	private List<PoissonDisc> discCache2 = new ArrayList<PoissonDisc>(64);
	
	public List<PoissonDisc> generatePoissonDiscs(Random random, int chunkX, int chunkZ) {
		
		// Step 0.) Clear the temporary caches
		List<PoissonDisc> discs = discCache1;
		List<PoissonDisc> unsolvedDiscs = discCache2;
		discs.clear();
		unsolvedDiscs.clear();
		if(debug != null) { debug.begin(chunkX, chunkZ); }
		
		
		// Step 1.) Collect already solved discs from surrounding chunks
		for(CoordUtils.Surround surr: CoordUtils.Surround.values()) {
			Vec3i dir = surr.getOffset();
			getChunkPoissonDiscs(discs, chunkX + dir.getX(), chunkZ + dir.getZ());
		}
		if(debug != null) { debug.collectSolved(discs); }

		
		// Step 2.) Do edge masking
		int chunkXStart = chunkX << 4;
		int chunkZStart = chunkZ << 4;

		for(PoissonDisc c: discs) {
			c.edgeMask(chunkXStart, chunkZStart);
		}
		if(debug != null) { debug.doEdgeMasking(discs); }
		
		
		// Step 3.) Mask out circles against one another
		for(int i = 0; i < discs.size() - 1; i++) {
			for(int j = i + 1; j < discs.size(); j++) {
				PoissonDiscHelper.maskDiscs(discs.get(i), discs.get(j));
			}
		}
		if(debug != null) { debug.maskSolvedDiscs(discs); }
		
		
		// Step 4.) Handle no existing circles by creating a single circle to build off of
		if(discs.size() == 0) {
			int x = chunkXStart + random.nextInt(16);
			int z = chunkZStart + random.nextInt(16);
			int radius = radiusCoordinator.getRadiusAtCoords(x, z);
			PoissonDisc rootDisc = new PoissonDisc(x, z, radius);
			rootDisc.real = true;
			discs.add(rootDisc);
			if(debug != null) { debug.createRootDisc(discs, rootDisc); }
		}
		
		
		// Step 5.) Gather the unsolved circles into a list
		PoissonDiscHelper.gatherUnsolved(unsolvedDiscs, discs);
		if(debug != null) { debug.gatherUnsolved(unsolvedDiscs, discs); }

		
		int count = 0;
		
		//Keep solving all unsolved disc until there aren't any more to solve.
		while(!unsolvedDiscs.isEmpty()) {
			
			// Step 6.) Pick a random disc from the pool of unsolved discs
			PoissonDisc master = unsolvedDiscs.get(0);//Any circle will do.  May as well be the first.
			if(debug != null) { debug.pickMasterDisc(master, unsolvedDiscs); }
			
			// Step 7.) Get the radius of the new tangential disc
			int radius = getRadiusAtDiscTangent(master);
			
			// Step 8.) Create a second disc tangential to the master disc.
			PoissonDisc slave = PoissonDiscHelper.findSecondDisc(master, radius, true);
			Vec2i slavePos = new Vec2i(slave);//Cache slave position
			if(debug != null) { debug.findSecondDisc(master, slave); }
			
			// Step 9.) Mask off the master so it won't happen again.
			master.arc |= 1 << master.getFreeBit();//Clear specific arc bit for good measure
			PoissonDiscHelper.maskDiscs(master, slave, true);
			if(debug != null) { debug.maskMasterSlave(master, slave); }

			// Step 10.) Create a list of existing circles that are intersecting with this circle.  List is ordered by penetration depth.
			int i = 0;
			Map<Integer, PoissonDisc> intersecting = new TreeMap<Integer, PoissonDisc>();
			for(PoissonDisc c: discs) {
				if(slave.doCirclesIntersectPadding(c)) {
					int depth = 16 + (int)c.discPenetration(slave);
					intersecting.put(depth << 8 | i++, c);
				}
			}
			if(debug != null) { debug.intersectingList(slave, intersecting, discs); }

			
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
				if(debug != null) { debug.findThirdDisc(master1, master2, slave); }
				if(slave != null) {//Found a 3rd circle candidate
					for(int ci = 0; ci < discs.size(); ci++) {
						PoissonDisc c = discs.get(ci);
						if(slave.doCirclesIntersect(c)){//See if this new circle intersects with any of the existing circles. If it does then..
							if(c.real || (!c.real && !slave.isInCenterChunk(chunkXStart, chunkZStart)) ) {
								slave = null;//Discard the circle because it's intersecting with an existing real circle
								break;//We needn't continue since we've proven that the circle intersects with any circle
							} else {//The overlapping circle is not real.. but the slave circle is.
								PoissonDiscHelper.fastRemove(discs, ci--);//Delete the offending non-real circle. The order of the circles is unimportant
							} 
						}
					}
				}
				
				if(slave != null) {
					if(debug != null) { debug.findThirdDiscSolved(slave); }
					break;//We found a viable circle.. time to move on
				}
			}
			
			if(slave != null) {//The circle has passed all of the non-intersection tests.  Let's add it to the list of circles
				slave.edgeMask(chunkXStart, chunkZStart);//Set the proper mask for whatever chunk this circle resides.
				slave.real = slave.isInCenterChunk(chunkXStart, chunkZStart);//Only circles created in the center chunk are real
				unsolvedDiscs.add(slave);//The new circle is necessarily unsolved and we need it in this list for the next step.
				PoissonDiscHelper.solveDiscs(unsolvedDiscs, discs);//run all of the unsolved circles again
				discs.add(slave);//add the new circle to the full list
				if(debug != null) { debug.solveDiscs(unsolvedDiscs, discs); }
			}
			
			PoissonDiscHelper.gatherUnsolved(unsolvedDiscs, discs);//List up the remaining unsolved circles and try again
			if(debug != null) { debug.gatherUnsolved2(unsolvedDiscs, discs); }
			
			//For debug purposes
			if(++count > 64 && !unsolvedDiscs.isEmpty()) {//It shouldn't over take 64 iterations to solve all of the circles
				if(debug != null) { debug.unsolvable(chunkX, chunkZ, count, unsolvedDiscs, discs); }
				break;//Something went terribly wrong and we shouldn't hang the system for it.
			}
			
		}
		
		//Add circles to circle set
		PoissonDiscChunkSet cSet = getChunkDiscSet(chunkX, chunkZ);
		cSet.generated = true;
		
		for(PoissonDisc c: discs) {
			if(c.isInCenterChunk(chunkXStart, chunkZStart)) {
				cSet.addDisc(c);
			}
		}
		discs.clear();
		
		return cSet.getDiscs(discs, chunkX, chunkZ);
	}
	
	private PoissonDiscChunkSet getChunkDiscSet(int chunkX, int chunkZ) {
		ChunkPos key = new ChunkPos(chunkX, chunkZ);
		PoissonDiscChunkSet cSet;
		
		if(chunkDiscs.containsKey(key)) {
			cSet = chunkDiscs.get(key);
		} else {
			cSet = new PoissonDiscChunkSet();
			chunkDiscs.put(key, cSet);
		}
		
		return cSet;
	}
	
	@Override
	public byte[] getChunkPoissonData(int chunkX, int chunkY, int chunkZ) {
		return getChunkDiscSet(chunkX, chunkZ).getDiscData();
	}
	
	@Override
	public void setChunkPoissonData(int chunkX, int chunkY, int chunkZ, byte[] circleData) {
		getChunkDiscSet(chunkX, chunkZ).setDiscData(circleData);
	}
	
	@Override
	public void unloadChunkPoissonData(int chunkX, int chunkY, int chunkZ) {
		chunkDiscs.remove(new ChunkPos(chunkX, chunkZ));
	}
	
	private List<PoissonDisc> getChunkPoissonDiscs(int chunkX, int chunkZ) {
		return getChunkPoissonDiscs(new ArrayList<PoissonDisc>(), chunkX, chunkZ);
	}
	
	private List<PoissonDisc> getChunkPoissonDiscs(List<PoissonDisc> discs, int chunkX, int chunkZ) {
		PoissonDiscChunkSet cSet = getChunkDiscSet(chunkX, chunkZ);
		cSet.getDiscs(discs, chunkX, chunkZ);
		return discs;
	}

	
}
