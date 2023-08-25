package com.ferreusveritas.dynamictrees.systems;

import com.ferreusveritas.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

public class GrowSignal {

    // Forward data
    public float energy;
    public Direction dir;
    public int numTurns;
    public int numSteps;
    private final Species species;
    public final Direction defaultDir;

    public BlockPos rootPos;
    public BlockPos delta;

    // Back data
    public float radius;
    public float tapering;
    public boolean success;

    /**
     * A choked signal indicates that the tree could not establish the needed space (girth) and so should stop growing.
     */
    public boolean choked;

    public RandomSource rand;
    public GrowSignal(Species species, BlockPos rootPos, float energy, RandomSource random) {
        this(species, rootPos, energy, random, Direction.UP);
    }

    public GrowSignal(Species species, BlockPos rootPos, float energy, RandomSource random, Direction defaultDir) {
        this.species = species;
        this.energy = energy;
        this.defaultDir = defaultDir;
        dir = defaultDir;
        radius = 0.0f;
        numTurns = 0;
        numSteps = 0;
        tapering = 0.3f;
        rand = random;
        success = true;
        choked = false;

        this.rootPos = rootPos;
        delta = new BlockPos(0, 0, 0);
    }

    public Species getSpecies() {
        return species;
    }

    public boolean step() {
        numSteps++;
        delta = delta.relative(dir);

        if (--energy <= 0.0f) {
            success = false; // Ran out of energy before it could grow.
        }

        return success;
    }

    public boolean doTurn(Direction targetDir) {
        if (dir != targetDir) { // Checks for a direction change.
            dir = targetDir;
            numTurns++;
            return true;
        }
        return false;
    }

    public boolean isInTrunk() {
        return numTurns == 0;
    }

}
