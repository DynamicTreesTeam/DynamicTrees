package com.ferreusveritas.dynamictrees.event;

import java.util.Iterator;

import com.ferreusveritas.dynamictrees.ModConfigs;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DropEventHandler {
	
	@SubscribeEvent
	public void onHarvestDropsEvent(BlockEvent.HarvestDropsEvent event) {

		if(ModConfigs.worldGen && ModConfigs.enableAppleTrees) {
			if(event.getState().getBlock() == Blocks.LEAVES || event.getState().getBlock() == Blocks.LEAVES2) {
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
