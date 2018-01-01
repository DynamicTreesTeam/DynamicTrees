package com.ferreusveritas.dynamictrees.systems.dropcreators;

import java.util.Random;

import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.treedata.IDropCreator;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CompatHelper;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class DropCreatorApple implements IDropCreator {

	public static final DropCreatorApple instance = new DropCreatorApple();
	
	@Override
	public ResourceLocation getName() {
		return new ResourceLocation(ModConstants.MODID, "apple");
	}
	
	@Override
	public ItemStack getHarvestDrop(World world, Species species, BlockPos leafPos, Random random, int soilLife, int fortune) {
		return CompatHelper.emptyStack();
	}

	@Override
	public ItemStack getVoluntaryDrop(World world, Species species, BlockPos rootPos, Random random, int soilLife) {
		return CompatHelper.emptyStack();
	}

	@Override
	public ItemStack getLeavesDrop(IBlockAccess access, Species species, BlockPos breakPos, Random random, int fortune) {
		//More fortune contrivances here.  Vanilla compatible returns.
		int chance = 200; //1 in 200 chance of returning an "apple"
		if (fortune > 0) {
			chance -= 10 << fortune;
			if (chance < 40) {
				chance = 40;
			}
		}
		
		return (random.nextInt(chance) == 0) ? new ItemStack(Items.APPLE, 1, 0) : CompatHelper.emptyStack();
	}

}
