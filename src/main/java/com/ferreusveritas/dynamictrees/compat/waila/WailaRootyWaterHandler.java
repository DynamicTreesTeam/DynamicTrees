package com.ferreusveritas.dynamictrees.compat.waila;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class WailaRootyWaterHandler implements IBlockComponentProvider {

    public static final ResourceLocation ROOTY_WATER_UID = new ResourceLocation(DynamicTrees.MOD_ID, "rooty_water");
    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        tooltip.add(Component.literal(ChatFormatting.WHITE + Component.translatable(accessor.getBlock().getDescriptionId()).getString()));
    }

    @Override
    public ResourceLocation getUid() {
        return ROOTY_WATER_UID;
    }
}
