package com.ferreusveritas.dynamictrees.systems.genfeature;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.systems.genfeature.context.PostGenerationContext;
import com.ferreusveritas.dynamictrees.systems.genfeature.context.PostGrowContext;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;

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
    public GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(MIN_RADIUS, 6);
    }

    @Override
    protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
        if (context.fertility() > 0) {
            this.flareBottom(configuration, context.level(), context.pos(), context.species());
            return true;
        }
        return false;
    }

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        this.flareBottom(configuration, context.level(), context.pos(), context.species());
        return true;
    }

    /**
     * Put a cute little flare on the bottom of the dark oaks
     *
     * @param level   The level
     * @param rootPos The position of the rooty dirt block of the tree
     */
    public void flareBottom(GenFeatureConfiguration configuration, LevelAccessor level, BlockPos rootPos, Species species) {
        Family family = species.getFamily();

        //Put a cute little flare on the bottom of the dark oaks
        int radius3 = TreeHelper.getRadius(level, rootPos.above(3));

        if (radius3 > configuration.get(MIN_RADIUS)) {
            family.getBranch().ifPresent(branch -> {
                branch.setRadius(level, rootPos.above(2), radius3 + 1, Direction.UP);
                branch.setRadius(level, rootPos.above(1), radius3 + 2, Direction.UP);
            });
        }
    }

}
