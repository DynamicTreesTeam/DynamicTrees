package com.ferreusveritas.dynamictrees.compat.waila;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.block.rooty.RootyBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class WailaRootyHandler implements IBlockComponentProvider {

    public static final ResourceLocation ROOTY_UID = new ResourceLocation(DynamicTrees.MOD_ID, "rooty");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlock() instanceof final RootyBlock rooty) {
            final int fertility = rooty.getFertility(accessor.getBlockState(), accessor.getLevel(), accessor.getPosition());
            tooltip.add(Component.translatable("tooltip.dynamictrees.fertility", Mth.floor(fertility * 100 / 15f) + "%"));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ROOTY_UID;
    }
}
