package com.ferreusveritas.dynamictrees.compat;

import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import com.ferreusveritas.dynamictrees.blocks.BlockTrunkShell;

import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;

@WailaPlugin
public class WailaCompat implements IWailaPlugin {

	@Override
	public void register(IRegistrar registrar) {
		WailaBranchHandler branchHandler = new WailaBranchHandler();
		WailaRootyHandler rootyHandler = new WailaRootyHandler();

		registrar.registerComponentProvider(branchHandler, TooltipPosition.BODY, BlockBranch.class);
		registrar.registerComponentProvider(branchHandler, TooltipPosition.BODY, BlockTrunkShell.class);
		registrar.registerComponentProvider(rootyHandler, TooltipPosition.BODY, BlockRooty.class);
	}
}
