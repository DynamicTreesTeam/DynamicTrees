package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.items.Seed;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockPlanks;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Random;

import static com.ferreusveritas.dynamictrees.trees.Species.isOneOfBiomes;

public class TreeBirch extends TreeFamilyVanilla {

	public class SpeciesBirch extends Species {

		SpeciesBirch(TreeFamily treeFamily) {
			this(treeFamily.getName(), treeFamily);
		}

		public SpeciesBirch(ResourceLocation name, TreeFamily treeFamily) {
			super(name, treeFamily);

			// Birch are tall, skinny, fast growing trees.
			this.setBasicGrowingParameters(0.1f, 14.0f, 4, 4, 1.25f);

			this.envFactor(Type.COLD, 0.75f);
			this.envFactor(Type.HOT, 0.50f);
			this.envFactor(Type.DRY, 0.50f);
			this.envFactor(Type.FOREST, 1.05f);

			this.setupStandardSeedDropping();
		}

		@Override
		public boolean isBiomePerfect(Biome biome) {
			return isOneOfBiomes(biome, Biomes.BIRCH_FOREST, Biomes.BIRCH_FOREST_HILLS);
		}

		@Override
		public boolean rot(World world, BlockPos pos, int neighborCount, int radius, Random random, boolean rapid) {
			if (super.rot(world, pos, neighborCount, radius, random, rapid)) {
				if (radius > 4 && TreeHelper.isRooty(world.getBlockState(pos.down())) && world.getLightFor(EnumSkyBlock.SKY, pos) < 4) {
					world.setBlockState(pos, Blocks.BROWN_MUSHROOM.getDefaultState()); // Change branch to a brown mushroom.
					world.setBlockState(pos.down(), Blocks.DIRT.getDefaultState(), 3); // Change rooty dirt to regular dirt.
				}
				return true;
			}

			return false;
		}

	}
	
	public class SpeciesTallBirch extends SpeciesBirch {

		public SpeciesTallBirch(TreeFamily treeFamily) {
			super(new ResourceLocation(ModConstants.MODID, "tallbirch"), treeFamily);
			
			this.setBasicGrowingParameters(0.08F, 24.0F, 7, 7, 1.3F);
			this.setSoilLongevity(12);
		}

		@Override
		public boolean isBiomePerfect(Biome biome) {
			return isMutatedBirchForest(biome);
		}

		@Override
		public ItemStack getSeedStack(int qty) {
			return getCommonSpecies().getSeedStack(qty);
		}

		@Override
		public Seed getSeed() {
			return getCommonSpecies().getSeed();
		}
	}
	
	private static boolean isMutatedBirchForest(Biome biome) {
		return isOneOfBiomes(biome, Biomes.MUTATED_BIRCH_FOREST, Biomes.MUTATED_BIRCH_FOREST_HILLS);
	}
	
	Species tallSpecies;

	public TreeBirch() {
		super(BlockPlanks.EnumType.BIRCH);
		
		this.hasConiferVariants = true;
		this.addConnectableVanillaLeaves((state) -> 
			state.getBlock() instanceof BlockOldLeaf && (state.getValue(BlockOldLeaf.VARIANT) == BlockPlanks.EnumType.BIRCH)
		);
		this.addSpeciesLocationOverride(
			(world, trunkPos) -> isMutatedBirchForest(world.getBiome(trunkPos)) ? 
				this.tallSpecies : Species.NULLSPECIES
		);
	}

	@Override
	public void createSpecies() {
		this.setCommonSpecies(new SpeciesBirch(this));
		this.tallSpecies = new SpeciesTallBirch(this);
	}

	@Override
	public void registerSpecies(IForgeRegistry<Species> speciesRegistry) {
		super.registerSpecies(speciesRegistry);
		speciesRegistry.register(this.tallSpecies);
	}

}
