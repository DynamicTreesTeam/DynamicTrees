package com.ferreusveritas.dynamictrees.growthlogic;

import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.api.registry.ConfigurableRegistryEntry;
import com.ferreusveritas.dynamictrees.api.registry.Registry;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.growthlogic.context.DirectionManipulationContext;
import com.ferreusveritas.dynamictrees.growthlogic.context.EnergyContext;
import com.ferreusveritas.dynamictrees.growthlogic.context.LowestBranchHeightContext;
import com.ferreusveritas.dynamictrees.growthlogic.context.NewDirectionContext;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract class GrowthLogicKit extends ConfigurableRegistryEntry<GrowthLogicKit, ConfiguredGrowthLogicKit> {

    /**
     * Sets the amount of psuedorandom height variation added to a tree. Helpful to prevent all trees from turning out
     * the same height.
     */
    public static final ConfigurationProperty<Integer> HEIGHT_VARIATION = ConfigurationProperty.integer("height_variation");

    public static final GrowthLogicKit NULL_LOGIC = new GrowthLogicKit(DTTrees.NULL) {};

    /**
     * Central registry for all {@link GrowthLogicKit} objects.
     */
    public static final Registry<GrowthLogicKit> REGISTRY = new Registry<>(GrowthLogicKit.class, NULL_LOGIC);

    public GrowthLogicKit(final ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected ConfiguredGrowthLogicKit createDefaultConfiguration() {
        return new ConfiguredGrowthLogicKit(this);
    }

    @Override
    protected void registerProperties() {
    }

    @Nullable
    public Direction selectNewDirection(ConfiguredGrowthLogicKit configuration, World world, BlockPos pos, Species species, BranchBlock branch, GrowSignal signal) {
        return null;
    }

    public int[] directionManipulation(ConfiguredGrowthLogicKit configuration, DirectionManipulationContext context) {
        return context.probMap();
    }

    public Direction newDirectionSelected(ConfiguredGrowthLogicKit configuration, NewDirectionContext context) {
        return context.newDir();
    }

    public float getEnergy(ConfiguredGrowthLogicKit configuration, EnergyContext context) {
        return context.signalEnergy();
    }

    public int getLowestBranchHeight(ConfiguredGrowthLogicKit configuration, LowestBranchHeightContext context) {
        return context.lowestBranchHeight();
    }

}
