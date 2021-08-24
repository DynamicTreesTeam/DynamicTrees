package com.ferreusveritas.dynamictrees.growthlogic.context;

import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author Harley O'Connor
 */
public class NewDirectionContext extends PositionalSpeciesContext {
    private final Direction newDir;
    private final GrowSignal signal;

    public NewDirectionContext(World world, BlockPos pos, Species species, Direction newDir, GrowSignal signal) {
        super(world, pos, species);
        this.newDir = newDir;
        this.signal = signal;
    }

    public Direction newDir() {
        return newDir;
    }

    public GrowSignal signal() {
        return signal;
    }
}
