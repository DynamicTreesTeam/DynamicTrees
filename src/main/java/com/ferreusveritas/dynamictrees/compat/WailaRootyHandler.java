package com.ferreusveritas.dynamictrees.compat;

import java.util.List;

import com.ferreusveritas.dynamictrees.blocks.BlockRooty;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class WailaRootyHandler implements IWailaDataProvider {
	
	@Override
	public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
		IBlockState state = accessor.getWorld().getBlockState(accessor.getPosition());
		if(state.getBlock() instanceof BlockRooty) {
			BlockRooty rooty = (BlockRooty) state.getBlock();
			int life = rooty.getSoilLife(state, accessor.getWorld(), accessor.getPosition());
			tooltip.add("Soil Life: " + MathHelper.floor(life / 16.0) + "%");
		}
		
		return tooltip;
	}
}
