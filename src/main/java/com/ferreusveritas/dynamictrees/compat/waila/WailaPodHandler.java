package com.ferreusveritas.dynamictrees.compat.waila;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.block.PodBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class WailaPodHandler implements IBlockComponentProvider {
    private static final ResourceLocation POD_UID = new ResourceLocation(DynamicTrees.MOD_ID, "pod");

    /* Used to switch off component for cocoa, since Jade already supports this. */
    public static final ResourceLocation COCOA = DynamicTrees.location("cocoa");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlock() instanceof PodBlock podBlock && !ForgeRegistries.BLOCKS.getKey(accessor.getBlock()).equals(COCOA)) {
            float ageAsPercentage = podBlock.getAgeAsPercentage(accessor.getBlockState());
            tooltip.add(Component.translatable(
                    "tooltip.waila.crop_growth",
                    ageAsPercentage < 100F ? String.format("%.0f%%", ageAsPercentage) :
                            Component.translatable("tooltip.waila.crop_mature").withStyle(ChatFormatting.GREEN)
            ));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return POD_UID;
    }
}