package com.ferreusveritas.dynamictrees.trees;

import java.util.Random;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.treedata.ISpecies;

import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

public class TreeBirch extends DynamicTree {
		
	public class SpeciesBirch extends Species {

		SpeciesBirch(DynamicTree treeFamily) {
			super(treeFamily.getName(), treeFamily);
			
			//Birch are tall, skinny, fast growing trees
			setBasicGrowingParameters(0.1f, 14.0f, 4, 4, 1.25f);
			
			setRetries(1);//Special fast growing
			
			envFactor(Type.COLD, 0.75f);
			envFactor(Type.HOT, 0.50f);
			envFactor(Type.DRY, 0.50f);
			envFactor(Type.FOREST, 1.05f);
		}
		
		@Override
		public boolean isBiomePerfect(Biome biome) {
			return isOneOfBiomes(biome, Biomes.BIRCH_FOREST, Biomes.BIRCH_FOREST_HILLS);
		};
		
	}
	
	ISpecies species;
	
	public TreeBirch() {
		super(BlockPlanks.EnumType.BIRCH);
	}
	
	@Override
	public void createSpecies() {
		species = new SpeciesBirch(this);
	}
	
	@Override
	public void registerSpecies(IForgeRegistry<ISpecies> speciesRegistry) {
		speciesRegistry.register(species);
	}
	
	@Override
	public ISpecies getCommonSpecies() {
		return species;
	}
	
	@Override
	public boolean rot(World world, BlockPos pos, int neighborCount, int radius, Random random) {
		if(super.rot(world, pos, neighborCount, radius, random)) {
			if(radius > 4 && TreeHelper.isRootyDirt(world, pos.down()) && world.getLightFor(EnumSkyBlock.SKY, pos) < 4) {
				world.setBlockState(pos, Blocks.BROWN_MUSHROOM.getDefaultState());//Change branch to a brown mushroom
				world.setBlockState(pos.down(), Blocks.DIRT.getDefaultState(), 3);//Change rooty dirt to dirt
			}
			return true;
		}
		
		return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public int foliageColorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos) {
		return ColorizerFoliage.getFoliageColorBirch();
	}
	
}
