package com.ferreusveritas.dynamictrees.growthlogic.context;

import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author Harley O'Connor
 */
public class DirectionManipulationContext extends PositionalSpeciesContext {
    private final int radius;
    private final GrowSignal signal;
    private int[] probMap;

    public DirectionManipulationContext(World world, BlockPos pos, Species species, int radius, GrowSignal signal, int[] probMap) {
        super(world, pos, species);
        this.radius = radius;
        this.signal = signal;
        this.probMap = probMap;
    }

    public int radius() {
        return radius;
    }

    public GrowSignal signal() {
        return signal;
    }

    public int[] probMap() {
        return probMap;
    }

    public void probMap(int[] probMap) {
        this.probMap = probMap;
    }
}
