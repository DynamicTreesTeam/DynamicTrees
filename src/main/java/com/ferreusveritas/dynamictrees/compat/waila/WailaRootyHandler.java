package com.ferreusveritas.dynamictrees.compat.waila;

import com.ferreusveritas.dynamictrees.block.rooty.RootyBlock;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;

public class WailaRootyHandler implements IComponentProvider {

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlock() instanceof final RootyBlock rooty) {
            final int fertility = rooty.getFertility(accessor.getBlockState(), accessor.getLevel(), accessor.getPosition());
            tooltip.add(new TranslatableComponent("tooltip.dynamictrees.fertility", Mth.floor(fertility * 100 / 15f) + "%"));
        }
    }

}
