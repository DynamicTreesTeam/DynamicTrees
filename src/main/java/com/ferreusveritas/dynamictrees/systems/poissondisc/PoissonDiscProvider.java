package com.ferreusveritas.dynamictrees.systems.poissondisc;

import com.ferreusveritas.dynamictrees.api.worldgen.IPoissonDebug;
import com.ferreusveritas.dynamictrees.api.worldgen.IPoissonDiscProvider;
import com.ferreusveritas.dynamictrees.api.worldgen.IRadiusCoordinator;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.RandomXOR;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3i;

import java.util.*;
import java.util.Map.Entry;

/**
 * Manages and creates all the Poisson discs in a single dimension.
 *
 * @author ferreusveritas
 */
public class PoissonDiscProvider implements IPoissonDiscProvider {

    private final IRadiusCoordinator radiusCoordinator;
    private final HashMap<ChunkPos, PoissonDiscChunkSet> chunkDiscs;
    private RandomXOR random = new RandomXOR();
    private IPoissonDebug debug = IPoissonDebug.EMPTY_POISSON_DEBUG;

    public PoissonDiscProvider(IRadiusCoordinator radCoord) {
        this.chunkDiscs = new HashMap<>();
        this.radiusCoordinator = radCoord;
    }

    public PoissonDiscProvider setSeed(Long seed) {
        if (seed != null) {
            this.random = new RandomXOR(seed);
        }
        return this;
    }

    @SuppressWarnings("unused")
    public void setDebug(IPoissonDebug debug) {
        this.debug = debug;
    }

    @Override
    public List<PoissonDisc> getPoissonDiscs(int chunkX, int chunkY, int chunkZ) {
        this.random.setXOR(new BlockPos(chunkX, chunkY, chunkZ));
        final PoissonDiscChunkSet cSet = getChunkDiscSet(chunkX, chunkZ);
        if (cSet.generated) {
            return this.getChunkPoissonDiscs(chunkX, chunkZ);
        } else {
            int i = 0;
            List<PoissonDisc> output = null;
            while (this.radiusCoordinator.runPass(chunkX, chunkZ, i++)) {
                output = this.generatePoissonDiscs(random, chunkX, chunkZ);
            }

            return output;
        }
    }

    // A set of caches so we needn't create the lists from scratch for every chunk.
    private final List<PoissonDisc> discCache1 = new ArrayList<>(64); // 64 is above the typical range to expect for 9 chunks.
    private final List<PoissonDisc> discCache2 = new ArrayList<>(64);

