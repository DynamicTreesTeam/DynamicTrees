package com.ferreusveritas.dynamictrees.trees;

import java.util.Random;

import com.ferreusveritas.dynamictrees.ModConstants;

import net.minecraft.block.BlockMushroom;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class Mushroom extends Species {

	protected final boolean red;
	
	public Mushroom(boolean red) {
		super();
		this.red = red;
		setRegistryName(new ResourceLocation(ModConstants.MODID, "mushroom" + (red ? "red" : "brn")));
		setStandardSoils();
	}
	
	@Override
	public boolean generate(World world, BlockPos pos, Biome biome, Random random, int radius) {
		pos = pos.up();
		BlockMushroom shroom = (BlockMushroom)(red ? Blocks.RED_MUSHROOM : Blocks.BROWN_MUSHROOM);
		shroom.generateBigMushroom(world, pos, shroom.getDefaultState(), random);
		return true;
	}
	
}
