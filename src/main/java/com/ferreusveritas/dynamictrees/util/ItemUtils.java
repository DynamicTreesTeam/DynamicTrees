package com.ferreusveritas.dynamictrees.util;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author Harley O'Connor
 */
public final class ItemUtils {

	/**
	 * Spawns an {@link ItemStack} as an {@link EntityItem} in the {@link World} at the
	 * {@link BlockPos} given.
	 *
	 * @param world The {@link World} object to spawn the item in.
	 * @param pos The {@link BlockPos} object to spawn the item at.
	 * @param stack The {@link ItemStack} to spawn.
	 */
	public static void spawnItemStack (World world, BlockPos pos, ItemStack stack) {
		spawnItemStack(world, pos, stack, false);
	}

	/**
	 * Spawns an {@link ItemStack} as an {@link EntityItem} in the {@link World} at the
	 * {@link BlockPos} given.
	 *
	 * @param world The {@link World} object to spawn the item in.
	 * @param pos The {@link BlockPos} object to spawn the item at.
	 * @param stack The {@link ItemStack} to spawn.
	 * @param searchForAir If true, searches for air for the item to spawn in.
	 */
	public static void spawnItemStack (World world, BlockPos pos, ItemStack stack, boolean searchForAir) {
		if (searchForAir) {
			// Goes up one block at a time until an air block to spawn on is found.
			while (!world.isAirBlock(pos))
				pos = pos.up();
		}

        EntityItem entityItem = new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        entityItem.motionX = entityItem.motionY = entityItem.motionZ = 0;
        
        world.spawnEntity(entityItem);
    }

}
