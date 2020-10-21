package com.ferreusveritas.dynamictrees.client;

import com.ferreusveritas.dynamictrees.blocks.BlockFruit;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.seasons.SeasonHelper;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

public class TooltipHandler {
	
	private static final float seasonStart = 0.167f;
	private static final float seasonEnd = 0.833f;
	private static final float threshold = 0.75f;
	
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
					if(BlockFruit.getFruitBlockForSpecies(species) != null) {
						int dim = player.world.provider.getDimension();
						int mask = 0;
						
						for(int i = 0; i < 4; i++) {
							float prod1 = species.seasonalFruitProductionFactor(null, new BlockPos(dim, (int)((i + seasonStart) * 64.0f), 0));
							float prod2 = species.seasonalFruitProductionFactor(null, new BlockPos(dim, (int)((i + seasonEnd  ) * 64.0f), 0));
							if(Math.min(prod1, prod2) >= threshold) {
								mask |= 1 << i;
							}
						}
						
						if(mask != 0) {
							event.getToolTip().add("Fertile Seasons:");
						}
						
						if(mask == 15) {
							event.getToolTip().add(TextFormatting.LIGHT_PURPLE + " Year-Round");
						} else {
							if ((mask & 1) != 0) {
								event.getToolTip().add(TextFormatting.GREEN + " Spring");
							}
							if ((mask & 2) != 0) {
								event.getToolTip().add(TextFormatting.YELLOW + " Summer");
							}
							if ((mask & 4) != 0) {
								event.getToolTip().add(TextFormatting.GOLD + " Autumn");
							}
							if ((mask & 8) != 0) {
								event.getToolTip().add(TextFormatting.AQUA + " Winter");
							}
						}
					}
				}
			}
		}
		
	}
	
}
