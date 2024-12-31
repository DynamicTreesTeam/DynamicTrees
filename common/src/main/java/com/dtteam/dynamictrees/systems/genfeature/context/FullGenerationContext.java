package com.dtteam.dynamictrees.systems.genfeature.context;

import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;

/**
 * @author Harley O'Connor
 */
public final class FullGenerationContext extends GenFeatureContext {

    private final Holder<Biome> biome;
    private final int radius;
    private final boolean worldGen;

    public FullGenerationContext(LevelAccessor level, BlockPos rootPos, Species species, Holder<Biome> biome, int radius, boolean worldGen) {
        super(level, rootPos, species);
        this.biome = biome;
        this.radius = radius;
        this.worldGen = worldGen;
    }

    public Holder<Biome> biome() {
        return biome;
    }

    public int radius() {
        return radius;
    }

    public boolean isWorldGen() {
        return worldGen;
    }

}
