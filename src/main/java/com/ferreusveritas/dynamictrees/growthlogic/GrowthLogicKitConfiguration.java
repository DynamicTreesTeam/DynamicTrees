package com.ferreusveritas.dynamictrees.growthlogic;

import com.ferreusveritas.dynamictrees.api.configurations.Configuration;
import com.ferreusveritas.dynamictrees.api.configurations.TemplateRegistry;
import com.ferreusveritas.dynamictrees.growthlogic.context.DirectionManipulationContext;
import com.ferreusveritas.dynamictrees.growthlogic.context.DirectionSelectionContext;
import com.ferreusveritas.dynamictrees.growthlogic.context.PositionalSpeciesContext;
import net.minecraft.util.Direction;

/**
 * @author Harley O'Connor
 */
public final class GrowthLogicKitConfiguration extends Configuration<GrowthLogicKitConfiguration, GrowthLogicKit> {

    public static final GrowthLogicKitConfiguration NULL =
            new GrowthLogicKitConfiguration(GrowthLogicKit.NULL);

    public static final TemplateRegistry<GrowthLogicKitConfiguration> TEMPLATES = new TemplateRegistry<>();

    public GrowthLogicKitConfiguration(GrowthLogicKit configurable) {
        super(configurable);
    }

    @Override
    public GrowthLogicKitConfiguration copy() {
        final GrowthLogicKitConfiguration duplicateLogicKit = new GrowthLogicKitConfiguration(this.configurable);
        duplicateLogicKit.properties.putAll(this.properties);
        return duplicateLogicKit;
    }

    /**
     * Invokes {@link GrowthLogicKit#selectNewDirection(GrowthLogicKitConfiguration, DirectionSelectionContext)} for this
     * configured kit's growth logic kit.
     *
     * @param context the context
     * @return the direction for the signal to turn to
     * @see GrowthLogicKit#selectNewDirection(GrowthLogicKitConfiguration, DirectionSelectionContext)
     */
    public Direction selectNewDirection(DirectionSelectionContext context) {
        return this.configurable.selectNewDirection(this, context);
    }

    /**
     * Invokes {@link GrowthLogicKit#populateDirectionProbabilityMap(GrowthLogicKitConfiguration,
     * DirectionManipulationContext)} for this configured kit's growth logic kit.
     *
     * @param context the context
     * @return the direction for the signal to turn to
     * @see GrowthLogicKit#populateDirectionProbabilityMap(GrowthLogicKitConfiguration, DirectionManipulationContext)
     */
    public int[] populateDirectionProbabilityMap(DirectionManipulationContext context) {
        return this.configurable.populateDirectionProbabilityMap(this, context);
    }

    /**
     * Invokes {@link GrowthLogicKit#getEnergy(GrowthLogicKitConfiguration, PositionalSpeciesContext)} for this configured
     * kit's growth logic kit.
     *
     * @param context the context
     * @return the direction for the signal to turn to
     * @see GrowthLogicKit#getEnergy(GrowthLogicKitConfiguration, PositionalSpeciesContext)
     */
    public float getEnergy(PositionalSpeciesContext context) {
        return this.configurable.getEnergy(this, context);
    }

    /**
     * Invokes {@link GrowthLogicKit#getLowestBranchHeight(GrowthLogicKitConfiguration, PositionalSpeciesContext)} for this
     * configured kit's growth logic kit.
     *
     * @param context the context
     * @return the direction for the signal to turn to
     * @see GrowthLogicKit#getLowestBranchHeight(GrowthLogicKitConfiguration, PositionalSpeciesContext)
     */
    public int getLowestBranchHeight(PositionalSpeciesContext context) {
        return this.configurable.getLowestBranchHeight(this, context);
    }

}
