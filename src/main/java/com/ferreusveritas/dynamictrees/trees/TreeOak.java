package com.ferreusveritas.dynamictrees.trees;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.treedata.ISpecies;
import com.ferreusveritas.dynamictrees.special.BottomListenerPodzol;

import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class TreeOak extends DynamicTree {
	
	public class SpeciesOak extends Species {
		
		SpeciesOak(DynamicTree treeFamily) {
			super(treeFamily.getName(), treeFamily);
			
			//Oak trees are about as average as you can get
			setBasicGrowingParameters(0.3f, 12.0f, upProbability, lowestBranchHeight, 0.8f);
			
			envFactor(Type.COLD, 0.75f);
			envFactor(Type.HOT, 0.50f);
			envFactor(Type.DRY, 0.50f);
			envFactor(Type.FOREST, 1.05f);
		}
		
		@Override
		public boolean isBiomePerfect(Biome biome) {
			return isOneOfBiomes(biome, Biomes.FOREST, Biomes.FOREST_HILLS);
		}
		
	}
	
	public class SpeciesSwampOak extends Species {
		
		SpeciesSwampOak(DynamicTree treeFamily) {
			super(new ResourceLocation(treeFamily.getName().getResourceDomain(), treeFamily.getName().getResourcePath() + "swamp"), treeFamily);
			
			//Oak trees are about as average as you can get
			setBasicGrowingParameters(0.3f, 12.0f, upProbability, lowestBranchHeight, 0.8f);
			
			envFactor(Type.COLD, 0.50f);
			envFactor(Type.DRY, 0.50f);
		}
		
		@Override
		public boolean isBiomePerfect(Biome biome) {
			return isOneOfBiomes(biome, Biomes.SWAMPLAND);
		}
		
		@Override
		public boolean isAcceptableSoilForWorldgen(IBlockAccess blockAccess, BlockPos pos, IBlockState soilBlockState) {
			
			if(soilBlockState.getBlock() == Blocks.WATER) {
				Biome biome = blockAccess.getBiome(pos);
				if(BiomeDictionary.hasType(biome, Type.SWAMP)) {
					BlockPos down = pos.down();
					if(isAcceptableSoil(blockAccess, down, blockAccess.getBlockState(down))) {
						return true;
					}
				}
			}
			
			return super.isAcceptableSoilForWorldgen(blockAccess, pos, soilBlockState);
		}
		
		@Override
		public void postGeneration(World world, BlockPos pos, Biome biome, int radius, List<BlockPos> endPoints, boolean worldGen) {
			super.postGeneration(world, pos, biome, radius, endPoints, worldGen);
			
			// TODO Add Vines.
		}
	}

	ISpecies species;
	ISpecies swampSpecies;
	
	public TreeOak() {
		super(BlockPlanks.EnumType.OAK);
		
		registerBottomListener(new BottomListenerPodzol());
	}
	
	@Override
	public void createSpecies() {
		species = TreeRegistry.registerSpecies(new SpeciesOak(this));
		swampSpecies = TreeRegistry.registerSpecies(new SpeciesSwampOak(this));
	}
	
	@Override
	public ISpecies getCommonSpecies() {
		return species;
	}
	
	/**
	 * This will cause worldgen to select the swamp variation of the oak
	 * when the biome is appropriate for it.
	 * 
	 */
	@Override
	public ISpecies getSpeciesForLocation(IBlockAccess access, BlockPos pos) {
		if(BiomeDictionary.hasType(access.getBiome(pos), Type.SWAMP)) {
			return swampSpecies;
		}
		
		return getCommonSpecies();
	}
	
	@Override
	public boolean rot(World world, BlockPos pos, int neighborCount, int radius, Random random) {
		if(super.rot(world, pos, neighborCount, radius, random)) {
			if(radius > 4 && TreeHelper.isRootyDirt(world, pos.down()) && world.getLightFor(EnumSkyBlock.SKY, pos) < 4) {
				world.setBlockState(pos, random.nextInt(3) == 0 ? Blocks.RED_MUSHROOM.getDefaultState() : Blocks.BROWN_MUSHROOM.getDefaultState());//Change branch to a mushroom
				world.setBlockState(pos.down(), Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL));//Change rooty dirt to Podzol
			}
			return true;
		}
		
		return false;
	}
	
	@Override
	public ArrayList<ItemStack> getDrops(IBlockAccess blockAccess, BlockPos pos, int chance, ArrayList<ItemStack> drops) {
		Random rand = blockAccess instanceof World ? ((World)blockAccess).rand : new Random();
		if ((rand.nextInt(chance) == 0)) {
			drops.add(new ItemStack(Items.APPLE, 1, 0));
		}
		return drops;
	}
	
}
