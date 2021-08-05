package com.ferreusveritas.dynamictrees.client;

import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.seasons.SeasonHelper;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

public class TooltipHandler {

	public static void setupTooltips(ItemTooltipEvent event) {

		ItemStack stack = event.getItemStack();
		Item item = stack.getItem();

		if (item instanceof Seed) {
			Seed seed = (Seed) item;

			EntityPlayer player = event.getEntityPlayer();
			if (player != null) {
				World world = player.world;
				if (SeasonHelper.getSeasonValue(world, BlockPos.ORIGIN) != null) {
					Species species = seed.getSpecies(stack);
					if (species == null || !species.isValid()) {
						return;
					}
					int flags = seed.getSpecies(stack).getSeasonalTooltipFlags(world.provider.getDimension());
					applySeasonalTooltips(event.getToolTip(), flags);
				}
			}
		}

	}

	public static void applySeasonalTooltips(List<String> tipList, int flags) {
		if (flags != 0) {
			tipList.add(getTranslationText("tooltip.seed.season.fertile_seasons"));

			if ((flags & 15) == 15) {
				tipList.add(getTranslationText("tooltip.seed.season.year_round"));
			} else {
				if ((flags & 1) != 0) {
					tipList.add(getTranslationText("tooltip.seed.season.spring"));
				}
				if ((flags & 2) != 0) {
					tipList.add(getTranslationText("tooltip.seed.season.summer"));
				}
				if ((flags & 4) != 0) {
					tipList.add(getTranslationText("tooltip.seed.season.autumn"));
				}
				if ((flags & 8) != 0) {
					tipList.add(getTranslationText("tooltip.seed.season.winter"));
				}
			}
		}
	}

	private static String getTranslationText(String path) {
		return new TextComponentTranslation(path).getUnformattedComponentText();
	}

}
