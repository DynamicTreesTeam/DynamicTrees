package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.init.DTConfigs;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Iterator;

public class DropEventHandler {
	
	@SubscribeEvent
	public void onHarvestDropsEvent(BlockEvent.HarvestDropsEvent event) {

		if(DTConfigs.worldGen.get() && DTConfigs.enableAppleTrees.get()) {
			if(event.getState().getBlock() instanceof LeavesBlock) {
				Iterator<ItemStack> iter = event.getDrops().iterator();
				while(iter.hasNext()) {
					ItemStack stack = iter.next();
					if(stack.getItem() == Items.APPLE) {
						iter.remove();
					}
				}
			}
		}
	}

	
}
