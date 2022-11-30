package com.ferreusveritas.dynamictrees.tree.species;

import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.ferreusveritas.dynamictrees.util.LevelContext;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

import java.util.Random;

public class SwampOakSpecies extends Species {

    public static final TypedRegistry.EntryType<Species> TYPE = createDefaultType(SwampOakSpecies::new);

    public SwampOakSpecies(ResourceLocation name, Family family, LeavesProperties leavesProperties) {
        super(name, family, leavesProperties);
    }

    private static final int minRadiusForSunkGeneration = 5;

    @Override
    public boolean generate(LevelContext levelContext, BlockPos rootPos, Biome biome, Random random, int radius, SafeChunkBounds safeBounds) {
        if (isWater(levelContext.accessor().getBlockState(rootPos))) {
            switch (DTConfigs.SWAMP_OAKS_IN_WATER.get()) {
                case SUNK: //generate 1 block down
                    if (radius >= minRadiusForSunkGeneration) {
                        return super.generate(levelContext, rootPos.below(), biome, random, radius, safeBounds);
                    } else {
                        return false;
                    }
                case DISABLED: //do not generate
                    return false;
                case ROOTED: //just generate normally
            }
        }
        return super.generate(levelContext, rootPos, biome, random, radius, safeBounds);
    }

}
