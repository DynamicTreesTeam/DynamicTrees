package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.systems.RootyBlockHelper;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeatures;
import com.ferreusveritas.dynamictrees.systems.genfeatures.VinesGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.registries.IForgeRegistry;

import static com.ferreusveritas.dynamictrees.systems.genfeatures.VinesGenFeature.*;

public class CrimsonFungus extends NetherTreeFamily {

	private static final ConfiguredGenFeature<?> WEEPING_VINES_FEATURE = GenFeatures.VINES.with(VINE_TYPE, VinesGenFeature.VineType.CEILING)
			.with(VINE_BLOCK, Blocks.WEEPING_VINES_PLANT).with(TIP_BLOCK, Blocks.WEEPING_VINES).with(MAX_LENGTH, 5).with(QUANTITY, 7);

	public class CrimsonSpecies extends BaseNetherFungiSpecies {

		CrimsonSpecies(TreeFamily family){
			super(family.getName(), family);

			setBasicGrowingParameters(0.3f, 14.0f, 0, 4, 1f);

			this.addGenFeature(WEEPING_VINES_FEATURE);
		}

		@Override
		public boolean isBiomePerfect(RegistryKey<Biome> biome) { return isOneOfBiomes(biome, Biomes.CRIMSON_FOREST); }

		@Override
		public boolean canSaplingGrow(World world, BlockPos pos) {
			BlockState soilState = world.getBlockState(pos.down());
			return soilState.getBlock() == Blocks.CRIMSON_NYLIUM || soilState.getBlock() == RootyBlockHelper.getRootyBlock(Blocks.CRIMSON_NYLIUM);
		}

		@Override
		public Species getMegaSpecies() {
			return megaCrimsonSpecies;
		}
	}

	public class MegaCrimsonSpecies extends MegaNetherFungiSpecies {

		MegaCrimsonSpecies(TreeFamily treeFamily) {
			super(new ResourceLocation(treeFamily.getName().getNamespace(), "mega_"+treeFamily.getName().getPath()), treeFamily);

			setBasicGrowingParameters(1f, 25.0f, 7, 20, 0.9f);

			this.addGenFeature(WEEPING_VINES_FEATURE);
		}

		@Override
		public boolean isBiomePerfect(RegistryKey<Biome> biome) { return isOneOfBiomes(biome, Biomes.CRIMSON_FOREST); }

	}

	public CrimsonFungus(DynamicTrees.VanillaWoodTypes type) {
		super(type);
	}

	private Species megaCrimsonSpecies;
	public CrimsonFungus() {
		this(DynamicTrees.VanillaWoodTypes.crimson);

		addConnectableVanillaLeaves((state) -> state.getBlock() == Blocks.NETHER_WART_BLOCK);
	}
	
	@Override
	public void createSpecies() {
		megaCrimsonSpecies = new MegaCrimsonSpecies(this);
		setCommonSpecies(new CrimsonSpecies(this));
	}

	@Override
	public void registerSpecies(IForgeRegistry<Species> speciesRegistry) {
		super.registerSpecies(speciesRegistry);
		speciesRegistry.register(megaCrimsonSpecies);
	}

}
