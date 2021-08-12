package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.PostGenerationContext;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorld;

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
    public ConfiguredGenFeature createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(LEAVES_PROPERTIES, LeavesProperties.NULL_PROPERTIES);
    }

    @Override
    protected boolean postGenerate(ConfiguredGenFeature configuration, PostGenerationContext context) {
        if (context.endPoints().isEmpty()) {
            return false;
        }

        final IWorld world = context.world();

        // Find the highest end point.
        final BlockPos highest = Collections.max(context.endPoints(), Comparator.comparingInt(Vector3i::getY));
        // Fetch leaves properties property set or the default for the Species.
        final LeavesProperties leavesProperties = configuration.get(LEAVES_PROPERTIES)
                .elseIfInvalid(context.species().getLeavesProperties());

        // Manually place the highest few blocks of the conifer since the LeafCluster voxmap won't handle it.
        world.setBlock(highest.above(1), leavesProperties.getDynamicLeavesState(4), 3);
        world.setBlock(highest.above(2), leavesProperties.getDynamicLeavesState(3), 3);
        world.setBlock(highest.above(3), leavesProperties.getDynamicLeavesState(1), 3);

        return true;
    }

}
