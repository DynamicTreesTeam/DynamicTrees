package com.ferreusveritas.dynamictrees.trees;

import java.util.Random;

import com.ferreusveritas.dynamictrees.VanillaTreeData;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.backport.Biome;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.IBlockAccess;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.backport.SpeciesRegistry;
import com.ferreusveritas.dynamictrees.api.backport.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.init.Blocks;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary.Type;

public class TreeBirch extends DynamicTree {
		
	public class SpeciesBirch extends Species {

		SpeciesBirch(DynamicTree treeFamily) {
			super(treeFamily.getName(), treeFamily);
			
			//Birch are tall, skinny, fast growing trees
			setBasicGrowingParameters(0.1f, 14.0f, 4, 4, 1.25f);
			
			envFactor(Type.COLD, 0.75f);
			envFactor(Type.HOT, 0.50f);
			envFactor(Type.DRY, 0.50f);
			envFactor(Type.FOREST, 1.05f);
		}
		
		@Override
		public boolean isBiomePerfect(Biome biome) {
			return isOneOfBiomes(biome, BiomeGenBase.birchForest, BiomeGenBase.birchForestHills);
		};
		
	}
	
	Species species;
	
	public TreeBirch() {
		super(VanillaTreeData.EnumType.BIRCH);
	}
	
	@Override
	public void createSpecies() {
		species = new SpeciesBirch(this);
	}
	
	@Override
	public void registerSpecies(SpeciesRegistry speciesRegistry) {
		speciesRegistry.register(species);
	}
	
	@Override
	public Species getCommonSpecies() {
		return species;
	}
	
	@Override
	public boolean rot(World world, BlockPos pos, int neighborCount, int radius, Random random) {
		if(super.rot(world, pos, neighborCount, radius, random)) {
			if(radius > 4 && TreeHelper.isRootyDirt(world, pos.down()) && world.getLightFor(EnumSkyBlock.Sky, pos) < 4) {
				world.setBlockState(pos, Blocks.brown_mushroom);//Change branch to a brown mushroom
				world.setBlockState(pos.down(), Blocks.dirt);//Change rooty dirt to dirt
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
