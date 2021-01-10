package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import net.minecraft.block.Blocks;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.common.BiomeDictionary.Type;

import java.util.Random;

public class TreeBirch extends TreeFamilyVanilla {
	
	public class SpeciesBirch extends Species {
		
		SpeciesBirch(TreeFamily treeFamily) {
			super(treeFamily.getName(), treeFamily);
			
			//Birch are tall, skinny, fast growing trees
			setBasicGrowingParameters(0.1f, 14.0f, 4, 4, 1.25f);
			
			envFactor(Type.COLD, 0.75f);
			envFactor(Type.HOT, 0.50f);
			envFactor(Type.DRY, 0.50f);
			envFactor(Type.FOREST, 1.05f);
			
			setupStandardSeedDropping();
		}
		
		@Override
		public boolean isBiomePerfect(RegistryKey<Biome> biome) {
			return isOneOfBiomes(biome, Biomes.BIRCH_FOREST, Biomes.BIRCH_FOREST_HILLS);
		};
		
		@Override
		public boolean rot(World world, BlockPos pos, int neighborCount, int radius, Random random, boolean rapid) {
			if(super.rot(world, pos, neighborCount, radius, random, rapid)) {
				if(radius > 4 && TreeHelper.isRooty(world.getBlockState(pos.down())) && world.getLightFor(LightType.SKY, pos) < 4) {
					world.setBlockState(pos, Blocks.BROWN_MUSHROOM.getDefaultState());//Change branch to a brown mushroom
					world.setBlockState(pos.down(), Blocks.DIRT.getDefaultState(), 3);//Change rooty dirt to dirt
				}
				return true;
			}
			
			return false;
		}
		
	}
	
	public TreeBirch() {
		super(DynamicTrees.VanillaWoodTypes.birch);
		hasConiferVariants = true;
		addConnectableVanillaLeaves((state) -> state.getBlock() == Blocks.BIRCH_LEAVES);
	}
	
	@Override
	public void createSpecies() {
		setCommonSpecies(new SpeciesBirch(this));
	}
	
	@Override
	public boolean autoCreateBranch() {
		return true;
	}
	
}
