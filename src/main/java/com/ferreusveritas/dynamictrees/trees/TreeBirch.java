package com.ferreusveritas.dynamictrees.trees;

import java.util.Random;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;

import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary.Type;

public class TreeBirch extends TreeFamily {
		
	public class SpeciesBirch extends Species {
		
		SpeciesBirch(TreeFamily treeFamily) {
			super(treeFamily.getName(), treeFamily, ModBlocks.birchLeavesProperties);
			
			//Birch are tall, skinny, fast growing trees
			setBasicGrowingParameters(0.1f, 14.0f, 4, 4, 1.25f);
			
			envFactor(Type.COLD, 0.75f);
			envFactor(Type.HOT, 0.50f);
			envFactor(Type.DRY, 0.50f);
			envFactor(Type.FOREST, 1.05f);
			
			setupStandardSeedDropping();
		}
		
		@Override
		public boolean isBiomePerfect(Biome biome) {
			return isOneOfBiomes(biome, Biomes.BIRCH_FOREST, Biomes.BIRCH_FOREST_HILLS);
		};
		
		@Override
		public boolean rot(World world, BlockPos pos, int neighborCount, int radius, Random random) {
			if(super.rot(world, pos, neighborCount, radius, random)) {
				if(radius > 4 && TreeHelper.isRooty(world.getBlockState(pos.down())) && world.getLightFor(EnumSkyBlock.SKY, pos) < 4) {
					world.setBlockState(pos, Blocks.BROWN_MUSHROOM.getDefaultState());//Change branch to a brown mushroom
					world.setBlockState(pos.down(), Blocks.DIRT.getDefaultState(), 3);//Change rooty dirt to dirt
				}
				return true;
			}
			
			return false;
		}
		
	}
	
	public TreeBirch() {
		super(BlockPlanks.EnumType.BIRCH);
		ModBlocks.birchLeavesProperties.setTree(this);
		
		addConnectableVanillaLeaves(new IConnectable() {
			@Override
			public boolean isConnectable(IBlockState blockState) {
				return blockState.getBlock() instanceof BlockOldLeaf && (blockState.getValue(BlockOldLeaf.VARIANT) == BlockPlanks.EnumType.BIRCH);
			}
		});
	}
	
	@Override
	public void createSpecies() {
		setCommonSpecies(new SpeciesBirch(this));
	}
	
	@Override
	public int getRadiusForCellKit(IBlockAccess blockAccess, BlockPos pos, IBlockState blockState, EnumFacing dir, BlockBranch branch) {
		int radius = branch.getRadius(blockState, blockAccess, pos);
		if(radius == 1) {
			if(blockAccess.getBlockState(pos.down()).getBlock() == branch) {
				return 128;
			}
		}
		return radius;
	}
	
}
