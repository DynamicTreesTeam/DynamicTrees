package com.dtteam.dynamictrees.tree.species;

import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.platform.Services;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.worldgen.DynamicTreeGenerationContext;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public class SwampSpecies extends Species {

    public enum WaterSurfaceGenerationState {
        ROOTED,
        SUNK,
        DISABLED
    }

    public static final TypedRegistry.EntryType<Species> TYPE = createDefaultType(SwampSpecies::new);

    public SwampSpecies(ResourceLocation name, Family family, LeavesProperties leavesProperties) {
        super(name, family, leavesProperties);
    }

    private static final int minRadiusForSunkGeneration = 5;

    @Override
    public boolean generate(DynamicTreeGenerationContext context) {
        if (isWater(context.level().getBlockState(context.rootPos()))) {
            switch (Services.CONFIG.getEnumConfig("swampOaksInWater", WaterSurfaceGenerationState.class)) {
                case WaterSurfaceGenerationState.SUNK: //generate 1 block down
                    if (context.radius() >= minRadiusForSunkGeneration) {
                        context.rootPos().move(Direction.DOWN, countWaterBlocksBelow(context.level(), context.rootPos(), getAllowedWaterHeightForWorldgen()));
                        break;
                    } else {
                        return false;
                    }
                case WaterSurfaceGenerationState.DISABLED: //do not generate
                    return false;
                case WaterSurfaceGenerationState.ROOTED: //just generate normally
            }
        }
        return super.generate(context);
    }

}
