package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.systems.DirtHelper;
import com.ferreusveritas.dynamictrees.systems.RootyBlockHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class CrimsonFungus extends VanillaTreeFamily {

	public class CrimsonSpecies extends Species {

		CrimsonSpecies(TreeFamily treeFamily) {
			super(treeFamily.getName(), treeFamily);

			setBasicGrowingParameters(0.15f, 12.0f, 0, 3, 0.7f);

			envFactor(Type.COLD, 0.25f);
			envFactor(Type.WET, 0.75f);

			setupStandardSeedDropping();
		}

		@Override
		protected void setStandardSoils() {
			addAcceptableSoils(DirtHelper.DIRTLIKE, DirtHelper.FUNGUSLIKE);
		}

		@Override
		public boolean isBiomePerfect(RegistryKey<Biome> biome) {
			return BiomeDictionary.hasType(biome, Type.NETHER);
		}

		@Override
		public SoundType getSaplingSound() {
			return SoundType.FUNGUS;
		}

		@Override
		public boolean canSaplingGrow(World world, BlockPos pos) {
			BlockState soilState = world.getBlockState(pos.down());
			return soilState.getBlock() == Blocks.CRIMSON_NYLIUM || soilState.getBlock() == RootyBlockHelper.getRootyBlock(Blocks.CRIMSON_NYLIUM);
		}
	}

	public CrimsonFungus() {
		this(DynamicTrees.VanillaWoodTypes.crimson);
	}
	public CrimsonFungus(DynamicTrees.VanillaWoodTypes type) {
		super(type);
		addConnectableVanillaLeaves((state) -> state.getBlock() == Blocks.NETHER_WART_BLOCK);
	}
	
	@Override
	public void createSpecies() {
		setCommonSpecies(new CrimsonSpecies(this));
	}

}
