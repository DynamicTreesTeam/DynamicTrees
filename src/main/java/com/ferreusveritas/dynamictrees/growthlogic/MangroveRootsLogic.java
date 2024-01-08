package com.ferreusveritas.dynamictrees.growthlogic;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.growthlogic.context.DirectionManipulationContext;
import com.ferreusveritas.dynamictrees.growthlogic.context.DirectionSelectionContext;
import com.ferreusveritas.dynamictrees.growthlogic.context.PositionalSpeciesContext;
import com.ferreusveritas.dynamictrees.tree.species.MangroveSpecies;
import com.ferreusveritas.dynamictrees.util.MathHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class MangroveRootsLogic extends GrowthLogicKit {

    public MangroveRootsLogic(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    public int[] populateDirectionProbabilityMap(GrowthLogicKitConfiguration configuration,
                                                 DirectionManipulationContext context) {
        final int[] probMap = context.probMap();
        final Direction originDir = context.signal().dir.getOpposite();
        final Direction defaultDir = context.signal().defaultDir; //usually UP

        for (Direction dir : Direction.values()) {
            if (!dir.equals(originDir)) {
                if (dir.getAxis().isHorizontal()) probMap[dir.get3DDataValue()] = 1;
                //Main direction (down) use up probability
                if (dir.equals(defaultDir)) probMap[dir.get3DDataValue()] = context.species().getUpProbability();
                //Never go up
                if (dir.equals(defaultDir.getOpposite())) probMap[dir.get3DDataValue()] = 0;

                final BlockPos deltaPos = context.pos().relative(dir);
                // Check probability for surrounding blocks.
                // Typically, Air: 1, Leaves: 2, Branches: 2 + radius
                final BlockState deltaBlockState = context.level().getBlockState(deltaPos);
                probMap[dir.get3DDataValue()] += TreeHelper.getTreePart(deltaBlockState)
                        .probabilityForBlock(deltaBlockState, context.level(), deltaPos, context.branch());
            }
        }

        //Never go down through the center of the trunk
        if (context.signal().isInTrunk())
            probMap[defaultDir.ordinal()] =  0;

        probMap[defaultDir.getOpposite().ordinal()] = 0;
        //disable the direction we came from
        probMap[originDir.ordinal()] = 0;

        return probMap;
    }

    public Direction selectNewDirection(GrowthLogicKitConfiguration configuration, DirectionSelectionContext context) {
        // Populate the direction probability map.
        final int[] probMap = configuration.populateDirectionProbabilityMap(
                new DirectionManipulationContext(context.level(), context.pos(), context.species(), context.branch(),
                        context.signal(), context.branch().getRadius(context.level().getBlockState(context.pos())), new int[6])
        );

        // Select a direction from the probability map.
        final int choice = MathHelper.selectRandomFromDistribution(context.signal().rand, probMap);
        return Direction.values()[choice != -1 ? choice : 1]; // Default to up if it failed.
    }

    @Override
    public int getLowestBranchHeight(GrowthLogicKitConfiguration configuration, PositionalSpeciesContext context) {
        return 0;
    }

    @Override
    public float getEnergy(GrowthLogicKitConfiguration configuration, PositionalSpeciesContext context) {
        if (context.species() instanceof MangroveSpecies mangroveSpecies){
            return mangroveSpecies.getRootSignalEnergy();
        }
        return context.species().getSignalEnergy();
    }
}
