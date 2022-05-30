package com.ferreusveritas.dynamictrees.systems.genfeatures.context;

import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.Random;

/**
 * @author Harley O'Connor
 */
public abstract class GenerationContext<W extends IWorld> {

    private final W world;
    private final BlockPos pos;
    private final Species species;

    public GenerationContext(W world, BlockPos pos, Species species) {
        this.world = world;
        this.pos = pos;
        this.species = species;
    }

    public W world() {
        return world;
    }

    public BlockPos pos() {
        return pos;
    }

    public Species species() {
        return species;
    }

    public final Random random() {
        return this.world.getRandom();
    }

}
