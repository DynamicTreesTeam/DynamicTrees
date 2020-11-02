package com.ferreusveritas.dynamictrees.client;

import java.util.List;

import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.seasons.SeasonHelper;

import com.ferreusveritas.dynamictrees.trees.Species;
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
					Species species = seed.getSpecies(stack);
					if (species == null || !species.isValid()) return;
					int flags = seed.getSpecies(stack).getSeasonalTooltipFlags(world.provider.getDimension());
					applySeasonalTooltips(event.getToolTip(), flags);
				}
			}
		}
		
	}
	
	public static void applySeasonalTooltips(List<String> tipList, int flags) {
		if(flags != 0) {
			tipList.add("Fertile Seasons:");
			
			if((flags & 15) == 15) {
				tipList.add(TextFormatting.LIGHT_PURPLE + " Year-Round");
			} else {
				if ((flags & 1) != 0) {
					tipList.add(TextFormatting.GREEN + " Spring");
				}
				if ((flags & 2) != 0) {
					tipList.add(TextFormatting.YELLOW + " Summer");
				}
				if ((flags & 4) != 0) {
					tipList.add(TextFormatting.GOLD + " Autumn");
				}
				if ((flags & 8) != 0) {
					tipList.add(TextFormatting.AQUA + " Winter");
				}
			}
		}
	}
	
}
