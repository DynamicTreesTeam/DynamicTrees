package com.ferreusveritas.dynamictrees.compat.waila;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class WailaRootyWaterHandler implements IComponentProvider {

    @Override
    public void appendHead(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
        tooltip.add(new StringTextComponent(TextFormatting.WHITE + new TranslationTextComponent(accessor.getBlock().getDescriptionId()).getString()));
    }

}
