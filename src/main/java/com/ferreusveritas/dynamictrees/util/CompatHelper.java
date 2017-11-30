package com.ferreusveritas.dynamictrees.util;

import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.WorldDec;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class CompatHelper {
	
	public static boolean spawnEntity(World world, Entity entity) {
		return world.spawnEntityInWorld(entity);
	}

	public static boolean spawnEntity(WorldDec world, Entity entity) {
		return world.getWorld().spawnEntityInWorld(entity);
	}
	
    /**
     * Spawns the given ItemStack as an EntityItem into the World at the given position
     */
    public static void spawnItemStackAsEntity(World world, BlockPos pos, ItemStack stack)  {
        if (!world.isRemote && !world.restoringBlockSnapshots) {// do not drop items while restoring blockstates, prevents item dupe
            double x = (double)(world.rand.nextFloat() * 0.5F) + 0.25D;
            double y = (double)(world.rand.nextFloat() * 0.5F) + 0.25D;
            double z = (double)(world.rand.nextFloat() * 0.5F) + 0.25D;
            EntityItem entityitem = new EntityItem(world, (double)pos.getX() + x, (double)pos.getY() + y, (double)pos.getZ() + z, stack);
            entityitem.delayBeforeCanPickup = 10;
            world.spawnEntityInWorld(entityitem);
        }
    }

	
}
