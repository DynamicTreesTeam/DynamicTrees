package com.dtteam.dynamictrees.worldgen;

import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.util.LevelContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;

public class DynamicTreeGenerationContext {

    private final LevelContext levelContext;
    private final Species species;
    private final BlockPos originPos;
    private final BlockPos.MutableBlockPos rootPos;
    private final Holder<Biome> biome;
    private final Direction facing;
    private final int radius;
    private final boolean worldGen;
    private boolean secondChanceRegen;

    public DynamicTreeGenerationContext(LevelContext levelContext, Species species, BlockPos originPos, BlockPos.MutableBlockPos rootPos, Holder<Biome> biome, Direction facing, int radius, boolean worldGen) {
        this.levelContext = levelContext;
        this.species = species;
        this.originPos = originPos;
        this.rootPos = rootPos;
        this.biome = biome;
        this.facing = facing;
        this.radius = Mth.clamp(radius, 2, 8);
        this.worldGen = worldGen;
    }

    public LevelContext levelContext() {
        return levelContext;
    }

    public LevelAccessor level() {
        return levelContext.accessor();
    }

    public RandomSource random() {
        return level().getRandom();
    }

    public Species species() {
        return species;
    }

    public BlockPos originPos() {
        return originPos;
    }

    public BlockPos.MutableBlockPos rootPos() {
        return rootPos;
    }

    public Holder<Biome> biome() {
        return biome;
    }

    public Direction facing() {
        return facing;
    }

    public int radius() {
        return radius;
    }

    public boolean isWorldGen() {
        return worldGen;
    }

    public boolean secondChanceRegen() {
        return secondChanceRegen;
    }

    public void secondChance() {
        this.secondChanceRegen = true;
    }

}
