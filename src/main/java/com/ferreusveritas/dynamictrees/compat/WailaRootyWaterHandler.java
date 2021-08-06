package com.ferreusveritas.dynamictrees.compat;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.google.common.base.Strings;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.config.FormattingConfig;
import mcp.mobius.waila.utils.ModIdentification;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public class WailaRootyWaterHandler implements IWailaDataProvider {

	@Nonnull
	@Override
	public List<String> getWailaHead(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
		tooltip.add(String.format(FormattingConfig.blockFormat, accessor.getBlock().getLocalizedName()));
		return tooltip;
	}

	@Nonnull
	@Override
	public List<String> getWailaTail(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
		IBlockState upState = accessor.getWorld().getBlockState(accessor.getPosition().up());
		if (TreeHelper.isBranch(upState)) {
			String modName = ModIdentification.nameFromStack(new ItemStack(upState.getBlock()));
			if (!Strings.isNullOrEmpty(FormattingConfig.modNameFormat)) {
				tooltip.add(String.format(FormattingConfig.modNameFormat, modName));
			}
		}


		return tooltip;
	}

}
