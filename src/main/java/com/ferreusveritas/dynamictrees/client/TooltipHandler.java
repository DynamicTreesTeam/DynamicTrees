package com.ferreusveritas.dynamictrees.client;

import com.ferreusveritas.dynamictrees.compat.seasons.SeasonHelper;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

public class TooltipHandler {
	
	public static void setupTooltips(ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		Item item = stack.getItem();
		
		if(!(item instanceof Seed))
			return;

		Seed seed = (Seed) item;

		PlayerEntity player = event.getPlayer();
		if(player == null || player.world == null || SeasonHelper.getSeasonValue(player.world, BlockPos.ZERO) == null)
			return;

		Species species = seed.getSpecies();
		if (species == null || !species.isValid())
			return;

		int flags = seed.getSpecies().getSeasonalTooltipFlags(player.world);
		applySeasonalTooltips(event.getToolTip(), flags);
	}
	
	public static void applySeasonalTooltips(List<ITextComponent> tipList, int flags) {
		if (flags != 0) {
			tipList.add(new TranslationTextComponent("desc.sereneseasons.fertile_seasons").appendString(":"));
			
			if ((flags & 15) == 15) {
				tipList.add(new StringTextComponent(" ").appendSibling(new TranslationTextComponent("desc.sereneseasons.year_round").mergeStyle(TextFormatting.LIGHT_PURPLE)));
			} else {
				if ((flags & 1) != 0) {
					tipList.add(new StringTextComponent(" ").appendSibling(new TranslationTextComponent("desc.sereneseasons.spring").mergeStyle(TextFormatting.GREEN)));
				}
				if ((flags & 2) != 0) {
					tipList.add(new StringTextComponent(" ").appendSibling(new TranslationTextComponent("desc.sereneseasons.summer").mergeStyle(TextFormatting.YELLOW)));
				}
				if ((flags & 4) != 0) {
					tipList.add(new StringTextComponent(" ").appendSibling(new TranslationTextComponent("desc.sereneseasons.autumn").mergeStyle(TextFormatting.GOLD)));
				}
				if ((flags & 8) != 0) {
					tipList.add(new StringTextComponent(" ").appendSibling(new TranslationTextComponent("desc.sereneseasons.winter").mergeStyle(TextFormatting.AQUA)));
				}
			}
		}
	}

}
