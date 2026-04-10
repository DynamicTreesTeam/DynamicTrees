package com.dtteam.dynamictrees.compat.waila;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.block.pod.PodBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class WailaPodHandler implements IBlockComponentProvider {

    private static final Identifier POD_UID = DynamicTrees.location("pod");

    /* Used to switch off component for cocoa, since Jade already supports this. */
    public static final Identifier COCOA = DynamicTrees.location("cocoa");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlock() instanceof PodBlock podBlock && !BuiltInRegistries.BLOCK.getKey(accessor.getBlock()).equals(COCOA)) {
            float ageAsPercentage = podBlock.getAgeAsPercentage(accessor.getBlockState());
            tooltip.add(Component.translatable(
                    "tooltip.jade.crop_growth",
                    ageAsPercentage < 100F ? String.format("%.0f%%", ageAsPercentage) :
                            Component.translatable("tooltip.jade.crop_mature").withStyle(ChatFormatting.GREEN)
            ));
        }
    }

    @Override
    public Identifier getUid() {
        return POD_UID;
    }
}