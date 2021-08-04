package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.IPreGenFeature;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorld;

public class ClearVolumeGenFeature extends GenFeature implements IPreGenFeature {

    public static final ConfigurationProperty<Integer> HEIGHT = ConfigurationProperty.integer("height");

    public ClearVolumeGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(HEIGHT);
    }

    @Override
    public ConfiguredGenFeature<GenFeature> createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(HEIGHT, 8);
    }

    @Override
    public BlockPos preGeneration(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockPos rootPos, Species species, int radius, Direction facing, SafeChunkBounds safeBounds, JoCode joCode) {
        //Erase a volume of blocks that could potentially get in the way
        for (BlockPos pos : BlockPos.betweenClosed(rootPos.offset(new Vector3i(-1, 1, -1)), rootPos.offset(new Vector3i(1, configuredGenFeature.get(HEIGHT), 1)))) {
            world.removeBlock(pos, false);
        }
        return rootPos;
    }

}
