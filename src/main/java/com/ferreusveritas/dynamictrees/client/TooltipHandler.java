package com.ferreusveritas.dynamictrees.client;

import com.ferreusveritas.dynamictrees.compat.seasons.SeasonHelper;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

public class TooltipHandler {

    public static void setupTooltips(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();

        if (!(item instanceof Seed)) {
            return;
        }

        Seed seed = (Seed) item;

        Player player = event.getEntity();
        if (player == null || player.level == null || SeasonHelper.getSeasonValue(player.level, BlockPos.ZERO) == null) {
            return;
        }

        Species species = seed.getSpecies();
        if (species == null || !species.isValid()) {
            return;
        }

        int flags = seed.getSpecies().getSeasonalTooltipFlags(player.level);
        applySeasonalTooltips(event.getToolTip(), flags);
    }

    public static void applySeasonalTooltips(List<Component> tipList, int flags) {
        if (flags != 0) {
            tipList.add(Component.translatable("desc.sereneseasons.fertile_seasons").append(":"));

            if ((flags & 15) == 15) {
                tipList.add(Component.literal(" ").append(Component.translatable("desc.sereneseasons.year_round").withStyle(ChatFormatting.LIGHT_PURPLE)));
            } else {
                if ((flags & 1) != 0) {
                    tipList.add(Component.literal(" ").append(Component.translatable("desc.sereneseasons.spring").withStyle(ChatFormatting.GREEN)));
                }
                if ((flags & 2) != 0) {
                    tipList.add(Component.literal(" ").append(Component.translatable("desc.sereneseasons.summer").withStyle(ChatFormatting.YELLOW)));
                }
                if ((flags & 4) != 0) {
                    tipList.add(Component.literal(" ").append(Component.translatable("desc.sereneseasons.autumn").withStyle(ChatFormatting.GOLD)));
                }
                if ((flags & 8) != 0) {
                    tipList.add(Component.literal(" ").append(Component.translatable("desc.sereneseasons.winter").withStyle(ChatFormatting.AQUA)));
                }
            }
        }
    }

}
