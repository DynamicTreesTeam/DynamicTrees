package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.ModTrees;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.growthlogic.ConiferLogic;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.systems.featuregen.FeatureGenClearVolume;
import com.ferreusveritas.dynamictrees.systems.featuregen.FeatureGenConiferTopper;
import com.ferreusveritas.dynamictrees.systems.featuregen.FeatureGenMound;
import com.ferreusveritas.dynamictrees.systems.featuregen.FeatureGenPodzol;

import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockPlanks;
import net.minecraft.init.Biomes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.registries.IForgeRegistry;

public class TreeSpruce extends TreeFamilyVanilla {
	
	public class SpeciesBaseSpruce extends Species {
		
		SpeciesBaseSpruce(ResourceLocation name, TreeFamily treeFamily) {
			super(name, treeFamily);
			
			//Spruce are conical thick slower growing trees
			setBasicGrowingParameters(0.25f, 16.0f, 3, 3, 0.9f);
			setGrowthLogicKit(TreeRegistry.findGrowthLogicKit(ModTrees.CONIFER));
			
			envFactor(Type.HOT, 0.50f);
			envFactor(Type.DRY, 0.25f);
			envFactor(Type.WET, 0.75f);
			
			setupStandardSeedDropping();
			
			//Add species features
			addGenFeature(new FeatureGenConiferTopper(getLeavesProperties()));
			addGenFeature(new FeatureGenPodzol());
		}
		
		@Override
		public boolean isBiomePerfect(Biome biome) {
			return BiomeDictionary.hasType(biome, Type.CONIFEROUS);
		}
		
	}
	
	public class SpeciesSpruce extends SpeciesBaseSpruce {
		
		SpeciesSpruce(TreeFamily treeFamily) {
			super(treeFamily.getName(), treeFamily);
		}
		
	}
	
	public class SpeciesMegaSpruce extends SpeciesBaseSpruce {
		
		private static final String speciesName = "megaspruce";
		
		SpeciesMegaSpruce(TreeFamily treeFamily) {
			super(new ResourceLocation(treeFamily.getName().getResourceDomain(), speciesName), treeFamily);
			setBasicGrowingParameters(0.25f, 24.0f, 7, 5, 0.9f);
			setGrowthLogicKit(new ConiferLogic(5.0f));
			
			setSoilLongevity(16);//Grows for a while so it can actually get tall
			
			addGenFeature(new FeatureGenClearVolume(8));//Clear a spot for the thick tree trunk
			addGenFeature(new FeatureGenMound(999));//Place a 3x3 of dirt under thick trees
		}
		
		//Mega spruce are just spruce in a mega taiga..  So they have the same seeds
		@Override
		public ItemStack getSeedStack(int qty) {
			return getCommonSpecies().getSeedStack(qty);
		}
		
		//Mega spruce are just spruce in a mega taiga..  So they have the same seeds
		@Override
		public Seed getSeed() {
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
	}
	
	Species megaSpecies;
	
	public TreeSpruce() {
		super(BlockPlanks.EnumType.SPRUCE);
		hasConiferVariants = true;
		addConnectableVanillaLeaves((state) -> { return state.getBlock() instanceof BlockOldLeaf && (state.getValue(BlockOldLeaf.VARIANT) == BlockPlanks.EnumType.SPRUCE); });
		
		//This will cause the mega spruce to be planted if the player is in a mega taiga biome
		addSpeciesLocationOverride(new ISpeciesLocationOverride() {
			@Override
			public Species getSpeciesForLocation(World access, BlockPos trunkPos) {
				if(Species.isOneOfBiomes(access.getBiome(trunkPos), Biomes.REDWOOD_TAIGA, Biomes.REDWOOD_TAIGA_HILLS)) {
					return megaSpecies;
				}
				return Species.NULLSPECIES;
			}
		});
		
	}
	
	@Override
	public void createSpecies() {
		megaSpecies = new SpeciesMegaSpruce(this);
		setCommonSpecies(new SpeciesSpruce(this));
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
	
	@Override
	public boolean autoCreateBranch() {
		return true;
	}
}
