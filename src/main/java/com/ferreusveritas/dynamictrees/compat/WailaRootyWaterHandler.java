package com.ferreusveritas.dynamictrees.compat;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.util.text.*;

import java.util.List;

public class WailaRootyWaterHandler implements IComponentProvider {

	@Override
	public void appendHead(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
		tooltip.add(new StringTextComponent(TextFormatting.WHITE + new TranslationTextComponent(accessor.getBlock().getTranslationKey()).getString()));
	}

}
