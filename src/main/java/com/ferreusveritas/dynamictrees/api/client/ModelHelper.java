package com.ferreusveritas.dynamictrees.api.client;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ModelHelper {

    public static void regColorHandler(Block block, IBlockColor blockColor) {
        Minecraft.getInstance().getBlockColors().register(blockColor, block);
    }

    public static void regColorHandler(Item item, IItemColor itemColor) {
        Minecraft.getInstance().getItemColors().register(itemColor, new Item[]{item});
    }

}
