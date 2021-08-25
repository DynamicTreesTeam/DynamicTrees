package com.ferreusveritas.dynamictrees.growthlogic;

import com.ferreusveritas.dynamictrees.api.configurations.Configured;
import com.ferreusveritas.dynamictrees.growthlogic.context.DirectionManipulationContext;
import com.ferreusveritas.dynamictrees.growthlogic.context.DirectionSelectionContext;
import com.ferreusveritas.dynamictrees.growthlogic.context.PositionalSpeciesContext;
import net.minecraft.util.Direction;

/**
 * @author Harley O'Connor
 */
public final class ConfiguredGrowthLogicKit extends Configured<ConfiguredGrowthLogicKit, GrowthLogicKit> {

    public static final ConfiguredGrowthLogicKit NULL_LOGIC_KIT =
            new ConfiguredGrowthLogicKit(GrowthLogicKit.NULL_LOGIC);

    public ConfiguredGrowthLogicKit(GrowthLogicKit configurable) {
        super(configurable);
    }

    @Override
    public ConfiguredGrowthLogicKit copy() {
        final ConfiguredGrowthLogicKit duplicateLogicKit = new ConfiguredGrowthLogicKit(this.configurable);
        duplicateLogicKit.properties.putAll(this.properties);
        return duplicateLogicKit;
    }

    /**
     * Invokes {@link GrowthLogicKit#selectNewDirection(ConfiguredGrowthLogicKit, DirectionSelectionContext)} for this
     * configured kit's growth logic kit.
     *
     * @param context the context
     * @return the direction for the signal to turn to
     * @see GrowthLogicKit#selectNewDirection(ConfiguredGrowthLogicKit, DirectionSelectionContext)
     */
    public Direction selectNewDirection(DirectionSelectionContext context) {
        return this.configurable.selectNewDirection(this, context);
    }

    /**
     * Invokes {@link GrowthLogicKit#populateDirectionProbabilityMap(ConfiguredGrowthLogicKit,
     * DirectionManipulationContext)} for this configured kit's growth logic kit.
     *
     * @param context the context
     * @return the direction for the signal to turn to
     * @see GrowthLogicKit#populateDirectionProbabilityMap(ConfiguredGrowthLogicKit, DirectionManipulationContext)
     */
    public int[] populateDirectionProbabilityMap(DirectionManipulationContext context) {
        return this.configurable.populateDirectionProbabilityMap(this, context);
    }

    /**
     * Invokes {@link GrowthLogicKit#getEnergy(ConfiguredGrowthLogicKit, PositionalSpeciesContext)} for this configured
     * kit's growth logic kit.
     *
     * @param context the context
     * @return the direction for the signal to turn to
     * @see GrowthLogicKit#getEnergy(ConfiguredGrowthLogicKit, PositionalSpeciesContext)
     */
    public float getEnergy(PositionalSpeciesContext context) {
        return this.configurable.getEnergy(this, context);
    }

    /**
     * Invokes {@link GrowthLogicKit#getLowestBranchHeight(ConfiguredGrowthLogicKit, PositionalSpeciesContext)} for this
     * configured kit's growth logic kit.
     *
     * @param context the context
     * @return the direction for the signal to turn to
     * @see GrowthLogicKit#getLowestBranchHeight(ConfiguredGrowthLogicKit, PositionalSpeciesContext)
     */
    public int getLowestBranchHeight(PositionalSpeciesContext context) {
        return this.configurable.getLowestBranchHeight(this, context);
    }

}
