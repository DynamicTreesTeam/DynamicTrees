package com.ferreusveritas.dynamictrees.growthlogic;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.growthlogic.context.DirectionManipulationContext;
import com.ferreusveritas.dynamictrees.growthlogic.context.DirectionSelectionContext;
import com.ferreusveritas.dynamictrees.growthlogic.context.PositionalSpeciesContext;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NetherFungusLogic extends GrowthLogicKit {

    public static final ConfigurationProperty<Integer> MIN_CAP_HEIGHT = ConfigurationProperty.integer("min_cap_height");

    public NetherFungusLogic(final ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected GrowthLogicKitConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(MIN_CAP_HEIGHT, 3)
                .with(HEIGHT_VARIATION, 8);
    }

    @Override
    protected void registerProperties() {
        this.register(MIN_CAP_HEIGHT, HEIGHT_VARIATION);
    }

    @Override
    public Direction selectNewDirection(GrowthLogicKitConfiguration configuration, DirectionSelectionContext context) {
        final Direction newDir = super.selectNewDirection(configuration, context);
        if (context.signal().isInTrunk() && newDir != Direction.UP) { // Turned out of trunk
            context.signal().energy = Math.min(context.signal().energy, context.species().isMegaSpecies() ? 3 : 2);
        }
        return newDir;
    }

    @Override
    public int[] populateDirectionProbabilityMap(GrowthLogicKitConfiguration configuration,
                                                 DirectionManipulationContext context) {
        final int[] probMap = super.populateDirectionProbabilityMap(configuration, context);

        if (context.signal().isInTrunk()) {
            if (TreeHelper.isBranch(context.world().getBlockState(context.pos().above())) &&
                    !TreeHelper.isBranch(context.world().getBlockState(context.pos().above(3)))) {
                context.probMap(new int[]{0, 0, 0, 0, 0, 0});
            } else if (!context.species().isMegaSpecies()) {
                for (Direction direction : CoordUtils.HORIZONTALS) {
                    if (TreeHelper.isBranch(
                            context.world().getBlockState(context.pos().offset(direction.getOpposite().getNormal())))) {
                        probMap[direction.get3DDataValue()] = 0;
                    }
                }
            }
            probMap[Direction.UP.get3DDataValue()] = 4;
        } else {
            probMap[Direction.UP.get3DDataValue()] = 0;
        }
        return probMap;
    }

    private float getHashedVariation(GrowthLogicKitConfiguration configuration, World world, BlockPos pos) {
        long day = world.getGameTime() / 24000L;
        int month = (int) day / 30;//Change the hashs every in-game month
        return (CoordUtils.coordHashCode(pos.above(month), 2) %
                configuration.get(HEIGHT_VARIATION));//Vary the height energy by a psuedorandom hash function
    }

    @Override
    public float getEnergy(GrowthLogicKitConfiguration configuration, PositionalSpeciesContext context) {
        return Math.min(configuration.getLowestBranchHeight(
                        new PositionalSpeciesContext(context.world(), context.pos(), context.species())) +
                        configuration.get(MIN_CAP_HEIGHT) +
                        getHashedVariation(configuration, context.world(), context.pos()) / 1.5f,
                super.getEnergy(configuration, context));
    }

    @Override
    public int getLowestBranchHeight(GrowthLogicKitConfiguration configuration, PositionalSpeciesContext context) {
        // Vary the lowest branch height by a psuedorandom hash function
        return (int) (super.getLowestBranchHeight(configuration, context) *
                context.species().biomeSuitability(context.world(), context.pos()) +
                getHashedVariation(configuration, context.world(), context.pos()));
    }

}
