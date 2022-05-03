package com.ferreusveritas.dynamictrees.api.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ModelHelper {

    public static void regColorHandler(Block block, BlockColor blockColor) {
        Minecraft.getInstance().getBlockColors().register(blockColor, block);
    }

    public static void regColorHandler(Item item, ItemColor itemColor) {
        Minecraft.getInstance().getItemColors().register(itemColor, new Item[]{item});
    }

}
