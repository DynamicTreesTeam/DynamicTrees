package com.ferreusveritas.dynamictrees.systems.dropcreators;

import java.util.List;
import java.util.Random;

import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.treedata.IDropCreator;
import com.ferreusveritas.dynamictrees.trees.Species;

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
	public List<ItemStack> getHarvestDrop(World world, Species species, BlockPos leafPos, Random random, List<ItemStack> dropList, int soilLife, int fortune) {
		return dropList;
	}

	@Override
	public List<ItemStack> getVoluntaryDrop(World world, Species species, BlockPos rootPos, Random random, List<ItemStack> dropList, int soilLife) {
		return dropList;
	}

	@Override
	public List<ItemStack> getLeavesDrop(IBlockAccess access, Species species, BlockPos breakPos, Random random, List<ItemStack> dropList, int fortune) {
		//More fortune contrivances here.  Vanilla compatible returns.
		int chance = 200; //1 in 200 chance of returning an "apple"
		if (fortune > 0) {
			chance -= 10 << fortune;
			if (chance < 40) {
				chance = 40;
			}
		}
		
		if(random.nextInt(chance) == 0) {
			dropList.add(new ItemStack(Items.APPLE));
		}
		return dropList;
	}

	@Override
	public List<ItemStack> getLogsDrop(World world, Species species, BlockPos breakPos, Random random, List<ItemStack> dropList, int volume) {
		return dropList;
	}

}
