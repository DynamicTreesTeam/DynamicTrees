package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.systems.RootyBlockHelper;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeatures;
import com.ferreusveritas.dynamictrees.systems.genfeatures.VinesGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.google.common.collect.Sets;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

import java.util.Set;

import static com.ferreusveritas.dynamictrees.systems.genfeatures.VinesGenFeature.*;

public class CrimsonFungus extends NetherTreeFamily {

	private static final ConfiguredGenFeature<?> WEEPING_VINES_FEATURE = GenFeatures.VINES.with(VINE_TYPE, VinesGenFeature.VineType.CEILING)
			.with(VINE_BLOCK, Blocks.WEEPING_VINES_PLANT).with(TIP_BLOCK, Blocks.WEEPING_VINES).with(MAX_LENGTH, 5).with(QUANTITY, 7);

	public class CrimsonSpecies extends BaseNetherFungiSpecies {

		CrimsonSpecies(Family family){
			super(family.getRegistryName(), family);

			this.setPrimitiveSapling(Blocks.CRIMSON_FUNGUS);

			this.addGenFeature(WEEPING_VINES_FEATURE);
		}

		@Override
		public boolean isBiomePerfect(RegistryKey<Biome> biome) { return isOneOfBiomes(biome, Biomes.CRIMSON_FOREST); }

		@Override
		public boolean canSaplingGrow(World world, BlockPos pos) {
			BlockState soilState = world.getBlockState(pos.down());
			return soilState.getBlock() == Blocks.CRIMSON_NYLIUM || soilState.getBlock() == RootyBlockHelper.getRootyBlock(Blocks.CRIMSON_NYLIUM);
		}

//		@Override
//		public Species getMegaSpecies() {
//			return megaCrimsonSpecies;
//		}
	}

	public class MegaCrimsonSpecies extends MegaNetherFungiSpecies {

		MegaCrimsonSpecies(Family family) {
			super(new ResourceLocation(family.getRegistryName().getNamespace(), "mega_"+ family.getRegistryName().getPath()), family);

			this.addGenFeature(WEEPING_VINES_FEATURE);
		}

		@Override
		public boolean isBiomePerfect(RegistryKey<Biome> biome) { return isOneOfBiomes(biome, Biomes.CRIMSON_FOREST); }

	}

	public CrimsonFungus(DynamicTrees.VanillaWoodTypes type) {
		super(type);
	}

	public CrimsonFungus() {
		this(DynamicTrees.VanillaWoodTypes.crimson);

		addConnectableVanillaLeaves((state) -> state.getBlock() == Blocks.NETHER_WART_BLOCK);
	}

	@Override
	public Set<ResourceLocation> getExtraSpeciesNames() {
		return Sets.newHashSet(new ResourceLocation(DynamicTrees.MOD_ID, "mega_" + this.getRegistryName().getPath()));
	}

}
