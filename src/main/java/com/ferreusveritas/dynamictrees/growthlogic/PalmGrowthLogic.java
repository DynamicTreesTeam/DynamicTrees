package com.ferreusveritas.dynamictrees.growthlogic;

import com.ferreusveritas.dynamictrees.growthlogic.context.DirectionManipulationContext;
import com.ferreusveritas.dynamictrees.growthlogic.context.PositionalSpeciesContext;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public class PalmGrowthLogic extends GrowthLogicKit {

    public PalmGrowthLogic(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    public int[] populateDirectionProbabilityMap(GrowthLogicKitConfiguration configuration,
                                                 DirectionManipulationContext context) {
        final int[] probMap = super.populateDirectionProbabilityMap(configuration, context);
        Direction originDir = context.signal().dir.getOpposite();

        // Alter probability map for direction change
        probMap[0] = 0; // Down is always disallowed for palm
        probMap[1] = 10;
        probMap[2] = probMap[3] = probMap[4] = probMap[5] = 0;
        probMap[originDir.ordinal()] = 0; // Disable the direction we came from

        return probMap;
    }

    @Override
    public float getEnergy(GrowthLogicKitConfiguration configuration, PositionalSpeciesContext context) {
        long day = context.level().getGameTime() / 24000L;
        int month = (int) day / 30; // Change the hashs every in-game month
        return super.getEnergy(configuration, context) *
                context.species().biomeSuitability(context.level(), context.pos()) +
                (CoordUtils.coordHashCode(context.pos().above(month), 3) %
                        3); // Vary the height energy by a psuedorandom hash function

    }

}
