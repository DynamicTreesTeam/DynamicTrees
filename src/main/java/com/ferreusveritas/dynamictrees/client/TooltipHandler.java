package com.ferreusveritas.dynamictrees.client;

import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.seasons.SeasonHelper;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

public class TooltipHandler {
	
	public static void setupTooltips(ItemTooltipEvent event) {
		
		ItemStack stack = event.getItemStack();
		Item item = stack.getItem();
		
		if(item instanceof Seed) {
			Seed seed = (Seed) item;
			
			EntityPlayer player = event.getEntityPlayer();
			if(player != null) {
				World world = player.world;
				if(SeasonHelper.getSeasonValue(world) != null) {
					int flags = seed.getSpecies(stack).getSeasonalTooltipFlags(world.provider.getDimension());
					
					if(flags != 0) {
						event.getToolTip().add("Fertile Seasons:");
						
						if((flags & 15) == 15) {
							event.getToolTip().add(TextFormatting.LIGHT_PURPLE + " Year-Round");
						} else {
							if ((flags & 1) != 0) {
								event.getToolTip().add(TextFormatting.GREEN + " Spring");
							}
							if ((flags & 2) != 0) {
								event.getToolTip().add(TextFormatting.YELLOW + " Summer");
							}
							if ((flags & 4) != 0) {
								event.getToolTip().add(TextFormatting.GOLD + " Autumn");
							}
							if ((flags & 8) != 0) {
								event.getToolTip().add(TextFormatting.AQUA + " Winter");
							}
						}
					}
				}
			}
		}
		
	}
	
}
