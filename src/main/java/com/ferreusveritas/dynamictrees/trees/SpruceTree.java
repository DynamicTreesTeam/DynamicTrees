package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKits;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.systems.genfeatures.ConiferTopperGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeatures;
import com.ferreusveritas.dynamictrees.systems.genfeatures.MoundGenFeature;
import com.google.common.collect.Sets;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

import java.util.Optional;
import java.util.Set;

public class SpruceTree extends VanillaTreeFamily {
	
	public static class BaseSpruceSpecies extends Species {
		
		BaseSpruceSpecies(ResourceLocation name, TreeFamily treeFamily) {
			super(name, treeFamily);
			
			//Spruce are conical thick slower growing trees
			setBasicGrowingParameters(0.25f, 16.0f, 3, 3, 0.9f);
			setGrowthLogicKit(GrowthLogicKits.CONIFER);
			
			envFactor(Type.HOT, 0.50f);
			envFactor(Type.DRY, 0.25f);
			envFactor(Type.WET, 0.75f);
			
			setupStandardSeedDropping();
			setupStandardStickDropping();

			this.setPrimitiveSapling(Blocks.SPRUCE_SAPLING);
			
			//Add species features
			this.addGenFeature(GenFeatures.CONIFER_TOPPER.with(ConiferTopperGenFeature.LEAVES_PROPERTIES, this.getLeavesProperties()));
			this.addGenFeature(GenFeatures.PODZOL);
		}
		
		@Override
		public boolean isBiomePerfect(RegistryKey<Biome> biome) {
			return BiomeDictionary.hasType(biome, Type.CONIFEROUS);
		}
		
	}
	
	public class SpruceSpecies extends BaseSpruceSpecies {
		
		SpruceSpecies(TreeFamily treeFamily) {
			super(treeFamily.getRegistryName(), treeFamily);
		}

//		@Override
//		public Species getMegaSpecies() {
//			return megaSpecies;
//		}

	}

	protected boolean isLocationForMega(IWorld world, BlockPos trunkPos) {
		return Species.isOneOfBiomes(Species.getBiomeKey(world.getBiome(trunkPos)), Biomes.GIANT_TREE_TAIGA, Biomes.GIANT_TREE_TAIGA_HILLS);
	}

	public class MegaSpruceSpecies extends BaseSpruceSpecies {
		
		private static final String speciesName = "mega_spruce";
		
		MegaSpruceSpecies(TreeFamily treeFamily) {
			super(new ResourceLocation(treeFamily.getRegistryName().getNamespace(), speciesName), treeFamily);
			setBasicGrowingParameters(0.25f, 24.0f, 7, 5, 0.9f);
			setGrowthLogicKit(GrowthLogicKits.MEGA_CONIFER);
			
			setSoilLongevity(16);//Grows for a while so it can actually get tall

			//This will cause the mega spruce to be planted if the player is in a mega taiga biome.
			this.commonOverride = SpruceTree.this::isLocationForMega;
			
			this.addGenFeature(GenFeatures.CLEAR_VOLUME);//Clear a spot for the thick tree trunk
			this.addGenFeature(GenFeatures.MOUND.with(MoundGenFeature.MOUND_CUTOFF_RADIUS, 999));//Place a 3x3 of dirt under thick trees
		}
		
		//Mega spruce are just spruce in a mega taiga..  So they have the same seeds
		@Override
		public ItemStack getSeedStack(int qty) {
			return getCommonSpecies().getSeedStack(qty);
		}
		
		//Mega spruce are just spruce in a mega taiga..  So they have the same seeds
		@Override
		public Optional<Seed> getSeed() {
			return getCommonSpecies().getSeed();
		}
		
		@Override
		public int getMaxBranchRadius() {
			return 24;
		}
		
		@Override
		public boolean isThick() {
			return true;
		}

		@Override
		public boolean isMega() {
			return true;
		}

		@Override
		public boolean doesRequireTileEntity(IWorld world, BlockPos pos) {
			return !isLocationForMega(world, pos);
		}

	}

	public SpruceTree() {
		super(DynamicTrees.VanillaWoodTypes.spruce);
		hasConiferVariants = true;
		addConnectableVanillaLeaves((state) -> state.getBlock() == Blocks.SPRUCE_LEAVES);
	}

	@Override
	public Set<ResourceLocation> getExtraSpeciesNames() {
		return Sets.newHashSet(new ResourceLocation(DynamicTrees.MOD_ID, "mega_" + this.getRegistryName().getPath()));
	}

	@Override
	public boolean isThick() {
		return true;
	}

}
