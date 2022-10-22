package com.ferreusveritas.dynamictrees.growthlogic.context;

import com.ferreusveritas.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * @author Harley O'Connor
 */
public class PositionalSpeciesContext {
    private final Level level;
    private final BlockPos pos;
    private final Species species;

    public PositionalSpeciesContext(Level level, BlockPos pos, Species species) {
        this.level = level;
        this.pos = pos;
        this.species = species;
    }

    public Level level() {
        return level;
    }

    public BlockPos pos() {
        return pos;
    }

    public Species species() {
        return species;
    }
}
