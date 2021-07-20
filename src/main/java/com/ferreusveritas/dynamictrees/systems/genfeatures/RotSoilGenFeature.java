package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.PostRotContext;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

/**
 * A{@link GenFeature} handling post rot behaviour in which the soil below the base
 * branch is turned to the {@link #ROTTEN_SOIL} property block after that branch has
 * rotted away.
 *
 * @author Harley O'Connor
 */
public class RotSoilGenFeature extends GenFeature {

    public static final ConfigurationProperty<Block> ROTTEN_SOIL = ConfigurationProperty.block("rotten_soil");

    public RotSoilGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(ROTTEN_SOIL);
    }

    @Override
    protected ConfiguredGenFeature<GenFeature> createDefaultConfiguration() {
        return super.createDefaultConfiguration().with(ROTTEN_SOIL, Blocks.DIRT);
    }

    @Override
    protected boolean postRot(ConfiguredGenFeature<GenFeature> configuration, PostRotContext context) {
        final IWorld world = context.world();
        final BlockPos belowPos = context.pos().below();

        if (!TreeHelper.isRooty(world.getBlockState(belowPos))) {
            return false;
        }

        // Change rooty dirt to rotted soil.
        world.setBlock(belowPos, configuration.get(ROTTEN_SOIL).defaultBlockState(), 3);
        return true;
    }

}
