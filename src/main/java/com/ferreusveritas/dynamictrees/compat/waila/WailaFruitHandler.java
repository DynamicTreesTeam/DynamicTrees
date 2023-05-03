package com.ferreusveritas.dynamictrees.compat.waila;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.block.FruitBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class WailaFruitHandler implements IBlockComponentProvider {

    public static final ResourceLocation FRUIT_UID = new ResourceLocation(DynamicTrees.MOD_ID, "fruit");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlock() instanceof FruitBlock fruitBlock) {
            float ageAsPercentage = fruitBlock.getAgeAsPercentage(accessor.getBlockState());
            tooltip.add(Component.translatable(
                    "tooltip.waila.crop_growth",
                    ageAsPercentage < 100F ? String.format("%.0f%%", ageAsPercentage) :
                            Component.translatable("tooltip.waila.crop_mature").withStyle(ChatFormatting.GREEN)
            ));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return FRUIT_UID;
    }
}
