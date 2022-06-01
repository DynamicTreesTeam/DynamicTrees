package com.ferreusveritas.dynamictrees.systems.genfeatures.context;

import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;

/**
 * @author Harley O'Connor
 */
public final class FullGenerationContext extends GenerationContext {

    private final Biome biome;
    private final int radius;
    private final SafeChunkBounds bounds;

    public FullGenerationContext(IWorld world, BlockPos rootPos, Species species, Biome biome, int radius, SafeChunkBounds bounds) {
        super(world, rootPos, species);
        this.biome = biome;
        this.radius = radius;
        this.bounds = bounds;
    }

    public Biome biome() {
        return biome;
    }

    public int radius() {
        return radius;
    }

    public SafeChunkBounds bounds() {
        return bounds;
    }

}
