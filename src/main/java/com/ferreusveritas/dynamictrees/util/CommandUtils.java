package com.ferreusveritas.dynamictrees.util;

import net.minecraft.entity.item.ItemEntity;
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

        ItemEntity entityItem = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        entityItem.setMotion(0, 0, 0);
        world.addEntity(entityItem);
    }

}
