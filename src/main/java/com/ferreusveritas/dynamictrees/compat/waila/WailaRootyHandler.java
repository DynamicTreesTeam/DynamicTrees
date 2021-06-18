package com.ferreusveritas.dynamictrees.compat.waila;

import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class WailaRootyHandler implements IComponentProvider {

	@Override
	public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
		final BlockState state = accessor.getWorld().getBlockState(accessor.getPosition());

		if (!(state.getBlock() instanceof RootyBlock))
			return;

		final RootyBlock rooty = (RootyBlock) state.getBlock();
		final int fertility = rooty.getFertility(state, accessor.getWorld(), accessor.getPosition());
		tooltip.add(new TranslationTextComponent("tooltip.dynamictrees.fertility",
				MathHelper.floor(fertility * 100 / 15f) + "%"));
	}
}
