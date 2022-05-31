package com.ferreusveritas.dynamictrees.growthlogic.context;

import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * @author Harley O'Connor
 */
public class PositionalSpeciesContext {
    private final Level world;
    private final BlockPos pos;
    private final Species species;

    public PositionalSpeciesContext(Level world, BlockPos pos, Species species) {
        this.world = world;
        this.pos = pos;
        this.species = species;
    }

    public Level world() {
        return world;
    }

    public BlockPos pos() {
        return pos;
    }

    public Species species() {
        return species;
    }
}
