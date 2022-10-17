package com.ferreusveritas.dynamictrees.compat.waila;

import com.ferreusveritas.dynamictrees.blocks.FruitBlock;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;

public class WailaFruitHandler implements IComponentProvider {

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlock() instanceof FruitBlock fruitBlock) {
            float ageAsPercentage = fruitBlock.getAgeAsPercentage(accessor.getBlockState());
            tooltip.add(new TranslatableComponent(
                    "tooltip.waila.crop_growth",
                    ageAsPercentage < 100F ? String.format("%.0f%%", ageAsPercentage) :
                            new TranslatableComponent("tooltip.waila.crop_mature").withStyle(ChatFormatting.GREEN)
            ));
        }
    }

}
