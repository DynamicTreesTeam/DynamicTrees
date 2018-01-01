package com.ferreusveritas.dynamictrees.systems.dropcreators;

import java.util.Random;

import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.treedata.IDropCreator;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CompatHelper;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class DropCreatorSeed implements IDropCreator {

	private final float rarity;
	
	public DropCreatorSeed() {
		this.rarity = 1.0f;
	}
	
	public DropCreatorSeed(float rarity) {
		this.rarity = rarity;
	}
	
	@Override
	public ResourceLocation getName() {
		return new ResourceLocation(ModConstants.MODID, "seed");
	}
	
	@Override
	public ItemStack getHarvestDrop(World world, Species species, BlockPos leafPos, Random random, int soilLife, int fortune) {
		return random.nextInt(64) == 0 ? species.getSeedStack(1) : CompatHelper.emptyStack();//1 in 64 chance to drop a seed on destruction..
	}

	@Override
	public ItemStack getVoluntaryDrop(World world, Species species, BlockPos rootPos, Random random, int soilLife) {
		return rarity * ModConfigs.seedDropRate > random.nextFloat() ? species.getSeedStack(1) : CompatHelper.emptyStack();
	}

	@Override
	public ItemStack getLeavesDrop(IBlockAccess access, Species species, BlockPos breakPos, Random random, int fortune) {		
		int chance = 20; //See BlockLeaves#getSaplingDropChance(state);
		//Hokey fortune stuff here to match Vanilla logic.
		if (fortune > 0) {
			chance -= 2 << fortune;
			if (chance < 10) { 
				chance = 10;
			}
		}
		return random.nextInt(chance) == 0 ? species.getSeedStack(1) : CompatHelper.emptyStack();
	}
		
}
