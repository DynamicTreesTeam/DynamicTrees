package com.ferreusveritas.growingtrees.trees;

import java.util.ArrayList;
import java.util.Random;

import com.ferreusveritas.growingtrees.TreeHelper;
import com.ferreusveritas.growingtrees.special.BottomListenerPodzol;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary.Type;

public class TreeOak extends GrowingTree {

	public TreeOak(int seq) {
		super("oak", seq);

		//Oak trees are about as average as you can get
		setBasicGrowingParameters(0.3f, 12.0f, upProbability, lowestBranchHeight, 0.8f);

		setPrimitiveLeaves(Blocks.leaves, 0);
		setPrimitiveLog(Blocks.log, 0);
		setPrimitiveSapling(Blocks.sapling, 0);

		envFactor(Type.COLD, 0.75f);
		envFactor(Type.HOT, 0.50f);
		envFactor(Type.DRY, 0.50f);
		envFactor(Type.FOREST, 1.05f);
		
		registerBottomSpecials(new BottomListenerPodzol());
	}

	@Override
	public boolean isBiomePerfect(BiomeGenBase biome) {
		return isOneOfBiomes(biome, BiomeGenBase.forest, BiomeGenBase.forestHills);
	}

	@Override
	public boolean rot(World world, int x, int y, int z, int neighborCount, int radius, Random random) {
		if(super.rot(world, x, y, z, neighborCount, radius, random)) {
			if(radius > 4 && TreeHelper.isRootyDirt(world, x, y - 1, z) && world.getSavedLightValue(EnumSkyBlock.Sky, x, y, z) < 4) {
				world.setBlock(x, y, z, random.nextInt(3) == 0 ? Blocks.redstone_block : Blocks.brown_mushroom);//Change branch to a mushroom
				world.setBlock(x, y - 1, z, Blocks.dirt, 2, 3);//Change rooty dirt to Podzol
			}
			return true;
		}

		return false;
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int chance, ArrayList<ItemStack> drops) {
		if ((world.rand.nextInt(chance) == 0)) {
			drops.add(new ItemStack(Items.apple, 1, 0));
		}
		return drops;
	}

}
