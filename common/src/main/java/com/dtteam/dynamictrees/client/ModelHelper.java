package com.dtteam.dynamictrees.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModelHelper {

    public static void regColorHandler(Block block, BlockColor blockColor) {
        Minecraft.getInstance().getBlockColors().register(blockColor, block);
    }

    public static void regColorHandler(Item item, ItemColor itemColor) {
//        Minecraft.getInstance().getItemColors().register(itemColor, new Item[]{item});
    }
}
