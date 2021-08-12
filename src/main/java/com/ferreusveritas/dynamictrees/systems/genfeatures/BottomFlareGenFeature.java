package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.PostGenerationContext;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.PostGrowContext;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class BottomFlareGenFeature extends GenFeature  {

    // Min radius for the flare.
    public static final ConfigurationProperty<Integer> MIN_RADIUS = ConfigurationProperty.integer("min_radius");

    public BottomFlareGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(MIN_RADIUS);
    }

    @Override
    public ConfiguredGenFeature createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(MIN_RADIUS, 6);
    }

    @Override
    protected boolean postGrow(ConfiguredGenFeature configuration, PostGrowContext context) {
        if (context.fertility() > 0) {
            this.flareBottom(configuration, context.world(), context.pos(), context.species());
            return true;
        }
        return false;
    }

    @Override
    protected boolean postGenerate(ConfiguredGenFeature configuration, PostGenerationContext context) {
        this.flareBottom(configuration, context.world(), context.pos(), context.species());
        return true;
    }

    /**
     * Put a cute little flare on the bottom of the dark oaks
     *
     * @param world   The world
     * @param rootPos The position of the rooty dirt block of the tree
     */
    public void flareBottom(ConfiguredGenFeature configuredGenFeature, IWorld world, BlockPos rootPos, Species species) {
        Family family = species.getFamily();

        //Put a cute little flare on the bottom of the dark oaks
        int radius3 = TreeHelper.getRadius(world, rootPos.above(3));

        if (radius3 > configuredGenFeature.get(MIN_RADIUS)) {
            family.getBranch().setRadius(world, rootPos.above(2), radius3 + 1, Direction.UP);
            family.getBranch().setRadius(world, rootPos.above(1), radius3 + 2, Direction.UP);
        }
    }

}