    public List<PoissonDisc> generatePoissonDiscs(Random random, int chunkX, int chunkZ) {
        final List<PoissonDisc> allDiscs = discCache1;
        final List<PoissonDisc> unsolvedDiscs = discCache2;

        // Step 0. Clear the temporary caches.
        allDiscs.clear();
        unsolvedDiscs.clear();

        this.debug.begin(chunkX, chunkZ);

        // Step 1. Collect already solved discs from surrounding chunks.
        this.getChunkPoissonDiscs(allDiscs, chunkX, chunkZ);
        for (CoordUtils.Surround surr : CoordUtils.Surround.values()) {
            final Vector3i dir = surr.getOffset();
            this.getChunkPoissonDiscs(allDiscs, chunkX + dir.getX(), chunkZ + dir.getZ());
        }

        this.debug.collectSolved(allDiscs);

        // Step 2. Do edge masking.
        final int chunkXStart = chunkX << 4;
        final int chunkZStart = chunkZ << 4;

        for (final PoissonDisc c : allDiscs) {
            c.edgeMask(chunkXStart, chunkZStart);
        }

        this.debug.doEdgeMasking(allDiscs);

        // Step 3. Mask out circles against one another.
        for (int i = 0; i < allDiscs.size() - 1; i++) {
            for (int j = i + 1; j < allDiscs.size(); j++) {
                PoissonDiscHelper.maskDiscs(allDiscs.get(i), allDiscs.get(j));
            }
        }

        this.debug.maskSolvedDiscs(allDiscs);

        // Step 4. Handle no existing circles by creating a single circle to build off of.
        if (allDiscs.size() == 0) {
            final int x = chunkXStart + random.nextInt(16);
            final int z = chunkZStart + random.nextInt(16);
            final int radius = radiusCoordinator.getRadiusAtCoords(x, z);
            final PoissonDisc rootDisc = new PoissonDisc(x, z, radius);
            rootDisc.real = true;
            allDiscs.add(rootDisc);
            this.debug.createRootDisc(allDiscs, rootDisc);
        }

        // Step 5. Gather the unsolved circles into a list.
        PoissonDiscHelper.gatherUnsolved(unsolvedDiscs, allDiscs);
        this.debug.gatherUnsolved(unsolvedDiscs, allDiscs);


        int count = 0; // This counter is used to make sure we don't endlessly generate for an unsolvable set.

        // Keep solving all unsolved disc until there aren't any more to solve.
        while (!unsolvedDiscs.isEmpty()) {
            this.debug.updateCount(count, unsolvedDiscs, allDiscs);

            // Step 6. Pick a random disc from the pool of unsolved discs this will be the master disc.
            final PoissonDisc master = unsolvedDiscs.get(0); // Any circle will do. May as well be the first.
            this.debug.pickMasterDisc(master, unsolvedDiscs, allDiscs);

            // The goal here is to try both directions and prefer the direction that creates an intersection with an existing disc.
            PoissonDisc slave = null;
            Vec2i slavePos = null;
            int radius = 0;
            for (int dir = 0; dir <= 1; dir++) {
                boolean CCW = dir == 0;

                // Step 7. Use the master disc and it's free arc angle to find the radius of the new tangential disc.
                final float angle = CCW ? (float) master.getFreeAngleCCW() : (float) master.getFreeAngleCW();
                //System.out.println("dir: " + (CCW ? "CCW" : "CW") + ", angle: " + (angle * 180 / Math.PI));
                final double dx = master.x + (MathHelper.sin(angle) * master.radius * 1.5);
                final double dz = master.z + (MathHelper.cos(angle) * master.radius * 1.5);
                radius = this.radiusCoordinator.getRadiusAtCoords((int) dx, (int) dz);
                this.debug.getRadius(master, radius, unsolvedDiscs, allDiscs);

                // Step 8. Create a second disc tangential to the master disc.
                slave = PoissonDiscHelper.findSecondDisc(master, radius, true, CCW);
                slavePos = new Vec2i(slave);//Cache slave position
                this.debug.findSecondDisc(master, slave, unsolvedDiscs, allDiscs);

                if (this.doesDiscIntersectWith(slave, allDiscs)) {
                    break;
                }
            }

            // Step 9. Mask off the master so it won't happen again.
            master.arc |= 1 << master.getFreeBitCW(); // Clear specific arc bit for good measure.
            PoissonDiscHelper.maskDiscs(master, slave, true);
            this.debug.maskMasterSlave(master, slave, unsolvedDiscs, allDiscs);

            // Step 10. Create a list of existing circles that are intersecting with this circle.  List is ordered by penetration depth.
            int i = 0;
            final Map<Integer, PoissonDisc> intersecting = new TreeMap<>();
            for (final PoissonDisc c : allDiscs) {
                if (slave.doCirclesIntersectPadding(c)) {
                    final int depth = 16 + (int) c.discPenetration(slave);
                    intersecting.put(depth << 8 | i++, c);
                }
            }
            this.debug.intersectingList(slave, intersecting, allDiscs);


            // Run through all of the circles that were intersecting.
            for (final Entry<Integer, PoissonDisc> entry : intersecting.entrySet()) {
                PoissonDisc master1 = master; // Cache master value because we do swapping later.
                PoissonDisc master2 = entry.getValue();

                // Determine handedness of 3rd circle interaction.
                final int cross = Vec2i.crossProduct(new Vec2i(slavePos).sub(master1), new Vec2i(master2).sub(master1));
                if (cross < 0) { //Swap circles if the cross product is negative.
                    final PoissonDisc temp = master2;
                    master2 = master1;
                    master1 = temp;
                }

                slave = PoissonDiscHelper.findThirdDisc(master1, master2, radius); // Attempt to triangulate a circle position that is touching tangentially to both master circles.
                this.debug.findThirdDiscCandidate(master1, master2, slave, unsolvedDiscs, allDiscs);
                if (slave != null) { // Found a 3rd circle candidate
                    // System.out.println("slave is not null");
                    for (int ci = 0; ci < allDiscs.size(); ci++) {
                        PoissonDisc c = allDiscs.get(ci);
                        if (slave.doCirclesIntersectPadding(c)) { // See if this new circle intersects with any of the existing circles. If it does then..
                            this.debug.thirdCircleCandidateIntersects(master1, master2, slave, c, unsolvedDiscs, allDiscs);
                            if (c.real || !slave.isInCenterChunk(chunkXStart, chunkZStart)) {
                                // System.out.println("Discard the slave because it's intersecting with an existing real circle");
                                slave = null; // Discard the circle because it's intersecting with an existing real circle.
                                break; // We needn't continue since we've proven that the circle intersects with any circle.
                            } else { // The overlapping circle is not real.. but the slave circle is.
                                // System.out.println("Delete the offending non-real circle.");
                                PoissonDiscHelper.fastRemove(allDiscs, ci--); // Delete the offending non-real circle. The order of the circles is unimportant.
                            }
                        }
                    }

                    this.debug.findThirdDiscSolved(slave, unsolvedDiscs, allDiscs);
                    break; // We found a viable circle.. time to move on.
                }
            }

            if (slave != null) { // The circle has passed all of the non-intersection tests.  Let's add it to the list of circles.
                slave.edgeMask(chunkXStart, chunkZStart); // Set the proper mask for whatever chunk this circle resides.
                slave.real = slave.isInCenterChunk(chunkXStart, chunkZStart); // Only circles created in the center chunk are real.
                unsolvedDiscs.add(slave); // The new circle is necessarily unsolved and we need it in this list for the next step.
                PoissonDiscHelper.solveDiscs(unsolvedDiscs, allDiscs); // Run all of the unsolved circles again.
                allDiscs.add(slave); // add the new circle to the full list.
                this.debug.solveDiscs(unsolvedDiscs, allDiscs);
            }

            PoissonDiscHelper.gatherUnsolved(unsolvedDiscs, allDiscs); // List up the remaining unsolved circles and try again.
            this.debug.gatherUnsolved2(unsolvedDiscs, allDiscs);

            // For debug purposes.
            if (++count > 64 && !unsolvedDiscs.isEmpty()) { // It shouldn't over take 64 iterations to solve all of the circles.
                this.debug.unsolvable(chunkX, chunkZ, count, unsolvedDiscs, allDiscs);
                break; // Something went terribly wrong and we shouldn't hang the system for it.
            }

        }

        // Add circles to circle set.
        final PoissonDiscChunkSet cSet = getChunkDiscSet(chunkX, chunkZ);
        cSet.generated = true;

        for (final PoissonDisc disc : allDiscs) {
            if (disc.isInCenterChunk(chunkXStart, chunkZStart)) {
                cSet.addDisc(disc);
            }
        }

        return cSet.getDiscs(new ArrayList<>(16), chunkX, chunkZ);
    }

