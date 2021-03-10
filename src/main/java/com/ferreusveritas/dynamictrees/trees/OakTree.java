package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.systems.dropcreators.FruitDropCreator;
import com.ferreusveritas.dynamictrees.systems.genfeatures.FruitGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeatures;
import com.ferreusveritas.dynamictrees.systems.genfeatures.VinesGenFeature;
import com.google.common.collect.Sets;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

import java.util.Optional;
import java.util.Random;
import java.util.Set;

public class OakTree extends VanillaTreeFamily {
	
	public static class OakSpecies extends Species {
		
		OakSpecies(Family family) {
			super(family.getRegistryName(), family);
			
			//Oak trees are about as average as you can get
			setBasicGrowingParameters(0.3f, 12.0f, upProbability, lowestBranchHeight, 0.8f);
			
			envFactor(Type.COLD, 0.75f);
			envFactor(Type.HOT, 0.50f);
			envFactor(Type.DRY, 0.50f);
			envFactor(Type.FOREST, 1.05f);
			
			if(DTConfigs.worldGen.get() && !DTConfigs.enableAppleTrees.get()) {//If we've disabled apple trees we still need some way to get apples.
				addDropCreator(new FruitDropCreator());
			}

			setupStandardSeedDropping();
			setupStandardStickDropping();

			this.setPrimitiveSapling(Blocks.OAK_SAPLING);

			this.addGenFeature(GenFeatures.BEE_NEST);
		}
		
		@Override
		public boolean isBiomePerfect(RegistryKey<Biome> biome) {
			return BiomeDictionary.hasType(biome, Type.FOREST) && BiomeDictionary.hasType(biome, Type.OVERWORLD);
		}

		@Override
		public boolean rot(IWorld world, BlockPos pos, int neighborCount, int radius, Random random, boolean rapid) {
			if(super.rot(world, pos, neighborCount, radius, random, rapid)) {
				if(radius > 4 && TreeHelper.isRooty(world.getBlockState(pos.down())) && world.getLightFor(LightType.SKY, pos) < 4) {
					world.setBlockState(pos, random.nextInt(3) == 0 ? DTRegistries.blockStates.RED_MUSHROOM : DTRegistries.blockStates.BROWN_MUSHROOM, 3);//Change branch to a mushroom
					world.setBlockState(pos.down(), DTRegistries.blockStates.PODZOL, 3);//Change rooty dirt to Podzol
				}
				return true;
			}
			
			return false;
		}
		
	}
	
	/**
	 * Swamp Oaks are just Oaks with slight growth differences that can generate in water
	 * and with vines hanging from their leaves.
	 */
	public class SwampOakSpecies extends Species {
				
		SwampOakSpecies(Family family) {
			super(new ResourceLocation(family.getRegistryName().getNamespace(), "swamp_" + family.getRegistryName().getPath()), family);
			
			setBasicGrowingParameters(0.3f, 12.0f, upProbability, lowestBranchHeight, 0.8f);
			
			envFactor(Type.COLD, 0.50f);
			envFactor(Type.DRY, 0.50f);
			
			setupStandardSeedDropping();

			this.commonOverride = (world, trunkPos) -> BiomeDictionary.hasType(getBiomeKey(world.getBiome(trunkPos)), Type.SWAMP);
			
			//Add species features
			this.addGenFeature(GenFeatures.VINES.with(VinesGenFeature.MAX_LENGTH, 7)
					.with(VinesGenFeature.VERTICAL_SPREAD, 30f).with(VinesGenFeature.RAY_DISTANCE, 6f)
					.with(VinesGenFeature.QUANTITY, 24)); // Generate Vines
		}
		
		@Override
		public boolean isBiomePerfect(RegistryKey<Biome> biome) {
			return BiomeDictionary.hasType(biome, Type.SWAMP);
		}
		
		@Override
		public boolean isAcceptableSoilForWorldgen(IWorld world, BlockPos pos, BlockState soilBlockState) {
			
			if(DTConfigs.enableSwampOaksInWater.get() && soilBlockState.getBlock() == Blocks.WATER) {
				Biome biome = world.getBiome(pos);
				if(BiomeDictionary.hasType(getBiomeKey(biome), Type.SWAMP)) {
					BlockPos down = pos.down();
					if(isAcceptableSoil(world, down, world.getBlockState(down))) {
						return true;
					}
				}
			}
			
			return super.isAcceptableSoilForWorldgen(world, pos, soilBlockState);
		}
		
		//Swamp Oaks are just oaks in a swamp..  So they have the same seeds
		@Override
		public ItemStack getSeedStack(int qty) {
			return getCommonSpecies().getSeedStack(qty);
		}
		
		//Swamp Oaks are just oaks in a swamp..  So they have the same seeds
		@Override
		public Optional<Seed> getSeed() {
			return getCommonSpecies().getSeed();
		}
		
		@Override
		public boolean rot(IWorld world, BlockPos pos, int neighborCount, int radius, Random random, boolean rapid) {
			if(super.rot(world, pos, neighborCount, radius, random, rapid)) {
				if(radius > 4 && TreeHelper.isRooty(world.getBlockState(pos.down())) && world.getLightFor(LightType.SKY, pos) < 4) {
					world.setBlockState(pos, random.nextInt(3) == 0 ? DTRegistries.blockStates.RED_MUSHROOM : DTRegistries.blockStates.BROWN_MUSHROOM, 3);//Change branch to a mushroom
					world.setBlockState(pos.down(), DTRegistries.blockStates.PODZOL, 3);//Change rooty dirt to Podzol
				}
				return true;
			}
			
			return false;
		}
		
	}
	
	/**
	 * This species drops no seeds at all.  One must craft the seed from an apple.
	 */
	public static class AppleOakSpecies extends Species {
		
		private static final String SPECIES_NAME = "apple_oak";
		
		public AppleOakSpecies(Family family) {
			super(new ResourceLocation(family.getRegistryName().getNamespace(), SPECIES_NAME), family);
			
//			setRequiresTileEntity(true);
			
			//A bit stockier, smaller and slower than your basic oak
			setBasicGrowingParameters(0.4f, 10.0f, 1, 4, 0.7f);

			envFactor(Type.COLD, 0.75f);
			envFactor(Type.HOT, 0.75f);
			envFactor(Type.DRY, 0.25f);
			
			generateSeed();
			generateSapling();

			DTRegistries.appleBlock.setSpecies(this);
			this.addGenFeature(GenFeatures.FRUIT.with(FruitGenFeature.RAY_DISTANCE, 4f));
		}
		
		@Override
		public boolean isBiomePerfect(RegistryKey<Biome> biome) {
			return biome.equals(Biomes.PLAINS);
		}
		
	}

	public OakTree() {
		super(DynamicTrees.VanillaWoodTypes.oak);
		hasConiferVariants = true;
		addConnectableVanillaLeaves((state) -> state.getBlock() == Blocks.OAK_LEAVES);
	}

	// TODO: Eww... This needs changing.
	@Override
	public Set<Species> createSpecies() {
		final Set<Species> species = super.createSpecies();
		final Species appleOak = new Species(new ResourceLocation(DynamicTrees.MOD_ID, "apple_oak"), this);

		appleOak.generateSeed();
		appleOak.generateSapling();

		DTRegistries.appleBlock.setSpecies(appleOak);
		species.add(appleOak);
		return species;
	}

	@Override
	public Set<ResourceLocation> getExtraSpeciesNames() {
		return Sets.newHashSet(new ResourceLocation(DynamicTrees.MOD_ID, "swamp_oak"));
	}

}
