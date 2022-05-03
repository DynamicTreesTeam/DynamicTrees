package com.ferreusveritas.dynamictrees.compat.waila;

import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class WailaRootyHandler implements IComponentProvider {

    @Override
    public void appendBody(List<Component> tooltip, IDataAccessor accessor, IPluginConfig config) {
        final BlockState state = accessor.getWorld().getBlockState(accessor.getPosition());

        if (!(state.getBlock() instanceof RootyBlock)) {
            return;
        }

        final RootyBlock rooty = (RootyBlock) state.getBlock();
        final int fertility = rooty.getFertility(state, accessor.getWorld(), accessor.getPosition());
        tooltip.add(new TranslatableComponent("tooltip.dynamictrees.fertility",
                Mth.floor(fertility * 100 / 15f) + "%"));
    }
}
