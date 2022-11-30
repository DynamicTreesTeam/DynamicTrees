package com.ferreusveritas.dynamictrees.growthlogic;

import com.ferreusveritas.dynamictrees.api.configuration.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.growthlogic.context.DirectionManipulationContext;
import com.ferreusveritas.dynamictrees.growthlogic.context.DirectionSelectionContext;
import com.ferreusveritas.dynamictrees.growthlogic.context.PositionalSpeciesContext;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public class ConiferLogic extends GrowthLogicKit {

    /**
     * Sets the remaining energy a branch has, affects the conical slope of the tree shape.
     */
    public static final ConfigurationProperty<Float> ENERGY_DIVISOR = ConfigurationProperty.floatProperty("energy_divisor");
    /**
     * Sets the maximum amount of energy a branch has left after leaving the trunk. Helps to make a tree develop a more
     * cylindrical shape.
     */
    public static final ConfigurationProperty<Float> HORIZONTAL_LIMITER = ConfigurationProperty.floatProperty("horizontal_limiter");

    public ConiferLogic(final ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected GrowthLogicKitConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(ENERGY_DIVISOR, 3F)
                .with(HORIZONTAL_LIMITER, 16F)
                .with(HEIGHT_VARIATION, 5);
    }

    @Override
    protected void registerProperties() {
        this.register(ENERGY_DIVISOR, HORIZONTAL_LIMITER, HEIGHT_VARIATION);
    }

    @Override
    public Direction selectNewDirection(GrowthLogicKitConfiguration configuration, DirectionSelectionContext context) {
        final Direction newDir = super.selectNewDirection(configuration, context);
        if (context.signal().isInTrunk() && newDir != Direction.UP) { // Turned out of trunk.
            context.signal().energy /= configuration.get(ENERGY_DIVISOR);
            final Float horizontalLimiter = configuration.get(HORIZONTAL_LIMITER);
            if (context.signal().energy > horizontalLimiter) {
                context.signal().energy = horizontalLimiter;
            }
        }
        return newDir;
    }

    @Override
    public int[] populateDirectionProbabilityMap(GrowthLogicKitConfiguration configuration, DirectionManipulationContext context) {
        final int[] probMap = super.populateDirectionProbabilityMap(configuration, context);
        Direction originDir = context.signal().dir.getOpposite();

        //Alter probability map for direction change
        probMap[0] = 0;//Down is always disallowed for spruce
        probMap[1] = context.signal().isInTrunk() ? context.species().getUpProbability() : 0;
        probMap[2] = probMap[3] = probMap[4] = probMap[5] = //Only allow turns when we aren't in the trunk(or the branch is not a twig and step is odd)
                !context.signal().isInTrunk() || (context.signal().isInTrunk() && context.signal().numSteps % 2 == 1 && context.radius() > 1) ? 2 : 0;
        probMap[originDir.ordinal()] = 0;//Disable the direction we came from
        probMap[context.signal().dir.ordinal()] += context.signal().isInTrunk() ? 0 : context.signal().numTurns == 1 ? 2 : 1;//Favor current travel direction

        return probMap;
    }

    //Spruce trees are so similar that it makes sense to randomize their height for a little variation
    //but we don't want the trees to always be the same height all the time when planted in the same location
    //so we feed the hash function the in-game month
    @Override
    public float getEnergy(GrowthLogicKitConfiguration configuration, PositionalSpeciesContext context) {
        long day = context.level().getGameTime() / 24000L;
        int month = (int) day / 30;//Change the hashs every in-game month

        return super.getEnergy(configuration, context) * context.species().biomeSuitability(context.level(), context.pos()) +
                (CoordUtils.coordHashCode(context.pos().above(month), 2) % configuration.get(HEIGHT_VARIATION)); // Vary the height energy by a psuedorandom hash function
    }

}
