package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.systems.DirtHelper;
import com.ferreusveritas.dynamictrees.systems.RootyBlockHelper;
import com.ferreusveritas.dynamictrees.systems.featuregen.VinesGenFeature;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class WarpedFungus extends CrimsonFungus {

	public class WarpedSpecies extends CrimsonSpecies {

		WarpedSpecies(TreeFamily treeFamily) {
			super(treeFamily.getName(), treeFamily);
			setupStandardSeedDropping();
		}

		@Override
		public boolean canSaplingGrow(World world, BlockPos pos) {
			BlockState soilState = world.getBlockState(pos.down());
			return soilState.getBlock() == Blocks.WARPED_NYLIUM || soilState.getBlock() == RootyBlockHelper.getRootyBlock(Blocks.WARPED_NYLIUM);
		}

		@Override
		public boolean isBiomePerfect(RegistryKey<Biome> biome) { return isOneOfBiomes(biome, Biomes.WARPED_FOREST); }

	}

	public WarpedFungus() {
		super(DynamicTrees.VanillaWoodTypes.warped);
		addConnectableVanillaLeaves((state) -> state.getBlock() == Blocks.WARPED_WART_BLOCK);
	}
	
	@Override
	public void createSpecies() {
		setCommonSpecies(new WarpedSpecies(this));
	}

}
