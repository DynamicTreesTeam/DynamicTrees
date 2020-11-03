package com.ferreusveritas.dynamictrees.compat;

import java.util.List;

import com.ferreusveritas.dynamictrees.blocks.BlockRooty;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class WailaRootyHandler implements IComponentProvider {

	@Override
	public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
		BlockState state = accessor.getWorld().getBlockState(accessor.getPosition());
		if(state.getBlock() instanceof BlockRooty) {
			BlockRooty rooty = (BlockRooty) state.getBlock();
			int life = rooty.getSoilLife(state, accessor.getWorld(), accessor.getPosition());
			tooltip.add(new StringTextComponent("Soil Life: " + MathHelper.floor(life * 100 / 15f) + "%"));
		}
	}
}
