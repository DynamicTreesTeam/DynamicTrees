package com.ferreusveritas.dynamictrees.util;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author Harley O'Connor
 */
public final class CommandUtils {

    public static void spawnItemStack (World world, BlockPos pos, ItemStack stack) {
        while (!world.isAirBlock(pos))
            pos = pos.up();

        EntityItem entityItem = new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        entityItem.motionX = 0;
        entityItem.motionY = 0;
        entityItem.motionZ = 0;
        
        world.spawnEntity(entityItem);
    }

}
