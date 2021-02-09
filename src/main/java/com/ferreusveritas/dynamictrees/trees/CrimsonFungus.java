package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.leaves.DynamicLeavesBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.DynamicWartBlock;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.systems.DirtHelper;
import com.ferreusveritas.dynamictrees.systems.RootyBlockHelper;
import com.ferreusveritas.dynamictrees.systems.featuregen.ClearVolumeGenFeature;
import com.ferreusveritas.dynamictrees.systems.featuregen.MoundGenFeature;
import com.ferreusveritas.dynamictrees.systems.featuregen.ShroomlightGenFeature;
import com.ferreusveritas.dynamictrees.systems.featuregen.VinesGenFeature;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Optional;

public class CrimsonFungus extends NetherFungusFamily {

	public class CrimsonSpecies extends BaseNetherFungiSpecies {

		CrimsonSpecies(TreeFamily family){
			super(family.getName(), family);
			setBasicGrowingParameters(0.3f, 14.0f, 0, 4, 1f);
			this.addGenFeature(new VinesGenFeature(Blocks.WEEPING_VINES_PLANT, VinesGenFeature.VineType.CEILING).setTipBlock(Blocks.WEEPING_VINES).setMaxLength(5).setQuantity(7));
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

			this.addGenFeature(new VinesGenFeature(Blocks.WEEPING_VINES_PLANT, VinesGenFeature.VineType.CEILING).setTipBlock(Blocks.WEEPING_VINES).setMaxLength(5).setQuantity(7));
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
