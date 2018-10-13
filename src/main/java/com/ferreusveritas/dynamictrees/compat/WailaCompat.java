package com.ferreusveritas.dynamictrees.compat;

import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import com.ferreusveritas.dynamictrees.blocks.BlockTrunkShell;

import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.WailaPlugin;

@WailaPlugin
public class WailaCompat implements IWailaPlugin {
	
	@Override
	public void register(IWailaRegistrar registrar) {
		WailaBranchHandler branchHandler = new WailaBranchHandler();
		WailaRootyHandler rootyHandler = new WailaRootyHandler();
		
		registrar.registerBodyProvider(branchHandler, BlockBranch.class);
		registrar.registerNBTProvider(branchHandler, BlockBranch.class);
		registrar.registerBodyProvider(branchHandler, BlockTrunkShell.class);
		registrar.registerBodyProvider(rootyHandler, BlockRooty.class);
	}
	
}
