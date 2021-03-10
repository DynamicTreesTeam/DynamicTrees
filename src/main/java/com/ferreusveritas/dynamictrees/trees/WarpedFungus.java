package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.systems.RootyBlockHelper;
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

public class WarpedFungus extends NetherTreeFamily {

	public class WarpedSpecies extends BaseNetherFungiSpecies {

		WarpedSpecies(Family family) {
			super(family.getRegistryName(), family);

			this.setPrimitiveSapling(Blocks.WARPED_FUNGUS);
		}

		@Override
		public boolean canSaplingGrow(World world, BlockPos pos) {
			BlockState soilState = world.getBlockState(pos.down());
			return soilState.getBlock() == Blocks.WARPED_NYLIUM || soilState.getBlock() == RootyBlockHelper.getRootyBlock(Blocks.WARPED_NYLIUM);
		}

		@Override
		public boolean isBiomePerfect(RegistryKey<Biome> biome) { return isOneOfBiomes(biome, Biomes.WARPED_FOREST); }

//		@Override
//		public Species getMegaSpecies() {
//			return megaWarpedSpecies;
//		}
	}

	public class MegaWarpedSpecies extends MegaNetherFungiSpecies {

		MegaWarpedSpecies(Family family) {
			super(new ResourceLocation(family.getRegistryName().getNamespace(), "mega_"+ family.getRegistryName().getPath()), family);
		}

		@Override
		public boolean isBiomePerfect(RegistryKey<Biome> biome) { return isOneOfBiomes(biome, Biomes.WARPED_FOREST); }

	}

	public WarpedFungus() {
		super(DynamicTrees.VanillaWoodTypes.warped);

		addConnectableVanillaLeaves((state) -> state.getBlock() == Blocks.WARPED_WART_BLOCK);
	}

	@Override
	public Set<ResourceLocation> getExtraSpeciesNames() {
		return Sets.newHashSet(new ResourceLocation(DynamicTrees.MOD_ID, "mega_" + this.getRegistryName().getPath()));
	}

}
