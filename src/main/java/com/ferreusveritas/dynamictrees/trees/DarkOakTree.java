package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.systems.dropcreators.FruitDropCreator;
import com.ferreusveritas.dynamictrees.systems.genfeatures.*;
import net.minecraft.block.Blocks;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.common.BiomeDictionary.Type;

import java.util.Random;

import static com.ferreusveritas.dynamictrees.systems.genfeatures.BiomePredicateGenFeature.BIOME_PREDICATE;
import static com.ferreusveritas.dynamictrees.systems.genfeatures.BiomePredicateGenFeature.GEN_FEATURE;
import static com.ferreusveritas.dynamictrees.systems.genfeatures.HugeMushroomsGenFeature.MAX_ATTEMPTS;
import static com.ferreusveritas.dynamictrees.systems.genfeatures.HugeMushroomsGenFeature.MAX_MUSHROOMS;

public class DarkOakTree extends VanillaTreeFamily {
	
	public class DarkOakSpecies extends Species {
		
		protected HugeMushroomsGenFeature underGen;
		protected BottomFlareGenFeature flareBottomGen;
		protected RootsGenFeature rootGen;
		protected MoundGenFeature moundGen;
		
		DarkOakSpecies(TreeFamily treeFamily) {
			super(treeFamily.getName(), treeFamily);
			
			//Dark Oak Trees are tall, slowly growing, thick trees
			setBasicGrowingParameters(0.30f, 18.0f, 4, 6, 0.8f);
			setGrowthLogicKit(TreeRegistry.findGrowthLogicKit(DTTrees.DARK_OAK));
			
			setSoilLongevity(14);//Grows for a long long time
			
			envFactor(Type.COLD, 0.75f);
			envFactor(Type.HOT, 0.50f);
			envFactor(Type.DRY, 0.25f);
			envFactor(Type.MUSHROOM, 1.25f);
			
			if(DTConfigs.worldGen.get() && !DTConfigs.enableAppleTrees.get()) {
				addDropCreator(new FruitDropCreator());
			}
			
			setupStandardSeedDropping();
			setupStandardStickDropping();

			this.setPrimitiveSapling(Blocks.DARK_OAK_SAPLING);
			
			//Add species features
			this.addGenFeature(GenFeatures.CLEAR_VOLUME.with(ClearVolumeGenFeature.HEIGHT, 6)); // Clear a spot for the thick tree trunk.
			this.addGenFeature(GenFeatures.BOTTOM_FLARE); // Flare the bottom.
			this.addGenFeature(GenFeatures.MOUND); // Establish mounds.
			this.addGenFeature(GenFeatures.BIOME_PREDICATE
					.with(GEN_FEATURE, GenFeatures.HUGE_MUSHROOMS.with(MAX_MUSHROOMS, 1).with(MAX_ATTEMPTS, 3))
					.with(BIOME_PREDICATE, biome -> isBiomePerfect(getBiomeKey(biome)))); // Generate huge mushrooms.
			this.addGenFeature(GenFeatures.ROOTS); // Finally generate roots.
		}

		@Override
		public boolean isBiomePerfect(RegistryKey<Biome> biome) {
			return isOneOfBiomes(biome, Biomes.DARK_FOREST, Biomes.DARK_FOREST_HILLS);
		};
		
		@Override
		public int getLowestBranchHeight(World world, BlockPos pos) {
			return (int)(super.getLowestBranchHeight(world, pos) * biomeSuitability(world, pos));
		}
		
		@Override
		public float getEnergy(World world, BlockPos pos) {
			return super.getEnergy(world, pos) * biomeSuitability(world, pos);
		}
		
		@Override
		public float getGrowthRate(World world, BlockPos pos) {
			return super.getGrowthRate(world, pos) * biomeSuitability(world, pos);
		}
		
		@Override
		public boolean rot(IWorld world, BlockPos pos, int neighborCount, int radius, Random random, boolean rapid) {
			if(super.rot(world, pos, neighborCount, radius, random, rapid)) {
				if(radius > 2 && TreeHelper.isRooty(world.getBlockState(pos.down())) && world.getLightFor(LightType.SKY, pos) < 6) {
					world.setBlockState(pos, DTRegistries.blockStates.RED_MUSHROOM, 3);//Change branch to a red mushroom
					world.setBlockState(pos.down(), DTRegistries.blockStates.PODZOL, 3);//Change rooty dirt to Podzol
				}
				return true;
			}
			
			return false;
		}
		
		@Override
		public boolean isThick() {
			return true;
		}
		
	}

	public DarkOakTree() {
		super(DynamicTrees.VanillaWoodTypes.dark_oak);
		hasConiferVariants = true;

		addConnectableVanillaLeaves((state) -> state.getBlock() == Blocks.DARK_OAK_LEAVES);
	}
	
	@Override
	public void createSpecies() {
		setCommonSpecies(new DarkOakSpecies(this));
	}
	
	@Override
	public boolean isThick() {
		return true;
	}

	@Override
	public boolean hasSurfaceRoot() {
		return true;
	}

}
