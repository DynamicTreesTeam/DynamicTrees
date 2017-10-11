package com.ferreusveritas.dynamictrees.trees;

import java.util.Random;

import com.ferreusveritas.dynamictrees.VanillaTreeData;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.init.Blocks;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary.Type;

public class TreeBirch extends DynamicTree {

	public TreeBirch() {
		super(VanillaTreeData.EnumType.BIRCH);

		//Birch are tall, skinny, fast growing trees
		setBasicGrowingParameters(0.1f, 14.0f, 4, 4, 1.25f);

		setRetries(1);//Special fast growing

		envFactor(Type.COLD, 0.75f);
		envFactor(Type.HOT, 0.50f);
		envFactor(Type.DRY, 0.50f);
		envFactor(Type.FOREST, 1.05f);
	}

	@Override
	public boolean isBiomePerfect(BiomeGenBase biome) {
		return isOneOfBiomes(biome, BiomeGenBase.birchForest, BiomeGenBase.birchForestHills);
	};

	@Override
	public boolean rot(World world, BlockPos pos, int neighborCount, int radius, Random random) {
		if(super.rot(world, pos, neighborCount, radius, random)) {
			if(radius > 4 && TreeHelper.isRootyDirt(world, pos.down()) && world.getSavedLightValue(EnumSkyBlock.Sky, pos.getX(), pos.getY(), pos.getZ()) < 4) {
				world.setBlock(pos.getX(), pos.getY(), pos.getZ(), Blocks.brown_mushroom);//Change branch to a brown mushroom
				world.setBlock(pos.getX(), pos.getY() - 1, pos.getZ(), Blocks.dirt, 0, 3);//Change rooty dirt to dirt
			}
			return true;
		}

		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int foliageColorMultiplier(IBlockAccess blockAccess, int x, int y, int z) {
		return ColorizerFoliage.getFoliageColorBirch();
	}
}
