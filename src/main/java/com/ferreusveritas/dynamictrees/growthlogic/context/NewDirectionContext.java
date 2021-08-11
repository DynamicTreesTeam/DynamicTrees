package com.ferreusveritas.dynamictrees.growthlogic.context;

import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.util.Direction;

/**
 * @author Harley O'Connor
 */
public class NewDirectionContext {
    private final Species species;
    private final Direction newDir;
    private final GrowSignal signal;

    public NewDirectionContext(Species species, Direction newDir, GrowSignal signal) {
        this.species = species;
        this.newDir = newDir;
        this.signal = signal;
    }

    public Species species() {
        return species;
    }

    public Direction newDir() {
        return newDir;
    }

    public GrowSignal signal() {
        return signal;
    }
}
