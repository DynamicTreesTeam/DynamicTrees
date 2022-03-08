package com.ferreusveritas.dynamictrees.trees.species;

import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.util.WorldContext;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

import java.util.Random;

public class SwampOakSpecies extends Species {

    public static final TypedRegistry.EntryType<Species> TYPE = createDefaultType(SwampOakSpecies::new);

    public SwampOakSpecies(ResourceLocation name, Family family, LeavesProperties leavesProperties) {
        super(name, family, leavesProperties);
    }

    private static final int minRadiusForSunkGeneration = 5;

    @Override
    public boolean generate(WorldContext worldContext, BlockPos rootPos,
                            Biome biome, Random random, int radius,
                            SafeChunkBounds safeBounds) {
        if (isWater(worldContext.access().getBlockState(rootPos))) {
            switch (DTConfigs.SWAMP_OAKS_IN_WATER.get()) {
                case SUNK: //generate 1 block down
                    if (radius >= minRadiusForSunkGeneration) {
                        return super.generate(worldContext, rootPos.below(), biome, random, radius, safeBounds);
                    } else {
                        return false;
                    }
                case DISABLED: //do not generate
                    return false;
                case ROOTED: //just generate normally
            }
        }
        return super.generate(worldContext, rootPos, biome, random, radius, safeBounds);
    }

}
