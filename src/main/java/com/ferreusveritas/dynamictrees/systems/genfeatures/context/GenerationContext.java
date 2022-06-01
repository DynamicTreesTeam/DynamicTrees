package com.ferreusveritas.dynamictrees.systems.genfeatures.context;

import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.WorldContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.Random;

/**
 * @author Harley O'Connor
 */
public abstract class GenerationContext {

    private final WorldContext worldContext;
    private final BlockPos pos;
    private final Species species;

    public GenerationContext(IWorld world, BlockPos pos, Species species) {
        this.worldContext = WorldContext.create(world);
        this.pos = pos;
        this.species = species;
    }

    public WorldContext worldContext() {
        return worldContext;
    }

    public IWorld world() {
        return worldContext.access();
    }

    public BlockPos pos() {
        return pos;
    }

    public Species species() {
        return species;
    }

    public final Random random() {
        return this.worldContext.access().getRandom();
    }

}