    private boolean doesDiscIntersectWith(PoissonDisc disc, List<PoissonDisc> others) {
        for (final PoissonDisc otherDisc : others) {
            if (disc.doCirclesIntersectPadding(otherDisc)) {
                return true;
            }
        }

        return false;
    }

    private PoissonDiscChunkSet getChunkDiscSet(int chunkX, int chunkZ) {
        final ChunkPos key = new ChunkPos(chunkX, chunkZ);
        final PoissonDiscChunkSet cSet;

        if (this.chunkDiscs.containsKey(key)) {
            cSet = this.chunkDiscs.get(key);
        } else {
            cSet = new PoissonDiscChunkSet();
            this.chunkDiscs.put(key, cSet);
        }

        return cSet;
    }

    @Override
    public byte[] getChunkPoissonData(int chunkX, int chunkY, int chunkZ) {
        return this.getChunkDiscSet(chunkX, chunkZ).getDiscData();
    }

    @Override
    public void setChunkPoissonData(int chunkX, int chunkY, int chunkZ, byte[] circleData) {
        this.getChunkDiscSet(chunkX, chunkZ).setDiscData(circleData);
    }

    @Override
    public void unloadChunkPoissonData(int chunkX, int chunkY, int chunkZ) {
        this.chunkDiscs.remove(new ChunkPos(chunkX, chunkZ));
    }

    private List<PoissonDisc> getChunkPoissonDiscs(int chunkX, int chunkZ) {
        return this.getChunkPoissonDiscs(new ArrayList<>(), chunkX, chunkZ);
    }

    private List<PoissonDisc> getChunkPoissonDiscs(List<PoissonDisc> discs, int chunkX, int chunkZ) {
        final PoissonDiscChunkSet cSet = getChunkDiscSet(chunkX, chunkZ);
        cSet.getDiscs(discs, chunkX, chunkZ);
        return discs;
    }


}
