package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.PreGenerationContext;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;

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
	public ConfiguredGenFeature<GenFeature> createDefaultConfiguration() {
		return super.createDefaultConfiguration()
				.with(HEIGHT, 8);
	}

	@Override
	protected BlockPos preGenerate(ConfiguredGenFeature<GenFeature> configuration, PreGenerationContext context) {
		final BlockPos rootPos = context.pos();

		// Erase a volume of blocks that could potentially get in the way.
		for (BlockPos pos : BlockPos.betweenClosed(
				rootPos.offset(new Vector3i(-1,  1, -1)),
				rootPos.offset(new Vector3i(1, configuration.get(HEIGHT), 1))
		)) {
			context.world().removeBlock(pos, false);
		}

		return rootPos;
	}

}
