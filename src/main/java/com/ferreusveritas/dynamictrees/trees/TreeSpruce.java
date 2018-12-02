package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.systems.featuregen.FeatureGenClearVolume;
import com.ferreusveritas.dynamictrees.systems.featuregen.FeatureGenConiferTopper;
import com.ferreusveritas.dynamictrees.systems.featuregen.FeatureGenMound;
import com.ferreusveritas.dynamictrees.systems.featuregen.FeatureGenPodzol;
import com.ferreusveritas.dynamictrees.util.CoordUtils;

import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockPlanks;
import net.minecraft.init.Biomes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
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
			super(name, treeFamily, ModBlocks.leaves.get("spruce"));
			
			//Spruce are conical thick slower growing trees
			setBasicGrowingParameters(0.25f, 16.0f, 3, 3, 0.9f);
			
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
		
		@Override
		protected int[] customDirectionManipulation(World world, BlockPos pos, int radius, GrowSignal signal, int probMap[]) {
			
			EnumFacing originDir = signal.dir.getOpposite();
			
			//Alter probability map for direction change
			probMap[0] = 0;//Down is always disallowed for spruce
			probMap[1] = signal.isInTrunk() ? getUpProbability(): 0;
			probMap[2] = probMap[3] = probMap[4] = probMap[5] = //Only allow turns when we aren't in the trunk(or the branch is not a twig and step is odd)
					!signal.isInTrunk() || (signal.isInTrunk() && signal.numSteps % 2 == 1 && radius > 1) ? 2 : 0;
			probMap[originDir.ordinal()] = 0;//Disable the direction we came from
			probMap[signal.dir.ordinal()] += signal.isInTrunk() ? 0 : signal.numTurns == 1 ? 2 : 1;//Favor current travel direction 
			
			return probMap;
		}
		
		@Override
		protected EnumFacing newDirectionSelected(EnumFacing newDir, GrowSignal signal) {
			if(signal.isInTrunk() && newDir != EnumFacing.UP){//Turned out of trunk
				signal.energy /= 3.0f;
			}
			return newDir;
		}
		
		//Spruce trees are so similar that it makes sense to randomize their height for a little variation
		//but we don't want the trees to always be the same height all the time when planted in the same location
		//so we feed the hash function the in-game month
		@Override
		public float getEnergy(World world, BlockPos pos) {
			long day = world.getTotalWorldTime() / 24000L;
			int month = (int)day / 30;//Change the hashs every in-game month
			
			return super.getEnergy(world, pos) * biomeSuitability(world, pos) + (CoordUtils.coordHashCode(pos.up(month), 2) % 5);//Vary the height energy by a psuedorandom hash function
		}
		
	}
	
	public class SpeciesSpruce extends SpeciesBaseSpruce {
		
		SpeciesSpruce(TreeFamily treeFamily) {
			super(treeFamily.getName(), treeFamily);
		}
		
		@Override
		public boolean isThick() {
			return false;
		}
		
	}
	
	public class SpeciesMegaSpruce extends SpeciesBaseSpruce {
		
		private static final String speciesName = "megaspruce";
		
		SpeciesMegaSpruce(TreeFamily treeFamily) {
			super(new ResourceLocation(treeFamily.getName().getResourceDomain(), speciesName), treeFamily);
			setBasicGrowingParameters(0.25f, 24.0f, 7, 5, 0.9f);
			setSoilLongevity(16);//Grows for a while so it can actually get tall
			
			addGenFeature(new FeatureGenClearVolume(8));//Clear a spot for the thick tree trunk
			addGenFeature(new FeatureGenMound(this, 999));//Place a 3x3 of dirt under thick trees
		}
		
		@Override
		protected EnumFacing newDirectionSelected(EnumFacing newDir, GrowSignal signal) {
			if(signal.isInTrunk() && newDir != EnumFacing.UP){//Turned out of trunk
				signal.energy /= 5f;
			}
			return newDir;
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
	}
	
	Species megaSpecies;
	
	public TreeSpruce() {
		super(BlockPlanks.EnumType.SPRUCE);
		ModBlocks.leaves.get("spruce").setTree(this);
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
	
}
