package com.ferreusveritas.dynamictrees.compat;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockRooty;

import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.WailaPlugin;

@WailaPlugin
public class WailaCompat implements IWailaPlugin {

	@Override
	public void register(IWailaRegistrar registrar) {
		BlockBranch oak = TreeRegistry.findSpeciesSloppy("oak").getFamily().getDynamicBranch();
		registrar.registerBodyProvider(oak, BlockBranch.class);
		registrar.registerNBTProvider(oak, BlockBranch.class);
		registrar.registerBodyProvider(ModBlocks.blockRootyDirt, BlockRooty.class);
	}
	
}
