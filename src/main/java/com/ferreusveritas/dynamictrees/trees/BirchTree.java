package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.systems.genfeatures.BeeNestGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeatures;
import net.minecraft.block.Blocks;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.common.BiomeDictionary.Type;

import java.util.Random;

public class BirchTree extends VanillaTreeFamily {
	
	public class BirchSpecies extends Species {
		
		BirchSpecies(TreeFamily treeFamily) {
			super(treeFamily.getName(), treeFamily);
			
			//Birch are tall, skinny, fast growing trees
			setBasicGrowingParameters(0.1f, 14.0f, 4, 4, 1.25f);
			
			envFactor(Type.COLD, 0.75f);
			envFactor(Type.HOT, 0.50f);
			envFactor(Type.DRY, 0.50f);
			envFactor(Type.FOREST, 1.05f);
			
			setupStandardSeedDropping();
			setupStandardStickDropping();

			this.addGenFeature(GenFeatures.BEE_NEST);
		}
		
		@Override
		public boolean isBiomePerfect(RegistryKey<Biome> biome) {
			return isOneOfBiomes(biome, Biomes.BIRCH_FOREST, Biomes.BIRCH_FOREST_HILLS, Biomes.TALL_BIRCH_FOREST, Biomes.TALL_BIRCH_HILLS);
		}
		
		@Override
		public boolean rot(IWorld world, BlockPos pos, int neighborCount, int radius, Random random, boolean rapid) {
			if(super.rot(world, pos, neighborCount, radius, random, rapid)) {
				if(radius > 4 && TreeHelper.isRooty(world.getBlockState(pos.down())) && world.getLightFor(LightType.SKY, pos) < 4) {
					world.setBlockState(pos, Blocks.BROWN_MUSHROOM.getDefaultState(), 3);//Change branch to a brown mushroom
					world.setBlockState(pos.down(), Blocks.DIRT.getDefaultState(), 3);//Change rooty dirt to dirt
				}
				return true;
			}
			
			return false;
		}
		
	}
	
	public BirchTree() {
		super(DynamicTrees.VanillaWoodTypes.birch);
		hasConiferVariants = true;
		addConnectableVanillaLeaves((state) -> state.getBlock() == Blocks.BIRCH_LEAVES);
	}
	
	@Override
	public void createSpecies() {
		setCommonSpecies(new BirchSpecies(this));
	}

}
