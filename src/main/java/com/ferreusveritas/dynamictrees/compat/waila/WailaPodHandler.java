package com.ferreusveritas.dynamictrees.compat.waila;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.blocks.PodBlock;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class WailaPodHandler implements IComponentProvider {

    /* Used to switch off component for cocoa, since Jade already supports this. */
    public static final ResourceLocation COCOA = DynamicTrees.resLoc("cocoa");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlock() instanceof PodBlock podBlock && !accessor.getBlock().getRegistryName().equals(COCOA)) {
            float ageAsPercentage = podBlock.getAgeAsPercentage(accessor.getBlockState());
            tooltip.add(new TranslatableComponent(
                    "tooltip.waila.crop_growth",
                    ageAsPercentage < 100F ? String.format("%.0f%%", ageAsPercentage) :
                            new TranslatableComponent("tooltip.waila.crop_mature").withStyle(ChatFormatting.GREEN)
            ));
        }
    }

}