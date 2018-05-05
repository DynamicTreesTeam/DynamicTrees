package com.ferreusveritas.dynamictrees.trees;

import java.util.Random;

import com.ferreusveritas.dynamictrees.ModConstants;

import net.minecraft.block.BlockMushroom;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class Mushroom extends Species {

	protected static final IBlockState dirtState = Blocks.DIRT.getDefaultState();
	
	protected final boolean red;
	
	public Mushroom(boolean red) {
		super();
		this.red = red;
		setRegistryName(new ResourceLocation(ModConstants.MODID, "mushroom" + (red ? "red" : "brn")));
		setStandardSoils();
	}
	
	@Override
	public boolean generate(World world, BlockPos pos, Biome biome, Random random, int radius) {
		BlockPos mushPos = pos.up();
		BlockMushroom shroom = (BlockMushroom)(red ? Blocks.RED_MUSHROOM : Blocks.BROWN_MUSHROOM);
		IBlockState originalSoil = world.getBlockState(pos);
		world.setBlockState(pos, dirtState);
		shroom.generateBigMushroom(world, mushPos, shroom.getDefaultState(), random);
		world.setBlockState(pos, originalSoil);
		return true;
	}
	
}
