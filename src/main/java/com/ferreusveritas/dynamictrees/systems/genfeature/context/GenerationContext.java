package com.ferreusveritas.dynamictrees.systems.genfeature.context;

import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.LevelContext;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

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
        return levelContext.accessor();
    }

    public BlockPos pos() {
        return pos;
    }

    public Species species() {
        return species;
    }

    public final RandomSource random() {
        return this.level().getRandom();
    }

}
