package com.ferreusveritas.dynamictrees.trees;

import java.util.ArrayList;
import java.util.Random;

import com.ferreusveritas.dynamictrees.VanillaTreeData;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.backport.BlockAccess;
import com.ferreusveritas.dynamictrees.api.backport.BlockState;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.special.BottomListenerPodzol;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class TreeOak extends DynamicTree {
	
	public TreeOak() {
		super(VanillaTreeData.EnumType.OAK);
		
		//Oak trees are about as average as you can get
		setBasicGrowingParameters(0.3f, 12.0f, getUpProbability(), getLowestBranchHeight(), 0.8f);
		
		envFactor(Type.COLD, 0.75f);
		envFactor(Type.HOT, 0.50f);
		envFactor(Type.DRY, 0.50f);
		envFactor(Type.FOREST, 1.05f);
		
		registerBottomListener(new BottomListenerPodzol());
	}
	
	@Override
	public boolean isBiomePerfect(BiomeGenBase biome) {
		return isOneOfBiomes(biome, BiomeGenBase.forest, BiomeGenBase.forestHills);
	}
	
	@Override
	public boolean rot(World world, BlockPos pos, int neighborCount, int radius, Random random) {
		if(super.rot(world, pos, neighborCount, radius, random)) {
			if(radius > 4 && TreeHelper.isRootyDirt(world, pos.down()) && world.getLightFor(EnumSkyBlock.Sky, pos) < 4) {
				world.setBlockState(pos, new BlockState(random.nextInt(3) == 0 ? Blocks.red_mushroom : Blocks.brown_mushroom));//Change branch to a mushroom
				world.setBlockState(pos.down(), new BlockState(Blocks.dirt, 2), 3);//Change rooty dirt to Podzol
			}
			return true;
		}
		
		return false;
	}
	
	@Override
	public ArrayList<ItemStack> getDrops(BlockAccess blockAccess, BlockPos pos, int chance, ArrayList<ItemStack> drops) {
		Random rand = blockAccess instanceof World ? ((World)blockAccess).rand : new Random();
		if ((rand.nextInt(chance) == 0)) {
			drops.add(new ItemStack(Items.apple, 1, 0));
		}
		return drops;
	}
	
	@Override
	public boolean isAcceptableSoilForWorldgen(IBlockAccess blockAccessIn, BlockPos pos, IBlockState soilBlockState) {
		BlockAccess blockAccess = new BlockAccess(blockAccessIn);
		
		if(soilBlockState.getBlock() == Blocks.water) {
			BiomeGenBase biome = blockAccess.getBiome(pos);
			if(BiomeDictionary.isBiomeOfType(biome, Type.SWAMP)) {
				BlockPos down = pos.down();
				if(isAcceptableSoil(blockAccess, down, blockAccess.getBlockState(down))) {
					return true;
				}
			}
		}
		
		return super.isAcceptableSoilForWorldgen(blockAccess, pos, soilBlockState);
	}
	
}
