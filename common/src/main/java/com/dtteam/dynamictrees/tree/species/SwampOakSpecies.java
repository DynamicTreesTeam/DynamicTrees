package com.dtteam.dynamictrees.tree.species;

import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.tree.family.Family;
import net.minecraft.resources.ResourceLocation;

public class SwampOakSpecies extends Species {

    public static final TypedRegistry.EntryType<Species> TYPE = createDefaultType(SwampOakSpecies::new);

    public SwampOakSpecies(ResourceLocation name, Family family, LeavesProperties leavesProperties) {
        super(name, family, leavesProperties);
    }

    private static final int minRadiusForSunkGeneration = 5;

//    @Override
//    public boolean generate(GenerationContext context) {
//        if (isWater(context.level().getBlockState(context.rootPos()))) {
//            switch (DTConfigs.SWAMP_OAKS_IN_WATER.get()) {
//                case SUNK: //generate 1 block down
//                    if (context.radius() >= minRadiusForSunkGeneration) {
//                        context.rootPos().move(Direction.DOWN, countWaterBlocksBelow(context.level(), context.rootPos(), getAllowedWaterHeightForWorldgen()));
//                        break;
//                    } else {
//                        return false;
//                    }
//                case DISABLED: //do not generate
//                    return false;
//                case ROOTED: //just generate normally
//            }
//        }
//        return super.generate(context);
//    }

}
