package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.systems.RootyBlockHelper;
import com.ferreusveritas.dynamictrees.systems.featuregen.VinesGenFeature;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.registries.IForgeRegistry;

public class WarpedFungus extends NetherTreeFamily {

	public class WarpedSpecies extends BaseNetherFungiSpecies {

		WarpedSpecies(TreeFamily treeFamily) {
			super(treeFamily.getName(), treeFamily);

			this.addGenFeature(new VinesGenFeature(Blocks.TWISTING_VINES_PLANT, VinesGenFeature.VineType.FLOOR).setTipBlock(Blocks.TWISTING_VINES).setMaxLength(5).setQuantity(7));
		}

		@Override
		public boolean canSaplingGrow(World world, BlockPos pos) {
			BlockState soilState = world.getBlockState(pos.down());
			return soilState.getBlock() == Blocks.WARPED_NYLIUM || soilState.getBlock() == RootyBlockHelper.getRootyBlock(Blocks.WARPED_NYLIUM);
		}

		@Override
		public boolean isBiomePerfect(RegistryKey<Biome> biome) { return isOneOfBiomes(biome, Biomes.WARPED_FOREST); }

		@Override
		public Species getMegaSpecies() {
			return megaWarpedSpecies;
		}
	}

	public class MegaWarpedSpecies extends MegaNetherFungiSpecies {

		MegaWarpedSpecies(TreeFamily treeFamily) {
			super(new ResourceLocation(treeFamily.getName().getNamespace(), "mega_"+treeFamily.getName().getPath()), treeFamily);

			setBasicGrowingParameters(1f, 25.0f, 7, 20, 0.9f);

			this.addGenFeature(new VinesGenFeature(Blocks.TWISTING_VINES_PLANT, VinesGenFeature.VineType.FLOOR).setTipBlock(Blocks.TWISTING_VINES).setMaxLength(5).setQuantity(7));
		}

		@Override
		public boolean isBiomePerfect(RegistryKey<Biome> biome) { return isOneOfBiomes(biome, Biomes.WARPED_FOREST); }

	}

	private Species megaWarpedSpecies;
	public WarpedFungus() {
		super(DynamicTrees.VanillaWoodTypes.warped);

		addConnectableVanillaLeaves((state) -> state.getBlock() == Blocks.WARPED_WART_BLOCK);
	}
	
	@Override
	public void createSpecies() {
		megaWarpedSpecies = new MegaWarpedSpecies(this);
		setCommonSpecies(new WarpedSpecies(this));
	}

	@Override
	public void registerSpecies(IForgeRegistry<Species> speciesRegistry) {
		super.registerSpecies(speciesRegistry);
		speciesRegistry.register(megaWarpedSpecies);
	}
}
