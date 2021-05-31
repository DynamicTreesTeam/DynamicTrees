package com.ferreusveritas.dynamictrees.compat.waila;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class WailaCocoaHandler implements IComponentProvider {

	@Override
	public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
		float growthValue = accessor.getBlockState().getValue(BlockStateProperties.AGE_2) / 2.0F * 100.0F;
		if (growthValue < 100.0F)
			tooltip.add(new TranslationTextComponent("tooltip.waila.crop_growth", String.format("%.0f%%", growthValue)));
		else
			tooltip.add(new TranslationTextComponent("tooltip.waila.crop_growth", new TranslationTextComponent("tooltip.waila.crop_mature")));
	}
}
