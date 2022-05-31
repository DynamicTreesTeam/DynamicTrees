package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.PreGenerationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;

public class ClearVolumeGenFeature extends GenFeature {

    public static final ConfigurationProperty<Integer> HEIGHT = ConfigurationProperty.integer("height");

    public ClearVolumeGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(HEIGHT);
    }

    @Override
    public GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(HEIGHT, 8);
    }

    @Override
    protected BlockPos preGenerate(GenFeatureConfiguration configuration, PreGenerationContext context) {
        final BlockPos rootPos = context.pos();

        // Erase a volume of blocks that could potentially get in the way.
        for (BlockPos pos : BlockPos.betweenClosed(
                rootPos.offset(new Vec3i(-1, 1, -1)),
                rootPos.offset(new Vec3i(1, configuration.get(HEIGHT), 1))
        )) {
            context.world().removeBlock(pos, false);
        }

        return rootPos;
    }

}
