package com.ferreusveritas.dynamictrees.systems.genfeatures.context;

import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.LevelContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

import java.util.Random;

/**
 * @author Harley O'Connor
 */
public abstract class GenerationContext {

    private final LevelContext levelContext;
    private final BlockPos pos;
    private final Species species;

    public GenerationContext(LevelAccessor level, BlockPos pos, Species species) {
        this.levelContext = LevelContext.create(level);
        this.pos = pos;
        this.species = species;
    }

    public LevelContext levelContext() {
        return levelContext;
    }

    public LevelAccessor level() {
        return levelContext.access();
    }

    public BlockPos pos() {
        return pos;
    }

    public Species species() {
        return species;
    }

    public final Random random() {
        return this.level().getRandom();
    }

}
