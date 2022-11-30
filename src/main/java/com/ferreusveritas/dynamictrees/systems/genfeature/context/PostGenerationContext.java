package com.ferreusveritas.dynamictrees.systems.genfeature.context;

import com.ferreusveritas.dynamictrees.block.rooty.RootyBlock;
import com.ferreusveritas.dynamictrees.compat.season.SeasonProvider;
import com.ferreusveritas.dynamictrees.systems.poissondisc.PoissonDisc;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * @author Harley O'Connor
 */
public class PostGenerationContext extends GenerationContext {

    private final Biome biome;
    private final int radius;
    private final List<BlockPos> endPoints;
    private final SafeChunkBounds bounds;
    private final BlockState initialDirtState;
    private final Float seasonValue;
    private final Float fruitProductionFactor;

    /**
     * Instantiates a new {@link PostGenerationContext} object.
     *
     * @param level                 The {@link LevelAccessor} object.
     * @param rootPos               The {@link BlockPos} of the {@link RootyBlock} the generated tree is planted on.
     * @param biome                 The {@link Biome} the tree has generated in.
     * @param radius                The radius of the {@link PoissonDisc} the tree generated in.
     * @param endPoints             A {@link List} of {@link BlockPos} in the level designating branch endpoints.
     * @param bounds                The {@link SafeChunkBounds} to generate in.
     * @param initialDirtState      The {@link BlockState} of the dirt that became rooty. Useful for matching terrain.
     * @param seasonValue           The current season value, as obtained from {@link SeasonProvider#getSeasonValue(net.minecraft.world.level.Level,
     *                              BlockPos)}.
     * @param fruitProductionFactor The current fruit production factor, as obtained from {@link
     *                              Species#seasonalFruitProductionFactor(com.ferreusveritas.dynamictrees.util.LevelContext, BlockPos)}.
     */
    public PostGenerationContext(LevelAccessor level, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints,
                                 SafeChunkBounds bounds, BlockState initialDirtState, Float seasonValue, Float fruitProductionFactor) {
        super(level, rootPos, species);
        this.biome = biome;
        this.radius = radius;
        this.endPoints = endPoints;
        this.bounds = bounds;
        this.initialDirtState = initialDirtState;
        this.seasonValue = seasonValue;
        this.fruitProductionFactor = fruitProductionFactor;
    }

    public Biome biome() {
        return biome;
    }

    public int radius() {
        return radius;
    }

    public List<BlockPos> endPoints() {
        return endPoints;
    }

    public SafeChunkBounds bounds() {
        return bounds;
    }

    public BlockState initialDirtState() {
        return initialDirtState;
    }

    public Float seasonValue() {
        return seasonValue;
    }

    public Float fruitProductionFactor() {
        return fruitProductionFactor;
    }

    public final boolean isWorldGen() {
        return this.bounds != SafeChunkBounds.ANY;
    }

}
