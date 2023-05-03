package com.ferreusveritas.dynamictrees.systems.genfeature.context;

import com.ferreusveritas.dynamictrees.compat.season.SeasonHelper;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * @author Harley O'Connor
 */
public class PostGenerationContext extends GenerationContext {

    private final BlockPos originPos;
    private final Holder<Biome> biome;
    private final int radius;
    private final List<BlockPos> endPoints;
    private final SafeChunkBounds bounds;
    private final BlockState initialDirtState;
    private final Float seasonValue;
    private final Float fruitProductionFactor;

    /**
     * @param endPoints        a list of positions designating the branch endpoints
     * @param initialDirtState the block state of the dirt that became rooty. Useful for matching terrain.
     */
    public PostGenerationContext(com.ferreusveritas.dynamictrees.worldgen.GenerationContext context, List<BlockPos> endPoints, BlockState initialDirtState) {
        super(context.level(), context.rootPos().immutable(), context.species());
        this.originPos = context.originPos();
        this.biome = context.biome();
        this.radius = context.radius();
        this.endPoints = endPoints;
        this.bounds = context.safeBounds();
        this.initialDirtState = initialDirtState;
        this.seasonValue = SeasonHelper.getSeasonValue(context.levelContext(), pos());
        this.fruitProductionFactor = species().seasonalFruitProductionFactor(context.levelContext(), pos());
    }

    public BlockPos originPos() {
        return originPos;
    }

    public Holder<Biome> biome() {
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
