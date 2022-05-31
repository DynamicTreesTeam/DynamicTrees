package com.ferreusveritas.dynamictrees.growthlogic;

import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.growthlogic.context.DirectionManipulationContext;
import com.ferreusveritas.dynamictrees.growthlogic.context.DirectionSelectionContext;
import com.ferreusveritas.dynamictrees.growthlogic.context.PositionalSpeciesContext;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public class JungleLogic extends GrowthLogicKit {

    public static final ConfigurationProperty<Integer> CANOPY_HEIGHT = ConfigurationProperty.integer("canopy_height");
    public static final ConfigurationProperty<Integer> BRANCH_OUT_CHANCE = ConfigurationProperty.integer("branch_out_chance");

    public JungleLogic(final ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected GrowthLogicKitConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(CANOPY_HEIGHT, 14)
                .with(BRANCH_OUT_CHANCE, 5);
    }

    @Override
    protected void registerProperties() {
        this.register(CANOPY_HEIGHT, BRANCH_OUT_CHANCE);
    }

    @Override
    public Direction selectNewDirection(GrowthLogicKitConfiguration configuration, DirectionSelectionContext context) {
        final Direction newDir = super.selectNewDirection(configuration, context);
        if (context.signal().isInTrunk() && newDir != Direction.UP) { // Turned out of trunk
            context.signal().energy = 4.0f;
        }
        return newDir;
    }

    @Override
    public int[] populateDirectionProbabilityMap(GrowthLogicKitConfiguration configuration, DirectionManipulationContext context) {

        final int[] probMap = super.populateDirectionProbabilityMap(configuration, context);
        Direction originDir = context.signal().dir.getOpposite();

        int treeHash = CoordUtils.coordHashCode(context.signal().rootPos, 2);
        int posHash = CoordUtils.coordHashCode(context.pos(), 2);

        //Alter probability map for direction change
        probMap[0] = 0;//Down is always disallowed for jungle
        probMap[1] = context.signal().isInTrunk() ? context.species().getUpProbability() : 0;
        probMap[2] = probMap[3] = probMap[4] = probMap[5] = 0;
        boolean branchOut = (context.signal().numSteps + treeHash) % configuration.get(BRANCH_OUT_CHANCE) == 0;
        int sideTurn = !context.signal().isInTrunk() || (context.signal().isInTrunk() && branchOut && (context.radius() > 1)) ? 2 : 0;//Only allow turns when we aren't in the trunk(or the branch is not a twig)

        int height = configuration.get(CANOPY_HEIGHT) + ((treeHash % 7829) % 8);

        if (context.signal().delta.getY() < height) {
            probMap[2 + (posHash % 4)] = sideTurn;
        } else {
            probMap[1] = probMap[2] = probMap[3] = probMap[4] = probMap[5] = 2;//At top of tree allow any direction
        }

        probMap[originDir.ordinal()] = 0;//Disable the direction we came from
        probMap[context.signal().dir.ordinal()] += context.signal().isInTrunk() ? 0 : context.signal().numTurns == 1 ? 2 : 1;//Favor current travel direction

        return probMap;
    }

    //Jungle trees grow taller in suitable biomes
    @Override
    public float getEnergy(GrowthLogicKitConfiguration configuration, PositionalSpeciesContext context) {
        return super.getEnergy(configuration, context) *
                context.species().biomeSuitability(context.world(), context.pos());
    }

}
