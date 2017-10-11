package com.ferreusveritas.dynamictrees.special;

import java.util.Random;

import com.ferreusveritas.dynamictrees.api.IBottomListener;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class BottomListenerDropItems implements IBottomListener {

	private ItemStack toDrop;
	public float chance;
	public boolean onlyEdge;

	public BottomListenerDropItems(ItemStack itemStack, float dropChance, boolean edge) {
		toDrop = itemStack;
		chance = dropChance;
		onlyEdge = edge;
	}

	@Override
	public void run(World world, DynamicTree tree, BlockPos pos, Random random) {
		if (!world.isRemote && !world.restoringBlockSnapshots) { // do not drop items while restoring blockstates, prevents item dupe
			//Don't spawn drops if this block is in a chunk that is next to an unloaded chunk
			if(!TreeHelper.isSurroundedByExistingChunks(world, pos)) {
				return;
			}

			//Spawn seed
			if(!onlyEdge || tree.getGrowingLeaves().getHydrationLevel(world, pos) == 1) {
				EntityItem itemEntity = new EntityItem(world, pos.getX(), pos.getY() - 1, pos.getZ(), toDrop.copy());
				world.spawnEntityInWorld(itemEntity);
			}
		}
	}

	@Override
	public float chance() {
		return chance;
	}

	@Override
	public String getName() {
		return "dropitem";
	}

}
