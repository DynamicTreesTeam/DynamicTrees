package com.ferreusveritas.dynamictrees.systems.genfeature;

import com.ferreusveritas.dynamictrees.api.configuration.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.systems.genfeature.context.PostGenerationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;

import java.util.Collections;
import java.util.Comparator;

public class ConiferTopperGenFeature extends GenFeature {

    public static final ConfigurationProperty<LeavesProperties> LEAVES_PROPERTIES = ConfigurationProperty.property("leaves_properties", LeavesProperties.class);

    public ConiferTopperGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(LEAVES_PROPERTIES);
    }

    @Override
    public GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(LEAVES_PROPERTIES, LeavesProperties.NULL);
    }

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        if (context.endPoints().isEmpty()) {
            return false;
        }

        final LevelAccessor level = context.level();

        // Find the highest end point.
        final BlockPos highest = Collections.max(context.endPoints(), Comparator.comparingInt(Vec3i::getY));
        // Fetch leaves properties property set or the default for the Species.
        final LeavesProperties leavesProperties = configuration.get(LEAVES_PROPERTIES)
                .elseIfInvalid(context.species().getLeavesProperties());

        // Manually place the highest few blocks of the conifer since the LeafCluster voxmap won't handle it.
        level.setBlock(highest.above(1), leavesProperties.getDynamicLeavesState(4), 3);
        level.setBlock(highest.above(2), leavesProperties.getDynamicLeavesState(3), 3);
        level.setBlock(highest.above(3), leavesProperties.getDynamicLeavesState(1), 3);

        return true;
    }

}
