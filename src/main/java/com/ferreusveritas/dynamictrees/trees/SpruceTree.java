package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.growthlogic.ConiferLogic;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.systems.featuregen.ClearVolumeGenFeature;
import com.ferreusveritas.dynamictrees.systems.featuregen.ConiferTopperGenFeature;
import com.ferreusveritas.dynamictrees.systems.featuregen.MoundGenFeature;
import com.ferreusveritas.dynamictrees.systems.featuregen.PodzolGenFeature;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Optional;

public class SpruceTree extends VanillaTreeFamily {
	
	public class BaseSpruceSpecies extends Species {
		
		BaseSpruceSpecies(ResourceLocation name, TreeFamily treeFamily) {
			super(name, treeFamily);
			
			//Spruce are conical thick slower growing trees
			setBasicGrowingParameters(0.25f, 16.0f, 3, 3, 0.9f);
			setGrowthLogicKit(TreeRegistry.findGrowthLogicKit(DTTrees.CONIFER));
			
			envFactor(Type.HOT, 0.50f);
			envFactor(Type.DRY, 0.25f);
			envFactor(Type.WET, 0.75f);
			
			setupStandardSeedDropping();
			
			//Add species features
			addGenFeature(new ConiferTopperGenFeature(getLeavesProperties()));
			addGenFeature(new PodzolGenFeature());
		}
		
		@Override
		public boolean isBiomePerfect(RegistryKey<Biome> biome) {
			return BiomeDictionary.hasType(biome, Type.CONIFEROUS);
		}
		
	}
	
	public class SpruceSpecies extends BaseSpruceSpecies {
		
		SpruceSpecies(TreeFamily treeFamily) {
			super(treeFamily.getName(), treeFamily);
		}

		@Override
		public Species getMegaSpecies() {
			return megaSpecies;
		}

	}

	protected boolean isLocationForMega(IWorld world, BlockPos trunkPos) {
		return Species.isOneOfBiomes(Species.getBiomeKey(world.getBiome(trunkPos)), Biomes.GIANT_TREE_TAIGA, Biomes.GIANT_TREE_TAIGA_HILLS);
	}

	public class MegaSpruceSpecies extends BaseSpruceSpecies {
		
		private static final String speciesName = "mega_spruce";
		
		MegaSpruceSpecies(TreeFamily treeFamily) {
			super(new ResourceLocation(treeFamily.getName().getNamespace(), speciesName), treeFamily);
			setBasicGrowingParameters(0.25f, 24.0f, 7, 5, 0.9f);
			setGrowthLogicKit(new ConiferLogic(5.0f));
			
			setSoilLongevity(16);//Grows for a while so it can actually get tall
			
			addGenFeature(new ClearVolumeGenFeature(8));//Clear a spot for the thick tree trunk
			addGenFeature(new MoundGenFeature(999));//Place a 3x3 of dirt under thick trees
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
		public int maxBranchRadius() {
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
		public boolean getRequiresTileEntity(IWorld world, BlockPos pos) {
			return !isLocationForMega(world, pos);
		}

	}
	
	Species megaSpecies;
	
	public SpruceTree() {
		super(DynamicTrees.VanillaWoodTypes.spruce);
		hasConiferVariants = true;
		addConnectableVanillaLeaves((state) -> state.getBlock() == Blocks.SPRUCE_LEAVES);
		
		//This will cause the mega spruce to be planted if the player is in a mega taiga biome
		addSpeciesLocationOverride((world, trunkPos) -> isLocationForMega(world, trunkPos) ? megaSpecies : Species.NULLSPECIES);
		
	}
	
	@Override
	public void createSpecies() {
		megaSpecies = new MegaSpruceSpecies(this);
		setCommonSpecies(new SpruceSpecies(this));
	}
	
	@Override
	public void registerSpecies(IForgeRegistry<Species> speciesRegistry) {
		super.registerSpecies(speciesRegistry);
		speciesRegistry.register(megaSpecies);
	}
	
	@Override
	public boolean isThick() {
		return true;
	}

}
